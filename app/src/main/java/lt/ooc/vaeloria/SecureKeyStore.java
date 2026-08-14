package lt.ooc.vaeloria;

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
    private static final String ALIAS="vaeloria_groq_key_v1";
    private final SharedPreferences prefs;
    public SecureKeyStore(Context c){ prefs=c.getSharedPreferences("secure",Context.MODE_PRIVATE); }

    private SecretKey key() throws Exception {
        KeyStore ks=KeyStore.getInstance("AndroidKeyStore"); ks.load(null);
        if(ks.containsAlias(ALIAS)) return ((KeyStore.SecretKeyEntry)ks.getEntry(ALIAS,null)).getSecretKey();
        KeyGenerator kg=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
        kg.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());
        return kg.generateKey();
    }

    public void save(String apiKey) throws Exception {
        if(apiKey==null || apiKey.trim().isEmpty()){ clear(); return; }
        Cipher c=Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.ENCRYPT_MODE,key());
        byte[] enc=c.doFinal(apiKey.trim().getBytes(StandardCharsets.UTF_8));
        prefs.edit().putString("iv",Base64.encodeToString(c.getIV(),Base64.NO_WRAP)).putString("ct",Base64.encodeToString(enc,Base64.NO_WRAP)).apply();
    }

    public String load(){
        try{
            String iv=prefs.getString("iv",null), ct=prefs.getString("ct",null); if(iv==null||ct==null) return "";
            Cipher c=Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(iv,Base64.NO_WRAP)));
            return new String(c.doFinal(Base64.decode(ct,Base64.NO_WRAP)),StandardCharsets.UTF_8);
        }catch(Exception e){ clear(); return ""; }
    }
    public boolean hasKey(){ return !load().isEmpty(); }
    public void clear(){ prefs.edit().remove("iv").remove("ct").apply(); }
}
