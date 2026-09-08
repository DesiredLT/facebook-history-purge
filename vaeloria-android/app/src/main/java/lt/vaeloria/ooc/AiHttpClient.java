package lt.vaeloria.ooc;

import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/** One transport per request: cancelling an old Activity cannot cancel a new one. */
final class AiHttpClient {
    private volatile HttpURLConnection connection;
    private volatile boolean cancelled;

    JSONObject post(String endpoint, String token, JSONObject body) throws Exception {
        checkCancelled();
        URL url = new URL(endpoint);
        if (!"https".equals(url.getProtocol()) || url.getUserInfo() != null)
            throw new IOException("Ryšiui būtinas HTTPS adresas.");
        HttpURLConnection current = (HttpURLConnection) url.openConnection();
        connection = current;
        try {
            checkCancelled();
            current.setInstanceFollowRedirects(false);
            current.setConnectTimeout(12000);
            current.setReadTimeout(45000);
            current.setRequestMethod("POST");
            current.setRequestProperty("Authorization", "Bearer " + token);
            current.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            current.setRequestProperty("Accept", "application/json");
            current.setDoOutput(true);
            byte[] data = body.toString().getBytes(StandardCharsets.UTF_8);
            current.setFixedLengthStreamingMode(data.length);
            try (OutputStream out = current.getOutputStream()) { out.write(data); }
            checkCancelled();
            int status = current.getResponseCode();
            if (status < 200 || status >= 300) throw new ServiceException(status);
            String response = readBounded(current.getInputStream(), 131072);
            checkCancelled();
            return new JSONObject(response);
        } finally {
            current.disconnect();
            if (connection == current) connection = null;
        }
    }

    void cancel() { cancelled = true; HttpURLConnection current = connection; if (current != null) current.disconnect(); }
    boolean isCancelled() { return cancelled; }
    private void checkCancelled() throws InterruptedIOException {
        if (cancelled || Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Užklausa atšaukta.");
    }

    static String readBounded(InputStream stream, int limit) throws IOException {
        if (stream == null) throw new IOException("Tuščias atsakymas.");
        try (InputStream in = stream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096]; int count;
            while ((count = in.read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Užklausa atšaukta.");
                if (count > limit - out.size()) throw new IOException("Failas arba atsakymas per didelis.");
                out.write(buffer, 0, count);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }

    static String userMessage(Exception error) {
        if (error instanceof ServiceException) {
            int code = ((ServiceException) error).status;
            if (code == 401 || code == 403) return "DI prisijungimas atmestas. Patikrink nustatymus.";
            if (code == 429) return "Pasiekta DI užklausų riba. Pabandyk vėliau.";
            if (code == 422) return "DI negalėjo pateikti šios scenos.";
            if (code == 503) return "DI paslauga dar nesukonfigūruota arba laikinai užimta.";
        }
        if (error instanceof SocketTimeoutException) return "DI atsakymo laukimo laikas baigėsi.";
        return "DI atsakymo gauti nepavyko.";
    }

    static final class ServiceException extends IOException {
        final int status;
        ServiceException(int status) { super("DI HTTP " + status); this.status = status; }
    }
}
