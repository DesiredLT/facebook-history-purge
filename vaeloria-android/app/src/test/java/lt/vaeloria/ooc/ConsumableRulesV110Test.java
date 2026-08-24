package lt.vaeloria.ooc;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConsumableRulesV110Test {
    @Test public void restorativePotionUsesCanonicalResourceValues(){
        ConsumableRulesV110.Effect effect=ConsumableRulesV110.effect(ItemCatalogV092.byId("I092-201"));
        assertTrue(effect.hp==25);assertFalse(effect.requiresCombat);assertTrue(effect.hasRestoration());
    }

    @Test public void namedElixirsHaveDistinctStructuredBuffs(){
        ConsumableRulesV110.Effect stone=ConsumableRulesV110.effect(ItemCatalogV092.byId("I092-212"));
        ConsumableRulesV110.Effect speed=ConsumableRulesV110.effect(ItemCatalogV092.byId("I092-214"));
        ConsumableRulesV110.Effect arcane=ConsumableRulesV110.effect(ItemCatalogV092.byId("I092-215"));
        assertTrue(stone.defense>=20&&stone.turns>=4);assertTrue(speed.speed>=20&&speed.critical>0);assertTrue(arcane.magic>=20&&arcane.check>0);
    }

    @Test public void combatConsumablesRequireCombatAndAdvanceEnemyEffects(){
        ConsumableRulesV110.Effect fire=ConsumableRulesV110.effect(ItemCatalogV092.byId("I092-226"));
        ConsumableRulesV110.Effect frost=ConsumableRulesV110.effect(ItemCatalogV092.byId("I092-227"));
        assertTrue(fire.requiresCombat&&fire.directDamage>0&&fire.enemyEffectTurns>=3);
        assertTrue(frost.requiresCombat&&frost.guard>0&&frost.enemyEffect.contains("Sulėt"));
        GameState state=new GameState();assertFalse(ConsumableRulesV110.useful(fire,state));state.combatActive=true;assertTrue(ConsumableRulesV110.useful(fire,state));
    }

    @Test public void fullRestorationIsNotWastedButBuffStillCanBeUsed(){
        GameState state=new GameState();state.hp=state.hpMax;state.mana=state.manaMax;state.stamina=state.staminaMax;state.aeonic=state.aeonicMax;
        assertFalse(ConsumableRulesV110.useful(ConsumableRulesV110.effect(ItemCatalogV092.byId("I092-201")),state));
        assertTrue(ConsumableRulesV110.useful(ConsumableRulesV110.effect(ItemCatalogV092.byId("I092-212")),state));
    }
}
