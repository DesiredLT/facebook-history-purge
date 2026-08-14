from pathlib import Path
import re

# This patch runs AFTER the v0.3.2 stat integration scripts and upgrades the generated sources to v0.4.

activity=Path('app/src/main/java/lt/vaeloria/ooc/VaeloriaActivity.java')
s=activity.read_text(encoding='utf-8')

# Fields for visible mastery feedback.
s=s.replace(
    'String screen="game",feedback=""; boolean busy=false; StatEngine.Check pendingCheck; final ExecutorService pool=Executors.newSingleThreadExecutor();',
    'String screen="game",feedback=""; boolean busy=false; StatEngine.Check pendingCheck; int pendingMasteryBonus=0,pendingPrimaryXp=0,pendingSecondaryXp=0; final ExecutorService pool=Executors.newSingleThreadExecutor();'
)

# Remove the last remaining UI anglicism and make the resource name explicit.
s=s.replace('chip("VIETINIS RPG · "+BuildConfig.VERSION_NAME)','chip("VIETINIS · "+BuildConfig.VERSION_NAME)')
s=s.replace('res("EONAS",state.aeonic,state.aeonicMax,Color.rgb(154,115,212))','res("EONINĖ",state.aeonic,state.aeonicMax,Color.rgb(154,115,212))')

# Stronger scene visual: location + scene + event + world time.
s=s.replace(
    'SceneBannerView art=new SceneBannerView(this);art.setLocation(state.location);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(165));p.setMargins(0,0,0,dp(10));c.addView(art,p);',
    'SceneBannerView art=new SceneBannerView(this);art.setScene(state.location,state.sceneTitle,"",state.worldMinute);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(210));p.setMargins(0,0,0,dp(10));c.addView(art,p);'
)

# Better choice spacing and numbering.
s=s.replace(
    'c.addView(label("KĄ DARAI?"));for(int i=0;i<Math.min(3,state.choices.size());i++){String a=state.choices.get(i);Button b=choice((i+1)+"  "+a);b.setEnabled(!busy);b.setOnClickListener(v->act(a));c.addView(b,m(dp(7)));}',
    'c.addView(label("KĄ DARAI?"));for(int i=0;i<Math.min(3,state.choices.size());i++){String a=state.choices.get(i);String nr=i==0?"①":i==1?"②":"③";Button b=choice(nr+"   "+a);b.setEnabled(!busy);b.setOnClickListener(v->act(a));c.addView(b,m(dp(7)));}'
)
s=s.replace(
    'Button choice(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(12);b.setTextColor(TEXT);b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);b.setBackground(round(SUR2,15,Color.rgb(47,70,81)));return b;}',
    'Button choice(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(12);b.setTextColor(TEXT);b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);b.setPadding(dp(18),dp(8),dp(14),dp(8));b.setMinHeight(dp(64));b.setBackground(round(SUR2,15,Color.rgb(47,70,81)));return b;}'
)
s=s.replace('DI žaidimo meistras išjungtas · veikia vietinis režimas','VIETINIS REŽIMAS · DI žaidimo meistras išjungtas')

