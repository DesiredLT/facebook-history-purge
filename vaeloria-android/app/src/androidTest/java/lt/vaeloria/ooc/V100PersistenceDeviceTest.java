package lt.vaeloria.ooc;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class V100PersistenceDeviceTest {
    private Context context;
    private VaeloriaDb database;

    @Before public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("vaeloria.db");
        database = new VaeloriaDb(context);
        database.getWritableDatabase();
    }

    @After public void tearDown() {
        if (database != null) database.close();
        context.deleteDatabase("vaeloria.db");
    }

    @Test public void atomicUndoRestoresEveryMutableSubsystem() {
        GameState state = balancedState("Undo herojus");
        String stat = database.getStatValues().keySet().iterator().next();
        String mastery = database.getMasteries().keySet().iterator().next();
        int originalStat = database.getStatValue(stat);
        VaeloriaDb.Mastery originalMastery = database.getMasteries().get(mastery);
        int originalAbilities = database.getAbilities().size();
        long originalCrowns = state.crowns;
        assertFalse(database.world().isDiscovered("Žaliasis Labirintas"));

        database.checkpoint("pilnos būsenos patikra", state);
        String temporaryItem = database.addLoot("Laikinas testo radinys", "artifact", "rare", "Turi išnykti po atšaukimo.");
        database.setStatValue(stat, 1);
        database.awardMastery(mastery, 777);
        database.getWritableDatabase().delete("abilities", null, null);
        database.getWritableDatabase().execSQL("UPDATE locations SET discovered=1 WHERE name='Žaliasis Labirintas'");
        state.crowns += 9876;
        state.location = "Žaliasis Labirintas";
        database.saveState(state);

        assertTrue(database.undo());
        GameState restored = database.loadState();
        assertEquals(originalCrowns, restored.crowns);
        assertEquals("Luminara", restored.location);
        assertNull(database.getItem(temporaryItem));
        assertEquals(originalStat, database.getStatValue(stat));
        VaeloriaDb.Mastery restoredMastery = database.getMasteries().get(mastery);
        assertNotNull(restoredMastery);
        assertEquals(originalMastery.level, restoredMastery.level);
        assertEquals(originalMastery.xp, restoredMastery.xp);
        assertEquals(originalAbilities, database.getAbilities().size());
        assertFalse(database.world().isDiscovered("Žaliasis Labirintas"));
    }

    @Test public void namedSaveSlotRestoresCompleteSnapshotAndSurvivesLoading() {
        GameState state = balancedState("Lizdo herojus");
        String stat = database.getStatValues().keySet().iterator().next();
        int originalStat = database.getStatValue(stat);
        int originalAbilities = database.getAbilities().size();
        int originalUnits = units();
        state.crowns = 4321;
        database.saveState(state);

        assertTrue(database.saveToSlot(1, "Prieš Meridianą"));
        assertEquals(1, database.saveSlots().size());
        assertEquals("Prieš Meridianą", database.saveSlots().get(0).name);

        state.crowns = 2;
        state.location = "Kharad Vorn";
        database.saveState(state);
        database.addLoot("Lizdo laikinas radinys", "artifact", "common", "Neturi likti.");
        database.setStatValue(stat, 2);
        database.getWritableDatabase().delete("abilities", null, null);
        database.getWritableDatabase().execSQL("UPDATE locations SET discovered=1 WHERE name='Žaliasis Labirintas'");

        assertTrue(database.loadFromSlot(1));
        GameState restored = database.loadState();
        assertEquals(4321, restored.crowns);
        assertEquals("Luminara", restored.location);
        assertEquals(originalUnits, units());
        assertEquals(originalStat, database.getStatValue(stat));
        assertEquals(originalAbilities, database.getAbilities().size());
        assertFalse(database.world().isDiscovered("Žaliasis Labirintas"));
        assertEquals(1, database.saveSlots().size());
        assertTrue(database.deleteSlot(1));
        assertTrue(database.saveSlots().isEmpty());
    }

    private GameState balancedState(String name) {
        GameState state = database.loadState();
        state.characterCreated = true;
        state.characterName = name;
        state.characterOriginId = "akademija";
        state.characterArchetypeId = "arkanistas";
        state.characterTraitIds.clear();
        state.characterTraitIds.add("smalsumas");
        state.characterTraitIds.add("drausme");
        state.characterTraitIds.add("atjauta");
        database.initializeCharacterProgression(state, "balanced", "akademija", "arkanistas");
        state.location = "Luminara";
        database.saveState(state);
        return state;
    }

    private int units() {
        int total = 0;
        for (VaeloriaDb.Item item : database.getItems()) total += item.quantity;
        return total;
    }
}
