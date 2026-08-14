package lt.vaeloria.ooc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class WorldMapView extends View {
    public interface Listener { void onLocationSelected(String name, int danger); }
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Node> nodes = new ArrayList<>();
    private final ScaleGestureDetector scaler;
    private Listener listener;
    private float scale = 1f, tx = 0f, ty = 0f, lastX, lastY;
    private boolean dragging = false;
    private String current = "Luminara", selected="";

    private static class Node {
        String oldName, name; float x,y; int danger;
        Node(String oldName,String name,float x,float y,int d){this.oldName=oldName;this.name=name;this.x=x;this.y=y;this.danger=d;}
    }

    public WorldMapView(Context c) {
        super(c);setBackgroundColor(Color.rgb(7,15,22));setMinimumHeight(dp(450));
        nodes.add(new Node("Luminara","Luminara",500,330,3));
        nodes.add(new Node("Veyrhold","Veyrhold",665,255,3));
        nodes.add(new Node("Aurelion Reach","Aureliono Pakraštys",760,405,4));
        nodes.add(new Node("Crown of Aster","Asterio Karūna",350,195,4));
        nodes.add(new Node("The Starfall Vault","Žvaigždėkritos Skliautas",540,495,7));
        nodes.add(new Node("The Glasswood","Stiklo Giria",735,155,5));
        nodes.add(new Node("The Hollow Spire","Tuščiavidurė Smailė",320,555,8));
        nodes.add(new Node("Kharad Vorn","Kharad Vorn",1140,280,5));
        nodes.add(new Node("Dragonwake Peaks","Drakono Pabudimo Viršūnės",1330,155,8));
        nodes.add(new Node("Ashen Crown Citadel","Pelenų Karūnos Citadelė",965,210,7));
        nodes.add(new Node("The Sapphire Expanse","Safyro Platybės",1010,535,7));
        nodes.add(new Node("Everspring Vale","Amžinojo Šaltinio Slėnis",915,650,4));
        nodes.add(new Node("The Verdant Labyrinth","Žaliasis Labirintas",1135,735,8));
        nodes.add(new Node("Mire of Saints","Šventųjų Pelkynas",790,755,6));
        scaler = new ScaleGestureDetector(c, new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override public boolean onScale(ScaleGestureDetector d){scale=Math.max(.9f,Math.min(3.6f,scale*d.getScaleFactor()));invalidate();return true;}
        });
    }

    public void setListener(Listener l){listener=l;}
    public void setCurrentLocation(String s){current=s==null?"Luminara":s;invalidate();}
    public String getSelected(){return selected;}
    public void resetView(){scale=1f;tx=ty=0f;invalidate();}

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);float fit=Math.min(getWidth()/1600f,getHeight()/900f);
        c.save();c.translate(getWidth()/2f,getHeight()/2f);c.scale(fit*scale,fit*scale);c.translate(-800+tx/(fit*scale),-450+ty/(fit*scale));drawOcean(c);drawContinents(c);drawRoutes(c);drawNodes(c);c.restore();drawOverlay(c);
    }

    private void drawOcean(Canvas c){p.setColor(Color.rgb(10,28,39));p.setStyle(Paint.Style.FILL);c.drawRect(0,0,1600,900,p);p.setColor(Color.argb(45,92,142,170));p.setStrokeWidth(2);p.setStyle(Paint.Style.STROKE);for(int y=60;y<900;y+=90)c.drawLine(0,y,1600,y,p);}
    private void drawContinents(Canvas c){
        p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(27,52,49));Path a=new Path();a.moveTo(150,220);a.cubicTo(245,120,420,95,575,130);a.cubicTo(725,145,835,235,850,365);a.cubicTo(845,490,760,570,630,600);a.cubicTo(500,650,330,620,210,540);a.cubicTo(120,455,105,325,150,220);a.close();c.drawPath(a,p);
        p.setColor(Color.rgb(63,49,42));Path d=new Path();d.moveTo(870,130);d.cubicTo(1000,60,1180,55,1340,105);d.cubicTo(1460,160,1510,280,1450,390);d.cubicTo(1370,490,1220,520,1070,485);d.cubicTo(930,455,845,355,855,240);d.cubicTo(855,190,860,155,870,130);d.close();c.drawPath(d,p);
        p.setColor(Color.rgb(39,58,42));Path l=new Path();l.moveTo(760,560);l.cubicTo(875,500,1010,515,1130,555);l.cubicTo(1260,585,1405,620,1480,710);l.cubicTo(1435,815,1265,850,1110,835);l.cubicTo(945,855,785,820,720,730);l.cubicTo(700,660,710,605,760,560);l.close();c.drawPath(l,p);
        p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));p.setTextSize(31);p.setColor(Color.argb(110,230,223,196));c.drawText("ASTERRA",410,405,p);c.drawText("DRAVENN",1100,350,p);c.drawText("LYSARA",1060,720,p);
    }
    private void drawRoutes(Canvas c){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setColor(Color.argb(135,214,182,107));line(c,"Luminara","Veyrhold");line(c,"Luminara","Aurelion Reach");line(c,"Luminara","Crown of Aster");line(c,"Luminara","The Starfall Vault");line(c,"Veyrhold","The Glasswood");line(c,"Aurelion Reach","Kharad Vorn");line(c,"Kharad Vorn","Dragonwake Peaks");line(c,"Aurelion Reach","The Sapphire Expanse");line(c,"The Sapphire Expanse","Everspring Vale");line(c,"Everspring Vale","The Verdant Labyrinth");line(c,"Everspring Vale","Mire of Saints");line(c,"Luminara","The Hollow Spire");p.setPathEffect(new android.graphics.DashPathEffect(new float[]{14,10},0));p.setColor(Color.argb(180,111,182,214));line(c,"Luminara","Kharad Vorn");line(c,"Aurelion Reach","The Sapphire Expanse");p.setPathEffect(null);}
    private Node find(String n){for(Node x:nodes)if(x.oldName.equals(n)||x.name.equals(n))return x;return null;}
    private void line(Canvas c,String a,String b){Node x=find(a),y=find(b);if(x!=null&&y!=null)c.drawLine(x.x,x.y,y.x,y.y,p);}

    private void drawNodes(Canvas c){
        p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(21);p.setStyle(Paint.Style.FILL);
        for(Node n:nodes){boolean here=n.name.equals(current)||n.oldName.equals(current),sel=n.name.equals(selected);int color=here?Color.rgb(244,214,138):dangerColor(n.danger);p.setColor(color);c.drawCircle(n.x,n.y,here?18:(sel?17:13),p);if(here||sel){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sel?5:4);p.setColor(Color.argb(180,sel?82:244,sel?177:214,sel?167:138));c.drawCircle(n.x,n.y,sel?31:28,p);p.setStyle(Paint.Style.FILL);}drawNodeLabel(c,n,color);}
    }
    private void drawNodeLabel(Canvas c,Node n,int accent){String s=n.name;p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(20);float tw=p.measureText(s),pad=8;boolean left=n.x>1180;float x=left?n.x-18-tw:n.x+18,y=n.y-11;RectF bg=new RectF(x-pad,y-22,x+tw+pad,y+6);p.setColor(Color.argb(170,7,15,22));c.drawRoundRect(bg,7,7,p);p.setColor(Color.rgb(235,238,233));c.drawText(s,x,y,p);p.setColor(accent);c.drawRect(left?bg.right-4:bg.left,bg.top,left?bg.right:bg.left+4,bg.bottom,p);}
    private int dangerColor(int d){if(d>=8)return Color.rgb(205,75,70);if(d>=6)return Color.rgb(220,134,68);if(d>=4)return Color.rgb(206,177,84);return Color.rgb(89,178,129);}

    private void drawOverlay(Canvas c){p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(220,8,16,24));c.drawRoundRect(dp(10),dp(10),dp(230),dp(65),dp(14),dp(14),p);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(11));p.setColor(Color.rgb(244,214,138));c.drawText("VAELORIA",dp(22),dp(33),p);p.setTypeface(Typeface.DEFAULT);p.setTextSize(dp(8));p.setColor(Color.rgb(190,203,206));c.drawText("Priartink · tempk · paliesk",dp(22),dp(51),p);}

    @Override public boolean onTouchEvent(MotionEvent e){scaler.onTouchEvent(e);if(scaler.isInProgress())return true;switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:lastX=e.getX();lastY=e.getY();dragging=false;return true;case MotionEvent.ACTION_MOVE:{float dx=e.getX()-lastX,dy=e.getY()-lastY;if(Math.abs(dx)+Math.abs(dy)>4)dragging=true;tx+=dx;ty+=dy;lastX=e.getX();lastY=e.getY();invalidate();return true;}case MotionEvent.ACTION_UP:if(!dragging)selectAt(e.getX(),e.getY());return true;}return true;}
    private void selectAt(float sx,float sy){float fit=Math.min(getWidth()/1600f,getHeight()/900f);float wx=(sx-getWidth()/2f-tx)/(fit*scale)+800,wy=(sy-getHeight()/2f-ty)/(fit*scale)+450;Node best=null;float bd=Float.MAX_VALUE;for(Node n:nodes){float dx=n.x-wx,dy=n.y-wy,d=dx*dx+dy*dy;if(d<bd){bd=d;best=n;}}if(best!=null&&bd<82*82){selected=best.name;invalidate();if(listener!=null)listener.onLocationSelected(best.name,best.danger);}}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
}
