package lt.vaeloria.ooc;

import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class V091MobileDeviceTest {
    private ActivityScenario<PolishedActivity> scenario;

    @Before public void setUp() {
        InstrumentationRegistry.getInstrumentation().getTargetContext().deleteDatabase("vaeloria.db");
        InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getSharedPreferences("vaeloria_visual", 0)
                .edit().putBoolean("animations", false).putBoolean("large_text",false).commit();
        scenario = ActivityScenario.launch(PolishedActivity.class);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @After public void tearDown() {
        if (scenario != null) scenario.close();
    }

    @Test public void allPrimaryScreensFitAReal360DpPhone() {
        scenario.onActivity(this::createProfile);
        for (String screen : new String[]{"character", "game", "hero", "items", "map", "journal", "settings"}) {
            scenario.onActivity(activity -> activity.show(screen));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
                float widthDp = metrics.widthPixels / metrics.density;
                assertTrue("Emulator must exercise a 360dp viewport, got " + widthDp,
                        widthDp >= 355f && widthDp <= 365f);
                View root = activity.getWindow().getDecorView();
                assertFalse(screen+" failed to render",containsText(root,"Ekrano klaida"));
                assertNoHorizontalScroll(root, screen);
                assertCriticalTapTargets(root, metrics.density, screen);
                assertTextLayoutsAreNotEllipsized(root, screen);
            });
        }
    }

    @Test public void freshInstallStartsWithCharacterCreatorAndProfilePersists() {
        scenario.onActivity(activity -> {
            assertEquals("character",activity.screen);
            assertTrue(containsText(activity.getWindow().getDecorView(),"Sukurk savo veikėją"));
            createProfile(activity);
            GameState restored=activity.db.loadState();
            assertTrue(restored.characterCreated);assertEquals("Austėja",restored.characterName);assertEquals("akademija",restored.characterOriginId);assertEquals("arkanistas",restored.characterArchetypeId);assertEquals(3,restored.characterTraitIds.size());
        });
    }

    @Test public void localResolverStartsIllustratedEnemyAndPersistsCombat() {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        scenario.onActivity(activity -> {
            try {
                activity.state.location = "Luminara";
                JSONObject result = activity.local("Kovoti su Pašvaistės drakonu", null);
                assertTrue(result.getBoolean("combat_active"));
                String enemyName = result.getString("enemy_name");
                EnemyCatalogV091.Enemy enemy = EnemyCatalogV091.find(enemyName);
                assertNotNull("Local resolver must select one of the 200 new illustrated enemies", enemy);
                assertEquals(enemy.artwork, VisualAssetCatalog.monsterFor(enemyName));

                activity.state.applyTurn(result);
                activity.db.saveState(activity.state);
                GameState restored = activity.db.loadState();
                assertTrue(restored.combatActive);
                assertEquals(enemyName, restored.enemyName);
                assertFalse(restored.enemyTelegraph.isEmpty());
                assertTrue(restored.enemyHp > 0);
                assertEquals(restored.enemyHp, restored.enemyHpMax);
            } catch (Throwable throwable) {
                failure.set(throwable);
            }
        });
        if (failure.get() != null) throw new AssertionError(failure.get());
    }

    @Test public void itemCodexPotionsAndAuthoritativeWorldBossDropsWork() {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        scenario.onActivity(activity -> {
            try {
                createProfile(activity);
                activity.show("items");
                assertTrue(containsText(activity.getWindow().getDecorView(),"DAIKTŲ KODEKSAS · 325"));
                VaeloriaDb.Item potion=null;for(VaeloriaDb.Item item:activity.db.getItems())if("I092-201".equals(item.catalogId)){potion=item;break;}
                assertNotNull(potion);activity.state.hp=50;String used=activity.db.consumeItem(potion.id,activity.state);assertNotNull(used);assertEquals(75,activity.state.hp);assertEquals(2,activity.db.getItem(potion.id).quantity);

                activity.state.combatActive=true;activity.state.enemyName="Užtemimo drakonas";activity.state.enemyHp=1;activity.state.enemyHpMax=559;activity.state.combatRound=3;
                int before=units(activity.db.getItems());OpenAiSettings.select(activity,OpenAiSettings.LOCAL);activity.db.saveState(activity.state);activity.act("Atakuoti Užtemimo drakoną");int after=units(activity.db.getItems());assertEquals(before+3,after);assertFalse(activity.state.combatActive);
            } catch (Throwable throwable) { failure.set(throwable); }
        });
        if (failure.get() != null) throw new AssertionError(failure.get());
    }

    @Test public void largeTextInventoryCardsAreMeasuredAndFitTheirContents(){
        scenario.onActivity(activity->{createProfile(activity);activity.getSharedPreferences("vaeloria_visual",0).edit().putBoolean("large_text",true).commit();activity.show("items");});
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        scenario.onActivity(activity->{
            View root=activity.getWindow().getDecorView();
            assertTrue(assertInventoryCards(root,activity.getResources().getDisplayMetrics().density)>0);
        });
    }

    private int assertInventoryCards(View view,float density){
        int found=0;
        if(view instanceof ViewGroup){
            ViewGroup group=(ViewGroup)view;
            if(group.getChildCount()>0&&group.getChildAt(0) instanceof ItemArtView&&view.isClickable()){
                assertTrue("Large-text item must use a full-width card",view.getWidth()/density>250);
                for(int i=1;i<group.getChildCount();i++){
                    View child=group.getChildAt(i);assertTrue("Card clips its contents",child.getBottom()<=view.getHeight()-view.getPaddingBottom());
                    if(child instanceof TextView){TextView text=(TextView)child;assertNotNull(text.getLayout());assertTrue("Card clips text",text.getLayout().getHeight()<=text.getHeight()-text.getCompoundPaddingTop()-text.getCompoundPaddingBottom());}
                }
                found++;
            }
            for(int i=0;i<group.getChildCount();i++)found+=assertInventoryCards(group.getChildAt(i),density);
        }
        return found;
    }

    private int units(java.util.List<VaeloriaDb.Item> items){int total=0;for(VaeloriaDb.Item item:items)total+=item.quantity;return total;}

    private void createProfile(PolishedActivity activity){
        if(activity.state.characterCreated)return;
        assertTrue(activity.applyCharacterProfile("Austėja",27,false,"moteris","Sidabrinis apsiaustas","akademija","arkanistas",Arrays.asList("smalsumas","drausme","atjauta")));
        activity.show("game");
    }

    private boolean containsText(View view,String text){if(view instanceof TextView&&((TextView)view).getText().toString().contains(text))return true;if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int index=0;index<group.getChildCount();index++)if(containsText(group.getChildAt(index),text))return true;}return false;}

    private void assertNoHorizontalScroll(View view, String screen) {
        assertFalse(screen + " contains forbidden horizontal scrolling: " + view.getClass().getName(),
                view instanceof HorizontalScrollView);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                assertNoHorizontalScroll(group.getChildAt(index), screen);
            }
        }
    }

    private void assertCriticalTapTargets(View view, float density, String screen) {
        if (view.getVisibility() == View.VISIBLE && view.isEnabled() && view.isClickable()
                && view.getWidth() > 0 && view.getHeight() > 0) {
            float width = view.getWidth() / density;
            float height = view.getHeight() / density;
            assertTrue(screen + " tap target narrower than 44dp: " + describe(view) + " = " + width,
                    width >= 43.5f);
            assertTrue(screen + " tap target shorter than 44dp: " + describe(view) + " = " + height,
                    height >= 43.5f);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                assertCriticalTapTargets(group.getChildAt(index), density, screen);
            }
        }
    }

    private void assertTextLayoutsAreNotEllipsized(View view, String screen) {
        if (view instanceof TextView && view.getVisibility() == View.VISIBLE) {
            TextView textView = (TextView) view;
            Layout layout = textView.getLayout();
            if (layout != null && textView.getText() != null && textView.getText().length() > 0) {
                for (int line = 0; line < layout.getLineCount(); line++) {
                    assertEquals(screen + " ellipsized text: " + textView.getText(),
                            0, layout.getEllipsisCount(line));
                }
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                assertTextLayoutsAreNotEllipsized(group.getChildAt(index), screen);
            }
        }
    }

    private String describe(View view) {
        CharSequence description = view.getContentDescription();
        if (description != null && description.length() > 0) return description.toString();
        if (view instanceof TextView) return ((TextView) view).getText().toString();
        return view.getClass().getSimpleName();
    }
}
