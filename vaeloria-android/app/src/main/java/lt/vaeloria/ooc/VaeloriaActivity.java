package lt.vaeloria.ooc;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;
import java.util.concurrent.*;

public class VaeloriaActivity extends Activity {
    static final int BG=Color.rgb(7,15,22),SUR=Color.rgb(16,28,37),SUR2=Color.rgb(23,38,49),TEXT=Color.rgb(238,239,232),MUT=Color.rgb(166,183,188),GOLD=Color.rgb(214,182,107),TEAL=Color.rgb(82,177,167),GREEN=Color.rgb(94,185,131),RED=Color.rgb(213,91,82);
    VaeloriaDb db; GameState state; FrameLayout content; LinearLayout nav,root; String screen="game",feedback=""; boolean busy=false; final ExecutorService pool=Executors.newSingleThreadExecutor();

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        db=new VaeloriaDb(this);state=db.loadState();shell();show("game");
    }
    @Override public void onDestroy(){pool.shutdownNow();if(db!=null)db.close();super.onDestroy();}

    void shell(){
        root=col();root.setBackgroundColor(BG);
        root.setOnApplyWindowInsetsListener((v,insets)->{
            int top,bottom;
            if(Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars());top=bars.top;bottom=bars.bottom;}
            else{top=insets.getSystemWindowInsetTop();bottom=insets.getSystemWindowInsetBottom();}
            v.setPadding(0,top,0,bottom);return insets;
        });
        LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(18),dp(4),dp(14),dp(4));
        top.addView(t("VAELORIA",22,GOLD,true),new LinearLayout.LayoutParams(0,dp(52),1));top.addView(chip("VIETINIS RPG · "+BuildConfig.VERSION_NAME));
        root.addView(top,new LinearLayout.LayoutParams(-1,dp(60)));
        content=new FrameLayout(this);root.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        nav=row();nav.setPadding(dp(5),dp(4),dp(5),dp(5));nav.setBackgroundColor(Color.rgb(9,19,27));root.addView(nav,new LinearLayout.LayoutParams(-1,dp(68)));
        setContentView(root);root.requestApplyInsets();
    }

    void nav(){nav.removeAllViews();tab("game","✦","ŽAISTI");tab("hero","♜","VEIKĖJAS");tab("map","⌖","ŽEMĖLAPIS");tab("journal","≡","ŽURNALAS");tab("settings","⚙","NUSTAT.");}
    void tab(String id,String icon,String label){LinearLayout x=col();x.setGravity(Gravity.CENTER);boolean a=id.equals(screen);x.setBackground(round(a?SUR2:Color.TRANSPARENT,15,a?Color.argb(90,214,182,107):Color.TRANSPARENT));TextView i=t(icon,19,a?GOLD:MUT,true);i.setGravity(Gravity.CENTER);x.addView(i);TextView l=t(label,9,a?TEXT:MUT,true);l.setGravity(Gravity.CENTER);x.addView(l);x.setOnClickListener(v->show(id));nav.addView(x,new LinearLayout.LayoutParams(0,-1,1));}
    void show(String id){screen=id;nav();content.removeAllViews();try{content.addView("game".equals(id)?game():"hero".equals(id)?hero():"map".equals(id)?map():"journal".equals(id)?journal():settings());}catch(Throwable e){content.addView(errorView(e));}}

    View game(){
        ScrollView sv=new ScrollView(this);LinearLayout c=col();c.setPadding(dp(14),dp(6),dp(14),dp(22));sv.addView(c);
        LinearLayout h=card();h.addView(t("📍 "+state.location+"  ·  METAI "+state.worldYear,15,TEXT,true));h.addView(t(clock(),11,MUT,false));c.addView(h,m(dp(9)));
        LinearLayout bars=row();bars.addView(res("GYVYBĖ",state.hp,state.hpMax,GREEN),w1());bars.addView(res("MANA",state.mana,state.manaMax,TEAL),w1());bars.addView(res("IŠTVERMĖ",state.stamina,state.staminaMax,GOLD),w1());bars.addView(res("EONAS",state.aeonic,state.aeonicMax,Color.rgb(154,115,212)),w1());c.addView(bars,m(dp(10)));
        if(!feedback.isEmpty()){TextView f=t(feedback,11,TEXT,true);f.setPadding(dp(10),dp(8),dp(10),dp(8));f.setBackground(round(Color.rgb(24,52,50),13,Color.argb(100,82,177,167)));c.addView(f,m(dp(10)));}
        if(state.combatActive){
            LinearLayout x=strong();x.addView(label("⚔ KOVA · "+safe(state.enemyName,"PRIEŠAS").toUpperCase(Locale.ROOT)));CombatBoardView b=new CombatBoardView(this);b.setState(state);x.addView(b,new LinearLayout.LayoutParams(-1,dp(190)));x.addView(t("PRIEŠO KETINIMAS · "+safe(state.enemyTelegraph,"Ketinimas dar neaiškus"),12,Color.rgb(236,157,111),true));if(!state.enemyStatus.isEmpty())x.addView(t("Būsena: "+state.enemyStatus,11,MUT,false));c.addView(x,m(dp(10)));
        }else{
            SceneBannerView art=new SceneBannerView(this);art.setLocation(state.location);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(165));p.setMargins(0,0,0,dp(10));c.addView(art,p);
        }
        LinearLayout q=card();q.addView(label("AKTYVUS SIUŽETAS"));q.addView(t(state.questTitle,18,GOLD,true));q.addView(t(state.objective,12,MUT,false));c.addView(q,m(dp(10)));
        LinearLayout sc=strong();sc.addView(label("DABARTINĖ SCENA"));sc.addView(t(state.sceneTitle,22,TEXT,true));TextView story=t(state.scene,15,Color.rgb(225,229,225),false);story.setLineSpacing(0,1.15f);story.setPadding(0,dp(6),0,0);sc.addView(story);c.addView(sc,m(dp(12)));
        c.addView(label("KĄ DARAI?"));for(int i=0;i<Math.min(3,state.choices.size());i++){String a=state.choices.get(i);Button b=choice((i+1)+"  "+a);b.setEnabled(!busy);b.setOnClickListener(v->act(a));c.addView(b,m(dp(7)));}
        LinearLayout free=card();free.addView(label("LAISVAS VEIKSMAS"));EditText in=new EditText(this);in.setHint("Aprašyk bet kokį veiksmą…");in.setHintTextColor(Color.rgb(113,137,145));in.setTextColor(TEXT);in.setMinLines(2);in.setMaxLines(5);in.setBackground(round(Color.rgb(10,21,29),12,Color.rgb(43,65,76)));in.setPadding(dp(11),dp(8),dp(11),dp(8));free.addView(in);Button send=accent(busy?"SPRENDŽIAMA…":"ATLIKTI VEIKSMĄ");send.setEnabled(!busy);send.setOnClickListener(v->{String a=in.getText().toString().trim();if(!a.isEmpty())act(a);});free.addView(send,m(dp(6)));boolean hasKey=!SecureKeyStore.load(this).isEmpty();free.addView(t(hasKey?"DI žaidimo meistras · Groq · gpt-oss-120b":"DI žaidimo meistras išjungtas · veikia vietinis režimas",10,hasKey?TEAL:MUT,false));c.addView(free,m(dp(8)));return sv;
    }

    View hero(){
        ScrollView sv=new ScrollView(this);LinearLayout c=col();c.setPadding(dp(14),dp(6),dp(14),dp(22));sv.addView(c);
        LinearLayout top=strong();top.setGravity(Gravity.CENTER_HORIZONTAL);TextView e=t("E",34,BG,true);e.setGravity(Gravity.CENTER);e.setBackground(round(GOLD,45,GOLD));top.addView(e,new LinearLayout.LayoutParams(dp(76),dp(76)));TextView n=t("EINORAS",24,TEXT,true);n.setGravity(Gravity.CENTER);n.setPadding(0,dp(7),0,0);top.addView(n);top.addView(center("201 m. chronologinis · 20 m. biologinis · biologinis amžius nekinta",11,MUT));c.addView(top,m(dp(10)));
        LinearLayout stats=card();stats.addView(label("PAŽANGA"));stats.addView(t("92 / 92 bazinės savybės · 100/100",16,TEXT,true));stats.addView(bar("BAZINĖ RIBA",100,100,GOLD));stats.addView(t("Toliau augama per meistriškumą, principus, technikas ir pasaulio pažinimą, o ne papildomus bazinių savybių taškus.",11,MUT,false));c.addView(stats,m(dp(9)));
        c.addView(equipmentPanel(),m(dp(9)));

        LinearLayout loose=card();loose.addView(label("NEUŽDĖTA ĮRANGA"));int looseCount=0;for(VaeloriaDb.Item i:db.getItems())if(i.slot!=null&&!i.equipped){loose.addView(inventoryRow(i));looseCount++;}if(looseCount==0)loose.addView(t("Visi šiuo metu turimi dėvimi daiktai yra užsidėti. Naują įrangą gali rasti, gauti kaip atlygį arba atimti iš priešininko, jei tai pagrįsta siužetu.",11,MUT,false));c.addView(loose,m(dp(9)));

        LinearLayout inv=card();inv.addView(label("ARTEFAKTAI IR ĮGALIOJIMAI"));for(VaeloriaDb.Item i:db.getItems())if(i.slot==null){inv.addView(t(i.name+" · "+rarityLabel(i.rarity).toUpperCase(Locale.ROOT),13,rarity(i.rarity),true));inv.addView(t(i.description,10,MUT,false));}c.addView(inv,m(dp(9)));

        LinearLayout ab=card();ab.addView(label("GEBĖJIMAI VIRŠ BAZINĖS RIBOS"));for(String[] a:db.getAbilities()){ab.addView(t("• "+a[0],12,TEXT,true));ab.addView(t(abilityType(a[1])+" · "+a[2],10,MUT,false));}c.addView(ab,m(dp(8)));return sv;
    }

    View equipmentPanel(){
        LinearLayout gear=strong();gear.addView(label("DĖVIMA ĮRANGA"));gear.addView(t("Paliesk vietą, kad pakeistum arba nuimtum daiktą. Tuščios vietos yra tikros įrangos kategorijos ir gali būti užpildytos žaidimo metu.",11,MUT,false));
        gear.addView(slotRow("head","neck"));gear.addView(slotRow("weapon","offhand"));gear.addView(slotRow("chest","utility"));gear.addView(slotRow("hands","belt"));gear.addView(slotRow("legs","feet"));gear.addView(slotRow("ring_left","ring_right"));
        TextView r=label("RELIKVIJŲ VIETOS");r.setPadding(0,dp(10),0,dp(6));gear.addView(r);gear.addView(slotRow("relic_1","relic_2"));gear.addView(slotRow("relic_3","relic_4"));return gear;
    }

    View slotRow(String a,String b){LinearLayout r=row();LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(0,dp(5),dp(4),0);r.addView(slotCard(a),p);LinearLayout.LayoutParams p2=new LinearLayout.LayoutParams(0,-2,1);p2.setMargins(dp(4),dp(5),0,0);r.addView(slotCard(b),p2);return r;}
    View slotCard(String target){VaeloriaDb.Item i=db.getEquippedAt(target);LinearLayout x=col();x.setMinimumHeight(dp(78));x.setPadding(dp(10),dp(9),dp(10),dp(9));x.setBackground(round(Color.rgb(13,27,36),13,i==null?Color.rgb(43,61,70):rarity(i.rarity)));x.addView(t(VaeloriaDb.slotLabel(target).toUpperCase(Locale.ROOT),9,i==null?MUT:GOLD,true));x.addView(t(i==null?"Tuščia":i.name,12,i==null?Color.rgb(116,137,145):TEXT,true));if(i!=null)x.addView(t(rarityLabel(i.rarity)+(i.synced?" · susieta":""),9,rarity(i.rarity),false));else x.addView(t("Laukiama tinkamo daikto",9,MUT,false));x.setOnClickListener(v->openSlot(target));return x;}

    void openSlot(String target){
        VaeloriaDb.Item current=db.getEquippedAt(target);List<VaeloriaDb.Item> candidates=db.getItemsForTarget(target);ArrayList<VaeloriaDb.Item> usable=new ArrayList<>();for(VaeloriaDb.Item i:candidates)if(!i.equipped||target.equals(i.equippedSlot))usable.add(i);
        if(usable.isEmpty()){
            new AlertDialog.Builder(this).setTitle(VaeloriaDb.slotLabel(target)).setMessage("Šiai vietai tinkamos įrangos dar neturi. Tinkamas daiktas gali atsirasti kaip pagrįstas grobis, atlygis, pirkinys ar radinys.").setPositiveButton("Gerai",null).show();return;
        }
        String[] names=new String[usable.size()];for(int i=0;i<usable.size();i++){VaeloriaDb.Item it=usable.get(i);names[i]=it.name+" · "+rarityLabel(it.rarity)+(target.equals(it.equippedSlot)?" · UŽDĖTA":"");}
        AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle(VaeloriaDb.slotLabel(target)).setItems(names,(d,which)->{VaeloriaDb.Item it=usable.get(which);db.checkpoint("prieš įrangos pakeitimą",state);db.equipToSlot(it.id,target);feedback="⚙ Užsidėta: "+it.name;show("hero");});
        if(current!=null)b.setNegativeButton("NUIMTI",(d,w)->{db.checkpoint("prieš įrangos pakeitimą",state);db.unequipSlot(target);feedback="⚙ Nuimta: "+current.name;show("hero");});b.setNeutralButton("Uždaryti",null).show();
    }

    View inventoryRow(VaeloriaDb.Item i){LinearLayout r=row();r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(0,dp(6),0,dp(6));LinearLayout x=col();x.addView(t(categoryLabel(i.slot)+" · "+rarityLabel(i.rarity).toUpperCase(Locale.ROOT),9,rarity(i.rarity),true));x.addView(t(i.name,13,TEXT,true));x.addView(t(i.description,10,MUT,false));r.addView(x,new LinearLayout.LayoutParams(0,-2,1));return r;}

    View map(){
        ScrollView sv=new ScrollView(this);LinearLayout root=col();root.setPadding(dp(12),dp(5),dp(12),dp(20));sv.addView(root);
        LinearLayout h=card();h.addView(label("PASAULIO ŽEMĖLAPIS"));h.addView(t("◆ Dabartinė vieta: "+state.location,13,TEXT,true));h.addView(t("Priartink dviem pirštais, tempk žemėlapį ir paliesk vietą. Kelionė įvyksta tik tada, kai žaidimo meistras išsprendžia jos laiką, kainą ir galimas pasekmes.",11,MUT,false));root.addView(h,m(dp(9)));
        WorldMapView map=new WorldMapView(this);map.setCurrentLocation(state.location);map.setListener((name,danger)->locationDialog(name,danger));LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,dp(355));root.addView(map,mp);
        LinearLayout legend=card();legend.addView(label("GRĖSMĖS LYGIS"));legend.addView(t("● 1–3  žemas     ● 4–5  vidutinis     ● 6–7  aukštas     ● 8–10  labai aukštas",10,MUT,false));legend.addView(t("Žemėlapis rodo žinomą Vaeloria kelių tinklą. Neatrastos vietos ir nauji ryšiai gali atsirasti žaidžiant.",11,MUT,false));root.addView(legend,m(dp(8)));return sv;
    }

    void locationDialog(String name,int danger){new AlertDialog.Builder(this).setTitle(name).setMessage("Grėsmė: "+danger+"/10\n\n"+lore(name)+"\n\nKelionė gali pareikalauti laiko, išteklių, susidūrimo ar kelionės vartų sprendimo.").setNegativeButton("Uždaryti",null).setPositiveButton("KELIAUTI",(d,w)->{show("game");act("Keliauti iš "+state.location+" į "+name+" saugiausiu pagrįstu maršrutu, įvertinant kelionės laiką ir realias pasekmes.");}).show();}

    View journal(){
        ScrollView sv=new ScrollView(this);LinearLayout c=col();c.setPadding(dp(14),dp(6),dp(14),dp(22));sv.addView(c);
        LinearLayout q=strong();q.addView(label("PAGRINDINĖ SIUŽETO LINIJA · I DALIS"));q.addView(t("LŪŽĘS MERIDIANAS",20,GOLD,true));q.addView(obj("◆",state.objective,true));q.addView(obj("○","Užsitikrinti tris nepriklausomai prižiūrimus kelio atramos taškus",false));q.addView(obj("○","Pereiti Meridianą ir grįžti su patikrinamais Orisono kontakto duomenimis",false));q.addView(obj("○","Derėtis dėl Pirmosios Meridiano chartijos arba ją atmesti",false));c.addView(q,m(dp(9)));
        LinearLayout th=card();th.addView(label("AKTYVIOS SIUŽETO GIJOS"));for(String s:new String[]{"Atviri horizontai: Lūžęs Meridianas","Orisono ryšio protokolas","Kelionės vartų poslinkis","Santarvės priežiūros valdymas","Drakoniškoji įpėdinystė"})th.addView(t("• "+s,12,TEXT,false));c.addView(th,m(dp(9)));
        LinearLayout log=card();log.addView(label("PASKUTINIAI ĖJIMAI"));if(state.recentTurns.isEmpty())log.addView(t("Naujų vietinių ėjimų dar nėra.",11,MUT,false));else for(int i=state.recentTurns.size()-1;i>=0;i--)log.addView(t("• "+state.recentTurns.get(i),11,TEXT,false));c.addView(log,m(dp(8)));return sv;
    }

    View settings(){
        ScrollView sv=new ScrollView(this);LinearLayout c=col();c.setPadding(dp(14),dp(6),dp(14),dp(22));sv.addView(c);
        LinearLayout ai=strong();ai.addView(label("DI ŽAIDIMO MEISTRAS"));boolean has=!SecureKeyStore.load(this).isEmpty();ai.addView(t(has?"Groq raktas saugomas Android raktų saugykloje":"Groq raktas nenustatytas",14,has?TEAL:MUT,true));Button key=accent(has?"PAKEISTI API RAKTĄ":"ĮVESTI API RAKTĄ");key.setOnClickListener(v->key());ai.addView(key,m(dp(5)));c.addView(ai,m(dp(9)));
        LinearLayout sav=card();sav.addView(label("IŠSAUGOJIMAS IR ATKŪRIMAS"));Button undo=outline("↶ ATŠAUKTI PASKUTINĮ ĖJIMĄ");undo.setOnClickListener(v->{if(db.undo()){state=db.loadState();feedback="↶ Atkurtas ankstesnis kontrolinis taškas";show("game");}else Toast.makeText(this,"Nėra ankstesnio kontrolinio taško",Toast.LENGTH_SHORT).show();});sav.addView(undo,m(dp(5)));Button ex=outline("KOPIJUOTI IŠSAUGOJIMĄ");ex.setOnClickListener(v->export());sav.addView(ex,m(dp(5)));Button im=outline("IMPORTUOTI IŠSAUGOJIMĄ");im.setOnClickListener(v->importSave());sav.addView(im,m(dp(5)));Button reset=outline("ATKURTI PRADINĘ BŪSENĄ");reset.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Atkurti pradinę būseną?").setMessage("Vietiniai ėjimai ir įrangos pakeitimai bus panaikinti. API raktas liks telefone.").setNegativeButton("Ne",null).setPositiveButton("Atkurti",(d,w)->{db.reset();state=db.loadState();feedback="Atkurta kanoninė Luminara pradžia";show("game");}).show());sav.addView(reset);c.addView(sav,m(dp(9)));
        LinearLayout tech=card();tech.addView(label("VEIKIMO PRINCIPAS"));tech.addView(t("Telefonas → vietinė SQLite duomenų bazė → Groq tik naujai scenai",13,TEXT,true));tech.addView(t("Žaidimo būsena, įranga, žemėlapis ir ėjimų istorija lieka telefone. Be Groq rakto pagrindinės vietinės funkcijos ir atsarginis scenos sprendimas veikia toliau.",11,MUT,false));c.addView(tech,m(dp(8)));return sv;
    }

    void act(String action){if(busy)return;db.checkpoint("prieš veiksmą",state);busy=true;feedback="⟳ Sprendžiama…";show("game");String key=SecureKeyStore.load(this);pool.execute(()->{JSONObject r;String warn=null;try{r=key.isEmpty()?local(action):GroqClient.resolveTurn(key,state,action,db.equippedSummary(),db.getAbilities());}catch(Exception e){r=local(action);warn="Groq nepasiekiamas · panaudotas vietinis sprendimas";}JSONObject rr=r;String ww=warn;runOnUiThread(()->finish(action,rr,ww));});}

    void finish(String action,JSONObject r,String warn){
        int hp=state.hp,ma=state.mana,st=state.stamina,ae=state.aeonic;long cr=state.crowns;String old=state.location;String event=r.optString("event_tag","none");
        state.applyTurn(r);ArrayList<String> gained=new ArrayList<>();JSONArray loot=r.optJSONArray("loot");if(loot!=null)for(int i=0;i<loot.length();i++){JSONObject o=loot.optJSONObject(i);if(o==null)continue;String name=o.optString("name","Nežinomas radinys");db.addLoot(name,o.optString("category","artifact"),o.optString("rarity","common"),o.optString("description",""));gained.add(name);}
        state.recentTurns.add(action+" → "+state.sceneTitle);while(state.recentTurns.size()>12)state.recentTurns.remove(0);db.saveState(state);
        StringBuilder f=new StringBuilder();String ev=eventLabel(event);if(!ev.isEmpty())f.append("◆ ").append(ev).append("  ");delta(f,"gyvybė",state.hp-hp);delta(f,"mana",state.mana-ma);delta(f,"ištvermė",state.stamina-st);delta(f,"eoninė energija",state.aeonic-ae);delta(f,"karūnos",state.crowns-cr);if(!old.equals(state.location)){if(f.length()>0)f.append("  ");f.append("📍 ").append(state.location);}if(!gained.isEmpty()){if(f.length()>0)f.append("  ");f.append("🎁 Gauta: ").append(String.join(", ",gained));}if(warn!=null){if(f.length()>0)f.append("  ");f.append(warn);}feedback=f.length()==0?"✓ Ėjimas išspręstas":f.toString();busy=false;show("game");
    }

    JSONObject local(String action){
        try{JSONObject o=new JSONObject();o.put("scene_title","Veiksmas užfiksuotas");o.put("scene","Bandai: „"+action+"“. Ryšys su DI žaidimo meistru nepasiekiamas, todėl vietinis režimas nepakeičia kanoninės pasaulio būsenos ir leidžia saugiai tęsti vėliau.");JSONArray c=new JSONArray().put("Apsidairyti ir surinkti daugiau informacijos").put("Patikrinti turimą įrangą ir užrašus").put("Palaukti tinkamo momento ir stebėti aplinką");o.put("choices",c);o.put("location",state.location);o.put("time_minutes",1);o.put("hp_delta",0);o.put("mana_delta",0);o.put("stamina_delta",0);o.put("aeonic_delta",0);o.put("crowns_delta",0);o.put("quest_note",state.objective);o.put("event_tag","none");o.put("combat_active",state.combatActive);o.put("enemy_name",state.combatActive?state.enemyName:"");o.put("enemy_status",state.combatActive?state.enemyStatus:"");o.put("enemy_telegraph",state.combatActive?state.enemyTelegraph:"");o.put("combat_distance",state.combatActive?state.combatDistance:"mid");o.put("combat_hazard",state.combatActive?state.combatHazard:"");o.put("loot",new JSONArray());return o;}catch(Exception e){return new JSONObject();}
    }

    void key(){EditText i=new EditText(this);i.setHint("gsk_…");i.setSingleLine(true);i.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);new AlertDialog.Builder(this).setTitle("Groq API raktas").setMessage("Raktas šifruojamas Android raktų saugykloje ir lieka šiame telefone.").setView(i).setNegativeButton("Atšaukti",null).setPositiveButton("Išsaugoti",(d,w)->{try{String k=i.getText().toString().trim();if(k.length()<10)throw new Exception();SecureKeyStore.save(this,k);show("settings");}catch(Exception e){Toast.makeText(this,"Nepavyko išsaugoti rakto",Toast.LENGTH_SHORT).show();}}).show();}
    void export(){((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Vaeloria išsaugojimas",db.exportSave()));Toast.makeText(this,"Išsaugojimas nukopijuotas",Toast.LENGTH_SHORT).show();}
    void importSave(){ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(!cm.hasPrimaryClip()){Toast.makeText(this,"Iškarpinė tuščia",Toast.LENGTH_SHORT).show();return;}String x=cm.getPrimaryClip().getItemAt(0).coerceToText(this).toString();if(db.importSave(x)){state=db.loadState();feedback="Išsaugojimas importuotas";show("game");}else Toast.makeText(this,"Netinkamas Vaeloria išsaugojimas",Toast.LENGTH_SHORT).show();}

    View errorView(Throwable e){ScrollView sv=new ScrollView(this);LinearLayout c=col();c.setPadding(dp(18),dp(18),dp(18),dp(18));sv.addView(c);c.addView(t("Ekrano klaida",20,RED,true));c.addView(t(e.getClass().getSimpleName()+"\n"+safe(e.getMessage(),"be papildomo aprašymo"),12,TEXT,false));Button b=accent("GRĮŽTI Į ŽAIDIMĄ");b.setOnClickListener(v->show("game"));c.addView(b,m(dp(12)));return sv;}

    LinearLayout card(){LinearLayout x=col();x.setPadding(dp(13),dp(12),dp(13),dp(12));x.setBackground(round(SUR,17,Color.rgb(36,57,68)));return x;} LinearLayout strong(){LinearLayout x=col();x.setPadding(dp(14),dp(13),dp(14),dp(13));x.setBackground(round(SUR2,19,Color.argb(130,214,182,107)));return x;} LinearLayout col(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);return x;} LinearLayout row(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.HORIZONTAL);return x;}
    TextView t(String s,int z,int color,boolean bold){TextView x=new TextView(this);x.setText(s);x.setTextSize(z);x.setTextColor(color);x.setTypeface(Typeface.create(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL));x.setLineSpacing(0,1.08f);return x;} TextView label(String s){TextView x=t(s,10,GOLD,true);x.setPadding(0,0,0,dp(6));return x;} TextView center(String s,int z,int color){TextView x=t(s,z,color,false);x.setGravity(Gravity.CENTER);return x;} TextView chip(String s){TextView x=t(s,9,TEAL,true);x.setGravity(Gravity.CENTER);x.setPadding(dp(9),0,dp(9),0);x.setBackground(round(Color.argb(55,82,177,167),18,Color.argb(90,82,177,167)));return x;}
    Button choice(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(12);b.setTextColor(TEXT);b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);b.setBackground(round(SUR2,15,Color.rgb(47,70,81)));return b;} Button accent(String s){Button b=new Button(this);b.setText(s);b.setTextSize(11);b.setTextColor(BG);b.setTypeface(Typeface.DEFAULT_BOLD);b.setBackground(round(GOLD,13,GOLD));return b;} Button outline(String s){Button b=new Button(this);b.setText(s);b.setTextSize(10);b.setTextColor(TEXT);b.setBackground(round(Color.TRANSPARENT,13,Color.rgb(55,80,91)));return b;}
    LinearLayout res(String name,int v,int max,int color){LinearLayout x=col();x.setPadding(dp(2),0,dp(2),0);TextView a=center(name,8,MUT);x.addView(a);TextView n=center(v+"/"+max,12,TEXT);n.setTypeface(Typeface.DEFAULT_BOLD);x.addView(n);ProgressBar b=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);b.setMax(max);b.setProgress(v);b.setProgressTintList(android.content.res.ColorStateList.valueOf(color));b.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(34,49,57)));x.addView(b,new LinearLayout.LayoutParams(-1,dp(6)));return x;} View bar(String name,int v,int max,int color){LinearLayout x=col();x.addView(t(name+"  "+v+"/"+max,10,MUT,true));ProgressBar b=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);b.setMax(max);b.setProgress(v);b.setProgressTintList(android.content.res.ColorStateList.valueOf(color));x.addView(b,new LinearLayout.LayoutParams(-1,dp(7)));return x;} View obj(String icon,String s,boolean a){LinearLayout x=row();x.setPadding(0,dp(6),0,dp(6));x.addView(t(icon,14,a?GOLD:MUT,true),new LinearLayout.LayoutParams(dp(26),-2));x.addView(t(s,11,a?TEXT:MUT,a),new LinearLayout.LayoutParams(0,-2,1));return x;}
    GradientDrawable round(int fill,int r,int stroke){GradientDrawable g=new GradientDrawable();g.setColor(fill);g.setCornerRadius(dp(r));if(Color.alpha(stroke)>0)g.setStroke(dp(1),stroke);return g;} LinearLayout.LayoutParams m(int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,bottom);return p;} LinearLayout.LayoutParams w1(){return new LinearLayout.LayoutParams(0,dp(55),1);} int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    int rarity(String r){return "unique".equals(r)?Color.rgb(229,118,210):"legendary".equals(r)?GOLD:"ancient".equals(r)?Color.rgb(188,115,224):"epic".equals(r)?Color.rgb(196,92,220):"rare".equals(r)?Color.rgb(88,157,225):"uncommon".equals(r)?GREEN:TEXT;}
    String rarityLabel(String r){if(r==null)return"paprastas";switch(r){case"unique":return"unikalus";case"legendary":return"legendinis";case"ancient":return"senovinis";case"epic":return"epinis";case"rare":return"retas";case"uncommon":return"neįprastas";default:return"paprastas";}}
    String categoryLabel(String s){if(s==null)return"Artefaktas";if("ring".equals(s))return"Žiedas";if("relic".equals(s))return"Relikvija";for(String x:VaeloriaDb.EQUIPMENT_SLOTS)if(x.equals(s))return VaeloriaDb.slotLabel(x);return s;}
    String abilityType(String t){if(t==null)return"Gebėjimas";return t.replace("gebėjimas_virš_ribos","Gebėjimas virš ribos").replace("tobulinimas_virš_ribos","Tobulinimas virš ribos").replace("principas_virš_ribos","Principas virš ribos").replace("technika_virš_ribos","Technika virš ribos").replace('_',' ');}
    String eventLabel(String e){if(e==null)return"";switch(e){case"combat":return"KOVA";case"discovery":return"ATRADIMAS";case"social":return"BENDRAVIMAS";case"travel":return"KELIONĖ";case"reward":return"ATLYGIS";case"setback":return"NESĖKMĖ";default:return"";}}
    String safe(String s,String d){return s==null||s.isEmpty()?d:s;} String clock(){long day=state.worldMinute/1440,mn=state.worldMinute%1440;return "Diena "+day+" · "+String.format(Locale.ROOT,"%02d:%02d",mn/60,mn%60);} 
    String lore(String n){if("Luminara".equals(n))return"Septynių plaukiojančių maginių žiedų metropolis.";if(n.contains("Drakono Pabudimo"))return"Drakonų teritorija ir itin pavojingi kalnai.";if(n.contains("Kharad"))return"Kalvystės miestas ir nuliui atsparaus amato centras.";if(n.contains("Tuščiavidur"))return"Erdvinės ir nulinės anomalijos čia persidengia.";if(n.contains("Žaliasis")||n.contains("Šventųjų")||n.contains("Šaltinio"))return"Gyvybės, senųjų kelių ir nestabilių gamtos jėgų regionas.";return"Kanoninė Vaeloria vieta, susieta su gyvu kelionių tinklu.";}
    void delta(StringBuilder b,String n,long d){if(d!=0){if(b.length()>0)b.append("  ");b.append(d>0?"+":"").append(d).append(" ").append(n);}}
}