# Replace hero screen with identity + mastery summary + visual loadout + mechanical ability cards.
start=s.index('    View hero(){')
end=s.index('    View equipmentPanel(){',start)
hero='''    View hero(){
        ScrollView sv=new ScrollView(this);LinearLayout c=col();c.setPadding(dp(14),dp(6),dp(14),dp(22));sv.addView(c);
        LinearLayout top=strong();top.addView(label("VEIKĖJAS"));top.addView(t("EINORAS",26,TEXT,true));top.addView(t("201 m. chronologinis · 20 m. biologinis · biologinis amžius nekinta",11,MUT,false));LinearLayout tags=row();tags.addView(chip("LEGENDINĖ BAZĖ"));Space gap=new Space(this);tags.addView(gap,new LinearLayout.LayoutParams(dp(7),1));tags.addView(chip("92 / 92 · 100/100"));top.addView(tags,m(dp(4)));c.addView(top,m(dp(10)));

        Map<String,Integer> mastery=db.getMasteryLevels();int sum=0;for(String stat:mastery.keySet())sum+=mastery.get(stat);int avg=mastery.isEmpty()?70:Math.round(sum/(float)mastery.size());
        LinearLayout stats=card();stats.addView(label("PAŽANGA"));stats.addView(t("Bazinės savybės užbaigtos · post-cap meistriškumas "+avg+"/100",15,TEXT,true));stats.addView(bar("VIDUTINIS MEISTRIŠKUMAS",avg,100,GOLD));stats.addView(t("Kiekviena naudojama savybė gauna atskirą patirtį. Meistriškumas realiai keičia patikros rezultatą, bet nekelia bazinės savybės virš 100/100.",11,MUT,false));Button allStats=outline("PERŽIŪRĖTI VISAS 92 SAVYBES IR XP");allStats.setOnClickListener(v->startActivity(new Intent(this,StatsActivity.class)));stats.addView(allStats,m(dp(7)));c.addView(stats,m(dp(9)));

        c.addView(equipmentPanel(),m(dp(9)));

        LinearLayout loose=card();loose.addView(label("NEUŽDĖTA ĮRANGA"));int looseCount=0;for(VaeloriaDb.Item i:db.getItems())if(i.slot!=null&&!i.equipped){loose.addView(inventoryRow(i));looseCount++;}if(looseCount==0)loose.addView(t("Visi šiuo metu turimi dėvimi daiktai yra užsidėti. Nauja įranga gali atsirasti kaip pagrįstas radinys, atlygis, pirkinys ar grobis.",11,MUT,false));c.addView(loose,m(dp(9)));

        LinearLayout inv=card();inv.addView(label("ARTEFAKTAI IR ĮGALIOJIMAI"));for(VaeloriaDb.Item i:db.getItems())if(i.slot==null){inv.addView(t(i.name+" · "+rarityLabel(i.rarity).toUpperCase(Locale.ROOT),13,rarity(i.rarity),true));inv.addView(t(i.description,10,MUT,false));}c.addView(inv,m(dp(9)));

        LinearLayout ab=col();ab.addView(label("GEBĖJIMAI VIRŠ BAZINĖS RIBOS"));for(String[] a:db.getAbilities())ab.addView(abilityCard(a),m(dp(7)));c.addView(ab,m(dp(8)));return sv;
    }

    View abilityCard(String[] a){LinearLayout x=card();LinearLayout h=row();TextView tag=t(AbilityMechanics.tag(a[1]),9,TEAL,true);tag.setPadding(dp(8),dp(3),dp(8),dp(3));tag.setBackground(round(Color.argb(45,82,177,167),10,Color.argb(100,82,177,167)));h.addView(tag);x.addView(h,m(dp(5)));x.addView(t(AbilityMechanics.displayName(a[0]),14,TEXT,true));x.addView(t(a[2],10,MUT,false));TextView mech=t("MECHANIKA · "+AbilityMechanics.mechanic(a[0]),10,GOLD,true);mech.setPadding(0,dp(7),0,0);x.addView(mech);return x;}

'''
s=s[:start]+hero+s[end:]

# Replace the equipment grid with a tappable visual silhouette.
start=s.index('    View equipmentPanel(){')
end=s.index('    View slotRow(',start)
equip='''    View equipmentPanel(){
        LinearLayout gear=strong();gear.addView(label("DĖVIMA ĮRANGA"));gear.addView(t("Paliesk įrangos vietą aplink siluetą. Spalvotas rėmelis reiškia uždėtą daiktą; tuščia vieta gali būti užpildyta žaidimo metu.",11,MUT,false));CharacterLoadoutView v=new CharacterLoadoutView(this);for(String slot:VaeloriaDb.EQUIPMENT_SLOTS){VaeloriaDb.Item i=db.getEquippedAt(slot);v.put(slot,VaeloriaDb.slotLabel(slot),i==null?"":i.name,i==null?MUT:rarity(i.rarity));}v.setListener(this::openSlot);gear.addView(v,new LinearLayout.LayoutParams(-1,dp(510)));return gear;
    }

'''
s=s[:start]+equip+s[end:]

