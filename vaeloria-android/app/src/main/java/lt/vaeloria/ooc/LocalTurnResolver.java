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
                    title="Kelionė nepradėta";minutes=0;stamina=0;event="none";
                    scene="Šiuo keliu dar negali keliauti. Atlase pasirink jau atrastą vietą arba pirmiausia ištirk naują kryptį.";
                }else{
                    title="Kelionė";minutes=45;stamina=-5;event="travel";
                    scene="Leidiesi į kelią. Kelionė reikalauja laiko ir ištvermės. Pasieki vietą: "+location+".";
                }
                c.put("Apsidairyti ir įvertinti aplinką").put("Ieškoti vietinių kontaktų").put("Patikrinti kelionės užrašus");
            }else if(q.contains("manifest")){
                title="Manifesto neatitikimas";minutes=12;stamina=0;event="discovery";
                scene="Palyginęs registracijos žymas, antspaudą ir pristatymo seką randi tikrą neatitikimą: manifesto įrašas sistemoje atsirado anksčiau, nei karavanas galėjo fiziškai pasiekti Luminara. Tai jau ne gandas, o patikrinamas Meridiano poslinkio pėdsakas.";
                questNote="Patvirtintas pirmas lauko įrodymas: manifesto registracijos laikas nesutampa su fiziniu karavano atvykimu.";
                c.put("Patikrinti Meridiano vartų žurnalą").put("Surasti karavano liudininkus").put("Palyginti įrašą su Veyrhold duomenimis");
            }else if(q.contains("meridian")||q.contains("vart")||q.contains("waygate")){
                title="Vartų rezonansas";minutes=15;stamina=-2;aeonic=q.contains("kalib")?-4:0;event="discovery";
                scene="Prie vartų rezonanso laukas pulsuoja nevienodu ritmu. Keli matavimo taškai rodo tą pačią kryptį: problema nėra vien laikrodžių paklaida — pats atvykimo eiliškumas trumpam persislenka.";
                c.put("Matuoti poslinkį dar kartą").put("Sekti paskutinio atvykimo pėdsaką").put("Klausti vartų prižiūrėtojo apie ankstesnius atvejus");
            }else if(q.contains("poils")||q.contains("mieg")||q.contains("laukti")||q.contains("palaukt")||q.contains("stovykl")){
                title="Trumpas atokvėpis";minutes=30;stamina=12;mana=4;event="rest";
                scene="Skiri laiko atsikvėpti ir stebėti aplinką be skubėjimo. Kvėpavimas nurimsta, judesiai vėl tampa lengvi, o per tą laiką miestas gyvena toliau — sargybos keičiasi, prekybininkai juda, gandai sklinda.";
                c.put("Grįžti prie Meridiano tyrimo").put("Patikrinti naujus gandus").put("Apsilankyti centrinėje rinkoje");
            }else if(q.contains("kalb")||q.contains("paklaus")||q.contains("susisiekt")||q.contains("pasikalb")){
                title="Pokalbis";minutes=10;stamina=0;event="dialogue";
                scene="Pokalbis neduoda tobulo atsakymo, bet atskiria faktus nuo nuomonių. Vietiniai sutaria dėl vieno: pastaruoju metu vartų anomalijos kartojasi dažniau, tačiau skirtingi žmonės jas aiškina skirtingai.";
                c.put("Paprašyti konkretaus liudijimo").put("Paklausti, kas galėtų žinoti daugiau").put("Užrašyti informaciją ir tęsti tyrimą");
            }else if(q.contains("puol")||q.contains("kov")||q.contains("smūg")||q.contains("smug")||q.contains("ataka")||q.contains("ataku")||q.contains("pulti")){
                title="Kova prasideda";minutes=2;stamina=-8;hp=-2;event="combat";combat=true;
                if(enemy==null||enemy.isEmpty()){
                    EnemyCatalogV091.Enemy requested=EnemyCatalogV091.find(a);
                    EnemyCatalogV091.Enemy encounter=requested!=null?requested:EnemyCatalogV091.encounterFor(state.location,state.worldMinute+a.hashCode());
                    enemy=encounter.name;
                    enemyHp=encounter.hp;enemyHpMax=encounter.hp;combatRound=1;
                    enemyStatus="Budrus · pavojus "+encounter.danger+"/10 · gyvybė "+enemyHp+"/"+enemyHpMax;
                }else{
                    int damage=check==null?52:(check.outcome.contains("išskirtinė")?96:check.outcome.equals("sėkmė")?72:check.outcome.contains("dalinė")?48:26);
                    enemyHp=Math.max(0,enemyHp-damage);combatRound++;
                    if(enemyHp==0){title="Priešas nugalėtas";event="combat_victory";combat=false;hp=0;stamina=-4;scene="Tavo veiksmas pralaužia paskutinę priešininko gynybą. Kova baigta, o patvirtinta būtybės iškritimo lentelė pritaikoma vietiniame žaidimo variklyje.";c.put("Apžiūrėti gautą grobį").put("Atsigauti po kovos").put("Tęsti kelionę");}
                    else{enemyStatus="Spaudžiamas · gyvybė "+enemyHp+"/"+enemyHpMax;title="Kovos "+combatRound+" ėjimas";scene="Ataka pasiekia tikslą, tačiau priešininkas dar laikosi. Jo laikysena keičiasi pagal likusią gyvybę, todėl kitas veiksmas vis dar turi kainą ir riziką.";}
                }
                telegraph="Žemas žingsnis į šoną ir pasiruošimas kontratakai";
                distance="close";
                hazard="Slidus akmuo ir siauras praėjimas";
                if(combatRound==1){scene="Tu inicijuoji kontaktą. Priešininkas atsitraukia tik pusę žingsnio ir iškart persitvarko kontratakai; erdvė ankšta, todėl pozicija tampa svarbesnė už gryną jėgą.";}
                if(c.length()==0)c.put("Spausti ir neleisti atkurti distancijos").put("Išprovokuoti kontrataką ir bausti ją").put("Atsitraukti į saugesnę poziciją");
            }else if(q.contains("bėg")||q.contains("beg")||q.contains("trauktis")||q.contains("atsitrauk")){
                title="Atsitraukimas";minutes=4;stamina=-4;event="combat_escape";combat=false;
                enemy="";enemyStatus="";telegraph="";distance="mid";hazard="";enemyHp=0;enemyHpMax=0;combatRound=0;
                scene="Nutrauki kontaktą ir pasirenki erdvę, kurioje gali vėl vertinti situaciją. Priešininkas tavęs iškart nesiveja — kova baigiasi be aiškios pergalės, bet iniciatyva grįžta tau.";
                c.put("Stebėti, ar kas nors seka").put("Grįžti prie pagrindinės užduoties").put("Atsigauti prieš tęsiant kelią");
            }else if(q.contains("tirti")||q.contains("ištirt")||q.contains("istirt")||q.contains("apžiūr")||q.contains("apziur")||q.contains("iešk")||q.contains("iesk")||q.contains("sekti")){
                title="Tyrimas";minutes=14;stamina=-2;event="discovery";
                scene="Sistemingai tikrini aplinką ir atmeti pirmus akivaizdžius paaiškinimus. Randi kelias smulkias detales, kurios atskirai nieko neįrodo, bet kartu parodo kryptį, kurią verta tikrinti toliau.";
                c.put("Patikrinti stipriausią pėdsaką").put("Palyginti radinius su ankstesniais įrašais").put("Paklausti vietinio liudininko");
            }else{
                title="Veiksmas pasaulyje";minutes=7;event="action";
                scene="Atlieki: „"+a+"“. Veiksmas turi pasekmę: praeina laikas, keičiasi tavo pozicija situacijoje, o aplinka pateikia naują informaciją vietoje ankstesnio statiško ekrano.";
                c.put("Tęsti tą pačią kryptį").put("Apsidairyti ir įvertinti pasekmes").put("Grįžti prie Lūžusio Meridiano užduoties");
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
        String q=text.toLowerCase(Locale.forLanguageTag("lt-LT"));
        return q.startsWith("keliaut")||q.startsWith("vykti")||q.startsWith("eiti į")||q.startsWith("eiti i ")||q.startsWith("važiuot")||q.startsWith("vaziuot");
    }
    static String destination(String action,String current,java.util.Set<String> discovered){
        String q=action.toLowerCase(Locale.forLanguageTag("lt-LT"));
        int to=Math.max(q.lastIndexOf(" į "),q.lastIndexOf(" i "));
        if(to>=0)q=q.substring(to+3);
        for(String name:discovered){
            String target=name.toLowerCase(Locale.forLanguageTag("lt-LT"));
            if(java.util.regex.Pattern.compile("(?<![\\p{L}])"+java.util.regex.Pattern.quote(target)+"(?![\\p{L}])").matcher(q).find())return name;
        }
        return current;
    }
    private LocalTurnResolver(){}
}
