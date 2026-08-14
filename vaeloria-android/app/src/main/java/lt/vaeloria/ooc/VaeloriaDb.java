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

public class VaeloriaDb extends SQLiteOpenHelper {
    private static final String DB = "vaeloria.db";
    private static final int VERSION = 2;

    public static class Item {
        public String id, name, type, rarity, description, slot;
        public boolean equipped, synced;
    }

    public VaeloriaDb(Context c) { super(c, DB, null, VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE state (id INTEGER PRIMARY KEY CHECK(id=1), json TEXT NOT NULL)");
        db.execSQL("CREATE TABLE items (id TEXT PRIMARY KEY, name TEXT NOT NULL, type TEXT NOT NULL, rarity TEXT NOT NULL, description TEXT NOT NULL, slot TEXT, equipped INTEGER NOT NULL DEFAULT 0, synced INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE abilities (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL, type TEXT NOT NULL, description TEXT NOT NULL)");
        db.execSQL("CREATE TABLE checkpoints (id INTEGER PRIMARY KEY AUTOINCREMENT, label TEXT, state_json TEXT NOT NULL, equipment_json TEXT NOT NULL, created_at INTEGER NOT NULL)");
        seed(db);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) migrateV1(db);
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

    private static int parseInt(String s, int fallback) { try { return Integer.parseInt(s); } catch (Exception e) { return fallback; } }
    private static long parseLong(String s, long fallback) { try { return Long.parseLong(s); } catch (Exception e) { return fallback; } }
    private static String compactLegacy(String s) { if (s == null) return "legacy turn"; s=s.replace('\n',' '); return s.length()>90?s.substring(0,90)+"…":s; }

    private void seed(SQLiteDatabase db) {
        try {
            GameState s = new GameState();
            ContentValues st = new ContentValues();
            st.put("id", 1); st.put("json", s.toJson().toString());
            db.insert("state", null, st);
        } catch (JSONException ignored) {}

        addItem(db,"6deb2206-413c-4ef4-bc75-876edeee91c2","Asterion Edge","weapon","legendary","Einoras primary synchronized weapon","primary_weapon",true,true);
        addItem(db,"4d04d2de-11f3-4e14-8dd2-295224ee998e","Sevenfold Mantle","armor_system","legendary","Einoras synchronized layered armor system","armor_system",true,true);
        addItem(db,"66d2a35a-a00c-4566-bb34-0819af559e32","Wayfold Satchel","utility_item","rare","Spatial utility satchel","utility",true,false);
        addItem(db,"3b1de6bc-4198-4e4a-9d0d-631891c644e5","Resonance Signet","major_relic","legendary","Resonance-focused major relic","relic",true,true);
        addItem(db,"0ea61c33-cbe9-4417-9b2b-6575baf1e38d","Nullglass Prism","major_relic","legendary","Null interaction and analysis relic","relic",true,true);
        addItem(db,"327f8e01-866c-44f1-aa10-189cd03f9155","Meridian Key","major_relic","legendary","Meridian access and boundary relic","relic",true,true);
        addItem(db,"50dfd168-9b38-4c10-a88b-6f71b1920e9d","Dragonwake Concord Scale","major_relic","legendary","Draconic concord relic","relic",true,true);
        addItem(db,"fe12ec4f-acea-4c51-b351-0d7449ec7ab8","Heart of Still Thunder","artifact","ancient","Stored campaign artifact",null,false,false);
        addItem(db,"23639e76-8c70-4df8-97b8-02272314506a","Living Rune Seed","artifact","ancient","Living rune artifact",null,false,false);
        addItem(db,"ca47899c-451b-4e6e-a635-ab45a4c872c3","Starfall Compass","artifact","ancient","Starfall navigation artifact",null,false,false);
        addItem(db,"18304996-9186-41d5-9421-7b2a20e31df2","Orison Astrolabe","artifact","ancient","Orison navigation and observation artifact",null,false,false);
        addItem(db,"fb6dd5fd-8324-4bea-813c-8659bcdd531b","Arkforge Seed","anchored_artifact","ancient","Anchored to the Axiom Crucible architecture",null,false,false);
        addItem(db,"40e8b726-c711-4f8a-9900-70b32efc9819","Triune Concordance Seal","credential","unique","Noncombat steward credential issued to each Concordance quorum member",null,false,false);

        addAbility(db,"Aeonic Bastion","post_cap_ability","Autonomous layered physical, elemental, arcane, spatial and hostile-transmutation defenses.");
        addAbility(db,"Continuity Lattice","post_cap_ability","Identity and neural continuity anchors permit recovery from otherwise fatal localized destruction if a coherent anchor survives.");
        addAbility(db,"Catastrophic Regeneration","post_cap_ability","Rebuilds extreme trauma from surviving structure, mana and time; not instant.");
        addAbility(db,"Null-Adaptive Physiology","post_cap_ability","Legendary physical body remains functional in anti-magic while magical layers are suppressed.");
        addAbility(db,"Reflexive Spatial Evasion","post_cap_ability","Automatic displacement, vector redirection and partial shunting under threat.");
        addAbility(db,"Adaptive Counterweaving","post_cap_ability","Defenses adapt after exposure to hostile principles.");
        addAbility(db,"Relic Symbiosis","post_cap_ability","Coordinates synchronized relics subject to resonance bandwidth.");
        addAbility(db,"Temporal Parallax Discrimination","post_cap_refinement","Distinguishes local continuity from cross-branch timing echoes after observation.");
        addAbility(db,"Unknown-Rule Calibration","post_cap_refinement","Forms safer provisional models faster after first contact with unfamiliar rules.");
        addAbility(db,"Decentered Mastery","post_cap_refinement","Permits competent peers to override Einoras decisions without reducing coordination.");
        addAbility(db,"Conditional Causality Framing","post_cap_principle","Restructures some magic into anchored condition-to-consequence bindings.");
        addAbility(db,"Concordance Adapter Framing","post_cap_technique","Designs provisional adapters that translate understood causal forms into sandbox-compatible representations.");
    }

    private void addItem(SQLiteDatabase db,String id,String name,String type,String rarity,String desc,String slot,boolean eq,boolean synced){
        ContentValues v=new ContentValues(); v.put("id",id);v.put("name",name);v.put("type",type);v.put("rarity",rarity);v.put("description",desc);v.put("slot",slot);v.put("equipped",eq?1:0);v.put("synced",synced?1:0); db.insert("items",null,v);
    }
    private void addAbility(SQLiteDatabase db,String name,String type,String desc){
        ContentValues v=new ContentValues(); v.put("name",name);v.put("type",type);v.put("description",desc); db.insert("abilities",null,v);
    }

    public GameState loadState() {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT json FROM state WHERE id=1", null)) {
            if (c.moveToFirst()) return GameState.fromJson(new JSONObject(c.getString(0)));
        } catch (Exception ignored) {}
        return new GameState();
    }