# Full-height map: top information, map fills remaining screen, compact legend.
start=s.index('    View map(){')
end=s.index('    void locationDialog(',start)
map_method='''    View map(){
        LinearLayout r=col();r.setPadding(dp(10),dp(4),dp(10),dp(8));
        LinearLayout h=card();h.addView(label("PASAULIO ŽEMĖLAPIS"));h.addView(t("◆ Dabartinė vieta: "+state.location,13,TEXT,true));h.addView(t("Priartink dviem pirštais, tempk ir paliesk vietą. Pasirinkus vietą matysi grėsmę ir galėsi pradėti kelionę.",10,MUT,false));r.addView(h,m(dp(7)));
        WorldMapView map=new WorldMapView(this);map.setCurrentLocation(state.location);map.setListener((name,danger)->locationDialog(name,danger));r.addView(map,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout legend=card();legend.addView(t("GRĖSMĖ  ● 1–3 žema   ● 4–5 vidutinė   ● 6–7 aukšta   ● 8–10 labai aukšta",9,MUT,true));r.addView(legend,m(dp(2)));return r;
    }

'''
s=s[:start]+map_method+s[end:]

# Richer journal, less administrative wording.
start=s.index('    View journal(){')
end=s.index('    View settings(){',start)
journal='''    View journal(){
        ScrollView sv=new ScrollView(this);LinearLayout c=col();c.setPadding(dp(14),dp(6),dp(14),dp(22));sv.addView(c);
        LinearLayout q=strong();q.addView(label("PAGRINDINĖ SIUŽETO LINIJA · I DALIS"));q.addView(t("LŪŽĘS MERIDIANAS",20,GOLD,true));q.addView(t("PROGRESAS · 1 / 4",10,TEAL,true));q.addView(obj("◆",state.objective,true));q.addView(obj("○","Užsitikrinti tris nepriklausomai prižiūrimus kelio atramos taškus",false));q.addView(obj("○","Pereiti Meridianą ir grįžti su patikrinamais Orisono kontakto duomenimis",false));q.addView(obj("○","Derėtis dėl Pirmosios Meridiano chartijos arba ją sąmoningai atmesti",false));c.addView(q,m(dp(9)));
        LinearLayout ev=card();ev.addView(label("SURINKTI ĮRODYMAI"));ev.addView(t("• Karavano manifestas Luminara pasiekė anksčiau už patį karavaną.",11,TEXT,false));ev.addView(t("• Laiko neatitikimas sutampa su nauju kelionės vartų poslinkiu.",11,TEXT,false));ev.addView(t("• Vakarinio kelionės vartų tranzito žyma yra pirmas tikrinamas fizinis pėdsakas.",11,TEXT,false));c.addView(ev,m(dp(9)));
        LinearLayout th=card();th.addView(label("AKTYVIOS SIUŽETO GIJOS"));for(String x:new String[]{"Atviri horizontai · Lūžęs Meridianas","Orisono ryšio protokolas","Kelionės vartų poslinkis","Santarvės priežiūros valdymas","Drakoniškoji įpėdinystė"}){TextView row=t("• "+x,11,TEXT,false);row.setPadding(0,dp(4),0,dp(4));th.addView(row);}c.addView(th,m(dp(9)));
        LinearLayout log=card();log.addView(label("PASKUTINIAI ĖJIMAI"));if(state.recentTurns.isEmpty())log.addView(t("Šiame įrenginyje ėjimų istorijos dar nėra.",11,MUT,false));else for(int i=state.recentTurns.size()-1;i>=0;i--)log.addView(t("• "+state.recentTurns.get(i),11,TEXT,false));c.addView(log,m(dp(8)));return sv;
    }

'''
s=s[:start]+journal+s[end:]

