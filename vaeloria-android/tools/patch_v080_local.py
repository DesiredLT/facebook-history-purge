from pathlib import Path

p = Path('app/src/main/java/lt/vaeloria/ooc/VaeloriaActivity.java')
s = p.read_text(encoding='utf-8')
start_marker = '    JSONObject local(String action){'
end_marker = '    void key(){'
start = s.find(start_marker)
end = s.find(end_marker, start)
if start < 0 or end < 0:
    raise SystemExit('Could not locate VaeloriaActivity.local() boundaries')

method = r'''    JSONObject local(String action,StatEngine.Check check){
        try{
            String a=action==null?"":action.trim();
            String q=a.toLowerCase(Locale.ROOT);
            JSONObject o=new JSONObject();
            JSONArray c=new JSONArray();
            String title="Veiksmas įvykdytas";
            String scene="Einoras imasi veiksmo: „"+a+"“. Aplinka sureaguoja, laikas juda pirmyn, o rezultatas įrašomas į vietinę pasaulio būseną.";
            String location=state.location;
            String questNote=state.objective;
            String event="action";
            int minutes=6,hp=0,mana=0,stamina=-1,aeonic=0;
            long crowns=0;
            boolean combat=state.combatActive;
            String enemy=combat?state.enemyName:"";
            String enemyStatus=combat?state.enemyStatus:"";
            String telegraph=combat?state.enemyTelegraph:"";
            String distance=combat?state.combatDistance:"mid";
            String hazard=combat?state.combatHazard:"";

            if(q.contains("manifest")){
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
                title="Pokalbis Luminara";minutes=10;stamina=0;event="dialogue";
                scene="Pokalbis neduoda tobulo atsakymo, bet atskiria faktus nuo nuomonių. Vietiniai sutaria dėl vieno: pastaruoju metu vartų anomalijos kartojasi dažniau, tačiau skirtingi žmonės jas aiškina skirtingai.";
                c.put("Paprašyti konkretaus liudijimo").put("Paklausti, kas galėtų žinoti daugiau").put("Užrašyti informaciją ir tęsti tyrimą");
            }else if(q.contains("puol")||q.contains("kov")||q.contains("smūg")||q.contains("smug")||q.contains("ataka")||q.contains("pulti")){
                title="Kova prasideda";minutes=2;stamina=-8;hp=-2;event="combat";combat=true;
                if(enemy==null||enemy.isEmpty())enemy="Kelio plėšikas";
                enemyStatus="Budrus · spaudžiamas";
                telegraph="Žemas žingsnis į šoną ir pasiruošimas kontratakai";
                distance="close";
                hazard="Slidus akmuo ir siauras praėjimas";
                scene="Tu inicijuoji kontaktą. Priešininkas atsitraukia tik pusę žingsnio ir iškart persitvarko kontratakai; erdvė ankšta, todėl pozicija tampa svarbesnė už gryną jėgą.";
                c.put("Spausti ir neleisti atkurti distancijos").put("Išprovokuoti kontrataką ir bausti ją").put("Atsitraukti į saugesnę poziciją");
            }else if(q.contains("bėg")||q.contains("beg")||q.contains("trauktis")||q.contains("atsitrauk")){
                title="Atsitraukimas";minutes=4;stamina=-4;event="combat_end";combat=false;
                enemy="";enemyStatus="";telegraph="";distance="mid";hazard="";
                scene="Nutrauki kontaktą ir pasirenki erdvę, kurioje gali vėl vertinti situaciją. Priešininkas tavęs iškart nesiveja — kova baigiasi be aiškios pergalės, bet iniciatyva grįžta tau.";
                c.put("Stebėti, ar kas nors seka").put("Grįžti prie pagrindinės užduoties").put("Atsigauti prieš tęsiant kelią");
            }else if(q.contains("vykti")||q.contains("keliaut")||q.contains("keliauti")||q.contains("eiti į")||q.contains("eiti i")||q.contains("važiuoti")||q.contains("vaziuoti")){
                title="Kelionė";minutes=45;stamina=-5;event="travel";
                if(q.contains("veyrhold"))location="Veyrhold";
                else if(q.contains("stiklo")||q.contains("glasswood"))location="Stiklo Giria";
                else if(q.contains("kharad"))location="Kharad Vorn";
                else if(q.contains("safyro")||q.contains("sapphire"))location="Safyro Platybės";
                else if(q.contains("šventųjų")||q.contains("sventuju")||q.contains("pelkyn"))location="Šventųjų Pelkynas";
                else if(q.contains("labirint"))location="Žaliasis Labirintas";
                else if(q.contains("asterio")||q.contains("crown of aster"))location="Asterio Karūna";
                scene="Palieki "+state.location+" ir judi pasirinkta kryptimi. Kelionė užima laiko ir ištvermės; pakeliui stebi kelią, žmonių judėjimą bei Meridiano infrastruktūros ženklus. Pasieki: "+location+".";
                c.put("Apsidairyti naujoje vietoje").put("Ieškoti vietinių kontaktų").put("Patikrinti, ar čia jaučiamas Meridiano poslinkis");
            }else if(q.contains("tirti")||q.contains("ištirt")||q.contains("istirt")||q.contains("apžiūr")||q.contains("apziur")||q.contains("iešk")||q.contains("iesk")||q.contains("sekti")){
                title="Tyrimas";minutes=14;stamina=-2;event="discovery";
                scene="Sistemingai tikrini aplinką ir atmeti pirmus akivaizdžius paaiškinimus. Randi kelias smulkias detales, kurios atskirai nieko neįrodo, bet kartu parodo kryptį, kurią verta tikrinti toliau.";
                c.put("Patikrinti stipriausią pėdsaką").put("Palyginti radinius su ankstesniais įrašais").put("Paklausti vietinio liudininko");
            }else{
                title="Veiksmas pasaulyje";minutes=7;event="action";
                scene="Atlieki: „"+a+"“. Veiksmas turi pasekmę: praeina laikas, keičiasi tavo pozicija situacijoje, o aplinka pateikia naują informaciją vietoje ankstesnio statiško ekrano.";
                c.put("Tęsti tą pačią kryptį").put("Apsidairyti ir įvertinti pasekmes").put("Grįžti prie Lūžusio Meridiano užduoties");
            }

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
            o.put("event_tag",event);
            o.put("combat_active",combat);
            o.put("enemy_name",combat?enemy:"");
            o.put("enemy_status",combat?enemyStatus:"");
            o.put("enemy_telegraph",combat?telegraph:"");
            o.put("combat_distance",combat?distance:"mid");
            o.put("combat_hazard",combat?hazard:"");
            o.put("loot",new JSONArray());
            return o;
        }catch(Exception e){return new JSONObject();}
    }

'''

patched = s[:start] + method + s[end:]
p.write_text(patched, encoding='utf-8')
print('v0.8 local resolver patched:', p)
