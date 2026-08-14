package lt.vaeloria.ooc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.view.View;

public class CombatBoardView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private GameState state;

    public CombatBoardView(Context c) { super(c); setMinimumHeight(dp(190)); }
    public void setState(GameState s){state=s;invalidate();}

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        if(state==null)return;
        int w=getWidth(),h=getHeight();
        p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(9,20,28));c.drawRoundRect(0,0,w,h,dp(18),dp(18),p);
        p.setColor(Color.rgb(23,42,51));
        for(int i=1;i<5;i++)c.drawRoundRect(w*i/5f-dp(2),dp(32),w*i/5f+dp(2),h-dp(22),dp(2),dp(2),p);

        if(state.combatHazard!=null&&!state.combatHazard.isEmpty()){
            p.setColor(Color.argb(75,220,128,72));
            Path hazard=new Path();hazard.moveTo(w*.40f,h*.28f);hazard.lineTo(w*.64f,h*.20f);hazard.lineTo(w*.72f,h*.72f);hazard.lineTo(w*.43f,h*.80f);hazard.close();c.drawPath(hazard,p);
        }

        float px=w*.18f, py=h*.60f;
        float ex="close".equals(state.combatDistance)?w*.42f:"far".equals(state.combatDistance)?w*.82f:w*.64f;
        float ey=h*.46f;
        drawActor(c,px,py,"E",Color.rgb(214,182,107),"Einoras");
        drawActor(c,ex,ey,"X",Color.rgb(205,82,77),state.enemyName.isEmpty()?"Priešas":state.enemyName);

        p.setStrokeWidth(dp(2));p.setStyle(Paint.Style.STROKE);p.setColor(Color.argb(150,205,82,77));
        c.drawLine(ex-dp(18),ey+dp(23),px+dp(22),py-dp(18),p);p.setStyle(Paint.Style.FILL);
        p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(10));p.setColor(Color.rgb(170,187,193));
        c.drawText("ATSTUMAS: "+distance(state.combatDistance),dp(14),dp(22),p);
        if(state.combatHazard!=null&&!state.combatHazard.isEmpty()){
            p.setColor(Color.rgb(223,155,83));c.drawText("PAVOJUS: "+trim(state.combatHazard,32),dp(14),h-dp(8),p);
        }
    }

    private String distance(String d){if("close".equals(d))return"ARTIMAS";if("far".equals(d))return"TOLIMAS";return"VIDUTINIS";}
    private void drawActor(Canvas c,float x,float y,String mark,int color,String name){
        p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawCircle(x,y,dp(23),p);
        p.setColor(Color.rgb(7,15,22));p.setTextSize(dp(18));p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);c.drawText(mark,x,y+dp(6),p);
        p.setColor(Color.rgb(235,238,232));p.setTextSize(dp(9));c.drawText(trim(name,20),x,y+dp(40),p);p.setTextAlign(Paint.Align.LEFT);
    }
    private String trim(String s,int n){return s==null?"":(s.length()>n?s.substring(0,n-1)+"…":s);}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
}
