from pathlib import Path
import re

# Vaeloria presentation + gameplay compatibility patch.
# Python imports sitecustomize automatically during GitHub Actions builds.
p = Path(__file__).resolve().parent / "v070" / "PremiumViewsV070.java"
if p.exists():
    s = p.read_text(encoding="utf-8")

    # 1) WORLD MAP
    s = s.replace("R.drawable.world_map_v070", "R.drawable.world_map_v060")
    old_nodes = 'add("Luminara",500,330,3);add("Asterio Karūna",350,195,4);add("Stiklo Giria",735,155,5);add("Veyrhold",665,255,3);add("Aureliono Pakraštys",760,405,4);add("Žvaigždėkritos Skliautas",540,495,7);add("Tuščiavidurė Smailė",320,555,8);add("Pelenų Karūnos Citadelė",965,210,7);add("Kharad Vorn",1140,280,5);add("Drakono Pabudimo Viršūnės",1330,155,8);add("Safyro Platybės",1010,535,7);add("Amžinojo Šaltinio Slėnis",915,650,4);add("Žaliasis Labirintas",1135,735,8);add("Šventųjų Pelkynas",790,755,6);'
    new_nodes = 'add("Luminara",388,405,3);add("Asterio Karūna",270,183,4);add("Stiklo Giria",505,180,5);add("Veyrhold",620,326,3);add("Aureliono Pakraštys",705,513,4);add("Žvaigždėkritos Skliautas",525,512,7);add("Tuščiavidurė Smailė",110,613,8);add("Pelenų Karūnos Citadelė",925,275,7);add("Kharad Vorn",1180,365,5);add("Drakono Pabudimo Viršūnės",1175,90,8);add("Safyro Platybės",967,600,7);add("Amžinojo Šaltinio Slėnis",770,741,4);add("Šventųjų Pelkynas",617,920,6);add("Žaliasis Labirintas",1190,790,8);'
    s = s.replace(old_nodes, new_nodes)
    s = s.replace("base=Math.min(getWidth()/(float)map.getWidth(),getHeight()/(float)map.getHeight());", "base=Math.max(getWidth()/(float)map.getWidth(),getHeight()/(float)map.getHeight());")
    s = s.replace("Math.min(4.7f,zoom*d.getScaleFactor())", "Math.min(4.5f,zoom*d.getScaleFactor())")
    s = s.replace("bd<76*76", "bd<70*70")
    s = s.replace("m=dp(60)", "m=dp(55)")

    # 2) PLAY / SCENE
    s = s.replace(
        "luminara=BitmapFactory.decodeResource(getResources(),R.drawable.scene_luminara_v070);wild=BitmapFactory.decodeResource(getResources(),R.drawable.scene_wild_v070);map=BitmapFactory.decodeResource(getResources(),R.drawable.world_map_v060);",
        "luminara=BitmapFactory.decodeResource(getResources(),R.drawable.scene_luminara);wild=BitmapFactory.decodeResource(getResources(),R.drawable.combat_forest);map=BitmapFactory.decodeResource(getResources(),R.drawable.world_map_v060);",
    )

    # 3) HERO
    s = s.replace("R.drawable.hero_einoras_v070", "R.drawable.hero_einoras")

    # 4) NPC PORTRAITS
    if "class NpcPortraitV070" not in s:
        s += r'''

class NpcPortraitV070 extends View {
    final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); String name="",role="";
    NpcPortraitV070(Context c){super(c);setLayerType(LAYER_TYPE_SOFTWARE,null);}
    void setNpc(String n,String r){name=n==null?"":n;role=r==null?"":r;invalidate();}
    protected void onDraw(Canvas c){
        float w=getWidth(),h=getHeight(),cx=w*.5f;int seed=Math.abs(name.hashCode());
        int ar=95+(seed%70),ag=92+((seed/7)%65),ab=78+((seed/13)%70);int accent=Color.rgb(ar,ag,ab);
        p.setShader(new LinearGradient(0,0,w,h,Color.rgb(11,22,29),Color.rgb(4,9,14),Shader.TileMode.CLAMP));c.drawRoundRect(0,0,w,h,Math.min(w,h)*.16f,Math.min(w,h)*.16f,p);p.setShader(null);
        p.setColor(Color.argb(70,Color.red(accent),Color.green(accent),Color.blue(accent)));c.drawCircle(cx,h*.37f,Math.min(w,h)*.42f,p);
        p.setColor(Color.rgb(20+(seed%18),27+((seed/5)%18),31+((seed/9)%20)));Path body=new Path();body.moveTo(w*.12f,h);body.quadTo(w*.20f,h*.68f,cx,h*.65f);body.quadTo(w*.80f,h*.68f,w*.88f,h);body.close();c.drawPath(body,p);
        p.setColor(Color.rgb(174+seed%34,142+(seed/3)%30,111+(seed/5)%25));c.drawRoundRect(w*.42f,h*.48f,w*.58f,h*.70f,w*.06f,w*.06f,p);c.drawOval(new RectF(w*.30f,h*.15f,w*.70f,h*.58f),p);
        p.setColor(Color.rgb(18+(seed%24),19+((seed/4)%23),19+((seed/8)%24)));Path hair=new Path();hair.moveTo(w*.27f,h*.39f);hair.quadTo(w*.28f,h*.08f,cx,h*.09f);hair.quadTo(w*.76f,h*.11f,w*.72f,h*.46f);hair.lineTo(w*.63f,h*.35f);hair.quadTo(w*.58f,h*.20f,w*.47f,h*.19f);hair.quadTo(w*.35f,h*.22f,w*.34f,h*.42f);hair.close();c.drawPath(hair,p);
        p.setColor(Color.rgb(216,224,211));c.drawCircle(w*.40f,h*.34f,Math.max(1,w*.018f),p);c.drawCircle(w*.60f,h*.34f,Math.max(1,w*.018f),p);
        p.setColor(Color.argb(120,80,53,40));p.setStrokeWidth(Math.max(1,w*.012f));c.drawLine(cx,h*.34f,cx-w*.018f,h*.43f,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1,w*.025f));p.setColor(accent);c.drawRoundRect(w*.05f,h*.05f,w*.95f,h*.95f,w*.14f,w*.14f,p);p.setStyle(Paint.Style.FILL);
    }
}
'''
    p.write_text(s, encoding="utf-8")

