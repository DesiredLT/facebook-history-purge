package lt.vaeloria.ooc;

import android.content.Context;
import android.database.Cursor;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class GameplayV120DeviceTest {
    private Context context;private VaeloriaDb db;
    @Before public void prepare(){
        context=ApplicationProvider.getApplicationContext();context.deleteDatabase("vaeloria.db");
        OpenAiSettings.select(context,OpenAiSettings.LOCAL);context.getSharedPreferences("vaeloria_visual",0).edit().putBoolean("animations",false).putBoolean("large_text",false).commit();
        db=new VaeloriaDb(context);db.getWritableDatabase();
    }
    @After public void close(){if(db!=null)db.close();}
    private GameState player(){GameState state=db.loadState();state.characterCreated=true;state.worldMinute=720;state.location="Luminara";db.saveState(state);return state;}
    private StatEngine.Check success(){StatEngine.Check c=new StatEngine.Check();c.outcome="sėkmė";return c;}
    private String items()throws Exception{return new JSONObject(db.exportSave()).getJSONArray("items").toString();}

    @Test public void actualActivityDefeatAndBlockedTravelNeverAwardLootOrProgress(){
        db.close();db=null;
        try(ActivityScenario<PolishedActivity> scenario=ActivityScenario.launch(PolishedActivity.class)){
            scenario.onActivity(a->{try{
                assertTrue(a.applyCharacterProfile("Austėja",27,false,"moteris","Sidabrinis apsiaustas","akademija","arkanistas",Arrays.asList("smalsumas","drausme","atjauta")));
                String before=a.db.exportSave();a.act("Keliauti į neegzistuojančią salą");assertEquals(before,a.db.exportSave());
                a.act("Pradėti kovą su Pelenų skaliku");assertTrue(a.state.combatActive);
                a.state.hp=1;a.state.enemyHp=9999;a.state.enemyHpMax=9999;a.state.enemyAttack=9999;a.state.enemyDefense=9999;a.db.saveState(a.state);
                String inventory=new JSONObject(a.db.exportSave()).getJSONArray("items").toString();
                a.act("Atakuoti ir išlaikyti spaudimą");assertFalse(a.state.combatActive);assertEquals(1,a.state.hp);
                assertEquals(inventory,new JSONObject(a.db.exportSave()).getJSONArray("items").toString());
            }catch(Exception error){throw new AssertionError(error);}});
        }
    }

    @Test public void itemFailureRollsBackInventoryRewardsWorldAndCallerState()throws Exception{
        GameState state=player();state.hp=50;db.addCatalogLoot(ItemCatalogV092.byId("I092-201"),2);db.saveState(state);
        VaeloriaDb.Item potion=null;for(VaeloriaDb.Item item:db.getItems())if("I092-201".equals(item.catalogId))potion=item;assertNotNull(potion);
        String before=db.exportSave(),caller=state.toJson().toString();
        db.getWritableDatabase().execSQL("CREATE TRIGGER fail_consumable BEFORE UPDATE ON state BEGIN SELECT RAISE(ABORT,'test'); END");
        try{assertTrue(db.consumeItem(potion.id,state).contains("nepavyko"));}
        finally{db.getWritableDatabase().execSQL("DROP TRIGGER fail_consumable");}
        assertEquals(before,db.exportSave());assertEquals(caller,state.toJson().toString());
        assertTrue(db.consumeItem(potion.id,state).contains("Panaudota"));assertEquals(75,state.hp);assertTrue(db.undo());assertEquals(before,db.exportSave());
    }

    @Test public void everySideQuestHasACompleteOneTimeRewardCycle()throws Exception{
        for(SideQuestCatalog.QuestDef def:SideQuestCatalog.ALL){
            db.reset();GameState state=player();
            assertTrue(def.id,db.sideQuests().accept(def.id,state).ok);assertFalse(db.sideQuests().accept(def.id,state).ok);
            assertTrue(db.world().isDiscovered(def.location));state.location=def.location;
            db.sideQuests().record("Keliauti","travel",state,success());assertEquals(1,db.sideQuests().quest(def.id).stage);
            db.sideQuests().record(def.action,"setback",state,success());assertEquals("active",db.sideQuests().quest(def.id).status);
            for(int i=0;i<def.target;i++)db.sideQuests().record(def.action,"combat".equals(def.kind)?"combat_victory":"discovery",state,success());
            assertEquals("ready",db.sideQuests().quest(def.id).status);assertFalse(db.sideQuests().claim(def.id,state).ok);
            state.location="Luminara";db.saveState(state);long crowns=state.crowns;assertTrue(def.id,db.sideQuests().claim(def.id,state).ok);
            assertEquals(crowns+def.gold,state.crowns);assertEquals("completed",db.sideQuests().quest(def.id).status);
            String after=db.exportSave();assertFalse(db.sideQuests().claim(def.id,state).ok);assertEquals(after,db.exportSave());
            assertTrue(db.undo());state=db.loadState();assertEquals("ready",db.sideQuests().quest(def.id).status);assertTrue(db.sideQuests().claim(def.id,state).ok);
        }
    }

    @Test public void allThreeOfferedStoryEndingsCompleteWithVerifiedEvidence()throws Exception{
        for(int ending=0;ending<3;ending++){
            db.reset();GameState state=player();storyTurn(state,"Ištirti karavano manifestą ir jo laiko žymas");storyTurn(state,"Patikrinti Meridiano vartų žurnalą");
            assertEquals(2,db.world().activeMainStep().position);
            state.location="Veyrhold";assertEquals("",db.world().progressStory("Užfiksuoti Lyros lauko matavimus kaip atramos tašką","discovery",state,success()));
            state.location="Luminara";storyTurn(state,"Užfiksuoti Lyros lauko matavimus kaip atramos tašką");storyTurn(state,"Patvirtinti Kaelio sargybos žurnalą kaip atramos tašką");
            state.location="Veyrhold";storyTurn(state,"Patikrinti Veyrhold tranzito įrašą");assertEquals(3,db.world().activeMainStep().position);assertTrue(db.world().isDiscovered("Aureliono Pakraštys"));
            state.location="Aureliono Pakraštys";storyTurn(state,"Pereiti Meridianą ir užmegzti Orisono kontaktą");
            state.location="Luminara";db.world().applyStructuredChoices(state);String choice=state.choices.get(ending);storyTurn(state,choice);
            assertEquals(new String[]{"priimta","atmesta","nepriklausoma"}[ending],state.storyEnding);
            db.world().applyStructuredChoices(state);storyTurn(state,state.choices.get(0));assertNull(db.world().activeMainStep());assertEquals("completed",db.world().activeMainQuest().status);
        }
    }
    private void storyTurn(GameState state,String action)throws Exception{
        long minute=state.worldMinute;JSONObject result=LocalTurnResolver.resolve(state,action,success(),db.world().discoveredLocations());
        result=AiTurnPolicyV101.sanitize(result,state,success(),action,db.world().discoveredLocations(),false);db.world().validateAction(state,action,result);
        assertFalse(action,result.optBoolean("blocked"));state.applyTurn(result);
        assertFalse(action,db.world().progressStory(action,result.optString("event_tag"),state,success(),minute).isEmpty());db.saveState(state);
    }

    @Test public void worldPricesUseElapsedTimeAndRemoteNamesDoNotDiscoverPlaces()throws Exception{
        GameState state=player();db.getWritableDatabase().execSQL("UPDATE economy SET price_index=130,updated_minute=720");
        assertEquals(130,db.world().priceIndex("Luminara"));
        for(int i=0;i<20;i++)db.world().advanceWorld(state,"none",0);assertEquals(130,db.world().priceIndex("Luminara"));
        state.worldMinute=840;db.world().advanceWorld(state,"travel",120);assertEquals(128,db.world().priceIndex("Luminara"));
        db.world().recordExploration("Ištirti kryptį į Žaliąjį Labirintą",state);assertFalse(db.world().isDiscovered("Žaliasis Labirintas"));
        db.world().recordExploration("Ištirti kryptį į Stiklo Girią",state);assertTrue(db.world().isDiscovered("Stiklo Giria"));
    }

    @Test public void trackedQuestAndFavoriteSurviveExportAndCannotBeSold()throws Exception{
        GameState state=player();assertTrue(db.sideQuests().accept("Q-HEALER",state).ok);
        db.addCatalogLoot(ItemCatalogV092.byId("I092-201"),1);VaeloriaDb.Item selected=null;for(VaeloriaDb.Item item:db.getItems())if("I092-201".equals(item.catalogId))selected=item;
        assertNotNull(selected);state.favoriteItemIds.add(selected.id);db.saveState(state);String before=items();
        WorldRepository.Shop shop=db.world().shopForNpc("Elen Var");assertNotNull(shop);assertFalse(db.world().sell(shop,selected.id,state).ok);assertEquals(before,items());
        String save=db.exportSave();assertTrue(db.importSave(save));state=db.loadState();assertEquals("Q-HEALER",state.trackedQuestId);assertTrue(state.favoriteItemIds.contains(selected.id));
    }
    @Test public void trackedQuestCannotReplaceCombatActionsEvenWhenUsingAPotion()throws Exception{
        GameState state=player();assertTrue(db.sideQuests().accept("Q-SMITH",state).ok);
        state.applyTurn(CombatEngine.resolve(state,"Pradėti kovą",null,db.equipmentStats(),db.getStatValues()));
        state.hp=50;state.enemyHp=9999;state.enemyHpMax=9999;state.enemyAttack=1;db.saveState(state);
        List<String> combatChoices=new ArrayList<>(state.choices);db.world().applyQuestToState(state);db.world().applyStructuredChoices(state);
        assertEquals(combatChoices,state.choices);
        db.addCatalogLoot(ItemCatalogV092.byId("I092-201"),1);VaeloriaDb.Item potion=null;
        for(VaeloriaDb.Item item:db.getItems())if("I092-201".equals(item.catalogId))potion=item;
        assertNotNull(potion);assertTrue(db.consumeItem(potion.id,state).contains("Panaudota"));assertTrue(state.combatActive);
        for(String choice:state.choices)assertFalse(choice,ActionText.travel(choice));
        assertTrue(ActionText.contains(state.choices.get(0),"ataku","kvap"));
    }
}
