package lt.vaeloria.ooc;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class SecureKeyStore {
    private static final String ALIAS = "vaeloria_groq_key";
    private static final String PREF = "vaeloria_secure";
    private static final String VALUE = "groq_api_key";

    private SecureKeyStore() {}

    public static void save(Context context, String secret) throws Exception {
        SecretKey key = getOrCreateKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] enc = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));
        String packed = Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP) + ":" + Base64.encodeToString(enc, Base64.NO_WRAP);
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(VALUE, packed).apply();
    }

    public static String load(Context context) {
        String current = loadCurrent(context);
        if (!current.isEmpty()) return current;
        String legacy = loadLegacy(context);
        if (!legacy.isEmpty()) {
            try { save(context, legacy); } catch (Exception ignored) {}
            return legacy;
        }
        return "";
    }

    private static String loadCurrent(Context context) {
        try {
            SharedPreferences p = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            String packed = p.getString(VALUE, "");
            if (packed == null || packed.isEmpty()) return "";
            String[] parts = packed.split(":", 2);
            if (parts.length != 2) return "";
            byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] enc = Base64.decode(parts[1], Base64.NO_WRAP);
            return decrypt(iv, enc);
        } catch (Exception e) { return ""; }
    }

    private static String loadLegacy(Context context) {
        try {
            SharedPreferences p = context.getSharedPreferences("secure", Context.MODE_PRIVATE);
            String ct = p.getString("ct", "");
            String iv = p.getString("iv", "");
            if (ct == null || iv == null || ct.isEmpty() || iv.isEmpty()) return "";
            byte[] enc = java.util.Base64.getDecoder().decode(ct);
            byte[] ivBytes = java.util.Base64.getDecoder().decode(iv);
            return decrypt(ivBytes, enc);
        } catch (Exception e) { return ""; }
    }

    private static String decrypt(byte[] iv, byte[] enc) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
        return new String(cipher.doFinal(enc), StandardCharsets.UTF_8);
    }

    public static void clear(Context context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().remove(VALUE).apply();
        context.getSharedPreferences("secure", Context.MODE_PRIVATE).edit().remove("ct").remove("iv").apply();
    }

    private static SecretKey getOrCreateKey() throws Exception {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        if (ks.containsAlias(ALIAS)) return (SecretKey) ks.getKey(ALIAS, null);
        KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        kg.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        return kg.generateKey();
    }
}
