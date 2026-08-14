package lt.vaeloria.ooc;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class GroqClient {
    private static final String ENDPOINT="https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL="openai/gpt-oss-120b";
    private GroqClient(){}

    public static JSONObject resolveTurn(String apiKey,GameState s,String action,String equipped,List<String[]> abilities)throws Exception{
        JSONObject req=new JSONObject();
        req.put("model",MODEL);
        req.put("reasoning_effort","low");
        req.put("messages",messages(s,action,equipped,abilities));
        req.put("response_format",responseFormat());
        HttpURLConnection c=(HttpURLConnection)new URL(ENDPOINT).openConnection();
        c.setConnectTimeout(15000);c.setReadTimeout(45000);c.setRequestMethod("POST");
        c.setRequestProperty("Authorization","Bearer "+apiKey);c.setRequestProperty("Content-Type","application/json");c.setDoOutput(true);
        try(OutputStream os=c.getOutputStream()){os.write(req.toString().getBytes(StandardCharsets.UTF_8));}
        int code=c.getResponseCode();InputStream stream=code>=200&&code<300?c.getInputStream():c.getErrorStream();String body=read(stream);
        if(code<200||code>=300)throw new IllegalStateException("Groq HTTP "+code+": "+body);
        String content=new JSONObject(body).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        return new JSONObject(content);
    }

    private static JSONArray messages(GameState s,String action,String equipped,List<String[]> abilities)throws Exception{
        JSONArray m=new JSONArray();
        JSONObject sys=new JSONObject();sys.put("role","system");
        sys.put("content","Tu esi Vaeloria žaidimo meistras. Visą žaidėjui matomą tekstą rašyk lietuviškai. Venk anglicizmų: jei yra aiškus lietuviškas žodis, vartok jį. Išimtis tik tikriniams pasaulio vardams, prekių ženklams ir techniniams modelių pavadinimams. Vartok „kelionės vartai“, ne waygate; „įranga“, ne gear; „grobis“ arba „atlygis“, ne loot; „kova“, ne combat; „atradimas“, ne discovery; „siužeto gija“, ne thread. Žaidėjas deklaruoja bandymą, ne rezultatą. Pasaulis turi savarankiškas taisykles, veikėjų interesus ir realias pasekmes. Aukštos statistikos automatiškai neišsprendžia nežinomų taisyklių, politikos ar nepriklausomų veikėjų valios. Nekurk nemokamų daiktų ar pergalių. Scena 2–4 trumpi sakiniai. Pateik 3 materialiai skirtingus pasirinkimus. Kova turi trukti kelis ėjimus ir aiškiai parodyti priešo ketinimą. Kai combat_active=true užpildyk visus kovos laukus; kai false kovos tekstiniai laukai turi būti tušti. Jei nėra pagrindo resurso ar pinigų pokyčiui, delta=0. Grobį pateik tik tada, kai scena logiškai pagrindžia jo gavimą; kitu atveju loot turi būti tuščias masyvas. Nauja dėvima įranga gali būti šių kategorijų: weapon, offhand, head, chest, hands, legs, feet, belt, neck, ring, utility, relic. Artefaktams naudok artifact.");
        m.put(sys);
        StringBuilder a=new StringBuilder();for(String[] ab:abilities){if(a.length()>0)a.append("; ");a.append(ab[0]);}
        String recent=s.recentTurns.isEmpty()?"nėra":String.join(" | ",s.recentTurns.subList(Math.max(0,s.recentTurns.size()-6),s.recentTurns.size()));
        JSONObject user=new JSONObject();user.put("role","user");
        user.put("content","KANONAS\nEinoras: 201 m. chronologinis / 20 m. biologinis, biologinis senėjimas sustabdytas. 92 bazinės statistikos 100/100; pažanga virš ribos yra kokybinė.\nVieta: "+s.location+". Metai: "+s.worldYear+". Minutė: "+s.worldMinute+".\nGyvybė "+s.hp+"/"+s.hpMax+", Mana "+s.mana+"/"+s.manaMax+", Ištvermė "+s.stamina+"/"+s.staminaMax+", Eoninė energija "+s.aeonic+"/"+s.aeonicMax+", Karūnos "+s.crowns+".\nSiužetas: "+s.questTitle+". Tikslas: "+s.objective+"\nDėvima įranga: "+equipped+"\nGebėjimai: "+a+"\nScena: "+s.scene+"\nPaskutiniai ėjimai: "+recent+"\n\nVEIKSMAS: "+action);
        m.put(user);return m;
    }

    private static JSONObject responseFormat()throws Exception{
        JSONObject schema=new JSONObject().put("type","object").put("additionalProperties",false);
        JSONObject p=new JSONObject();
        p.put("scene_title",str());p.put("scene",str());p.put("choices",new JSONObject().put("type","array").put("minItems",3).put("maxItems",3).put("items",str()));p.put("location",str());
        p.put("time_minutes",integer(0,1440));p.put("hp_delta",integer(-100,100));p.put("mana_delta",integer(-100,100));p.put("stamina_delta",integer(-100,100));p.put("aeonic_delta",integer(-900,900));p.put("crowns_delta",integer(-1000000,1000000));p.put("quest_note",str());
        p.put("event_tag",new JSONObject().put("type","string").put("enum",new JSONArray().put("none").put("combat").put("discovery").put("social").put("travel").put("reward").put("setback")));
        p.put("combat_active",new JSONObject().put("type","boolean"));p.put("enemy_name",str());p.put("enemy_status",str());p.put("enemy_telegraph",str());p.put("combat_distance",new JSONObject().put("type","string").put("enum",new JSONArray().put("close").put("mid").put("far")));p.put("combat_hazard",str());

        JSONObject lootItem=new JSONObject().put("type","object").put("additionalProperties",false);
        JSONObject lp=new JSONObject();
        lp.put("name",str());
        lp.put("category",new JSONObject().put("type","string").put("enum",new JSONArray().put("weapon").put("offhand").put("head").put("chest").put("hands").put("legs").put("feet").put("belt").put("neck").put("ring").put("utility").put("relic").put("artifact")));
        lp.put("rarity",new JSONObject().put("type","string").put("enum",new JSONArray().put("common").put("uncommon").put("rare").put("epic").put("legendary").put("ancient").put("unique")));
        lp.put("description",str());
        lootItem.put("properties",lp).put("required",new JSONArray().put("name").put("category").put("rarity").put("description"));
        p.put("loot",new JSONObject().put("type","array").put("minItems",0).put("maxItems",3).put("items",lootItem));
        schema.put("properties",p);
        JSONArray req=new JSONArray();
        for(String k:new String[]{"scene_title","scene","choices","location","time_minutes","hp_delta","mana_delta","stamina_delta","aeonic_delta","crowns_delta","quest_note","event_tag","combat_active","enemy_name","enemy_status","enemy_telegraph","combat_distance","combat_hazard","loot"})req.put(k);
        schema.put("required",req);
        return new JSONObject().put("type","json_schema").put("json_schema",new JSONObject().put("name","vaeloria_turn").put("strict",true).put("schema",schema));
    }
    private static JSONObject str()throws Exception{return new JSONObject().put("type","string");}
    private static JSONObject integer(int min,int max)throws Exception{return new JSONObject().put("type","integer").put("minimum",min).put("maximum",max);}
    private static String read(InputStream in)throws Exception{if(in==null)return"";BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();String line;while((line=r.readLine())!=null)b.append(line);return b.toString();}
}
