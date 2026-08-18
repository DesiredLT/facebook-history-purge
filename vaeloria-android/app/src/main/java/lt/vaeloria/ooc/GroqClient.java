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
    private static final int MAX_COMPLETION_TOKENS=1800;
    private GroqClient(){}

    public static JSONObject resolveTurn(String apiKey,GameState s,String action,String equipped,List<String[]> abilities,StatEngine.Check check)throws Exception{
        JSONObject req=new JSONObject();
        req.put("model",MODEL);
        req.put("reasoning_effort","low");
        req.put("max_completion_tokens",MAX_COMPLETION_TOKENS);
        req.put("messages",messages(s,action,equipped,abilities,check));
        req.put("response_format",responseFormat());

        Exception last=null;
        for(int attempt=0;attempt<2;attempt++){
            HttpURLConnection c=null;
            try{
                c=(HttpURLConnection)new URL(ENDPOINT).openConnection();
                c.setConnectTimeout(15000);c.setReadTimeout(60000);c.setRequestMethod("POST");
                c.setRequestProperty("Authorization","Bearer "+apiKey);c.setRequestProperty("Content-Type","application/json");c.setDoOutput(true);
                try(OutputStream os=c.getOutputStream()){os.write(req.toString().getBytes(StandardCharsets.UTF_8));}
                int code=c.getResponseCode();InputStream stream=code>=200&&code<300?c.getInputStream():c.getErrorStream();String body=read(stream);
                if(code>=200&&code<300){
                    String content=new JSONObject(body).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                    return new JSONObject(content);
                }
                String msg="Groq HTTP "+code+": "+apiError(body);
                if((code==429||code>=500)&&attempt==0){last=new IllegalStateException(msg);Thread.sleep(1200);continue;}
                throw new IllegalStateException(msg);
            }catch(SocketTimeoutException|UnknownHostException e){
                last=e;if(attempt==0){Thread.sleep(700);continue;}throw e;
            }finally{if(c!=null)c.disconnect();}
        }
        throw last==null?new IllegalStateException("Groq užklausa nepavyko"):last;
    }

    public static JSONObject resolveTurn(String apiKey,GameState s,String action,String equipped,List<String[]> abilities)throws Exception{
        return resolveTurn(apiKey,s,action,equipped,abilities,null);
    }

    private static JSONArray messages(GameState s,String action,String equipped,List<String[]> abilities,StatEngine.Check check)throws Exception{
        JSONArray m=new JSONArray();
        JSONObject sys=new JSONObject();sys.put("role","system");
        sys.put("content","Tu esi Vaeloria žaidimo meistras. Visą žaidėjui matomą tekstą rašyk lietuviškai. Venk anglicizmų: jei yra aiškus lietuviškas žodis, vartok jį. Išimtis tik tikriniams pasaulio vardams, prekių ženklams ir techniniams modelių pavadinimams. Vartok „kelionės vartai“, ne waygate; „įranga“, ne gear; „grobis“ arba „atlygis“, ne loot; „kova“, ne combat; „atradimas“, ne discovery; „siužeto gija“, ne thread. Žaidėjas deklaruoja bandymą, ne rezultatą. Pasaulis turi savarankiškas taisykles, veikėjų interesus ir realias pasekmes. Aukštos statistikos automatiškai neišsprendžia nežinomų taisyklių, politikos ar nepriklausomų veikėjų valios. Nekurk nemokamų daiktų ar pergalių. Scena 2–4 trumpi sakiniai. Pateik 3 materialiai skirtingus pasirinkimus. Kova turi trukti kelis ėjimus ir aiškiai parodyti priešo ketinimą. Kai pradedi kovą su būtybe, enemy_name parink iš iliustruoto bestiarijaus, jei tinka scena: Meridiano vilkas, Pelkių trolis, Nuodų perų motina, Kristalų golemas, Nakties harpija, Maitėdis drake'as, Nuskendęs riteris, Pelenų revenantas, Kaulų orakulas, Tuštumos parazitas, Maro kiautas, Kapų kolosas, Meridiano wyrmas, Bekarūnis titanas, Kraujšaknė Matriarchė, Stiklo lichas, Audros kolosas arba Bedugnės šauklys. Pasaulio bosą rink tik pagrįstai, ne atsitiktiniam susidūrimui. Kai combat_active=true užpildyk visus kovos laukus; kai false kovos tekstiniai laukai turi būti tušti. Jei nėra pagrindo resurso, pinigų ar frakcijos įtakos pokyčiui, delta=0. Frakcijų deltas keisk tik kai scena realiai paveikia jų interesus. Grobį pateik tik tada, kai scena logiškai pagrindžia jo gavimą; kitu atveju loot turi būti tuščias masyvas. Nauja dėvima įranga gali būti šių kategorijų: weapon, offhand, head, chest, hands, legs, feet, belt, neck, ring, utility, relic. Artefaktams naudok artifact. Kai vartotojo žinutėje pateikta PRIVALOMA SAVYBĖS PATIKRA, jos skaitinį rezultatą, įskaitant virš bazinės ribos meistriškumo poveikį, laikyk nekintamu žaidimo variklio sprendimu. Negali nesėkmės paversti sėkme ar sėkmės nesėkme. Interpretacijos mastą, kainą ir pasaulio reakciją parink pagal nurodytą rezultatą.");
        sys.put("content", sys.getString("content")
                + "\nPapildomas v0.9.1 iliustruotas bestiarijus (rinkis tik scenai tinkamą vardą): "
                + EnemyCatalogV091.promptRoster() + "."
                + "\nKovos grobis yra autoritetingai apskaičiuojamas telefono v0.9.2 iškritimo lentelėje pagal būtybės pavojų ir regioną. Jo nekurk loot masyve. Pergalės ėjimui naudok event_tag=combat_victory ir combat_active=false; sąmoningam atsitraukimui naudok combat_escape. Aktyvioje kovoje grąžink nuoseklią enemy_hp, enemy_hp_max ir combat_round būseną. Ne kovos atlygiui loot leidžiamas tik kai scena jį realiai pagrindžia. Galimi retumai apima mythic tarp legendary ir ancient.");
        m.put(sys);
        StringBuilder a=new StringBuilder();for(String[] ab:abilities){if(a.length()>0)a.append("; ");a.append(ab[0]);}
        String recent=s.recentTurns.isEmpty()?"nėra":String.join(" | ",s.recentTurns.subList(Math.max(0,s.recentTurns.size()-6),s.recentTurns.size()));
        JSONObject user=new JSONObject();user.put("role","user");
        user.put("content","KANONAS\nEinoras: 201 m. chronologinis / 20 m. biologinis, biologinis senėjimas sustabdytas. 92 bazinės statistikos 100/100; pažanga virš ribos yra kokybinė.\nVieta: "+s.location+". Metai: "+s.worldYear+". Minutė: "+s.worldMinute+".\nGyvybė "+s.hp+"/"+s.hpMax+", Mana "+s.mana+"/"+s.manaMax+", Ištvermė "+s.stamina+"/"+s.staminaMax+", Eoninė energija "+s.aeonic+"/"+s.aeonicMax+", Karūnos "+s.crowns+".\nKova: "+(s.combatActive?(s.enemyName+" · gyvybė "+s.enemyHp+"/"+s.enemyHpMax+" · ėjimas "+s.combatRound):"neaktyvi")+".\nSiužetas: "+s.questTitle+". Tikslas: "+s.objective+"\nFrakcijos: Asterra "+s.asterraInfluence+" ("+s.asterraRelation+"), Dravenn "+s.dravennInfluence+" ("+s.dravennRelation+"), Lysara "+s.lysaraInfluence+" ("+s.lysaraRelation+").\nDėvima įranga: "+equipped+"\nGebėjimai: "+a+"\nScena: "+s.scene+"\nPaskutiniai ėjimai: "+recent+"\n\nVEIKSMAS: "+action+"\n\n"+(check==null?"":check.prompt()));
        m.put(user);return m;
    }

    private static JSONObject responseFormat()throws Exception{
        JSONObject schema=new JSONObject().put("type","object").put("additionalProperties",false);
        JSONObject p=new JSONObject();
        p.put("scene_title",str());p.put("scene",str());p.put("choices",new JSONObject().put("type","array").put("minItems",3).put("maxItems",3).put("items",str()));p.put("location",str());
        p.put("time_minutes",integer(0,1440));p.put("hp_delta",integer(-100,100));p.put("mana_delta",integer(-100,100));p.put("stamina_delta",integer(-100,100));p.put("aeonic_delta",integer(-900,900));p.put("crowns_delta",integer(-1000000,1000000));p.put("quest_note",str());p.put("asterra_delta",integer(-10,10));p.put("dravenn_delta",integer(-10,10));p.put("lysara_delta",integer(-10,10));
        p.put("event_tag",new JSONObject().put("type","string").put("enum",new JSONArray().put("none").put("combat").put("combat_victory").put("combat_escape").put("discovery").put("social").put("travel").put("reward").put("setback")));
        p.put("combat_active",new JSONObject().put("type","boolean"));p.put("enemy_name",str(64));p.put("enemy_status",str(60));p.put("enemy_telegraph",str(100));p.put("combat_distance",new JSONObject().put("type","string").put("enum",new JSONArray().put("close").put("mid").put("far")));p.put("combat_hazard",str(70));
        p.put("enemy_hp",integer(0,10000));p.put("enemy_hp_max",integer(0,10000));p.put("combat_round",integer(0,999));

        JSONObject lootItem=new JSONObject().put("type","object").put("additionalProperties",false);
        JSONObject lp=new JSONObject();
        lp.put("name",str());
        lp.put("category",new JSONObject().put("type","string").put("enum",new JSONArray().put("weapon").put("offhand").put("head").put("chest").put("hands").put("legs").put("feet").put("belt").put("neck").put("ring").put("utility").put("relic").put("potion").put("combat_consumable").put("food").put("material").put("tool").put("artifact")));
        lp.put("rarity",new JSONObject().put("type","string").put("enum",new JSONArray().put("common").put("uncommon").put("rare").put("epic").put("legendary").put("mythic").put("ancient").put("unique")));
        lp.put("description",str());
        lootItem.put("properties",lp).put("required",new JSONArray().put("name").put("category").put("rarity").put("description"));
        p.put("loot",new JSONObject().put("type","array").put("minItems",0).put("maxItems",3).put("items",lootItem));
        schema.put("properties",p);
        JSONArray req=new JSONArray();
        for(String k:new String[]{"scene_title","scene","choices","location","time_minutes","hp_delta","mana_delta","stamina_delta","aeonic_delta","crowns_delta","quest_note","asterra_delta","dravenn_delta","lysara_delta","event_tag","combat_active","enemy_name","enemy_status","enemy_telegraph","combat_distance","combat_hazard","enemy_hp","enemy_hp_max","combat_round","loot"})req.put(k);
        schema.put("required",req);
        return new JSONObject().put("type","json_schema").put("json_schema",new JSONObject().put("name","vaeloria_turn").put("strict",true).put("schema",schema));
    }
    static JSONObject responseFormatForTest()throws Exception{return responseFormat();}
    private static JSONObject str()throws Exception{return new JSONObject().put("type","string");}
    private static JSONObject str(int maxLength)throws Exception{return str().put("maxLength",maxLength);}
    private static JSONObject integer(int min,int max)throws Exception{return new JSONObject().put("type","integer").put("minimum",min).put("maximum",max);}
    private static String read(InputStream in)throws Exception{if(in==null)return"";try(BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){StringBuilder b=new StringBuilder();String line;while((line=r.readLine())!=null)b.append(line);return b.toString();}}
    private static String apiError(String body){try{String m=new JSONObject(body).getJSONObject("error").optString("message",body);return m.length()>500?m.substring(0,500)+"…":m;}catch(Exception e){return body==null?"":(body.length()>500?body.substring(0,500)+"…":body);}}
}
