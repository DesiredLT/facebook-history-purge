package lt.vaeloria.ooc;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ItemCatalogV092Test {
    @Test public void catalogContains325UniqueMechanicallyDefinedIllustratedItems() {
        assertEquals(325, ItemCatalogV092.ALL.length);
        Set<String> ids=new HashSet<>(),names=new HashSet<>();Set<Integer> artwork=new HashSet<>();
        Set<String> rarities=new HashSet<>(),categories=new HashSet<>();
        for(ItemCatalogV092.ItemDef item:ItemCatalogV092.ALL){
            assertTrue(ids.add(item.id));assertTrue(names.add(item.name));assertTrue(artwork.add(item.artwork));
            assertTrue(item.artwork!=0);assertTrue(item.level>=1&&item.level<=100);assertTrue(item.power>=0);
            assertTrue(item.value>=0);assertTrue(item.dropWeight>0);assertFalse(item.description.isEmpty());assertFalse(item.effect.isEmpty());
            assertNotNull(ItemCatalogV092.find(item.name));assertEquals(item,ItemCatalogV092.byId(item.id));
            rarities.add(item.rarity);categories.add(item.category);
        }
        assertEquals(325,ids.size());assertEquals(325,names.size());assertEquals(325,artwork.size());
        assertEquals(new HashSet<>(java.util.Arrays.asList(ItemCatalogV092.RARITIES)),rarities);
        assertEquals(new HashSet<>(java.util.Arrays.asList(ItemCatalogV092.CATEGORIES)),categories);
    }

    @Test public void tenSetsHaveExactlySixEquippablePiecesAndTierBonuses() {
        assertEquals(10,ItemCatalogV092.SETS.length);Map<String,Integer> pieces=new HashMap<>();
        for(ItemCatalogV092.ItemDef item:ItemCatalogV092.ALL)if(!item.setId.isEmpty()){
            pieces.put(item.setId,pieces.getOrDefault(item.setId,0)+1);assertNotNull(item.slot);assertNotNull(ItemCatalogV092.setById(item.setId));
        }
        assertEquals(10,pieces.size());
        for(ItemCatalogV092.SetDef set:ItemCatalogV092.SETS){assertEquals(Integer.valueOf(6),pieces.get(set.id));assertFalse(set.bonus2.isEmpty());assertFalse(set.bonus4.isEmpty());assertFalse(set.bonus6.isEmpty());}
    }

    @Test public void potionsFoodAndCombatConsumablesCanActuallyBeUsed() {
        int consumables=0,restoratives=0,combat=0;
        for(ItemCatalogV092.ItemDef item:ItemCatalogV092.ALL)if(item.consumable){
            consumables++;assertTrue(item.stackable);
            if(item.hpRestore+item.manaRestore+item.staminaRestore+item.aeonicRestore>0)restoratives++;
            if("combat_consumable".equals(item.category)){combat++;assertTrue(item.power>0);}
        }
        assertTrue(consumables>=50);assertTrue(restoratives>=30);assertTrue(combat>=16);
    }
}
