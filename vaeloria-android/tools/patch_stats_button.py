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

# 3) Calculate the check on-device before the AI is called and read actual saved stat values.
old_act = '''void act(String action){if(busy)return;db.checkpoint("prieš veiksmą",state);busy=true;feedback="⟳ Sprendžiama…";show("game");String key=SecureKeyStore.load(this);pool.execute(()->{JSONObject r;String warn=null;try{r=key.isEmpty()?local(action):GroqClient.resolveTurn(key,state,action,db.equippedSummary(),db.getAbilities());}catch(Exception e){r=local(action);warn="Groq nepasiekiamas · panaudotas vietinis sprendimas";}JSONObject rr=r;String ww=warn;runOnUiThread(()->finish(action,rr,ww));});}'''
new_act = '''void act(String action){if(busy)return;db.checkpoint("prieš veiksmą",state);String equipped=db.equippedSummary();List<String[]> abilities=db.getAbilities();Map<String,Integer> statValues=db.getStatValues();pendingCheck=StatEngine.resolve(state,action,equipped,abilities,statValues);busy=true;feedback="⟳ Sprendžiama…\n"+pendingCheck.compact();show("game");String key=SecureKeyStore.load(this);StatEngine.Check check=pendingCheck;pool.execute(()->{JSONObject r;String warn=null;try{r=key.isEmpty()?local(action):GroqClient.resolveTurn(key,state,action,equipped,abilities,check);}catch(Exception e){r=local(action);warn="Groq nepasiekiamas · panaudotas vietinis sprendimas";}JSONObject rr=r;String ww=warn;runOnUiThread(()->finish(action,rr,ww));});}'''
if "StatEngine.resolve(state,action,equipped,abilities,statValues)" not in text:
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
g = g.replace('req.put("messages",messages(s,action,equipped,abilities));','req.put("messages",messages(s,action,equipped,abilities,check));')
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

# 6) Persist every base stat separately in SQLite so future effects can change them independently.
db_path = Path("app/src/main/java/lt/vaeloria/ooc/VaeloriaDb.java")
d = db_path.read_text(encoding="utf-8")
d = d.replace('private static final int VERSION = 3;', 'private static final int VERSION = 4;')

if 'CREATE TABLE stats (' not in d:
    needle = 'db.execSQL("CREATE TABLE checkpoints (id INTEGER PRIMARY KEY AUTOINCREMENT, label TEXT, state_json TEXT NOT NULL, equipment_json TEXT NOT NULL, created_at INTEGER NOT NULL)");\n        seed(db);'
    replacement = 'db.execSQL("CREATE TABLE checkpoints (id INTEGER PRIMARY KEY AUTOINCREMENT, label TEXT, state_json TEXT NOT NULL, equipment_json TEXT NOT NULL, created_at INTEGER NOT NULL)");\n        db.execSQL("CREATE TABLE stats (name TEXT PRIMARY KEY, group_name TEXT NOT NULL, value INTEGER NOT NULL, cap INTEGER NOT NULL)");\n        seed(db);\n        seedStats(db);'
    if needle not in d:
        raise SystemExit("Nepavyko pridėti stats lentelės į onCreate")
    d = d.replace(needle, replacement, 1)

if 'migrateV3toV4' not in d:
    needle = 'if (oldVersion < 3) migrateV2toV3(db);'
    replacement = 'if (oldVersion < 3) { migrateV2toV3(db); oldVersion = 3; }\n        if (oldVersion < 4) migrateV3toV4(db);'
    if needle not in d:
        raise SystemExit("Nepavyko pridėti v4 migracijos")
    d = d.replace(needle, replacement, 1)

    marker = '    private static int parseInt(String s, int fallback)'
    methods = '''    private void migrateV3toV4(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS stats (name TEXT PRIMARY KEY, group_name TEXT NOT NULL, value INTEGER NOT NULL, cap INTEGER NOT NULL)");
        seedStats(db);
    }

    private void seedStats(SQLiteDatabase db) {
        for (BaseStatCatalog.Group group : BaseStatCatalog.GROUPS) {
            for (String stat : group.stats) {
                ContentValues v = new ContentValues();
                v.put("name", stat); v.put("group_name", group.name); v.put("value", 100); v.put("cap", 100);
                db.insertWithOnConflict("stats", null, v, SQLiteDatabase.CONFLICT_IGNORE);
            }
        }
    }

'''
    if marker not in d:
        raise SystemExit("Nepavyko įterpti stats migracijos metodų")
    d = d.replace(marker, methods + marker, 1)

