package lt.vaeloria.ooc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Vaeloria v0.9.1 scene renderer. Only the selected bundled bitmap is decoded. */
class SceneV090View extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Bitmap artwork;
    private int artworkId;
    private String location = "Luminara";
    private String scene = "";

    SceneV090View(Context context) {
        super(context);
        setFocusable(true);
    }

    void setScene(String nextLocation, String nextScene) {
        location = nextLocation == null ? "Luminara" : nextLocation;
        scene = nextScene == null ? "" : nextScene;
        int nextId = VisualAssetCatalog.sceneFor(location, scene, false);
        if (nextId != artworkId) {
            releaseArtwork();
            artworkId = nextId;
            artwork = BitmapFactory.decodeResource(getResources(), nextId);
        }
        setContentDescription(location + ". " + scene);
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        canvas.drawColor(Color.rgb(3, 9, 14));
        drawCover(canvas, artwork, width, height, paint);

        paint.setShader(new LinearGradient(
                0, 0, 0, height,
                new int[]{Color.argb(10, 0, 0, 0), Color.argb(20, 0, 0, 0), Color.argb(238, 2, 7, 11)},
                null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);

        paint.setColor(Color.argb(185, 3, 10, 15));
        canvas.drawRoundRect(new RectF(dp(12), dp(12), dp(104), dp(40)), dp(14), dp(14), paint);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(8));
        paint.setColor(Color.rgb(221, 187, 104));
        canvas.drawText("GYVA SCENA", dp(23), dp(30), paint);

        String title = location.toUpperCase(Locale.forLanguageTag("lt-LT"));
        paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        paint.setColor(Color.rgb(245, 229, 194));
        paint.setShadowLayer(dp(5), 0, dp(2), Color.BLACK);
        drawAdaptiveLine(canvas, title, dp(16), height - dp(45), width - dp(32), 25f, 12f);
        paint.clearShadowLayer();

        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(Color.rgb(197, 209, 209));
        drawAdaptiveLine(canvas, scene, dp(17), height - dp(20), width - dp(34), 10f, 7f);
    }

    @Override protected void onDetachedFromWindow() {
        releaseArtwork();
        super.onDetachedFromWindow();
    }

    private void releaseArtwork() {
        if (artwork != null && !artwork.isRecycled()) artwork.recycle();
        artwork = null;
    }

    private void drawAdaptiveLine(Canvas canvas, String value, float x, float baseline,
                                  float maxWidth, float preferredSp, float minimumSp) {
        String text = value == null ? "" : value;
        float density = getResources().getDisplayMetrics().scaledDensity;
        float size = preferredSp;
        paint.setTextSize(size * density);
        while (size > minimumSp && paint.measureText(text) > maxWidth) {
            size -= .5f;
            paint.setTextSize(size * density);
        }
        canvas.drawText(text, x, baseline, paint);
    }

    static void drawCover(Canvas canvas, Bitmap bitmap, int width, int height, Paint paint) {
        if (bitmap == null || bitmap.isRecycled()) return;
        float scale = Math.max(width / (float) bitmap.getWidth(), height / (float) bitmap.getHeight());
        int sourceWidth = Math.round(width / scale);
        int sourceHeight = Math.round(height / scale);
        int left = Math.max(0, (bitmap.getWidth() - sourceWidth) / 2);
        int top = Math.max(0, (bitmap.getHeight() - sourceHeight) / 2);
        Rect source = new Rect(left, top,
                Math.min(bitmap.getWidth(), left + sourceWidth),
                Math.min(bitmap.getHeight(), top + sourceHeight));
        canvas.drawBitmap(bitmap, source, new RectF(0, 0, width, height), paint);
    }

    int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

