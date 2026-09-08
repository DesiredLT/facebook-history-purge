package lt.vaeloria.ooc;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GroqContractV093Test {
    @Test public void systemPromptRequiresClearCoherentLithuanian() throws Exception{
        String prompt=NarrationTestAssets.policy();
        assertTrue(prompt.contains("taisyklinga, natūralia ir rišlia lietuvių kalba"));
        assertTrue(prompt.contains("antruoju asmeniu"));
        assertTrue(prompt.contains("2–4 trumpų"));
        assertTrue(prompt.contains("lygiai 3 leistini veiksmai"));
        assertTrue(prompt.toLowerCase(java.util.Locale.ROOT).contains("kilmės, archetipo, bruožų"));
    }

    @Test public void userPromptUsesTheSavedProfileInsteadOfHardcodedEinorasCanon(){
        GameState state=new GameState();state.characterCreated=true;state.characterName="Austėja";state.characterIdentity="moteris";state.characterOriginId="pelkynai";state.characterArchetypeId="zvalgas";state.characterTraitIds.addAll(Arrays.asList("pastabumas","atsargumas","vikrumas"));state.progressionMode="balanced";state.level=17;state.experience=320;state.experienceNext=900;state.difficulty="hard";
        String prompt=NarrativeTurn.userPrompt(state,"Ištirti Meridiano poslinkį","nėra",java.util.Collections.emptyList(),null,"");
        assertTrue(prompt.contains("Austėja"));assertTrue(prompt.contains("Šventųjų pelkynų vaikas"));assertTrue(prompt.contains("Žvalgas"));assertTrue(prompt.contains("Pastabumas, Atsargumas, Vikrumas"));
        assertTrue(prompt.contains("subalansuotas"));assertTrue(prompt.contains("veikėjo lygis 17"));assertTrue(prompt.contains("sunkumas hard"));
        assertFalse(prompt.contains("Einoras: 201"));
    }
}
