package lt.vaeloria.ooc;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.JSONArray;
import org.json.JSONObject;

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

    @Test public void versionElevenBalancedProfileUpgradesThroughVersionFourteen() {
        GameState state=balancedState("Migracijos herojė");
        database.getWritableDatabase().execSQL("UPDATE stats SET value=value+4 WHERE group_name='MAGINĖS SAVYBĖS'");
        database.getWritableDatabase().execSQL("UPDATE stats SET value=value+8 WHERE name IN ('Manos kontrolė','Magijos jutimas','Burtų stabilumas','Relikvijų rezonansas')");
        assertEquals(52,database.getStatValue("Manos kontrolė"));
        assertEquals(44,database.getStatValue("Burtų galia"));
        database.getWritableDatabase().setVersion(11);
        database.close();

        database=new VaeloriaDb(context);
        assertEquals(14,database.getWritableDatabase().getVersion());
        assertEquals(40,database.getStatValue("Manos kontrolė"));
        assertEquals(40,database.getStatValue("Burtų galia"));
    }

    @Test public void itemUseAndEquipmentRespectCharacterLevel(){
        GameState state=balancedState("Lygių herojė");assertEquals(1,state.level);
        ItemCatalogV092.ItemDef unique=ItemCatalogV092.byId("I092-025");String weaponId=database.addCatalogLoot(unique,1);
        assertFalse(database.equipToSlot(weaponId,"weapon",state.level));assertFalse(database.getItem(weaponId).equipped);
        ItemCatalogV092.ItemDef highPotion=ItemCatalogV092.byId("I092-225");String potionId=database.addCatalogLoot(highPotion,2);int before=database.getItem(potionId).quantity;
        assertTrue(database.consumeItem(potionId,state).contains("reikia"));assertEquals(before,database.getItem(potionId).quantity);
    }

    @Test public void versionElevenJsonImportAlsoRemovesDuplicatedProfileBonuses() throws Exception {
        balancedState("Seno importo herojė");
        JSONObject root=new JSONObject(database.exportSave()).put("version",11);
        JSONArray stats=root.getJSONArray("stats");
        for(int i=0;i<stats.length();i++)if("Manos kontrolė".equals(stats.getJSONObject(i).getString("name")))stats.getJSONObject(i).put("value",52);
        assertTrue(database.importSave(root.toString()));assertEquals(40,database.getStatValue("Manos kontrolė"));
        assertTrue(database.importSave(database.exportSave()));assertEquals(40,database.getStatValue("Manos kontrolė"));
    }

    @Test public void attributePointsAndMultiIngredientRecipesAreRealMechanics(){
        GameState state=balancedState("Amatininkė");state.attributePoints=1;database.saveState(state);int before=database.getStatValue("Jėga");
        assertTrue(database.spendAttributePoint("Jėga",state).contains("padidinta"));assertEquals(Math.min(100,before+2),database.getStatValue("Jėga"));assertEquals(0,state.attributePoints);
        assertTrue(database.world().recipes().size()>=27);boolean foundMulti=false;for(WorldRepository.Recipe recipe:database.world().recipes())if(recipe.ingredients.size()>=2){foundMulti=true;break;}assertTrue(foundMulti);
    }

    @Test public void importedCatalogItemCannotForgePowerOrEquipmentSlot() throws Exception {
        balancedState("Importo herojus");
        JSONObject root=new JSONObject(database.exportSave());JSONArray items=root.getJSONArray("items");JSONObject selected=null;
        for(int i=0;i<items.length();i++)if(!items.getJSONObject(i).isNull("catalog_id")){selected=items.getJSONObject(i);break;}
        assertNotNull(selected);String id=selected.getString("id");String catalogId=selected.getString("catalog_id");ItemCatalogV092.ItemDef canonical=ItemCatalogV092.byId(catalogId);assertNotNull(canonical);
        selected.put("name","Suklastotas daiktas").put("power",99999).put("item_level",999).put("value",99999999).put("effect","Suteikia 99999 puolimo galios").put("category","ring").put("equipped_slot","ring_left");

        assertTrue(database.importSave(root.toString()));
        VaeloriaDb.Item restored=database.getItem(id);assertNotNull(restored);
        assertEquals(canonical.name,restored.name);assertEquals(canonical.power,restored.power);assertEquals(canonical.level,restored.itemLevel);assertEquals(canonical.value,restored.value);assertEquals(canonical.effect,restored.effect);
        assertFalse(restored.equipped);assertNull(restored.equippedSlot);
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
