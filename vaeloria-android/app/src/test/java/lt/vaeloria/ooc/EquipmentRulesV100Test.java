package lt.vaeloria.ooc;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EquipmentRulesV100Test {
    @Test public void weaponAndArmorEffectsBecomeRealStats(){
        VaeloriaDb.Item weapon=item("Kardas","weapon","Suteikia 31 puolimo galios",31,null);
        VaeloriaDb.Item chest=item("Šarvai","chest","Suteikia 24 apsaugos arba kontrolės galios",24,null);
        EquipmentRules.Stats stats=EquipmentRules.calculate(Arrays.asList(weapon,chest));
        assertEquals(31,stats.attack);
        assertEquals(24,stats.defense);
    }

    @Test public void sixPieceSetsUseTheirDeclaredMechanics(){
        VaeloriaDb.Item[] pieces=new VaeloriaDb.Item[6];
        for(int i=0;i<pieces.length;i++)pieces[i]=item("Dalis "+i,"chest","Suteikia 1 apsaugos galios",1,"set_kapu_valdovas");
        EquipmentRules.Stats stats=EquipmentRules.calculate(Arrays.asList(pieces));
        assertEquals(25,stats.necroticResistance);
        assertTrue(stats.cheatDeath);
        assertTrue(stats.magicPower>=10);
    }

    private static VaeloriaDb.Item item(String name,String slot,String effect,int power,String set){
        VaeloriaDb.Item item=new VaeloriaDb.Item();item.name=name;item.slot=slot;item.effect=effect;item.power=power;item.setId=set;item.equipped=true;return item;
    }
}