/** Real bundled item artwork with a rarity frame. */
class ItemArtView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Bitmap artwork;
    private int artworkId;
    private int accent = Color.rgb(111, 137, 148);

    ItemArtView(Context context) {
        super(context);
    }

    void setItem(String name, String category, String rarity) {
        int nextId = VisualAssetCatalog.itemFor(name, category);
        if (nextId != artworkId) {
            releaseArtwork();
            artworkId = nextId;
            artwork = BitmapFactory.decodeResource(getResources(), nextId);
        }
        accent = rarityColor(rarity);
        setContentDescription(name);
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        float radius = dp(12);
        Path clip = new Path();
        clip.addRoundRect(new RectF(0, 0, width, height), radius, radius, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clip);
        canvas.drawColor(Color.rgb(7, 12, 16));
        SceneV090View.drawCover(canvas, artwork, width, height, paint);
        paint.setShader(new LinearGradient(0, height * 0.55f, 0, height,
                Color.TRANSPARENT, Color.argb(105, 0, 0, 0), Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);
        canvas.restore();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.5f));
        paint.setColor(accent);
        canvas.drawRoundRect(new RectF(dp(1), dp(1), width - dp(1), height - dp(1)), radius, radius, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override protected void onDetachedFromWindow() {
        releaseArtwork();
        super.onDetachedFromWindow();
    }

    private void releaseArtwork() {
        if (artwork != null && !artwork.isRecycled()) artwork.recycle();
        artwork = null;
    }

    private int rarityColor(String rarity) {
        String value = rarity == null ? "" : rarity.toLowerCase(Locale.ROOT);
        if (value.contains("legend")) return Color.rgb(225, 190, 94);
        if (value.contains("ancient")) return Color.rgb(175, 116, 218);
        if (value.contains("unique")) return Color.rgb(83, 184, 189);
        if (value.contains("rare")) return Color.rgb(87, 147, 211);
        return Color.rgb(111, 137, 148);
    }

    int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

/** Individual bundled NPC portrait. */
class NpcArtView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Bitmap artwork;

    NpcArtView(Context context) {
        super(context);
    }

    void setNpc(String name) {
        releaseArtwork();
        artwork = BitmapFactory.decodeResource(getResources(), VisualAssetCatalog.npcFor(name));
        setContentDescription(name);
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        float radius = dp(12);
        Path clip = new Path();
        clip.addRoundRect(new RectF(0, 0, width, height), radius, radius, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clip);
        SceneV090View.drawCover(canvas, artwork, width, height, paint);
        canvas.restore();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.argb(180, 221, 187, 104));
        canvas.drawRoundRect(new RectF(dp(1), dp(1), width - dp(1), height - dp(1)), radius, radius, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override protected void onDetachedFromWindow() {
        releaseArtwork();
        super.onDetachedFromWindow();
    }

    private void releaseArtwork() {
        if (artwork != null && !artwork.isRecycled()) artwork.recycle();
        artwork = null;
    }

    int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

/** One lazily decoded bestiary portrait. */
class MonsterArtView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Bitmap artwork;

    MonsterArtView(Context context) {
        super(context);
    }

    void setMonster(String name) {
        releaseArtwork();
        artwork = BitmapFactory.decodeResource(getResources(), VisualAssetCatalog.monsterFor(name));
        setContentDescription(name);
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        float radius = dp(12);
        Path clip = new Path();
        clip.addRoundRect(new RectF(0, 0, width, height), radius, radius, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clip);
        canvas.drawColor(Color.rgb(3, 8, 12));
        SceneV090View.drawCover(canvas, artwork, width, height, paint);
        paint.setShader(new LinearGradient(0, height * .55f, 0, height,
                Color.TRANSPARENT, Color.argb(145, 0, 0, 0), Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);
        canvas.restore();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.5f));
        paint.setColor(Color.rgb(190, 76, 67));
        canvas.drawRoundRect(new RectF(dp(1), dp(1), width - dp(1), height - dp(1)), radius, radius, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override protected void onDetachedFromWindow() {
        releaseArtwork();
        super.onDetachedFromWindow();
    }

    private void releaseArtwork() {
        if (artwork != null && !artwork.isRecycled()) artwork.recycle();
        artwork = null;
    }

    int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

/** Combat presentation with a real portrait selected from the active enemy name. */
class CombatV090View extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap background;
    private Bitmap enemyArtwork;
    private int enemyArtworkId;
    private GameState state;

    CombatV090View(Context context) {
        super(context);
        background = BitmapFactory.decodeResource(getResources(), R.drawable.scene_combat_v090);
    }

    void setState(GameState nextState) {
        state = nextState;
        String enemyName = state == null ? "" : state.enemyName;
        int nextId = VisualAssetCatalog.monsterFor(enemyName);
        if (nextId != enemyArtworkId) {
            releaseEnemy();
            enemyArtworkId = nextId;
            enemyArtwork = BitmapFactory.decodeResource(getResources(), nextId);
        }
        setContentDescription("Kova su " + safe(enemyName, "priešu"));
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        if (state == null) return;
        int width = getWidth();
        int height = getHeight();
        SceneV090View.drawCover(canvas, background, width, height, paint);
        paint.setShader(new LinearGradient(0, 0, width, 0,
                Color.argb(235, 2, 7, 11), Color.argb(55, 2, 7, 11), Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);

        RectF enemyBox = new RectF(width * .42f, dp(38), width - dp(12), height - dp(140));
        Path clip = new Path();
        clip.addRoundRect(enemyBox, dp(14), dp(14), Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clip);
        canvas.translate(enemyBox.left, enemyBox.top);
        SceneV090View.drawCover(canvas, enemyArtwork,
                Math.max(1, (int) enemyBox.width()), Math.max(1, (int) enemyBox.height()), paint);
        canvas.restore();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.5f));
        paint.setColor(Color.argb(220, 203, 78, 67));
        canvas.drawRoundRect(enemyBox, dp(14), dp(14), paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        paint.setTextSize(dp(13));
        paint.setColor(Color.rgb(229, 194, 112));
        canvas.drawText("EINORAS", dp(14), dp(25), paint);

        String enemyName = safe(state.enemyName, "Priešas").toUpperCase(Locale.forLanguageTag("lt-LT"));
        paint.setColor(Color.rgb(229, 104, 89));
        drawAdaptiveLine(canvas, enemyName, dp(14), dp(51), width - dp(30), 12f, 7f);

        String distance = "close".equals(state.combatDistance) ? "ARTIMAS"
                : "far".equals(state.combatDistance) ? "TOLIMAS" : "VIDUTINIS";
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(8));
        paint.setColor(Color.rgb(195, 207, 207));
        canvas.drawText("ATSTUMAS · " + distance, dp(14), dp(73), paint);
        canvas.drawText("KOVOS LAUKAS", dp(14), dp(92), paint);

        RectF panel = new RectF(dp(12), height - dp(132), width - dp(12), height - dp(10));
        paint.setColor(Color.argb(232, 4, 12, 17));
        canvas.drawRoundRect(panel, dp(12), dp(12), paint);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(8));
        paint.setColor(Color.rgb(232, 150, 91));
        canvas.drawText("PRIEŠO KETINIMAS", panel.left + dp(10), panel.top + dp(17), paint);
        float textLeft = panel.left + dp(10);
        float textWidth = panel.width() - dp(20);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setColor(Color.rgb(236, 238, 231));
        float next = drawWrapped(canvas, safe(state.enemyTelegraph, "Ketinimas dar neaiškus"),
                textLeft, panel.top + dp(37), textWidth, 9.5f, 7f, 2);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(Color.rgb(182, 198, 194));
        next = drawWrapped(canvas, "BŪSENA · " + safe(state.enemyStatus, "Nežinoma"),
                textLeft, next + dp(14), textWidth, 8f, 6.5f, 2);
        paint.setColor(Color.rgb(221, 187, 104));
        drawWrapped(canvas, "APLINKA · " + safe(state.combatHazard, "Stabili"),
                textLeft, next + dp(13), textWidth, 8f, 6.5f, 2);
    }

    @Override protected void onDetachedFromWindow() {
        releaseEnemy();
        if (background != null && !background.isRecycled()) background.recycle();
        super.onDetachedFromWindow();
    }

    private void releaseEnemy() {
        if (enemyArtwork != null && !enemyArtwork.isRecycled()) enemyArtwork.recycle();
        enemyArtwork = null;
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private void drawAdaptiveLine(Canvas canvas, String value, float x, float baseline,
                                  float maxWidth, float preferredSp, float minimumSp) {
        float density = getResources().getDisplayMetrics().scaledDensity;
        float size = preferredSp;
        paint.setTextSize(size * density);
        while (size > minimumSp && paint.measureText(value) > maxWidth) {
            size -= .5f;
            paint.setTextSize(size * density);
        }
        canvas.drawText(value, x, baseline, paint);
    }

    private float drawWrapped(Canvas canvas, String value, float x, float baseline, float maxWidth,
                              float preferredSp, float minimumSp, int targetLines) {
        float density = getResources().getDisplayMetrics().scaledDensity;
        float size = preferredSp;
        List<String> lines;
        do {
            paint.setTextSize(size * density);
            lines = wrap(value, maxWidth);
            if (lines.size() <= targetLines || size <= minimumSp) break;
            size -= .5f;
        } while (true);
        float lineHeight = paint.getTextSize() * 1.18f;
        for (int index = 0; index < lines.size(); index++) {
            canvas.drawText(lines.get(index), x, baseline + index * lineHeight, paint);
        }
        return baseline + Math.max(0, lines.size() - 1) * lineHeight;
    }

    private List<String> wrap(String value, float maxWidth) {
        ArrayList<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : value.trim().split("\\s+")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (line.length() > 0 && paint.measureText(candidate) > maxWidth) {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            } else {
                if (line.length() > 0) line.append(' ');
                line.append(word);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        if (lines.isEmpty()) lines.add("");
        return lines;
    }

    int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

/** Equipment is separated from the hero artwork so no slot covers the character. */
class LoadoutV090View extends View {
    interface Listener { void onSlotPressed(String slot); }

    private static final class Slot {
        final String title;
        final String item;
        final String rarity;
        final int color;
        final int artwork;

        Slot(String title, String item, String rarity, int color) {
            this.title = title;
            this.item = item;
            this.rarity = rarity;
            this.color = color;
            this.artwork = item == null || item.isEmpty() ? 0 : VisualAssetCatalog.itemFor(item, title);
        }
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final LinkedHashMap<String, Slot> slots = new LinkedHashMap<>();
    private final LinkedHashMap<String, RectF> hits = new LinkedHashMap<>();
    private final HashMap<Integer, Bitmap> bitmaps = new HashMap<>();
    private Listener listener;

    LoadoutV090View(Context context) {
        super(context);
        setFocusable(true);
    }

    void setListener(Listener nextListener) { listener = nextListener; }

    void put(String key, String title, String item, String rarity, int color) {
        slots.put(key, new Slot(title, item, rarity, color));
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        hits.clear();
        int width = getWidth();
        int height = getHeight();
        paint.setShader(new LinearGradient(0, 0, 0, height,
                Color.rgb(8, 18, 25), Color.rgb(3, 9, 14), Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(0, 0, width, height), dp(18), dp(18), paint);
        paint.setShader(null);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(8));
        paint.setColor(Color.rgb(221, 187, 104));
        canvas.drawText("ĮRANGA", dp(12), dp(23), paint);

        float gap = dp(7);
        float side = dp(9);
        float cardWidth = (width - side * 2 - gap) / 2f;
        float cardHeight = dp(58);
        float y = dp(34);
        String[] keys = {"head", "neck", "weapon", "offhand", "chest", "utility",
                "hands", "belt", "legs", "feet", "ring_left", "ring_right"};
        for (int index = 0; index < keys.length; index += 2) {
            drawSlot(canvas, keys[index], side, y, cardWidth, cardHeight);
            drawSlot(canvas, keys[index + 1], side + cardWidth + gap, y, cardWidth, cardHeight);
            y += cardHeight + gap;
        }

        String[] relics = {"relic_1", "relic_2", "relic_3", "relic_4"};
        for (int index = 0; index < relics.length; index++) {
            int row = index / 2;
            int column = index % 2;
            drawSlot(canvas, relics[index], side + column * (cardWidth + gap),
                    y + row * (cardHeight + gap), cardWidth, cardHeight);
        }
    }

    private void drawSlot(Canvas canvas, String key, float x, float y, float width, float height) {
        Slot slot = slots.get(key);
        boolean equipped = slot != null && slot.item != null && !slot.item.isEmpty();
        int color = equipped ? slot.color : Color.rgb(58, 73, 80);
        RectF card = new RectF(x, y, x + width, y + height);
        hits.put(key, card);

        paint.setColor(Color.argb(equipped ? 235 : 185, 5, 14, 20));
        canvas.drawRoundRect(card, dp(10), dp(10), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(equipped ? dp(1.4f) : dp(1));
        paint.setColor(color);
        canvas.drawRoundRect(card, dp(10), dp(10), paint);
        paint.setStyle(Paint.Style.FILL);

        float imageSize = height - dp(8);
        RectF imageRect = new RectF(x + dp(4), y + dp(4), x + dp(4) + imageSize, y + height - dp(4));
        if (equipped && slot.artwork != 0) {
            Bitmap bitmap = bitmap(slot.artwork);
            if (bitmap != null) canvas.drawBitmap(bitmap, null, imageRect, paint);
        } else {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1.2f));
            paint.setColor(color);
            canvas.drawCircle(imageRect.centerX(), imageRect.centerY(), dp(8), paint);
            paint.setStyle(Paint.Style.FILL);
        }

        float textX = imageRect.right + dp(6);
        float textWidth = Math.max(dp(18), card.right - textX - dp(5));
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(color);
        String title = slot == null ? key : slot.title;
        drawAdaptiveLine(canvas, title.toUpperCase(Locale.ROOT), textX, y + dp(15),
                textWidth, 6.4f, 5f);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setColor(equipped ? Color.rgb(235, 233, 222) : Color.rgb(104, 120, 128));
        drawItemName(canvas, equipped ? slot.item : "Tuščia", textX, y + dp(33), textWidth);
    }

    private Bitmap bitmap(int resource) {
        Bitmap bitmap = bitmaps.get(resource);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapFactory.decodeResource(getResources(), resource);
            bitmaps.put(resource, bitmap);
        }
        return bitmap;
    }

    private void drawAdaptiveLine(Canvas canvas, String value, float x, float baseline,
                                  float maxWidth, float preferredSp, float minimumSp) {
        float density = getResources().getDisplayMetrics().scaledDensity;
        float size = preferredSp;
        paint.setTextSize(size * density);
        while (size > minimumSp && paint.measureText(value) > maxWidth) {
            size -= .25f;
            paint.setTextSize(size * density);
        }
        canvas.drawText(value, x, baseline, paint);
    }

    private void drawItemName(Canvas canvas, String value, float x, float baseline, float maxWidth) {
        float density = getResources().getDisplayMetrics().scaledDensity;
        float size = 7.3f;
        List<String> lines;
        do {
            paint.setTextSize(size * density);
            lines = wrap(value, maxWidth);
            if (lines.size() <= 2 || size <= 5f) break;
            size -= .25f;
        } while (true);
        float lineHeight = paint.getTextSize() * 1.12f;
        for (int index = 0; index < lines.size(); index++) {
            canvas.drawText(lines.get(index), x, baseline + index * lineHeight, paint);
        }
    }

    private List<String> wrap(String value, float maxWidth) {
        ArrayList<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : (value == null ? "" : value).trim().split("\\s+")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (line.length() > 0 && paint.measureText(candidate) > maxWidth) {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            } else {
                if (line.length() > 0) line.append(' ');
                line.append(word);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        if (lines.isEmpty()) lines.add("");
        return lines;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            for (Map.Entry<String, RectF> entry : hits.entrySet()) {
                if (entry.getValue().contains(event.getX(), event.getY())) {
                    performClick();
                    if (listener != null) listener.onSlotPressed(entry.getKey());
                    return true;
                }
            }
        }
        return true;
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override protected void onDetachedFromWindow() {
        for (Bitmap bitmap : bitmaps.values()) if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
        bitmaps.clear();
        super.onDetachedFromWindow();
    }

    int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

/** Interactive premium atlas: base art, location labels and faction influence are separate layers. */
class WorldMapV090View extends View {
    interface Listener { void onLocationSelected(String name, int danger); }

    private static final class Node {
        final String name;
        final String type;
        final float nx;
        final float ny;
        final int danger;
        final float labelDx;
        final float labelDy;

        Node(String name, float nx, float ny, int danger, String type, float labelDx, float labelDy) {
            this.name = name;
            this.nx = nx;
            this.ny = ny;
            this.danger = danger;
            this.type = type;
            this.labelDx = labelDx;
            this.labelDy = labelDy;
        }
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap map;
    private final GameState state;
    private final ArrayList<Node> nodes = new ArrayList<>();
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestures;
    private Listener listener;
    private String current = "Luminara";
    private String selected = "";
    private float baseScale = 1f;
    private float zoom = 1f;
    private float translateX;
    private float translateY;
    private float lastX;
    private float lastY;
    private boolean dragged;
    private boolean showFactions;

    WorldMapV090View(Context context, GameState gameState) {
        super(context);
        state = gameState;
        map = BitmapFactory.decodeResource(getResources(), R.drawable.world_map_base_v090);
        setBackgroundColor(Color.rgb(3, 8, 13));
        setFocusable(true);
        setContentDescription("Interaktyvus Vaeloria pasaulio atlasas");

        add("Luminara", .40f, .36f, 3, "capital", 13, -7);
        add("Asterio Karūna", .29f, .17f, 4, "fortress", -13, -8);
        add("Stiklo Giria", .22f, .57f, 5, "anomaly", 13, -5);
        add("Veyrhold", .17f, .34f, 3, "city", 13, 13);
        add("Aureliono Pakraštys", .62f, .24f, 4, "gate", 13, -8);
        add("Žvaigždėkritos Skliautas", .73f, .52f, 7, "anomaly", -13, -9);
        add("Tuščiavidurė Smailė", .65f, .66f, 8, "dungeon", -13, 14);
        add("Pelenų Karūnos Citadelė", .78f, .31f, 7, "fortress", -13, 14);
        add("Kharad Vorn", .14f, .39f, 5, "city", 13, -7);
        add("Drakono Pabudimo Viršūnės", .67f, .10f, 8, "anomaly", -13, -8);
        add("Safyro Platybės", .57f, .46f, 7, "unknown", 13, 13);
        add("Amžinojo Šaltinio Slėnis", .20f, .68f, 4, "temple", 13, -8);
        add("Žaliasis Labirintas", .73f, .72f, 8, "dungeon", -13, -8);
        add("Šventųjų Pelkynas", .46f, .84f, 6, "unknown", 13, -8);

        scaleDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override public boolean onScale(ScaleGestureDetector detector) {
                        float before = baseScale * zoom;
                        float mapX = (detector.getFocusX() - translateX) / before;
                        float mapY = (detector.getFocusY() - translateY) / before;
                        zoom = Math.max(1f, Math.min(4.5f, zoom * detector.getScaleFactor()));
                        float after = baseScale * zoom;
                        translateX = detector.getFocusX() - mapX * after;
                        translateY = detector.getFocusY() - mapY * after;
                        clamp();
                        invalidate();
                        return true;
                    }
                });
        gestures = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDoubleTap(MotionEvent event) {
                if (zoom > 1.1f) resetView();
                else zoomAt(event.getX(), event.getY(), 2.2f);
                return true;
            }
        });
    }

    private void add(String name, float nx, float ny, int danger, String type, float dx, float dy) {
        nodes.add(new Node(name, nx, ny, danger, type, dx, dy));
    }

    void setListener(Listener nextListener) { listener = nextListener; }

    void setCurrentLocation(String value) {
        current = value == null ? "Luminara" : value;
        invalidate();
    }

    void toggleFactions() {
        showFactions = !showFactions;
        invalidate();
    }

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        resetView();
    }

    void resetView() {
        if (map == null || getWidth() == 0 || getHeight() == 0) return;
        baseScale = Math.min(getWidth() / (float) map.getWidth(), getHeight() / (float) map.getHeight());
        if (baseScale <= 0) baseScale = 1f;
        zoom = 1f;
        translateX = (getWidth() - map.getWidth() * baseScale) / 2f;
        translateY = (getHeight() - map.getHeight() * baseScale) / 2f;
        clamp();
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (map == null) return;
        float scale = baseScale * zoom;
        canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scale, scale);
        canvas.drawBitmap(map, 0, 0, paint);
        if (showFactions) drawFactionLayer(canvas);
        float inverse = 1f / scale;
        Node currentNode = find(current);
        for (Node node : nodes) {
            drawNode(canvas, node, inverse, node == currentNode, node.name.equals(selected));
        }
        canvas.restore();
        if (isAttachedToWindow()) postInvalidateDelayed(80);
    }

    private void drawFactionLayer(Canvas canvas) {
        drawInfluence(canvas, .40f, .36f, state.asterraInfluence, Color.rgb(90, 151, 206), "ASTERRA");
        drawInfluence(canvas, .78f, .31f, state.dravennInfluence, Color.rgb(204, 91, 69), "DRAVENN");
        drawInfluence(canvas, .20f, .68f, state.lysaraInfluence, Color.rgb(104, 170, 94), "LYSARA");
    }

    private void drawInfluence(Canvas canvas, float nx, float ny, int influence, int color, String label) {
        float x = nx * map.getWidth();
        float y = ny * map.getHeight();
        float radius = map.getWidth() * (0.12f + Math.max(0, Math.min(100, influence)) * 0.0019f);
        paint.setShader(new RadialGradient(x, y, radius,
                Color.argb(72, Color.red(color), Color.green(color), Color.blue(color)),
                Color.TRANSPARENT, Shader.TileMode.CLAMP));
        canvas.drawCircle(x, y, radius, paint);
        paint.setShader(null);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(13);
        paint.setColor(Color.argb(205, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawText(label, x - paint.measureText(label) / 2f, y - radius * .52f, paint);
    }

    private void drawNode(Canvas canvas, Node node, float inverse, boolean here, boolean isSelected) {
        float x = node.nx * map.getWidth();
        float y = node.ny * map.getHeight();
        int color = dangerColor(node.danger);
        float icon = dp(8) * inverse;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2) * inverse);
        paint.setColor(color);

        if ("capital".equals(node.type)) {
            Path diamond = new Path();
            diamond.moveTo(x, y - icon * 1.25f);
            diamond.lineTo(x + icon, y);
            diamond.lineTo(x, y + icon * 1.25f);
            diamond.lineTo(x - icon, y);
            diamond.close();
            canvas.drawPath(diamond, paint);
            canvas.drawCircle(x, y, icon * .28f, paint);
        } else if ("fortress".equals(node.type)) {
            canvas.drawRect(x - icon, y - icon * .75f, x + icon, y + icon, paint);
            canvas.drawLine(x - icon, y - icon * .75f, x, y - icon * 1.35f, paint);
            canvas.drawLine(x, y - icon * 1.35f, x + icon, y - icon * .75f, paint);
        } else if ("dungeon".equals(node.type)) {
            Path triangle = new Path();
            triangle.moveTo(x, y - icon * 1.2f);
            triangle.lineTo(x + icon, y + icon);
            triangle.lineTo(x - icon, y + icon);
            triangle.close();
            canvas.drawPath(triangle, paint);
        } else if ("temple".equals(node.type)) {
            canvas.drawCircle(x, y, icon, paint);
            canvas.drawCircle(x, y, icon * .36f, paint);
        } else if ("gate".equals(node.type)) {
            canvas.drawCircle(x, y, icon, paint);
            canvas.drawLine(x, y - icon, x, y + icon, paint);
        } else if ("anomaly".equals(node.type)) {
            canvas.drawCircle(x, y, icon, paint);
            canvas.drawLine(x - icon * .8f, y, x + icon * .8f, y, paint);
            canvas.drawLine(x, y - icon * .8f, x, y + icon * .8f, paint);
        } else if ("city".equals(node.type)) {
            canvas.drawCircle(x, y, icon, paint);
            canvas.drawRect(x - icon * .35f, y - icon * .35f,
                    x + icon * .35f, y + icon * .35f, paint);
        } else if ("unknown".equals(node.type)) {
            Path unknown = new Path();
            unknown.moveTo(x, y - icon);
            unknown.lineTo(x + icon, y);
            unknown.lineTo(x, y + icon);
            unknown.lineTo(x - icon, y);
            unknown.close();
            canvas.drawPath(unknown, paint);
            canvas.drawLine(x - icon * .45f, y - icon * .45f,
                    x + icon * .45f, y + icon * .45f, paint);
        } else {
            canvas.drawCircle(x, y, icon * .72f, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        drawLabel(canvas, node, x, y, inverse);

        if (here) {
            float pulse = (System.currentTimeMillis() % 1600L) / 1600f;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2.5f) * inverse);
            paint.setColor(Color.argb((int) (220 * (1f - pulse)), 244, 204, 103));
            canvas.drawCircle(x, y, dp(14 + 24 * pulse) * inverse, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(244, 204, 103));
            canvas.drawCircle(x, y, dp(4.5f) * inverse, paint);
        }
        if (isSelected) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2.3f) * inverse);
            paint.setColor(Color.rgb(92, 190, 178));
            canvas.drawCircle(x, y, dp(17) * inverse, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawLabel(Canvas canvas, Node node, float x, float y, float inverse) {
        List<String> lines = splitLabel(node.name, 20);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(7.2f) * inverse);
        float maxWidth = 0;
        for (String line : lines) maxWidth = Math.max(maxWidth, paint.measureText(line));
        float padding = dp(4) * inverse;
        float lineHeight = dp(9) * inverse;
        float labelX = x + dp(node.labelDx) * inverse;
        if (node.labelDx < 0) labelX -= maxWidth + padding * 2;
        float baseline = y + dp(node.labelDy) * inverse;
        float top = baseline - lineHeight;
        float bottom = baseline + (lines.size() - 1) * lineHeight + dp(3) * inverse;
        paint.setColor(Color.argb(224, 3, 9, 13));
        canvas.drawRoundRect(labelX - padding, top,
                labelX + maxWidth + padding, bottom, dp(4) * inverse, dp(4) * inverse, paint);
        paint.setColor(Color.rgb(239, 232, 214));
        for (int index = 0; index < lines.size(); index++) {
            canvas.drawText(lines.get(index), labelX, baseline + index * lineHeight, paint);
        }
    }

    private List<String> splitLabel(String value, int targetLength) {
        ArrayList<String> lines = new ArrayList<>();
        if (value.length() <= targetLength) {
            lines.add(value);
            return lines;
        }
        String[] words = value.split(" ");
        StringBuilder first = new StringBuilder();
        int index = 0;
        while (index < words.length) {
            String candidate = first.length() == 0 ? words[index] : first + " " + words[index];
            if (candidate.length() > targetLength && first.length() > 0) break;
            if (first.length() > 0) first.append(' ');
            first.append(words[index++]);
        }
        lines.add(first.toString());
        StringBuilder second = new StringBuilder();
        while (index < words.length) {
            if (second.length() > 0) second.append(' ');
            second.append(words[index++]);
        }
        if (second.length() > 0) lines.add(second.toString());
        return lines;
    }

    private Node find(String value) {
        if (value == null) return null;
        for (Node node : nodes) if (node.name.equalsIgnoreCase(value) || alias(node.name, value)) return node;
        String query = value.toLowerCase(Locale.ROOT);
        if (query.contains("luminara") || query.contains("rūm") || query.contains("rum")) return direct("Luminara");
        return null;
    }

    private Node direct(String name) {
        for (Node node : nodes) if (node.name.equals(name)) return node;
        return null;
    }

    private boolean alias(String canonical, String value) {
        String query = value.toLowerCase(Locale.ROOT);
        return (canonical.startsWith("Asterio") && query.contains("crown"))
                || (canonical.startsWith("Stiklo") && query.contains("glass"))
                || (canonical.startsWith("Aureliono") && query.contains("aurel"))
                || (canonical.startsWith("Žvaigždė") && query.contains("star"))
                || (canonical.startsWith("Tuščiavidurė") && query.contains("hollow"))
                || (canonical.startsWith("Pelenų") && query.contains("ashen"))
                || (canonical.startsWith("Drakono") && query.contains("dragon"))
                || (canonical.startsWith("Safyro") && query.contains("sapphire"))
                || (canonical.startsWith("Amžinojo") && query.contains("spring"))
                || (canonical.startsWith("Šventųjų") && query.contains("mire"))
                || (canonical.startsWith("Žaliasis") && query.contains("verdant"));
    }

    private int dangerColor(int danger) {
        if (danger >= 8) return Color.rgb(219, 86, 75);
        if (danger >= 6) return Color.rgb(219, 145, 74);
        if (danger >= 4) return Color.rgb(219, 187, 104);
        return Color.rgb(92, 184, 174);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        gestures.onTouchEvent(event);
        scaleDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                lastX = event.getX();
                lastY = event.getY();
                dragged = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!scaleDetector.isInProgress()) {
                    float dx = event.getX() - lastX;
                    float dy = event.getY() - lastY;
                    if (Math.abs(dx) + Math.abs(dy) > dp(5)) dragged = true;
                    translateX += dx;
                    translateY += dy;
                    lastX = event.getX();
                    lastY = event.getY();
                    clamp();
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (!dragged) select(event.getX(), event.getY());
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            default:
                return true;
        }
    }

    private void select(float screenX, float screenY) {
        float scale = baseScale * zoom;
        Node best = null;
        float bestDistance = Float.MAX_VALUE;
        for (Node node : nodes) {
            float nodeX = translateX + node.nx * map.getWidth() * scale;
            float nodeY = translateY + node.ny * map.getHeight() * scale;
            float dx = nodeX - screenX;
            float dy = nodeY - screenY;
            float distance = dx * dx + dy * dy;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = node;
            }
        }
        float targetRadius = dp(28);
        if (best != null && bestDistance <= targetRadius * targetRadius) {
            selected = best.name;
            performClick();
            invalidate();
            if (listener != null) listener.onLocationSelected(best.name, best.danger);
        }
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }

    private void zoomAt(float x, float y, float targetZoom) {
        float before = baseScale * zoom;
        float mapX = (x - translateX) / before;
        float mapY = (y - translateY) / before;
        zoom = targetZoom;
        float after = baseScale * zoom;
        translateX = x - mapX * after;
        translateY = y - mapY * after;
        clamp();
        invalidate();
    }

    private void clamp() {
        if (map == null) return;
        float scaledWidth = map.getWidth() * baseScale * zoom;
        float scaledHeight = map.getHeight() * baseScale * zoom;
        float margin = dp(36);
        translateX = scaledWidth <= getWidth()
                ? (getWidth() - scaledWidth) / 2f
                : Math.max(getWidth() - scaledWidth - margin, Math.min(margin, translateX));
        translateY = scaledHeight <= getHeight()
                ? (getHeight() - scaledHeight) / 2f
                : Math.max(getHeight() - scaledHeight - margin, Math.min(margin, translateY));
    }

    @Override protected void onDetachedFromWindow() {
        if (map != null && !map.isRecycled()) map.recycle();
        super.onDetachedFromWindow();
    }

    int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

