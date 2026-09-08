package lt.vaeloria.ooc;

import org.json.*;
import java.util.Locale;

final class LocalTurnResolver {
    static JSONObject resolve(GameState state,String action,StatEngine.Check check,java.util.Set<String> discovered){
        try{
            String a=action==null?"":action.trim();
            String q=a.toLowerCase(Locale.ROOT);
            JSONObject o=new JSONObject();
            JSONArray c=new JSONArray();
            String title="Veiksmas įvykdytas";
            String scene="Atlieki veiksmą: „"+a+"“. Aplinka sureaguoja, laikas juda pirmyn, o rezultatas įrašomas į vietinę pasaulio būseną.";
            String location=state.location;
            String questNote=state.objective;
            String event="action";
            int minutes=6,hp=0,mana=0,stamina=-1,aeonic=0;
            int asterraDelta=0,dravennDelta=0,lysaraDelta=0;
            long crowns=0;
            boolean combat=state.combatActive;
            String enemy=combat?state.enemyName:"";
            String enemyStatus=combat?state.enemyStatus:"";
            String telegraph=combat?state.enemyTelegraph:"";
            String distance=combat?state.combatDistance:"mid";
            String hazard=combat?state.combatHazard:"";
            int enemyHp=combat?state.enemyHp:0;
            int enemyHpMax=combat?state.enemyHpMax:0;
            int combatRound=combat?state.combatRound:0;

            if(isTravelIntent(q)){
                location=destination(a,state.location,discovered);
                if(location.equals(state.location)){
                    title="Kelionė nepradėta";minutes=0;stamina=0;event="none";o.put("blocked",true);
                    scene="Šiuo keliu dar negali keliauti. Atlase pasirink jau atrastą vietą arba pirmiausia ištirk naują kryptį.";
                }else{
                    title="Kelionė";minutes=45;stamina=-5;event="travel";
                    scene="Leidiesi į kelią. Kelionė reikalauja laiko ir ištvermės. Pasieki vietą: "+location+".";
                }
                c.put("Apsidairyti ir įvertinti aplinką").put("Ieškoti vietinių kontaktų").put("Patikrinti kelionės užrašus");
            }else if(q.contains("manifest")&&!ActionText.rest(q)&&!ActionText.social(q)){
                title="Manifesto neatitikimas";minutes=12;stamina=0;event="discovery";
                scene="Palyginęs registracijos žymas, antspaudą ir pristatymo seką randi tikrą neatitikimą: manifesto įrašas sistemoje atsirado anksčiau, nei karavanas galėjo fiziškai pasiekti Luminara. Tai jau ne gandas, o patikrinamas Meridiano poslinkio pėdsakas.";
                questNote="Patvirtintas pirmas lauko įrodymas: manifesto registracijos laikas nesutampa su fiziniu karavano atvykimu.";
                c.put("Patikrinti Meridiano vartų žurnalą").put("Surasti karavano liudininkus").put("Palyginti įrašą su Veyrhold duomenimis");
            }else if((q.contains("meridian")||q.contains("vart")||q.contains("waygate"))&&!ActionText.rest(q)&&!ActionText.social(q)){
                title="Vartų rezonansas";minutes=15;stamina=-2;aeonic=q.contains("kalib")?-4:0;event="discovery";
                scene="Prie vartų rezonanso laukas pulsuoja nevienodu ritmu. Keli matavimo taškai rodo tą pačią kryptį: problema nėra vien laikrodžių paklaida — pats atvykimo eiliškumas trumpam persislenka.";
                c.put("Matuoti poslinkį dar kartą").put("Sekti paskutinio atvykimo pėdsaką").put("Klausti vartų prižiūrėtojo apie ankstesnius atvejus");
            }else if(ActionText.rest(q)){
                title="Trumpas atokvėpis";minutes=30;stamina=12;mana=4;hp=8;event="rest";
                scene="Sustoji saugioje užuovėjoje, susitvarkai žaizdas ir pailsi. Kvėpavimas nurimsta, o sukauptos jėgos leidžia tęsti kelionę.";
                c.put("Grįžti prie Meridiano tyrimo").put("Patikrinti naujus gandus").put("Apsilankyti centrinėje rinkoje");
            }else if(ActionText.social(q)){
                title="Pokalbis";minutes=10;stamina=0;event="dialogue";
                scene="Pokalbis neduoda tobulo atsakymo, bet atskiria faktus nuo nuomonių. Vietiniai sutaria dėl vieno: pastaruoju metu vartų anomalijos kartojasi dažniau, tačiau skirtingi žmonės jas aiškina skirtingai.";
                c.put("Paprašyti konkretaus liudijimo").put("Paklausti, kas galėtų žinoti daugiau").put("Užrašyti informaciją ir tęsti tyrimą");
            }else if(ActionText.contains(q,"tirti","tyrinėti","rinkti","apžiūr","iešk","sekti","apsidair","stebėt","patikrin","palygin","užfiks","patvirt","kalib","priim","atmest","siūlyt","pereiti","surasti","matuoti","užrašyti","įvertinti")){
                title="Tyrimas";minutes=14;stamina=-2;event="discovery";
                scene="Sistemingai tikrini aplinką ir atmeti pirmus akivaizdžius paaiškinimus. Randi kelias smulkias detales, kurios atskirai nieko neįrodo, bet kartu parodo kryptį, kurią verta tikrinti toliau.";
                c.put("Patikrinti stipriausią pėdsaką").put("Palyginti radinius su ankstesniais įrašais").put("Paklausti vietinio liudininko");
            }else{
                title="Patikslink veiksmą";minutes=0;stamina=0;event="none";o.put("blocked",true);
                scene="Pasirink siūlomą veiksmą arba konkrečiai nurodyk, kur keliauti, su kuo kalbėtis ar ką ištirti.";
                c.put("Apsidairyti ir įvertinti aplinką").put("Pailsėti saugioje vietoje").put("Ištirti dabartinės užduoties pėdsakus");
            }

            if("dialogue".equals(event)){asterraDelta+=1;lysaraDelta+=1;}
            if("combat".equals(event)){dravennDelta+=2;asterraDelta-=1;}
            if("discovery".equals(event)){asterraDelta+=1;}
            if("travel".equals(event)){if(location.contains("Kharad")||location.contains("Safyro"))dravennDelta+=1;else if(location.contains("Pelkyn")||location.contains("Labirint"))lysaraDelta+=1;else asterraDelta+=1;}
            if(c.length()==0)c.put("Apsidairyti ir rinkti daugiau informacijos").put("Patikrinti įrangą ir užrašus").put("Tęsti pagrindinę užduotį");
            o.put("scene_title",title);
            o.put("scene",scene);
            o.put("choices",c);
            o.put("location",location);
            o.put("time_minutes",minutes);
            o.put("hp_delta",hp);
            o.put("mana_delta",mana);
            o.put("stamina_delta",stamina);
            o.put("aeonic_delta",aeonic);
            o.put("crowns_delta",crowns);
            o.put("quest_note",questNote);
            o.put("asterra_delta",asterraDelta);o.put("dravenn_delta",dravennDelta);o.put("lysara_delta",lysaraDelta);
            o.put("event_tag",event);
            o.put("combat_active",combat);
            o.put("enemy_name",combat?enemy:"");
            o.put("enemy_status",combat?enemyStatus:"");
            o.put("enemy_telegraph",combat?telegraph:"");
            o.put("combat_distance",combat?distance:"mid");
            o.put("combat_hazard",combat?hazard:"");
            o.put("enemy_hp",combat?enemyHp:0);
            o.put("enemy_hp_max",combat?enemyHpMax:0);
            o.put("combat_round",combat?combatRound:0);
            o.put("loot",new JSONArray());
            return LithuanianNarrative.polish(o,state);
        }catch(Exception e){return new JSONObject();}
    }

    static boolean isTravelIntent(String text){
        return ActionText.travel(text);
    }
    static String destination(String action,String current,java.util.Set<String> discovered){
        String q=action.toLowerCase(Locale.forLanguageTag("lt-LT"));
        int to=Math.max(q.lastIndexOf(" į "),q.lastIndexOf(" i "));
        if(to>=0)q=q.substring(to+3);
        for(String name:discovered){
            if(ActionText.mentions(q,name))return name;
        }
        return current;
    }
    private LocalTurnResolver(){}
}