# 5) COMBAT
cb = Path(__file__).resolve().parent / "v060" / "PremiumViews.java"
if cb.exists():
    s = cb.read_text(encoding="utf-8")
    s = s.replace("RectF box=new RectF(dp(12),h-dp(62),w-dp(12),h-dp(10));", "RectF box=new RectF(dp(12),h-dp(90),w-dp(12),h-dp(10));")
    old = 'c.drawText(trim(tel,54),box.left+dp(10),box.top+dp(39),p);}'
    new = 'c.drawText(trim(tel,54),box.left+dp(10),box.top+dp(39),p);p.setTextSize(dp(8));p.setColor(Color.rgb(182,198,194));String st=state.enemyStatus==null||state.enemyStatus.isEmpty()?"Būsena nežinoma":state.enemyStatus;c.drawText("BŪSENA · "+trim(st,42),box.left+dp(10),box.top+dp(58),p);String hz=state.combatHazard==null||state.combatHazard.isEmpty()?"Aplinka stabili":state.combatHazard;p.setColor(Color.rgb(221,187,104));c.drawText("APLINKA · "+trim(hz,42),box.left+dp(10),box.top+dp(75),p);}'
    s = s.replace(old,new)
    cb.write_text(s,encoding="utf-8")

# Screen-level patches that keep v0.7 information architecture.
a = Path(__file__).resolve().parent / "v070" / "PolishedActivity.java"
if a.exists():
    s = a.read_text(encoding="utf-8")
    s = s.replace("c.addView(art,new LinearLayout.LayoutParams(-1,dp(252)));", "c.addView(art,new LinearLayout.LayoutParams(-1,dp(292)));")
    s = s.replace("c.addView(b,new LinearLayout.LayoutParams(-1,dp(300)));", "c.addView(b,new LinearLayout.LayoutParams(-1,dp(340)));")
    s = s.replace("ImageView art=image(R.drawable.hero_einoras_v070);", "ImageView art=image(R.drawable.hero_einoras);")

    # 6) ITEMS
    s = s.replace("ItemGlyphV070 icon=new ItemGlyphV070(this);icon.setItem(it.name,it.rarity);x.addView(icon,new LinearLayout.LayoutParams(-1,dp(92)));", "ItemIconView icon=new ItemIconView(this);icon.setItem(it.name,it.slot,it.rarity);x.addView(icon,new LinearLayout.LayoutParams(-1,dp(92)));")

    # 7) POLISH
    s = s.replace("if(pref(\"animations\",true)){v.setAlpha(0);v.setTranslationY(dp(7));v.animate().alpha(1).translationY(0).setDuration(170).start();}", "if(pref(\"animations\",true)){v.setAlpha(0);v.setTranslationY(dp(7));v.setScaleX(.99f);v.setScaleY(.99f);v.animate().alpha(1).translationY(0).scaleX(1f).scaleY(1f).setDuration(190).start();}")
    s = s.replace("r.setOnClickListener(v->act(a));return r;", "r.setOnTouchListener((v,e)->{if(pref(\"animations\",true)){if(e.getAction()==MotionEvent.ACTION_DOWN)v.animate().scaleX(.985f).scaleY(.985f).setDuration(55).start();else if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL)v.animate().scaleX(1f).scaleY(1f).setDuration(85).start();}return false;});r.setOnClickListener(v->{haptic();sound(false);act(a);});return r;")
    a.write_text(s, encoding="utf-8")

