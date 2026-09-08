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
    private static final int VERSION = 14;
    private static final String ITEM_COLUMNS = "id,name,type,rarity,description,slot,equipped,synced,equipped_slot,catalog_id,item_level,power,set_id,quantity,value,effect";

    public static final String[] EQUIPMENT_SLOTS = new String[]{
            "weapon","offhand","head","chest","hands","legs","feet","belt","neck",
            "ring_left","ring_right","utility","relic_1","relic_2","relic_3","relic_4"
    };

    public static class Item {
        public String id, name, type, rarity, description, slot, equippedSlot, catalogId, setId, effect;
        public int itemLevel, power, quantity, value;
        public boolean equipped, synced;
    }

    public static class Mastery {
        public final int level,xp,nextXp;
        public Mastery(int level,int xp,int nextXp){this.level=level;this.xp=xp;this.nextXp=nextXp;}
    }

    public static class SaveSlot {
        public int slot,level;public String name,location;public long updatedAt;
    }

    private WorldRepository worldRepository;
    SideQuestRepository sideQuests(){return new SideQuestRepository(this);}

    public VaeloriaDb(Context c) { super(c, DB, null, VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE state (id INTEGER PRIMARY KEY CHECK(id=1), json TEXT NOT NULL)");
        db.execSQL("CREATE TABLE items (id TEXT PRIMARY KEY, name TEXT NOT NULL, type TEXT NOT NULL, rarity TEXT NOT NULL, description TEXT NOT NULL, slot TEXT, equipped INTEGER NOT NULL DEFAULT 0, synced INTEGER NOT NULL DEFAULT 0, equipped_slot TEXT, catalog_id TEXT, item_level INTEGER NOT NULL DEFAULT 1, power INTEGER NOT NULL DEFAULT 0, set_id TEXT, quantity INTEGER NOT NULL DEFAULT 1, value INTEGER NOT NULL DEFAULT 0, effect TEXT NOT NULL DEFAULT '')");
        db.execSQL("CREATE TABLE abilities (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL, type TEXT NOT NULL, description TEXT NOT NULL)");
        db.execSQL("CREATE TABLE checkpoints (id INTEGER PRIMARY KEY AUTOINCREMENT, label TEXT, state_json TEXT NOT NULL, equipment_json TEXT NOT NULL, snapshot_json TEXT, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE stats (name TEXT PRIMARY KEY, group_name TEXT NOT NULL, value INTEGER NOT NULL, cap INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE mastery (name TEXT PRIMARY KEY, level INTEGER NOT NULL, xp INTEGER NOT NULL, next_xp INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS save_slots (slot INTEGER PRIMARY KEY CHECK(slot BETWEEN 1 AND 3), name TEXT NOT NULL, snapshot_json TEXT NOT NULL, updated_at INTEGER NOT NULL, location TEXT NOT NULL, level INTEGER NOT NULL)");
        seed(db);
        seedStats(db);
        seedMastery(db);
        WorldRepository.create(db);
        WorldRepository.seed(db,new GameState());
        SideQuestRepository.seed(db);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            migrateV1(db);
            oldVersion = 2;
        }
        if (oldVersion < 3) { migrateV2toV3(db); oldVersion = 3; }
        if (oldVersion < 4) { migrateV3toV4(db); oldVersion = 4; }
        if (oldVersion < 5) { migrateV4toV5(db); oldVersion = 5; }
        if (oldVersion < 6) { migrateV5toV6(db); oldVersion = 6; }
        if (oldVersion < 7) { migrateV6toV7(db); oldVersion = 7; }
        if (oldVersion < 8) { migrateV7toV8(db); oldVersion = 8; }
        if (oldVersion < 9) { migrateV8toV9(db); oldVersion = 9; }
        if (oldVersion < 10) { migrateV9toV10(db); oldVersion = 10; }
        if (oldVersion < 11) { migrateV10toV11(db); oldVersion = 11; }
        if (oldVersion < 12) { migrateV11toV12(db); oldVersion = 12; }
        if (oldVersion < 13) migrateV12toV13(db);
        if (oldVersion < 14) {WorldRepository.seed(db,new GameState());SideQuestRepository.seed(db);}
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

    private void migrateV5toV6(SQLiteDatabase db) {
        addColumn(db, "ALTER TABLE items ADD COLUMN catalog_id TEXT");
        addColumn(db, "ALTER TABLE items ADD COLUMN item_level INTEGER NOT NULL DEFAULT 1");
        addColumn(db, "ALTER TABLE items ADD COLUMN power INTEGER NOT NULL DEFAULT 0");
        addColumn(db, "ALTER TABLE items ADD COLUMN set_id TEXT");
        addColumn(db, "ALTER TABLE items ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1");
        addColumn(db, "ALTER TABLE items ADD COLUMN value INTEGER NOT NULL DEFAULT 0");
        addColumn(db, "ALTER TABLE items ADD COLUMN effect TEXT NOT NULL DEFAULT ''");
        seedStarterConsumables(db);
    }

    private void migrateV6toV7(SQLiteDatabase db) {
        try {
            GameState state=loadStateFrom(db);
            LithuanianNarrative.polishState(state);
            ContentValues values=new ContentValues();values.put("json",state.toJson().toString());
            db.update("state",values,"id=1",null);
        } catch (Exception ignored) {}
        updateItem(db,"6deb2206-413c-4ef4-bc75-876edeee91c2","Asteriono Ašmenys","Su veikėju susietas pagrindinis ginklas.");
        updateItem(db,"4d04d2de-11f3-4e14-8dd2-295224ee998e","Septynsluoksnė Mantija","Su veikėju susieta daugiasluoksnė krūtinės apsauga.");
    }

    private void migrateV7toV8(SQLiteDatabase db) {
        addColumn(db, "ALTER TABLE checkpoints ADD COLUMN snapshot_json TEXT");
    }

    private void migrateV8toV9(SQLiteDatabase db) {
        WorldRepository.create(db);
        GameState current;
        try { current=loadStateFrom(db); } catch(Exception ignored) { current=new GameState(); }
        WorldRepository.seed(db,current);
    }

    private void migrateV9toV10(SQLiteDatabase db) {
        WorldRepository.create(db);
        GameState current;
        try { current=loadStateFrom(db); } catch(Exception ignored) { current=new GameState(); }
        WorldRepository.seed(db,current);
    }

    private void migrateV10toV11(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS save_slots (slot INTEGER PRIMARY KEY CHECK(slot BETWEEN 1 AND 3), name TEXT NOT NULL, snapshot_json TEXT NOT NULL, updated_at INTEGER NOT NULL, location TEXT NOT NULL, level INTEGER NOT NULL)");
        WorldRepository.create(db);GameState current;try{current=loadStateFrom(db);}catch(Exception ignored){current=new GameState();}WorldRepository.seed(db,current);
    }

    /** Pašalina v1.0.0 dvigubai pritaikytas profilio premijas, neliesdama kitų išsaugojimų. */
    private void migrateV11toV12(SQLiteDatabase db) {
        boolean balancedProfile=false;
        try(Cursor c=db.rawQuery("SELECT 1 FROM items WHERE id='starter-balanced-weapon' OR id='starter-balanced-chest' LIMIT 1",null)){
            balancedProfile=c.moveToFirst();
        }catch(Exception ignored){}
        if(!balancedProfile)return;
        try{
            GameState state=loadStateFrom(db);
            if(!state.characterCreated||!"balanced".equals(state.progressionMode))return;
            try(Cursor c=db.rawQuery("SELECT name,group_name,value FROM stats",null)){
                while(c.moveToNext()){
                    String name=c.getString(0),group=c.getString(1);int value=c.getInt(2);
                    int legacy=CharacterCatalogV093.legacyPersistedBonus(state.characterOriginId,state.characterArchetypeId,name,group);
                    if(legacy<=0)continue;
                    ContentValues fixed=new ContentValues();fixed.put("value",Math.max(0,value-legacy));
                    db.update("stats",fixed,"name=?",new String[]{name});
                }
            }
        }catch(Exception ignored){}
    }

    /** Įdiegia P1 receptus, savybių taškus ir pataiso senus viršlygius startinius daiktus. */
    private void migrateV12toV13(SQLiteDatabase db) {
        WorldRepository.migrateV13(db);
        try {
            GameState state=loadStateFrom(db);
            if(state.characterCreated&&"balanced".equals(state.progressionMode)){
                replaceStarter(db,"starter-balanced-weapon",starterWeapon(state.characterArchetypeId),"weapon");
                replaceStarter(db,"starter-balanced-chest",ItemCatalogV092.byId("I092-101"),"chest");
                if("sargybinis".equals(state.characterArchetypeId))replaceStarter(db,"starter-balanced-offhand",ItemCatalogV092.byId("I092-054"),"offhand");
                refreshStarterConsumable(db,"starter-I092-201",ItemCatalogV092.byId("I092-201"));
                refreshStarterConsumable(db,"starter-I092-203",ItemCatalogV092.byId("I092-203"));
                refreshStarterConsumable(db,"starter-I092-204",ItemCatalogV092.byId("I092-204"));
            }
            ContentValues saved=new ContentValues();saved.put("json",state.toJson().toString());db.update("state",saved,"id=1",null);
        } catch (Exception ignored) {}
    }

    private void replaceStarter(SQLiteDatabase db,String id,ItemCatalogV092.ItemDef item,String target){
        if(item==null)return;int quantity=1;boolean exists=false;
        try(Cursor c=db.rawQuery("SELECT quantity FROM items WHERE id=?",new String[]{id})){if(c.moveToFirst()){exists=true;quantity=Math.max(1,c.getInt(0));}}
        if(!exists)return;ContentValues values=catalogValues(item,quantity);values.put("id",id);values.put("equipped",1);values.put("equipped_slot",target);
        db.insertWithOnConflict("items",null,values,SQLiteDatabase.CONFLICT_REPLACE);
    }

    private void refreshStarterConsumable(SQLiteDatabase db,String id,ItemCatalogV092.ItemDef item){
        if(item==null)return;int quantity=0;try(Cursor c=db.rawQuery("SELECT quantity FROM items WHERE id=?",new String[]{id})){if(c.moveToFirst())quantity=Math.max(1,c.getInt(0));}
        if(quantity==0)return;ContentValues values=catalogValues(item,quantity);values.put("id",id);db.insertWithOnConflict("items",null,values,SQLiteDatabase.CONFLICT_REPLACE);
    }

    private void addColumn(SQLiteDatabase db, String sql) {
        try { db.execSQL(sql); } catch (Exception ignored) {}
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

        addItem(db,"6deb2206-413c-4ef4-bc75-876edeee91c2","Asteriono Ašmenys","weapon","legendary","Su veikėju susietas pagrindinis ginklas.","weapon","weapon",true,true);
        addItem(db,"4d04d2de-11f3-4e14-8dd2-295224ee998e","Septynsluoksnė Mantija","armor_system","legendary","Su veikėju susieta daugiasluoksnė krūtinės apsauga.","chest","chest",true,true);
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
        seedStarterConsumables(db);
    }

    private void addItem(SQLiteDatabase db,String id,String name,String type,String rarity,String desc,String slot,String equippedSlot,boolean eq,boolean synced){
        ContentValues v=new ContentValues();
        v.put("id",id);v.put("name",name);v.put("type",type);v.put("rarity",rarity);v.put("description",desc);v.put("slot",slot);v.put("equipped",eq?1:0);v.put("synced",synced?1:0);v.put("equipped_slot",equippedSlot);
        db.insert("items",null,v);
    }

    private void seedStarterConsumables(SQLiteDatabase db) {
        addCatalogItem(db, ItemCatalogV092.byId("I092-201"), 3, "starter-I092-201");
        addCatalogItem(db, ItemCatalogV092.byId("I092-203"), 2, "starter-I092-203");
        addCatalogItem(db, ItemCatalogV092.byId("I092-204"), 2, "starter-I092-204");
    }

    private void addCatalogItem(SQLiteDatabase db, ItemCatalogV092.ItemDef item, int quantity, String instanceId) {
        if (item == null) return;
        ContentValues v = catalogValues(item, Math.max(1, quantity));
        v.put("id", instanceId);
        db.insertWithOnConflict("items", null, v, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void addAbility(SQLiteDatabase db,String name,String type,String desc){
        ContentValues v=new ContentValues(); v.put("name",name);v.put("type",type);v.put("description",desc); db.insert("abilities",null,v);
    }

    private void localizeSeededContent(SQLiteDatabase db) {
        updateItem(db,"6deb2206-413c-4ef4-bc75-876edeee91c2","Asteriono Ašmenys","Su veikėju susietas pagrindinis ginklas.");
        updateItem(db,"4d04d2de-11f3-4e14-8dd2-295224ee998e","Septynsluoksnė Mantija","Su veikėju susieta daugiasluoksnė krūtinės apsauga.");
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
            if(syncEquipmentCapacity(s,equipmentStats()))saveState(s);
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
        LithuanianNarrative.polishState(s);
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
            if(getWritableDatabase().update("state", v, "id=1", null)!=1)throw new IllegalStateException("Missing game state");
        } catch (JSONException error) {throw new IllegalStateException("Invalid game state",error);}
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

    /** Vienas savybės taškas suteikia +2 pasirinktai bazinei savybei iki 100 ribos. */
    public String spendAttributePoint(String name,GameState state){
        if(state==null||!knownStat(name))return"Savybė nerasta";
        if(state.attributePoints<=0)return"Nėra laisvų savybių taškų";
        int current=getStatValue(name);if(current>=100)return name+" jau pasiekė bazinę 100 ribą";
        SQLiteDatabase database=getWritableDatabase();database.beginTransaction();
        try{
            ContentValues value=new ContentValues();value.put("value",Math.min(100,current+2));database.update("stats",value,"name=?",new String[]{name});
            state.attributePoints--;ContentValues saved=new ContentValues();saved.put("json",state.toJson().toString());database.update("state",saved,"id=1",null);
            database.setTransactionSuccessful();return name+" padidinta iki "+Math.min(100,current+2)+" · liko "+state.attributePoints+" savybių tšk.";
        }catch(Exception error){return"Nepavyko paskirstyti savybės taško";}finally{database.endTransaction();}
    }

    /** Vienkartinė naujo veikėjo ekonomikos ir galios kreivė. Esamų išsaugojimų nemažina. */
    public void initializeCharacterProgression(GameState state,String requestedMode,String originId,String archetypeId){
        String mode="legendary".equals(requestedMode)?"legendary":"balanced";
        state.hpMax=Math.max(1,state.hpMax-state.equipmentHpBonus);state.hp=Math.min(state.hp,state.hpMax);state.manaMax=Math.max(0,state.manaMax-state.equipmentManaBonus);state.mana=Math.min(state.mana,state.manaMax);state.equipmentHpBonus=0;state.equipmentManaBonus=0;
        state.progressionMode=mode;
        if("legendary".equals(mode)){
            state.level=100;state.experience=0;state.experienceNext=1;state.talentPoints=Math.max(75,state.talentPoints);
            state.attributePoints=0;
            state.hp=state.hpMax=100;state.mana=state.manaMax=100;state.stamina=state.staminaMax=100;state.aeonic=Math.max(180,state.aeonic);state.aeonicMax=Math.max(900,state.aeonicMax);syncEquipmentCapacity(state,equipmentStats());
            saveState(state);return;
        }

        SQLiteDatabase database=getWritableDatabase();database.beginTransaction();
        try{
            ContentValues base=new ContentValues();base.put("value",40);database.update("stats",base,null,null);
            // Kilmės (+3) ir archetipo (+5) premijas kiekvienai patikrai taiko
            // CharacterCatalogV093.effect. Bazėje laikoma tik tikroji bazinė reikšmė,
            // todėl pakeitus profilį nelieka seno archetipo paslėptų premijų.
            ContentValues mastery=new ContentValues();mastery.put("level",0);mastery.put("xp",0);mastery.put("next_xp",masteryNext(0));database.update("mastery",mastery,null,null);

            database.delete("items",null,null);database.delete("abilities",null,null);
            ItemCatalogV092.ItemDef weapon=starterWeapon(archetypeId);ItemCatalogV092.ItemDef chest=lowestItem("chest",null);
            insertStarter(database,weapon,1,"starter-balanced-weapon","weapon");
            insertStarter(database,chest,1,"starter-balanced-chest","chest");
            if("sargybinis".equals(archetypeId))insertStarter(database,lowestItem("offhand",null),1,"starter-balanced-offhand","offhand");
            insertStarter(database,ItemCatalogV092.byId("I092-201"),3,"starter-I092-201",null);
            insertStarter(database,ItemCatalogV092.byId("I092-203"),2,"starter-I092-203",null);
            insertStarter(database,ItemCatalogV092.byId("I092-204"),2,"starter-I092-204",null);
            addAbility(database,"Archetipinis pasirengimas","bazinis_gebėjimas",CharacterCatalogV093.archetypeName(archetypeId)+" moka saugiai taikyti savo pagrindines priemones.");
            addAbility(database,"Lauko improvizacija","bazinis_gebėjimas","Po nesėkmės leidžia ieškoti kito pagrįsto sprendimo, nepanaikinant pasekmių.");
            database.execSQL("UPDATE talents SET unlocked=0,unlocked_minute=0");
            database.execSQL("UPDATE companions SET recruited=0,active=0,loyalty=20");
            world().initializeOriginDiscoveries(originId);

            state.level=1;state.experience=0;state.experienceNext=ProgressionEngine.experienceForNext(1);state.talentPoints=1;state.attributePoints=0;
            state.crowns=650;state.aeonic=30;state.aeonicMax=100;
            state.hpMax="sargybinis".equals(archetypeId)?120:"arkanistas".equals(archetypeId)?88:100;state.hp=state.hpMax;
            state.staminaMax="klajunas".equals(archetypeId)?120:"arkanistas".equals(archetypeId)?90:100;state.stamina=state.staminaMax;
            state.manaMax="arkanistas".equals(archetypeId)?110:"amatininkas".equals(archetypeId)?65:45;state.mana=state.manaMax;syncEquipmentCapacity(state,equipmentStats());
            ContentValues saved=new ContentValues();saved.put("json",state.toJson().toString());database.update("state",saved,"id=1",null);
            database.setTransactionSuccessful();
        }catch(Exception ignored){}finally{database.endTransaction();}
    }

    private ItemCatalogV092.ItemDef starterWeapon(String archetype){
        if("zvalgas".equals(archetype))return ItemCatalogV092.byId("I092-026");
        // Visi kiti archetipai pradeda nuo tikro L1 ginklo. Specializuotas lazdas,
        // lankus ir kūjus jie gauna per progresiją, o ne apeidami daikto lygį.
        return ItemCatalogV092.byId("I092-003");
    }
    private ItemCatalogV092.ItemDef lowestItem(String category,String subtype){ItemCatalogV092.ItemDef best=null;for(ItemCatalogV092.ItemDef item:ItemCatalogV092.ALL)if(category.equals(item.category)&&(subtype==null||subtype.equals(item.subtype))&&(best==null||item.level<best.level))best=item;return best;}
    private void insertStarter(SQLiteDatabase database,ItemCatalogV092.ItemDef item,int quantity,String id,String target){if(item==null)return;ContentValues values=catalogValues(item,quantity);values.put("id",id);if(target!=null){values.put("equipped",1);values.put("equipped_slot",target);}database.insertWithOnConflict("items",null,values,SQLiteDatabase.CONFLICT_REPLACE);}

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
        try (Cursor c = getReadableDatabase().rawQuery("SELECT " + ITEM_COLUMNS + " FROM items ORDER BY equipped DESC, CASE rarity WHEN 'unique' THEN 0 WHEN 'ancient' THEN 1 WHEN 'mythic' THEN 2 WHEN 'legendary' THEN 3 WHEN 'epic' THEN 4 WHEN 'rare' THEN 5 WHEN 'uncommon' THEN 6 ELSE 7 END, item_level DESC, name", null)) {
            while (c.moveToNext()) out.add(readItem(c));
        }
        return out;
    }

    public Item getEquippedAt(String targetSlot){
        try(Cursor c=getReadableDatabase().rawQuery("SELECT "+ITEM_COLUMNS+" FROM items WHERE equipped=1 AND equipped_slot=? LIMIT 1",new String[]{targetSlot})){
            if(c.moveToFirst()) return readItem(c);
        }
        return null;
    }

    public Item getItem(String itemId) {
        try (Cursor c=getReadableDatabase().rawQuery("SELECT "+ITEM_COLUMNS+" FROM items WHERE id=? LIMIT 1",new String[]{itemId})) {
            return c.moveToFirst() ? readItem(c) : null;
        }
    }

    private Item readItem(Cursor c) {
        Item i=new Item();
        i.id=c.getString(0);i.name=c.getString(1);i.type=c.getString(2);i.rarity=c.getString(3);
        i.description=c.getString(4);i.slot=c.isNull(5)?null:c.getString(5);i.equipped=c.getInt(6)==1;
        i.synced=c.getInt(7)==1;i.equippedSlot=c.isNull(8)?null:c.getString(8);
        i.catalogId=c.isNull(9)?null:c.getString(9);i.itemLevel=c.getInt(10);i.power=c.getInt(11);
        i.setId=c.isNull(12)?null:c.getString(12);i.quantity=Math.max(1,c.getInt(13));i.value=c.getInt(14);
        i.effect=c.isNull(15)?"":c.getString(15);
        return i;
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
        GameState current=loadState();return equipToSlot(itemId,targetSlot,current.level);
    }

    public boolean equipToSlot(String itemId,String targetSlot,int playerLevel){
        SQLiteDatabase db=getWritableDatabase();
        String category=null;int requiredLevel=Integer.MAX_VALUE;
        try(Cursor c=db.rawQuery("SELECT slot,item_level FROM items WHERE id=?",new String[]{itemId})){if(c.moveToFirst()){category=c.isNull(0)?null:c.getString(0);requiredLevel=Math.max(1,c.getInt(1));}}
        if(category==null || !category.equals(categoryForTarget(targetSlot)) || Math.max(1,playerLevel)<requiredLevel)return false;
        GameState current=loadState();
        db.beginTransaction();
        try{
            ContentValues clearTarget=new ContentValues();clearTarget.put("equipped",0);clearTarget.putNull("equipped_slot");db.update("items",clearTarget,"equipped_slot=?",new String[]{targetSlot});
            ContentValues clearItem=new ContentValues();clearItem.put("equipped",0);clearItem.putNull("equipped_slot");db.update("items",clearItem,"id=?",new String[]{itemId});
            ContentValues on=new ContentValues();on.put("equipped",1);on.put("equipped_slot",targetSlot);db.update("items",on,"id=?",new String[]{itemId});
            syncEquipmentCapacity(current,equipmentStats());ContentValues saved=new ContentValues();saved.put("json",current.toJson().toString());db.update("state",saved,"id=1",null);
            db.setTransactionSuccessful();return true;
        }catch(Exception ignored){return false;}finally{db.endTransaction();}
    }

    public boolean unequipSlot(String targetSlot){
        SQLiteDatabase database=getWritableDatabase();GameState current=loadState();database.beginTransaction();try{ContentValues v=new ContentValues();v.put("equipped",0);v.putNull("equipped_slot");int changed=database.update("items",v,"equipped_slot=?",new String[]{targetSlot});if(changed<=0)return false;syncEquipmentCapacity(current,equipmentStats());ContentValues saved=new ContentValues();saved.put("json",current.toJson().toString());database.update("state",saved,"id=1",null);database.setTransactionSuccessful();return true;}catch(Exception ignored){return false;}finally{database.endTransaction();}
    }

    public boolean toggleEquip(String itemId) {
        return toggleEquip(itemId,loadState().level);
    }

    public boolean toggleEquip(String itemId,int playerLevel) {
        Item found=null;for(Item i:getItems())if(i.id.equals(itemId)){found=i;break;}
        if(found==null||found.slot==null)return false;
        if(found.equipped&&found.equippedSlot!=null)return unequipSlot(found.equippedSlot);
        String target=firstFreeTarget(found.slot);
        return target!=null&&equipToSlot(itemId,target,playerLevel);
    }

    public String equipRequirement(Item item,int playerLevel){
        if(item==null||item.slot==null)return"Šio daikto negalima įrengti";
        if(Math.max(1,playerLevel)<Math.max(1,item.itemLevel))return"Reikia "+item.itemLevel+" veikėjo lygio";
        return"";
    }

    private boolean syncEquipmentCapacity(GameState state,EquipmentRules.Stats equipment){int hpDelta=equipment.maxHp-state.equipmentHpBonus,manaDelta=equipment.maxMana-state.equipmentManaBonus;if(hpDelta==0&&manaDelta==0)return false;state.hpMax=Math.max(1,state.hpMax+hpDelta);state.hp=Math.max(0,Math.min(state.hpMax,state.hp));state.manaMax=Math.max(0,state.manaMax+manaDelta);state.mana=Math.max(0,Math.min(state.manaMax,state.mana));state.equipmentHpBonus=equipment.maxHp;state.equipmentManaBonus=equipment.maxMana;return true;}

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
        StringBuilder b=new StringBuilder();List<Item> equipped=new ArrayList<>();
        for(String target:EQUIPMENT_SLOTS){Item i=getEquippedAt(target);if(i!=null){equipped.add(i);if(b.length()>0)b.append("; ");b.append(slotLabel(target)).append(": ").append(i.name).append(" [L").append(i.itemLevel).append(" · ").append(EquipmentRules.itemMechanic(i)).append(']');}}
        EquipmentRules.Stats rules=EquipmentRules.calculate(equipped);
        int modifier=Math.min(25,rules.checkBonus+Math.max(0,(rules.attack+rules.defense+rules.magicPower)/100));
        if(b.length()>0)b.append("; ");b.append("Bendras įrangos modifikatorius +").append(modifier);
        b.append("; Mechanika: ").append(rules.compact());
        for(ItemCatalogV092.ActiveSetBonus bonus:ItemCatalogV092.activeSetBonuses(equipped))b.append("; Setas ").append(bonus.set.name).append(" ").append(bonus.pieces).append("/6: ").append(bonus.text);
        return b.toString();
    }

    public EquipmentRules.Stats equipmentStats(){return EquipmentRules.calculate(getItems());}

    public WorldRepository world(){if(worldRepository==null)worldRepository=new WorldRepository(this);return worldRepository;}

    public String addLoot(String name,String category,String rarity,String description){
        ItemCatalogV092.ItemDef known=ItemCatalogV092.find(name);
        if(known!=null)return addCatalogLoot(known,1);
        if(!isAllowedCategory(category))category="artifact";
        if(!isAllowedRarity(rarity))rarity="common";
        String id=UUID.randomUUID().toString();
        ContentValues v=new ContentValues();v.put("id",id);v.put("name",name);v.put("type","generated_loot");v.put("rarity",rarity.toLowerCase(Locale.ROOT));v.put("description",description);String slot=equipmentSlotForCategory(category);if(slot==null)v.putNull("slot");else v.put("slot",slot);v.put("equipped",0);v.put("synced",0);v.putNull("equipped_slot");v.put("item_level",1);v.put("power",0);v.put("quantity",1);v.put("value",0);v.put("effect",description==null?"":description);
        getWritableDatabase().insert("items",null,v);return id;
    }

    public String addCatalogLoot(ItemCatalogV092.ItemDef item,int amount){
        if(item==null)return"";int quantity=Math.max(1,amount);SQLiteDatabase db=getWritableDatabase();
        if(item.stackable){
            try(Cursor c=db.rawQuery("SELECT id,quantity FROM items WHERE catalog_id=? AND equipped=0 LIMIT 1",new String[]{item.id})){
                if(c.moveToFirst()){String id=c.getString(0);ContentValues update=new ContentValues();update.put("quantity",c.getInt(1)+quantity);db.update("items",update,"id=?",new String[]{id});return id;}
            }
        }
        String id=UUID.randomUUID().toString();ContentValues values=catalogValues(item,quantity);values.put("id",id);db.insertOrThrow("items",null,values);return id;
    }

    private ContentValues catalogValues(ItemCatalogV092.ItemDef item,int quantity){
        ContentValues v=new ContentValues();v.put("name",item.name);v.put("type",item.subtype);v.put("rarity",item.rarity);v.put("description",item.description);
        if(item.slot==null)v.putNull("slot");else v.put("slot",item.slot);v.put("equipped",0);v.put("synced",0);v.putNull("equipped_slot");
        v.put("catalog_id",item.id);v.put("item_level",item.level);v.put("power",item.power);if(item.setId==null||item.setId.isEmpty())v.putNull("set_id");else v.put("set_id",item.setId);
        v.put("quantity",Math.max(1,quantity));v.put("value",item.value);v.put("effect",item.effect);return v;
    }

    public String consumeItem(String itemId,GameState original){
        Item owned=getItem(itemId);
        if(owned==null)return "Daikto inventoriuje nebėra";
        ItemCatalogV092.ItemDef item=owned.catalogId==null?ItemCatalogV092.find(owned.name):ItemCatalogV092.byId(owned.catalogId);
        if(item==null||!item.consumable)return "Šio daikto negalima sunaudoti";
        if(original.level<item.level)return "Negalima naudoti · reikia "+item.level+" veikėjo lygio";
        ConsumableRulesV110.Effect use=ConsumableRulesV110.effect(item);
        if(use.requiresCombat&&!original.combatActive)return "Šį kovos reikmenį galima naudoti tik aktyvioje kovoje";
        if(!use.usableInCombat&&original.combatActive)return "Maistą galima vartoti tik ne kovos metu";
        if(!ConsumableRulesV110.useful(use,original))return "Daiktas nepanaudotas · jo poveikis dabar nieko nepakeistų";
        SQLiteDatabase database=getWritableDatabase();
        try{
            GameState state=GameState.fromJson(original.toJson());
            boolean wasCombat=original.combatActive;
            ArrayList<String> drops=new ArrayList<>();
            database.beginTransaction();
            try{
                checkpoint("prieš daikto naudojimą",original);
                state.hp=Math.min(state.hpMax,state.hp+use.hp);state.mana=Math.min(state.manaMax,state.mana+use.mana);
                state.stamina=Math.min(state.staminaMax,state.stamina+use.stamina);state.aeonic=Math.min(state.aeonicMax,state.aeonic+use.aeonic);
                if(use.cleanse)state.playerCombatStatus="";
                state.applyTemporaryEffect(use);
                if(state.combatActive){
                    String enemy=state.enemyName;int danger=state.enemyDanger;
                    JSONObject result=CombatEngine.useItem(state,item,equipmentStats(),getStatValues(),world().unlockedTalentIds(),
                            world().companionDefenseBonus(state.combatRound),world().companionVictoryHealing());
                    state.applyTurn(result);
                    String event=result.optString("event_tag");
                    if(CombatEngine.isVictory(true,result))for(ItemCatalogV092.ItemDef drop:DropTableV092.roll(enemy,state.worldMinute+item.id.hashCode())){
                        addCatalogLoot(drop,1);drops.add(drop.name);
                    }
                    ProgressionEngine.award(state,event,null,danger);
                    world().recordCompanionTurn(item.name,event,state);
                    sideQuests().record(item.name,event,state,null);
                    world().advanceWorld(state,event,result.optInt("time_minutes"));
                    world().applyQuestToState(state);world().applyStructuredChoices(state);
                    state.tickTemporaryEffect();
                }
                if(owned.quantity>1){ContentValues quantity=new ContentValues();quantity.put("quantity",owned.quantity-1);
                    if(database.update("items",quantity,"id=?",new String[]{owned.id})!=1)throw new IllegalStateException("Missing item");
                }else if(database.delete("items","id=?",new String[]{owned.id})!=1)throw new IllegalStateException("Missing item");
                saveState(state);database.setTransactionSuccessful();
            }finally{database.endTransaction();}
            original.copyFrom(state);
            StringBuilder message=new StringBuilder("Panaudota: ").append(item.name);
            if(wasCombat)message.append(" · ").append(state.sceneTitle);
            if(use.hasBuff())message.append(" · poveikis liko ").append(state.temporaryEffectTurns).append(" ėj.");
            if(!drops.isEmpty())message.append(" · grobis: ").append(String.join(", ",drops));
            return message.toString();
        }catch(Exception error){return "Daikto panaudoti nepavyko. Daiktas ir ankstesnė pažanga išliko.";}
    }

    private String equipmentSlotForCategory(String category){return java.util.Arrays.asList("weapon","offhand","head","chest","hands","legs","feet","belt","neck","ring","utility","relic").contains(category)?category:null;}
    private boolean isAllowedCategory(String c){return c!=null&&java.util.Arrays.asList(ItemCatalogV092.CATEGORIES).contains(c);}
    private boolean isAllowedRarity(String r){return r!=null&&java.util.Arrays.asList(ItemCatalogV092.RARITIES).contains(r.toLowerCase(Locale.ROOT));}

    public long checkpoint(String label, GameState state) {
        try {
            JSONArray eq = new JSONArray();
            for(Item i:getItems()) if(i.equipped) {JSONObject o=new JSONObject();o.put("id",i.id);o.put("slot",i.equippedSlot);eq.put(o);}
            ContentValues v=new ContentValues(); v.put("label",label);v.put("state_json",state.toJson().toString());v.put("equipment_json",eq.toString());v.put("snapshot_json",exportSave());v.put("created_at",System.currentTimeMillis());
            SQLiteDatabase db=getWritableDatabase(); long id=db.insertOrThrow("checkpoints",null,v);
            db.execSQL("DELETE FROM checkpoints WHERE id NOT IN (SELECT id FROM checkpoints ORDER BY id DESC LIMIT 20)");
            return id;
        } catch(Exception error){throw new IllegalStateException("Kontrolinio taško išsaugoti nepavyko.",error);}
    }

    void discardCheckpoint(long id){getWritableDatabase().delete("checkpoints","id=?",new String[]{String.valueOf(id)});}

    public boolean undo() {
        SQLiteDatabase db=getWritableDatabase();
        try(Cursor c=db.rawQuery("SELECT id,state_json,equipment_json,snapshot_json FROM checkpoints ORDER BY id DESC LIMIT 1",null)){
            if(!c.moveToFirst()) return false;
            long id=c.getLong(0); String state=c.getString(1); JSONArray eq=new JSONArray(c.getString(2));String snapshot=c.isNull(3)?null:c.getString(3);
            db.beginTransaction();
            try {
                if(snapshot!=null&&!snapshot.isEmpty())restoreCore(db,new JSONObject(snapshot));
                else restoreLegacyCheckpoint(db,state,eq);
                db.delete("checkpoints","id=?",new String[]{String.valueOf(id)});
                db.setTransactionSuccessful(); return true;
            } finally { db.endTransaction(); }
        } catch(Exception e){ return false; }
    }

    public List<SaveSlot> saveSlots(){
        ArrayList<SaveSlot> result=new ArrayList<>();try(Cursor c=getReadableDatabase().rawQuery("SELECT slot,name,updated_at,location,level FROM save_slots ORDER BY slot",null)){while(c.moveToNext()){SaveSlot slot=new SaveSlot();slot.slot=c.getInt(0);slot.name=c.getString(1);slot.updatedAt=c.getLong(2);slot.location=c.getString(3);slot.level=c.getInt(4);result.add(slot);}}return result;
    }

    public boolean saveToSlot(int slot,String name){
        if(slot<1||slot>3)return false;String snapshot=exportSave();if(snapshot.isEmpty())return false;GameState current=loadState();ContentValues values=new ContentValues();values.put("slot",slot);values.put("name",name==null||name.trim().isEmpty()?"Kelionė "+slot:name.trim());values.put("snapshot_json",snapshot);values.put("updated_at",System.currentTimeMillis());values.put("location",current.location);values.put("level",current.level);return getWritableDatabase().insertWithOnConflict("save_slots",null,values,SQLiteDatabase.CONFLICT_REPLACE)!=-1;
    }

    public boolean loadFromSlot(int slot){
        String snapshot=null;try(Cursor c=getReadableDatabase().rawQuery("SELECT snapshot_json FROM save_slots WHERE slot=?",new String[]{String.valueOf(slot)})){if(c.moveToFirst())snapshot=c.getString(0);}if(snapshot==null)return false;
        checkpoint("prieš lizdo atkūrimą",loadState());SQLiteDatabase database=getWritableDatabase();database.beginTransaction();try{restoreCore(database,new JSONObject(snapshot));database.setTransactionSuccessful();return true;}catch(Exception ignored){return false;}finally{database.endTransaction();}
    }

    public boolean deleteSlot(int slot){return getWritableDatabase().delete("save_slots","slot=?",new String[]{String.valueOf(slot)})>0;}

    public String exportSave() {
        try {
            JSONObject root=new JSONObject(); root.put("version",VERSION); root.put("state",loadState().toJson());
            JSONArray items=new JSONArray(); for(Item i:getItems()){JSONObject o=new JSONObject();o.put("id",i.id);o.put("name",i.name);o.put("type",i.type);o.put("category",i.slot==null?"artifact":i.slot);o.put("rarity",i.rarity);o.put("description",i.description);o.put("equipped_slot",i.equippedSlot==null?JSONObject.NULL:i.equippedSlot);o.put("synced",i.synced);o.put("catalog_id",i.catalogId==null?JSONObject.NULL:i.catalogId);o.put("item_level",i.itemLevel);o.put("power",i.power);o.put("set_id",i.setId==null?JSONObject.NULL:i.setId);o.put("quantity",i.quantity);o.put("value",i.value);o.put("effect",i.effect);items.put(o);} root.put("items",items);JSONArray abilities=new JSONArray();for(String[] ability:getAbilities()){JSONObject entry=new JSONObject();entry.put("name",ability[0]);entry.put("type",ability[1]);entry.put("description",ability[2]);abilities.put(entry);}root.put("abilities",abilities); JSONArray stats=new JSONArray();for(java.util.Map.Entry<String,Integer> e:getStatValues().entrySet()){JSONObject so=new JSONObject();so.put("name",e.getKey());so.put("value",e.getValue());stats.put(so);}root.put("stats",stats); JSONArray mastery=new JSONArray();for(java.util.Map.Entry<String,Mastery> e:getMasteries().entrySet()){JSONObject mo=new JSONObject();mo.put("name",e.getKey());mo.put("level",e.getValue().level);mo.put("xp",e.getValue().xp);mo.put("next_xp",e.getValue().nextXp);mastery.put(mo);}root.put("mastery",mastery);root.put("world",world().exportState()); return root.toString();
        } catch(Exception e){return "";}
    }

    public boolean importSave(String raw) {
        try {
            if(raw==null||raw.length()>5_000_000)return false;
            JSONObject root=new JSONObject(raw);int version=root.optInt("version",0);if(version<1||version>VERSION)return false;GameState.fromJson(root.getJSONObject("state"));
            SQLiteDatabase db=getWritableDatabase(); db.beginTransaction();
            try {
                restoreCore(db,root);
                db.setTransactionSuccessful();return true;
            } finally { db.endTransaction(); }
        } catch(Exception e){return false;}
    }

    private void restoreCore(SQLiteDatabase db,JSONObject root)throws Exception{
        GameState restored=GameState.fromJson(root.getJSONObject("state"));ContentValues st=new ContentValues();st.put("json",restored.toJson().toString());db.update("state",st,"id=1",null);
        JSONArray items=root.optJSONArray("items");
        if(items!=null){
            if(items.length()>5000)throw new IllegalArgumentException("Per daug daiktų");
            db.delete("items",null,null);java.util.HashSet<String> usedTargets=new java.util.HashSet<>();
            for(int i=0;i<items.length();i++){
                JSONObject o=items.optJSONObject(i);if(o==null)continue;
                String id=compactImported(o.optString("id",""),128);if(id.isEmpty())continue;
                int quantity=Math.max(1,Math.min(999,o.optInt("quantity",1)));
                String catalogId=o.isNull("catalog_id")?null:o.optString("catalog_id",null);
                ItemCatalogV092.ItemDef known=ItemCatalogV092.byId(catalogId);
                ContentValues add;String category;
                if(known!=null){
                    add=catalogValues(known,quantity);category=known.slot;
                }else{
                    add=new ContentValues();category=equipmentSlotForCategory(o.optString("category","artifact"));
                    add.put("name",compactImported(o.optString("name","Nežinomas daiktas"),80));
                    add.put("type",compactImported(o.optString("type","imported_artifact"),60));
                    String rarity=o.optString("rarity","common").toLowerCase(Locale.ROOT);add.put("rarity",isAllowedRarity(rarity)?rarity:"common");
                    add.put("description",compactImported(o.optString("description",""),1000));
                    if(category==null)add.putNull("slot");else add.put("slot",category);
                    add.putNull("catalog_id");add.put("item_level",1);add.put("power",0);add.putNull("set_id");
                    add.put("quantity",quantity);add.put("value",0);add.put("effect","");
                }
                String target=o.isNull("equipped_slot")?null:o.optString("equipped_slot",null);
                boolean equipped=target!=null&&validEquipmentTarget(target)&&category!=null
                        &&category.equals(categoryForTarget(target))&&(known==null||known.level<=restored.level)&&usedTargets.add(target);
                add.put("id",id);add.put("equipped",equipped?1:0);add.put("synced",o.optBoolean("synced",false)?1:0);
                if(equipped)add.put("equipped_slot",target);else add.putNull("equipped_slot");
                db.insertOrThrow("items",null,add);
            }
        }
        JSONArray stats=root.optJSONArray("stats");if(stats!=null){if(stats.length()>500)throw new IllegalArgumentException("Per daug savybių");db.delete("stats",null,null);seedStats(db);for(int i=0;i<stats.length();i++){JSONObject o=stats.optJSONObject(i);if(o==null)continue;String name=o.optString("name","");if(!knownStat(name))continue;ContentValues value=new ContentValues();value.put("value",Math.max(0,Math.min(100,o.optInt("value",45))));db.update("stats",value,"name=?",new String[]{name});}}
        JSONArray abilities=root.optJSONArray("abilities");if(abilities!=null){if(abilities.length()>100)throw new IllegalArgumentException("Per daug gebėjimų");db.delete("abilities",null,null);for(int i=0;i<abilities.length();i++){JSONObject o=abilities.optJSONObject(i);if(o==null)continue;String name=compactImported(o.optString("name",""),80);if(!allowedAbilityName(name))continue;ContentValues value=new ContentValues();value.put("name",name);value.put("type",compactImported(o.optString("type","bazinis_gebėjimas"),60));value.put("description",compactImported(o.optString("description",""),1000));db.insertWithOnConflict("abilities",null,value,SQLiteDatabase.CONFLICT_IGNORE);}}
        JSONArray mastery=root.optJSONArray("mastery");if(mastery!=null){if(mastery.length()>500)throw new IllegalArgumentException("Per daug meistriškumo įrašų");db.delete("mastery",null,null);seedMastery(db);for(int i=0;i<mastery.length();i++){JSONObject o=mastery.optJSONObject(i);if(o==null)continue;String name=o.optString("name","");if(!knownStat(name))continue;int level=Math.max(0,Math.min(100,o.optInt("level",0)));int next=masteryNext(level);ContentValues value=new ContentValues();value.put("level",level);value.put("xp",Math.max(0,Math.min(next-1,o.optInt("xp",0))));value.put("next_xp",level>=100?1:next);db.update("mastery",value,"name=?",new String[]{name});}}
        world().restoreState(db,root.optJSONObject("world"));
        if(root.optInt("version",1)<12)migrateV11toV12(db);
        if(root.optInt("version",1)<13)migrateV12toV13(db);
        if(root.optInt("version",1)<14){WorldRepository.seed(db,restored);SideQuestRepository.seed(db);}
    }

    private boolean validEquipmentTarget(String target){if(target==null)return false;for(String slot:EQUIPMENT_SLOTS)if(slot.equals(target))return true;return false;}
    private boolean knownStat(String name){for(BaseStatCatalog.Group group:BaseStatCatalog.GROUPS)for(String stat:group.stats)if(stat.equals(name))return true;return false;}
    private boolean allowedAbilityName(String name){
        if("Archetipinis pasirengimas".equals(name)||"Lauko improvizacija".equals(name))return true;
        for(String allowed:new String[]{"Eoninis Bastionas","Tęstinumo Gardelė","Katastrofinė Regeneracija","Nuliui Prisitaikanti Fiziologija","Refleksinis Erdvinis Išsisukimas","Prisitaikantis Kontrapynimas","Relikvijų Simbiozė","Laiko Paralakso Atskyrimas","Nežinomų Taisyklių Kalibravimas","Pasidalytas Meistriškumas","Sąlyginio Priežastingumo Struktūra","Santarvės Adapterio Struktūra"})if(allowed.equals(name))return true;
        return false;
    }
    private String compactImported(String value,int max){String clean=value==null?"":value.trim().replaceAll("\\s+"," ");return clean.length()<=max?clean:clean.substring(0,max);}

    private void restoreLegacyCheckpoint(SQLiteDatabase db,String state,JSONArray equipment)throws Exception{
        ContentValues st=new ContentValues();st.put("json",state);db.update("state",st,"id=1",null);ContentValues off=new ContentValues();off.put("equipped",0);off.putNull("equipped_slot");db.update("items",off,null,null);for(int i=0;i<equipment.length();i++){Object raw=equipment.get(i);String itemId;String target;if(raw instanceof JSONObject){JSONObject o=(JSONObject)raw;itemId=o.getString("id");target=o.optString("slot",null);}else{itemId=String.valueOf(raw);target=null;}if(target!=null&&!target.isEmpty()){ContentValues on=new ContentValues();on.put("equipped",1);on.put("equipped_slot",target);db.update("items",on,"id=?",new String[]{itemId});}}
    }

    private String groupForStat(String stat){for(BaseStatCatalog.Group group:BaseStatCatalog.GROUPS)for(String candidate:group.stats)if(candidate.equals(stat))return group.name;return"KITA";}

    public void reset() {
        SQLiteDatabase db=getWritableDatabase();WorldRepository.drop(db);db.execSQL("DROP TABLE IF EXISTS state");db.execSQL("DROP TABLE IF EXISTS items");db.execSQL("DROP TABLE IF EXISTS abilities");db.execSQL("DROP TABLE IF EXISTS checkpoints");db.execSQL("DROP TABLE IF EXISTS stats");db.execSQL("DROP TABLE IF EXISTS mastery");db.execSQL("DROP TABLE IF EXISTS turns");db.execSQL("DROP TABLE IF EXISTS undo");worldRepository=null;onCreate(db);
    }

    public static String slotLabel(String s){
        if(s==null)return"";
        switch(s){
            case"weapon":return"Pagrindinis ginklas";case"offhand":return"Antrinis ginklas / skydas";case"head":return"Šalmas";case"chest":return"Krūtinės šarvai";case"hands":return"Pirštinės";case"legs":return"Kelnės";case"feet":return"Batai";case"belt":return"Diržas";case"neck":return"Pakabukas";case"ring_left":return"Kairysis žiedas";case"ring_right":return"Dešinysis žiedas";case"utility":return"Naudingasis daiktas";case"relic_1":return"Relikvija I";case"relic_2":return"Relikvija II";case"relic_3":return"Relikvija III";case"relic_4":return"Relikvija IV";default:return s;
        }
    }
}
