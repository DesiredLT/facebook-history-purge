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
}