# NPC hub inherited from PremiumActivity.
n = Path(__file__).resolve().parent / "v060" / "PremiumActivity.java"
if n.exists():
    s = n.read_text(encoding="utf-8")
    old = 'if("Lyra Fen".equals(n[0]))r.addView(image(R.drawable.npc_lyra),new LinearLayout.LayoutParams(dp(54),dp(64)));else{TextView i=serif(n[0].substring(0,1),20,GOLD2,true);i.setGravity(Gravity.CENTER);i.setBackground(round(Color.rgb(24,34,38),40,Color.argb(170,221,187,104)));r.addView(i,new LinearLayout.LayoutParams(dp(54),dp(54)));}'
    new = 'NpcPortraitV070 i=new NpcPortraitV070(this);i.setNpc(n[0],n[1]);r.addView(i,new LinearLayout.LayoutParams(dp(58),dp(68)));'
    s = s.replace(old,new)
    s = s.replace('if("Lyra Fen".equals(n[0]))c.addView(image(R.drawable.npc_lyra),new LinearLayout.LayoutParams(-1,dp(220)));', 'NpcPortraitV070 portrait=new NpcPortraitV070(this);portrait.setNpc(n[0],n[1]);c.addView(portrait,new LinearLayout.LayoutParams(-1,dp(220)));')
    n.write_text(s,encoding="utf-8")

