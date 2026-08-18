package lt.vaeloria.ooc;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CharacterProfileV093Test {
    @Test public void catalogOffersSixOriginsSixArchetypesAndTwelveBalancedTraits(){
        assertEquals(6,CharacterCatalogV093.ORIGINS.length);
        assertEquals(6,CharacterCatalogV093.ARCHETYPES.length);
        assertEquals(12,CharacterCatalogV093.TRAITS.length);
        Set<String> ids=new HashSet<>();
        for(CharacterCatalogV093.Trait trait:CharacterCatalogV093.TRAITS){
            assertTrue(ids.add(trait.id));assertFalse(trait.benefit.isEmpty());assertFalse(trait.drawback.isEmpty());
            assertTrue(trait.positive>0);assertTrue(trait.negative<0);
        }
    }

    @Test public void profileRequiresExactlyThreeDistinctKnownTraits(){
        assertTrue(CharacterCatalogV093.validProfile("Austėja","moteris","akademija","arkanistas",Arrays.asList("smalsumas","drausme","atjauta")));
        assertFalse(CharacterCatalogV093.validProfile("Austėja","moteris","akademija","arkanistas",Arrays.asList("smalsumas","drausme")));
        assertFalse(CharacterCatalogV093.validProfile("Austėja","moteris","akademija","arkanistas",Arrays.asList("smalsumas","smalsumas","drausme")));
        assertFalse(CharacterCatalogV093.validProfile("A","moteris","akademija","arkanistas",Arrays.asList("smalsumas","drausme","atjauta")));
    }

    @Test public void originArchetypeAndTraitsApplyRealBonusesAndDrawbacks(){
        GameState state=new GameState();state.characterCreated=true;state.characterOriginId="akademija";state.characterArchetypeId="arkanistas";
        state.characterTraitIds.addAll(Arrays.asList("smalsumas","drausme","karstakraujiskumas"));
        CharacterCatalogV093.Effect mana=CharacterCatalogV093.effect(state,"Manos kontrolė");
        assertEquals(13,mana.value);assertTrue(mana.explanation.contains("Didžiosios akademijos"));assertTrue(mana.explanation.contains("Arkanistas"));assertTrue(mana.explanation.contains("Drausmė"));
        assertEquals(-5,CharacterCatalogV093.effect(state,"Diplomatija").value);
        assertEquals(0,CharacterCatalogV093.effect(state,"Plaukimas").value);
    }
}
