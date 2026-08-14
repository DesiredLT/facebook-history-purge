from pathlib import Path

activity = Path("app/src/main/java/lt/vaeloria/ooc/VaeloriaActivity.java")
text = activity.read_text(encoding="utf-8")

# 1) Full 92-stat screen entry point.
if "PERŽIŪRĖTI VISAS 92 SAVYBES" not in text:
    needle = 'stats.addView(t("Toliau augama per meistriškumą, principus, technikas ir pasaulio pažinimą, o ne papildomus bazinių savybių taškus.",11,MUT,false));c.addView(stats,m(dp(9)));'
    replacement = 'stats.addView(t("Toliau augama per meistriškumą, principus, technikas ir pasaulio pažinimą, o ne papildomus bazinių savybių taškus.",11,MUT,false));Button allStats=outline("PERŽIŪRĖTI VISAS 92 SAVYBES");allStats.setOnClickListener(v->startActivity(new Intent(this,StatsActivity.class)));stats.addView(allStats,m(dp(8)));c.addView(stats,m(dp(9)));'
    if needle not in text:
        raise SystemExit("Nepavyko rasti PAŽANGOS bloko VaeloriaActivity.java")
    text = text.replace(needle, replacement, 1)

# 2) Keep the locally calculated check for visible feedback and history.
if "StatEngine.Check pendingCheck" not in text:
    needle = 'String screen="game",feedback=""; boolean busy=false; final ExecutorService pool=Executors.newSingleThreadExecutor();'
    replacement = 'String screen="game",feedback=""; boolean busy=false; StatEngine.Check pendingCheck; final ExecutorService pool=Executors.newSingleThreadExecutor();'
    if needle not in text:
        raise SystemExit("Nepavyko pridėti pendingCheck")
    text = text.replace(needle, replacement, 1)

# 3) Calculate the check on-device before the AI is called.
old_act = '''void act(String action){if(busy)return;db.checkpoint("prieš veiksmą",state);busy=true;feedback="⟳ Sprendžiama…";show("game");String key=SecureKeyStore.load(this);pool.execute(()->{JSONObject r;String warn=null;try{r=key.isEmpty()?local(action):GroqClient.resolveTurn(key,state,action,db.equippedSummary(),db.getAbilities());}catch(Exception e){r=local(action);warn="Groq nepasiekiamas · panaudotas vietinis sprendimas";}JSONObject rr=r;String ww=warn;runOnUiThread(()->finish(action,rr,ww));});}'''
new_act = '''void act(String action){if(busy)return;db.checkpoint("prieš veiksmą",state);String equipped=db.equippedSummary();List<String[]> abilities=db.getAbilities();pendingCheck=StatEngine.resolve(state,action,equipped,abilities);busy=true;feedback="⟳ Sprendžiama…\n"+pendingCheck.compact();show("game");String key=SecureKeyStore.load(this);StatEngine.Check check=pendingCheck;pool.execute(()->{JSONObject r;String warn=null;try{r=key.isEmpty()?local(action):GroqClient.resolveTurn(key,state,action,equipped,abilities,check);}catch(Exception e){r=local(action);warn="Groq nepasiekiamas · panaudotas vietinis sprendimas";}JSONObject rr=r;String ww=warn;runOnUiThread(()->finish(action,rr,ww));});}'''
if "GroqClient.resolveTurn(key,state,action,equipped,abilities,check)" not in text:
    if old_act not in text:
        raise SystemExit("Nepavyko rasti act() metodo")
    text = text.replace(old_act, new_act, 1)

# 4) Persist and display which stat actually resolved the action.
old_recent = 'state.recentTurns.add(action+" → "+state.sceneTitle);while(state.recentTurns.size()>12)state.recentTurns.remove(0);db.saveState(state);'
new_recent = 'state.recentTurns.add(action+" → "+state.sceneTitle+(pendingCheck==null?"":" · "+pendingCheck.primary+": "+pendingCheck.outcome));while(state.recentTurns.size()>12)state.recentTurns.remove(0);db.saveState(state);'
if new_recent not in text:
    if old_recent not in text:
        raise SystemExit("Nepavyko rasti ėjimų istorijos įrašo")
    text = text.replace(old_recent, new_recent, 1)

old_feedback = 'StringBuilder f=new StringBuilder();String ev=eventLabel(event);if(!ev.isEmpty())f.append("◆ ").append(ev).append("  ");'
new_feedback = 'StringBuilder f=new StringBuilder();if(pendingCheck!=null)f.append(pendingCheck.compact());String ev=eventLabel(event);if(!ev.isEmpty()){if(f.length()>0)f.append("  ");f.append("◆ ").append(ev);}'
if new_feedback not in text:
    if old_feedback not in text:
        raise SystemExit("Nepavyko rasti rezultato grįžtamojo ryšio")
    text = text.replace(old_feedback, new_feedback, 1)

old_end = 'feedback=f.length()==0?"✓ Ėjimas išspręstas":f.toString();busy=false;show("game");'
new_end = 'feedback=f.length()==0?"✓ Ėjimas išspręstas":f.toString();busy=false;pendingCheck=null;show("game");'
if new_end not in text:
    if old_end not in text:
        raise SystemExit("Nepavyko užbaigti patikros būsenos")
    text = text.replace(old_end, new_end, 1)

activity.write_text(text, encoding="utf-8")

# 5) Make the AI obey the already-resolved local check instead of inventing success/failure.
groq = Path("app/src/main/java/lt/vaeloria/ooc/GroqClient.java")
g = groq.read_text(encoding="utf-8")

g = g.replace(
    'public static JSONObject resolveTurn(String apiKey,GameState s,String action,String equipped,List<String[]> abilities)throws Exception{',
    'public static JSONObject resolveTurn(String apiKey,GameState s,String action,String equipped,List<String[]> abilities,StatEngine.Check check)throws Exception{'
)
g = g.replace(
    'req.put("messages",messages(s,action,equipped,abilities));',
    'req.put("messages",messages(s,action,equipped,abilities,check));'
)
g = g.replace(
    'private static JSONArray messages(GameState s,String action,String equipped,List<String[]> abilities)throws Exception{',
    'private static JSONArray messages(GameState s,String action,String equipped,List<String[]> abilities,StatEngine.Check check)throws Exception{'
)

old_rule = 'Nauja dėvima įranga gali būti šių kategorijų: weapon, offhand, head, chest, hands, legs, feet, belt, neck, ring, utility, relic. Artefaktams naudok artifact.'
new_rule = old_rule + ' Kai vartotojo žinutėje pateikta PRIVALOMA SAVYBĖS PATIKRA, jos skaitinį rezultatą laikyk nekintamu žaidimo variklio sprendimu. Negali nesėkmės paversti sėkme ar sėkmės nesėkme. Interpretacijos mastą, kainą ir pasaulio reakciją parink pagal nurodytą rezultatą.'
if new_rule not in g:
    if old_rule not in g:
        raise SystemExit("Nepavyko papildyti žaidimo meistro taisyklių")
    g = g.replace(old_rule, new_rule, 1)

old_user_end = '+"\\n\\nVEIKSMAS: "+action);'
new_user_end = '+"\\n\\nVEIKSMAS: "+action+"\\n\\n"+(check==null?"":check.prompt()));'
if new_user_end not in g:
    if old_user_end not in g:
        raise SystemExit("Nepavyko įdėti patikros į DI užklausą")
    g = g.replace(old_user_end, new_user_end, 1)

groq.write_text(g, encoding="utf-8")
