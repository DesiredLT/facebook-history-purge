package lt.vaeloria.ooc;

import android.app.*;
import android.os.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.InputType;
import android.view.*;
import android.widget.*;

import org.json.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class MainActivity extends Activity {
    private GameDb db;
    private SecretStore secrets;
    private LinearLayout root;
    private TextView header, hud, scene, status;
    private final Button[] choiceButtons = new Button[3];
    private EditText freeform;
    private Button actButton;
    private boolean busy = false;

    private static final int BG = Color.rgb(12, 17, 28);
    private static final int PANEL = Color.rgb(23, 31, 47);
    private static final int PANEL2 = Color.rgb(31, 42, 62);
    private static final int TEXT = Color.rgb(235, 240, 247);
    private static final int MUTED = Color.rgb(156, 169, 190);
    private static final int ACCENT = Color.rgb(102, 190, 255);
    private static final int GOLD = Color.rgb(244, 198, 92);

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        db = new GameDb(this);
        secrets = new SecretStore(this);
        db.seedIfNeeded();
        buildUi();
        refresh();
        if (secrets.get().isEmpty()) new Handler().postDelayed(this::showApiSettings, 450);
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(12), dp(14), dp(24));
        root.setBackgroundColor(BG);
        scroll.addView(root);

        header = text(18, TEXT, true);
        hud = text(13, MUTED, false);
        status = text(12, GOLD, false);
        scene = text(17, TEXT, false);
        scene.setLineSpacing(0, 1.14f);

        root.addView(header);
        root.addView(hud, lp(-1, -2, 0, 4));
        root.addView(status, lp(-1, -2, 0, 10));

        FrameLayout visual = new FrameLayout(this);
        visual.setMinimumHeight(dp(155));
        visual.setBackground(panel(PANEL, 18));
        visual.addView(new SceneView(this), new FrameLayout.LayoutParams(-1, -1));
        TextView badge = text(12, Color.WHITE, true);
        badge.setText("LUMINARA · THE BROKEN MERIDIAN");
        badge.setPadding(dp(12), dp(9), dp(12), dp(9));
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.LEFT);
        bp.setMargins(dp(10),0,0,dp(10));
        visual.addView(badge, bp);
        root.addView(visual, lp(-1, dp(155), 0, 12));

        LinearLayout scenePanel = new LinearLayout(this);
        scenePanel.setOrientation(LinearLayout.VERTICAL);
        scenePanel.setPadding(dp(15), dp(14), dp(15), dp(14));
        scenePanel.setBackground(panel(PANEL, 18));
        scenePanel.addView(scene);
        root.addView(scenePanel, lp(-1, -2, 0, 12));

        for (int i=0;i<3;i++) {
            final int idx=i;
            choiceButtons[i] = button("…", PANEL2);
            choiceButtons[i].setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            choiceButtons[i].setOnClickListener(v -> performAction(choiceButtons[idx].getText().toString()));
            root.addView(choiceButtons[i], lp(-1, dp(58), 0, 8));
        }

        freeform = new EditText(this);
        freeform.setTextColor(TEXT);
        freeform.setHintTextColor(MUTED);
        freeform.setHint("Arba parašyk savo veiksmą…");
        freeform.setMinLines(2);
        freeform.setMaxLines(5);
        freeform.setPadding(dp(13), dp(9), dp(13), dp(9));
        freeform.setBackground(panel(PANEL, 14));
        root.addView(freeform, lp(-1, -2, 0, 8));

        actButton = button("ATLIKTI VEIKSMĄ", ACCENT);
        actButton.setTextColor(Color.rgb(5,17,29));
        actButton.setOnClickListener(v -> {
            String a = freeform.getText().toString().trim();
            if (!a.isEmpty()) performAction(a);
        });
        root.addView(actButton, lp(-1, dp(52), 0, 14));

        TextView navTitle = text(11, MUTED, true);
        navTitle.setText("DUOMENYS IR ĮRANKIAI");
        root.addView(navTitle, lp(-1,-2,0,6));

        LinearLayout row1 = navRow();
        addNav(row1,"STATUSAI",v->showStats());
        addNav(row1,"INVENTORIUS",v->showInventory());
        addNav(row1,"QUESTAI",v->showQuests());
        root.addView(row1);
        LinearLayout row2 = navRow();
        addNav(row2,"ŽEMĖLAPIS",v->showMap());
        addNav(row2,"ŽURNALAS",v->showJournal());
        addNav(row2,"NUSTATYMAI",v->showTools());
        root.addView(row2);

        setContentView(scroll);
    }

    private void refresh() {
        header.setText("📍 " + db.get("location") + "  ·  🕘 " + db.get("clock") + "  ·  ⚠ " + db.get("danger"));
        hud.setText("HP " + db.get("hp") + "/100   ·   MANA " + db.get("mana") + "/100   ·   STAMINA " + db.get("stamina") + "/100   ·   AEONIC " + db.get("aeonic") + "/900");
        scene.setText(db.get("scene"));
        for(int i=0;i<3;i++) choiceButtons[i].setText((i+1)+". "+db.get("choice"+(i+1)));
        status.setText(busy ? "GM mąsto ir skaičiuoja pasekmes…" : "LOCAL SAVE · " + db.historyCount() + " ėjimų · Groq " + (secrets.get().isEmpty()?"NEPRIJUNGTAS":"PARUOŠTAS"));
    }

    private void performAction(String action) {
        if (busy) return;
        if (action == null || action.trim().isEmpty()) return;
        if (secrets.get().isEmpty()) { showApiSettings(); return; }
        busy=true; setInteractive(false); refresh();
        db.pushUndoSnapshot(action);
        String state = db.compactState();
        String recent = db.recentJournal(5);
        new Thread(() -> {
            try {
                JSONObject result = Groq.turn(secrets.get(), action, state, recent);
                db.applyTurn(result, action);
                runOnUiThread(() -> {
                    busy=false; setInteractive(true); freeform.setText(""); refresh();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    db.removeNewestUndo();
                    busy=false; setInteractive(true); refresh();
                    error("Groq užklausa nepavyko", cleanError(e));
                });
            }
        }).start();
    }

    private void setInteractive(boolean yes) {
        actButton.setEnabled(yes);
        for(Button b:choiceButtons) b.setEnabled(yes);
        freeform.setEnabled(yes);
    }

    private void showStats() {
        String body = "EINORAS\n"+
                "Chronologinis amžius: 201 m.\nBiologinis amžius: 20 m. · senėjimas sustabdytas\n\n"+
                "HP: "+db.get("hp")+" / 100\nMana: "+db.get("mana")+" / 100\nStamina: "+db.get("stamina")+" / 100\nAeonic: "+db.get("aeonic")+" / 900\n\n"+
                "92 baziniai statsai: 100 / 100 · LEGENDARY\nPost-cap mastery: century perfected + 30-year peer refinement\n\n"+
                "AKTYVŪS GEBĖJIMAI\n"+db.get("abilities");
        showTextDialog("Veikėjas / Stats", body);
    }

    private void showInventory() {
        showTextDialog("Inventorius · "+db.get("crowns")+" crowns", db.get("inventory"));
    }

    private void showQuests() {
        String q = "◆ THE BROKEN MERIDIAN\n"+
                "Tikslas: sukurti fiziškai stabilų, politiškai teisėtą ir paprastiems žmonėms naudojamą maršrutų tinklą, nepaverčiant Concordance monopoliu.\n\n"+
                "◆ AKTYVU · Investigate the first drift\nIštirk pirmą šviežią waygate drift incidentą ir atskirk faktus nuo prielaidų.\n\n"+
                "○ UŽRAKINTA · Secure three anchors\n○ UŽRAKINTA · Cross the Meridian\n○ UŽRAKINTA · Negotiate the First Meridian Charter\n\n"+
                "AKTYVIOS GIJOS\n"+db.get("threads");
        showTextDialog("Questai", q);
    }

    private void showJournal() {
        String j=db.recentJournal(20);
        showTextDialog("Paskutiniai ėjimai", j.isEmpty()?"Dar nėra atliktų ėjimų.":j);
    }

    private void showMap() {
        Dialog d = new Dialog(this, android.R.style.Theme_Material_NoActionBar_Fullscreen);
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setBackgroundColor(BG); box.setPadding(dp(12),dp(10),dp(12),dp(12));
        TextView title=text(17,TEXT,true); title.setText("VAELORIA · PASAULIO ŽEMĖLAPIS"); box.addView(title);
        TextView sub=text(12,MUTED,false); sub.setText("◆ Einoras: Luminara · bakstelėk vietą informacijai"); box.addView(sub, lp(-1,-2,0,8));
        WorldMap map=new WorldMap(this); box.addView(map,new LinearLayout.LayoutParams(-1,0,1));
        Button close=button("GRĮŽTI",PANEL2); close.setOnClickListener(v->d.dismiss()); box.addView(close,lp(-1,dp(50),0,0));
        d.setContentView(box); d.show();
    }

    private void showTools() {
        String[] items={"Groq API raktas","Atšaukti paskutinį ėjimą","Eksportuoti save į iškarpinę","Importuoti save iš iškarpinės","Atstatyti kanoninį checkpointą"};
        new AlertDialog.Builder(this).setTitle("Nustatymai ir sauga").setItems(items,(d,w)->{
            if(w==0) showApiSettings();
            if(w==1) { if(db.undo()) refresh(); else toast("Nėra ką atšaukti"); }
            if(w==2) exportSave();
            if(w==3) importSave();
            if(w==4) confirmReset();
        }).setNegativeButton("Uždaryti",null).show();
    }

    private void showApiSettings() {
        EditText input=new EditText(this); input.setSingleLine(true); input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); input.setHint("gsk_…"); input.setText(secrets.get());
        int p=dp(20); FrameLayout wrap=new FrameLayout(this); wrap.setPadding(p,0,p,0); wrap.addView(input);
        new AlertDialog.Builder(this).setTitle("Groq Free Tier API raktas")
                .setMessage("Raktas šifruojamas Android Keystore ir lieka tik šiame telefone. Į GitHub ar save failą jis nepatenka.")
                .setView(wrap)
                .setPositiveButton("Išsaugoti",(d,w)->{ String k=input.getText().toString().trim(); if(!k.isEmpty()) secrets.put(k); refresh(); })
                .setNegativeButton("Vėliau",null).show();
    }

    private void exportSave() {
        android.content.ClipboardManager cm=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(android.content.ClipData.newPlainText("Vaeloria save",db.exportJson().toString()));
        toast("Save nukopijuotas į iškarpinę");
    }

    private void importSave() {
        android.content.ClipboardManager cm=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        if(!cm.hasPrimaryClip()) {toast("Iškarpinė tuščia");return;}
        try {
            String s=cm.getPrimaryClip().getItemAt(0).coerceToText(this).toString();
            JSONObject j=new JSONObject(s);
            new AlertDialog.Builder(this).setTitle("Importuoti save?").setMessage("Dabartinė būsena bus pakeista importuota.")
                    .setPositiveButton("Importuoti",(d,w)->{db.importJson(j);refresh();toast("Importuota");}).setNegativeButton("Atšaukti",null).show();
        } catch(Exception e){error("Importas nepavyko","Iškarpinėje nėra tinkamo Vaeloria save JSON.");}
    }

    private void confirmReset() {
        new AlertDialog.Builder(this).setTitle("Atstatyti checkpointą?")
                .setMessage("Bus grįžta į 923 m. Luminara būseną prieš The Broken Meridian pradžią. Groq raktas liks.")
                .setPositiveButton("Atstatyti",(d,w)->{db.reset();refresh();})
                .setNegativeButton("Atšaukti",null).show();
    }

    private void showTextDialog(String title,String body) {
        TextView t=text(15,TEXT,false); t.setText(body); t.setPadding(dp(20),dp(14),dp(20),dp(20)); t.setTextIsSelectable(true);
        ScrollView s=new ScrollView(this); s.setBackgroundColor(BG); s.addView(t);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle(title).setView(s).setPositiveButton("Uždaryti",null).create(); dlg.show();
    }

    private void error(String title,String msg){ new AlertDialog.Builder(this).setTitle(title).setMessage(msg).setPositiveButton("Gerai",null).show(); }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private String cleanError(Exception e){String m=e.getMessage(); return m==null?e.getClass().getSimpleName():m.replace("gsk_","***");}

    private LinearLayout navRow(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);return r;}
    private void addNav(LinearLayout r,String label,View.OnClickListener l){Button b=button(label,PANEL);b.setTextSize(11);b.setOnClickListener(l);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(48),1);p.setMargins(dp(3),dp(3),dp(3),dp(3));r.addView(b,p);}
    private TextView text(float size,int color,boolean bold){TextView t=new TextView(this);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private Button button(String label,int color){Button b=new Button(this);b.setText(label);b.setTextColor(TEXT);b.setTextSize(13);b.setAllCaps(false);b.setBackground(panel(color,14));b.setPadding(dp(12),0,dp(12),0);return b;}
    private GradientDrawable panel(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
    private LinearLayout.LayoutParams lp(int w,int h,int top,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.setMargins(0,dp(top),0,dp(bottom));return p;}
    private int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}

    // ---------------- LOCAL SQLITE ----------------
    static class GameDb extends SQLiteOpenHelper {
        private static final String NAME="vaeloria.db";
        GameDb(Context c){super(c,NAME,null,1);}
        @Override public void onCreate(SQLiteDatabase d){
            d.execSQL("CREATE TABLE state(k TEXT PRIMARY KEY,v TEXT NOT NULL)");
            d.execSQL("CREATE TABLE turns(id INTEGER PRIMARY KEY AUTOINCREMENT, action TEXT, result TEXT, created INTEGER)");
            d.execSQL("CREATE TABLE undo(id INTEGER PRIMARY KEY AUTOINCREMENT, snapshot TEXT NOT NULL, action TEXT, created INTEGER)");
        }
        @Override public void onUpgrade(SQLiteDatabase d,int a,int b){}
        void seedIfNeeded(){if(get("seeded").isEmpty()) reset();}
        synchronized String get(String k){try(Cursor c=getReadableDatabase().rawQuery("SELECT v FROM state WHERE k=?",new String[]{k})){return c.moveToFirst()?c.getString(0):"";}}
        synchronized void put(String k,String v){android.content.ContentValues cv=new android.content.ContentValues();cv.put("k",k);cv.put("v",v==null?"":v);getWritableDatabase().insertWithOnConflict("state",null,cv,SQLiteDatabase.CONFLICT_REPLACE);}
        synchronized void reset(){SQLiteDatabase d=getWritableDatabase();d.beginTransaction();try{d.delete("state",null,null);d.delete("turns",null,null);d.delete("undo",null,null);
            put("seeded","1");put("location","Luminara");put("clock","923 m. · 09:20");put("world_minute","95358680");put("danger","3/10");
            put("hp","100");put("mana","0");put("stamina","100");put("aeonic","180");put("crowns","1062400");
            put("scene","Luminara rytas prasideda neįprastai: į Concordance rankas patenka karavano manifestas, nors pats karavanas dar neatvyko. Dokumento žymos rodo, kad jis turėjo būti atgabentas būtent tuo karavanu. Tai pirmas šviežias ženklas, kad Meridian maršrutų laikas vėl slysta.");
            put("choice1","Apžiūrėti manifestą ir pirmiausia patikrinti, kas jame tikrai įrodyta");
            put("choice2","Vykti prie waygate ir laukti paties karavano");
            put("choice3","Susisiekti su Mira ir Kaelis bei palyginti jų nepriklausomus vertinimus");
            put("abilities","• Aeonic Bastion\n• Continuity Lattice\n• Catastrophic Regeneration\n• Null-Adaptive Physiology\n• Reflexive Spatial Evasion\n• Adaptive Counterweaving\n• Relic Symbiosis\n• Temporal Parallax Discrimination\n• Unknown-Rule Calibration\n• Decentered Mastery\n• Conditional Causality Framing\n• Concordance Adapter Framing");
            put("inventory","⚔ Asterion Edge · legendary · EQUIPPED · synced\n🛡 Sevenfold Mantle · legendary · EQUIPPED · synced\n◈ Resonance Signet · legendary · synced\n◈ Nullglass Prism · legendary · synced\n◈ Meridian Key · legendary · synced\n◈ Dragonwake Concord Scale · legendary · synced\n◇ Arkforge Seed · ancient · anchored to Axiom Crucible\n◇ Heart of Still Thunder · ancient\n◇ Living Rune Seed · ancient\n◇ Orison Astrolabe · ancient\n◇ Starfall Compass · ancient\n▣ Wayfold Satchel · rare · EQUIPPED\n✦ Triune Concordance Seal · unique credential");
            put("threads","• Open Horizons: The Broken Meridian · priority 96 · mystery 82\n• Orison Contact Protocol · priority 90 · mystery 85\n• Waygate Drift · priority 82 · mystery 78\n• Concordance Stewardship Governance · priority 76\n• Draconic Succession · priority 74");
            d.setTransactionSuccessful();}finally{d.endTransaction();}}
        synchronized JSONObject stateJson(){JSONObject j=new JSONObject();try(Cursor c=getReadableDatabase().rawQuery("SELECT k,v FROM state",null)){while(c.moveToNext())try{j.put(c.getString(0),c.getString(1));}catch(Exception ignored){}}return j;}
        synchronized JSONObject exportJson(){JSONObject j=new JSONObject();try{j.put("format","vaeloria_local_save_v1");j.put("state",stateJson());j.put("exported_at",System.currentTimeMillis());}catch(Exception ignored){}return j;}
        synchronized void importJson(JSONObject j){JSONObject s=j.optJSONObject("state");if(s==null)return;SQLiteDatabase d=getWritableDatabase();d.beginTransaction();try{d.delete("state",null,null);Iterator<String> it=s.keys();while(it.hasNext()){String k=it.next();put(k,s.optString(k,""));}put("seeded","1");d.setTransactionSuccessful();}finally{d.endTransaction();}}
        synchronized void pushUndoSnapshot(String action){android.content.ContentValues cv=new android.content.ContentValues();cv.put("snapshot",stateJson().toString());cv.put("action",action);cv.put("created",System.currentTimeMillis());getWritableDatabase().insert("undo",null,cv);getWritableDatabase().execSQL("DELETE FROM undo WHERE id NOT IN (SELECT id FROM undo ORDER BY id DESC LIMIT 20)");}
        synchronized void removeNewestUndo(){getWritableDatabase().execSQL("DELETE FROM undo WHERE id=(SELECT MAX(id) FROM undo)");}
        synchronized boolean undo(){SQLiteDatabase d=getWritableDatabase();try(Cursor c=d.rawQuery("SELECT id,snapshot FROM undo ORDER BY id DESC LIMIT 1",null)){if(!c.moveToFirst())return false;long id=c.getLong(0);JSONObject s=new JSONObject(c.getString(1));d.beginTransaction();try{d.delete("state",null,null);Iterator<String>it=s.keys();while(it.hasNext()){String k=it.next();put(k,s.optString(k,""));}d.delete("undo","id=?",new String[]{String.valueOf(id)});d.setTransactionSuccessful();}finally{d.endTransaction();}return true;}catch(Exception e){return false;}}
        synchronized int historyCount(){try(Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM turns",null)){return c.moveToFirst()?c.getInt(0):0;}}
        synchronized String recentJournal(int n){StringBuilder b=new StringBuilder();try(Cursor c=getReadableDatabase().rawQuery("SELECT action,result FROM turns ORDER BY id DESC LIMIT "+Math.max(1,Math.min(n,30)),null)){int i=1;while(c.moveToNext()){b.append(i++).append(". TU: ").append(c.getString(0)).append("\n   GM: ").append(c.getString(1)).append("\n\n");}}return b.toString().trim();}
        String compactState(){return "Location="+get("location")+"; time="+get("clock")+"; world_minute="+get("world_minute")+"; HP="+get("hp")+"; mana="+get("mana")+"; stamina="+get("stamina")+"; aeonic="+get("aeonic")+"; crowns="+get("crowns")+"; scene="+get("scene");}
        synchronized void applyTurn(JSONObject r,String action){
            String narrative=r.optString("narrative","Veiksmas įvyko, bet GM nepateikė aprašymo."); put("scene",narrative);
            JSONArray ch=r.optJSONArray("choices");if(ch!=null)for(int i=0;i<3&&i<ch.length();i++)put("choice"+(i+1),ch.optString(i,"Tęsti atsargiai"));
            applyInt("hp",r.optInt("hp_delta",0),0,100);applyInt("mana",r.optInt("mana_delta",0),0,100);applyInt("stamina",r.optInt("stamina_delta",0),0,100);applyInt("aeonic",r.optInt("aeonic_delta",0),0,900);
            int advance=Math.max(0,Math.min(r.optInt("time_advance_minutes",5),1440));long wm=parseLong(get("world_minute"),95358680)+advance;put("world_minute",String.valueOf(wm));put("clock",clockFromWorldMinute(wm));
            String loc=r.optString("location","").trim();if(!loc.isEmpty())put("location",loc);
            android.content.ContentValues cv=new android.content.ContentValues();cv.put("action",action);cv.put("result",narrative);cv.put("created",System.currentTimeMillis());getWritableDatabase().insert("turns",null,cv);
        }
        void applyInt(String k,int delta,int min,int max){int v=(int)parseLong(get(k),min);v=Math.max(min,Math.min(max,v+delta));put(k,String.valueOf(v));}
        static long parseLong(String s,long d){try{return Long.parseLong(s);}catch(Exception e){return d;}}
        static String clockFromWorldMinute(long m){long day=m/1440;int min=(int)(m%1440);int h=min/60;int mm=min%60;long year=740+day/360;long yday=day%360+1;return String.format(Locale.US,"%d m. · d.%d · %02d:%02d",year,yday,h,mm);}
    }

    // ---------------- GROQ GAME MASTER ----------------
    static class Groq {
        static JSONObject turn(String apiKey,String action,String state,String recent) throws Exception {
            URL url=new URL("https://api.groq.com/openai/v1/chat/completions");
            HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setRequestMethod("POST");c.setConnectTimeout(15000);c.setReadTimeout(60000);c.setDoOutput(true);c.setRequestProperty("Authorization","Bearer "+apiKey);c.setRequestProperty("Content-Type","application/json");
            JSONObject req=new JSONObject();req.put("model","openai/gpt-oss-120b");req.put("temperature",0.75);req.put("max_completion_tokens",900);
            JSONArray messages=new JSONArray();
            messages.put(new JSONObject().put("role","system").put("content",SYSTEM));
            messages.put(new JSONObject().put("role","user").put("content","AUTHORITATIVE STATE:\n"+state+"\n\nRECENT TURNS:\n"+recent+"\n\nPLAYER ACTION:\n"+action));
            req.put("messages",messages);
            JSONObject schema=new JSONObject();schema.put("type","object");schema.put("additionalProperties",false);
            JSONObject props=new JSONObject();
            props.put("narrative",new JSONObject().put("type","string"));
            props.put("choices",new JSONObject().put("type","array").put("minItems",3).put("maxItems",3).put("items",new JSONObject().put("type","string")));
            props.put("time_advance_minutes",new JSONObject().put("type","integer").put("minimum",0).put("maximum",1440));
            props.put("hp_delta",new JSONObject().put("type","integer").put("minimum",-100).put("maximum",100));
            props.put("mana_delta",new JSONObject().put("type","integer").put("minimum",-100).put("maximum",100));
            props.put("stamina_delta",new JSONObject().put("type","integer").put("minimum",-100).put("maximum",100));
            props.put("aeonic_delta",new JSONObject().put("type","integer").put("minimum",-900).put("maximum",900));
            props.put("location",new JSONObject().put("type","string"));
            schema.put("properties",props);schema.put("required",new JSONArray(Arrays.asList("narrative","choices","time_advance_minutes","hp_delta","mana_delta","stamina_delta","aeonic_delta","location")));
            JSONObject fmt=new JSONObject().put("type","json_schema").put("json_schema",new JSONObject().put("name","vaeloria_turn").put("strict",true).put("schema",schema));req.put("response_format",fmt);
            try(OutputStream os=c.getOutputStream()){os.write(req.toString().getBytes(StandardCharsets.UTF_8));}
            int code=c.getResponseCode();InputStream in=(code>=200&&code<300)?c.getInputStream():c.getErrorStream();String raw=read(in);
            if(code<200||code>=300)throw new IOException("HTTP "+code+": "+raw.substring(0,Math.min(raw.length(),500)));
            JSONObject outer=new JSONObject(raw);String content=outer.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");return new JSONObject(content);
        }
        static String read(InputStream in)throws Exception{if(in==null)return"";BufferedReader br=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();String l;while((l=br.readLine())!=null)b.append(l);return b.toString();}
        static final String SYSTEM="""
Tu esi Vaeloria OOC žaidimo Game Master. Atsakyk žaidimo fikcija LIETUVIŠKAI. Žaidėjas yra Einoras, 201 m. chronologinio ir 20 m. biologinio amžiaus, biologinis senėjimas sustabdytas. Visi 92 baziniai statsai yra Legendary 100/100, bet galia nėra automatinis problemų sprendimas. Dabartinis lankas: Open Horizons: The Broken Meridian. Pagrindinis tikslas: ištirti waygate drift, vėliau sukurti fiziškai stabilų, politiškai teisėtą ir paprastiems žmonėms prieinamą Meridian tinklą. NPC turi autonomiją; nežinomos taisyklės, politinė legitimacija ir nepriklausomi interesai negali būti apeiti vien raw power. Nesėkmė ir dalinė sėkmė galimos ir turi išlikti kanone. Žaidėjas deklaruoja ketinimą, o ne garantuotą rezultatą. Combat pagal nutylėjimą multi-turn, ne vieno sakinio auto-win. Pasaulio laikas juda tik tiek, kiek sunaudoja veiksmas. Saugok tęstinumą su AUTHORITATIVE STATE ir RECENT TURNS. Narrative turi būti kompaktiškas: paprastai 2–4 sakiniai. Po kiekvieno neišbaigto veiksmo duok lygiai 3 materialiai skirtingus, ne išsamius pasirinkimus; freeform visada leidžiamas. Neatskleisk slapto optimalaus pasirinkimo. Vietą keisk tik jei veiksmas realiai perkėlė Einorą. Delta laukus naudok tik realiems resursų pokyčiams.
Kanoniniai gebėjimai: Aeonic Bastion, Continuity Lattice, Catastrophic Regeneration, Null-Adaptive Physiology, Reflexive Spatial Evasion, Adaptive Counterweaving, Relic Symbiosis, Temporal Parallax Discrimination, Unknown-Rule Calibration, Decentered Mastery, Conditional Causality Framing, Concordance Adapter Framing. Svarbūs peer'ai: Mira Vey ir Kaelis Vey – buvę mokiniai, dabar nepriklausomi meistrai/training peers. Pagrindinis pradinis kabliukas: karavano manifestas Luminara pasirodė anksčiau už patį jį gabenusį karavaną.
""";
    }

    // ---------------- ANDROID KEYSTORE ----------------
    static class SecretStore {
        private final android.content.SharedPreferences prefs; private static final String ALIAS="vaeloria_groq_key";
        SecretStore(Context c){prefs=c.getSharedPreferences("secure",MODE_PRIVATE);}
        void put(String value){try{SecretKey key=key();Cipher ci=Cipher.getInstance("AES/GCM/NoPadding");ci.init(Cipher.ENCRYPT_MODE,key);byte[] ct=ci.doFinal(value.getBytes(StandardCharsets.UTF_8));prefs.edit().putString("ct",Base64.getEncoder().encodeToString(ct)).putString("iv",Base64.getEncoder().encodeToString(ci.getIV())).apply();}catch(Exception ignored){}}
        String get(){try{String a=prefs.getString("ct","");String b=prefs.getString("iv","");if(a.isEmpty()||b.isEmpty())return"";Cipher ci=Cipher.getInstance("AES/GCM/NoPadding");ci.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.getDecoder().decode(b)));return new String(ci.doFinal(Base64.getDecoder().decode(a)),StandardCharsets.UTF_8);}catch(Exception e){return"";}}
        SecretKey key()throws Exception{KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);if(ks.containsAlias(ALIAS))return((KeyStore.SecretKeyEntry)ks.getEntry(ALIAS,null)).getSecretKey();KeyGenerator kg=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");kg.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());return kg.generateKey();}
    }

    // ---------------- LIGHTWEIGHT VISUALS ----------------
    class SceneView extends View {
        Paint p=new Paint(1); SceneView(Context c){super(c);}
        protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();
            p.setShader(new LinearGradient(0,0,w,h,Color.rgb(27,45,74),Color.rgb(79,48,105),Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
            p.setColor(Color.argb(80,150,220,255));for(int i=0;i<7;i++){float cx=w*(.12f+i*.125f);float cy=h*(.32f+(i%2)*.13f);c.drawCircle(cx,cy,dp(22+i%3*7),p);}p.setColor(Color.argb(165,8,15,28));c.drawRect(0,h*.63f,w,h,p);
            p.setColor(Color.argb(200,225,240,255));for(int i=0;i<8;i++){float x=w*(.05f+i*.135f);float top=h*(.48f-(i%3)*.08f);c.drawRect(x,top,x+dp(20),h*.73f,p);} }
    }

    class WorldMap extends View {
        final Paint p=new Paint(1); final String[] names={"Luminara","Veyrhold","Aurelion Reach","Crown of Aster","Starfall Vault","Glasswood","Hollow Spire","Kharad Vorn","Dragonwake Peaks","Ashen Crown","Sapphire Expanse","Everspring Vale","Verdant Labyrinth","Mire of Saints"};
        final float[][] pos={{.31f,.36f},{.42f,.27f},{.48f,.44f},{.22f,.21f},{.34f,.55f},{.46f,.17f},{.20f,.62f},{.72f,.31f},{.84f,.17f},{.61f,.23f},{.64f,.59f},{.58f,.73f},{.72f,.82f},{.50f,.84f}};
        WorldMap(Context c){super(c);setBackgroundColor(Color.rgb(8,20,34));setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_UP){int best=0;float bd=99999;for(int i=0;i<pos.length;i++){float dx=e.getX()-getWidth()*pos[i][0],dy=e.getY()-getHeight()*pos[i][1],d=dx*dx+dy*dy;if(d<bd){bd=d;best=i;}}toast(names[best]+(best==0?" · ◆ Einoras":""));}return true;});}
        protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(33,69,66));c.drawOval(w*.05f,h*.08f,w*.54f,h*.68f,p);p.setColor(Color.rgb(76,59,59));c.drawOval(w*.53f,h*.06f,w*.96f,h*.52f,p);p.setColor(Color.rgb(43,74,57));c.drawOval(w*.42f,h*.49f,w*.94f,h*.95f,p);
            p.setStrokeWidth(dp(2));p.setColor(Color.argb(120,160,210,255));for(int i=1;i<7;i++)c.drawLine(w*pos[0][0],h*pos[0][1],w*pos[i][0],h*pos[i][1],p);
            p.setTextSize(dp(10));for(int i=0;i<names.length;i++){float x=w*pos[i][0],y=h*pos[i][1];p.setColor(i==0?GOLD:Color.rgb(205,220,234));c.drawCircle(x,y,dp(i==0?7:5),p);p.setColor(Color.WHITE);String n=names[i];if(i==4)n="Starfall";if(i==8)n="Dragonwake";if(i==10)n="Sapphire";if(i==12)n="Verdant";c.drawText(n,x+dp(7),y-dp(5),p);} }
    }
}
