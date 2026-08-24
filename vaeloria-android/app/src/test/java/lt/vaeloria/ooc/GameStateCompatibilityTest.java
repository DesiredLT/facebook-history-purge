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
        assertTrue(state.characterCreated);
        assertEquals("luminara",state.characterOriginId);
        assertEquals(3,state.characterTraitIds.size());
    }

    @Test public void freshStateWaitsForCharacterCreation(){
        GameState state=new GameState();assertFalse(state.characterCreated);assertTrue(state.characterTraitIds.isEmpty());
    }

    @Test public void roundTripPreservesCreatedCharacterProfile() throws Exception {
        GameState original=new GameState();original.characterCreated=true;original.characterName="Austėja";original.characterIdentity="moteris";original.characterOriginId="akademija";original.characterArchetypeId="arkanistas";original.characterAppearance="Sidabriniai plaukai ir mėlynas apsiaustas";original.characterTraitIds.add("smalsumas");original.characterTraitIds.add("drausme");original.characterTraitIds.add("atjauta");
        GameState restored=GameState.fromJson(original.toJson());
        assertTrue(restored.characterCreated);assertEquals("Austėja",restored.characterName);assertEquals("akademija",restored.characterOriginId);assertEquals("arkanistas",restored.characterArchetypeId);assertEquals(3,restored.characterTraitIds.size());assertEquals("Sidabriniai plaukai ir mėlynas apsiaustas",restored.characterAppearance);
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
        original.enemyHp = 217;
        original.enemyHpMax = 480;
        original.combatRound = 4;
        original.combatSpellCount = 5;
        original.combatHeavyMitigationUsed = true;
        original.combatDawnBarrierUsed = true;
        original.combatCheatDeathUsed = true;
        original.combatLastStandUsed = true;

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
        assertEquals(217, restored.enemyHp);
        assertEquals(480, restored.enemyHpMax);
        assertEquals(4, restored.combatRound);
        assertEquals(5, restored.combatSpellCount);
        assertTrue(restored.combatHeavyMitigationUsed);
        assertTrue(restored.combatDawnBarrierUsed);
        assertTrue(restored.combatCheatDeathUsed);
        assertTrue(restored.combatLastStandUsed);
    }

    @Test public void roundTripPreservesV100ProgressionDifficultyAndEnding() throws Exception {
        GameState original = new GameState();
        original.progressionMode = "balanced";
        original.level = 37;
        original.experience = 812;
        original.experienceNext = 1400;
        original.talentPoints = 6;
        original.difficulty = "nightmare";
        original.storyEnding = "nepriklausoma_chartija";
        original.tutorialComplete = true;

        GameState restored = GameState.fromJson(original.toJson());

        assertEquals("balanced", restored.progressionMode);
        assertEquals(37, restored.level);
        assertEquals(812, restored.experience);
        assertEquals(1400, restored.experienceNext);
        assertEquals(6, restored.talentPoints);
        assertEquals("nightmare", restored.difficulty);
        assertEquals("nepriklausoma_chartija", restored.storyEnding);
        assertTrue(restored.tutorialComplete);
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
