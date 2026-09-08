import { createServer as httpServer } from 'node:http';
import { createHash, randomUUID, timingSafeEqual } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { pathToFileURL } from 'node:url';

const assets = new URL('../vaeloria-android/app/src/main/assets/ai/', import.meta.url);
const instructions = readFileSync(new URL('narration-policy.txt', assets), 'utf8');
const schema = JSON.parse(readFileSync(new URL('narration-schema.json', assets), 'utf8'));
const OPENAI_ENDPOINT = 'https://api.openai.com/v1/responses';
const MAX_BODY = 65536;
const hash = value => createHash('sha256').update(value).digest();

class HttpError extends Error {
  constructor(status, code) { super(code); this.status = status; this.code = code; }
}

export function configFromEnv(env = process.env) {
  const apiKey = env.OPENAI_API_KEY?.trim();
  const tokens = (env.VAELORIA_CLIENT_TOKENS || '').split(',').map(s => s.trim()).filter(Boolean);
  if (!apiKey || apiKey.length < 20) throw new Error('Nustatyk serverio OPENAI_API_KEY.');
  if (!tokens.length || tokens.length > 100 || tokens.some(t => !/^[A-Za-z0-9_-]{32,200}$/.test(t) || t.startsWith('sk-'))
      || new Set(tokens).size !== tokens.length) throw new Error('Nustatyk unikalius VAELORIA_CLIENT_TOKENS (32–200 simbolių).');
  const model = env.OPENAI_MODEL || 'gpt-6-astra';
  if (!/^[A-Za-z0-9_.-]{1,100}$/.test(model)) throw new Error('Netinkamas OPENAI_MODEL.');
  return { apiKey, tokens, model, upstreamTimeoutMs: 35000, perMinute: 8, perDay: 100, maxConcurrent: 4 };
}

export function validateNarration(value) {
  const fields = ['scene_title', 'scene', 'choices'];
  const validText = (text, max) => typeof text === 'string' && text.trim().length > 0 && text.length <= max;
  if (!value || typeof value !== 'object' || Array.isArray(value) || Object.keys(value).length !== 3
      || Object.keys(value).some(key => !fields.includes(key)) || !validText(value.scene_title, 100)
      || !validText(value.scene, 1000) || !Array.isArray(value.choices) || value.choices.length !== 3
      || value.choices.some(c => !validText(c, 150))
      || new Set(value.choices.map(c => c.trim().toLocaleLowerCase('lt-LT'))).size !== 3) {
    throw new HttpError(502, 'invalid_narration');
  }
  return value;
}

export function extractNarration(response) {
  if (response.status !== 'completed' || !Array.isArray(response.output)) throw new HttpError(502, 'incomplete_response');
  const text = [];
  for (const item of response.output) {
    if (item.type !== 'message' || item.role !== 'assistant') continue;
    if (item.status && item.status !== 'completed') throw new HttpError(502, 'incomplete_response');
    for (const content of item.content || []) {
      if (content.type === 'refusal') throw new HttpError(422, 'narration_refused');
      if (content.type === 'output_text' && typeof content.text === 'string') text.push(content.text);
    }
  }
  try { return validateNarration(JSON.parse(text.join(''))); }
  catch (error) { if (error instanceof HttpError) throw error; throw new HttpError(502, 'invalid_narration'); }
}

function readInput(req) {
  if (!/^application\/json(?:\s*;|$)/i.test(req.headers['content-type'] || '')) throw new HttpError(415, 'json_required');
  if (req.headers['content-encoding'] && req.headers['content-encoding'] !== 'identity') throw new HttpError(415, 'encoding_unsupported');
  if (Number(req.headers['content-length'] || 0) > MAX_BODY) throw new HttpError(413, 'input_too_large');
  return new Promise((resolve, reject) => {
    let size = 0; const chunks = [];
    req.on('data', chunk => {
      size += chunk.length;
      if (size > MAX_BODY) { req.pause(); reject(new HttpError(413, 'input_too_large')); return; }
      chunks.push(chunk);
    });
    req.on('error', () => reject(new HttpError(400, 'invalid_request')));
    req.on('aborted', () => reject(new HttpError(400, 'request_cancelled')));
    req.on('end', () => {
      try {
        const body = JSON.parse(Buffer.concat(chunks).toString('utf8'));
        if (!body || typeof body !== 'object' || Object.keys(body).length !== 1 || typeof body.context !== 'string'
            || !body.context.trim() || body.context.length > 24000) throw new Error();
        resolve(body.context);
      } catch { reject(new HttpError(400, 'invalid_context')); }
    });
  });
}

async function readUpstream(response) {
  let size = 0; const parts = [];
  for await (const part of response.body) {
    size += part.length;
    if (size > 131072) throw new HttpError(502, 'response_too_large');
    parts.push(Buffer.from(part));
  }
  try { return JSON.parse(Buffer.concat(parts).toString('utf8')); }
  catch { throw new HttpError(502, 'invalid_upstream_response'); }
}

