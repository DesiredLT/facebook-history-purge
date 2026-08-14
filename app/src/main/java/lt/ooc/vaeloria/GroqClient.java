package lt.ooc.vaeloria;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class GroqClient {
    public interface Callback { void ok(JSONObject result); void error(String message); }
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private static final String ENDPOINT="https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL="openai/gpt-oss-120b";

    public void resolve(String apiKey,String action,JSONObject state,Callback cb){ executor.submit(() -> {
        try { cb.ok(request(apiKey,action,state)); } catch(Exception e){ cb.error(e.getMessage()==null?e.toString():e.getMessage()); }
    }); }

    private JSONObject request(String key,String action,JSONObject state) throws Exception {
        JSONObject body=new JSONObject(); body.put("model",MODEL); body.put("reasoning_effort","low"); body.put("max_completion_tokens",1800);
        JSONArray messages=new JSONArray();
        messages.put(new JSONObject().put("role","system").put("content",systemPrompt()));
        messages.put(new JSONObject().put("role","user").put("content","KANONINĖ AKTUALI BŪSENA:\n"+compactState(state).toString()+"\n\nŽAIDĖJO VEIKSMAS:\n"+action));
        body.put("messages",messages);
        body.put("response_format",new JSONObject().put("type","json_schema").put("json_schema",new JSONObject()
                .put("name","vaeloria_turn").put("strict",true).put("schema",schema())));

        Exception last=null;
        for(int attempt=0;attempt<2;attempt++){
            HttpURLConnection c=(HttpURLConnection)new URL(ENDPOINT).openConnection();
            try{
                c.setRequestMethod("POST"); c.setConnectTimeout(15000); c.setReadTimeout(60000); c.setDoOutput(true);
                c.setRequestProperty("Authorization","Bearer "+key); c.setRequestProperty("Content-Type","application/json");
                try(OutputStream os=c.getOutputStream()){ os.write(body.toString().getBytes(StandardCharsets.UTF_8)); }
                int code=c.getResponseCode(); String text=read(code>=200&&code<300?c.getInputStream():c.getErrorStream());
                if(code>=200&&code<300){
                    JSONObject envelope=new JSONObject(text); String content=envelope.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                    return new JSONObject(content);
                }
                String friendly="Groq HTTP "+code+": "+extractError(text);
                if((code==429||code>=500) && attempt==0){ Thread.sleep(1200); last=new Exception(friendly); continue; }
                throw new Exception(friendly);
            } finally { c.disconnect(); }
        }
        throw last==null?new Exception("Groq užklausa nepavyko"):last;
    }

    private String systemPrompt(){
        return "Tu esi Vaeloria RPG Game Master. Atsakyk lietuviškai. Žaidėjas deklaruoja ketinimą, o ne garantuotą rezultatą. " +
                "Išsaugok kanoną ir tęstinumą. Einoras yra nepaprastai galingas, bet nežinomos taisyklės, politinis legitimumas ir savarankiški NPC negali būti automatiškai apeiti vien galia. " +
                "Nesukurk nemokamos pergalės; sėkmė, dalinė sėkmė, komplikacija ir nesėkmė turi būti galimos pagal situaciją. " +
                "Pagrindinis aktyvus arc yra The Broken Meridian. Dabartinis tikslas: ištirti pirmą Waygate Drift incidentą, atskiriant faktus nuo prielaidų. " +
                "Scenos tekstas 2–4 trumpi sakiniai. Pateik tik 3 materialiai skirtingus pasiūlymus, bet jie neriboja freeform. " +
                "Resursų delta turi būti konservatyvi ir pagrįsta. Neperrašinėk visos būsenos; grąžink tik sutartą struktūrą.";
    }

    private JSONObject compactState(JSONObject s) throws Exception {
        JSONObject out=new JSONObject();
        out.put("character",s.getJSONObject("character")); out.put("resources",s.getJSONObject("resources")); out.put("crowns",s.getLong("crowns"));
        out.put("world",s.getJSONObject("world")); out.put("quest",s.getJSONObject("quest")); out.put("threads",s.getJSONArray("threads")); out.put("pending_consequences",s.getJSONArray("pending_consequences"));
        JSONArray ab=new JSONArray(); JSONArray src=s.getJSONArray("abilities"); for(int i=0;i<src.length();i++) ab.put(src.getJSONObject(i).getString("name")); out.put("abilities",ab);
        JSONArray inv=new JSONArray(); src=s.getJSONArray("inventory"); for(int i=0;i<src.length();i++){ JSONObject x=src.getJSONObject(i); inv.put(new JSONObject().put("name",x.getString("name")).put("equipped_slot",x.optString("equipped_slot",""))); } out.put("inventory",inv);
        out.put("scene",s.getJSONObject("scene"));
        JSONArray journal=s.optJSONArray("journal"); JSONArray last=new JSONArray(); if(journal!=null){ for(int i=Math.max(0,journal.length()-5);i<journal.length();i++) last.put(journal.get(i)); } out.put("recent_journal",last);
        return out;
    }

    private JSONObject schema() throws Exception {
        JSONObject str=new JSONObject().put("type","string");
        JSONObject choice=new JSONObject().put("type","object").put("additionalProperties",false)
                .put("properties",new JSONObject().put("label",str).put("action",str)).put("required",new JSONArray().put("label").put("action"));
        JSONObject deltas=new JSONObject().put("type","object").put("additionalProperties",false)
                .put("properties",new JSONObject()
                        .put("hp",intRange(-100,100)).put("mana",intRange(-100,100)).put("stamina",intRange(-100,100)).put("aeonic",intRange(-900,900)))
                .put("required",new JSONArray().put("hp").put("mana").put("stamina").put("aeonic"));
        JSONObject props=new JSONObject()
                .put("scene_title",str).put("scene_text",str)
                .put("scene_type",new JSONObject().put("type","string").put("enum",new JSONArray().put("investigation").put("social").put("travel").put("combat").put("discovery").put("downtime")))
                .put("location",str).put("danger",intRange(0,10))
                .put("choices",new JSONObject().put("type","array").put("items",choice).put("minItems",3).put("maxItems",3))
                .put("time_advance_minutes",intRange(0,1440)).put("resource_delta",deltas)
                .put("crowns_delta",intRange(-1000000,1000000))
                .put("inventory_add",new JSONObject().put("type","array").put("items",str).put("maxItems",5))
                .put("inventory_remove",new JSONObject().put("type","array").put("items",str).put("maxItems",5))
                .put("quest_note",str)
                .put("event_log",new JSONObject().put("type","array").put("items",str).put("maxItems",5));
        return new JSONObject().put("type","object").put("additionalProperties",false).put("properties",props)
                .put("required",new JSONArray().put("scene_title").put("scene_text").put("scene_type").put("location").put("danger").put("choices").put("time_advance_minutes").put("resource_delta").put("crowns_delta").put("inventory_add").put("inventory_remove").put("quest_note").put("event_log"));
    }
    private JSONObject intRange(int min,int max) throws Exception { return new JSONObject().put("type","integer").put("minimum",min).put("maximum",max); }
    private String read(InputStream in) throws Exception { if(in==null)return ""; StringBuilder b=new StringBuilder(); try(BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){ String l; while((l=r.readLine())!=null)b.append(l); } return b.toString(); }
    private String extractError(String text){ try{return new JSONObject(text).getJSONObject("error").optString("message",text);}catch(Exception e){return text.length()>300?text.substring(0,300):text;} }
}
