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

import java.util.Locale;

public class SceneBannerView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private String location="Luminara",scene="",event="";
    private long minute=0;
    public SceneBannerView(Context c){super(c);setMinimumHeight(dp(210));}
    public void setLocation(String l){setScene(l,"","",0);}
    public void setScene(String l,String s,String e,long m){location=l==null?"Luminara":l;scene=s==null?"":s;event=e==null?"":e;minute=m;invalidate();}

    @Override protected void onDraw(Canvas c){super.onDraw(c);int w=getWidth(),h=getHeight();int hour=(int)((minute%1440)/60);boolean night=hour<6||hour>=20;
        int a=night?Color.rgb(7,20,35):Color.rgb(19,52,73),b=night?Color.rgb(5,10,20):Color.rgb(9,22,34);p.setShader(new LinearGradient(0,0,w,h,a,b, Shader.TileMode.CLAMP));c.drawRoundRect(0,0,w,h,dp(18),dp(18),p);p.setShader(null);
        drawSky(c,w,h,night,hour);
        String s=(location+" "+scene+" "+event).toLowerCase(Locale.forLanguageTag("lt-LT"));
        if(s.contains("luminara"))drawLuminara(c,w,h,s);
        else if(s.contains("drakono")||s.contains("kharad")||s.contains("kaln"))drawMountains(c,w,h,night);
        else if(s.contains("slėn")||s.contains("labir")||s.contains("pelkyn")||s.contains("giria"))drawWild(c,w,h,night);
        else drawArcane(c,w,h,s);
        if(s.contains("manifest"))drawManifest(c,w,h);
        if(s.contains("vart")||s.contains("meridian")||s.contains("poslink"))drawGateAnomaly(c,w,h);
        drawFooter(c,w,h);
    }

    private void drawSky(Canvas c,int w,int h,boolean night,int hour){
        if(night){p.setColor(Color.argb(210,222,227,213));c.drawCircle(w*.83f,h*.18f,dp(13),p);p.setColor(Color.rgb(7,20,35));c.drawCircle(w*.87f,h*.15f,dp(13),p);p.setColor(Color.argb(150,210,225,235));for(int i=0;i<15;i++)c.drawCircle((i*83%Math.max(1,w-20))+10,(i*37%Math.max(1,h/2))+8,dp(1),p);}else{p.setColor(Color.argb(165,230,185,103));c.drawCircle(w*.83f,h*.17f,dp(18),p);}
    }

    private void drawLuminara(Canvas c,int w,int h,String s){
        float ground=h-dp(48);p.setColor(Color.rgb(24,52,66));for(int i=0;i<11;i++){float x=w*.03f+i*w*.09f;float bh=dp(35+(i%4)*19);c.drawRect(x,ground-bh,x+dp(21),ground,p);p.setColor(Color.rgb(31,65,80));c.drawRect(x+dp(4),ground-bh+dp(8),x+dp(7),ground-bh+dp(13),p);p.setColor(Color.rgb(24,52,66));}
        float gx=w*.64f,gy=h*.40f;p.setStyle(Paint.Style.STROKE);for(int i=0;i<7;i++){p.setStrokeWidth(dp(i==0?3:2));p.setColor(Color.argb(160-i*12,93+8*i,187,220));float rx=dp(30+i*10),ry=dp(12+i*4);c.drawOval(gx-rx,gy-ry,gx+rx,gy+ry,p);}p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(214,182,107));c.drawCircle(gx,gy,dp(6),p);
        p.setColor(Color.argb(150,174,224,236));Path ship=new Path();ship.moveTo(w*.13f,h*.24f);ship.lineTo(w*.28f,h*.20f);ship.lineTo(w*.24f,h*.31f);ship.close();c.drawPath(ship,p);
        if(s.contains("vėlyv")||s.contains("karavan")){p.setColor(Color.rgb(133,100,65));for(int i=0;i<3;i++){float x=w*.19f+i*dp(25);c.drawRect(x,ground-dp(18),x+dp(20),ground-dp(7),p);c.drawCircle(x+dp(5),ground-dp(5),dp(4),p);c.drawCircle(x+dp(16),ground-dp(5),dp(4),p);}}
    }

    private void drawMountains(Canvas c,int w,int h,boolean night){p.setColor(night?Color.rgb(35,39,48):Color.rgb(60,62,64));Path m=new Path();m.moveTo(0,h*.72f);m.lineTo(w*.18f,h*.25f);m.lineTo(w*.34f,h*.63f);m.lineTo(w*.54f,h*.19f);m.lineTo(w*.76f,h*.68f);m.lineTo(w,h*.30f);m.lineTo(w,h);m.lineTo(0,h);m.close();c.drawPath(m,p);p.setColor(Color.argb(180,232,135,72));c.drawCircle(w*.72f,h*.25f,dp(15),p);}
    private void drawWild(Canvas c,int w,int h,boolean night){p.setColor(night?Color.rgb(21,52,43):Color.rgb(34,75,57));for(int i=0;i<15;i++){float x=w*(i/15f)+dp(4);float top=h*(.24f+(i%4)*.07f);c.drawRect(x,top,x+dp(7),h-dp(45),p);c.drawCircle(x+dp(4),top,dp(17),p);}p.setColor(Color.argb(115,86,210,167));c.drawOval(w*.12f,h*.64f,w*.83f,h*.81f,p);}
    private void drawArcane(Canvas c,int w,int h,String s){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));for(int i=0;i<6;i++){p.setColor(Color.argb(170-i*16,139,105+i*8,211));c.drawCircle(w*.52f,h*.44f,dp(18+i*15),p);}p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(214,182,107));c.drawCircle(w*.52f,h*.44f,dp(7),p);}

    private void drawManifest(Canvas c,int w,int h){float x=w*.12f,y=h*.39f,ww=dp(70),hh=dp(82);p.setColor(Color.argb(225,218,196,144));c.drawRoundRect(x,y,x+ww,y+hh,dp(5),dp(5),p);p.setColor(Color.rgb(100,79,55));p.setStrokeWidth(dp(2));for(int i=0;i<5;i++)c.drawLine(x+dp(10),y+dp(15+i*11),x+ww-dp(11),y+dp(15+i*11),p);p.setColor(Color.rgb(146,56,50));c.drawCircle(x+ww-dp(16),y+hh-dp(15),dp(8),p);}
    private void drawGateAnomaly(Canvas c,int w,int h){float x=w*.66f,y=h*.48f;p.setStyle(Paint.Style.STROKE);for(int i=0;i<5;i++){p.setStrokeWidth(dp(2));p.setColor(Color.argb(150-i*20,94,190,220));c.drawOval(x-dp(38+i*7),y-dp(56+i*5),x+dp(38+i*7),y+dp(56+i*5),p);}p.setColor(Color.argb(180,214,182,107));c.drawLine(x,y-dp(62),x,y+dp(62),p);p.setStyle(Paint.Style.FILL);}
    private void drawFooter(Canvas c,int w,int h){p.setColor(Color.argb(178,0,0,0));c.drawRoundRect(0,h-dp(49),w,h,0,0,p);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(14));p.setColor(Color.rgb(239,226,193));c.drawText(location.toUpperCase(Locale.forLanguageTag("lt-LT")),dp(15),h-dp(20),p);if(event!=null&&!event.isEmpty()&&!"none".equals(event)){p.setTextSize(dp(9));p.setColor(Color.rgb(82,177,167));String e=event.toUpperCase(Locale.ROOT);float tw=p.measureText(e);c.drawText(e,w-tw-dp(15),h-dp(20),p);}}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
}
