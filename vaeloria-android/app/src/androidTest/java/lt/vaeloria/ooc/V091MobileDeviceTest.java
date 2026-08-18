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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class V091MobileDeviceTest {
    private ActivityScenario<PolishedActivity> scenario;

    @Before public void setUp() {
        InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getSharedPreferences("vaeloria_visual", 0)
                .edit().putBoolean("animations", false).commit();
        scenario = ActivityScenario.launch(PolishedActivity.class);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @After public void tearDown() {
        if (scenario != null) scenario.close();
    }

    @Test public void allPrimaryScreensFitAReal360DpPhone() {
        for (String screen : new String[]{"game", "hero", "items", "map", "journal", "settings"}) {
            scenario.onActivity(activity -> {
                DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
                float widthDp = metrics.widthPixels / metrics.density;
                assertTrue("Emulator must exercise a 360dp viewport, got " + widthDp,
                        widthDp >= 355f && widthDp <= 365f);
                activity.show(screen);
                View root = activity.getWindow().getDecorView();
                assertNoHorizontalScroll(root, screen);
                assertCriticalTapTargets(root, metrics.density, screen);
                assertTextLayoutsAreNotEllipsized(root, screen);
            });
        }
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
            } catch (Throwable throwable) {
                failure.set(throwable);
            }
        });
        if (failure.get() != null) throw new AssertionError(failure.get());
    }

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
