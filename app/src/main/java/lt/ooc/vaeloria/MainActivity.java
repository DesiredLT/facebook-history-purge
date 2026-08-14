package lt.ooc.vaeloria;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class MainActivity extends Activity {
    private static final int EXPORT_REQ=41, IMPORT_REQ=42;
    private static final int BG=Color.rgb(7,19,28), CARD=Color.rgb(14,35,46), CARD2=Color.rgb(18,43,55), GOLD=Color.rgb(214,179,106), TEXT=Color.rgb(237,239,232), MUTED=Color.rgb(157,178,183), GREEN=Color.rgb(91,176,133), BLUE=Color.rgb(94,159,204);
    private GameStore store; private SecureKeyStore keys; private GroqClient groq; private JSONObject state;
    private FrameLayout content; private TextView headerTitle,headerSub; private String tab="Žaidimas"; private boolean busy=false;
    private final Handler main=new Handler(Looper.getMainLooper());

    @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        store=new GameStore(this); keys=new SecureKeyStore(this); groq=new GroqClient(); state=store.load(); buildShell(); render();
        if(!keys.hasKey()) main.postDelayed(this::showApiKeyDialog,450);
    }

    private void buildShell(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG); root.setPadding(dp(12),dp(10),dp(12),dp(8));
        LinearLayout top=new LinearLayout(this); top.setOrientation(LinearLayout.HORIZONTAL); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(dp(4),dp(4),dp(4),dp(10));
        LinearLayout titles=new LinearLayout(this); titles.setOrientation(LinearLayout.VERTICAL);
        headerTitle=text("VAELORIA",22,TEXT,true); headerSub=text("",12,MUTED,false); titles.addView(headerTitle); titles.addView(headerSub);
        top.addView(titles,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
        Button gear=smallButton("⚙"); gear.setOnClickListener(v->{tab="Nustatymai";render();}); top.addView(gear,new LinearLayout.LayoutParams(dp(48),dp(44)));
        root.addView(top);
        content=new FrameLayout(this); root.addView(content,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));
        HorizontalScrollView navScroll=new HorizontalScrollView(this); navScroll.setHorizontalScrollBarEnabled(false); LinearLayout nav=new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL);
        String[] tabs={"Žaidimas","Veikėjas","Inventorius","Questai","Žemėlapis","Žurnalas"};
        for(String t:tabs){ Button bnav=smallButton(t); bnav.setOnClickListener(v->{tab=t;render();}); nav.addView(bnav,new LinearLayout.LayoutParams(dp(t.equals("Inventorius")?108:96),dp(48))); }
        navScroll.addView(nav); root.addView(navScroll,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(54)));
        setContentView(root);
    }

    private void render(){ try{
        JSONObject w=state.getJSONObject("world"); headerSub.setText(w.getString("location")+" · 923 m. · "+clock(w)); content.removeAllViews();
        if(tab.equals("Žaidimas")) renderGame(); else if(tab.equals("Veikėjas")) renderCharacter(); else if(tab.equals("Inventorius")) renderInventory(); else if(tab.equals("Questai")) renderQuest(); else if(tab.equals("Žemėlapis")) renderMap(); else if(tab.equals("Žurnalas")) renderJournal(); else renderSettings();
    }catch(Exception e){ showError("Būsenos klaida: "+e.getMessage()); } }

    private void renderGame() throws Exception {
        ScrollView sv=scroll(); LinearLayout box=column(); JSONObject sc=state.getJSONObject("scene"); JSONObject w=state.getJSONObject("world"); JSONObject r=state.getJSONObject("resources");
        box.addView(text("📍 "+sc.optString("location",w.getString("location"))+" · "+clock(w)+" · ⚠ "+sc.optInt("danger",w.optInt("danger",0)),13,MUTED,false));
        box.addView(space(8)); box.addView(cardTitle(sc.getString("title")));
        box.addView(text(sc.getString("text"),17,TEXT,false)); box.addView(space(12));
        LinearLayout hud=new LinearLayout(this); hud.setOrientation(LinearLayout.VERTICAL); hud.setPadding(dp(12),dp(10),dp(12),dp(10)); hud.setBackground(cardBg(CARD));
        hud.addView(resource("HP",r.getInt("hp"),r.getInt("hp_max"),GREEN)); hud.addView(resource("MANA",r.getInt("mana"),r.getInt("mana_max"),BLUE)); hud.addView(resource("STAMINA",r.getInt("stamina"),r.getInt("stamina_max"),GOLD)); hud.addView(resource("AEONIC",r.getInt("aeonic"),r.getInt("aeonic_max"),Color.rgb(170,120,220))); box.addView(hud);
        box.addView(space(12)); box.addView(text("Ką darai?",14,GOLD,true)); box.addView(space(6));
        JSONArray choices=sc.getJSONArray("choices"); for(int i=0;i<choices.length();i++){ JSONObject ch=choices.getJSONObject(i); Button b=actionButton(ch.getString("label")); final String a=ch.getString("action"); b.setOnClickListener(v->runAction(a)); b.setEnabled(!busy); box.addView(b); box.addView(space(7)); }
        EditText free=new EditText(this); free.setHint("Arba įrašyk savo veiksmą…"); free.setHintTextColor(MUTED); free.setTextColor(TEXT); free.setTextSize(16); free.setSingleLine(false); free.setMinLines(2); free.setMaxLines(4); free.setPadding(dp(12),dp(10),dp(12),dp(10)); free.setBackground(cardBg(CARD2)); box.addView(free);
        Button send=actionButton(busy?"Vykdoma…":"Atlikti veiksmą"); send.setEnabled(!busy); send.setOnClickListener(v->{String a=free.getText().toString().trim(); if(a.isEmpty())Toast.makeText(this,"Įrašyk veiksmą",Toast.LENGTH_SHORT).show(); else runAction(a);}); box.addView(space(7)); box.addView(send);
        sv.addView(box); content.addView(sv);
    }

    private void renderCharacter() throws Exception {
        ScrollView sv=scroll(); LinearLayout box=column(); JSONObject c=state.getJSONObject("character"),r=state.getJSONObject("resources");
        box.addView(cardTitle(c.getString("name"))); box.addView(kv("Statusas","Aktyvus · biologinis senėjimas sustabdytas")); box.addView(kv("Amžius","201 chronologinis / 20 biologinis")); box.addView(kv("Bazė",c.getString("base_stats"))); box.addView(kv("Rolė",c.getString("role"))); box.addView(space(10));
        box.addView(text("RESURSAI",13,GOLD,true)); box.addView(kv("HP",r.getInt("hp")+" / "+r.getInt("hp_max"))); box.addView(kv("Mana",r.getInt("mana")+" / "+r.getInt("mana_max"))); box.addView(kv("Stamina",r.getInt("stamina")+" / "+r.getInt("stamina_max"))); box.addView(kv("Aeonic",r.getInt("aeonic")+" / "+r.getInt("aeonic_max"))); box.addView(kv("Crowns",String.format("%,d",state.getLong("crowns"))));
        box.addView(space(12)); box.addView(text("POST-CAP GEBĖJIMAI",13,GOLD,true)); JSONArray a=state.getJSONArray("abilities"); for(int i=0;i<a.length();i++){ JSONObject x=a.getJSONObject(i); box.addView(itemCard(x.getString("name"),x.getString("description"))); box.addView(space(7)); }
        sv.addView(box); content.addView(sv);
    }

    private void renderInventory() throws Exception {
        ScrollView sv=scroll(); LinearLayout box=column(); box.addView(cardTitle("Inventorius · "+state.getJSONArray("inventory").length()+" objektų")); JSONArray a=state.getJSONArray("inventory");
        for(int i=0;i<a.length();i++){ JSONObject x=a.getJSONObject(i); String sub=x.optString("rarity","unknown")+" · "+x.optString("type","item"); if(!x.optString("equipped_slot","").isEmpty()) sub+=" · EQUIPPED: "+x.getString("equipped_slot"); if(x.optBoolean("resonance_synced",false)) sub+=" · synced"; box.addView(itemCard(x.getString("name"),sub)); box.addView(space(7)); }
        sv.addView(box); content.addView(sv);
    }

    private void renderQuest() throws Exception {
        ScrollView sv=scroll(); LinearLayout box=column(); JSONObject q=state.getJSONObject("quest"); box.addView(cardTitle(q.getString("title"))); box.addView(text(q.getString("stakes"),15,TEXT,false)); box.addView(space(10)); box.addView(text("TIKSLAI",13,GOLD,true)); JSONArray o=q.getJSONArray("objectives");
        for(int i=0;i<o.length();i++){JSONObject x=o.getJSONObject(i); String icon=x.getString("status").equals("active")?"◆":"○"; box.addView(text(icon+" "+x.getString("description"),15,x.getString("status").equals("active")?TEXT:MUTED,false)); box.addView(space(7));}
        box.addView(space(10)); box.addView(text("AKTYVŪS SIUŽETO THREAD'AI",13,GOLD,true)); JSONArray t=state.getJSONArray("threads"); for(int i=0;i<t.length();i++){JSONObject x=t.getJSONObject(i); box.addView(itemCard(x.getString("title"),"priority "+x.getInt("priority")+" · heat "+x.getInt("heat")+" · mystery "+x.getInt("mystery"))); box.addView(space(7));}
        sv.addView(box); content.addView(sv);
    }

    private void renderMap() throws Exception {
        LinearLayout outer=column(); outer.addView(text("ŽEMĖLAPIS · žnybk, kad priartintum",13,GOLD,true)); outer.addView(space(6));
        WorldMapView map=new WorldMapView(this); outer.addView(map,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(430)));
        outer.addView(space(8)); outer.addView(text("◆ Dabartinė vieta: "+state.getJSONObject("world").getString("location"),15,TEXT,true)); outer.addView(text("Žemėlapis yra lokalus — interneto jo peržiūrai nereikia.",12,MUTED,false));
        ScrollView sv=scroll(); sv.addView(outer); content.addView(sv);
    }

    private void renderJournal() throws Exception {
        ScrollView sv=scroll(); LinearLayout box=column(); box.addView(cardTitle("Žurnalas")); JSONArray j=state.optJSONArray("journal"); if(j==null||j.length()==0) box.addView(text("Įrašų dar nėra.",14,MUTED,false)); else for(int i=j.length()-1;i>=0;i--){JSONObject x=j.getJSONObject(i); box.addView(itemCard("T+"+x.optLong("world_minute",0),x.optString("entry",""))); box.addView(space(7));} sv.addView(box); content.addView(sv);
    }

    private void renderSettings(){
        ScrollView sv=scroll(); LinearLayout box=column(); box.addView(cardTitle("Nustatymai")); box.addView(kv("AI modelis","Groq · openai/gpt-oss-120b")); box.addView(kv("API raktas",keys.hasKey()?"Saugomas Android Keystore":"NENUSTATYTAS")); box.addView(kv("Duomenys","SQLite · tik šiame telefone"));
        Button key=actionButton(keys.hasKey()?"Pakeisti Groq API raktą":"Įvesti Groq API raktą"); key.setOnClickListener(v->showApiKeyDialog()); box.addView(key); box.addView(space(7));
        Button undo=actionButton("↶ Atšaukti paskutinį sėkmingą ėjimą"); undo.setOnClickListener(v->{JSONObject u=store.undo(); if(u==null)toast("Nėra ką atšaukti"); else {state=u;tab="Žaidimas";render();toast("Atkurta ankstesnė būsena");}}); box.addView(undo); box.addView(space(7));
        Button ex=actionButton("Eksportuoti save (.json)"); ex.setOnClickListener(v->exportSave()); box.addView(ex); box.addView(space(7));
        Button im=actionButton("Importuoti save (.json)"); im.setOnClickListener(v->importSave()); box.addView(im); box.addView(space(7));
        Button reset=actionButton("Atkurti kanoninį checkpointą"); reset.setOnClickListener(v->confirmReset()); box.addView(reset); box.addView(space(12));
        box.addView(text("API raktas niekada neįrašomas į SQLite save ar eksportuojamą JSON.",12,MUTED,false)); sv.addView(box); content.addView(sv);
    }

    private void runAction(String action){
        if(busy)return; String key=keys.load(); if(key.isEmpty()){showApiKeyDialog();return;} busy=true; render();
        groq.resolve(key,action,state,new GroqClient.Callback(){ public void ok(JSONObject result){ main.post(()->{ try{ store.checkpoint(state); applyTurn(action,result); store.logTurn(action,result,state.getJSONObject("world").getLong("world_minute")); store.save(state); }catch(Exception e){showError("Turn apply: "+e.getMessage());} busy=false; tab="Žaidimas"; render(); }); }
            public void error(String m){ main.post(()->{busy=false;render();showError(m);}); }});
    }

    private void applyTurn(String action,JSONObject res) throws Exception {
        JSONObject r=state.getJSONObject("resources"), d=res.getJSONObject("resource_delta");
        r.put("hp",clamp(r.getInt("hp")+d.getInt("hp"),0,r.getInt("hp_max"))); r.put("mana",clamp(r.getInt("mana")+d.getInt("mana"),0,r.getInt("mana_max"))); r.put("stamina",clamp(r.getInt("stamina")+d.getInt("stamina"),0,r.getInt("stamina_max"))); r.put("aeonic",clamp(r.getInt("aeonic")+d.getInt("aeonic"),0,r.getInt("aeonic_max")));
        state.put("crowns",Math.max(0,state.getLong("crowns")+res.getLong("crowns_delta")));
        JSONObject w=state.getJSONObject("world"); long adv=res.getLong("time_advance_minutes"); long wm=w.getLong("world_minute")+adv; w.put("world_minute",wm); w.put("location",res.getString("location")); w.put("danger",res.getInt("danger")); updateClock(w,adv);
        JSONArray inv=state.getJSONArray("inventory"), rem=res.getJSONArray("inventory_remove"); for(int i=0;i<rem.length();i++) removeInventory(inv,rem.getString(i)); JSONArray add=res.getJSONArray("inventory_add"); for(int i=0;i<add.length();i++) if(!hasItem(inv,add.getString(i))) inv.put(new JSONObject().put("name",add.getString(i)).put("type","discovered_item").put("rarity","unknown").put("resonance_synced",false).put("equipped_slot",""));
        state.put("scene",new JSONObject().put("title",res.getString("scene_title")).put("text",res.getString("scene_text")).put("type",res.getString("scene_type")).put("location",res.getString("location")).put("danger",res.getInt("danger")).put("choices",res.getJSONArray("choices")));
        JSONArray journal=state.optJSONArray("journal"); if(journal==null){journal=new JSONArray();state.put("journal",journal);} journal.put(new JSONObject().put("world_minute",wm).put("entry","Veiksmas: "+action+"\n"+res.getString("scene_text")));
        String q=res.getString("quest_note"); if(!q.trim().isEmpty()) journal.put(new JSONObject().put("world_minute",wm).put("entry","Quest: "+q)); JSONArray ev=res.getJSONArray("event_log"); for(int i=0;i<ev.length();i++) journal.put(new JSONObject().put("world_minute",wm).put("entry","Pasaulis: "+ev.getString(i)));
        while(journal.length()>120) journal.remove(0);
    }

    private void updateClock(JSONObject w,long add) throws Exception { long minute=w.optLong("minute",0)+add; long hour=w.optLong("hour",0)+minute/60; long day=w.optLong("day",0)+hour/24; w.put("minute",minute%60).put("hour",hour%24).put("day",day); }
    private int clamp(int v,int min,int max){return Math.max(min,Math.min(max,v));}
    private boolean hasItem(JSONArray a,String n) throws Exception{for(int i=0;i<a.length();i++)if(a.getJSONObject(i).getString("name").equalsIgnoreCase(n))return true;return false;}
    private void removeInventory(JSONArray a,String n) throws Exception{for(int i=a.length()-1;i>=0;i--)if(a.getJSONObject(i).getString("name").equalsIgnoreCase(n))a.remove(i);}

    private void showApiKeyDialog(){ EditText e=new EditText(this); e.setHint("gsk_..."); e.setSingleLine(true); e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); e.setTextColor(TEXT); e.setHintTextColor(MUTED); e.setText(keys.load());
        new AlertDialog.Builder(this).setTitle("Groq API raktas").setMessage("Įklijuok Free Tier Groq API raktą. Jis bus užšifruotas Android Keystore ir nebus eksportuojamas su save.").setView(e)
                .setPositiveButton("Išsaugoti",(d,w)->{try{String k=e.getText().toString().trim(); if(!k.startsWith("gsk_")){toast("Raktas turėtų prasidėti gsk_");return;} keys.save(k);toast("API raktas išsaugotas");render();}catch(Exception x){showError(x.getMessage());}}).setNegativeButton("Vėliau",null).show(); }

    private void confirmReset(){new AlertDialog.Builder(this).setTitle("Atkurti checkpointą?").setMessage("Dabartinė eiga bus pakeista kanonine Luminara / The Broken Meridian pradine būsena. Prieš resetą rekomenduoju eksportuoti save.").setPositiveButton("Atkurti",(d,w)->{state=store.reset();tab="Žaidimas";render();}).setNegativeButton("Atšaukti",null).show();}
    private void exportSave(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").putExtra(Intent.EXTRA_TITLE,"vaeloria-save.json");startActivityForResult(i,EXPORT_REQ);}
    private void importSave(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,IMPORT_REQ);}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data); if(result!=RESULT_OK||data==null||data.getData()==null)return; Uri u=data.getData(); try{ if(req==EXPORT_REQ){try(OutputStream os=getContentResolver().openOutputStream(u)){if(os==null)throw new IllegalStateException("Nepavyko atidaryti failo");os.write(state.toString(2).getBytes(StandardCharsets.UTF_8));}toast("Save eksportuotas");} else if(req==IMPORT_REQ){StringBuilder b=new StringBuilder();try(BufferedReader r=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(u),StandardCharsets.UTF_8))){String l;while((l=r.readLine())!=null)b.append(l);}JSONObject s=new JSONObject(b.toString()); validateImport(s);store.importState(s);state=s;tab="Žaidimas";render();toast("Save importuotas");}}catch(Exception e){showError("Failo klaida: "+e.getMessage());}}
    private void validateImport(JSONObject s) throws Exception{s.getJSONObject("character");s.getJSONObject("resources");s.getJSONObject("world");s.getJSONObject("scene");s.getJSONArray("inventory");s.getJSONArray("abilities");s.getJSONObject("quest");}

    private TextView cardTitle(String s){TextView t=text(s,20,GOLD,true);t.setPadding(0,dp(4),0,dp(10));return t;}
    private LinearLayout kv(String k,String v){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(12),dp(8),dp(12),dp(8));l.setBackground(cardBg(CARD));l.addView(text(k.toUpperCase(),11,MUTED,true));l.addView(text(v,15,TEXT,false));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);p.setMargins(0,0,0,dp(7));l.setLayoutParams(p);return l;}
    private LinearLayout itemCard(String title,String sub){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(12),dp(10),dp(12),dp(10));l.setBackground(cardBg(CARD));l.addView(text(title,15,TEXT,true));l.addView(text(sub,12,MUTED,false));return l;}
    private LinearLayout resource(String name,int current,int max,int color){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);TextView label=text(name+"  "+current+" / "+max,11,MUTED,true);ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(max);p.setProgress(current);p.setProgressTintList(ColorStateList.valueOf(color));p.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(40,59,67)));l.addView(label);l.addView(p,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(7)));return l;}
    private Button actionButton(String s){Button b=new Button(this);b.setText(s);b.setTextColor(TEXT);b.setTextSize(14);b.setAllCaps(false);b.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);b.setPadding(dp(14),0,dp(14),0);b.setBackground(cardBg(CARD2));b.setMinHeight(dp(52));return b;}
    private Button smallButton(String s){Button b=new Button(this);b.setText(s);b.setTextColor(TEXT);b.setTextSize(12);b.setAllCaps(false);b.setBackgroundColor(Color.TRANSPARENT);b.setPadding(dp(5),0,dp(5),0);return b;}
    private TextView text(String s,int sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);t.setLineSpacing(0,1.12f);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private GradientDrawable cardBg(int c){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(14));g.setStroke(dp(1),Color.rgb(30,57,69));return g;}
    private ScrollView scroll(){ScrollView s=new ScrollView(this);s.setFillViewport(true);s.setClipToPadding(false);s.setPadding(0,0,0,dp(8));return s;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(2),dp(2),dp(2),dp(14));return l;}
    private View space(int h){View v=new View(this);v.setLayoutParams(new LinearLayout.LayoutParams(1,dp(h)));return v;}
    private String clock(JSONObject w){return String.format("%02d:%02d",w.optInt("hour",0),w.optInt("minute",0));}
    private int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private void showError(String s){new AlertDialog.Builder(this).setTitle("Vaeloria").setMessage(s).setPositiveButton("Gerai",null).show();}
}
