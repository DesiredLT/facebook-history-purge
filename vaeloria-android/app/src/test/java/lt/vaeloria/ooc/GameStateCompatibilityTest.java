package lt.vaeloria.ooc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class GameStateCompatibilityTest {
    @Test public void oldSaveWithoutFactionFieldsKeepsLegacyDataAndDefaults() throws Exception {
        JSONObject legacy = new JSONObject();
        legacy.put("characterName", "Einoras");
        legacy.put("location", "Veyrhold");
        legacy.put("worldYear", 922);
        legacy.put("worldMinute", 123456L);
        legacy.put("hp", 77);
        legacy.put("hpMax", 100);
        legacy.put("questTitle", "Lūžęs Meridianas");
        legacy.put("sceneTitle", "Senas išsaugojimas");
        legacy.put("scene", "Būsena sukurta iki frakcijų laukų atsiradimo.");
        legacy.put("choices", new JSONArray().put("Pirmas").put("Antras").put("Trečias"));

        GameState state = GameState.fromJson(legacy);

        assertEquals("Veyrhold", state.location);
        assertEquals(77, state.hp);
        assertEquals(34, state.asterraInfluence);
        assertEquals(72, state.dravennInfluence);
        assertEquals(24, state.lysaraInfluence);
        assertEquals(3, state.choices.size());
        assertFalse(state.combatActive);
    }

    @Test public void roundTripPreservesDynamicFactionAndCombatState() throws Exception {
        GameState original = new GameState();
        original.asterraInfluence = 61;
        original.dravennInfluence = 43;
        original.lysaraInfluence = 78;
        original.asterraRelation = "PALANKI";
        original.dravennRelation = "ATSARGI";
        original.lysaraRelation = "SĄJUNGINĖ";
        original.combatActive = true;
        original.enemyName = "Pelenų revenantas";
        original.enemyStatus = "Sužeistas";
        original.enemyTelegraph = "Ruošia kontrataką";
        original.combatDistance = "close";
        original.combatHazard = "Slidus akmuo";

        GameState restored = GameState.fromJson(original.toJson());

        assertEquals(61, restored.asterraInfluence);
        assertEquals(43, restored.dravennInfluence);
        assertEquals(78, restored.lysaraInfluence);
        assertEquals("PALANKI", restored.asterraRelation);
        assertEquals("ATSARGI", restored.dravennRelation);
        assertEquals("SĄJUNGINĖ", restored.lysaraRelation);
        assertTrue(restored.combatActive);
        assertEquals("Pelenų revenantas", restored.enemyName);
        assertEquals("Slidus akmuo", restored.combatHazard);
    }

    @Test public void turnConsequencesClampFactionInfluence() throws Exception {
        GameState state = new GameState();
        state.asterraInfluence = 98;
        state.dravennInfluence = 2;
        state.lysaraInfluence = 50;
        JSONObject turn = new JSONObject();
        turn.put("asterra_delta", 20);
        turn.put("dravenn_delta", -20);
        turn.put("lysara_delta", 7);
        turn.put("choices", new JSONArray().put("A").put("B").put("C"));

        state.applyTurn(turn);

        assertEquals(100, state.asterraInfluence);
        assertEquals(0, state.dravennInfluence);
        assertEquals(57, state.lysaraInfluence);
    }
}
