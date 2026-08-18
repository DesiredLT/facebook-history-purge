package lt.vaeloria.ooc;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GroqContractV092Test {
    @Test public void combatStateAndAuthoritativeVictoryTagsAreStrictlyRequired() throws Exception {
        JSONObject schema=GroqClient.responseFormatForTest().getJSONObject("json_schema").getJSONObject("schema");
        JSONObject properties=schema.getJSONObject("properties");JSONArray required=schema.getJSONArray("required");
        for(String field:new String[]{"enemy_hp","enemy_hp_max","combat_round"}){assertTrue(properties.has(field));assertTrue(contains(required,field));}
        JSONArray events=properties.getJSONObject("event_tag").getJSONArray("enum");
        assertTrue(contains(events,"combat_victory"));assertTrue(contains(events,"combat_escape"));
        JSONArray rarities=properties.getJSONObject("loot").getJSONObject("items").getJSONObject("properties").getJSONObject("rarity").getJSONArray("enum");
        assertTrue(contains(rarities,"mythic"));
    }

    private boolean contains(JSONArray array,String value){for(int i=0;i<array.length();i++)if(value.equals(array.optString(i)))return true;return false;}
}
