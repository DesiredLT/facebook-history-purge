package lt.vaeloria.ooc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class GroqContractV091Test {
    @Test public void combatTextFieldsHaveMobileSafeLimits() throws Exception {
        JSONObject format = GroqClient.responseFormatForTest();
        JSONObject schema = format.getJSONObject("json_schema").getJSONObject("schema");
        JSONObject properties = schema.getJSONObject("properties");
        assertEquals(64, properties.getJSONObject("enemy_name").getInt("maxLength"));
        assertEquals(60, properties.getJSONObject("enemy_status").getInt("maxLength"));
        assertEquals(100, properties.getJSONObject("enemy_telegraph").getInt("maxLength"));
        assertEquals(70, properties.getJSONObject("combat_hazard").getInt("maxLength"));

        JSONArray required = schema.getJSONArray("required");
        assertTrue(contains(required, "combat_active"));
        assertTrue(contains(required, "enemy_name"));
        assertTrue(contains(required, "enemy_telegraph"));
        assertTrue(contains(required, "loot"));
    }

    @Test public void aiRosterIncludesAllAdditionalIllustratedEnemies() {
        String roster = EnemyCatalogV091.promptRoster();
        for (EnemyCatalogV091.Enemy enemy : EnemyCatalogV091.ALL) {
            assertTrue(roster.contains(enemy.name));
        }
    }

    private boolean contains(JSONArray array, String expected) {
        for (int index = 0; index < array.length(); index++) {
            if (expected.equals(array.optString(index))) return true;
        }
        return false;
    }
}
