package lt.vaeloria.ooc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

import java.util.Locale;

/** Lengvas procedūrinis portretas, atspindintis realiai sukurtą veikėją. */
final class CharacterAvatarV100View extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private GameState state=new GameState();
    private int seed;

    CharacterAvatarV100View(Context context){super(context);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}

    void setCharacter(GameState value){
        state=value==null?new GameState():value;seed=(state.characterName+"|"+state.characterOriginId+"|"+state.characterArchetypeId+"|"+state.characterAppearance).hashCode();
        String appearance=state.characterAppearance==null||state.characterAppearance.trim().isEmpty()?"išvaizda neaprašyta":state.characterAppearance;
        setContentDescription("Veikėjo portretas. "+state.characterName+", "+CharacterCatalogV093.identityName(state.characterIdentity)+", "+CharacterCatalogV093.originName(state.characterOriginId)+", "+CharacterCatalogV093.archetypeName(state.characterArchetypeId)+". "+appearance);
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
        int[] palette=originPalette(state.characterOriginId);paint.setShader(new LinearGradient(0,0,w,h,palette,null,Shader.TileMode.CLAMP));canvas.drawRect(0,0,w,h,paint);paint.setShader(null);
        drawAtmosphere(canvas,w,h,palette[1]);drawSilhouette(canvas,w,h);drawEmblem(canvas,w,h);drawFrame(canvas,w,h);
    }

    private void drawAtmosphere(Canvas canvas,float w,float h,int accent){
        paint.setShader(new RadialGradient(w*.52f,h*.36f,w*.58f,Color.argb(115,Color.red(accent),Color.green(accent),Color.blue(accent)),Color.TRANSPARENT,Shader.TileMode.CLAMP));canvas.drawCircle(w*.52f,h*.37f,w*.58f,paint);paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(1));for(int i=0;i<7;i++){float x=((seed>>>i*3)&31)/31f*w;float y=.08f*h+((seed>>>i*2)&15)/15f*h*.58f;paint.setColor(Color.argb(75+i*8,221,187,104));canvas.drawCircle(x,y,dp(1.2f+i*.18f),paint);}paint.setStyle(Paint.Style.FILL);
    }

    private void drawSilhouette(Canvas canvas,float w,float h){
        float cx=w*.51f,headY=h*.30f,headR=Math.min(w,h)*.105f;int skin=skinColor();int hair=hairColor();int cloth=archetypeColor(state.characterArchetypeId);
        // apsiaustas ir pečiai
        path.reset();path.moveTo(w*.13f,h);path.cubicTo(w*.17f,h*.66f,w*.34f,h*.56f,cx,h*.55f);path.cubicTo(w*.68f,h*.56f,w*.87f,h*.68f,w*.92f,h);path.close();paint.setColor(darken(cloth,36));canvas.drawPath(path,paint);
        path.reset();path.moveTo(w*.23f,h);path.cubicTo(w*.30f,h*.62f,w*.42f,h*.57f,cx,h*.59f);path.cubicTo(w*.62f,h*.57f,w*.77f,h*.64f,w*.82f,h);path.close();paint.setShader(new LinearGradient(w*.25f,h*.62f,w*.78f,h,lighten(cloth,16),darken(cloth,55),Shader.TileMode.CLAMP));canvas.drawPath(path,paint);paint.setShader(null);
        // kaklas, ausys ir veidas
        paint.setColor(darken(skin,8));canvas.drawRoundRect(new RectF(cx-headR*.34f,headY+headR*.67f,cx+headR*.34f,headY+headR*1.55f),headR*.20f,headR*.20f,paint);
        paint.setColor(skin);canvas.drawOval(new RectF(cx-headR,headY-headR*1.18f,cx+headR,headY+headR*1.23f),paint);
        // plaukai kinta pagal tapatybę ir aprašą
        float hairLength="moteris".equals(state.characterIdentity)?headR*1.45f:"nenurodyta".equals(state.characterIdentity)?headR*.95f:headR*.55f;
        path.reset();path.moveTo(cx-headR*1.06f,headY+headR*.1f);path.cubicTo(cx-headR,headY-headR*1.42f,cx+headR*.80f,headY-headR*1.55f,cx+headR*1.06f,headY-headR*.05f);path.lineTo(cx+headR*.88f,headY+hairLength);path.cubicTo(cx+headR*.45f,headY+headR*.72f,cx-headR*.58f,headY+headR*.72f,cx-headR*.94f,headY+hairLength);path.close();paint.setColor(hair);canvas.drawPath(path,paint);
        // veido šviesa ir bruožai
        paint.setShader(new LinearGradient(cx-headR,headY,cx+headR,headY,Color.argb(0,255,255,255),Color.argb(58,255,244,220),Shader.TileMode.CLAMP));canvas.drawOval(new RectF(cx-headR*.82f,headY-headR*.88f,cx+headR*.82f,headY+headR*1.06f),paint);paint.setShader(null);
        float eyeY=headY-headR*.10f;paint.setColor(Color.rgb(24,31,33));canvas.drawOval(new RectF(cx-headR*.55f,eyeY-headR*.055f,cx-headR*.16f,eyeY+headR*.055f),paint);canvas.drawOval(new RectF(cx+headR*.16f,eyeY-headR*.055f,cx+headR*.55f,eyeY+headR*.055f),paint);
        paint.setColor(eyeColor());canvas.drawCircle(cx-headR*.35f,eyeY,headR*.045f,paint);canvas.drawCircle(cx+headR*.35f,eyeY,headR*.045f,paint);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(1.2f));paint.setColor(darken(skin,50));canvas.drawArc(new RectF(cx-headR*.28f,headY+headR*.27f,cx+headR*.28f,headY+headR*.72f),18,144,false,paint);paint.setStyle(Paint.Style.FILL);
        // archetipinis segės simbolis
        paint.setColor(Color.rgb(225,190,100));canvas.drawCircle(cx,h*.68f,dp(16),paint);paint.setColor(Color.rgb(20,30,35));paint.setTypeface(Typeface.DEFAULT_BOLD);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(dp(14));canvas.drawText(archetypeGlyph(state.characterArchetypeId),cx,h*.68f+dp(5),paint);paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawEmblem(Canvas canvas,float w,float h){
        String label=CharacterCatalogV093.originName(state.characterOriginId).toUpperCase(Locale.forLanguageTag("lt-LT"));paint.setTypeface(Typeface.DEFAULT_BOLD);paint.setTextSize(dp(8));float width=paint.measureText(label);paint.setColor(Color.argb(190,3,10,15));canvas.drawRoundRect(new RectF(dp(13),dp(13),dp(29)+width,dp(42)),dp(14),dp(14),paint);paint.setColor(Color.rgb(231,210,158));canvas.drawText(label,dp(21),dp(32),paint);
    }

    private void drawFrame(Canvas canvas,float w,float h){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(1.5f));paint.setColor(Color.argb(185,221,187,104));canvas.drawRoundRect(new RectF(dp(1),dp(1),w-dp(1),h-dp(1)),dp(17),dp(17),paint);paint.setStyle(Paint.Style.FILL);}
    private int[] originPalette(String id){if("akademija".equals(id))return new int[]{Color.rgb(12,18,38),Color.rgb(65,67,140),Color.rgb(4,10,18)};if("pasienis".equals(id))return new int[]{Color.rgb(16,30,29),Color.rgb(62,105,82),Color.rgb(4,10,14)};if("pelkynai".equals(id))return new int[]{Color.rgb(15,32,24),Color.rgb(70,112,61),Color.rgb(3,11,12)};if("dravenn".equals(id))return new int[]{Color.rgb(38,13,13),Color.rgb(134,55,41),Color.rgb(10,7,10)};if("gildija".equals(id))return new int[]{Color.rgb(34,24,14),Color.rgb(133,96,48),Color.rgb(7,9,12)};return new int[]{Color.rgb(7,25,36),Color.rgb(49,102,126),Color.rgb(3,9,14)};}
    private int archetypeColor(String id){if("sargybinis".equals(id))return Color.rgb(91,117,134);if("zvalgas".equals(id))return Color.rgb(56,105,77);if("arkanistas".equals(id))return Color.rgb(91,70,148);if("diplomatas".equals(id))return Color.rgb(136,57,68);if("amatininkas".equals(id))return Color.rgb(130,91,42);return Color.rgb(80,101,91);}
    private String archetypeGlyph(String id){if("sargybinis".equals(id))return"◆";if("zvalgas".equals(id))return"⌖";if("arkanistas".equals(id))return"✦";if("diplomatas".equals(id))return"§";if("amatininkas".equals(id))return"⚒";return"△";}
    private int skinColor(){int tone=Math.abs(seed)%5;int[] values={Color.rgb(238,201,170),Color.rgb(216,169,130),Color.rgb(183,126,91),Color.rgb(137,88,65),Color.rgb(91,58,48)};return values[tone];}
    private int hairColor(){String a=state.characterAppearance==null?"":state.characterAppearance.toLowerCase(Locale.forLanguageTag("lt-LT"));if(a.contains("sidabr")||a.contains("žil"))return Color.rgb(205,207,201);if(a.contains("raudon"))return Color.rgb(130,55,38);if(a.contains("auks"))return Color.rgb(190,151,76);if(a.contains("juod"))return Color.rgb(26,27,30);return new int[]{Color.rgb(46,30,23),Color.rgb(77,49,31),Color.rgb(28,31,35),Color.rgb(142,101,60)}[Math.abs(seed/7)%4];}
    private int eyeColor(){return new int[]{Color.rgb(62,130,147),Color.rgb(80,137,83),Color.rgb(139,98,50),Color.rgb(116,83,146)}[Math.abs(seed/13)%4];}
    private int darken(int color,int amount){return Color.rgb(Math.max(0,Color.red(color)-amount),Math.max(0,Color.green(color)-amount),Math.max(0,Color.blue(color)-amount));}
    private int lighten(int color,int amount){return Color.rgb(Math.min(255,Color.red(color)+amount),Math.min(255,Color.green(color)+amount),Math.min(255,Color.blue(color)+amount));}
    private float dp(float value){return value*getResources().getDisplayMetrics().density;}
}