# Mastery-aware action resolution. The local engine and AI receive the SAME already-fixed check result.
old='''void act(String action){if(busy)return;db.checkpoint("prieš veiksmą",state);String equipped=db.equippedSummary();List<String[]> abilities=db.getAbilities();Map<String,Integer> statValues=db.getStatValues();pendingCheck=StatEngine.resolve(state,action,equipped,abilities,statValues);busy=true;feedback="⟳ Sprendžiama…\\n"+pendingCheck.compact();show("game");String key=SecureKeyStore.load(this);StatEngine.Check check=pendingCheck;pool.execute(()->{JSONObject r;String warn=null;try{r=key.isEmpty()?local(action):GroqClient.resolveTurn(key,state,action,equipped,abilities,check);}catch(Exception e){r=local(action);warn="Groq nepasiekiamas · panaudotas vietinis sprendimas";}JSONObject rr=r;String ww=warn;runOnUiThread(()->finish(action,rr,ww));});}'''
new='''void act(String action){if(busy)return;db.checkpoint("prieš veiksmą",state);String equipped=db.equippedSummary();List<String[]> abilities=db.getAbilities();Map<String,Integer> statValues=db.getStatValues();pendingCheck=StatEngine.resolve(state,action,equipped,abilities,statValues);Map<String,Integer> mastery=db.getMasteryLevels();pendingMasteryBonus=MasteryEngine.checkBonus(pendingCheck.primary,pendingCheck.secondary,mastery);MasteryEngine.apply(pendingCheck,pendingMasteryBonus);pendingPrimaryXp=MasteryEngine.primaryXp(pendingCheck);pendingSecondaryXp=MasteryEngine.secondaryXp(pendingCheck);busy=true;feedback="⟳ Sprendžiama…\\n"+pendingCheck.compact()+" · meistriškumas +"+pendingMasteryBonus;show("game");String key=SecureKeyStore.load(this);StatEngine.Check check=pendingCheck;pool.execute(()->{JSONObject r;String warn=null;try{r=key.isEmpty()?local(action,check):GroqClient.resolveTurn(key,state,action,equipped,abilities,check);}catch(Exception e){r=local(action,check);warn="Groq nepasiekiamas · panaudotas vietinis sprendimas";}JSONObject rr=r;String ww=warn;runOnUiThread(()->finish(action,rr,ww));});}'''
if old not in s: raise SystemExit('v0.4 act() integration point not found')
s=s.replace(old,new,1)

# Award XP only when the turn is actually resolved.
needle='state.applyTurn(r);ArrayList<String> gained=new ArrayList<>();'
repl='state.applyTurn(r);if(pendingCheck!=null){db.awardMastery(pendingCheck.primary,pendingPrimaryXp);db.awardMastery(pendingCheck.secondary,pendingSecondaryXp);}ArrayList<String> gained=new ArrayList<>();'
if needle not in s: raise SystemExit('finish mastery insertion point not found')
s=s.replace(needle,repl,1)

# Append mastery information to visible turn feedback.
needle='StringBuilder f=new StringBuilder();if(pendingCheck!=null)f.append(pendingCheck.compact());String ev=eventLabel(event);'
repl='StringBuilder f=new StringBuilder();if(pendingCheck!=null)f.append(pendingCheck.compact()).append(" · ").append(MasteryEngine.effectLine(pendingMasteryBonus,pendingPrimaryXp,pendingSecondaryXp));String ev=eventLabel(event);'
if needle not in s: raise SystemExit('feedback mastery insertion point not found')
s=s.replace(needle,repl,1)
s=s.replace('busy=false;pendingCheck=null;show("game");','busy=false;pendingCheck=null;pendingMasteryBonus=0;pendingPrimaryXp=0;pendingSecondaryXp=0;show("game");')

