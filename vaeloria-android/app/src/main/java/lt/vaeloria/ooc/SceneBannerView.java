package lt.vaeloria.ooc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

public class SceneBannerView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private String location="Luminara";
    public SceneBannerView(Context c){super(c);setMinimumHeight(dp(155));}
    public void setLocation(String l){location=l==null?"Luminara":l;invalidate();}
    @Override protected void onDraw(Canvas c){super.onDraw(c);int w=getWidth(),h=getHeight();
        p.setShader(new LinearGradient(0,0,w,h,Color.rgb(16,39,56),Color.rgb(9,18,29), Shader.TileMode.CLAMP));c.drawRoundRect(0,0,w,h,dp(18),dp(18),p);p.setShader(null);
        if(location.contains("Luminara"))drawLuminara(c,w,h);else if(location.contains("Dragonwake")||location.contains("Kharad"))drawMountains(c,w,h);else if(location.contains("Vale")||location.contains("Verdant")||location.contains("Mire"))drawWild(c,w,h);else drawArcane(c,w,h);
        p.setColor(Color.argb(145,0,0,0));c.drawRect(0,h-dp(46),w,h,p);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(15));p.setColor(Color.rgb(239,226,193));c.drawText(location.toUpperCase(),dp(15),h-dp(18),p);
    }
    private void drawLuminara(Canvas c,int w,int h){
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(3));
        for(int i=0;i<7;i++){float r=dp(22+i*8);p.setColor(Color.argb(135-i*9,95+8*i,190,220));c.drawOval(w*.65f-r,h*.44f-r*.45f,w*.65f+r,h*.44f+r*.45f,p);}p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(214,182,107));c.drawCircle(w*.65f,h*.44f,dp(7),p);
        p.setColor(Color.rgb(29,55,70));for(int i=0;i<8;i++){float x=w*.08f+i*w*.11f;float bh=dp(35+(i%3)*18);c.drawRect(x,h-dp(46)-bh,x+dp(19),h-dp(46),p);}
        p.setColor(Color.argb(130,170,220,235));Path ship=new Path();ship.moveTo(w*.19f,h*.30f);ship.lineTo(w*.30f,h*.25f);ship.lineTo(w*.27f,h*.34f);ship.close();c.drawPath(ship,p);
    }
    private void drawMountains(Canvas c,int w,int h){p.setColor(Color.rgb(55,58,61));Path m=new Path();m.moveTo(0,h*.72f);m.lineTo(w*.20f,h*.22f);m.lineTo(w*.35f,h*.64f);m.lineTo(w*.55f,h*.18f);m.lineTo(w*.78f,h*.70f);m.lineTo(w,h*.32f);m.lineTo(w,h);m.lineTo(0,h);m.close();c.drawPath(m,p);p.setColor(Color.argb(190,232,135,72));c.drawCircle(w*.72f,h*.25f,dp(16),p);}
    private void drawWild(Canvas c,int w,int h){p.setColor(Color.rgb(31,68,54));for(int i=0;i<13;i++){float x=w*(i/13f)+dp(4);float top=h*(.25f+(i%4)*.08f);c.drawRect(x,top,x+dp(8),h-dp(44),p);c.drawCircle(x+dp(4),top,dp(18),p);}p.setColor(Color.argb(120,86,210,167));c.drawOval(w*.15f,h*.60f,w*.80f,h*.78f,p);}
    private void drawArcane(Canvas c,int w,int h){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(3));p.setColor(Color.argb(160,139,105,211));for(int i=0;i<5;i++)c.drawCircle(w*.52f,h*.45f,dp(18+i*15),p);p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(214,182,107));c.drawCircle(w*.52f,h*.45f,dp(8),p);}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
}
