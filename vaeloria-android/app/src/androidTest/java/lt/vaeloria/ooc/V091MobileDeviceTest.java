package lt.vaeloria.ooc;

import android.test.ActivityInstrumentationTestCase2;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import org.json.JSONObject;

@SuppressWarnings("deprecation")
public class V091MobileDeviceTest extends ActivityInstrumentationTestCase2<PolishedActivity> {
    private PolishedActivity activity;

    public V091MobileDeviceTest() {
        super(PolishedActivity.class);
    }

    @Override protected void setUp() throws Exception {
        super.setUp();
        getInstrumentation().getTargetContext().getSharedPreferences("vaeloria_visual", 0)
                .edit().putBoolean("animations", false).commit();
        activity = getActivity();
        getInstrumentation().waitForIdleSync();
    }

    public void testAllPrimaryScreensFitAReal360DpPhone() throws Exception {
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        float widthDp = metrics.widthPixels / metrics.density;
        assertTrue("Emulator must exercise a 360dp viewport, got " + widthDp,
                widthDp >= 355f && widthDp <= 365f);

        for (String screen : new String[]{"game", "hero", "items", "map", "journal", "settings"}) {
            show(screen);
            View root = activity.getWindow().getDecorView();
            assertNoHorizontalScroll(root, screen);
            assertCriticalTapTargets(root, metrics.density, screen);
            assertTextLayoutsAreNotEllipsized(root, screen);
        }
    }

    public void testLocalResolverStartsIllustratedEnemyAndPersistsCombat() throws Exception {
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
    }

    private void show(String screen) {
        getInstrumentation().runOnMainSync(() -> activity.show(screen));
        getInstrumentation().waitForIdleSync();
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
