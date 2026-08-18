package lt.vaeloria.ooc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class V083DatabaseMigrationDeviceTest {
    private Context context;

    @Before public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("vaeloria.db");
    }

    @After public void tearDown() {
        context.deleteDatabase("vaeloria.db");
    }

    @Test public void realVersionThreeDatabaseUpgradesInPlaceToVersionFive() throws Exception {
        File path = context.getDatabasePath("vaeloria.db");
        File parent = path.getParentFile();
        assertNotNull(parent);
        assertTrue(parent.exists() || parent.mkdirs());

        SQLiteDatabase legacy = SQLiteDatabase.openOrCreateDatabase(path, null);
        legacy.execSQL("CREATE TABLE state (id INTEGER PRIMARY KEY CHECK(id=1), json TEXT NOT NULL)");
        legacy.execSQL("CREATE TABLE items (id TEXT PRIMARY KEY, name TEXT NOT NULL, type TEXT NOT NULL, rarity TEXT NOT NULL, description TEXT NOT NULL, slot TEXT, equipped INTEGER NOT NULL DEFAULT 0, synced INTEGER NOT NULL DEFAULT 0, equipped_slot TEXT)");
        legacy.execSQL("CREATE TABLE abilities (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL, type TEXT NOT NULL, description TEXT NOT NULL)");
        legacy.execSQL("CREATE TABLE checkpoints (id INTEGER PRIMARY KEY AUTOINCREMENT, label TEXT, state_json TEXT NOT NULL, equipment_json TEXT NOT NULL, created_at INTEGER NOT NULL)");

        GameState oldState = new GameState();
        oldState.location = "Veyrhold";
        oldState.hp = 73;
        oldState.crowns = 12345;
        ContentValues state = new ContentValues();
        state.put("id", 1);
        state.put("json", oldState.toJson().toString());
        legacy.insertOrThrow("state", null, state);

        ContentValues item = new ContentValues();
        item.put("id", "legacy-v083-item");
        item.put("name", "Senasis žiedas");
        item.put("type", "major_relic");
        item.put("rarity", "legendary");
        item.put("description", "v0.8.3 išsaugojimo daiktas");
        item.put("slot", "ring");
        item.put("equipped", 1);
        item.put("synced", 1);
        item.put("equipped_slot", "ring_left");
        legacy.insertOrThrow("items", null, item);
        legacy.setVersion(3);
        legacy.close();

        VaeloriaDb upgraded = new VaeloriaDb(context);
        SQLiteDatabase database = upgraded.getWritableDatabase();
        assertEquals(5, database.getVersion());
        GameState restored = upgraded.loadState();
        assertEquals("Veyrhold", restored.location);
        assertEquals(73, restored.hp);
        assertEquals(12345L, restored.crowns);
        assertFalse(upgraded.getStatValues().isEmpty());
        assertFalse(upgraded.getMasteryLevels().isEmpty());
        assertEquals("Senasis žiedas", upgraded.getEquippedAt("ring_left").name);
        assertTrue(tableExists(database, "stats"));
        assertTrue(tableExists(database, "mastery"));
        upgraded.close();
    }

    private boolean tableExists(SQLiteDatabase database, String name) {
        try (Cursor cursor = database.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name=?", new String[]{name})) {
            return cursor.moveToFirst();
        }
    }
}
