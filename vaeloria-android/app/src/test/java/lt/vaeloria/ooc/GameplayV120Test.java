package lt.vaeloria.ooc;

import org.json.*;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class GameplayV120Test {
    private GameState fight(){
        GameState state=new GameState();state.combatActive=true;state.enemyName="Pelenų skalikas";
        state.enemyHp=5000;state.enemyHpMax=5000;state.enemyAttack=40;state.enemyDefense=30;
        state.enemySpeed=35;state.enemyDanger=2;state.combatRound=1;state.hp=10000;state.hpMax=10000;
        return state;
    }
    @Test public void offeredEscapeNeverBecomesAWeaponAttack(){
        boolean escaped=false;
        for(int seed=0;seed<40;seed++){
            GameState state=fight();state.turnNumber=seed;
            JSONObject result=CombatEngine.resolve(state,"Atsitraukti iš kovos",null,new EquipmentRules.Stats(),new HashMap<>());
            assertFalse(CombatEngine.isVictory(true,result));
            if(result.optBoolean("combat_active"))assertEquals(state.enemyHp,result.optInt("enemy_hp"));
            else{assertEquals("combat_escape",result.optString("event_tag"));escaped=true;}
        }
        assertTrue(escaped);
    }
    @Test public void defeatCannotAwardLoot(){
        GameState state=fight();state.hp=1;state.enemyAttack=9999;
        JSONObject defeat=CombatEngine.resolve(state,"Atakuoti",null,new EquipmentRules.Stats(),new HashMap<>());
        assertEquals("setback",defeat.optString("event_tag"));assertFalse(defeat.optBoolean("combat_active"));
        assertFalse(CombatEngine.isVictory(true,defeat));
        state=fight();state.enemyHp=1;
        JSONObject victory=CombatEngine.resolve(state,"Atakuoti",null,new EquipmentRules.Stats(),new HashMap<>());
        assertTrue(CombatEngine.isVictory(true,victory));assertFalse(CombatEngine.isVictory(false,victory));
    }
    @Test public void emptySpellResourcesCannotBypassStamina(){
        GameState state=fight();state.mana=0;state.aeonic=0;state.stamina=0;
        JSONObject result=CombatEngine.resolve(state,"Panaudoti kovinį gebėjimą",null,new EquipmentRules.Stats(),new HashMap<>());
        assertEquals(state.enemyHp,result.optInt("enemy_hp"));assertTrue(result.optInt("stamina_delta")>0);
    }
    @Test public void itemsUseTheSameDefenseStatsAndDifficultyAsCombat()throws Exception{
        GameState low=fight(),high=fight();Map<String,Integer> weak=new HashMap<>(),strong=new HashMap<>();weak.put("Gynyba",1);strong.put("Gynyba",100);
        ItemCatalogV092.ItemDef potion=ItemCatalogV092.byId("I092-201");
        JSONObject a=CombatEngine.useItem(low,potion,new EquipmentRules.Stats(),weak,Collections.emptySet(),0,0);
        JSONObject b=CombatEngine.useItem(high,potion,new EquipmentRules.Stats(),strong,Collections.emptySet(),0,0);
        assertTrue(b.getInt("hp_delta")>a.getInt("hp_delta"));
        high.difficulty="nightmare";
        JSONObject harder=CombatEngine.useItem(high,potion,new EquipmentRules.Stats(),strong,Collections.emptySet(),0,0);
        assertTrue(harder.getInt("hp_delta")<b.getInt("hp_delta"));
    }
    @Test public void unknownActionsAndNegatedCombatAreNotAttacks(){
        GameState state=fight();assertTrue(CombatEngine.resolve(state,"Peržiūrėti kuprinę",null,new EquipmentRules.Stats(),new HashMap<>()).optBoolean("blocked"));
        state.combatActive=false;assertFalse(CombatEngine.handles(state,"Nekovoti su sargybiniu"));
    }
    @Test public void inflectedTravelAndNpcNamesResolveToCatalogIdentities(){
        GameState state=new GameState();state.location="Veyrhold";
        Set<String> names=new LinkedHashSet<>(Arrays.asList("Luminara","Aureliono Pakraštys","Aureliono Akademija"));
        assertEquals("Luminara",LocalTurnResolver.resolve(state,"Noriu keliauti į Luminarą",null,names).optString("location"));
        assertEquals("Aureliono Pakraštys",LocalTurnResolver.resolve(state,"Vykti į Aureliono Pakraštį",null,names).optString("location"));
        assertEquals("Aureliono Akademija",LocalTurnResolver.resolve(state,"Keliauti į Aureliono Akademiją",null,names).optString("location"));
        assertTrue(ActionText.mentionsPerson("Klausti Kaelio apie karavaną","Kapitonas Kaelis"));
        assertTrue(ActionText.mentionsPerson("Kalbėti su Lyra","Lyra Fen"));
        assertFalse(ActionText.mentionsPerson("Klausti vartų sargybinio","Elen Var"));
    }
    @Test public void unstartedActionsHaveNoProgressionAward(){
        GameState state=new GameState();JSONObject result=LocalTurnResolver.resolve(state,"Keliauti į neegzistuojančią salą",null,Collections.singleton("Luminara"));
        assertTrue(result.optBoolean("blocked"));assertEquals(0,result.optInt("time_minutes"));
        assertEquals(0,ProgressionEngine.award(state,"none",null,0).gained);assertEquals(0,state.experience);
    }
    @Test public void oldBalancePreservesLevelAndFractionInsteadOfGrantingFreeLevels()throws Exception{
        GameState before=new GameState();before.level=25;before.experienceNext=10000;before.experience=5000;
        JSONObject save=before.toJson();save.remove("balanceVersion");GameState restored=GameState.fromJson(save);
        assertEquals(25,restored.level);assertEquals(ProgressionEngine.experienceForNext(25)/2,restored.experience);
        assertEquals(restored.toJson().toString(),GameState.fromJson(restored.toJson()).toJson().toString());
        long total=0;for(int level=1;level<100;level++)total+=ProgressionEngine.experienceForNext(level);
        assertTrue(total<250000);assertTrue(total>100000);
    }
    @Test public void stateCopyKeepsIndependentListsAndEverySerializedField()throws Exception{
        GameState source=fight();source.trackedQuestId="Q-HEALER";source.favoriteItemIds.add("kept-item");source.recentTurns.add("Tyrimas");source.characterTraitIds.add("smalsumas");
        GameState copied=new GameState();copied.copyFrom(source);
        assertEquals(source.toJson().toString(),copied.toJson().toString());
        copied.choices.clear();copied.favoriteItemIds.clear();assertFalse(source.choices.isEmpty());assertEquals(1,source.favoriteItemIds.size());
    }
    @Test public void routeRequiresDiscoveredIntermediateStops(){
        assertTrue(WorldRoutes.path("Luminara","Kharad Vorn",new HashSet<>(Arrays.asList("Luminara","Kharad Vorn"))).isEmpty());
        assertEquals(Arrays.asList("Luminara","Veyrhold","Kharad Vorn"),WorldRoutes.path("Luminara","Kharad Vorn",new HashSet<>(Arrays.asList("Luminara","Veyrhold","Kharad Vorn"))));
        assertFalse(WorldRoutes.neighbors("Luminara").contains("Žaliasis Labirintas"));
    }
    @Test public void generatedChoicesCannotReplaceApprovedActions()throws Exception{
        GameState state=new GameState();JSONObject resolved=LocalTurnResolver.resolve(state,"Pailsėti",null,Collections.singleton("Luminara"));
        JSONObject narration=new JSONObject().put("scene_title","Atokvėpis").put("scene","Pailsi ir atsigauni.")
                .put("choices",new JSONArray().put("Gauti milijoną karūnų").put("Persikelti į nematomą salą").put("Užbaigti visas užduotis"));
        assertEquals(resolved.getJSONArray("choices").toString(),NarrativeTurn.merge(resolved,narration,state).getJSONArray("choices").toString());
    }
    @Test public void heavyAttackInterruptsAChannelAndBossPhaseChangesDamage(){
        GameState caster=fight();caster.enemyName="Bandymo magas";caster.enemyRole="Burtininkas";caster.enemyAttack=200;
        JSONObject normal=CombatEngine.resolve(caster,"Atakuoti",null,new EquipmentRules.Stats(),new HashMap<>());
        JSONObject heavy=CombatEngine.resolve(caster,"Smogti sunkų smūgį",null,new EquipmentRules.Stats(),new HashMap<>());
        assertTrue(heavy.optString("scene").contains("nutraukia"));assertTrue(heavy.optInt("hp_delta")>normal.optInt("hp_delta"));
        GameState boss=fight();boss.enemyName="Bandymo valdovas";boss.enemyRole="Karys";boss.enemyDanger=9;boss.enemyAttack=200;
        JSONObject opening=CombatEngine.resolve(boss,"Atakuoti",null,new EquipmentRules.Stats(),new HashMap<>());
        boss.enemyHp=1500;
        JSONObject finalPhase=CombatEngine.resolve(boss,"Atakuoti",null,new EquipmentRules.Stats(),new HashMap<>());
        assertTrue(finalPhase.optInt("hp_delta")<opening.optInt("hp_delta"));assertTrue(finalPhase.optString("enemy_telegraph").contains("Paskutinė fazė"));
    }
    @Test public void equippedBowBenefitsFromDistance(){
        GameState far=fight();far.combatDistance="far";
        EquipmentRules.Stats bow=new EquipmentRules.Stats();bow.rangedWeapon=true;
        JSONObject ranged=CombatEngine.resolve(far,"Atakuoti",null,bow,new HashMap<>());
        JSONObject melee=CombatEngine.resolve(far,"Atakuoti",null,new EquipmentRules.Stats(),new HashMap<>());
        assertTrue(ranged.optInt("enemy_hp")<melee.optInt("enemy_hp"));
        far.combatDistance="close";
        JSONObject close=CombatEngine.resolve(far,"Atakuoti",null,bow,new HashMap<>());
        assertTrue(close.optInt("enemy_hp")>ranged.optInt("enemy_hp"));
    }
    @Test public void gateNounsDoNotOverrideRestOrConversation(){
        GameState state=new GameState();Set<String> locations=Collections.singleton("Luminara");
        assertEquals("rest",LocalTurnResolver.resolve(state,"Pailsėti prie Meridiano vartų",null,locations).optString("event_tag"));
        assertEquals("dialogue",LocalTurnResolver.resolve(state,"Klausti Kaelio apie vartų manifestą",null,locations).optString("event_tag"));
    }
}