    public void saveState(GameState s) {
        try {
            ContentValues v = new ContentValues(); v.put("json", s.toJson().toString());
            getWritableDatabase().update("state", v, "id=1", null);
        } catch (JSONException ignored) {}
    }

    public List<Item> getItems() {
        List<Item> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,type,rarity,description,slot,equipped,synced FROM items ORDER BY equipped DESC, CASE rarity WHEN 'unique' THEN 0 WHEN 'legendary' THEN 1 WHEN 'ancient' THEN 2 WHEN 'rare' THEN 3 ELSE 4 END, name", null)) {
            while (c.moveToNext()) { Item i=new Item(); i.id=c.getString(0);i.name=c.getString(1);i.type=c.getString(2);i.rarity=c.getString(3);i.description=c.getString(4);i.slot=c.isNull(5)?null:c.getString(5);i.equipped=c.getInt(6)==1;i.synced=c.getInt(7)==1;out.add(i); }
        }
        return out;
    }

    public List<String[]> getAbilities() {
        List<String[]> out=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT name,type,description FROM abilities ORDER BY id",null)){
            while(c.moveToNext()) out.add(new String[]{c.getString(0),c.getString(1),c.getString(2)});
        }
        return out;
    }

    public boolean toggleEquip(String itemId) {
        SQLiteDatabase db = getWritableDatabase();
        String slot = null; boolean equipped = false;
        try(Cursor c=db.rawQuery("SELECT slot,equipped FROM items WHERE id=?",new String[]{itemId})){
            if(c.moveToFirst()){ slot=c.isNull(0)?null:c.getString(0); equipped=c.getInt(1)==1; }
        }
        if(slot==null) return false;
        if(!equipped && "relic".equals(slot) && equippedRelicCount()>=4) return false;
        db.beginTransaction();
        try {
            if(!equipped && !"relic".equals(slot)) {
                ContentValues off=new ContentValues(); off.put("equipped",0); db.update("items",off,"slot=?",new String[]{slot});
            }
            ContentValues v=new ContentValues();v.put("equipped",equipped?0:1);db.update("items",v,"id=?",new String[]{itemId});
            db.setTransactionSuccessful(); return true;
        } finally { db.endTransaction(); }
    }

    private int equippedRelicCount(){
        try(Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM items WHERE slot='relic' AND equipped=1",null)){ if(c.moveToFirst()) return c.getInt(0); }
        return 0;
    }

    public String equippedSummary() {
        StringBuilder b=new StringBuilder();
        for(Item i:getItems()) if(i.equipped) { if(b.length()>0)b.append(", "); b.append(i.name); }
        return b.toString();
    }

    public void checkpoint(String label, GameState state) {
        try {
            JSONArray eq = new JSONArray(); for(Item i:getItems()) if(i.equipped) eq.put(i.id);
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
                ContentValues off=new ContentValues();off.put("equipped",0);db.update("items",off,null,null);
                ContentValues on=new ContentValues();on.put("equipped",1);for(int i=0;i<eq.length();i++)db.update("items",on,"id=?",new String[]{eq.getString(i)});
                db.delete("checkpoints","id=?",new String[]{String.valueOf(id)});
                db.setTransactionSuccessful(); return true;
            } finally { db.endTransaction(); }
        } catch(Exception e){ return false; }
    }

    public String exportSave() {
        try {
            JSONObject root=new JSONObject(); root.put("version",2); root.put("state",loadState().toJson());
            JSONArray items=new JSONArray(); for(Item i:getItems()){JSONObject o=new JSONObject();o.put("id",i.id);o.put("equipped",i.equipped);items.put(o);} root.put("items",items); return root.toString();
        } catch(Exception e){return "";}
    }

    public boolean importSave(String raw) {
        try {
            JSONObject root=new JSONObject(raw); GameState state=GameState.fromJson(root.getJSONObject("state")); JSONArray items=root.optJSONArray("items");
            SQLiteDatabase db=getWritableDatabase(); db.beginTransaction();
            try {
                ContentValues st=new ContentValues();st.put("json",state.toJson().toString());db.update("state",st,"id=1",null);
                ContentValues off=new ContentValues();off.put("equipped",0);db.update("items",off,null,null);
                if(items!=null)for(int i=0;i<items.length();i++){JSONObject o=items.getJSONObject(i);if(o.optBoolean("equipped")){ContentValues on=new ContentValues();on.put("equipped",1);db.update("items",on,"id=?",new String[]{o.getString("id")});}}
                db.setTransactionSuccessful();return true;
            } finally { db.endTransaction(); }
        } catch(Exception e){return false;}
    }

    public void reset() {
        SQLiteDatabase db=getWritableDatabase(); db.execSQL("DROP TABLE IF EXISTS state");db.execSQL("DROP TABLE IF EXISTS items");db.execSQL("DROP TABLE IF EXISTS abilities");db.execSQL("DROP TABLE IF EXISTS checkpoints");db.execSQL("DROP TABLE IF EXISTS turns");db.execSQL("DROP TABLE IF EXISTS undo");onCreate(db);
    }
}
