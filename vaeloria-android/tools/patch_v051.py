from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app"
JAVA = APP / "src/main/java/lt/vaeloria/ooc"
RES = APP / "src/main/res"

# 1) Version + build hardening.
gradle = (APP / "build.gradle").read_text(encoding="utf-8")
gradle = re.sub(r"versionCode\s+12\b", "versionCode 13", gradle)
gradle = re.sub(r"versionName\s+'0\.5\.0'", "versionName '0.5.1'", gradle)
gradle = gradle.replace("debug {\n            signingConfig signingConfigs.vaeloriaStable", "debug {\n            debuggable false\n            signingConfig signingConfigs.vaeloriaStable")
gradle = gradle.replace("release {\n            minifyEnabled false", "release {\n            debuggable false\n            minifyEnabled false")
(APP / "build.gradle").write_text(gradle, encoding="utf-8")

# 2) Manifest hardening and correct visible version.
manifest_path = APP / "src/main/AndroidManifest.xml"
manifest = manifest_path.read_text(encoding="utf-8")
manifest = manifest.replace('android:allowBackup="true"', 'android:allowBackup="false"')
manifest = re.sub(r'android:label="Vaeloria OOC [^"]+"', 'android:label="Vaeloria OOC 0.5.1"', manifest)
if 'android:debuggable=' not in manifest:
    manifest = manifest.replace('<application\n', '<application\n        android:debuggable="false"\n')
if 'android:usesCleartextTraffic=' not in manifest:
    manifest = manifest.replace('android:supportsRtl="true"', 'android:supportsRtl="true"\n        android:usesCleartextTraffic="false"')
if 'android:icon=' not in manifest:
    manifest = manifest.replace('android:label="Vaeloria OOC 0.5.1"', 'android:label="Vaeloria OOC 0.5.1"\n        android:icon="@drawable/ic_vaeloria"\n        android:roundIcon="@drawable/ic_vaeloria"')
manifest_path.write_text(manifest, encoding="utf-8")

# 3) Rich offline map renderer uses only local Canvas drawing; no external map service or runtime download.

# 4) Local app icon; no extra permissions or network needed.
icon_dir = RES / "drawable"
icon_dir.mkdir(parents=True, exist_ok=True)
(icon_dir / "ic_vaeloria.xml").write_text('''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#07131C" android:pathData="M0,0h108v108h-108z"/>
    <path android:fillColor="#D6B36A" android:pathData="M54,12 L68,38 L96,54 L68,70 L54,96 L40,70 L12,54 L40,38 Z"/>
    <path android:fillColor="#0E232E" android:pathData="M54,25 L62,45 L83,54 L62,63 L54,83 L46,63 L25,54 L46,45 Z"/>
    <path android:fillColor="#F1E7C8" android:pathData="M54,39 L59,49 L70,54 L59,59 L54,70 L49,59 L38,54 L49,49 Z"/>
</vector>\n''', encoding="utf-8")

