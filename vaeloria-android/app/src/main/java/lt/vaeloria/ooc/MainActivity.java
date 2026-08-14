package lt.vaeloria.ooc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(7,15,22);
    private static final int SURFACE = Color.rgb(16,28,37);
    private static final int SURFACE_2 = Color.rgb(23,38,49);
    private static final int TEXT = Color.rgb(238,239,232);
    private static final int MUTED = Color.rgb(166,183,188);
    private static final int GOLD = Color.rgb(214,182,107);
    private static final int TEAL = Color.rgb(82,177,167);
    private static final int GREEN = Color.rgb(94,185,131);

    private VaeloriaDb db;
    private GameState state;
    private FrameLayout content;
    private LinearLayout nav;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private String currentScreen = "game";
    private String feedback = "";
    private boolean busy = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w=getWindow();
        w.setStatusBarColor(BG); w.setNavigationBarColor(BG);
        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        db = new VaeloriaDb(this);
        state = db.loadState();
        buildShell();
        showScreen("game");
    }

    @Override protected void onDestroy(){ super.onDestroy(); executor.shutdownNow(); }

    private void buildShell(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(18),dp(13),dp(18),dp(10));
        TextView title=txt("VAELORIA",20,GOLD,true); top.addView(title,new LinearLayout.LayoutParams(0,dp(44),1));
        TextView ver=chip("LOCAL RPG · v0.2",TEAL);top.addView(ver);
        root.addView(top,new LinearLayout.LayoutParams(-1,dp(66)));
        content=new FrameLayout(this);root.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);nav.setPadding(dp(6),dp(6),dp(6),dp(10));nav.setBackgroundColor(Color.rgb(9,19,27));
        root.addView(nav,new LinearLayout.LayoutParams(-1,dp(72)));
        setContentView(root); rebuildNav();
    }

    private void rebuildNav(){
        nav.removeAllViews();
        addNav("game","✦","ŽAISTI");
        addNav("hero","♜","VEIKĖJAS");
        addNav("map","⌖","ŽEMĖLAPIS");
        addNav("journal","≡","ŽURNALAS");
        addNav("settings","⚙","NUSTAT.");
    }

    private void addNav(String id,String icon,String label){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);box.setPadding(dp(2),dp(4),dp(2),dp(2));
        boolean active=id.equals(currentScreen);box.setBackground(round(active?SURFACE_2:Color.TRANSPARENT,16,active?Color.argb(90,214,182,107):Color.TRANSPARENT));
        TextView i=txt(icon,20,active?GOLD:MUTED,true);i.setGravity(Gravity.CENTER);box.addView(i,new LinearLayout.LayoutParams(-1,dp(28)));
        TextView l=txt(label,9,active?TEXT:MUTED,true);l.setGravity(Gravity.CENTER);box.addView(l,new LinearLayout.LayoutParams(-1,dp(22)));
        box.setOnClickListener(v->showScreen(id));nav.addView(box,new LinearLayout.LayoutParams(0,-1,1));
    }

    private void showScreen(String id){
        currentScreen=id;rebuildNav();content.removeAllViews();
        if("game".equals(id)) content.addView(gameScreen());
        else if("hero".equals(id)) content.addView(heroScreen());
        else if("map".equals(id)) content.addView(mapScreen());
        else if("journal".equals(id)) content.addView(journalScreen());
        else content.addView(settingsScreen());
    }

    private View gameScreen(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        LinearLayout col=column();col.setPadding(dp(14),dp(8),dp(14),dp(24));scroll.addView(col);
        LinearLayout header=card();
        TextView loc=txt("📍 "+state.location+"  ·  METAI "+state.worldYear+"  ·  ⚠ 3",14,TEXT,true);header.addView(loc);
        TextView clock=txt("Meridiano laikas: "+formatWorldTime(state.worldMinute),11,MUTED,false);header.addView(clock);
        col.addView(header,matchWrap(dp(8)));

        LinearLayout hud=new LinearLayout(this);hud.setOrientation(LinearLayout.HORIZONTAL);hud.setPadding(0,dp(4),0,dp(4));
        hud.addView(resource("HP",state.hp,state.hpMax,GREEN),new LinearLayout.LayoutParams(0,dp(58),1));
        hud.addView(resource("MANA",state.mana,state.manaMax,TEAL),new LinearLayout.LayoutParams(0,dp(58),1));
        hud.addView(resource("STAM",state.stamina,state.staminaMax,GOLD),new LinearLayout.LayoutParams(0,dp(58),1));
        hud.addView(resource("AEON",state.aeonic,state.aeonicMax,Color.rgb(154,115,212)),new LinearLayout.LayoutParams(0,dp(58),1));
        col.addView(hud,matchWrap(dp(8)));

        if(!feedback.isEmpty()){
            TextView f=txt(feedback,12,Color.rgb(225,231,219),true);f.setPadding(dp(12),dp(10),dp(12),dp(10));f.setBackground(round(Color.rgb(24,52,50),14,Color.argb(100,82,177,167)));col.addView(f,matchWrap(dp(10)));
        }

        LinearLayout q=card();q.addView(label("AKTYVUS SIUŽETAS"));q.addView(txt(state.questTitle,17,GOLD,true));q.addView(txt(state.objective,12,MUTED,false));col.addView(q,matchWrap(dp(10)));

        LinearLayout scene=cardStrong();
        scene.addView(label("DABARTINĖ SCENA"));scene.addView(txt(state.sceneTitle,22,TEXT,true));
        TextView body=txt(state.scene,15,Color.rgb(225,229,225),false);body.setLineSpacing(0,1.15f);body.setPadding(0,dp(7),0,dp(2));scene.addView(body);
        col.addView(scene,matchWrap(dp(12)));

        TextView choose=txt("KĄ DARAI?",12,MUTED,true);choose.setPadding(dp(4),0,0,dp(6));col.addView(choose);
        for(int i=0;i<Math.min(3,state.choices.size());i++){
            final String action=state.choices.get(i);Button b=choiceButton((i+1)+"  "+action);b.setEnabled(!busy);b.setOnClickListener(v->resolveAction(action));col.addView(b,matchWrap(dp(8)));
        }

        LinearLayout free=card();free.addView(label("LAISVAS VEIKSMAS"));
        EditText input=new EditText(this);input.setHint("Aprašyk bet kokį veiksmą…");input.setHintTextColor(Color.rgb(113,137,145));input.setTextColor(TEXT);input.setTextSize(14);input.setMinLines(2);input.setMaxLines(5);input.setPadding(dp(12),dp(10),dp(12),dp(10));input.setBackground(round(Color.rgb(10,21,29),13,Color.rgb(43,65,76)));free.addView(input,new LinearLayout.LayoutParams(-1,-2));
        Button send=accentButton(busy?"SPRENDŽIAMA…":"ATLIKTI VEIKSMĄ");send.setEnabled(!busy);send.setOnClickListener(v->{String a=input.getText().toString().trim();if(!a.isEmpty())resolveAction(a);});free.addView(send,matchWrap(dp(8)));
        String key=SecureKeyStore.load(this);
        TextView ai=txt(key.isEmpty()?"AI GM: išjungtas · veikia lokalus fallback":"AI GM: Groq · openai/gpt-oss-120b",10,key.isEmpty()?MUTED:TEAL,false);ai.setPadding(dp(4),dp(7),0,0);free.addView(ai);
        col.addView(free,matchWrap(dp(10)));
        return scroll;
    }

    private View heroScreen(){
        ScrollView s=new ScrollView(this);LinearLayout col=column();col.setPadding(dp(14),dp(8),dp(14),dp(26));s.addView(col);
        LinearLayout hero=cardStrong();hero.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView portrait=txt("E",34,BG,true);portrait.setGravity(Gravity.CENTER);portrait.setBackground(round(GOLD,48,GOLD));hero.addView(portrait,new LinearLayout.LayoutParams(dp(86),dp(86)));
        TextView name=txt("EINORAS",25,TEXT,true);name.setGravity(Gravity.CENTER);name.setPadding(0,dp(10),0,0);hero.addView(name);
        TextView title=txt("INDEPENDENT SOVEREIGN-SCALE STRATEGIC ACTOR",10,GOLD,true);title.setGravity(Gravity.CENTER);hero.addView(title);
        hero.addView(txt("201 m. chronologinis · 20 m. biologinis · ageless",12,MUTED,false));
        col.addView(hero,matchWrap(dp(10)));

        LinearLayout stats=card();stats.addView(label("PROGRESIJA"));stats.addView(txt("92 / 92 bazinės statistikos",17,TEXT,true));stats.addView(progressLine("LEGENDARY CAP",100,100,GOLD));stats.addView(txt("Visos bazinės statistikos 100/100. Tolesnė pažanga: kokybinė post-cap mastery.",11,MUTED,false));col.addView(stats,matchWrap(dp(10)));

        LinearLayout gear=card();gear.addView(label("AKTYVUS EQUIPMENT"));
        List<VaeloriaDb.Item> items=db.getItems();
        int resonant=0;for(VaeloriaDb.Item i:items)if(i.equipped&&i.synced)resonant++;
        gear.addView(progressLine("RESONANCE LINK",resonant,Math.max(1,Math.min(7,resonant+2)),TEAL));
        for(VaeloriaDb.Item i:items) if(i.slot!=null) gear.addView(itemRow(i));
        TextView hint=txt("Relic slotai: iki 4 aktyvių. Weapon / armor / utility – po vieną. Gear nekeičia 100/100 bazinių statų; jis keičia situacines galimybes, kurias mato GM.",10,MUTED,false);hint.setPadding(0,dp(8),0,0);gear.addView(hint);col.addView(gear,matchWrap(dp(10)));

        LinearLayout inv=card();inv.addView(label("KITI ARTEFAKTAI"));for(VaeloriaDb.Item i:items)if(i.slot==null)inv.addView(itemInfo(i));col.addView(inv,matchWrap(dp(10)));

        LinearLayout ab=card();ab.addView(label("POST-CAP GEBĖJIMAI"));for(String[] x:db.getAbilities()){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(0,dp(8),0,dp(8));r.addView(txt(x[0],14,TEXT,true));r.addView(txt(x[1].replace('_',' ').toUpperCase(Locale.ROOT),9,GOLD,true));r.addView(txt(x[2],11,MUTED,false));ab.addView(r);}col.addView(ab,matchWrap(dp(8)));
        return s;
    }

    private View itemRow(VaeloriaDb.Item i){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(8),0,dp(8));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.addView(txt(slotName(i.slot)+" · "+i.rarity.toUpperCase(Locale.ROOT),9,rarityColor(i.rarity),true));info.addView(txt(i.name,14,TEXT,true));info.addView(txt(i.description+(i.synced?" · Resonance synced":""),10,MUTED,false));row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
        Button b=smallButton(i.equipped?"NUIMTI":"UŽSIDĖTI",i.equipped?Color.rgb(69,83,90):TEAL);b.setOnClickListener(v->{db.checkpoint("Equipment",state);if(!db.toggleEquip(i.id))Toast.makeText(this,"Slotas neprieinamas arba jau aktyvūs 4 relicai",Toast.LENGTH_SHORT).show();feedback="⚙ Equipment atnaujintas: "+i.name;showScreen("hero");});row.addView(b,new LinearLayout.LayoutParams(dp(96),dp(42)));return row;
    }

    private View itemInfo(VaeloriaDb.Item i){
        LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(0,dp(7),0,dp(7));x.addView(txt(i.name+"  ·  "+i.rarity.toUpperCase(Locale.ROOT),13,rarityColor(i.rarity),true));x.addView(txt(i.description,10,MUTED,false));return x;
    }

    private View mapScreen(){
        LinearLayout root=column();root.setPadding(dp(10),dp(4),dp(10),dp(10));
        LinearLayout info=card();info.addView(label("PASAULIO ŽEMĖLAPIS"));info.addView(txt("Dabartinė vieta: "+state.location+". Spausk lokaciją – žemėlapis neperkelia automatiškai; kelionė tampa tavo veiksmu ir GM ją išsprendžia.",11,MUTED,false));root.addView(info,matchWrap(dp(8)));
        WorldMapView map=new WorldMapView(this);map.setCurrentLocation(state.location);root.addView(map,new LinearLayout.LayoutParams(-1,0,1));
        map.setListener((name,danger)->{
            AlertDialog.Builder b=new AlertDialog.Builder(this);b.setTitle(name+" · Pavojus "+danger+"/10");b.setMessage(locationLore(name)+"\n\nKelionė gali turėti kainą, encounter ar Waygate komplikaciją.");b.setNegativeButton("Uždaryti",null);b.setPositiveButton("KELIAUTI",(d,w)->{showScreen("game");resolveAction("Keliauti iš "+state.location+" į "+name+" saugiausiu pagrįstu maršrutu. Neperšok kelionės pasekmių.");});b.show();
        });
        return root;
    }

    private View journalScreen(){
        ScrollView s=new ScrollView(this);LinearLayout col=column();col.setPadding(dp(14),dp(8),dp(14),dp(24));s.addView(col);
        LinearLayout q=cardStrong();q.addView(label("MAIN ARC · ACT I"));q.addView(txt("THE BROKEN MERIDIAN",21,GOLD,true));q.addView(txt("Build a route network that is physically stable, politically legitimate, and usable by ordinary people without turning Concordance stewardship into a monopoly.",12,MUTED,false));q.addView(objective("◆",state.objective,true));q.addView(objective("○","Secure three independently maintained route anchors",false));q.addView(objective("○","Cross the Meridian and return with verifiable Orison contact data",false));q.addView(objective("○","Negotiate or deliberately reject the First Meridian Charter",false));col.addView(q,matchWrap(dp(10)));

        LinearLayout threads=card();threads.addView(label("AKTYVŪS THREADS"));
        threads.addView(thread("Open Horizons: The Broken Meridian",96,58,82));threads.addView(thread("Orison Contact Protocol",90,55,85));threads.addView(thread("Waygate Drift",82,50,78));threads.addView(thread("Concordance Stewardship Governance",76,62,38));threads.addView(thread("Draconic Succession",74,46,52));col.addView(threads,matchWrap(dp(10)));

        LinearLayout log=card();log.addView(label("PASKUTINIAI ĖJIMAI"));if(state.recentTurns.isEmpty())log.addView(txt("Dar nėra naujų lokalių ėjimų. Pradžia išsaugota iš autoritetingo Neon checkpointo.",11,MUTED,false));else for(int i=state.recentTurns.size()-1;i>=0;i--){log.addView(txt("• "+state.recentTurns.get(i),11,TEXT,false));}col.addView(log,matchWrap(dp(8)));
        return s;
    }

    private View settingsScreen(){
        ScrollView s=new ScrollView(this);LinearLayout col=column();col.setPadding(dp(14),dp(8),dp(14),dp(24));s.addView(col);
        LinearLayout ai=cardStrong();ai.addView(label("AI GAME MASTER"));String key=SecureKeyStore.load(this);ai.addView(txt(key.isEmpty()?"Groq raktas nenustatytas":"Groq raktas saugomas Android Keystore",15,key.isEmpty()?MUTED:TEAL,true));ai.addView(txt("Modelis: openai/gpt-oss-120b · strict JSON schema. Raktas nėra įrašytas nei į APK, nei į GitHub, nei į save failą.",11,MUTED,false));Button set=accentButton(key.isEmpty()?"ĮVESTI GROQ API RAKTĄ":"PAKEISTI GROQ API RAKTĄ");set.setOnClickListener(v->apiKeyDialog());ai.addView(set,matchWrap(dp(8)));if(!key.isEmpty()){Button rm=outlineButton("PAŠALINTI RAKTĄ");rm.setOnClickListener(v->{SecureKeyStore.clear(this);Toast.makeText(this,"API raktas pašalintas",Toast.LENGTH_SHORT).show();showScreen("settings");});ai.addView(rm,matchWrap(dp(6)));}col.addView(ai,matchWrap(dp(10)));

        LinearLayout save=card();save.addView(label("SAVE / RECOVERY"));Button undo=outlineButton("↶ ATŠAUKTI PASKUTINĮ ŽINGSNĮ");undo.setOnClickListener(v->{if(db.undo()){state=db.loadState();feedback="↶ Atkurtas ankstesnis checkpointas";Toast.makeText(this,"Atkurta",Toast.LENGTH_SHORT).show();}else Toast.makeText(this,"Nėra checkpointo",Toast.LENGTH_SHORT).show();});save.addView(undo,matchWrap(dp(6)));
        Button exp=outlineButton("KOPIJUOTI SAVE Į IŠKARPINĘ");exp.setOnClickListener(v->exportSave());save.addView(exp,matchWrap(dp(6)));
        Button imp=outlineButton("IMPORTUOTI SAVE IŠ IŠKARPINĖS");imp.setOnClickListener(v->importSave());save.addView(imp,matchWrap(dp(6)));
        Button reset=outlineButton("ATKURTI PRADINĮ NEON CHECKPOINTĄ");reset.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Atkurti checkpointą?").setMessage("Bus ištrinti lokalūs ėjimai ir equipment pakeitimai. Groq raktas liks telefone.").setNegativeButton("Ne",null).setPositiveButton("Atkurti",(d,w)->{db.reset();state=db.loadState();feedback="Atkurta autoritetinga Luminara pradžia";showScreen("game");}).show());save.addView(reset,matchWrap(dp(6)));col.addView(save,matchWrap(dp(10)));

        LinearLayout tech=card();tech.addView(label("ARCHITEKTŪRA"));tech.addView(txt("Telefonas → lokalus SQLite save → Groq tik scenos sprendimui",13,TEXT,true));tech.addView(txt("Nėra Vercel, nėra serverio, nėra mokėjimo kortelės reikalaujančio hostingo. Žaidimo būsena lieka telefone; AI gauna tik kompaktišką aktualią būseną ir kelis paskutinius ėjimus.",11,MUTED,false));col.addView(tech,matchWrap(dp(8)));
        return s;
    }

    private void resolveAction(String action){
        if(busy)return;
        db.checkpoint("Prieš veiksmą",state);busy=true;feedback="⟳ Sprendžiamas veiksmas…";showScreen("game");
        final String key=SecureKeyStore.load(this);
        if(key.isEmpty()){
            executor.execute(()->{try{Thread.sleep(180);}catch(Exception ignored){}JSONObject r=localResolve(action);runOnUiThread(()->finishTurn(action,r,null));});
            return;
        }
        executor.execute(()->{
            try{JSONObject r=GroqClient.resolveTurn(key,state,action,db.equippedSummary(),db.getAbilities());runOnUiThread(()->finishTurn(action,r,null));}
            catch(Exception e){JSONObject fallback=localResolve(action);runOnUiThread(()->finishTurn(action,fallback,"Groq nepasiekiamas: "+compactError(e.getMessage())+" · pritaikytas lokalus fallback"));}
        });
    }

    private void finishTurn(String action,JSONObject r,String warning){
        try{
            int oldHp=state.hp,oldMana=state.mana,oldSt=state.stamina,oldAe=state.aeonic;long oldC=state.crowns;String oldLoc=state.location;
            state.applyTurn(r);state.recentTurns.add(action+" → "+state.sceneTitle);while(state.recentTurns.size()>12)state.recentTurns.remove(0);db.saveState(state);
            StringBuilder f=new StringBuilder();String event=r.optString("event_tag","none");if(!"none".equals(event))f.append("◆ ").append(event.toUpperCase(Locale.ROOT)).append("  ");
            delta(f,"HP",state.hp-oldHp);delta(f,"Mana",state.mana-oldMana);delta(f,"Stamina",state.stamina-oldSt);delta(f,"Aeonic",state.aeonic-oldAe);if(state.crowns!=oldC)f.append(state.crowns-oldC>0?" +":" ").append(state.crowns-oldC).append(" crowns");if(!oldLoc.equals(state.location))f.append("  → ").append(state.location);if(f.length()==0)f.append("Veiksmas išspręstas · būsena išsaugota");if(warning!=null)f.append("\n⚠ ").append(warning);feedback=f.toString();
        }catch(Exception e){feedback="⚠ Nepavyko pritaikyti rezultato";}
        busy=false;showScreen("game");
    }

    private JSONObject localResolve(String action){
        try{
            String a=action.toLowerCase(Locale.ROOT);JSONObject r=new JSONObject();r.put("location",state.location);r.put("time_minutes",15);r.put("hp_delta",0);r.put("mana_delta",0);r.put("stamina_delta",0);r.put("aeonic_delta",0);r.put("crowns_delta",0);r.put("quest_note",state.objective);r.put("event_tag","discovery");JSONArray c=new JSONArray();
            if(a.contains("manifest")||a.contains("ištir")||a.contains("tirti")){
                r.put("scene_title","Manifesto neatitikimas");r.put("scene","Po ranka esančios datos ir antspaudai nesutampa ne dėl raštininko klaidos: krovinio registracijos seka turi dvi skirtingas, abi viduje nuoseklias laiko versijas. Tai dar neatsako, kas sukėlė driftą, bet duoda pirmą patikrinamą pėdsaką – vakarinio waygate tranzito žymą.");r.put("quest_note","Patikrinti vakarinio Luminara waygate tranzito žymą ir palyginti ją su fizine karavano atvykimo būsena.");c.put("Vykti prie vakarinio waygate ir paimti lauko matavimus");c.put("Surasti manifestą išdavusį raštininką ir patikrinti jo prisiminimų seką");c.put("Palyginti krovinio plombas su Meridian Key rezonansu");
            } else if(a.contains("mira")||a.contains("kaelis")){
                r.put("scene_title","Trijų meistrų palyginimas");r.put("scene","Mira ir Kaelis nepritaria vienai patogiai teorijai: jų stebėjimuose driftas atrodo ne kaip vienas laiko poslinkis, o kaip maršruto pasirinkimo neatitikimas. Jie siūlo prieš darant išvadą paimti du nepriklausomus matavimus prie waygate.");c.put("Kartu vykti prie waygate ir atlikti dvigubą matavimą");c.put("Paprašyti jų atskirai suformuluoti hipotezes, kad nesusiderintų iš anksto");c.put("Iš archyvo paimti seną Meridian maršruto žemėlapį palyginimui");
            } else if(a.contains("keliauti")||a.contains("vykti")||a.contains("waygate")){
                r.put("scene_title","Kelias į incidento vietą");r.put("scene","Kelionė iš Luminara prasideda be teleportavimo: pirmasis maršruto segmentas stabilus, tačiau kuo arčiau waygate, tuo daugiau ženklų rodo nevienodą tranzito ritmą. Sargyba jau stabdo civilius, todėl prieiga į incidento zoną tampa ir praktiniu, ir politiniu klausimu.");r.put("event_tag","travel");r.put("time_minutes",35);c.put("Prisistatyti kaip Concordance steward ir prašyti ribotos prieigos");c.put("Stebėti zoną iš išorės ir surinkti nepriklausomus telemetrijos požymius");c.put("Apeiti oficialų perimetrą tik teisėtu alternatyviu maršrutu");
            } else {
                r.put("scene_title","Veiksmo pasekmė");r.put("scene","Veiksmas pakeičia situaciją, bet neapeina nežinomų Meridian taisyklių. Artimiausias patikimas žingsnis lieka rinkti lauko įrodymus ir tikrinti, ar nauja informacija dera su manifestų neatitikimu.");c.put("Rinkti fizinius įrodymus prie waygate");c.put("Kalbėtis su liudininkais atskirai");c.put("Patikrinti Meridian Key reakciją saugiu atstumu");
            }
            r.put("choices",c);return r;
        }catch(Exception e){return new JSONObject();}
    }

    private void apiKeyDialog(){
        EditText input=new EditText(this);input.setHint("gsk_…");input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);input.setSingleLine(true);input.setPadding(dp(16),dp(12),dp(16),dp(12));
        new AlertDialog.Builder(this).setTitle("Groq API raktas").setMessage("Raktas bus užšifruotas Android Keystore ir saugomas tik šiame telefone.").setView(input).setNegativeButton("Atšaukti",null).setPositiveButton("Išsaugoti",(d,w)->{String k=input.getText().toString().trim();if(k.length()<10){Toast.makeText(this,"Raktas per trumpas",Toast.LENGTH_SHORT).show();return;}try{SecureKeyStore.save(this,k);Toast.makeText(this,"Raktas išsaugotas",Toast.LENGTH_SHORT).show();showScreen("settings");}catch(Exception e){Toast.makeText(this,"Nepavyko saugiai išsaugoti",Toast.LENGTH_LONG).show();}}).show();
    }

    private void exportSave(){ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);cm.setPrimaryClip(ClipData.newPlainText("Vaeloria save",db.exportSave()));Toast.makeText(this,"Save nukopijuotas",Toast.LENGTH_SHORT).show();}
    private void importSave(){ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);if(!cm.hasPrimaryClip()){Toast.makeText(this,"Iškarpinė tuščia",Toast.LENGTH_SHORT).show();return;}CharSequence x=cm.getPrimaryClip().getItemAt(0).coerceToText(this);if(db.importSave(x.toString())){state=db.loadState();feedback="Importuotas save";showScreen("game");}else Toast.makeText(this,"Tai nėra tinkamas Vaeloria save",Toast.LENGTH_LONG).show();}

    private LinearLayout resource(String name,int val,int max,int color){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(3),0,dp(3),0);TextView n=txt(name,9,MUTED,true);n.setGravity(Gravity.CENTER);x.addView(n);TextView v=txt(val+"/"+max,13,TEXT,true);v.setGravity(Gravity.CENTER);x.addView(v);ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(max);bar.setProgress(val);bar.setProgressTintList(android.content.res.ColorStateList.valueOf(color));bar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(34,49,57)));x.addView(bar,new LinearLayout.LayoutParams(-1,dp(6)));return x;}
    private View progressLine(String title,int val,int max,int color){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(0,dp(7),0,dp(7));TextView t=txt(title+"  "+val+"/"+max,10,MUTED,true);x.addView(t);ProgressBar b=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);b.setMax(max);b.setProgress(val);b.setProgressTintList(android.content.res.ColorStateList.valueOf(color));b.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(42,54,60)));x.addView(b,new LinearLayout.LayoutParams(-1,dp(8)));return x;}
    private View objective(String icon,String text,boolean active){LinearLayout r=new LinearLayout(this);r.setPadding(0,dp(8),0,dp(8));r.setGravity(Gravity.TOP);TextView i=txt(icon,15,active?GOLD:MUTED,true);r.addView(i,new LinearLayout.LayoutParams(dp(28),-2));r.addView(txt(text,12,active?TEXT:MUTED,active),new LinearLayout.LayoutParams(0,-2,1));return r;}
    private View thread(String name,int priority,int heat,int mystery){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(0,dp(7),0,dp(7));x.addView(txt(name,13,TEXT,true));x.addView(txt("Priority "+priority+"  ·  Heat "+heat+"  ·  Mystery "+mystery,10,MUTED,false));return x;}

    private LinearLayout card(){LinearLayout x=column();x.setPadding(dp(14),dp(13),dp(14),dp(13));x.setBackground(round(SURFACE,18,Color.rgb(36,57,68)));return x;}
    private LinearLayout cardStrong(){LinearLayout x=column();x.setPadding(dp(16),dp(15),dp(16),dp(15));x.setBackground(round(SURFACE_2,20,Color.argb(140,214,182,107)));return x;}
    private LinearLayout column(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);return x;}
    private TextView label(String s){TextView t=txt(s,10,GOLD,true);t.setPadding(0,0,0,dp(7));return t;}
    private TextView txt(String s,int sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);t.setTypeface(Typeface.create(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL));t.setLineSpacing(0,1.08f);return t;}
    private TextView chip(String s,int color){TextView t=txt(s,9,color,true);t.setGravity(Gravity.CENTER);t.setPadding(dp(10),0,dp(10),0);t.setBackground(round(Color.argb(55,82,177,167),20,Color.argb(95,82,177,167)));return t;}
    private Button choiceButton(String s){Button b=new Button(this);b.setText(s);b.setTextSize(12);b.setTextColor(TEXT);b.setAllCaps(false);b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);b.setPadding(dp(14),0,dp(14),0);b.setBackground(round(SURFACE_2,16,Color.rgb(47,70,81)));return b;}
    private Button accentButton(String s){Button b=new Button(this);b.setText(s);b.setTextSize(12);b.setTextColor(BG);b.setTypeface(Typeface.DEFAULT_BOLD);b.setBackground(round(GOLD,14,GOLD));return b;}
    private Button outlineButton(String s){Button b=new Button(this);b.setText(s);b.setTextSize(11);b.setTextColor(TEXT);b.setBackground(round(Color.TRANSPARENT,14,Color.rgb(55,80,91)));return b;}
    private Button smallButton(String s,int color){Button b=new Button(this);b.setText(s);b.setTextSize(9);b.setTextColor(TEXT);b.setPadding(dp(5),0,dp(5),0);b.setBackground(round(Color.argb(65,255,255,255),12,color));return b;}
    private GradientDrawable round(int fill,int radius,int stroke){GradientDrawable g=new GradientDrawable();g.setColor(fill);g.setCornerRadius(dp(radius));if(Color.alpha(stroke)>0)g.setStroke(dp(1),stroke);return g;}
    private LinearLayout.LayoutParams matchWrap(int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,bottom);return p;}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private int rarityColor(String r){if("unique".equals(r))return Color.rgb(229,118,210);if("legendary".equals(r))return GOLD;if("ancient".equals(r))return Color.rgb(188,115,224);if("rare".equals(r))return Color.rgb(88,157,225);return TEXT;}
    private String slotName(String s){if("primary_weapon".equals(s))return "WEAPON";if("armor_system".equals(s))return "ARMOR";if("utility".equals(s))return "UTILITY";if("relic".equals(s))return "RELIC";return s==null?"ITEM":s.toUpperCase(Locale.ROOT);}
    private String formatWorldTime(long min){long day=min/1440;long m=min%1440;return "Diena "+day+" · "+String.format(Locale.ROOT,"%02d:%02d",m/60,m%60);}
    private String locationLore(String n){if(n.equals("Luminara"))return "Radiant high-magic metropolis around seven floating arcane rings.";if(n.equals("Kharad Vorn"))return "Forge-city and deepworks, tied to null-resistant craft and institutional power.";if(n.equals("Dragonwake Peaks"))return "Draconic mountain territory with succession pressure and apex threats.";if(n.equals("The Hollow Spire"))return "High-risk anomaly zone where spatial and null ecologies overlap.";if(n.equals("The Starfall Vault"))return "Ancient astral vault governed by protocol, constructs and dangerous rules.";return "Known Vaeloria location linked to the living travel network.";}
    private void delta(StringBuilder b,String name,long d){if(d!=0){if(b.length()>0)b.append("  ");b.append(d>0?"+":"").append(d).append(" ").append(name);}}
    private String compactError(String x){if(x==null)return "klaida";return x.length()>100?x.substring(0,100)+"…":x;}
}
