package lt.vaeloria.ooc;

import org.json.*;
import org.junit.Test;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;

public class OpenAiNarrationTest {
    private JSONObject narration() throws Exception {
        return new JSONObject().put("scene_title","Vartų žymės").put("scene","Apžiūri vartų akmenis. Juose pastebi senas žymes.")
                .put("choices",new JSONArray().put("Ištirti žymes").put("Paklausti sargybinio").put("Užrašyti pastebėjimus"));
    }

    @Test public void forgedModelRewardsCombatAndLocationCannotChangeAnyEngineField() throws Exception {
        GameState state=new GameState();JSONObject resolved=LocalTurnResolver.resolve(state,"Ištirti manifestą",null,new HashSet<>(Arrays.asList("Luminara","Veyrhold")));
        JSONObject forged=narration().put("crowns_delta",999999).put("event_tag","combat_victory").put("location","Veyrhold")
                .put("combat_active",true).put("enemy_hp",0).put("loot",new JSONArray().put(new JSONObject().put("name",ItemCatalogV092.ALL[0].name)))
                .put("quest_note","Viskas atlikta").put("experience",99999);
        JSONObject safe=NarrativeTurn.merge(resolved,forged,state);
        for(Iterator<String> keys=resolved.keys();keys.hasNext();){String key=keys.next();
            if(!Arrays.asList("scene_title","scene","choices").contains(key))assertEquals(key,resolved.get(key).toString(),safe.get(key).toString());}
        assertFalse(safe.has("experience"));assertEquals("Vartų žymės",safe.getString("scene_title"));assertEquals("Manifesto neatitikimas",resolved.getString("scene_title"));
    }

    @Test public void malformedAndEnglishNarrationFallsBackInsteadOfApplyingPartialOutput() throws Exception {
        for(JSONObject bad:new JSONObject[]{narration().put("scene",7),narration().put("scene","You have found the key and you can open the gate."),
                narration().put("choices",new JSONArray().put("A").put("A").put("A")),narration().put("scene","")}){
            try{NarrativeTurn.merge(new JSONObject(),bad,new GameState());fail("Invalid narrative was accepted");}catch(IllegalArgumentException expected){}
        }
    }

    @Test public void travelUsesDestinationAfterSourceAndSupportsReturningHome() throws Exception {
        Set<String> places=new LinkedHashSet<>(Arrays.asList("Luminara","Veyrhold","Aureliono Akademija"));
        GameState state=new GameState();state.location="Veyrhold";
        JSONObject turn=LocalTurnResolver.resolve(state,"Keliauti iš Veyrhold į Luminara saugiausiu keliu",null,places);
        assertEquals("Luminara",turn.getString("location"));assertEquals("travel",turn.getString("event_tag"));
        turn=LocalTurnResolver.resolve(state,"Keliauti iš Veyrhold į Aureliono Akademija",null,places);
        assertEquals("Aureliono Akademija",turn.getString("location"));
        turn=LocalTurnResolver.resolve(state,"Keliauti į neatrastą Žaliąjį Labirintą",null,places);
        assertEquals(state.location,turn.getString("location"));assertEquals(0,turn.getInt("time_minutes"));
    }

    @Test public void openAiConfigRejectsCleartextRedirectCredentialsAndProviderKeys() throws Exception {
        String code="device_code_".repeat(4);
        assertEquals("https://game.example/ai",OpenAiSettings.validated("https://game.example/ai/",code).url);
        for(String url:new String[]{"http://game.example","https://user:secret@game.example","https://api.openai.com","https://game.example?token=x","https://game.example/#x"}){
            try{OpenAiSettings.validated(url,code);fail(url);}catch(IllegalArgumentException expected){}
        }
        try{OpenAiSettings.validated("https://game.example","sk-"+code);fail();}catch(IllegalArgumentException expected){}
    }

    @Test public void boundedReadersRejectOversizeBeforeReturningAndCloseStreams() throws Exception {
        final boolean[] closed={false};ByteArrayInputStream input=new ByteArrayInputStream(new byte[100]){
            @Override public void close() throws IOException{closed[0]=true;super.close();}
        };
        try{AiHttpClient.readBounded(input,10);fail();}catch(IOException expected){}
        assertTrue(closed[0]);
        AiHttpClient cancelled=new AiHttpClient();cancelled.cancel();
        try{cancelled.post("https://game.example", "",new JSONObject());fail();}catch(InterruptedIOException expected){}
    }
}