# 5) Replace schematic map renderer with richer offline Canvas map while preserving the existing public API.
(JAVA / "WorldMapView.java").write_text(r'''package lt.vaeloria.ooc;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class WorldMapView extends View {
    public interface Listener { void onLocationSelected(String name, int danger); }
    private static final class Node {
        final String oldName,name; final float x,y; final int danger;
        Node(String o,String n,float x,float y,int d){oldName=o;name=n;this.x=x;this.y=y;danger=d;}
        boolean matches(String s){return s!=null&&(name.equalsIgnoreCase(s)||oldName.equalsIgnoreCase(s));}
    }
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); private final Random rng=new Random(51);
    private final List<Node> nodes=new ArrayList<>(); private final ScaleGestureDetector scaler; private final GestureDetector gestures;
    private Listener listener; private String current="Luminara",selected=""; private float scale=1f,tx=0,ty=0,lastX,lastY; private boolean dragging;

    public WorldMapView(Context c){super(c);setBackgroundColor(Color.rgb(6,17,25));setMinimumHeight(dp(450));
        node("Luminara","Luminara",500,330,3);node("Veyrhold","Veyrhold",665,255,3);node("Aurelion Reach","Aureliono Pakraštys",760,405,4);node("Crown of Aster","Asterio Karūna",350,195,4);node("The Starfall Vault","Žvaigždėkritos Skliautas",540,495,7);node("The Glasswood","Stiklo Giria",735,155,5);node("The Hollow Spire","Tuščiavidurė Smailė",320,555,8);node("Kharad Vorn","Kharad Vorn",1140,280,5);node("Dragonwake Peaks","Drakono Pabudimo Viršūnės",1330,155,8);node("Ashen Crown Citadel","Pelenų Karūnos Citadelė",965,210,7);node("The Sapphire Expanse","Safyro Platybės",1010,535,7);node("Everspring Vale","Amžinojo Šaltinio Slėnis",915,650,4);node("The Verdant Labyrinth","Žaliasis Labirintas",1135,735,8);node("Mire of Saints","Šventųjų Pelkynas",790,755,6);
        scaler=new ScaleGestureDetector(c,new ScaleGestureDetector.SimpleOnScaleGestureListener(){@Override public boolean onScale(ScaleGestureDetector d){float old=scale;scale=Math.max(.9f,Math.min(4.2f,scale*d.getScaleFactor()));float f=scale/old;tx=d.getFocusX()-(d.getFocusX()-tx)*f;ty=d.getFocusY()-(d.getFocusY()-ty)*f;invalidate();return true;}});
        gestures=new GestureDetector(c,new GestureDetector.SimpleOnGestureListener(){@Override public boolean onDoubleTap(MotionEvent e){if(scale>1.05f)resetView();else{scale=2.1f;tx=e.getX()-(e.getX()-tx)*2.1f;ty=e.getY()-(e.getY()-ty)*2.1f;invalidate();}return true;}});
    }
    private void node(String o,String n,float x,float y,int d){nodes.add(new Node(o,n,x,y,d));}
    public void setListener(Listener l){listener=l;} public void setCurrentLocation(String s){current=s==null?"Luminara":s;invalidate();} public String getSelected(){return selected;} public void resetView(){scale=1f;tx=ty=0f;invalidate();}

    @Override protected void onDraw(Canvas c){super.onDraw(c);float fit=Math.min(getWidth()/1600f,getHeight()/900f);c.save();c.translate(getWidth()/2f+tx,getHeight()/2f+ty);c.scale(fit*scale,fit*scale);c.translate(-800,-450);drawOcean(c);drawContinents(c);drawBiomes(c);drawRivers(c);drawRoutes(c);drawNodes(c);c.restore();drawOverlay(c);}
    private void drawOcean(Canvas c){p.setShader(new LinearGradient(0,0,0,900,Color.rgb(8,31,43),Color.rgb(5,18,30),Shader.TileMode.CLAMP));c.drawRect(0,0,1600,900,p);p.setShader(null);p.setColor(Color.argb(35,119,175,195));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);for(int y=40;y<900;y+=45){Path w=new Path();w.moveTo(0,y);for(int x=0;x<=1600;x+=80)w.quadTo(x+40,y+(x/80%2==0?6:-6),x+80,y);c.drawPath(w,p);}p.setStyle(Paint.Style.FILL);}
    private void drawContinents(Canvas c){p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(43,76,54));Path a=new Path();a.moveTo(150,220);a.cubicTo(245,120,420,95,575,130);a.cubicTo(725,145,835,235,850,365);a.cubicTo(845,490,760,570,630,600);a.cubicTo(500,650,330,620,210,540);a.cubicTo(120,455,105,325,150,220);a.close();c.drawPath(a,p);p.setColor(Color.rgb(74,53,43));Path d=new Path();d.moveTo(870,130);d.cubicTo(1000,60,1180,55,1340,105);d.cubicTo(1460,160,1510,280,1450,390);d.cubicTo(1370,490,1220,520,1070,485);d.cubicTo(930,455,845,355,855,240);d.cubicTo(855,190,860,155,870,130);d.close();c.drawPath(d,p);p.setColor(Color.rgb(48,82,58));Path l=new Path();l.moveTo(760,560);l.cubicTo(875,500,1010,515,1130,555);l.cubicTo(1260,585,1405,620,1480,710);l.cubicTo(1435,815,1265,850,1110,835);l.cubicTo(945,855,785,820,720,730);l.cubicTo(700,660,710,605,760,560);l.close();c.drawPath(l,p);}
    private void drawBiomes(Canvas c){drawForest(c,235,185,360,285,70,Color.rgb(29,67,43));drawForest(c,610,110,760,225,48,Color.rgb(24,66,48));drawForest(c,1025,660,1275,805,85,Color.rgb(30,70,44));drawMarsh(c,735,690,905,820);drawMountains(c,250,120,440,220,12,Color.rgb(106,103,92));drawMountains(c,850,110,1430,340,24,Color.rgb(91,77,70));drawVolcanic(c);drawCrystalField(c);drawLabyrinth(c);drawDesert(c,160,470,320,590);drawRegionLabels(c);}
    private void drawForest(Canvas c,int x1,int y1,int x2,int y2,int count,int color){rng.setSeed(x1*31L+y1);p.setColor(color);for(int i=0;i<count;i++){float x=x1+rng.nextFloat()*(x2-x1),y=y1+rng.nextFloat()*(y2-y1),r=5+rng.nextFloat()*7;c.drawCircle(x,y,r,p);p.setColor(Color.rgb(20,53,38));c.drawRect(x-1,y+r-1,x+1,y+r+7,p);p.setColor(color);}}
    private void drawMarsh(Canvas c,int x1,int y1,int x2,int y2){p.setColor(Color.argb(100,72,139,137));for(int y=y1;y<y2;y+=20)c.drawOval(x1,y,x2,y+8,p);p.setColor(Color.rgb(45,92,59));for(int x=x1;x<x2;x+=24){c.drawRect(x,y1+20,x+3,y1+42,p);c.drawLine(x+1,y1+24,x-7,y1+15,p);c.drawLine(x+1,y1+29,x+8,y1+20,p);}}
    private void drawMountains(Canvas c,int x1,int y1,int x2,int y2,int count,int color){rng.setSeed(x1*17L+y2);p.setColor(color);for(int i=0;i<count;i++){float x=x1+rng.nextFloat()*(x2-x1),y=y1+rng.nextFloat()*(y2-y1),h=25+rng.nextFloat()*45,w=18+rng.nextFloat()*22;Path t=new Path();t.moveTo(x-w,y+h);t.lineTo(x,y-h);t.lineTo(x+w,y+h);t.close();c.drawPath(t,p);p.setColor(Color.argb(125,225,224,214));Path s=new Path();s.moveTo(x,y-h);s.lineTo(x-w*.35f,y-h*.22f);s.lineTo(x+w*.28f,y-h*.08f);s.close();c.drawPath(s,p);p.setColor(color);}}
    private void drawVolcanic(Canvas c){p.setColor(Color.rgb(114,52,34));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);for(int i=0;i<9;i++){Path q=new Path();q.moveTo(1260+i*8,110+i*5);q.cubicTo(1210+i*6,175,1180+i*9,245,1090+i*14,330);c.drawPath(q,p);}p.setStyle(Paint.Style.FILL);}
    private void drawCrystalField(Canvas c){p.setColor(Color.rgb(91,168,191));for(int i=0;i<16;i++){float x=930+(i%6)*28,y=510+(i/6)*22;Path k=new Path();k.moveTo(x,y+15);k.lineTo(x+6,y-10);k.lineTo(x+12,y+15);k.close();c.drawPath(k,p);}}
    private void drawLabyrinth(Canvas c){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setColor(Color.rgb(81,126,66));for(int i=0;i<5;i++)c.drawOval(1070-i*7,690-i*7,1210+i*7,805+i*7,p);p.setStyle(Paint.Style.FILL);}
    private void drawDesert(Canvas c,int x1,int y1,int x2,int y2){p.setColor(Color.rgb(129,105,64));c.drawOval(x1,y1,x2,y2,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(Color.argb(100,230,202,137));for(int y=y1+20;y<y2;y+=20)c.drawArc(x1+10,y,x2-10,y+35,200,135,false,p);p.setStyle(Paint.Style.FILL);}
    private void drawRivers(Canvas c){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(8);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(Color.rgb(71,139,156));Path r=new Path();r.moveTo(430,120);r.cubicTo(470,210,430,310,510,390);r.cubicTo(590,470,650,520,760,575);c.drawPath(r,p);Path s=new Path();s.moveTo(980,520);s.cubicTo(935,610,900,690,835,820);c.drawPath(s,p);p.setStyle(Paint.Style.FILL);}
    private void drawRoutes(Canvas c){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setColor(Color.argb(175,218,181,105));line(c,"Luminara","Veyrhold");line(c,"Luminara","Aurelion Reach");line(c,"Luminara","Crown of Aster");line(c,"Luminara","The Starfall Vault");line(c,"Veyrhold","The Glasswood");line(c,"Aurelion Reach","Kharad Vorn");line(c,"Kharad Vorn","Dragonwake Peaks");line(c,"Aurelion Reach","The Sapphire Expanse");line(c,"The Sapphire Expanse","Everspring Vale");line(c,"Everspring Vale","The Verdant Labyrinth");line(c,"Everspring Vale","Mire of Saints");line(c,"Luminara","The Hollow Spire");p.setPathEffect(new DashPathEffect(new float[]{14,10},0));p.setColor(Color.argb(190,99,180,213));line(c,"Luminara","Kharad Vorn");p.setPathEffect(null);p.setStyle(Paint.Style.FILL);}
    private Node find(String n){for(Node x:nodes)if(x.oldName.equals(n)||x.name.equals(n))return x;return null;}private void line(Canvas c,String a,String b){Node x=find(a),y=find(b);if(x!=null&&y!=null)c.drawLine(x.x,x.y,y.x,y.y,p);}
    private void drawRegionLabels(Canvas c){p.setTypeface(Typeface.create(Typeface.SERIF,Typeface.BOLD));p.setTextSize(38);p.setColor(Color.argb(135,235,224,181));c.drawText("ASTERRA",300,390,p);c.drawText("DRAVENN",1060,360,p);c.drawText("LYSARA",1005,720,p);}
    private void drawNodes(Canvas c){p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(20);for(Node n:nodes){boolean here=n.matches(current),sel=n.name.equals(selected);int color=here?Color.rgb(255,213,92):dangerColor(n.danger);p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(185,6,15,22));c.drawCircle(n.x,n.y,here?25:20,p);p.setColor(color);c.drawCircle(n.x,n.y,here?13:9,p);if(here||sel){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(Color.WHITE);c.drawCircle(n.x,n.y,here?21:18,p);p.setStyle(Paint.Style.FILL);}drawNodeLabel(c,n);}}
    private void drawNodeLabel(Canvas c,Node n){String s=n.name;p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(18);float tw=p.measureText(s),x=n.x+18,y=n.y-14;if(n.x>1200)x=n.x-18-tw;p.setColor(Color.argb(185,5,14,20));c.drawRoundRect(x-7,y-20,x+tw+7,y+6,7,7,p);p.setColor(Color.rgb(240,236,217));c.drawText(s,x,y,p);}
    private int dangerColor(int d){if(d>=8)return Color.rgb(216,83,73);if(d>=6)return Color.rgb(222,138,72);if(d>=4)return Color.rgb(211,181,84);return Color.rgb(91,182,131);}
    private void drawOverlay(Canvas c){p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(220,6,16,23));c.drawRoundRect(dp(10),dp(10),dp(246),dp(66),dp(14),dp(14),p);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(11));p.setColor(Color.rgb(244,214,138));c.drawText("VAELORIA · PASAULIO ŽEMĖLAPIS",dp(20),dp(34),p);p.setTypeface(Typeface.DEFAULT);p.setTextSize(dp(8));p.setColor(Color.rgb(188,204,209));c.drawText("žnybk · tempk · dukart bakstelėk",dp(20),dp(52),p);}
    @Override public boolean onTouchEvent(MotionEvent e){gestures.onTouchEvent(e);scaler.onTouchEvent(e);if(scaler.isInProgress())return true;switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:getParent().requestDisallowInterceptTouchEvent(true);lastX=e.getX();lastY=e.getY();dragging=false;return true;case MotionEvent.ACTION_MOVE:{float dx=e.getX()-lastX,dy=e.getY()-lastY;if(Math.abs(dx)+Math.abs(dy)>4)dragging=true;tx+=dx;ty+=dy;lastX=e.getX();lastY=e.getY();invalidate();return true;}case MotionEvent.ACTION_UP:if(!dragging)selectAt(e.getX(),e.getY());getParent().requestDisallowInterceptTouchEvent(false);return true;case MotionEvent.ACTION_CANCEL:getParent().requestDisallowInterceptTouchEvent(false);return true;}return true;}
    private void selectAt(float sx,float sy){float fit=Math.min(getWidth()/1600f,getHeight()/900f);float wx=(sx-getWidth()/2f-tx)/(fit*scale)+800,wy=(sy-getHeight()/2f-ty)/(fit*scale)+450;Node best=null;float bd=Float.MAX_VALUE;for(Node n:nodes){float dx=n.x-wx,dy=n.y-wy,d=dx*dx+dy*dy;if(d<bd){bd=d;best=n;}}if(best!=null&&bd<75*75){selected=best.name;invalidate();if(listener!=null)listener.onLocationSelected(best.name,best.danger);}}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
}
''', encoding="utf-8")

