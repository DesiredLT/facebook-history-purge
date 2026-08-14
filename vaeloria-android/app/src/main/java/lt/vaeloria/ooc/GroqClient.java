package lt.vaeloria.ooc;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class GroqClient {
    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "openai/gpt-oss-120b";

    private GroqClient() {}

    public static JSONObject resolveTurn(String apiKey, GameState s, String action, String equipped, List<String[]> abilities) throws Exception {
        JSONObject req = new JSONObject();
        req.put("model", MODEL);
        req.put("reasoning_effort", "low");
        req.put("messages", messages(s, action, equipped, abilities));
        req.put("response_format", responseFormat());

        HttpURLConnection c = (HttpURLConnection) new URL(ENDPOINT).openConnection();
        c.setConnectTimeout(15_000);
        c.setReadTimeout(45_000);
        c.setRequestMethod("POST");
        c.setRequestProperty("Authorization", "Bearer " + apiKey);
        c.setRequestProperty("Content-Type", "application/json");
        c.setDoOutput(true);
        try(OutputStream os=c.getOutputStream()) { os.write(req.toString().getBytes(StandardCharsets.UTF_8)); }
        int code=c.getResponseCode();
        InputStream stream = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        String body=read(stream);
        if(code<200 || code>=300) throw new IllegalStateException("Groq HTTP " + code + ": " + body);
        JSONObject root=new JSONObject(body);
        String content=root.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        return new JSONObject(content);
    }

    private static JSONArray messages(GameState s, String action, String equipped, List<String[]> abilities) throws Exception {
        JSONArray m=new JSONArray();
        JSONObject sys=new JSONObject();
        sys.put("role","system");
        sys.put("content",
                "Tu esi Vaeloria Game Master. Atsakyk lietuviškai. Žaidėjas deklaruoja bandymą, ne rezultatą. " +
                "Pasaulis turi savarankiškas taisykles, NPC interesus ir realias pasekmes. Neleisk vien aukštoms statistikoms automatiškai išspręsti nežinomų taisyklių, politikos ar nepriklausomų NPC valios. " +
                "Nesukurk nemokamų daiktų, galių ar pergalių. Nesunaikink kanono. Scenos tekstas 2-4 trumpi sakiniai. " +
                "Visada pateik tris materialiai skirtingus tolesnius pasirinkimus; laisvas veiksmas programėlėje visada leidžiamas. " +
                "Combat turi būti kelių ėjimų, su aiškiais telegraph ir kainomis. Jei veiksmas užima laiką, nurodyk minutes. " +
                "Jei nėra pagrindo resurso ar pinigų pokyčiui, delta=0. location visada grąžink dabartinę arba naują patvirtintą vietą.");
        m.put(sys);

        JSONObject user=new JSONObject(); user.put("role","user");
        StringBuilder a=new StringBuilder();
        for(String[] ab:abilities){ if(a.length()>0)a.append("; "); a.append(ab[0]); }
        String recent = s.recentTurns.isEmpty()?"nėra":String.join(" | ", s.recentTurns.subList(Math.max(0,s.recentTurns.size()-6),s.recentTurns.size()));
        user.put("content",
                "KANONAS\n"+
                "Veikėjas: Einoras, 201 m. chronologinis / 20 m. biologinis, biologinis senėjimas sustabdytas. 92 bazinės statistikos yra 100/100 (Legendary); tolesnė pažanga kokybinė.\n"+
                "Vieta: "+s.location+". Pasaulio metai: "+s.worldYear+". Pasaulio minutė: "+s.worldMinute+".\n"+
                "Resursai: HP "+s.hp+"/"+s.hpMax+", Mana "+s.mana+"/"+s.manaMax+", Stamina "+s.stamina+"/"+s.staminaMax+", Aeonic "+s.aeonic+"/"+s.aeonicMax+", Crowns "+s.crowns+".\n"+
                "Aktyvus quest: The Broken Meridian. Tikslas: "+s.objective+"\n"+
                "Aktyvus gear: "+equipped+"\n"+
                "Gebėjimai: "+a+"\n"+
                "Dabartinė scena: "+s.scene+"\n"+
                "Paskutiniai ėjimai: "+recent+"\n\n"+
                "ŽAIDĖJO VEIKSMAS: "+action);
        m.put(user);
        return m;
    }

    private static JSONObject responseFormat() throws Exception {
        JSONObject schema=new JSONObject(); schema.put("type","object"); schema.put("additionalProperties",false);
        JSONObject p=new JSONObject();
        p.put("scene_title",new JSONObject().put("type","string"));
        p.put("scene",new JSONObject().put("type","string"));
        p.put("choices",new JSONObject().put("type","array").put("minItems",3).put("maxItems",3).put("items",new JSONObject().put("type","string")));
        p.put("location",new JSONObject().put("type","string"));
        p.put("time_minutes",new JSONObject().put("type","integer").put("minimum",0).put("maximum",1440));
        p.put("hp_delta",new JSONObject().put("type","integer").put("minimum",-100).put("maximum",100));
        p.put("mana_delta",new JSONObject().put("type","integer").put("minimum",-100).put("maximum",100));
        p.put("stamina_delta",new JSONObject().put("type","integer").put("minimum",-100).put("maximum",100));
        p.put("aeonic_delta",new JSONObject().put("type","integer").put("minimum",-900).put("maximum",900));
        p.put("crowns_delta",new JSONObject().put("type","integer").put("minimum",-1000000).put("maximum",1000000));
        p.put("quest_note",new JSONObject().put("type","string"));
        p.put("event_tag",new JSONObject().put("type","string").put("enum",new JSONArray().put("none").put("combat").put("discovery").put("social").put("travel").put("reward").put("setback")));
        schema.put("properties",p);
        JSONArray req=new JSONArray(); for(String k:new String[]{"scene_title","scene","choices","location","time_minutes","hp_delta","mana_delta","stamina_delta","aeonic_delta","crowns_delta","quest_note","event_tag"})req.put(k); schema.put("required",req);
        JSONObject js=new JSONObject();js.put("name","vaeloria_turn");js.put("strict",true);js.put("schema",schema);
        return new JSONObject().put("type","json_schema").put("json_schema",js);
    }

    private static String read(InputStream in) throws Exception {
        if(in==null)return "";
        BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
        StringBuilder b=new StringBuilder(); String line; while((line=r.readLine())!=null)b.append(line); return b.toString();
    }
}
