package lt.ooc.vaeloria;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public final class WorldMapView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); private final ScaleGestureDetector sd;
    private float scale=1f,tx=0,ty=0; private final PointF last=new PointF(); private boolean drag;
    private final List<Pin> pins=new ArrayList<>();
    public WorldMapView(Context c){super(c);setBackgroundColor(Color.rgb(4,17,24));
        pins.add(new Pin("Luminara",360,260,true)); pins.add(new Pin("Asterio Karūna",210,145,false)); pins.add(new Pin("Stiklo Giria",505,120,false)); pins.add(new Pin("Veyrhold",475,205,false)); pins.add(new Pin("Aureliono Pakraštys",590,325,false)); pins.add(new Pin("Žvaigždėkritos Skilautas",360,420,false)); pins.add(new Pin("Tuščiavidurė Smailė",180,455,false));
        pins.add(new Pin("Pelenų Karūnos Citadelė",755,220,false)); pins.add(new Pin("Kharad Vorn",965,290,false)); pins.add(new Pin("Drakono Pabudimo Viršūnės",900,115,false));
        pins.add(new Pin("Safyro Plynatės",720,500,false)); pins.add(new Pin("Amžinojo Šaltinio Slėnis",700,585,false)); pins.add(new Pin("Šventųjų Pelkynas",520,655,false)); pins.add(new Pin("Žaliasis Labirintas",850,665,false));
        sd=new ScaleGestureDetector(c,new ScaleGestureDetector.SimpleOnScaleGestureListener(){@Override public boolean onScale(ScaleGestureDetector d){float ns=Math.max(1f,Math.min(3.5f,scale*d.getScaleFactor()));float f=ns/scale;scale=ns;tx=d.getFocusX()-(d.getFocusX()-tx)*f;ty=d.getFocusY()-(d.getFocusY()-ty)*f;invalidate();return true;}});
    }
    @Override protected void onDraw(Canvas c){super.onDraw(c);float sx=getWidth()/1080f, sy=getHeight()/760f;c.save();c.translate(tx,ty);c.scale(scale*sx,scale*sy);
        p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(34,79,62));Path a=new Path();a.moveTo(80,90);a.cubicTo(250,25,580,45,650,180);a.cubicTo(690,350,590,470,420,510);a.cubicTo(250,550,70,470,55,280);a.close();c.drawPath(a,p);
        p.setColor(Color.rgb(75,54,43));Path d=new Path();d.moveTo(650,75);d.cubicTo(790,35,1020,65,1050,205);d.cubicTo(1060,350,930,440,750,405);d.cubicTo(645,365,610,220,650,75);d.close();c.drawPath(d,p);
        p.setColor(Color.rgb(42,88,66));Path l=new Path();l.moveTo(430,470);l.cubicTo(620,420,930,450,1025,560);l.cubicTo(980,735,700,750,470,715);l.cubicTo(390,645,370,535,430,470);l.close();c.drawPath(l,p);
        drawMountains(c);drawRivers(c);drawRoads(c);
        p.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.SERIF,android.graphics.Typeface.BOLD));p.setTextAlign(Paint.Align.CENTER);p.setTextSize(34);p.setColor(Color.argb(170,235,220,170));c.drawText("ASTERRA",300,330,p);c.drawText("DRAVENN",850,330,p);c.drawText("LYSARIA",735,625,p);
        for(Pin pin:pins)drawPin(c,pin);c.restore();}
    private void drawRoads(Canvas c){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setColor(Color.rgb(190,160,105));float[][] roads={{360,260,475,205},{360,260,210,145},{360,260,590,325},{360,260,360,420},{360,420,180,455},{590,325,755,220},{755,220,965,290},{590,325,720,500},{720,500,700,585},{700,585,520,655},{700,585,850,665}};for(float[]r:roads)c.drawLine(r[0],r[1],r[2],r[3],p);p.setStyle(Paint.Style.FILL);}
    private void drawRivers(Canvas c){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(8);p.setColor(Color.rgb(62,125,145));Path x=new Path();x.moveTo(430,95);x.cubicTo(420,210,355,255,390,360);x.cubicTo(430,450,600,470,665,545);c.drawPath(x,p);Path y=new Path();y.moveTo(780,455);y.cubicTo(760,535,710,570,620,690);c.drawPath(y,p);p.setStyle(Paint.Style.FILL);}
    private void drawMountains(Canvas c){p.setColor(Color.rgb(92,82,73));for(int x=630;x<760;x+=28){Path t=new Path();t.moveTo(x,170);t.lineTo(x+16,115-(x%3)*10);t.lineTo(x+32,170);t.close();c.drawPath(t,p);}p.setColor(Color.rgb(120,61,38));for(int x=810;x<980;x+=36){Path t=new Path();t.moveTo(x,165);t.lineTo(x+20,80+(x%4)*8);t.lineTo(x+40,165);t.close();c.drawPath(t,p);}}
    private void drawPin(Canvas c,Pin pin){p.setColor(pin.player?Color.rgb(235,193,93):Color.rgb(215,184,112));c.drawCircle(pin.x,pin.y,pin.player?12:8,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawCircle(pin.x,pin.y,pin.player?22:14,p);p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF,pin.player?android.graphics.Typeface.BOLD:android.graphics.Typeface.NORMAL));p.setTextSize(pin.player?18:15);p.setColor(Color.rgb(242,236,215));c.drawText(pin.name,pin.x,pin.y-24,p);}
    @Override public boolean onTouchEvent(MotionEvent e){sd.onTouchEvent(e);switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:last.set(e.getX(),e.getY());drag=true;break;case MotionEvent.ACTION_MOVE:if(drag&&!sd.isInProgress()){tx+=e.getX()-last.x;ty+=e.getY()-last.y;last.set(e.getX(),e.getY());invalidate();}break;case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:drag=false;break;}return true;}
    private static final class Pin{final String name;final float x,y;final boolean player;Pin(String n,float x,float y,boolean p){name=n;this.x=x;this.y=y;player=p;}}
}
