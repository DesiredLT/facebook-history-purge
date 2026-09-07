package lt.vaeloria.ooc;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

/** Optional legacy provider, using the same narrative-only contract as OpenAI. */
public final class GroqClient {
    private static final String ENDPOINT="https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL="openai/gpt-oss-120b";
    private GroqClient(){}

    static JSONObject resolveNarration(AiHttpClient transport, String key, String context, String policy, JSONObject schema) throws Exception {
        JSONObject request = new JSONObject().put("model", MODEL).put("reasoning_effort", "low")
                .put("max_completion_tokens", 3000)
                .put("messages", new JSONArray().put(new JSONObject().put("role", "system").put("content", policy))
                        .put(new JSONObject().put("role", "user").put("content", context)))
                .put("response_format", new JSONObject().put("type", "json_schema").put("json_schema",
                        new JSONObject().put("name", "vaeloria_narration").put("strict", true).put("schema", schema)));
        JSONObject choice = transport.post(ENDPOINT, key, request).getJSONArray("choices").getJSONObject(0);
        if (!"stop".equals(choice.optString("finish_reason"))) throw new IOException("DI atsakymas nebaigtas.");
        JSONObject message = choice.getJSONObject("message");
        if (!message.isNull("refusal")) throw new IOException("DI negalėjo pateikti pasakojimo.");
        JSONObject result = new JSONObject(message.getString("content"));
        NarrativeTurn.validate(result);
        return result;
    }

}
