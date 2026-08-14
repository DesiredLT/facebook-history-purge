package lt.vaeloria.ooc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedHashMap;
import java.util.Map;

public class CharacterLoadoutView extends View {
    public interface Listener { void onSlotPressed(String slot); }
    public static class SlotInfo {
        public final String title, item;
        public final int accent;
        public SlotInfo(String title,String item,int accent){this.title=title;this.item=item;this.accent=accent;}
    }

    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<String,SlotInfo> slots=new LinkedHashMap<>();
    private final Map<String,RectF> hits=new LinkedHashMap<>();
    private Listener listener;

    public CharacterLoadoutView(Context c){super(c);setMinimumHeight(dp(510));setFocusable(true);}
    public void setListener(Listener l){listener=l;}
    public void put(String slot,String title,String item,int accent){slots.put(slot,new SlotInfo(title,item,accent));invalidate();}

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);hits.clear();int w=getWidth(),h=getHeight();
        p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(10,21,29));c.drawRoundRect(0,0,w,h,dp(18),dp(18),p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(Color.rgb(43,64,74));c.drawRoundRect(dp(1),dp(1),w-dp(1),h-dp(1),dp(18),dp(18),p);

        float cx=w*.5f, top=dp(44), bodyBottom=h-dp(105);
        drawAura(c,cx,(top+bodyBottom)/2f,Math.min(w*.24f,dp(105)));
        drawSilhouette(c,cx,top,bodyBottom);

        float boxW=Math.min(dp(145),w*.31f), boxH=dp(52), left=dp(8), right=w-boxW-dp(8);
        float y=dp(18);
        drawSlot(c,"head",left,y,boxW,boxH,cx,top+dp(18));
        drawSlot(c,"neck",right,y,boxW,boxH,cx,top+dp(52)); y+=dp(61);
        drawSlot(c,"weapon",left,y,boxW,boxH,cx-dp(46),top+dp(104));
        drawSlot(c,"offhand",right,y,boxW,boxH,cx+dp(46),top+dp(104)); y+=dp(61);
        drawSlot(c,"chest",left,y,boxW,boxH,cx,top+dp(126));
        drawSlot(c,"utility",right,y,boxW,boxH,cx+dp(33),top+dp(152)); y+=dp(61);
        drawSlot(c,"hands",left,y,boxW,boxH,cx-dp(62),top+dp(155));
        drawSlot(c,"belt",right,y,boxW,boxH,cx,top+dp(186)); y+=dp(61);
        drawSlot(c,"legs",left,y,boxW,boxH,cx-dp(22),top+dp(235));
        drawSlot(c,"feet",right,y,boxW,boxH,cx+dp(22),top+dp(300)); y+=dp(61);
        drawSlot(c,"ring_left",left,y,boxW,boxH,cx-dp(64),top+dp(168));
        drawSlot(c,"ring_right",right,y,boxW,boxH,cx+dp(64),top+dp(168));

        float relicY=h-dp(88), gap=dp(6), relicW=(w-dp(16)-gap*3)/4f;
        drawCompact(c,"relic_1",dp(8),relicY,relicW,dp(68));
        drawCompact(c,"relic_2",dp(8)+relicW+gap,relicY,relicW,dp(68));
        drawCompact(c,"relic_3",dp(8)+(relicW+gap)*2,relicY,relicW,dp(68));
        drawCompact(c,"relic_4",dp(8)+(relicW+gap)*3,relicY,relicW,dp(68));

        p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(9));p.setColor(Color.rgb(214,182,107));
        c.drawText("EINORAS · DĖVIMA ĮRANGA",dp(12),h-dp(96),p);
    }

    private void drawAura(Canvas c,float cx,float cy,float r){
        p.setStyle(Paint.Style.STROKE);for(int i=0;i<4;i++){p.setStrokeWidth(dp(1));p.setColor(Color.argb(42-i*7,214,182,107));float rr=r+dp(i*18);c.drawCircle(cx,cy,rr,p);}p.setStyle(Paint.Style.FILL);
    }

    private void drawSilhouette(Canvas c,float cx,float top,float bottom){
        int fill=Color.rgb(35,51,61), edge=Color.rgb(116,139,146), gold=Color.rgb(214,182,107);
        p.setStyle(Paint.Style.FILL);p.setColor(fill);c.drawCircle(cx,top+dp(30),dp(23),p);
        Path body=new Path();body.moveTo(cx-dp(35),top+dp(63));body.quadTo(cx,top+dp(48),cx+dp(35),top+dp(63));body.lineTo(cx+dp(29),top+dp(190));body.lineTo(cx+dp(20),top+dp(205));body.lineTo(cx+dp(18),bottom-dp(20));body.lineTo(cx+dp(3),bottom);body.lineTo(cx-dp(8),bottom-dp(18));body.lineTo(cx-dp(18),top+dp(205));body.lineTo(cx-dp(29),top+dp(190));body.close();c.drawPath(body,p);
        p.setStrokeCap(Paint.Cap.ROUND);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(17));p.setColor(fill);c.drawLine(cx-dp(27),top+dp(82),cx-dp(62),top+dp(170),p);c.drawLine(cx+dp(27),top+dp(82),cx+dp(62),top+dp(170),p);p.setStrokeWidth(dp(1));p.setColor(edge);c.drawCircle(cx,top+dp(30),dp(24),p);c.drawPath(body,p);
        if(has("chest")){p.setStrokeWidth(dp(3));p.setColor(gold);c.drawLine(cx-dp(29),top+dp(80),cx+dp(29),top+dp(80),p);c.drawLine(cx-dp(27),top+dp(112),cx+dp(27),top+dp(112),p);}
        if(has("head")){p.setStrokeWidth(dp(3));p.setColor(gold);c.drawArc(cx-dp(24),top+dp(8),cx+dp(24),top+dp(52),195,150,false,p);}
        if(has("weapon")){p.setStrokeWidth(dp(4));p.setColor(gold);c.drawLine(cx-dp(70),top+dp(170),cx-dp(92),top+dp(62),p);}
        if(has("offhand")){p.setStrokeWidth(dp(3));p.setColor(gold);c.drawCircle(cx+dp(73),top+dp(135),dp(21),p);}
        p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.BUTT);
    }

    private boolean has(String slot){SlotInfo s=slots.get(slot);return s!=null&&s.item!=null&&!s.item.isEmpty();}

    private void drawSlot(Canvas c,String key,float x,float y,float w,float h,float ax,float ay){
        SlotInfo s=slots.get(key);if(s==null)s=new SlotInfo(key,"",Color.rgb(70,91,100));boolean eq=s.item!=null&&!s.item.isEmpty();
        RectF r=new RectF(x,y,x+w,y+h);hits.put(key,r);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(eq?s.accent:Color.rgb(48,69,79));float end=x<getWidth()/2f?x+w:x;c.drawLine(end,y+h/2f,ax,ay,p);
        p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(14,28,37));c.drawRoundRect(r,dp(12),dp(12),p);p.setStyle(Paint.Style.STROKE);p.setColor(eq?s.accent:Color.rgb(48,69,79));c.drawRoundRect(r,dp(12),dp(12),p);
        p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(8));p.setColor(eq?s.accent:Color.rgb(151,168,173));c.drawText(s.title.toUpperCase(),x+dp(9),y+dp(17),p);
        p.setTypeface(Typeface.DEFAULT);p.setTextSize(dp(9));p.setColor(eq?Color.rgb(238,239,232):Color.rgb(108,130,138));c.drawText(fit(s.item==null||s.item.isEmpty()?"Tuščia":s.item,w-dp(18)),x+dp(9),y+dp(37),p);
    }

    private void drawCompact(Canvas c,String key,float x,float y,float w,float h){
        SlotInfo s=slots.get(key);if(s==null)s=new SlotInfo("Relikvija","",Color.rgb(70,91,100));boolean eq=s.item!=null&&!s.item.isEmpty();RectF r=new RectF(x,y,x+w,y+h);hits.put(key,r);
        p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(14,28,37));c.drawRoundRect(r,dp(10),dp(10),p);p.setStyle(Paint.Style.STROKE);p.setColor(eq?s.accent:Color.rgb(48,69,79));c.drawRoundRect(r,dp(10),dp(10),p);
        p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(7));p.setColor(eq?s.accent:Color.rgb(151,168,173));c.drawText(s.title.toUpperCase(),x+dp(6),y+dp(16),p);p.setTypeface(Typeface.DEFAULT);p.setTextSize(dp(8));p.setColor(eq?Color.rgb(238,239,232):Color.rgb(108,130,138));c.drawText(fit(eq?s.item:"Tuščia",w-dp(12)),x+dp(6),y+dp(38),p);
    }

    private String fit(String s,float max){if(s==null)return"";if(p.measureText(s)<=max)return s;String x=s;while(x.length()>2&&p.measureText(x+"…")>max)x=x.substring(0,x.length()-1);return x+"…";}

    @Override public boolean onTouchEvent(MotionEvent e){if(e.getActionMasked()==MotionEvent.ACTION_UP){float x=e.getX(),y=e.getY();for(Map.Entry<String,RectF> h:hits.entrySet())if(h.getValue().contains(x,y)){if(listener!=null)listener.onSlotPressed(h.getKey());performClick();return true;}}return true;}
    @Override public boolean performClick(){super.performClick();return true;}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
}
