from pathlib import Path
import base64
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

# 3) Local fantasy map asset decoded from tracked text, so CI can build it without external services.
asset_b64 = ROOT / "assets/world_map_v051.webp.b64"
drawable = RES / "drawable-nodpi"
drawable.mkdir(parents=True, exist_ok=True)
(drawable / "world_map_v051.webp").write_bytes(base64.b64decode(asset_b64.read_text(encoding="ascii")))

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

# 5) Replace schematic map renderer with rich offline bitmap while preserving existing API.
(JAVA / "WorldMapView.java").write_text(r'''package lt.vaeloria.ooc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class WorldMapView extends View {
    public interface Listener { void onLocationSelected(String name, int danger); }
    private static final class Node {
        final String oldName,name; final float x,y; final int danger;
        Node(String o,String n,float x,float y,int d){oldName=o;name=n;this.x=x;this.y=y;danger=d;}
        boolean matches(String s){return s!=null&&(name.equalsIgnoreCase(s)||oldName.equalsIgnoreCase(s));}
    }

    private final Bitmap map;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final List<Node> nodes=new ArrayList<>();
    private final ScaleGestureDetector scaler;
    private final GestureDetector gestures;
    private Listener listener;
    private String current="Luminara",selected="";
    private float fit=1f,zoom=1f,tx=0f,ty=0f,lastX,lastY;
    private boolean dragging=false;

    public WorldMapView(Context c){
        super(c);setBackgroundColor(Color.rgb(5,14,20));setMinimumHeight(dp(450));
        map=BitmapFactory.decodeResource(getResources(),R.drawable.world_map_v051);
        node("Luminara","Luminara",356,329,3); node("Crown of Aster","Asterio Karūna",181,137,4);
        node("The Glasswood","Stiklo Giria",508,114,5); node("Veyrhold","Veyrhold",462,237,3);
        node("Aurelion Reach","Aureliono Pakraštys",590,394,4); node("The Starfall Vault","Žvaigždėkritos Skliautas",409,503,7);
        node("The Hollow Spire","Tuščiavidurė Smailė",141,537,8); node("Ashen Crown Citadel","Pelenų Karūnos Citadelė",742,243,7);
        node("Kharad Vorn","Kharad Vorn",979,327,5); node("Dragonwake Peaks","Drakono Pabudimo Viršūnės",928,133,8);
        node("The Sapphire Expanse","Safyro Plynatės",811,521,7); node("Everspring Vale","Amžinojo Šaltinio Slėnis",716,623,4);
        node("Mire of Saints","Šventųjų Pelkynas",517,732,6); node("The Verdant Labyrinth","Žaliasis Labirintas",924,758,8);
        scaler=new ScaleGestureDetector(c,new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override public boolean onScale(ScaleGestureDetector d){
                if(map==null)return false;float old=zoom,nz=Math.max(1f,Math.min(5f,zoom*d.getScaleFactor()));
                float oldScale=fit*old,mx=(d.getFocusX()-tx)/oldScale,my=(d.getFocusY()-ty)/oldScale;
                zoom=nz;float ns=fit*zoom;tx=d.getFocusX()-mx*ns;ty=d.getFocusY()-my*ns;clampTranslation();invalidate();return true;
            }});
        gestures=new GestureDetector(c,new GestureDetector.SimpleOnGestureListener(){
            @Override public boolean onDoubleTap(MotionEvent e){if(zoom>1.05f)resetView();else zoomAt(e.getX(),e.getY(),2.2f);return true;}
        });
    }
    private void node(String o,String n,float x,float y,int d){nodes.add(new Node(o,n,x,y,d));}
    public void setListener(Listener l){listener=l;}
    public void setCurrentLocation(String s){current=s==null?"Luminara":s;invalidate();}
    public String getSelected(){return selected;}
    public void resetView(){if(map==null||getWidth()==0||getHeight()==0)return;fit=Math.min(getWidth()/(float)map.getWidth(),getHeight()/(float)map.getHeight());zoom=1f;tx=(getWidth()-map.getWidth()*fit)/2f;ty=(getHeight()-map.getHeight()*fit)/2f;invalidate();}
    @Override protected void onSizeChanged(int w,int h,int ow,int oh){super.onSizeChanged(w,h,ow,oh);resetView();}
    @Override protected void onDraw(Canvas c){super.onDraw(c);if(map==null)return;c.save();c.translate(tx,ty);float s=fit*zoom;c.scale(s,s);c.drawBitmap(map,0,0,p);drawMarker(c,s);c.restore();drawHint(c);}
    private Node currentNode(){for(Node n:nodes)if(n.matches(current))return n;return null;}
    private void drawMarker(Canvas c,float s){Node n=currentNode();if(n==null)return;float inv=1f/Math.max(.001f,s);p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(120,0,0,0));c.drawCircle(n.x,n.y,24*inv,p);p.setColor(Color.rgb(255,210,82));c.drawCircle(n.x,n.y,11*inv,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3.2f*inv);p.setColor(Color.WHITE);c.drawCircle(n.x,n.y,17*inv,p);p.setStyle(Paint.Style.FILL);}
    private void drawHint(Canvas c){p.setColor(Color.argb(205,7,18,26));c.drawRoundRect(dp(9),dp(9),dp(212),dp(57),dp(12),dp(12),p);p.setColor(Color.rgb(235,204,128));p.setTextSize(dp(10));p.setFakeBoldText(true);c.drawText("VAELORIA · ŽEMĖLAPIS",dp(19),dp(30),p);p.setFakeBoldText(false);p.setColor(Color.rgb(188,204,209));p.setTextSize(dp(7));c.drawText("žnybk · tempk · dukart bakstelėk",dp(19),dp(47),p);}
    private void zoomAt(float sx,float sy,float target){if(map==null)return;float os=fit*zoom,mx=(sx-tx)/os,my=(sy-ty)/os;zoom=Math.max(1f,Math.min(5f,target));float ns=fit*zoom;tx=sx-mx*ns;ty=sy-my*ns;clampTranslation();invalidate();}
    @Override public boolean onTouchEvent(MotionEvent e){gestures.onTouchEvent(e);scaler.onTouchEvent(e);switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:getParent().requestDisallowInterceptTouchEvent(true);lastX=e.getX();lastY=e.getY();dragging=false;return true;case MotionEvent.ACTION_MOVE:if(!scaler.isInProgress()){float dx=e.getX()-lastX,dy=e.getY()-lastY;if(Math.abs(dx)+Math.abs(dy)>5)dragging=true;if(zoom>1.01f){tx+=dx;ty+=dy;clampTranslation();invalidate();}lastX=e.getX();lastY=e.getY();}return true;case MotionEvent.ACTION_UP:if(!dragging)selectAt(e.getX(),e.getY());getParent().requestDisallowInterceptTouchEvent(false);return true;case MotionEvent.ACTION_CANCEL:getParent().requestDisallowInterceptTouchEvent(false);return true;}return true;}
    private void selectAt(float sx,float sy){if(map==null)return;float s=fit*zoom,mx=(sx-tx)/s,my=(sy-ty)/s;Node best=null;float bd=Float.MAX_VALUE;for(Node n:nodes){float dx=n.x-mx,dy=n.y-my,d=dx*dx+dy*dy;if(d<bd){bd=d;best=n;}}if(best!=null&&bd<70*70){selected=best.name;invalidate();if(listener!=null)listener.onLocationSelected(best.name,best.danger);}}
    private void clampTranslation(){if(map==null)return;float sw=map.getWidth()*fit*zoom,sh=map.getHeight()*fit*zoom,m=Math.min(getWidth(),getHeight())*.12f;if(sw<=getWidth())tx=(getWidth()-sw)/2f;else tx=Math.max(getWidth()-sw-m,Math.min(m,tx));if(sh<=getHeight())ty=(getHeight()-sh)/2f;else ty=Math.max(getHeight()-sh-m,Math.min(m,ty));}
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
    JAVA/"WorldMapView.java":["world_map_v051","BitmapFactory.decodeResource","onDoubleTap"],
    activity_path:["ACTION_CREATE_DOCUMENT","ACTION_OPEN_DOCUMENT","k.startsWith(\"gsk_\")"],
}
for path,needles in checks.items():
    text=path.read_text(encoding="utf-8")
    for needle in needles:
        if needle not in text: raise SystemExit(f"v0.5.1 patch assertion failed: {needle} missing in {path}")
print("Vaeloria v0.5.1 hardening + visual map patch applied")
