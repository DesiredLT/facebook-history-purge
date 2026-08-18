package lt.vaeloria.ooc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class VaeloriaDb extends SQLiteOpenHelper {
    private static final String DB = "vaeloria.db";
    private static final int VERSION = 5;

    public static final String[] EQUIPMENT_SLOTS = new String[]{
            "weapon","offhand","head","chest","hands","legs","feet","belt","neck",
            "ring_left","ring_right","utility","relic_1","relic_2","relic_3","relic_4"
    };

    public static class Item {
        public String id, name, type, rarity, description, slot, equippedSlot;
        public boolean equipped, synced;
    }

    public static class Mastery {
        public final int level,xp,nextXp;
        public Mastery(int level,int xp,int nextXp){this.level=level;this.xp=xp;this.nextXp=nextXp;}
    }

    public VaeloriaDb(Context c) { super(c, DB, null, VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE state (id INTEGER PRIMARY KEY CHECK(id=1), json TEXT NOT NULL)");
        db.execSQL("CREATE TABLE items (id TEXT PRIMARY KEY, name TEXT NOT NULL, type TEXT NOT NULL, rarity TEXT NOT NULL, description TEXT NOT NULL, slot TEXT, equipped INTEGER NOT NULL DEFAULT 0, synced INTEGER NOT NULL DEFAULT 0, equipped_slot TEXT)");
        db.execSQL("CREATE TABLE abilities (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL, type TEXT NOT NULL, description TEXT NOT NULL)");
        db.execSQL("CREATE TABLE checkpoints (id INTEGER PRIMARY KEY AUTOINCREMENT, label TEXT, state_json TEXT NOT NULL, equipment_json TEXT NOT NULL, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE stats (name TEXT PRIMARY KEY, group_name TEXT NOT NULL, value INTEGER NOT NULL, cap INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE mastery (name TEXT PRIMARY KEY, level INTEGER NOT NULL, xp INTEGER NOT NULL, next_xp INTEGER NOT NULL)");
        seed(db);
        seedStats(db);
        seedMastery(db);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            migrateV1(db);
            oldVersion = 2;
        }
        if (oldVersion < 3) { migrateV2toV3(db); oldVersion = 3; }
        if (oldVersion < 4) { migrateV3toV4(db); oldVersion = 4; }
        if (oldVersion < 5) migrateV4toV5(db);
    }

    private void migrateV1(SQLiteDatabase db) {
        java.util.HashMap<String,String> old = new java.util.HashMap<>();
        java.util.ArrayList<String> legacyTurns = new java.util.ArrayList<>();
        try (Cursor c = db.rawQuery("SELECT k,v FROM state", null)) {
            while (c.moveToNext()) old.put(c.getString(0), c.getString(1));
        } catch (Exception ignored) {}
        try (Cursor c = db.rawQuery("SELECT action,result FROM turns ORDER BY id ASC", null)) {
            while (c.moveToNext()) legacyTurns.add(c.getString(0) + " → " + compactLegacy(c.getString(1)));
        } catch (Exception ignored) {}
        db.execSQL("DROP TABLE IF EXISTS state");
        db.execSQL("DROP TABLE IF EXISTS turns");
        db.execSQL("DROP TABLE IF EXISTS undo");
        db.execSQL("DROP TABLE IF EXISTS items");
        db.execSQL("DROP TABLE IF EXISTS abilities");
        db.execSQL("DROP TABLE IF EXISTS checkpoints");
        onCreate(db);
        try {
            GameState s = new GameState();
            s.location = old.containsKey("location") ? old.get("location") : s.location;
            s.worldMinute = parseLong(old.get("world_minute"), s.worldMinute);
            s.hp = parseInt(old.get("hp"), s.hp);
            s.mana = parseInt(old.get("mana"), s.mana);
            s.stamina = parseInt(old.get("stamina"), s.stamina);
            s.aeonic = parseInt(old.get("aeonic"), s.aeonic);
            s.crowns = parseLong(old.get("crowns"), s.crowns);
            if (old.get("scene") != null && !old.get("scene").isEmpty()) s.scene = old.get("scene");
            s.choices.clear();
            for (int i=1;i<=3;i++) {
                String choice = old.get("choice"+i);
                if (choice != null && !choice.isEmpty()) s.choices.add(choice);
            }
            while (s.choices.size() < 3) s.choices.add("Stebėti situaciją ir rinkti įrodymus");
            s.recentTurns.addAll(legacyTurns.subList(Math.max(0, legacyTurns.size()-12), legacyTurns.size()));
            ContentValues st = new ContentValues(); st.put("json", s.toJson().toString());
            db.update("state", st, "id=1", null);
        } catch (Exception ignored) {}
    }

    private void migrateV2toV3(SQLiteDatabase db) {
        try { db.execSQL("ALTER TABLE items ADD COLUMN equipped_slot TEXT"); } catch (Exception ignored) {}
        db.execSQL("UPDATE items SET slot='weapon', equipped_slot=CASE WHEN equipped=1 THEN 'weapon' ELSE NULL END WHERE slot='primary_weapon'");
        db.execSQL("UPDATE items SET slot='chest', equipped_slot=CASE WHEN equipped=1 THEN 'chest' ELSE NULL END WHERE slot='armor_system'");
        db.execSQL("UPDATE items SET equipped_slot=CASE WHEN equipped=1 THEN 'utility' ELSE NULL END WHERE slot='utility'");
        int relicIndex = 1;
        try (Cursor c = db.rawQuery("SELECT id FROM items WHERE slot='relic' AND equipped=1 ORDER BY rowid", null)) {
            while (c.moveToNext() && relicIndex <= 4) {
                ContentValues v = new ContentValues();
                v.put("equipped_slot", "relic_" + relicIndex++);
                db.update("items", v, "id=?", new String[]{c.getString(0)});
            }
        }
        localizeSeededContent(db);
        try {
            GameState s = loadStateFrom(db);
            localizeState(s);
            ContentValues v = new ContentValues();
            v.put("json", s.toJson().toString());
            db.update("state", v, "id=1", null);
        } catch (Exception ignored) {}
    }

    private void migrateV3toV4(SQLiteDatabase db) {
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

    private void migrateV4toV5(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS mastery (name TEXT PRIMARY KEY, level INTEGER NOT NULL, xp INTEGER NOT NULL, next_xp INTEGER NOT NULL)");
        seedMastery(db);
    }

    private int masteryNext(int level){ return 300 + Math.max(0,Math.min(99,level))*10; }

    private void seedMastery(SQLiteDatabase db) {
        for (BaseStatCatalog.Group group : BaseStatCatalog.GROUPS) for (String stat : group.stats) {
            ContentValues v=new ContentValues();
            v.put("name",stat);v.put("level",70);v.put("xp",0);v.put("next_xp",masteryNext(70));
            db.insertWithOnConflict("mastery",null,v,SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private static int parseInt(String s, int fallback) { try { return Integer.parseInt(s); } catch (Exception e) { return fallback; } }
    private static long parseLong(String s, long fallback) { try { return Long.parseLong(s); } catch (Exception e) { return fallback; } }
    private static String compactLegacy(String s) { if (s == null) return "senas ėjimas"; s=s.replace('\n',' '); return s.length()>90?s.substring(0,90)+"…":s; }

    private void seed(SQLiteDatabase db) {
        try {
            GameState s = new GameState();
            ContentValues st = new ContentValues();
            st.put("id", 1); st.put("json", s.toJson().toString());
            db.insert("state", null, st);
        } catch (JSONException ignored) {}

        addItem(db,"6deb2206-413c-4ef4-bc75-876edeee91c2","Asteriono Ašmenys","weapon","legendary","Su Einoru susietas pagrindinis ginklas.","weapon","weapon",true,true);
        addItem(db,"4d04d2de-11f3-4e14-8dd2-295224ee998e","Septynsluoksnė Mantija","armor_system","legendary","Su Einoru susieta daugiasluoksnė krūtinės apsauga.","chest","chest",true,true);
        addItem(db,"66d2a35a-a00c-4566-bb34-0819af559e32","Kelių Klostės Krepšys","utility_item","rare","Erdvę lankstantis kelioninis krepšys.","utility","utility",true,false);
        addItem(db,"3b1de6bc-4198-4e4a-9d0d-631891c644e5","Rezonanso Signetas","major_relic","legendary","Relikvija, stiprinanti rezonanso kontrolę.","relic","relic_1",true,true);
        addItem(db,"0ea61c33-cbe9-4417-9b2b-6575baf1e38d","Nulinio Stiklo Prizmė","major_relic","legendary","Relikvija nulinėms sąveikoms tirti ir analizuoti.","relic","relic_2",true,true);
        addItem(db,"327f8e01-866c-44f1-aa10-189cd03f9155","Meridiano Raktas","major_relic","legendary","Prieigos prie Meridiano ribų ir jų sąveikos relikvija.","relic","relic_3",true,true);
        addItem(db,"50dfd168-9b38-4c10-a88b-6f71b1920e9d","Drakono Pabudimo Santarvės Žvynas","major_relic","legendary","Su drakonų santarve susieta relikvija.","relic","relic_4",true,true);
        addItem(db,"fe12ec4f-acea-4c51-b351-0d7449ec7ab8","Tyliojo Perkūno Šerdis","artifact","ancient","Ankstesnių žygių metu įgytas artefaktas.",null,null,false,false);
        addItem(db,"23639e76-8c70-4df8-97b8-02272314506a","Gyvosios Runos Sėkla","artifact","ancient","Gyvos runų kilmės artefaktas.",null,null,false,false);
        addItem(db,"ca47899c-451b-4e6e-a635-ab45a4c872c3","Žvaigždėkritos Kompasas","artifact","ancient","Navigacinis Žvaigždėkritos artefaktas.",null,null,false,false);
        addItem(db,"18304996-9186-41d5-9421-7b2a20e31df2","Orisono Astrolabija","artifact","ancient","Orisono stebėjimo ir navigacijos artefaktas.",null,null,false,false);
        addItem(db,"fb6dd5fd-8324-4bea-813c-8659bcdd531b","Arkakūjės Sėkla","anchored_artifact","ancient","Įtvirtinta Aksiomos Tiglio architektūroje.",null,null,false,false);
        addItem(db,"40e8b726-c711-4f8a-9900-70b32efc9819","Trigubos Santarvės Antspaudas","credential","unique","Nekovinis Santarvės tarybos nario įgaliojimo ženklas.",null,null,false,false);

        addAbility(db,"Eoninis Bastionas","gebėjimas_virš_ribos","Autonominė daugiasluoksnė fizinė, elementinė, arkaninė, erdvinė ir transmutacinė gynyba.");
        addAbility(db,"Tęstinumo Gardelė","gebėjimas_virš_ribos","Tapatybės ir nervų sistemos tęstinumo atramos leidžia atsikurti po mirtinų vietinių pažeidimų, jei išlieka nuosekli atrama.");
        addAbility(db,"Katastrofinė Regeneracija","gebėjimas_virš_ribos","Atkuria ekstremalius sužalojimus iš išlikusios struktūros, manos ir laiko; poveikis nėra momentinis.");
        addAbility(db,"Nuliui Prisitaikanti Fiziologija","gebėjimas_virš_ribos","Legendinis kūnas išlieka funkcionalus antimaginėje aplinkoje net slopinant maginius sluoksnius.");
        addAbility(db,"Refleksinis Erdvinis Išsisukimas","gebėjimas_virš_ribos","Grėsmės metu automatiškai keičia padėtį, nukreipia vektorius ir dalinai perstumia kūną erdvėje.");
        addAbility(db,"Prisitaikantis Kontrapynimas","gebėjimas_virš_ribos","Po kontakto su priešišku principu gynyba prisitaiko prie jo veikimo.");
        addAbility(db,"Relikvijų Simbiozė","gebėjimas_virš_ribos","Koordinuoja susietas relikvijas neviršijant rezonanso pralaidumo.");
        addAbility(db,"Laiko Paralakso Atskyrimas","tobulinimas_virš_ribos","Po stebėjimo atskiria vietinį tęstinumą nuo kitų laiko šakų aidų.");
        addAbility(db,"Nežinomų Taisyklių Kalibravimas","tobulinimas_virš_ribos","Po pirmojo kontakto greičiau sudaro saugesnius nežinomų taisyklių modelius.");
        addAbility(db,"Pasidalytas Meistriškumas","tobulinimas_virš_ribos","Leidžia kompetentingiems sąjungininkams perimti sprendimą nesuardant koordinacijos.");
        addAbility(db,"Sąlyginio Priežastingumo Struktūra","principas_virš_ribos","Kai kurią magiją pertvarko į įtvirtintus sąlygos ir pasekmės ryšius.");
        addAbility(db,"Santarvės Adapterio Struktūra","technika_virš_ribos","Kuria laikinus adapterius, verčiančius suprastas priežastines formas į suderinamas išraiškas.");
    }

    private void addItem(SQLiteDatabase db,String id,String name,String type,String rarity,String desc,String slot,String equippedSlot,boolean eq,boolean synced){
        ContentValues v=new ContentValues();
        v.put("id",id);v.put("name",name);v.put("type",type);v.put("rarity",rarity);v.put("description",desc);v.put("slot",slot);v.put("equipped",eq?1:0);v.put("synced",synced?1:0);v.put("equipped_slot",equippedSlot);
        db.insert("items",null,v);
    }

    private void addAbility(SQLiteDatabase db,String name,String type,String desc){
        ContentValues v=new ContentValues(); v.put("name",name);v.put("type",type);v.put("description",desc); db.insert("abilities",null,v);
    }

    private void localizeSeededContent(SQLiteDatabase db) {
        updateItem(db,"6deb2206-413c-4ef4-bc75-876edeee91c2","Asteriono Ašmenys","Su Einoru susietas pagrindinis ginklas.");
        updateItem(db,"4d04d2de-11f3-4e14-8dd2-295224ee998e","Septynsluoksnė Mantija","Su Einoru susieta daugiasluoksnė krūtinės apsauga.");
        updateItem(db,"66d2a35a-a00c-4566-bb34-0819af559e32","Kelių Klostės Krepšys","Erdvę lankstantis kelioninis krepšys.");
        updateItem(db,"3b1de6bc-4198-4e4a-9d0d-631891c644e5","Rezonanso Signetas","Relikvija, stiprinanti rezonanso kontrolę.");
        updateItem(db,"0ea61c33-cbe9-4417-9b2b-6575baf1e38d","Nulinio Stiklo Prizmė","Relikvija nulinėms sąveikoms tirti ir analizuoti.");
        updateItem(db,"327f8e01-866c-44f1-aa10-189cd03f9155","Meridiano Raktas","Prieigos prie Meridiano ribų ir jų sąveikos relikvija.");
        updateItem(db,"50dfd168-9b38-4c10-a88b-6f71b1920e9d","Drakono Pabudimo Santarvės Žvynas","Su drakonų santarve susieta relikvija.");
        updateItem(db,"fe12ec4f-acea-4c51-b351-0d7449ec7ab8","Tyliojo Perkūno Šerdis","Ankstesnių žygių metu įgytas artefaktas.");
        updateItem(db,"23639e76-8c70-4df8-97b8-02272314506a","Gyvosios Runos Sėkla","Gyvos runų kilmės artefaktas.");
        updateItem(db,"ca47899c-451b-4e6e-a635-ab45a4c872c3","Žvaigždėkritos Kompasas","Navigacinis Žvaigždėkritos artefaktas.");
        updateItem(db,"18304996-9186-41d5-9421-7b2a20e31df2","Orisono Astrolabija","Orisono stebėjimo ir navigacijos artefaktas.");
        updateItem(db,"fb6dd5fd-8324-4bea-813c-8659bcdd531b","Arkakūjės Sėkla","Įtvirtinta Aksiomos Tiglio architektūroje.");
        updateItem(db,"40e8b726-c711-4f8a-9900-70b32efc9819","Trigubos Santarvės Antspaudas","Nekovinis Santarvės tarybos nario įgaliojimo ženklas.");

        db.execSQL("DELETE FROM abilities");
        addAbility(db,"Eoninis Bastionas","gebėjimas_virš_ribos","Autonominė daugiasluoksnė fizinė, elementinė, arkaninė, erdvinė ir transmutacinė gynyba.");
        addAbility(db,"Tęstinumo Gardelė","gebėjimas_virš_ribos","Tapatybės ir nervų sistemos tęstinumo atramos leidžia atsikurti po mirtinų vietinių pažeidimų, jei išlieka nuosekli atrama.");
        addAbility(db,"Katastrofinė Regeneracija","gebėjimas_virš_ribos","Atkuria ekstremalius sužalojimus iš išlikusios struktūros, manos ir laiko; poveikis nėra momentinis.");
        addAbility(db,"Nuliui Prisitaikanti Fiziologija","gebėjimas_virš_ribos","Legendinis kūnas išlieka funkcionalus antimaginėje aplinkoje net slopinant maginius sluoksnius.");
        addAbility(db,"Refleksinis Erdvinis Išsisukimas","gebėjimas_virš_ribos","Grėsmės metu automatiškai keičia padėtį, nukreipia vektorius ir dalinai perstumia kūną erdvėje.");
        addAbility(db,"Prisitaikantis Kontrapynimas","gebėjimas_virš_ribos","Po kontakto su priešišku principu gynyba prisitaiko prie jo veikimo.");
        addAbility(db,"Relikvijų Simbiozė","gebėjimas_virš_ribos","Koordinuoja susietas relikvijas neviršijant rezonanso pralaidumo.");
        addAbility(db,"Laiko Paralakso Atskyrimas","tobulinimas_virš_ribos","Po stebėjimo atskiria vietinį tęstinumą nuo kitų laiko šakų aidų.");
        addAbility(db,"Nežinomų Taisyklių Kalibravimas","tobulinimas_virš_ribos","Po pirmojo kontakto greičiau sudaro saugesnius nežinomų taisyklių modelius.");
        addAbility(db,"Pasidalytas Meistriškumas","tobulinimas_virš_ribos","Leidžia kompetentingiems sąjungininkams perimti sprendimą nesuardant koordinacijos.");
        addAbility(db,"Sąlyginio Priežastingumo Struktūra","principas_virš_ribos","Kai kurią magiją pertvarko į įtvirtintus sąlygos ir pasekmės ryšius.");
        addAbility(db,"Santarvės Adapterio Struktūra","technika_virš_ribos","Kuria laikinus adapterius, verčiančius suprastas priežastines formas į suderinamas išraiškas.");
    }

    private void updateItem(SQLiteDatabase db,String id,String name,String description){
        ContentValues v=new ContentValues();v.put("name",name);v.put("description",description);db.update("items",v,"id=?",new String[]{id});
    }

    public GameState loadState() {
        try {
            GameState s=loadStateFrom(getReadableDatabase());
            localizeState(s);
            return s;
        } catch (Exception ignored) {}
        return new GameState();
    }

    private GameState loadStateFrom(SQLiteDatabase db) throws Exception {
        try (Cursor c = db.rawQuery("SELECT json FROM state WHERE id=1", null)) {
            if (c.moveToFirst()) return GameState.fromJson(new JSONObject(c.getString(0)));
        }
        return new GameState();
    }

    private void localizeState(GameState s){
        s.questTitle=replaceKnown(s.questTitle);
        s.objective=replaceKnown(s.objective);
        s.sceneTitle=replaceKnown(s.sceneTitle);
        s.scene=replaceKnown(s.scene);
        for(int i=0;i<s.choices.size();i++)s.choices.set(i,replaceKnown(s.choices.get(i)));
        for(int i=0;i<s.recentTurns.size();i++)s.recentTurns.set(i,replaceKnown(s.recentTurns.get(i)));
    }

    private String replaceKnown(String v){
        if(v==null)return "";
        return v.replace("The Broken Meridian","Lūžęs Meridianas")
                .replace("Waygate Drift","kelionės vartų poslinkis")
                .replace("waygate","kelionės vartai")
                .replace("Waygate","Kelionės vartai")
                .replace("The Late Roads","Vėlyvieji keliai")
                .replace("Discovery","Atradimas")
                .replace("discovery","atradimas");
    }

    public void saveState(GameState s) {
        try {
            ContentValues v = new ContentValues(); v.put("json", s.toJson().toString());
            getWritableDatabase().update("state", v, "id=1", null);
        } catch (JSONException ignored) {}
    }

    public java.util.Map<String,Integer> getStatValues() {
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

    public java.util.Map<String,Mastery> getMasteries() {
        java.util.LinkedHashMap<String,Mastery> out=new java.util.LinkedHashMap<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT name,level,xp,next_xp FROM mastery ORDER BY rowid",null)){while(c.moveToNext())out.put(c.getString(0),new Mastery(c.getInt(1),c.getInt(2),c.getInt(3)));}
        if(out.isEmpty()){seedMastery(getWritableDatabase());try(Cursor c=getReadableDatabase().rawQuery("SELECT name,level,xp,next_xp FROM mastery ORDER BY rowid",null)){while(c.moveToNext())out.put(c.getString(0),new Mastery(c.getInt(1),c.getInt(2),c.getInt(3)));}}
        return out;
    }

    public java.util.Map<String,Integer> getMasteryLevels(){java.util.LinkedHashMap<String,Integer> out=new java.util.LinkedHashMap<>();for(java.util.Map.Entry<String,Mastery> e:getMasteries().entrySet())out.put(e.getKey(),e.getValue().level);return out;}

    public Mastery awardMastery(String name,int gain){
        Mastery cur=getMasteries().get(name);if(cur==null)cur=new Mastery(70,0,masteryNext(70));int level=cur.level,xp=cur.xp+Math.max(0,gain),next=cur.nextXp;while(level<100&&xp>=next){xp-=next;level++;next=masteryNext(level);}if(level>=100){level=100;xp=0;next=1;}ContentValues v=new ContentValues();v.put("level",level);v.put("xp",xp);v.put("next_xp",next);getWritableDatabase().update("mastery",v,"name=?",new String[]{name});return new Mastery(level,xp,next);
    }

    public List<Item> getItems() {
        List<Item> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,type,rarity,description,slot,equipped,synced,equipped_slot FROM items ORDER BY equipped DESC, CASE rarity WHEN 'unique' THEN 0 WHEN 'legendary' THEN 1 WHEN 'ancient' THEN 2 WHEN 'epic' THEN 3 WHEN 'rare' THEN 4 ELSE 5 END, name", null)) {
            while (c.moveToNext()) {
                Item i=new Item();
                i.id=c.getString(0);i.name=c.getString(1);i.type=c.getString(2);i.rarity=c.getString(3);i.description=c.getString(4);i.slot=c.isNull(5)?null:c.getString(5);i.equipped=c.getInt(6)==1;i.synced=c.getInt(7)==1;i.equippedSlot=c.isNull(8)?null:c.getString(8);out.add(i);
            }
        }
        return out;
    }

    public Item getEquippedAt(String targetSlot){
        try(Cursor c=getReadableDatabase().rawQuery("SELECT id,name,type,rarity,description,slot,equipped,synced,equipped_slot FROM items WHERE equipped=1 AND equipped_slot=? LIMIT 1",new String[]{targetSlot})){
            if(c.moveToFirst()){
                Item i=new Item();i.id=c.getString(0);i.name=c.getString(1);i.type=c.getString(2);i.rarity=c.getString(3);i.description=c.getString(4);i.slot=c.isNull(5)?null:c.getString(5);i.equipped=c.getInt(6)==1;i.synced=c.getInt(7)==1;i.equippedSlot=c.isNull(8)?null:c.getString(8);return i;
            }
        }
        return null;
    }

    public List<Item> getItemsForTarget(String targetSlot){
        List<Item> out=new ArrayList<>();
        String category=categoryForTarget(targetSlot);
        for(Item i:getItems())if(category.equals(i.slot))out.add(i);
        return out;
    }

    public List<String[]> getAbilities() {
        List<String[]> out=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT name,type,description FROM abilities ORDER BY id",null)){
            while(c.moveToNext()) out.add(new String[]{c.getString(0),c.getString(1),c.getString(2)});
        }
        return out;
    }

    public boolean equipToSlot(String itemId,String targetSlot){
        SQLiteDatabase db=getWritableDatabase();
        String category=null;
        try(Cursor c=db.rawQuery("SELECT slot FROM items WHERE id=?",new String[]{itemId})){if(c.moveToFirst())category=c.isNull(0)?null:c.getString(0);}
        if(category==null || !category.equals(categoryForTarget(targetSlot)))return false;
        db.beginTransaction();
        try{
            ContentValues clearTarget=new ContentValues();clearTarget.put("equipped",0);clearTarget.putNull("equipped_slot");db.update("items",clearTarget,"equipped_slot=?",new String[]{targetSlot});
            ContentValues clearItem=new ContentValues();clearItem.put("equipped",0);clearItem.putNull("equipped_slot");db.update("items",clearItem,"id=?",new String[]{itemId});
            ContentValues on=new ContentValues();on.put("equipped",1);on.put("equipped_slot",targetSlot);db.update("items",on,"id=?",new String[]{itemId});
            db.setTransactionSuccessful();return true;
        }finally{db.endTransaction();}
    }

    public boolean unequipSlot(String targetSlot){
        ContentValues v=new ContentValues();v.put("equipped",0);v.putNull("equipped_slot");
        return getWritableDatabase().update("items",v,"equipped_slot=?",new String[]{targetSlot})>0;
    }

    public boolean toggleEquip(String itemId) {
        Item found=null;for(Item i:getItems())if(i.id.equals(itemId)){found=i;break;}
        if(found==null||found.slot==null)return false;
        if(found.equipped&&found.equippedSlot!=null)return unequipSlot(found.equippedSlot);
        String target=firstFreeTarget(found.slot);
        return target!=null&&equipToSlot(itemId,target);
    }

    private String firstFreeTarget(String category){
        if("ring".equals(category)){if(getEquippedAt("ring_left")==null)return"ring_left";if(getEquippedAt("ring_right")==null)return"ring_right";return"ring_left";}
        if("relic".equals(category)){for(int i=1;i<=4;i++)if(getEquippedAt("relic_"+i)==null)return"relic_"+i;return"relic_1";}
        return category;
    }

    private String categoryForTarget(String target){
        if(target==null)return"";
        if(target.startsWith("ring_"))return"ring";
        if(target.startsWith("relic_"))return"relic";
        return target;
    }

    public String equippedSummary() {
        StringBuilder b=new StringBuilder();
        for(String target:EQUIPMENT_SLOTS){Item i=getEquippedAt(target);if(i!=null){if(b.length()>0)b.append("; ");b.append(slotLabel(target)).append(": ").append(i.name);}}
        return b.toString();
    }

    public String addLoot(String name,String category,String rarity,String description){
        if(!isAllowedCategory(category))category="artifact";
        if(!isAllowedRarity(rarity))rarity="common";
        String id=UUID.randomUUID().toString();
        ContentValues v=new ContentValues();v.put("id",id);v.put("name",name);v.put("type","generated_loot");v.put("rarity",rarity);v.put("description",description);v.put("slot","artifact".equals(category)?null:category);v.put("equipped",0);v.put("synced",0);v.putNull("equipped_slot");
        getWritableDatabase().insert("items",null,v);return id;
    }

    private boolean isAllowedCategory(String c){return c!=null&&java.util.Arrays.asList("weapon","offhand","head","chest","hands","legs","feet","belt","neck","ring","utility","relic","artifact").contains(c);}
    private boolean isAllowedRarity(String r){return r!=null&&java.util.Arrays.asList("common","uncommon","rare","epic","legendary","ancient","unique").contains(r.toLowerCase(Locale.ROOT));}

    public void checkpoint(String label, GameState state) {
        try {
            JSONArray eq = new JSONArray();
            for(Item i:getItems()) if(i.equipped) {JSONObject o=new JSONObject();o.put("id",i.id);o.put("slot",i.equippedSlot);eq.put(o);}
            ContentValues v=new ContentValues(); v.put("label",label);v.put("state_json",state.toJson().toString());v.put("equipment_json",eq.toString());v.put("created_at",System.currentTimeMillis());
            SQLiteDatabase db=getWritableDatabase(); db.insert("checkpoints",null,v);
            db.execSQL("DELETE FROM checkpoints WHERE id NOT IN (SELECT id FROM checkpoints ORDER BY id DESC LIMIT 20)");
        } catch(Exception ignored){}
    }

    public boolean undo() {
        SQLiteDatabase db=getWritableDatabase();
        try(Cursor c=db.rawQuery("SELECT id,state_json,equipment_json FROM checkpoints ORDER BY id DESC LIMIT 1",null)){
            if(!c.moveToFirst()) return false;
            long id=c.getLong(0); String state=c.getString(1); JSONArray eq=new JSONArray(c.getString(2));
            db.beginTransaction();
            try {
                ContentValues st=new ContentValues();st.put("json",state);db.update("state",st,"id=1",null);
                ContentValues off=new ContentValues();off.put("equipped",0);off.putNull("equipped_slot");db.update("items",off,null,null);
                for(int i=0;i<eq.length();i++){
                    Object raw=eq.get(i);String itemId;String target;
                    if(raw instanceof JSONObject){JSONObject o=(JSONObject)raw;itemId=o.getString("id");target=o.optString("slot",null);}else{itemId=String.valueOf(raw);target=null;}
                    if(target==null||target.isEmpty()){
                        String cat=null;try(Cursor q=db.rawQuery("SELECT slot FROM items WHERE id=?",new String[]{itemId})){if(q.moveToFirst())cat=q.getString(0);}target=firstFreeTarget(cat);
                    }
                    if(target!=null){ContentValues on=new ContentValues();on.put("equipped",1);on.put("equipped_slot",target);db.update("items",on,"id=?",new String[]{itemId});}
                }
                db.delete("checkpoints","id=?",new String[]{String.valueOf(id)});
                db.setTransactionSuccessful(); return true;
            } finally { db.endTransaction(); }
        } catch(Exception e){ return false; }
    }

    public String exportSave() {
        try {
            JSONObject root=new JSONObject(); root.put("version",5); root.put("state",loadState().toJson());
            JSONArray items=new JSONArray(); for(Item i:getItems()){JSONObject o=new JSONObject();o.put("id",i.id);o.put("name",i.name);o.put("category",i.slot==null?"artifact":i.slot);o.put("rarity",i.rarity);o.put("description",i.description);o.put("equipped_slot",i.equippedSlot==null?JSONObject.NULL:i.equippedSlot);items.put(o);} root.put("items",items); JSONArray stats=new JSONArray();for(java.util.Map.Entry<String,Integer> e:getStatValues().entrySet()){JSONObject so=new JSONObject();so.put("name",e.getKey());so.put("value",e.getValue());stats.put(so);}root.put("stats",stats); JSONArray mastery=new JSONArray();for(java.util.Map.Entry<String,Mastery> e:getMasteries().entrySet()){JSONObject mo=new JSONObject();mo.put("name",e.getKey());mo.put("level",e.getValue().level);mo.put("xp",e.getValue().xp);mo.put("next_xp",e.getValue().nextXp);mastery.put(mo);}root.put("mastery",mastery); return root.toString();
        } catch(Exception e){return "";}
    }

    public boolean importSave(String raw) {
        try {
            JSONObject root=new JSONObject(raw); GameState state=GameState.fromJson(root.getJSONObject("state")); JSONArray items=root.optJSONArray("items"); JSONArray stats=root.optJSONArray("stats"); JSONArray mastery=root.optJSONArray("mastery");
            SQLiteDatabase db=getWritableDatabase(); db.beginTransaction();
            try {
                ContentValues st=new ContentValues();st.put("json",state.toJson().toString());db.update("state",st,"id=1",null);
                ContentValues off=new ContentValues();off.put("equipped",0);off.putNull("equipped_slot");db.update("items",off,null,null);
                if(items!=null)for(int i=0;i<items.length();i++){
                    JSONObject o=items.getJSONObject(i);String id=o.optString("id","");
                    if(id.isEmpty())continue;
                    boolean exists=false;try(Cursor q=db.rawQuery("SELECT 1 FROM items WHERE id=?",new String[]{id})){exists=q.moveToFirst();}
                    if(!exists){ContentValues add=new ContentValues();add.put("id",id);add.put("name",o.optString("name","Nežinomas daiktas"));add.put("type","imported");add.put("rarity",o.optString("rarity","common"));add.put("description",o.optString("description",""));String cat=o.optString("category","artifact");add.put("slot","artifact".equals(cat)?null:cat);add.put("equipped",0);add.put("synced",0);add.putNull("equipped_slot");db.insert("items",null,add);}
                    String target=o.isNull("equipped_slot")?null:o.optString("equipped_slot",null);
                    if(target==null&&o.optBoolean("equipped",false)){String cat=o.optString("category","");target=firstFreeTarget(cat);}
                    if(target!=null){ContentValues on=new ContentValues();on.put("equipped",1);on.put("equipped_slot",target);db.update("items",on,"id=?",new String[]{id});}
                }
                if(stats!=null)for(int si=0;si<stats.length();si++){JSONObject so=stats.optJSONObject(si);if(so==null)continue;ContentValues sv=new ContentValues();sv.put("value",Math.max(0,Math.min(100,so.optInt("value",100))));db.update("stats",sv,"name=?",new String[]{so.optString("name","")});}
                if(mastery!=null)for(int mi=0;mi<mastery.length();mi++){JSONObject mo=mastery.optJSONObject(mi);if(mo==null)continue;ContentValues mv=new ContentValues();int lv=Math.max(0,Math.min(100,mo.optInt("level",70)));mv.put("level",lv);mv.put("xp",Math.max(0,mo.optInt("xp",0)));mv.put("next_xp",Math.max(1,mo.optInt("next_xp",masteryNext(lv))));db.update("mastery",mv,"name=?",new String[]{mo.optString("name","")});}
                db.setTransactionSuccessful();return true;
            } finally { db.endTransaction(); }
        } catch(Exception e){return false;}
    }

    public void reset() {
        SQLiteDatabase db=getWritableDatabase(); db.execSQL("DROP TABLE IF EXISTS state");db.execSQL("DROP TABLE IF EXISTS items");db.execSQL("DROP TABLE IF EXISTS abilities");db.execSQL("DROP TABLE IF EXISTS checkpoints");db.execSQL("DROP TABLE IF EXISTS stats");db.execSQL("DROP TABLE IF EXISTS mastery");db.execSQL("DROP TABLE IF EXISTS turns");db.execSQL("DROP TABLE IF EXISTS undo");onCreate(db);
    }

    public static String slotLabel(String s){
        if(s==null)return"";
        switch(s){
            case"weapon":return"Pagrindinis ginklas";case"offhand":return"Antrinis ginklas / skydas";case"head":return"Šalmas";case"chest":return"Krūtinės šarvai";case"hands":return"Pirštinės";case"legs":return"Kelnės";case"feet":return"Batai";case"belt":return"Diržas";case"neck":return"Pakabukas";case"ring_left":return"Kairysis žiedas";case"ring_right":return"Dešinysis žiedas";case"utility":return"Naudingasis daiktas";case"relic_1":return"Relikvija I";case"relic_2":return"Relikvija II";case"relic_3":return"Relikvija III";case"relic_4":return"Relikvija IV";default:return s;
        }
    }
}
