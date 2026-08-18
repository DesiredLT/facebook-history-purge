package lt.vaeloria.ooc;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LithuanianNarrativeV093Test {
    @Test public void visibleTextRemovesFormattingAndCommonAnglicisms(){
        String polished=LithuanianNarrative.polishTextForTest("  **Loot**  from waygate: gear, combat.  ");
        assertFalse(polished.contains("**"));assertFalse(polished.toLowerCase().contains("loot"));assertFalse(polished.toLowerCase().contains("waygate"));assertFalse(polished.toLowerCase().contains("gear"));assertFalse(polished.toLowerCase().contains("combat"));
        assertTrue(polished.contains("Grobis"));assertTrue(polished.endsWith("."));
    }

    @Test public void turnAlwaysContainsThreeDistinctReadableChoices() throws Exception {
        JSONObject turn=new JSONObject();turn.put("scene_title","# Discovery");turn.put("scene","  Veiksmas   turi   aiškią pasekmę  ");
        turn.put("choices",new JSONArray().put("Tirti pėdsaką").put("Tirti pėdsaką").put(""));turn.put("loot",new JSONArray());
        JSONObject polished=LithuanianNarrative.polish(turn,new GameState());JSONArray choices=polished.getJSONArray("choices");
        assertEquals(3,choices.length());assertFalse(choices.getString(0).equalsIgnoreCase(choices.getString(1)));assertFalse(polished.getString("scene_title").contains("#"));assertTrue(polished.getString("scene").endsWith("."));
    }
}
