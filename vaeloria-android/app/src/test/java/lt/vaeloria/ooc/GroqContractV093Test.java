package lt.vaeloria.ooc;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GroqContractV093Test {
    @Test public void systemPromptRequiresClearCoherentLithuanian(){
        String prompt=GroqClient.systemPromptForTest();
        assertTrue(prompt.contains("taisyklinga, natūralia ir rišlia lietuvių kalba"));
        assertTrue(prompt.contains("antruoju asmeniu"));
        assertTrue(prompt.contains("2–4 trumpų"));
        assertTrue(prompt.contains("lygiai 3 materialiai skirtingus pasirinkimus"));
        assertTrue(prompt.contains("kilmės, archetipo, bruožų"));
    }

    @Test public void userPromptUsesTheSavedProfileInsteadOfHardcodedEinorasCanon(){
        GameState state=new GameState();state.characterCreated=true;state.characterName="Austėja";state.characterIdentity="moteris";state.characterOriginId="pelkynai";state.characterArchetypeId="zvalgas";state.characterTraitIds.addAll(Arrays.asList("pastabumas","atsargumas","vikrumas"));
        String prompt=GroqClient.userPromptForTest(state);
        assertTrue(prompt.contains("Austėja"));assertTrue(prompt.contains("Šventųjų pelkynų vaikas"));assertTrue(prompt.contains("Žvalgas"));assertTrue(prompt.contains("Pastabumas, Atsargumas, Vikrumas"));
        assertFalse(prompt.contains("Einoras: 201"));
    }
}