export function createApp({ config = configFromEnv(), fetchImpl = fetch, now = Date.now, log = () => {} } = {}) {
  // Only configured token hashes are retained; arbitrary tokens cannot grow this map.
  const clients = config.tokens.map(token => ({ digest: hash(token), minute: 0, minuteCount: 0, day: 0, dayCount: 0, active: false }));
  let active = 0;
  const server = httpServer(async (req, res) => {
    const requestId = randomUUID(), started = now();
    res.setHeader('Content-Type', 'application/json; charset=utf-8');
    res.setHeader('Cache-Control', 'no-store');
    res.setHeader('X-Content-Type-Options', 'nosniff');
    res.setHeader('X-Request-Id', requestId);
    function send(status, value) {
      if (res.destroyed) return;
      res.statusCode = status;
      if (status >= 400) res.setHeader('Connection', 'close');
      res.end(JSON.stringify(value));
    }
    let client, counted = false, timer, providerRequestId;
    const controller = new AbortController();
    res.on('close', () => { if (!res.writableFinished) controller.abort(); });
    try {
      if (req.method === 'GET' && req.url === '/health') { send(200, { status: 'ok' }); return; }
      if (req.method !== 'POST' || req.url !== '/v1/turn') throw new HttpError(404, 'not_found');
      const token = /^Bearer ([A-Za-z0-9_-]{32,200})$/.exec(req.headers.authorization || '')?.[1];
      if (!token) throw new HttpError(401, 'unauthorized');
      const digest = hash(token);
      client = clients.find(c => timingSafeEqual(c.digest, digest));
      if (!client) throw new HttpError(401, 'unauthorized');
      const context = await readInput(req);
      if (res.destroyed) return;
      const minute = Math.floor(now() / 60000), day = Math.floor(now() / 86400000);
      if (client.minute !== minute) { client.minute = minute; client.minuteCount = 0; }
      if (client.day !== day) { client.day = day; client.dayCount = 0; }
      if (client.active || client.minuteCount >= config.perMinute || client.dayCount >= config.perDay) {
        const seconds = client.dayCount >= config.perDay ? 86400 - Math.floor(now() / 1000) % 86400 : 60 - Math.floor(now() / 1000) % 60;
        res.setHeader('Retry-After', String(seconds)); throw new HttpError(429, 'rate_limited');
      }
      if (active >= config.maxConcurrent) throw new HttpError(503, 'service_busy');
      client.minuteCount++; client.dayCount++; client.active = true; active++; counted = true;
      timer = setTimeout(() => controller.abort(), config.upstreamTimeoutMs);
      const upstream = await fetchImpl(OPENAI_ENDPOINT, {
        method: 'POST', redirect: 'error', signal: controller.signal,
        headers: { Authorization: `Bearer ${config.apiKey}`, 'Content-Type': 'application/json', 'X-Client-Request-Id': requestId },
        body: JSON.stringify({ model: config.model, store: false, instructions,
          input: [{ role: 'user', content: context }], reasoning: { effort: 'low' }, max_output_tokens: 4096,
          text: { format: { type: 'json_schema', name: 'vaeloria_narration', strict: true, schema } } })
      });
      providerRequestId = upstream.headers.get('x-request-id') || undefined;
      if (!upstream.ok) {
        await upstream.body?.cancel();
        throw new HttpError(upstream.status === 429 ? 429 : 502, upstream.status === 429 ? 'provider_rate_limited' : 'provider_error');
      }
      const narration = extractNarration(await readUpstream(upstream));
      send(200, { narration, request_id: requestId });
    } catch (error) {
      const known = error instanceof HttpError;
      send(known ? error.status : controller.signal.aborted ? 504 : 502,
        { error: known ? error.code : controller.signal.aborted ? 'upstream_timeout' : 'provider_unavailable', request_id: requestId });
    } finally {
      clearTimeout(timer);
      if (counted) { client.active = false; active--; }
      // Never log API keys, device codes, prompt content or provider error bodies.
      log({ requestId, providerRequestId, status: res.statusCode, durationMs: now() - started });
    }
  });
  server.requestTimeout = 15000;
  server.headersTimeout = 10000;
  server.keepAliveTimeout = 5000;
  server.maxConnections = 100;
  return server;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    const config = configFromEnv();
    const port = Number(process.env.PORT || 8080);
    if (!Number.isInteger(port) || port < 1 || port > 65535) throw new Error('Netinkamas PORT.');
    const server = createApp({ config, log: event => process.stdout.write(JSON.stringify(event) + '\n') });
    server.listen(port, process.env.HOST || '127.0.0.1', () => process.stdout.write(`Vaeloria DI paslauga veikia (${config.model}, prievadas ${port}).\n`));
    for (const signal of ['SIGTERM', 'SIGINT']) process.once(signal, () => {
      server.close(() => process.exit(0));
      setTimeout(() => process.exit(0), 40000).unref();
    });
  } catch (error) { process.stderr.write(error.message + '\n'); process.exitCode = 1; }
}
