import test from 'node:test';
import assert from 'node:assert/strict';
import { once } from 'node:events';
import { request } from 'node:http';
import { createApp, configFromEnv, extractNarration } from '../server.mjs';

const token = 'device_test_code_'.repeat(3);
const config = { apiKey: 'test-provider-secret-no-network', tokens: [token], model: 'gpt-6-astra',
  upstreamTimeoutMs: 500, perMinute: 8, perDay: 100, maxConcurrent: 4 };
const narration = { scene_title: 'Vartų šviesa', scene: 'Apžiūri vartų akmenis. Juose pastebi senų matavimų žymes.',
  choices: ['Ištirti žymes', 'Paklausti sargybinio', 'Užrašyti pastebėjimus'] };
const completed = (value = narration) => ({ status: 'completed', output: [
  { type: 'reasoning', summary: [] },
  { type: 'message', role: 'assistant', status: 'completed', content: [{ type: 'output_text', text: JSON.stringify(value) }] }
] });
const json = (value, status = 200) => new Response(JSON.stringify(value), { status, headers: { 'x-request-id': 'provider-test-id' } });

async function fixture(t, fetchImpl = async () => json(completed()), overrides = {}) {
  const logs = [];
  const server = createApp({ config: { ...config, ...overrides }, fetchImpl, now: () => 100000, log: e => logs.push(e) });
  server.listen(0, '127.0.0.1'); await once(server, 'listening');
  t.after(() => new Promise(resolve => { server.close(resolve); server.closeAllConnections(); }));
  const url = `http://127.0.0.1:${server.address().port}`;
  const post = (body = { context: 'Patvirtinta vietinė scena.' }, auth = token) => fetch(url + '/v1/turn', {
    method: 'POST', headers: { Authorization: `Bearer ${auth}`, 'Content-Type': 'application/json' }, body: JSON.stringify(body)
  });
  return { post, logs, url };
}

test('real HTTP request builds a strict Responses request and returns only narrative', async t => {
  let calls = 0;
  const f = await fixture(t, async (url, options) => {
    calls++;
    assert.equal(url, 'https://api.openai.com/v1/responses');
    assert.equal(options.redirect, 'error');
    assert.equal(options.headers.Authorization, `Bearer ${config.apiKey}`);
    const body = JSON.parse(options.body);
    assert.equal(body.model, 'gpt-6-astra'); assert.equal(body.store, false);
    assert.equal(body.reasoning.effort, 'low'); assert.equal(body.max_output_tokens, 4096);
    assert.equal(body.text.format.strict, true); assert.equal(body.text.format.schema.additionalProperties, false);
    assert.deepEqual(body.text.format.schema.required, ['scene_title', 'scene', 'choices']);
    assert.match(body.instructions, /lietuvių kalba/); assert.match(body.instructions, /nepatikimi žaidimo duomenys/);
    assert.deepEqual(body.input, [{ role: 'user', content: 'Patvirtinta vietinė scena.' }]);
    return json(completed());
  });
  const response = await f.post(); assert.equal(response.status, 200);
  assert.deepEqual((await response.json()).narration, narration); assert.equal(calls, 1);
  assert.equal(response.headers.get('cache-control'), 'no-store');
  assert.ok(!JSON.stringify(f.logs).includes(config.apiKey)); assert.ok(!JSON.stringify(f.logs).includes(token));
  assert.ok(!JSON.stringify(f.logs).includes('Patvirtinta vietinė scena'));
});

test('missing and wrong device codes cannot call the provider', async t => {
  let calls = 0; const f = await fixture(t, async () => { calls++; return json(completed()); });
  assert.equal((await f.post(undefined, '')).status, 401);
  assert.equal((await f.post(undefined, 'wrong_device_code_'.repeat(3))).status, 401);
  assert.equal(calls, 0);
});

test('input bounds and unexpected client options are rejected before billing', async t => {
  let calls = 0; const f = await fixture(t, async () => { calls++; return json(completed()); });
  for (const body of [{ context: '' }, { context: 'a'.repeat(24001) }, { context: 'x', model: 'expensive' }, { context: [] }])
    assert.equal((await f.post(body)).status, 400);
  assert.equal((await f.post({ context: 'a'.repeat(70000) })).status, 413);
  const badType = await fetch(f.url + '/v1/turn', { method: 'POST', headers: { Authorization: `Bearer ${token}` }, body: 'x' });
  assert.equal(badType.status, 415); assert.equal(calls, 0);
});

