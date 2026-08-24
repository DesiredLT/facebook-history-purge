package lt.vaeloria.ooc;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AiTurnPolicyV101Test {
    @Test public void failedCheckCannotGrantTravelRewardsLootOrQuestProgress() throws Exception {
        GameState state=new GameState();state.objective="Patikrinti manifestą";
        StatEngine.Check check=check("nesėkmė");
        ItemCatalogV092.ItemDef known=ItemCatalogV092.ALL[0];
        JSONObject raw=base().put("event_tag","reward").put("location","Veyrhold")
                .put("hp_delta",99).put("mana_delta",99).put("stamina_delta",99).put("aeonic_delta",999)
                .put("crowns_delta",999999).put("asterra_delta",10).put("quest_note","Užduotis baigta")
                .put("loot",new JSONArray().put(new JSONObject().put("name",known.name)));

        JSONObject safe=AiTurnPolicyV101.sanitize(raw,state,check,"Keliauti į Veyrhold",locations(),false);

        assertEquals("setback",safe.getString("event_tag"));
        assertEquals("Luminara",safe.getString("location"));
        assertEquals(0,safe.getInt("hp_delta"));assertEquals(0,safe.getInt("mana_delta"));
        assertEquals(0,safe.getInt("stamina_delta"));assertEquals(0,safe.getInt("aeonic_delta"));
        assertEquals(0,safe.getLong("crowns_delta"));assertEquals(0,safe.getInt("asterra_delta"));
        assertEquals("Patikrinti manifestą",safe.getString("quest_note"));assertEquals(0,safe.getJSONArray("loot").length());
        assertTrue(safe.getString("scene").contains("Telefono patikra"));
    }

    @Test public void explicitKnownTravelIsAllowedButTimeRemainsBounded() throws Exception {
        GameState state=new GameState();
        JSONObject raw=base().put("event_tag","travel").put("location","Veyrhold").put("time_minutes",1200).put("crowns_delta",500);
        JSONObject safe=AiTurnPolicyV101.sanitize(raw,state,check("sėkmė"),"Keliauti į Veyrhold",locations(),false);
        assertEquals("Veyrhold",safe.getString("location"));assertEquals(240,safe.getInt("time_minutes"));assertEquals(0,safe.getLong("crowns_delta"));
    }

    @Test public void unknownLocationIsNeverAccepted() throws Exception {
        GameState state=new GameState();
        JSONObject safe=AiTurnPolicyV101.sanitize(base().put("event_tag","travel").put("location","Modelio sala"),state,check("sėkmė"),"Keliauti į Modelio salą",locations(),false);
        assertEquals("Luminara",safe.getString("location"));
    }

    @Test public void onlyCanonicalCatalogLootSurvivesSuccessfulReward() throws Exception {
        GameState state=new GameState();ItemCatalogV092.ItemDef known=ItemCatalogV092.ALL[0];
        JSONArray loot=new JSONArray().put(new JSONObject().put("name",known.name).put("category","artifact").put("rarity","unique").put("description","suklastota"))
                .put(new JSONObject().put("name","Modelio išgalvotas kardas"));
        JSONObject safe=AiTurnPolicyV101.sanitize(base().put("event_tag","reward").put("crowns_delta",9000).put("loot",loot),state,check("sėkmė"),"Atsiimti pagrįstą atlygį",locations(),false);
        assertEquals(5000,safe.getLong("crowns_delta"));assertEquals(1,safe.getJSONArray("loot").length());
        JSONObject item=safe.getJSONArray("loot").getJSONObject(0);assertEquals(known.name,item.getString("name"));assertEquals(known.category,item.getString("category"));assertEquals(known.rarity,item.getString("rarity"));
    }

    @Test public void narrativeResponseCannotStartNonAuthoritativeCombat() throws Exception {
        JSONObject safe=AiTurnPolicyV101.sanitize(base().put("combat_active",true).put("enemy_name","Išgalvotas dievas").put("enemy_hp",9999),new GameState(),check("sėkmė"),"Paklausti kelio",locations(),false);
        assertFalse(safe.getBoolean("combat_active"));assertEquals("",safe.getString("enemy_name"));assertEquals(0,safe.getInt("enemy_hp"));
    }

    @Test public void storyProgressPolicyUsesTheActualCheckOutcome(){
        assertFalse(WorldRepository.checkAllowsProgress(check("rimta nesėkmė"),"discovery"));
        assertFalse(WorldRepository.checkAllowsProgress(check("sėkmė"),"setback"));
        assertTrue(WorldRepository.checkAllowsProgress(check("dalinė sėkmė"),"discovery"));
    }

    private StatEngine.Check check(String outcome){StatEngine.Check value=new StatEngine.Check();value.primary="Analitinis mąstymas";value.outcome=outcome;return value;}
    private LinkedHashSet<String> locations(){return new LinkedHashSet<>(Arrays.asList("Luminara","Veyrhold","Kharad Vorn"));}
    private JSONObject base() throws Exception{return new JSONObject().put("scene_title","Rezultatas").put("scene","Bandymas pavyko.")
            .put("choices",new JSONArray().put("A").put("B").put("C")).put("location","Luminara").put("time_minutes",5)
            .put("hp_delta",0).put("mana_delta",0).put("stamina_delta",0).put("aeonic_delta",0).put("crowns_delta",0)
            .put("quest_note","").put("asterra_delta",0).put("dravenn_delta",0).put("lysara_delta",0).put("event_tag","none")
            .put("combat_active",false).put("enemy_name","").put("enemy_status","").put("enemy_telegraph","").put("combat_distance","mid")
            .put("combat_hazard","").put("enemy_hp",0).put("enemy_hp_max",0).put("combat_round",0).put("loot",new JSONArray());}
}
