package lt.ooc.vaeloria;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONObject;

public final class GameStore extends SQLiteOpenHelper {
    public GameStore(Context c){ super(c,"vaeloria.db",null,1); }
    @Override public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE game_state(id INTEGER PRIMARY KEY CHECK(id=1), state_json TEXT NOT NULL, updated_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE checkpoints(id INTEGER PRIMARY KEY AUTOINCREMENT, state_json TEXT NOT NULL, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE turns(id INTEGER PRIMARY KEY AUTOINCREMENT, action TEXT NOT NULL, result_json TEXT NOT NULL, world_minute INTEGER, created_at INTEGER NOT NULL)");
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldV,int newV){}

    public synchronized JSONObject load(){
        SQLiteDatabase db=getWritableDatabase();
        try(Cursor c=db.rawQuery("SELECT state_json FROM game_state WHERE id=1",null)){
            if(c.moveToFirst()) return new JSONObject(c.getString(0));
        } catch(Exception ignored){}
        JSONObject seed=SeedState.create(); save(seed); return seed;
    }

    public synchronized void save(JSONObject state){
        ContentValues v=new ContentValues(); v.put("id",1); v.put("state_json",state.toString()); v.put("updated_at",System.currentTimeMillis());
        getWritableDatabase().insertWithOnConflict("game_state",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }

    public synchronized void checkpoint(JSONObject state){
        ContentValues v=new ContentValues(); v.put("state_json",state.toString()); v.put("created_at",System.currentTimeMillis());
        SQLiteDatabase db=getWritableDatabase(); db.insert("checkpoints",null,v);
        db.execSQL("DELETE FROM checkpoints WHERE id NOT IN (SELECT id FROM checkpoints ORDER BY id DESC LIMIT 20)");
    }

    public synchronized JSONObject undo(){
        SQLiteDatabase db=getWritableDatabase();
        try(Cursor c=db.rawQuery("SELECT id,state_json FROM checkpoints ORDER BY id DESC LIMIT 1",null)){
            if(!c.moveToFirst()) return null;
            long id=c.getLong(0); JSONObject s=new JSONObject(c.getString(1));
            db.delete("checkpoints","id=?",new String[]{String.valueOf(id)}); save(s); return s;
        } catch(Exception e){ return null; }
    }

    public synchronized void logTurn(String action, JSONObject result, long worldMinute){
        ContentValues v=new ContentValues(); v.put("action",action); v.put("result_json",result.toString()); v.put("world_minute",worldMinute); v.put("created_at",System.currentTimeMillis());
        getWritableDatabase().insert("turns",null,v);
    }

    public synchronized JSONObject reset(){
        SQLiteDatabase db=getWritableDatabase(); db.delete("checkpoints",null,null); db.delete("turns",null,null);
        JSONObject s=SeedState.create(); save(s); return s;
    }

    public synchronized void importState(JSONObject s){ checkpoint(load()); save(s); }
}
