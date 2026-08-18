package lt.vaeloria.ooc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class EnemyCatalogV091Test {
    @Test public void twoHundredEnemiesHaveUniqueArtAndCompleteStats() {
        assertEquals(200, EnemyCatalogV091.ALL.length);
        Set<String> ids = new HashSet<>();
        Set<String> names = new HashSet<>();
        Set<Integer> artwork = new HashSet<>();
        for (EnemyCatalogV091.Enemy enemy : EnemyCatalogV091.ALL) {
            assertTrue(ids.add(enemy.id));
            assertTrue(names.add(enemy.name));
            assertTrue(artwork.add(enemy.artwork));
            assertTrue(enemy.danger >= 1 && enemy.danger <= 10);
            assertTrue(enemy.hp > 0);
            assertTrue(enemy.attack > 0);
            assertTrue(enemy.defense > 0);
            assertTrue(enemy.speed > 0 && enemy.speed <= 100);
            assertNotNull(enemy.role);
            assertNotNull(enemy.trait);
            assertNotNull(enemy.description);
            assertEquals(enemy.artwork, EnemyCatalogV091.artFor(enemy.name));
            assertEquals(enemy.artwork, VisualAssetCatalog.monsterFor(enemy.name));
        }
    }

    @Test public void everyRegionContainsExactlyTwentyFiveEnemies() {
        assertEquals(8, EnemyCatalogV091.regions().length);
        for (String region : EnemyCatalogV091.regions()) {
            assertEquals(25, EnemyCatalogV091.inRegion(region).size());
        }
    }

    @Test public void longActionTextCanResolveAnIllustratedEnemy() {
        EnemyCatalogV091.Enemy enemy = EnemyCatalogV091.find("Noriu atsargiai kovoti su Pašvaistės drakonu");
        assertNotNull(enemy);
        assertEquals("Pašvaistės drakonas", enemy.name);
        assertNotEquals(R.drawable.monster_meridian_wolf_v090, VisualAssetCatalog.monsterFor(enemy.name));
    }

    @Test public void correctedNpcAndLegacyMonsterMappingsAreExact() {
        assertEquals(R.drawable.npc_oren_merchant_v090, VisualAssetCatalog.npcFor("Orenas Pelas"));
        assertEquals(R.drawable.npc_corva_broker_v090, VisualAssetCatalog.npcFor("Korva Dain"));
        assertEquals(R.drawable.monster_venom_broodmother_v090,
                VisualAssetCatalog.monsterFor("Nuodų perų motina"));
    }
}