# 6) User-facing cleanup + safer file-based save transfer instead of clipboard.
activity_path=JAVA/"VaeloriaActivity.java"
a=activity_path.read_text(encoding="utf-8")
a=a.replace('import java.util.*;\n', 'import java.util.*;\nimport java.io.*;\nimport java.nio.charset.StandardCharsets;\n')
a=a.replace('public class VaeloriaActivity extends Activity {\n', 'public class VaeloriaActivity extends Activity {\n    private static final int EXPORT_REQ=41, IMPORT_REQ=42;\n')
a=a.replace('"VIETINIS RPG · "+BuildConfig.VERSION_NAME', '"OOC · "+BuildConfig.VERSION_NAME')
a=a.replace('"post-cap"', '"meistriškumo"')
a=a.replace('POST-CAP', 'MEISTRIŠKUMO')
a=a.replace("if(k.length()<10)throw new Exception();", "if(!k.startsWith(\"gsk_\")||k.length()<20)throw new Exception();")
old_export='''void export(){((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Vaeloria išsaugojimas",db.exportSave()));Toast.makeText(this,"Išsaugojimas nukopijuotas",Toast.LENGTH_SHORT).show();}\n    void importSave(){ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(!cm.hasPrimaryClip()){Toast.makeText(this,"Iškarpinė tuščia",Toast.LENGTH_SHORT).show();return;}String x=cm.getPrimaryClip().getItemAt(0).coerceToText(this).toString();if(db.importSave(x)){state=db.loadState();feedback="Išsaugojimas importuotas";show("game");}else Toast.makeText(this,"Netinkamas Vaeloria išsaugojimas",Toast.LENGTH_SHORT).show();}'''
new_export='''void export(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").putExtra(Intent.EXTRA_TITLE,"vaeloria-save.json");startActivityForResult(i,EXPORT_REQ);}\n    void importSave(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,IMPORT_REQ);}\n    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;try{android.net.Uri u=data.getData();if(req==EXPORT_REQ){try(OutputStream os=getContentResolver().openOutputStream(u)){os.write(db.exportSave().getBytes(StandardCharsets.UTF_8));}Toast.makeText(this,"Išsaugojimas eksportuotas",Toast.LENGTH_SHORT).show();}else if(req==IMPORT_REQ){StringBuilder b=new StringBuilder();try(BufferedReader r=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(u),StandardCharsets.UTF_8))){String line;while((line=r.readLine())!=null)b.append(line);}if(db.importSave(b.toString())){state=db.loadState();feedback="Išsaugojimas importuotas";show("game");}else Toast.makeText(this,"Netinkamas Vaeloria išsaugojimas",Toast.LENGTH_SHORT).show();}}catch(Exception e){Toast.makeText(this,"Failo klaida: "+e.getMessage(),Toast.LENGTH_LONG).show();}}'''
if old_export in a:
    a=a.replace(old_export,new_export)
else:
    a=re.sub(r'void export\(\)\{.*?\}\n\s*void importSave\(\)\{.*?\}\n',new_export+'\n',a,flags=re.S)
activity_path.write_text(a,encoding="utf-8")

# 7) Strong assertions: fail CI instead of silently producing a partially hardened APK.
checks={
    APP/"build.gradle":["versionCode 13","versionName '0.5.1'","debuggable false"],
    manifest_path:['android:allowBackup="false"','android:debuggable="false"','Vaeloria OOC 0.5.1','android:usesCleartextTraffic="false"'],
    JAVA/"WorldMapView.java":["drawBiomes","drawVolcanic","onDoubleTap"],
    activity_path:["ACTION_CREATE_DOCUMENT","ACTION_OPEN_DOCUMENT","k.startsWith(\"gsk_\")"],
}
for path,needles in checks.items():
    text=path.read_text(encoding="utf-8")
    for needle in needles:
        if needle not in text: raise SystemExit(f"v0.5.1 patch assertion failed: {needle} missing in {path}")
print("Vaeloria v0.5.1 hardening + visual map patch applied")
