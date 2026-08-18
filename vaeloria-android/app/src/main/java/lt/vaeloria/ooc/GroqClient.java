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
    private static volatile HttpURLConnection activeConnection;
    private GroqClient(){}

    public static JSONObject resolveTurn(String apiKey,GameState s,String action,String equipped,List<String[]> abilities,StatEngine.Check check)throws Exception{
        return resolveTurn(apiKey,s,action,equipped,abilities,check,"");
    }

    public static JSONObject resolveTurn(String apiKey,GameState s,String action,String equipped,List<String[]> abilities,StatEngine.Check check,String worldContext)throws Exception{
        JSONObject req=new JSONObject();
        req.put("model",MODEL);
        req.put("reasoning_effort","low");
        req.put("max_completion_tokens",MAX_COMPLETION_TOKENS);
        req.put("messages",messages(s,action,equipped,abilities,check,worldContext));
        req.put("response_format",responseFormat());

        Exception last=null;
        for(int attempt=0;attempt<2;attempt++){
            if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("Užklausa atšaukta");
            HttpURLConnection c=null;
            try{
                c=(HttpURLConnection)new URL(ENDPOINT).openConnection();
                activeConnection=c;
                c.setConnectTimeout(15000);c.setReadTimeout(60000);c.setRequestMethod("POST");
                c.setRequestProperty("Authorization","Bearer "+apiKey);c.setRequestProperty("Content-Type","application/json");c.setDoOutput(true);
                try(OutputStream os=c.getOutputStream()){os.write(req.toString().getBytes(StandardCharsets.UTF_8));}
                int code=c.getResponseCode();InputStream stream=code>=200&&code<300?c.getInputStream():c.getErrorStream();String body=read(stream);
                if(code>=200&&code<300){
                    String content=new JSONObject(body).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                    return LithuanianNarrative.polish(new JSONObject(content),s);
                }
                String msg="Groq HTTP "+code+": "+apiError(body);
                if((code==429||code>=500)&&attempt==0){last=new IllegalStateException(msg);Thread.sleep(1200);continue;}
                throw new IllegalStateException(msg);
            }catch(SocketTimeoutException|UnknownHostException e){
                last=e;if(attempt==0){Thread.sleep(700);continue;}throw e;
            }finally{if(c!=null)c.disconnect();if(activeConnection==c)activeConnection=null;}
        }
        throw last==null?new IllegalStateException("Groq užklausa nepavyko"):last;
    }

    static void cancelActive(){HttpURLConnection connection=activeConnection;if(connection!=null)connection.disconnect();}

    public static JSONObject resolveTurn(String apiKey,GameState s,String action,String equipped,List<String[]> abilities)throws Exception{
        return resolveTurn(apiKey,s,action,equipped,abilities,null);
    }

    private static JSONArray messages(GameState s,String action,String equipped,List<String[]> abilities,StatEngine.Check check,String worldContext)throws Exception{
        JSONArray m=new JSONArray();
        JSONObject sys=new JSONObject();sys.put("role","system");
        sys.put("content",systemPrompt());
        m.put(sys);
        JSONObject user=new JSONObject();user.put("role","user");
        user.put("content",userPrompt(s,action,equipped,abilities,check,worldContext));
        m.put(user);return m;
    }

    private static String systemPrompt(){
        return "Tu esi Vaeloria žaidimo meistras.\n"
                +"KALBA IR RIŠLUMAS. Visą žaidėjui matomą tekstą rašyk taisyklinga, natūralia ir rišlia lietuvių kalba. Vartok pilnus sakinius, natūralią žodžių tvarką ir aiškias įvardžių nuorodas. Viename sakinyje dėstyk vieną pagrindinę mintį. Rašyk antruoju asmeniu ir esamuoju laiku („tu“), kad nereikėtų linksniuoti veikėjo vardo ar spėti jo giminės. Nekartok tos pačios informacijos ir nepalik neaiškių „jis“, „tai“ ar „ten“ be aiškaus atitikmens. Nenaudok Markdown antraščių, sąrašų ar paryškinimo žaidimo teksto laukuose.\n"
                +"LIETUVIŠKI TERMINAI. Venk pažodinių vertinių ir anglicizmų. Vartok „kelionės vartai“, ne waygate; „įranga“, ne gear; „grobis“ arba „atlygis“, ne loot; „kova“, ne combat; „atradimas“, ne discovery; „siužeto gija“, ne thread. Išimtis taikoma tik Vaelorios tikriniams vardams ir vidiniams JSON laukų pavadinimams.\n"
                +"SCENA. Tęsk ankstesnės scenos priežastis ir pasekmes. scene lauką sudaryk iš 2–4 trumpų, tarpusavyje logiškai susietų sakinių. Pirmu sakiniu aiškiai įvardyk atliktą veiksmą arba tiesioginį jo rezultatą, kitu – pasaulio reakciją ar naują informaciją. quest_note rašyk vienu glaustu sakiniu tik tada, kai užduoties tikslas iš tikrųjų pasikeičia; kitu atveju pakartok esamą tikslą.\n"
                +"PASIRINKIMAI. Pateik lygiai 3 materialiai skirtingus pasirinkimus. Kiekvieną pradėk aiškiu veiksmažodžiu, suformuluok glaustai ir nekartok tos pačios prasmės kitais žodžiais.\n"
                +"PASAULIO TAISYKLĖS. Žaidėjas deklaruoja bandymą, o ne rezultatą. Pasaulis, jo veikėjai ir frakcijos turi savarankiškus interesus bei realias pasekmes. Aukštos savybės automatiškai neišsprendžia nežinomų taisyklių, politikos ar kito veikėjo valios. Nekurk nemokamų daiktų ar nepagrįstų pergalių. Jei nėra pagrindo išteklių, pinigų ar frakcijos įtakos pokyčiui, atitinkama delta turi būti 0.\n"
                +"AUTORITETINGA BŪSENA. Struktūrizuotos užduotys, NPC atmintis, parduotuvių atsargos, ekonomika, frakcijų santykiai, kompanionai, daiktų efektai ir kovos matematika priklauso telefono varikliui. Jų neperrašyk ir neprieštarauk WORLD STATE santraukai. Tu pateiki natūralų pasakojimą apie patvirtintą būseną.\n"
                +"PATIKRA. Kai pateikta PRIVALOMA SAVYBĖS PATIKRA, jos skaitinį rezultatą, įskaitant veikėjo kilmės, archetipo, bruožų ir meistriškumo poveikį, laikyk nekintamu telefono variklio sprendimu. Negali nesėkmės paversti sėkme ar sėkmės nesėkme. Pasekmės mastą ir kainą derink prie nurodyto rezultato.\n"
                +"KOVA. Kova turi trukti kelis ėjimus ir aiškiai rodyti priešo ketinimą. Kai combat_active=true, užpildyk visus kovos laukus; kai false, kovos tekstiniai laukai turi būti tušti. Pasaulio bosą rink tik siužetiškai pagrįstai. Iliustruotas bestiarijus: "+EnemyCatalogV091.promptRoster()+". Kovos grobį autoritetingai apskaičiuoja telefono v1.0.0 iškritimo lentelė, todėl jo nedėk į loot masyvą. Pergalei naudok event_tag=combat_victory ir combat_active=false; sąmoningam atsitraukimui – combat_escape. Aktyvioje kovoje išlaikyk nuoseklias enemy_hp, enemy_hp_max ir combat_round reikšmes.\n"
                +"DAIKTAI. Ne kovos loot leidžiamas tik kai scena aiškiai pagrindžia radinį ar atlygį. Dėvimos įrangos kategorijos: weapon, offhand, head, chest, hands, legs, feet, belt, neck, ring, utility, relic; artefaktui naudok artifact. Retumai apima common, uncommon, rare, epic, legendary, mythic, ancient ir unique.";
    }

    private static String userPrompt(GameState s,String action,String equipped,List<String[]> abilities,StatEngine.Check check,String worldContext){
        StringBuilder abilityNames=new StringBuilder();
        if(abilities!=null)for(String[] ability:abilities){if(ability==null||ability.length==0)continue;if(abilityNames.length()>0)abilityNames.append("; ");abilityNames.append(ability[0]);}
        String recent=s.recentTurns.isEmpty()?"nėra":String.join(" | ",s.recentTurns.subList(Math.max(0,s.recentTurns.size()-6),s.recentTurns.size()));
        return "ŽAIDIMO BŪSENA\n"
                +"Veikėjas. "+CharacterCatalogV093.profilePrompt(s)+" Amžius: "+CharacterCatalogV093.ageLine(s)+". Progresijos režimas: "+("legendary".equals(s.progressionMode)?"legendinis, 92 bazinės savybės pasiekusios ribą":"subalansuotas, bazinės savybės auga nuo pasirinktos kilmės ir archetipo")+"; veikėjo lygis "+s.level+", patirtis "+s.experience+"/"+s.experienceNext+", sunkumas "+s.difficulty+". Meistriškumas yra atskira naudojamų savybių pažanga.\n"
                +"Vieta: "+s.location+". Metai: "+s.worldYear+". Pasaulio minutė: "+s.worldMinute+".\n"
                +"Ištekliai: gyvybė "+s.hp+"/"+s.hpMax+", mana "+s.mana+"/"+s.manaMax+", ištvermė "+s.stamina+"/"+s.staminaMax+", eoninė energija "+s.aeonic+"/"+s.aeonicMax+", karūnos "+s.crowns+".\n"
                +"Kova: "+(s.combatActive?(s.enemyName+" · gyvybė "+s.enemyHp+"/"+s.enemyHpMax+" · ėjimas "+s.combatRound):"neaktyvi")+".\n"
                +"Siužetas: "+s.questTitle+". Dabartinis tikslas: "+s.objective+"\n"
                +"Frakcijos: Asterra "+s.asterraInfluence+" ("+s.asterraRelation+"), Dravenn "+s.dravennInfluence+" ("+s.dravennRelation+"), Lysara "+s.lysaraInfluence+" ("+s.lysaraRelation+").\n"
                +"Dėvima įranga: "+(equipped==null?"nėra":equipped)+"\nGebėjimai: "+(abilityNames.length()==0?"nėra":abilityNames)+"\n"
                +"Struktūrizuota pasaulio atmintis: "+(worldContext==null||worldContext.isEmpty()?"nėra papildomų įrašų":worldContext)+"\n"
                +"Dabartinė scena: "+s.scene+"\nPaskutiniai ėjimai: "+recent+"\n\n"
                +"ŽAIDĖJO VEIKSMAS: "+(action==null?"":action)+"\n\n"+(check==null?"":check.prompt());
    }

    static String systemPromptForTest(){return systemPrompt();}
    static String userPromptForTest(GameState state){return userPrompt(state,"Ištirti Meridiano poslinkį","nėra",java.util.Collections.emptyList(),null,"");}

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
    private static String read(InputStream in)throws Exception{if(in==null)return"";try(BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){StringBuilder b=new StringBuilder();String line;while((line=r.readLine())!=null){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("Užklausa atšaukta");b.append(line);}return b.toString();}}
    private static String apiError(String body){try{String m=new JSONObject(body).getJSONObject("error").optString("message",body);return m.length()>500?m.substring(0,500)+"…":m;}catch(Exception e){return body==null?"":(body.length()>500?body.substring(0,500)+"…":body);}}
}