# A useful offline resolver: the stat check matters even with AI disabled.
start=s.index('    JSONObject local(String action){')
end=s.index('    void key(){',start)
local='''    JSONObject local(String action){return local(action,pendingCheck);}
    JSONObject local(String action,StatEngine.Check check){
        try{String a=action==null?"":action.toLowerCase(Locale.forLanguageTag("lt-LT"));String outcome=check==null?"sėkmė":check.outcome;boolean good=outcome.contains("sėkmė")&&!outcome.startsWith("ne")&&!outcome.startsWith("rimta");boolean partial=outcome.startsWith("dalinė");JSONObject o=new JSONObject();String title="Veiksmo pasekmė",scene="Veiksmas išspręstas pagal vietinę savybių patikrą: "+outcome+".",event="none",objective=state.objective,location=state.location;int minutes=4,hp=0,mana=0,stam=0,aeon=0;long crowns=0;JSONArray choices=new JSONArray();
            if(a.contains("manifest")){title="Manifesto pėdsakas";event="discovery";minutes=8;if(good||partial){scene="Manifesto datos, antspaudai ir perdavimo seka patvirtina, kad neatitikimas nėra paprasta raštinės klaida. Ryškiausias tikrinamas pėdsakas lieka vakarinio Luminara kelionės vartų tranzito žyma.";objective="Patikrinti vakarinio Luminara kelionės vartų tranzito žymą ir palyginti ją su fizine karavano būsena.";}else scene="Bandymas išskaidyti manifesto neatitikimą kol kas neatskiria tikro laiko poslinkio nuo galimos dokumentų klaidos. Reikia naujo nepriklausomo įrodymo.";choices.put("Vykti prie vakarinių kelionės vartų").put("Surasti manifestą išdavusį raštininką").put("Palyginti antspaudus su Meridiano Rakto rezonansu");}
            else if(a.contains("kelionės vart")||a.contains("vartų")||a.contains("meridian")){title="Vakariniai kelionės vartai";event="discovery";minutes=14;if(good||partial){scene="Tranzito žymoje aptinki poslinkį, kuris fiziškai nesutampa su įprasta vartų apskaita. Tai jau antras nepriklausomas požymis, kad problema yra pačiame kelių tinkle.";objective="Palyginti tranzito žymą su karavano fizine būsena ir nustatyti poslinkio kryptį.";}else scene="Kelionės vartų laukas išlieka per daug nestabilus patikimam matavimui. Bandymas nepatvirtina hipotezės ir palieka naują matavimo riziką.";choices.put("Pakartoti matavimą kitu kampu").put("Naudoti Nulinio Stiklo Prizmę").put("Stebėti kitą tranzitą ir palyginti žymas");}
            else if(a.contains("mira")||a.contains("kaeli")){title="Miros ir Kaelio stebėjimai";event="social";minutes=10;scene=(good||partial)?"Mira ir Kaelis pateikia nepriklausomus laiko stebėjimus. Jie nevisiškai sutampa tarpusavyje, tačiau abu nukrypsta ta pačia kryptimi nuo oficialios vartų apskaitos.":"Pokalbis neleidžia patikimai sujungti jų stebėjimų: trūksta bendro atskaitos taško.";choices.put("Sulyginti jų laikrodžių atskaitos taškus").put("Paprašyti atskirų užrašų").put("Grįžti prie fizinės vartų žymos");}
            else if(a.startsWith("keliauti")||a.contains("vykti į")){title="Kelionė";event="travel";minutes=75;stam=-5;String[] places={"Aureliono Pakraštys","Asterio Karūna","Žvaigždėkritos Skliautas","Stiklo Giria","Tuščiavidurė Smailė","Kharad Vorn","Drakono Pabudimo Viršūnės","Pelenų Karūnos Citadelė","Safyro Platybės","Amžinojo Šaltinio Slėnis","Žaliasis Labirintas","Šventųjų Pelkynas","Veyrhold","Luminara"};for(String p:places)if(action.contains(p)){if(good||partial)location=p;break;}scene=(good||partial)?"Kelionė išsprendžiama pagal žinomą maršrutą. Pasieki "+location+", tačiau vietinis režimas generuoja tik ribotą pasaulio reakciją.":"Maršruto bandymas nutrūksta dar nepasiekus tikslo; lieki "+state.location+" ir prarandi dalį laiko bei ištvermės.";choices.put("Apsidairyti naujoje vietoje").put("Patikrinti kelių tinklo žymas").put("Peržiūrėti žurnalą");}
            else {if(a.contains("bėg")||a.contains("smūg")||a.contains("kov")||a.contains("lip")||a.contains("plauk"))stam=-3;if(a.contains("bur")||a.contains("magij"))mana=-2;scene="Bandymas „"+action+"“ gauna rezultatą: "+outcome+". Vietinis variklis pritaikė savybes, meistriškumą, būseną, įrangą ir gebėjimus; platesnė pasaulio reakcija be DI lieka ribota.";choices.put("Tęsti pagal gautą rezultatą").put("Pakeisti metodą").put("Apsidairyti ir surinkti daugiau informacijos");}
            while(choices.length()<3)choices.put("Stebėti situaciją ir rinkti daugiau informacijos");o.put("scene_title",title);o.put("scene",scene);o.put("choices",choices);o.put("location",location);o.put("time_minutes",minutes);o.put("hp_delta",hp);o.put("mana_delta",mana);o.put("stamina_delta",stam);o.put("aeonic_delta",aeon);o.put("crowns_delta",crowns);o.put("quest_note",objective);o.put("event_tag",event);o.put("combat_active",state.combatActive);o.put("enemy_name",state.combatActive?state.enemyName:"");o.put("enemy_status",state.combatActive?state.enemyStatus:"");o.put("enemy_telegraph",state.combatActive?state.enemyTelegraph:"");o.put("combat_distance",state.combatActive?state.combatDistance:"mid");o.put("combat_hazard",state.combatActive?state.combatHazard:"");o.put("loot",new JSONArray());return o;}catch(Exception e){return new JSONObject();}
    }

'''
s=s[:start]+local+s[end:]

