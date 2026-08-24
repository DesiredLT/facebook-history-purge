package lt.vaeloria.ooc;

import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CombatEngineV100Test {
    @Test public void combatStartsWithCatalogStats(){
        GameState state=new GameState();
        JSONObject result=CombatEngine.resolve(state,"Atakuoti Pelenų skaliką",null,new EquipmentRules.Stats(),new HashMap<>());
        assertTrue(result.optBoolean("combat_active"));
        assertTrue(result.optInt("enemy_attack")>0);
        assertTrue(result.optInt("enemy_defense")>0);
        assertTrue(result.optInt("enemy_speed")>0);
        assertEquals(3,result.optJSONArray("choices").length());
    }

    @Test public void defenseUsesEnemyAndEquipmentStats(){
        GameState state=new GameState();state.combatActive=true;state.enemyName="Pelenų skalikas";state.enemyHp=144;state.enemyHpMax=144;
        state.enemyAttack=26;state.enemyDefense=23;state.enemySpeed=38;state.enemyDanger=2;state.combatRound=1;
        EquipmentRules.Stats gear=new EquipmentRules.Stats();gear.defense=60;
        JSONObject result=CombatEngine.resolve(state,"Blokuoti priešo smūgį",null,gear,new HashMap<>());
        assertTrue(result.optInt("player_guard")>0);
        assertTrue(result.optInt("hp_delta")<=0);
        assertEquals(2,result.optInt("combat_round"));
    }

    @Test public void zeroStaminaCannotProduceAFreeWeaponAttack(){
        GameState state=activeEnemy();state.stamina=0;
        JSONObject result=CombatEngine.resolve(state,"Atakuoti ir išlaikyti spaudimą",null,new EquipmentRules.Stats(),new HashMap<>());
        assertEquals(state.enemyHp,result.optInt("enemy_hp"));
        assertTrue(result.optInt("stamina_delta")>0);
        assertTrue(result.optString("player_combat_status").contains("Atgauna kvapą"));
    }

    @Test public void thirdSpellDiscountCountsOnlyActuallyCastSpells(){
        GameState state=activeEnemy();state.mana=100;state.combatCombo=2;state.combatSpellCount=0;
        EquipmentRules.Stats gear=new EquipmentRules.Stats();gear.thirdSpellDiscount=true;
        JSONObject first=CombatEngine.resolve(state,"Panaudoti kovinį magijos gebėjimą",null,gear,new HashMap<>());
        assertEquals(-18,first.optInt("mana_delta"));
        assertEquals(1,first.optInt("combat_spell_count"));

        state.combatCombo=0;state.combatSpellCount=2;state.combatAbilityCooldown=0;
        JSONObject third=CombatEngine.resolve(state,"Panaudoti kovinį magijos gebėjimą",null,gear,new HashMap<>());
        assertEquals(-8,third.optInt("mana_delta"));
        assertEquals(3,third.optInt("combat_spell_count"));
    }

    @Test public void oneUseMitigationIsPersistedOutsideDisplayStatus(){
        GameState fresh=activeEnemy();fresh.enemyAttack=160;fresh.enemyDefense=200;fresh.hp=100;fresh.hpMax=100;
        EquipmentRules.Stats gear=new EquipmentRules.Stats();gear.heavyHitMitigation=true;
        JSONObject protectedTurn=CombatEngine.resolve(fresh,"Atgauti kvapą ir saugoti poziciją",null,gear,new HashMap<>());
        assertTrue(protectedTurn.optBoolean("combat_heavy_mitigation_used"));

        GameState spent=activeEnemy();spent.enemyAttack=160;spent.enemyDefense=200;spent.hp=100;spent.hpMax=100;spent.combatHeavyMitigationUsed=true;
        JSONObject unprotectedTurn=CombatEngine.resolve(spent,"Atgauti kvapą ir saugoti poziciją",null,gear,new HashMap<>());
        assertTrue(Math.abs(unprotectedTurn.optInt("hp_delta"))>Math.abs(protectedTurn.optInt("hp_delta")));
    }

    private GameState activeEnemy(){
        GameState state=new GameState();state.combatActive=true;state.enemyName="Pelenų skalikas";state.enemyHp=600;state.enemyHpMax=600;
        state.enemyAttack=40;state.enemyDefense=30;state.enemySpeed=38;state.enemyDanger=2;state.combatRound=1;state.mana=100;state.manaMax=100;
        return state;
    }
}
