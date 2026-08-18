package lt.vaeloria.ooc;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DropTableV092Test {
    @Test public void everyDangerProfileIsACompletePercentDistribution() {
        for(int danger=1;danger<=10;danger++){
            DropTableV092.DropProfile profile=DropTableV092.forDanger(danger);
            assertEquals(danger,profile.danger);assertEquals(10000,profile.totalRarityBasisPoints());
            assertTrue(profile.rolls>=1&&profile.rolls<=3);assertTrue(profile.dropChanceBasisPoints>=7200&&profile.dropChanceBasisPoints<=10000);
        }
        assertTrue(DropTableV092.forDanger(10).rateFor("mythic")>DropTableV092.forDanger(3).rateFor("mythic"));
        assertTrue(DropTableV092.forDanger(10).rateFor("legendary")>DropTableV092.forDanger(3).rateFor("legendary"));
        assertEquals(100,DropTableV092.forDanger(10).rateFor("unique"));
    }

    @Test public void all200RegionalAnd18LegacyMonstersHaveProfiles() {
        for(EnemyCatalogV091.Enemy enemy:EnemyCatalogV091.ALL){DropTableV092.MonsterProfile profile=DropTableV092.profileFor(enemy.name);assertEquals(enemy.danger,profile.drops.danger);assertEquals(enemy.region,profile.region);}
        String[] legacy={"Meridiano vilkas","Pelkių trolis","Nuodų perų motina","Kristalų golemas","Nakties harpija","Maitėdis drake'as","Nuskendęs riteris","Pelenų revenantas","Kaulų orakulas","Tuštumos parazitas","Maro kiautas","Kapų kolosas","Meridiano wyrmas","Bekarūnis titanas","Kraujšaknė Matriarchė","Stiklo lichas","Audros kolosas","Bedugnės šauklys"};
        for(String name:legacy){DropTableV092.MonsterProfile profile=DropTableV092.profileFor(name);assertTrue(profile.drops.danger>=4);assertEquals(10000,profile.drops.totalRarityBasisPoints());}
    }

    @Test public void worldBossRollIsDeterministicAndNeverDropsQuestItems() {
        List<ItemCatalogV092.ItemDef> first=DropTableV092.roll("Užtemimo drakonas",987654321L);
        List<ItemCatalogV092.ItemDef> second=DropTableV092.roll("Užtemimo drakonas",987654321L);
        assertEquals(3,first.size());assertEquals(first.size(),second.size());
        for(int index=0;index<first.size();index++){assertEquals(first.get(index).id,second.get(index).id);assertFalse("quest".equals(first.get(index).category));assertTrue(ItemCatalogV092.rarityRank(first.get(index).rarity)>=1);}
    }
}