/** Dynamic faction cards backed exclusively by GameState. */
class FactionStatusV090View extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final GameState state;

    FactionStatusV090View(Context context, GameState gameState) {
        super(context);
        state = gameState;
        setContentDescription("Dinaminė frakcijų būsena");
    }

    @Override protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        paint.setColor(Color.rgb(8, 18, 25));
        canvas.drawRoundRect(new RectF(0, 0, width, height), dp(16), dp(16), paint);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(8));
        paint.setColor(Color.rgb(221, 187, 104));
        canvas.drawText("PASAULIO BŪSENA · MERIDIANO KRIZĖ", dp(13), dp(22), paint);

        String[] names = {"ASTERRA", "DRAVENN", "LYSARA"};
        String[] relations = {state.asterraRelation, state.dravennRelation, state.lysaraRelation};
        int[] colors = {Color.rgb(111, 156, 198), Color.rgb(206, 101, 69), Color.rgb(120, 174, 99)};
        int[] values = {state.asterraInfluence, state.dravennInfluence, state.lysaraInfluence};
        float gap = dp(7);
        float cardWidth = (width - dp(26) - gap * 2) / 3f;
        for (int index = 0; index < 3; index++) {
            float x = dp(13) + index * (cardWidth + gap);
            float y = dp(34);
            RectF card = new RectF(x, y, x + cardWidth, height - dp(10));
            paint.setColor(Color.rgb(11, 24, 31));
            canvas.drawRoundRect(card, dp(12), dp(12), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(Color.argb(155, Color.red(colors[index]), Color.green(colors[index]), Color.blue(colors[index])));
            canvas.drawRoundRect(card, dp(12), dp(12), paint);
            paint.setStyle(Paint.Style.FILL);

            float centerX = x + cardWidth / 2f;
            float centerY = y + dp(27);
            paint.setColor(Color.argb(40, Color.red(colors[index]), Color.green(colors[index]), Color.blue(colors[index])));
            canvas.drawCircle(centerX, centerY, dp(19), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(colors[index]);
            drawEmblem(canvas, index, centerX, centerY);
            paint.setStyle(Paint.Style.FILL);

            paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
            paint.setTextSize(dp(8));
            paint.setColor(Color.rgb(235, 223, 193));
            drawCentered(canvas, names[index], centerX, y + dp(57));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextSize(dp(6.2f));
            paint.setColor(colors[index]);
            drawCentered(canvas, relations[index], centerX, y + dp(72));

            float barY = y + dp(84);
            paint.setColor(Color.rgb(29, 41, 47));
            canvas.drawRoundRect(x + dp(8), barY, x + cardWidth - dp(8), barY + dp(5), dp(3), dp(3), paint);
            paint.setColor(colors[index]);
            float value = Math.max(0, Math.min(100, values[index]));
            canvas.drawRoundRect(x + dp(8), barY,
                    x + dp(8) + (cardWidth - dp(16)) * value / 100f,
                    barY + dp(5), dp(3), dp(3), paint);
            paint.setTextSize(dp(6));
            paint.setColor(Color.rgb(145, 163, 168));
            drawCentered(canvas, "ĮTAKA " + values[index] + "/100", centerX, barY + dp(17));
        }
    }

    private void drawEmblem(Canvas canvas, int index, float x, float y) {
        if (index == 0) {
            Path star = new Path();
            for (int point = 0; point < 8; point++) {
                double angle = -Math.PI / 2 + point * Math.PI / 4;
                float radius = dp(point % 2 == 0 ? 14 : 6);
                float px = x + (float) Math.cos(angle) * radius;
                float py = y + (float) Math.sin(angle) * radius;
                if (point == 0) star.moveTo(px, py); else star.lineTo(px, py);
            }
            star.close();
            canvas.drawPath(star, paint);
            canvas.drawCircle(x, y, dp(3), paint);
        } else if (index == 1) {
            Path crown = new Path();
            crown.moveTo(x, y - dp(15));
            crown.lineTo(x + dp(10), y - dp(4));
            crown.lineTo(x + dp(5), y + dp(15));
            crown.lineTo(x, y + dp(7));
            crown.lineTo(x - dp(6), y + dp(15));
            crown.lineTo(x - dp(11), y - dp(4));
            crown.close();
            canvas.drawPath(crown, paint);
            canvas.drawLine(x, y - dp(10), x, y + dp(9), paint);
        } else {
            Path leaf = new Path();
            leaf.moveTo(x, y - dp(15));
            leaf.cubicTo(x + dp(17), y - dp(8), x + dp(14), y + dp(12), x, y + dp(15));
            leaf.cubicTo(x - dp(14), y + dp(12), x - dp(17), y - dp(8), x, y - dp(15));
            canvas.drawPath(leaf, paint);
            canvas.drawLine(x, y - dp(10), x, y + dp(12), paint);
        }
    }

    private void drawCentered(Canvas canvas, String value, float x, float baseline) {
        String text = value == null ? "—" : value;
        canvas.drawText(text, x - paint.measureText(text) / 2f, baseline, paint);
    }

    int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
