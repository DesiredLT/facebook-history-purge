package lt.vaeloria.ooc;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GroqContractV092Test {
    @Test public void neitherProviderCanReturnMechanicalFields() throws Exception {
        JSONObject schema=NarrationTestAssets.schema();
        JSONObject properties=schema.getJSONObject("properties");JSONArray required=schema.getJSONArray("required");
        for(String field:new String[]{"enemy_hp","enemy_hp_max","combat_round","event_tag","loot","crowns_delta","location","quest_note"}){
            org.junit.Assert.assertFalse(properties.has(field));org.junit.Assert.assertFalse(contains(required,field));
        }
        org.junit.Assert.assertFalse(schema.getBoolean("additionalProperties"));
        assertTrue(schema.getJSONArray("required").length()==3);
    }

    private boolean contains(JSONArray array,String value){for(int i=0;i<array.length();i++)if(value.equals(array.optString(i)))return true;return false;}
}
