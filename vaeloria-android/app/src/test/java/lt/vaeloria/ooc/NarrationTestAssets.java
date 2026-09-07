package lt.vaeloria.ooc;

import org.json.JSONObject;

final class NarrationTestAssets {
    static JSONObject schema() throws Exception {
        return new JSONObject(AiHttpClient.readBounded(NarrationTestAssets.class.getResourceAsStream("/ai/narration-schema.json"),8000));
    }
    static String policy() throws Exception {
        return AiHttpClient.readBounded(NarrationTestAssets.class.getResourceAsStream("/ai/narration-policy.txt"),16000);
    }
}
