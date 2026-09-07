package lt.vaeloria.ooc;

import android.content.Context;
import org.json.JSONObject;
import java.net.URI;

final class OpenAiSettings {
    static final String LOCAL = "local", GROQ = "groq", OPENAI = "openai";
    static final String SECRET = "openai_connection";
    final String url, token;
    OpenAiSettings(String url, String token) { this.url = url; this.token = token; }

    static String provider(Context context) {
        String configured = context.getSharedPreferences("vaeloria_ai", Context.MODE_PRIVATE).getString("provider", "");
        if (LOCAL.equals(configured) || GROQ.equals(configured) || OPENAI.equals(configured)) return configured;
        return SecureKeyStore.load(context).isEmpty() ? LOCAL : GROQ;
    }
    static void select(Context context, String provider) {
        if (!LOCAL.equals(provider) && !GROQ.equals(provider) && !OPENAI.equals(provider)) throw new IllegalArgumentException();
        context.getSharedPreferences("vaeloria_ai", Context.MODE_PRIVATE).edit().putString("provider", provider).apply();
    }
    static OpenAiSettings load(Context context) {
        try {
            JSONObject value = new JSONObject(SecureKeyStore.loadSecret(context, SECRET));
            return validated(value.getString("url"), value.getString("token"));
        } catch (Exception ignored) { return new OpenAiSettings("", ""); }
    }
    static void save(Context context, String url, String token) throws Exception {
        OpenAiSettings config = validated(url, token);
        SecureKeyStore.saveSecret(context, SECRET, new JSONObject().put("url", config.url).put("token", config.token).toString());
        select(context, OPENAI);
    }
    static OpenAiSettings validated(String rawUrl, String rawToken) throws Exception {
        String url = rawUrl == null ? "" : rawUrl.trim(), token = rawToken == null ? "" : rawToken.trim();
        URI uri = new URI(url);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null
                || uri.getRawQuery() != null || uri.getFragment() != null || url.length() > 500
                || uri.getHost().equalsIgnoreCase("api.openai.com"))
            throw new IllegalArgumentException("Įvesk žaidimo DI paslaugos HTTPS adresą.");
        if (!token.matches("[A-Za-z0-9_-]{32,200}") || token.startsWith("sk-"))
            throw new IllegalArgumentException("Įvesk žaidimo prisijungimo kodą, kurį suteikė paslaugos administratorius.");
        while (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        return new OpenAiSettings(url, token);
    }
    static String label(Context context) {
        switch (provider(context)) {
            case OPENAI: return load(context).url.isEmpty() ? "OpenAI · reikia nustatyti prisijungimą" : "OpenAI · žaidimo paslauga";
            case GROQ: return SecureKeyStore.load(context).isEmpty() ? "Groq · reikia API rakto" : "Groq · gpt-oss-120b";
            default: return "Vietinis pasakotojas · veikia be interneto";
        }
    }
}