if 'public java.util.Map<String,Integer> getStatValues()' not in d:
    marker = '    public List<Item> getItems() {'
    methods = '''    public java.util.Map<String,Integer> getStatValues() {
        java.util.LinkedHashMap<String,Integer> out = new java.util.LinkedHashMap<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT name,value FROM stats ORDER BY rowid", null)) {
            while (c.moveToNext()) out.put(c.getString(0), c.getInt(1));
        }
        if (out.isEmpty()) {
            seedStats(getWritableDatabase());
            try (Cursor c = getReadableDatabase().rawQuery("SELECT name,value FROM stats ORDER BY rowid", null)) {
                while (c.moveToNext()) out.put(c.getString(0), c.getInt(1));
            }
        }
        return out;
    }

    public int getStatValue(String name) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT value FROM stats WHERE name=?", new String[]{name})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 100;
    }

    public boolean setStatValue(String name, int value) {
        ContentValues v = new ContentValues(); v.put("value", Math.max(0, Math.min(100, value)));
        return getWritableDatabase().update("stats", v, "name=?", new String[]{name}) > 0;
    }

'''
    if marker not in d:
        raise SystemExit("Nepavyko įterpti stats API")
    d = d.replace(marker, methods + marker, 1)

# Save export/import also carries individual stat values.
d = d.replace('root.put("version",3);', 'root.put("version",4);')
old_export = 'root.put("items",items); return root.toString();'
new_export = 'root.put("items",items); JSONArray stats=new JSONArray();for(java.util.Map.Entry<String,Integer> e:getStatValues().entrySet()){JSONObject so=new JSONObject();so.put("name",e.getKey());so.put("value",e.getValue());stats.put(so);}root.put("stats",stats); return root.toString();'
if new_export not in d:
    if old_export not in d:
        raise SystemExit("Nepavyko papildyti stats eksporto")
    d = d.replace(old_export, new_export, 1)

old_import_head = 'JSONArray items=root.optJSONArray("items");'
new_import_head = 'JSONArray items=root.optJSONArray("items"); JSONArray stats=root.optJSONArray("stats");'
if new_import_head not in d:
    if old_import_head not in d:
        raise SystemExit("Nepavyko papildyti stats importo")
    d = d.replace(old_import_head, new_import_head, 1)

old_success = 'db.setTransactionSuccessful();return true;'
new_success = 'if(stats!=null)for(int si=0;si<stats.length();si++){JSONObject so=stats.optJSONObject(si);if(so==null)continue;ContentValues sv=new ContentValues();sv.put("value",Math.max(0,Math.min(100,so.optInt("value",100))));db.update("stats",sv,"name=?",new String[]{so.optString("name","")});}\n                db.setTransactionSuccessful();return true;'
# Only patch the importSave transaction occurrence by searching after its method declaration.
import_pos = d.find('public boolean importSave(String raw)')
if import_pos >= 0 and 'if(stats!=null)for(int si=0;' not in d[import_pos:]:
    pos = d.find(old_success, import_pos)
    if pos < 0:
        raise SystemExit("Nepavyko papildyti stats importo transakcijos")
    d = d[:pos] + new_success + d[pos+len(old_success):]

old_reset = 'db.execSQL("DROP TABLE IF EXISTS checkpoints");db.execSQL("DROP TABLE IF EXISTS turns")'
new_reset = 'db.execSQL("DROP TABLE IF EXISTS checkpoints");db.execSQL("DROP TABLE IF EXISTS stats");db.execSQL("DROP TABLE IF EXISTS turns")'
if new_reset not in d:
    if old_reset not in d:
        raise SystemExit("Nepavyko papildyti reset stats lentele")
    d = d.replace(old_reset, new_reset, 1)

db_path.write_text(d, encoding="utf-8")