test('refusals, incomplete output and forged state never become a successful turn', async t => {
  const inputs = [
    { status: 'incomplete', output: [] },
    { status: 'completed', output: [{ type: 'message', role: 'assistant', content: [{ type: 'refusal', refusal: 'no' }] }] },
    completed({ ...narration, crowns_delta: 1000000 }),
    completed({ ...narration, choices: ['Kartoti', 'Kartoti', 'Kartoti'] }),
    completed({ ...narration, scene: 9 }),
    { status: 'completed', output: [] }
  ];
  const f = await fixture(t, async () => json(inputs.shift()));
  for (const status of [502, 422, 502, 502, 502, 502]) assert.equal((await f.post()).status, status);
});

test('provider errors are redacted and never automatically retried', async t => {
  let calls = 0;
  const f = await fixture(t, async () => { calls++; return json({ error: { message: config.apiKey + token } }, 401); });
  const response = await f.post(); assert.equal(response.status, 502);
  const body = await response.text(); assert.ok(!body.includes(config.apiKey)); assert.ok(!body.includes(token)); assert.equal(calls, 1);
});

test('minute and daily budgets cap provider requests', async t => {
  let calls = 0;
  const f = await fixture(t, async () => { calls++; return json(completed()); }, { perMinute: 1 });
  assert.equal((await f.post()).status, 200);
  const blocked = await f.post(); assert.equal(blocked.status, 429); assert.ok(blocked.headers.has('retry-after')); assert.equal(calls, 1);
  const daily = await fixture(t, async () => json(completed()), { perMinute: 8, perDay: 1 });
  assert.equal((await daily.post()).status, 200); assert.equal((await daily.post()).status, 429);
});

test('upstream timeout aborts the request and frees the active slot', async t => {
  let calls = 0, aborted = false;
  const f = await fixture(t, async (_, options) => {
    calls++;
    if (calls > 1) return json(completed());
    return new Promise((_, reject) => options.signal.addEventListener('abort', () => { aborted = true; reject(new Error('aborted')); }, { once: true }));
  }, { upstreamTimeoutMs: 20 });
  assert.equal((await f.post()).status, 504); assert.equal(aborted, true);
  assert.equal((await f.post()).status, 200);
});

test('a second concurrent request from one device is rejected', async t => {
  let release, began; const started = new Promise(resolve => { began = resolve; });
  const f = await fixture(t, async () => { began(); return new Promise(resolve => { release = () => resolve(json(completed())); }); });
  const first = f.post(); await started;
  assert.equal((await f.post()).status, 429); release(); assert.equal((await first).status, 200);
});

test('disconnecting the phone aborts the provider request', async t => {
  let began, stopped;
  const started = new Promise(resolve => { began = resolve; });
  const aborted = new Promise(resolve => { stopped = resolve; });
  const f = await fixture(t, async (_, options) => {
    began(); return new Promise((_, reject) => options.signal.addEventListener('abort', () => { stopped(); reject(new Error('cancel')); }, { once: true }));
  });
  const client = request(f.url + '/v1/turn', { method: 'POST', headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' } });
  client.on('error', () => {}); client.end(JSON.stringify({ context: 'Patvirtinta scena.' }));
  await started; client.destroy(); await aborted;
});

test('health is local and the service will not start with missing or reused secrets', async t => {
  let calls = 0; const f = await fixture(t, async () => { calls++; return json(completed()); });
  assert.equal((await fetch(f.url + '/health')).status, 200); assert.equal(calls, 0);
  assert.throws(() => configFromEnv({}));
  assert.throws(() => configFromEnv({ OPENAI_API_KEY: config.apiKey, VAELORIA_CLIENT_TOKENS: 'short' }));
  assert.throws(() => configFromEnv({ OPENAI_API_KEY: config.apiKey, VAELORIA_CLIENT_TOKENS: `${token},${token}` }));
  assert.equal(configFromEnv({ OPENAI_API_KEY: config.apiKey, VAELORIA_CLIENT_TOKENS: token }).model, 'gpt-6-astra');
  assert.deepEqual(extractNarration(completed()), narration);
});