# 8) REAL LOCAL GAME LOOP: offline actions must mutate world state instead of only reloading UI.
core = Path(__file__).resolve().parent / "app" / "src" / "main" / "java" / "lt" / "vaeloria" / "ooc" / "VaeloriaActivity.java"
if core.exists():
    s = core.read_text(encoding="utf-8")
    pattern = r'\n    JSONObject local\(String action\)\{.*?\n    \}\n\n    void key\(\)\{'
    local_method = r'''
    JSONObject local(String action){
        try{
            String a=action==null?"":action.trim();String q=a.toLowerCase(Locale.ROOT);
            JSONObject o=new JSONObject();JSONArray c=new JSONArray();
            String title="Veiksmas įvykdytas";String scene="Einoras imasi veiksmo: „"+a+"“. Aplinka sureaguoja, laikas juda pirmyn, o rezultatas įrašomas į vietinę pasaulio būseną.";
            String location=state.location;String questNote=state.objective;String event="action";
            int minutes=6,hp=0,mana=0,stamina=-1,aeonic=0;long crowns=0;
            boolean combat=state.combatActive;String enemy=combat?state.enemyName:"";String enemyStatus=combat?state.enemyStatus:"";String telegraph=combat?state.enemyTelegraph:"";String distance=combat?state.combatDistance:"mid";String hazard=combat?state.combatHazard:"";

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
            }else if(q.contains("puol")||q.contains("kov")||q.contains("smūg")||q.contains("ataka")||q.contains("pulti")){
                title="Kova prasideda";minutes=2;stamina=-8;hp=-2;event="combat";combat=true;
                if(enemy==null||enemy.isEmpty())enemy="Kelio plėšikas";enemyStatus="Budrus · spaudžiamas";telegraph="Žemas žingsnis į šoną ir pasiruošimas kontratakai";distance="close";hazard="Slidus akmuo ir siauras praėjimas";
                scene="Tu inicijuoji kontaktą. Priešininkas atsitraukia tik pusę žingsnio ir iškart persitvarko kontratakai; erdvė ankšta, todėl pozicija tampa svarbesnė už gryną jėgą.";
                c.put("Spausti ir neleisti atkurti distancijos").put("Išprovokuoti kontrataką ir bausti ją").put("Atsitraukti į saugesnę poziciją");
            }else if(q.contains("bėg")||q.contains("beg")||q.contains("trauktis")||q.contains("atsitrauk")){
                title="Atsitraukimas";minutes=4;stamina=-4;event="combat_end";combat=false;enemy="";enemyStatus="";telegraph="";distance="mid";hazard="";
                scene="Nutrauki kontaktą ir pasirenki erdvę, kurioje gali vėl vertinti situaciją. Priešininkas tavęs iškart nesiveja — kova baigiasi be aiškios pergalės, bet iniciatyva grįžta tau.";
                c.put("Stebėti, ar kas nors seka").put("Grįžti prie pagrindinės užduoties").put("Atsigauti prieš tęsiant kelią");
            }else if(q.contains("vykti")||q.contains("keliaut")||q.contains("keliauti")||q.contains("eiti į")||q.contains("važiuoti")){
                title="Kelionė";minutes=45;stamina=-5;event="travel";
                if(q.contains("veyrhold"))location="Veyrhold";else if(q.contains("stiklo")||q.contains("glasswood"))location="Stiklo Giria";else if(q.contains("kharad"))location="Kharad Vorn";else if(q.contains("safyro")||q.contains("sapphire"))location="Safyro Platybės";else if(q.contains("šventųjų")||q.contains("pelkyn"))location="Šventųjų Pelkynas";else if(q.contains("labirint"))location="Žaliasis Labirintas";else if(q.contains("asterio")||q.contains("crown of aster"))location="Asterio Karūna";
                scene="Palieki "+state.location+" ir judi pasirinkta kryptimi. Kelionė užima laiko ir ištvermės; pakeliui stebi kelią, žmonių judėjimą bei Meridiano infrastruktūros ženklus. Pasieki: "+location+".";
                c.put("Apsidairyti naujoje vietoje").put("Ieškoti vietinių kontaktų").put("Patikrinti, ar čia jaučiamas Meridiano poslinkis");
            }else if(q.contains("tirti")||q.contains("ištirt")||q.contains("apžiūr")||q.contains("iešk")||q.contains("sekti")){
                title="Tyrimas";minutes=14;stamina=-2;event="discovery";
                scene="Sistemingai tikrini aplinką ir atmeti pirmus akivaizdžius paaiškinimus. Randi kelias smulkias detales, kurios atskirai nieko neįrodo, bet kartu parodo kryptį, kurią verta tikrinti toliau.";
                c.put("Patikrinti stipriausią pėdsaką").put("Palyginti radinius su ankstesniais įrašais").put("Paklausti vietinio liudininko");
            }else{
                title="Veiksmas pasaulyje";minutes=7;event="action";
                scene="Atlieki: „"+a+"“. Veiksmas turi pasekmę: praeina laikas, keičiasi tavo pozicija situacijoje, o aplinka pateikia naują informaciją vietoje ankstesnio statiško ekrano.";
                c.put("Tęsti tą pačią kryptį").put("Apsidairyti ir įvertinti pasekmes").put("Grįžti prie Lūžusio Meridiano užduoties");
            }

            if(c.length()==0)c.put("Apsidairyti ir rinkti daugiau informacijos").put("Patikrinti įrangą ir užrašus").put("Tęsti pagrindinę užduotį");
            o.put("scene_title",title);o.put("scene",scene);o.put("choices",c);o.put("location",location);o.put("time_minutes",minutes);
            o.put("hp_delta",hp);o.put("mana_delta",mana);o.put("stamina_delta",stamina);o.put("aeonic_delta",aeonic);o.put("crowns_delta",crowns);o.put("quest_note",questNote);o.put("event_tag",event);
            o.put("combat_active",combat);o.put("enemy_name",combat?enemy:"");o.put("enemy_status",combat?enemyStatus:"");o.put("enemy_telegraph",combat?telegraph:"");o.put("combat_distance",combat?distance:"mid");o.put("combat_hazard",combat?hazard:"");o.put("loot",new JSONArray());return o;
        }catch(Exception e){return new JSONObject();}
    }
'''
    replacement = "\n" + local_method + "\n    void key(){"
    ns, count = re.subn(pattern, lambda m: replacement, s, flags=re.S)
    if count:
        core.write_text(ns,encoding="utf-8")