activity.write_text(s,encoding='utf-8')

# ----- SQLite mastery persistence -----
dbp=Path('app/src/main/java/lt/vaeloria/ooc/VaeloriaDb.java')
d=dbp.read_text(encoding='utf-8')
d=d.replace('private static final int VERSION = 4;','private static final int VERSION = 5;')

# Mastery value object near Item.
item_marker='''    public static class Item {
        public String id, name, type, rarity, description, slot, equippedSlot;
        public boolean equipped, synced;
    }
'''
if 'public static class Mastery' not in d:
    mastery_class=item_marker+'''\n    public static class Mastery {\n        public final int level,xp,nextXp;\n        public Mastery(int level,int xp,int nextXp){this.level=level;this.xp=xp;this.nextXp=nextXp;}\n    }\n'''
    if item_marker not in d: raise SystemExit('DB Item marker not found')
    d=d.replace(item_marker,mastery_class,1)

# Create table on fresh install.
needle='db.execSQL("CREATE TABLE stats (name TEXT PRIMARY KEY, group_name TEXT NOT NULL, value INTEGER NOT NULL, cap INTEGER NOT NULL)");\n        seed(db);\n        seedStats(db);'
repl='db.execSQL("CREATE TABLE stats (name TEXT PRIMARY KEY, group_name TEXT NOT NULL, value INTEGER NOT NULL, cap INTEGER NOT NULL)");\n        db.execSQL("CREATE TABLE mastery (name TEXT PRIMARY KEY, level INTEGER NOT NULL, xp INTEGER NOT NULL, next_xp INTEGER NOT NULL)");\n        seed(db);\n        seedStats(db);\n        seedMastery(db);'
if needle not in d: raise SystemExit('fresh mastery table insertion point not found')
d=d.replace(needle,repl,1)

# Upgrade v4 -> v5.
needle='if (oldVersion < 4) migrateV3toV4(db);'
repl='if (oldVersion < 4) { migrateV3toV4(db); oldVersion = 4; }\n        if (oldVersion < 5) migrateV4toV5(db);'
if needle not in d: raise SystemExit('v5 migration insertion point not found')
d=d.replace(needle,repl,1)

marker='    private static int parseInt(String s, int fallback)'
methods='''    private void migrateV4toV5(SQLiteDatabase db) {\n        db.execSQL("CREATE TABLE IF NOT EXISTS mastery (name TEXT PRIMARY KEY, level INTEGER NOT NULL, xp INTEGER NOT NULL, next_xp INTEGER NOT NULL)");\n        seedMastery(db);\n    }\n\n    private int masteryNext(int level){ return 300 + Math.max(0,Math.min(99,level))*10; }\n\n    private void seedMastery(SQLiteDatabase db) {\n        for (BaseStatCatalog.Group group : BaseStatCatalog.GROUPS) for (String stat : group.stats) {\n            ContentValues v=new ContentValues();v.put("name",stat);v.put("level",70);v.put("xp",0);v.put("next_xp",masteryNext(70));\n            db.insertWithOnConflict("mastery",null,v,SQLiteDatabase.CONFLICT_IGNORE);\n        }\n    }\n\n'''
if 'migrateV4toV5' not in d:
    if marker not in d: raise SystemExit('v5 methods marker not found')
    d=d.replace(marker,methods+marker,1)

