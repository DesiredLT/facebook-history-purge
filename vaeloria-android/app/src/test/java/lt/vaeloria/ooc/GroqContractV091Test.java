package lt.vaeloria.ooc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class GroqContractV091Test {
    @Test public void narrativeFieldsHaveMobileSafeLimits() throws Exception {
        JSONObject schema = NarrationTestAssets.schema();
        JSONObject properties = schema.getJSONObject("properties");
        assertEquals(100, properties.getJSONObject("scene_title").getInt("maxLength"));
        assertEquals(1000, properties.getJSONObject("scene").getInt("maxLength"));
        assertEquals(150, properties.getJSONObject("choices").getJSONObject("items").getInt("maxLength"));

        JSONArray required = schema.getJSONArray("required");
        assertEquals(3,required.length());
        assertTrue(contains(required, "scene_title"));
        assertTrue(contains(required, "scene"));
        assertTrue(contains(required, "choices"));
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