# Public mastery API before items API.
marker='    public List<Item> getItems() {'
api='''    public java.util.Map<String,Mastery> getMasteries() {\n        java.util.LinkedHashMap<String,Mastery> out=new java.util.LinkedHashMap<>();\n        try(Cursor c=getReadableDatabase().rawQuery("SELECT name,level,xp,next_xp FROM mastery ORDER BY rowid",null)){while(c.moveToNext())out.put(c.getString(0),new Mastery(c.getInt(1),c.getInt(2),c.getInt(3)));}\n        if(out.isEmpty()){seedMastery(getWritableDatabase());try(Cursor c=getReadableDatabase().rawQuery("SELECT name,level,xp,next_xp FROM mastery ORDER BY rowid",null)){while(c.moveToNext())out.put(c.getString(0),new Mastery(c.getInt(1),c.getInt(2),c.getInt(3)));}}\n        return out;\n    }\n\n    public java.util.Map<String,Integer> getMasteryLevels(){java.util.LinkedHashMap<String,Integer> out=new java.util.LinkedHashMap<>();for(java.util.Map.Entry<String,Mastery> e:getMasteries().entrySet())out.put(e.getKey(),e.getValue().level);return out;}\n\n    public Mastery awardMastery(String name,int gain){\n        Mastery cur=getMasteries().get(name);if(cur==null)cur=new Mastery(70,0,masteryNext(70));int level=cur.level,xp=cur.xp+Math.max(0,gain),next=cur.nextXp;while(level<100&&xp>=next){xp-=next;level++;next=masteryNext(level);}if(level>=100){level=100;xp=0;next=1;}ContentValues v=new ContentValues();v.put("level",level);v.put("xp",xp);v.put("next_xp",next);getWritableDatabase().update("mastery",v,"name=?",new String[]{name});return new Mastery(level,xp,next);\n    }\n\n'''
if 'getMasteries()' not in d:
    if marker not in d: raise SystemExit('mastery API marker not found')
    d=d.replace(marker,api+marker,1)

# Export/import mastery.
d=d.replace('root.put("version",4);','root.put("version",5);')
needle='root.put("stats",stats); return root.toString();'
repl='root.put("stats",stats); JSONArray mastery=new JSONArray();for(java.util.Map.Entry<String,Mastery> e:getMasteries().entrySet()){JSONObject mo=new JSONObject();mo.put("name",e.getKey());mo.put("level",e.getValue().level);mo.put("xp",e.getValue().xp);mo.put("next_xp",e.getValue().nextXp);mastery.put(mo);}root.put("mastery",mastery); return root.toString();'
if needle not in d: raise SystemExit('mastery export insertion point not found')
d=d.replace(needle,repl,1)
needle='JSONArray items=root.optJSONArray("items"); JSONArray stats=root.optJSONArray("stats");'
repl='JSONArray items=root.optJSONArray("items"); JSONArray stats=root.optJSONArray("stats"); JSONArray mastery=root.optJSONArray("mastery");'
if needle not in d: raise SystemExit('mastery import head not found')
d=d.replace(needle,repl,1)
needle='db.setTransactionSuccessful();return true;'
repl='if(mastery!=null)for(int mi=0;mi<mastery.length();mi++){JSONObject mo=mastery.optJSONObject(mi);if(mo==null)continue;ContentValues mv=new ContentValues();int lv=Math.max(0,Math.min(100,mo.optInt("level",70)));mv.put("level",lv);mv.put("xp",Math.max(0,mo.optInt("xp",0)));mv.put("next_xp",Math.max(1,mo.optInt("next_xp",masteryNext(lv))));db.update("mastery",mv,"name=?",new String[]{mo.optString("name","")});}\n                db.setTransactionSuccessful();return true;'
import_pos=d.find('public boolean importSave(String raw)')
pos=d.find(needle,import_pos)
if pos<0: raise SystemExit('mastery import transaction not found')
d=d[:pos]+repl+d[pos+len(needle):]

d=d.replace('db.execSQL("DROP TABLE IF EXISTS stats");db.execSQL("DROP TABLE IF EXISTS turns")','db.execSQL("DROP TABLE IF EXISTS stats");db.execSQL("DROP TABLE IF EXISTS mastery");db.execSQL("DROP TABLE IF EXISTS turns")')
dbp.write_text(d,encoding='utf-8')

# ----- Small v0.4 system prompt reinforcement -----
gp=Path('app/src/main/java/lt/vaeloria/ooc/GroqClient.java')
g=gp.read_text(encoding='utf-8')
old='Kai vartotojo žinutėje pateikta PRIVALOMA SAVYBĖS PATIKRA, jos skaitinį rezultatą laikyk nekintamu žaidimo variklio sprendimu.'
new='Kai vartotojo žinutėje pateikta PRIVALOMA SAVYBĖS PATIKRA, jos skaitinį rezultatą, įskaitant post-cap meistriškumo poveikį, laikyk nekintamu žaidimo variklio sprendimu.'
if old in g:g=g.replace(old,new,1)
gp.write_text(g,encoding='utf-8')
