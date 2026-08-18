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
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class VaeloriaActivity extends Activity {
    private static final int EXPORT_REQ=41, IMPORT_REQ=42;
    static final int BG=Color.rgb(7,15,22),SUR=Color.rgb(16,28,37),SUR2=Color.rgb(23,38,49),TEXT=Color.rgb(238,239,232),MUT=Color.rgb(166,183,188),GOLD=Color.rgb(214,182,107),TEAL=Color.rgb(82,177,167),GREEN=Color.rgb(94,185,131),RED=Color.rgb(213,91,82);
    VaeloriaDb db; GameState state; FrameLayout content; LinearLayout nav,root; String screen="game",feedback=""; boolean busy=false; StatEngine.Check pendingCheck; int pendingMasteryBonus=0,pendingPrimaryXp=0,pendingSecondaryXp=0; final ExecutorService pool=Executors.newSingleThreadExecutor();

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
        top.addView(t("VAELORIA",22,GOLD,true),new LinearLayout.LayoutParams(0,dp(52),1));top.addView(chip("VIETINIS · "+BuildConfig.VERSION_NAME));
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
        LinearLayout bars=row();bars.addView(res("GYVYBĖ",state.hp,state.hpMax,GREEN),w1());bars.addView(res("MANA",state.mana,state.manaMax,TEAL),w1());bars.addView(res("IŠTVERMĖ",state.stamina,state.staminaMax,GOLD),w1());bars.addView(res("EONINĖ",state.aeonic,state.aeonicMax,Color.rgb(154,115,212)),w1());c.addView(bars,m(dp(10)));
        if(!feedback.isEmpty()){TextView f=t(feedback,11,TEXT,true);f.setPadding(dp(10),dp(8),dp(10),dp(8));f.setBackground(round(Color.rgb(24,52,50),13,Color.argb(100,82,177,167)));c.addView(f,m(dp(10)));}
        if(state.combatActive){
            LinearLayout x=strong();x.addView(label("⚔ KOVA · "+safe(state.enemyName,"PRIEŠAS").toUpperCase(Locale.ROOT)));CombatBoardView b=new CombatBoardView(this);b.setState(state);x.addView(b,new LinearLayout.LayoutParams(-1,dp(190)));x.addView(t("PRIEŠO KETINIMAS · "+safe(state.enemyTelegraph,"Ketinimas dar neaiškus"),12,Color.rgb(236,157,111),true));if(!state.enemyStatus.isEmpty())x.addView(t("Būsena: "+state.enemyStatus,11,MUT,false));c.addView(x,m(dp(10)));
        }else{
            SceneBannerView art=new SceneBannerView(this);art.setScene(state.location,state.sceneTitle,"",state.worldMinute);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(210));p.setMargins(0,0,0,dp(10));c.addView(art,p);
        }
        LinearLayout q=card();q.addView(label("AKTYVUS SIUŽETAS"));q.addView(t(state.questTitle,18,GOLD,true));q.addView(t(state.objective,12,MUT,false));c.addView(q,m(dp(10)));
        LinearLayout sc=strong();sc.addView(label("DABARTINĖ SCENA"));sc.addView(t(state.sceneTitle,22,TEXT,true));TextView story=t(state.scene,15,Color.rgb(225,229,225),false);story.setLineSpacing(0,1.15f);story.setPadding(0,dp(6),0,0);sc.addView(story);c.addView(sc,m(dp(12)));
        c.addView(label("KĄ DARAI?"));for(int i=0;i<Math.min(3,state.choices.size());i++){String a=state.choices.get(i);String nr=i==0?"①":i==1?"②":"③";Button b=choice(nr+"   "+a);b.setEnabled(!busy);b.setOnClickListener(v->act(a));c.addView(b,m(dp(7)));}
        LinearLayout free=card();free.addView(label("LAISVAS VEIKSMAS"));EditText in=new EditText(this);in.setHint("Aprašyk bet kokį veiksmą…");in.setHintTextColor(Color.rgb(113,137,145));in.setTextColor(TEXT);in.setMinLines(2);in.setMaxLines(5);in.setBackground(round(Color.rgb(10,21,29),12,Color.rgb(43,65,76)));in.setPadding(dp(11),dp(8),dp(11),dp(8));free.addView(in);Button send=accent(busy?"SPRENDŽIAMA…":"ATLIKTI VEIKSMĄ");send.setEnabled(!busy);send.setOnClickListener(v->{String a=in.getText().toString().trim();if(!a.isEmpty())act(a);});free.addView(send,m(dp(6)));boolean hasKey=!SecureKeyStore.load(this).isEmpty();free.addView(t(hasKey?"DI žaidimo meistras · Groq · gpt-oss-120b":"VIETINIS REŽIMAS · DI žaidimo meistras išjungtas",10,hasKey?TEAL:MUT,false));c.addView(free,m(dp(8)));return sv;
    }

    View hero(){
        ScrollView sv=new ScrollView(this);LinearLayout c=col();c.setPadding(dp(14),dp(6),dp(14),dp(22));sv.addView(c);
        LinearLayout top=strong();top.addView(label("VEIKĖJAS"));top.addView(t("EINORAS",26,TEXT,true));top.addView(t("201 m. chronologinis · 20 m. biologinis · biologinis amžius nekinta",11,MUT,false));LinearLayout tags=row();tags.addView(chip("LEGENDINĖ BAZĖ"));Space gap=new Space(this);tags.addView(gap,new LinearLayout.LayoutParams(dp(7),1));tags.addView(chip("92 / 92 · 100/100"));top.addView(tags,m(dp(4)));c.addView(top,m(dp(10)));

        Map<String,Integer> mastery=db.getMasteryLevels();int sum=0;for(String stat:mastery.keySet())sum+=mastery.get(stat);int avg=mastery.isEmpty()?70:Math.round(sum/(float)mastery.size());
        LinearLayout stats=card();stats.addView(label("PAŽANGA"));stats.addView(t("Bazinės savybės užbaigtos · meistriškumas virš bazinės ribos "+avg+"/100",15,TEXT,true));stats.addView(bar("VIDUTINIS MEISTRIŠKUMAS",avg,100,GOLD));stats.addView(t("Kiekviena naudojama savybė gauna atskirą patirtį. Meistriškumas realiai keičia patikros rezultatą, bet nekelia bazinės savybės virš 100/100.",11,MUT,false));Button allStats=outline("PERŽIŪRĖTI VISAS 92 SAVYBES IR PATIRTĮ");allStats.setOnClickListener(v->startActivity(new Intent(this,StatsActivity.class)));stats.addView(allStats,m(dp(7)));c.addView(stats,m(dp(9)));

        c.addView(equipmentPanel(),m(dp(9)));

        LinearLayout loose=card();loose.addView(label("NEUŽDĖTA ĮRANGA"));int looseCount=0;for(VaeloriaDb.Item i:db.getItems())if(i.slot!=null&&!i.equipped){loose.addView(inventoryRow(i));looseCount++;}if(looseCount==0)loose.addView(t("Visi šiuo metu turimi dėvimi daiktai yra užsidėti. Nauja įranga gali atsirasti kaip pagrįstas radinys, atlygis, pirkinys ar grobis.",11,MUT,false));c.addView(loose,m(dp(9)));

        LinearLayout inv=card();inv.addView(label("ARTEFAKTAI IR ĮGALIOJIMAI"));for(VaeloriaDb.Item i:db.getItems())if(i.slot==null){inv.addView(t(i.name+" · "+rarityLabel(i.rarity).toUpperCase(Locale.ROOT),13,rarity(i.rarity),true));inv.addView(t(i.description,10,MUT,false));}c.addView(inv,m(dp(9)));

        LinearLayout ab=col();ab.addView(label("GEBĖJIMAI VIRŠ BAZINĖS RIBOS"));for(String[] a:db.getAbilities())ab.addView(abilityCard(a),m(dp(7)));c.addView(ab,m(dp(8)));return sv;
    }

    View abilityCard(String[] a){LinearLayout x=card();LinearLayout h=row();TextView tag=t(AbilityMechanics.tag(a[1]),9,TEAL,true);tag.setPadding(dp(8),dp(3),dp(8),dp(3));tag.setBackground(round(Color.argb(45,82,177,167),10,Color.argb(100,82,177,167)));h.addView(tag);x.addView(h,m(dp(5)));x.addView(t(AbilityMechanics.displayName(a[0]),14,TEXT,true));x.addView(t(a[2],10,MUT,false));TextView mech=t("MECHANIKA · "+AbilityMechanics.mechanic(a[0]),10,GOLD,true);mech.setPadding(0,dp(7),0,0);x.addView(mech);return x;}

    View equipmentPanel(){
        LinearLayout gear=strong();gear.addView(label("DĖVIMA ĮRANGA"));gear.addView(t("Paliesk įrangos vietą aplink siluetą. Spalvotas rėmelis reiškia uždėtą daiktą; tuščia vieta gali būti užpildyta žaidimo metu.",11,MUT,false));CharacterLoadoutView v=new CharacterLoadoutView(this);for(String slot:VaeloriaDb.EQUIPMENT_SLOTS){VaeloriaDb.Item i=db.getEquippedAt(slot);v.put(slot,VaeloriaDb.slotLabel(slot),i==null?"":i.name,i==null?MUT:rarity(i.rarity));}v.setListener(this::openSlot);gear.addView(v,new LinearLayout.LayoutParams(-1,dp(510)));return gear;
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
        LinearLayout r=col();r.setPadding(dp(10),dp(4),dp(10),dp(8));
        LinearLayout h=card();h.addView(label("PASAULIO ŽEMĖLAPIS"));h.addView(t("◆ Dabartinė vieta: "+state.location,13,TEXT,true));h.addView(t("Priartink dviem pirštais, tempk ir paliesk vietą. Pasirinkus vietą matysi grėsmę ir galėsi pradėti kelionę.",10,MUT,false));r.addView(h,m(dp(7)));
        WorldMapView map=new WorldMapView(this);map.setCurrentLocation(state.location);map.setListener((name,danger)->locationDialog(name,danger));r.addView(map,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout legend=card();legend.addView(t("GRĖSMĖ  ● 1–3 žema   ● 4–5 vidutinė   ● 6–7 aukšta   ● 8–10 labai aukšta",9,MUT,true));r.addView(legend,m(dp(2)));return r;
    }

    void locationDialog(String name,int danger){new AlertDialog.Builder(this).setTitle(name).setMessage("Grėsmė: "+danger+"/10\n\n"+lore(name)+"\n\nKelionė gali pareikalauti laiko, išteklių, susidūrimo ar kelionės vartų sprendimo.").setNegativeButton("Uždaryti",null).setPositiveButton("KELIAUTI",(d,w)->{show("game");act("Keliauti iš "+state.location+" į "+name+" saugiausiu pagrįstu maršrutu, įvertinant kelionės laiką ir realias pasekmes.");}).show();}

    View journal(){
        ScrollView sv=new ScrollView(this);LinearLayout c=col();c.setPadding(dp(14),dp(6),dp(14),dp(22));sv.addView(c);
        LinearLayout q=strong();q.addView(label("PAGRINDINĖ SIUŽETO LINIJA · I DALIS"));q.addView(t("LŪŽĘS MERIDIANAS",20,GOLD,true));q.addView(t("PROGRESAS · 1 / 4",10,TEAL,true));q.addView(obj("◆",state.objective,true));q.addView(obj("○","Užsitikrinti tris nepriklausomai prižiūrimus kelio atramos taškus",false));q.addView(obj("○","Pereiti Meridianą ir grįžti su patikrinamais Orisono kontakto duomenimis",false));q.addView(obj("○","Derėtis dėl Pirmosios Meridiano chartijos arba ją sąmoningai atmesti",false));c.addView(q,m(dp(9)));
        LinearLayout ev=card();ev.addView(label("SURINKTI ĮRODYMAI"));ev.addView(t("• Karavano manifestas Luminara pasiekė anksčiau už patį karavaną.",11,TEXT,false));ev.addView(t("• Laiko neatitikimas sutampa su nauju kelionės vartų poslinkiu.",11,TEXT,false));ev.addView(t("• Vakarinio kelionės vartų tranzito žyma yra pirmas tikrinamas fizinis pėdsakas.",11,TEXT,false));c.addView(ev,m(dp(9)));
        LinearLayout th=card();th.addView(label("AKTYVIOS SIUŽETO GIJOS"));for(String x:new String[]{"Atviri horizontai · Lūžęs Meridianas","Orisono ryšio protokolas","Kelionės vartų poslinkis","Santarvės priežiūros valdymas","Drakoniškoji įpėdinystė"}){TextView row=t("• "+x,11,TEXT,false);row.setPadding(0,dp(4),0,dp(4));th.addView(row);}c.addView(th,m(dp(9)));
        LinearLayout log=card();log.addView(label("PASKUTINIAI ĖJIMAI"));if(state.recentTurns.isEmpty())log.addView(t("Šiame įrenginyje ėjimų istorijos dar nėra.",11,MUT,false));else for(int i=state.recentTurns.size()-1;i>=0;i--)log.addView(t("• "+state.recentTurns.get(i),11,TEXT,false));c.addView(log,m(dp(8)));return sv;
    }

    View settings(){
        ScrollView sv=new ScrollView(this);LinearLayout c=col();c.setPadding(dp(14),dp(6),dp(14),dp(22));sv.addView(c);
        LinearLayout ai=strong();ai.addView(label("DI ŽAIDIMO MEISTRAS"));boolean has=!SecureKeyStore.load(this).isEmpty();ai.addView(t(has?"Groq raktas saugomas Android raktų saugykloje":"Groq raktas nenustatytas",14,has?TEAL:MUT,true));Button key=accent(has?"PAKEISTI API RAKTĄ":"ĮVESTI API RAKTĄ");key.setOnClickListener(v->key());ai.addView(key,m(dp(5)));c.addView(ai,m(dp(9)));
        LinearLayout sav=card();sav.addView(label("IŠSAUGOJIMAS IR ATKŪRIMAS"));Button undo=outline("↶ ATŠAUKTI PASKUTINĮ ĖJIMĄ");undo.setOnClickListener(v->{if(db.undo()){state=db.loadState();feedback="↶ Atkurtas ankstesnis kontrolinis taškas";show("game");}else Toast.makeText(this,"Nėra ankstesnio kontrolinio taško",Toast.LENGTH_SHORT).show();});sav.addView(undo,m(dp(5)));Button ex=outline("KOPIJUOTI IŠSAUGOJIMĄ");ex.setOnClickListener(v->export());sav.addView(ex,m(dp(5)));Button im=outline("IMPORTUOTI IŠSAUGOJIMĄ");im.setOnClickListener(v->importSave());sav.addView(im,m(dp(5)));Button reset=outline("ATKURTI PRADINĘ BŪSENĄ");reset.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Atkurti pradinę būseną?").setMessage("Vietiniai ėjimai ir įrangos pakeitimai bus panaikinti. API raktas liks telefone.").setNegativeButton("Ne",null).setPositiveButton("Atkurti",(d,w)->{db.reset();state=db.loadState();feedback="Atkurta kanoninė Luminara pradžia";show("game");}).show());sav.addView(reset);c.addView(sav,m(dp(9)));
        LinearLayout tech=card();tech.addView(label("VEIKIMO PRINCIPAS"));tech.addView(t("Telefonas → vietinė SQLite duomenų bazė → Groq tik naujai scenai",13,TEXT,true));tech.addView(t("Žaidimo būsena, įranga, žemėlapis ir ėjimų istorija lieka telefone. Be Groq rakto pagrindinės vietinės funkcijos ir atsarginis scenos sprendimas veikia toliau.",11,MUT,false));c.addView(tech,m(dp(8)));return sv;
    }

    void act(String action){if(busy)return;db.checkpoint("prieš veiksmą",state);String equipped=db.equippedSummary();List<String[]> abilities=db.getAbilities();Map<String,Integer> statValues=db.getStatValues();pendingCheck=StatEngine.resolve(state,action,equipped,abilities,statValues);Map<String,Integer> mastery=db.getMasteryLevels();pendingMasteryBonus=MasteryEngine.checkBonus(pendingCheck.primary,pendingCheck.secondary,mastery);MasteryEngine.apply(pendingCheck,pendingMasteryBonus);pendingPrimaryXp=MasteryEngine.primaryXp(pendingCheck);pendingSecondaryXp=MasteryEngine.secondaryXp(pendingCheck);busy=true;feedback="⟳ Sprendžiama…\n"+pendingCheck.compact()+" · meistriškumas +"+pendingMasteryBonus;show("game");String key=SecureKeyStore.load(this);StatEngine.Check check=pendingCheck;pool.execute(()->{JSONObject r;String warn=null;try{r=key.isEmpty()?local(action,check):GroqClient.resolveTurn(key,state,action,equipped,abilities,check);}catch(Exception e){r=local(action,check);warn="Groq nepasiekiamas · panaudotas vietinis sprendimas";}JSONObject rr=r;String ww=warn;runOnUiThread(()->finish(action,rr,ww));});}

    void finish(String action,JSONObject r,String warn){
        int hp=state.hp,ma=state.mana,st=state.stamina,ae=state.aeonic;long cr=state.crowns;String old=state.location;String event=r.optString("event_tag","none");boolean wasCombat=state.combatActive;String defeatedEnemy=state.enemyName;
        state.applyTurn(r);if(pendingCheck!=null){db.awardMastery(pendingCheck.primary,pendingPrimaryXp);db.awardMastery(pendingCheck.secondary,pendingSecondaryXp);}ArrayList<String> gained=new ArrayList<>();boolean victory="combat_victory".equals(event)||(wasCombat&&!state.combatActive&&!"combat_escape".equals(event)&&!"combat_end".equals(event));if(victory&&!defeatedEnemy.isEmpty()){for(ItemCatalogV092.ItemDef drop:DropTableV092.roll(defeatedEnemy,state.worldMinute+action.hashCode())){db.addCatalogLoot(drop,1);gained.add(drop.name);}}else{JSONArray loot=r.optJSONArray("loot");if(loot!=null)for(int i=0;i<loot.length();i++){JSONObject o=loot.optJSONObject(i);if(o==null)continue;String name=o.optString("name","Nežinomas radinys");db.addLoot(name,o.optString("category","artifact"),o.optString("rarity","common"),o.optString("description",""));gained.add(name);}}
        state.recentTurns.add(action+" → "+state.sceneTitle+(pendingCheck==null?"":" · "+pendingCheck.primary+": "+pendingCheck.outcome));while(state.recentTurns.size()>12)state.recentTurns.remove(0);db.saveState(state);
        StringBuilder f=new StringBuilder();if(pendingCheck!=null)f.append(pendingCheck.compact()).append(" · ").append(MasteryEngine.effectLine(pendingMasteryBonus,pendingPrimaryXp,pendingSecondaryXp));String ev=eventLabel(event);if(!ev.isEmpty()){if(f.length()>0)f.append("  ");f.append("◆ ").append(ev);}delta(f,"gyvybė",state.hp-hp);delta(f,"mana",state.mana-ma);delta(f,"ištvermė",state.stamina-st);delta(f,"eoninė energija",state.aeonic-ae);delta(f,"karūnos",state.crowns-cr);if(!old.equals(state.location)){if(f.length()>0)f.append("  ");f.append("📍 ").append(state.location);}if(!gained.isEmpty()){if(f.length()>0)f.append("  ");f.append("🎁 Gauta: ").append(String.join(", ",gained));}if(warn!=null){if(f.length()>0)f.append("  ");f.append(warn);}feedback=f.length()==0?"✓ Ėjimas išspręstas":f.toString();busy=false;pendingCheck=null;pendingMasteryBonus=0;pendingPrimaryXp=0;pendingSecondaryXp=0;show("game");
    }

    JSONObject local(String action,StatEngine.Check check){
        try{
            String a=action==null?"":action.trim();
            String q=a.toLowerCase(Locale.ROOT);
            JSONObject o=new JSONObject();
            JSONArray c=new JSONArray();
            String title="Veiksmas įvykdytas";
            String scene="Einoras imasi veiksmo: „"+a+"“. Aplinka sureaguoja, laikas juda pirmyn, o rezultatas įrašomas į vietinę pasaulio būseną.";
            String location=state.location;
            String questNote=state.objective;
            String event="action";
            int minutes=6,hp=0,mana=0,stamina=-1,aeonic=0;
            int asterraDelta=0,dravennDelta=0,lysaraDelta=0;
            long crowns=0;
            boolean combat=state.combatActive;
            String enemy=combat?state.enemyName:"";
            String enemyStatus=combat?state.enemyStatus:"";
            String telegraph=combat?state.enemyTelegraph:"";
            String distance=combat?state.combatDistance:"mid";
            String hazard=combat?state.combatHazard:"";
            int enemyHp=combat?state.enemyHp:0;
            int enemyHpMax=combat?state.enemyHpMax:0;
            int combatRound=combat?state.combatRound:0;

            if(q.contains("manifest")){
                title="Manifesto neatitikimas";minutes=12;stamina=0;event="discovery";
                scene="Palyginęs registracijos žymas, antspaudą ir pristatymo seką randi tikrą neatitikimą: manifesto įrašas sistemoje atsirado anksčiau, nei karavanas galėjo fiziškai pasiekti Luminara. Tai jau ne gandas, o patikrinamas Meridiano poslinkio pėdsakas.";
                questNote="Patvirtintas pirmas lauko įrodymas: manifesto registracijos laikas nesutampa su fiziniu karavano atvykimu.";
                c.put("Patikrinti Meridiano vartų žurnalą").put("Surasti karavano liudininkus").put("Palyginti įrašą su Veyrhold duomenimis");
            }else if(q.contains("meridian")||q.contains("vart")||q.contains("waygate")){
                title="Vartų rezonansas";minutes=15;stamina=-2;aeonic=q.contains("kalib")?-4:0;event="discovery";
                scene="Prie vartų rezonanso laukas pulsuoja nevienodu ritmu. Keli matavimo taškai rodo tą pačią kryptį: problema nėra vien laikrodžių paklaida — pats atvykimo eiliškumas trumpam persislenka.";
                c.put("Matuoti poslinkį dar kartą").put("Sekti paskutinio atvykimo pėdsaką").put("Klausti vartų prižiūrėtojo apie ankstesnius atvejus");
            }else if(q.contains("poils")||q.contains("mieg")||q.contains("laukti")||q.contains("palaukt")||q.contains("stovykl")){
                title="Trumpas atokvėpis";minutes=30;stamina=12;mana=4;event="rest";
                scene="Skiri laiko atsikvėpti ir stebėti aplinką be skubėjimo. Kvėpavimas nurimsta, judesiai vėl tampa lengvi, o per tą laiką miestas gyvena toliau — sargybos keičiasi, prekybininkai juda, gandai sklinda.";
                c.put("Grįžti prie Meridiano tyrimo").put("Patikrinti naujus gandus").put("Apsilankyti centrinėje rinkoje");
            }else if(q.contains("kalb")||q.contains("paklaus")||q.contains("susisiekt")||q.contains("pasikalb")){
                title="Pokalbis Luminara";minutes=10;stamina=0;event="dialogue";
                scene="Pokalbis neduoda tobulo atsakymo, bet atskiria faktus nuo nuomonių. Vietiniai sutaria dėl vieno: pastaruoju metu vartų anomalijos kartojasi dažniau, tačiau skirtingi žmonės jas aiškina skirtingai.";
                c.put("Paprašyti konkretaus liudijimo").put("Paklausti, kas galėtų žinoti daugiau").put("Užrašyti informaciją ir tęsti tyrimą");
            }else if(q.contains("puol")||q.contains("kov")||q.contains("smūg")||q.contains("smug")||q.contains("ataka")||q.contains("pulti")){
                title="Kova prasideda";minutes=2;stamina=-8;hp=-2;event="combat";combat=true;
                if(enemy==null||enemy.isEmpty()){
                    EnemyCatalogV091.Enemy requested=EnemyCatalogV091.find(a);
                    EnemyCatalogV091.Enemy encounter=requested!=null?requested:EnemyCatalogV091.encounterFor(state.location,state.worldMinute+a.hashCode());
                    enemy=encounter.name;
                    enemyHp=encounter.hp;enemyHpMax=encounter.hp;combatRound=1;
                    enemyStatus="Budrus · pavojus "+encounter.danger+"/10 · gyvybė "+enemyHp+"/"+enemyHpMax;
                }else{
                    int damage=check==null?52:(check.outcome.contains("išskirtinė")?96:check.outcome.equals("sėkmė")?72:check.outcome.contains("dalinė")?48:26);
                    enemyHp=Math.max(0,enemyHp-damage);combatRound++;
                    if(enemyHp==0){title="Priešas nugalėtas";event="combat_victory";combat=false;hp=0;stamina=-4;scene="Tavo veiksmas pralaužia paskutinę priešininko gynybą. Kova baigta, o patvirtinta būtybės iškritimo lentelė pritaikoma vietiniame žaidimo variklyje.";c.put("Apžiūrėti gautą grobį").put("Atsigauti po kovos").put("Tęsti kelionę");}
                    else{enemyStatus="Spaudžiamas · gyvybė "+enemyHp+"/"+enemyHpMax;title="Kovos "+combatRound+" ėjimas";scene="Ataka pasiekia tikslą, tačiau priešininkas dar laikosi. Jo laikysena keičiasi pagal likusią gyvybę, todėl kitas veiksmas vis dar turi kainą ir riziką.";}
                }
                telegraph="Žemas žingsnis į šoną ir pasiruošimas kontratakai";
                distance="close";
                hazard="Slidus akmuo ir siauras praėjimas";
                if(combatRound==1){scene="Tu inicijuoji kontaktą. Priešininkas atsitraukia tik pusę žingsnio ir iškart persitvarko kontratakai; erdvė ankšta, todėl pozicija tampa svarbesnė už gryną jėgą.";}
                if(c.length()==0)c.put("Spausti ir neleisti atkurti distancijos").put("Išprovokuoti kontrataką ir bausti ją").put("Atsitraukti į saugesnę poziciją");
            }else if(q.contains("bėg")||q.contains("beg")||q.contains("trauktis")||q.contains("atsitrauk")){
                title="Atsitraukimas";minutes=4;stamina=-4;event="combat_escape";combat=false;
                enemy="";enemyStatus="";telegraph="";distance="mid";hazard="";enemyHp=0;enemyHpMax=0;combatRound=0;
                scene="Nutrauki kontaktą ir pasirenki erdvę, kurioje gali vėl vertinti situaciją. Priešininkas tavęs iškart nesiveja — kova baigiasi be aiškios pergalės, bet iniciatyva grįžta tau.";
                c.put("Stebėti, ar kas nors seka").put("Grįžti prie pagrindinės užduoties").put("Atsigauti prieš tęsiant kelią");
            }else if(q.contains("vykti")||q.contains("keliaut")||q.contains("keliauti")||q.contains("eiti į")||q.contains("eiti i")||q.contains("važiuoti")||q.contains("vaziuoti")){
                title="Kelionė";minutes=45;stamina=-5;event="travel";
                if(q.contains("veyrhold"))location="Veyrhold";
                else if(q.contains("stiklo")||q.contains("glasswood"))location="Stiklo Giria";
                else if(q.contains("kharad"))location="Kharad Vorn";
                else if(q.contains("safyro")||q.contains("sapphire"))location="Safyro Platybės";
                else if(q.contains("šventųjų")||q.contains("sventuju")||q.contains("pelkyn"))location="Šventųjų Pelkynas";
                else if(q.contains("labirint"))location="Žaliasis Labirintas";
                else if(q.contains("asterio")||q.contains("crown of aster"))location="Asterio Karūna";
                scene="Palieki "+state.location+" ir judi pasirinkta kryptimi. Kelionė užima laiko ir ištvermės; pakeliui stebi kelią, žmonių judėjimą bei Meridiano infrastruktūros ženklus. Pasieki: "+location+".";
                c.put("Apsidairyti naujoje vietoje").put("Ieškoti vietinių kontaktų").put("Patikrinti, ar čia jaučiamas Meridiano poslinkis");
            }else if(q.contains("tirti")||q.contains("ištirt")||q.contains("istirt")||q.contains("apžiūr")||q.contains("apziur")||q.contains("iešk")||q.contains("iesk")||q.contains("sekti")){
                title="Tyrimas";minutes=14;stamina=-2;event="discovery";
                scene="Sistemingai tikrini aplinką ir atmeti pirmus akivaizdžius paaiškinimus. Randi kelias smulkias detales, kurios atskirai nieko neįrodo, bet kartu parodo kryptį, kurią verta tikrinti toliau.";
                c.put("Patikrinti stipriausią pėdsaką").put("Palyginti radinius su ankstesniais įrašais").put("Paklausti vietinio liudininko");
            }else{
                title="Veiksmas pasaulyje";minutes=7;event="action";
                scene="Atlieki: „"+a+"“. Veiksmas turi pasekmę: praeina laikas, keičiasi tavo pozicija situacijoje, o aplinka pateikia naują informaciją vietoje ankstesnio statiško ekrano.";
                c.put("Tęsti tą pačią kryptį").put("Apsidairyti ir įvertinti pasekmes").put("Grįžti prie Lūžusio Meridiano užduoties");
            }

            if("dialogue".equals(event)){asterraDelta+=1;lysaraDelta+=1;}
            if("combat".equals(event)){dravennDelta+=2;asterraDelta-=1;}
            if("discovery".equals(event)){asterraDelta+=1;}
            if("travel".equals(event)){if(location.contains("Kharad")||location.contains("Safyro"))dravennDelta+=1;else if(location.contains("Pelkyn")||location.contains("Labirint"))lysaraDelta+=1;else asterraDelta+=1;}
            if(c.length()==0)c.put("Apsidairyti ir rinkti daugiau informacijos").put("Patikrinti įrangą ir užrašus").put("Tęsti pagrindinę užduotį");
            o.put("scene_title",title);
            o.put("scene",scene);
            o.put("choices",c);
            o.put("location",location);
            o.put("time_minutes",minutes);
            o.put("hp_delta",hp);
            o.put("mana_delta",mana);
            o.put("stamina_delta",stamina);
            o.put("aeonic_delta",aeonic);
            o.put("crowns_delta",crowns);
            o.put("quest_note",questNote);
            o.put("asterra_delta",asterraDelta);o.put("dravenn_delta",dravennDelta);o.put("lysara_delta",lysaraDelta);
            o.put("event_tag",event);
            o.put("combat_active",combat);
            o.put("enemy_name",combat?enemy:"");
            o.put("enemy_status",combat?enemyStatus:"");
            o.put("enemy_telegraph",combat?telegraph:"");
            o.put("combat_distance",combat?distance:"mid");
            o.put("combat_hazard",combat?hazard:"");
            o.put("enemy_hp",combat?enemyHp:0);
            o.put("enemy_hp_max",combat?enemyHpMax:0);
            o.put("combat_round",combat?combatRound:0);
            o.put("loot",new JSONArray());
            return o;
        }catch(Exception e){return new JSONObject();}
    }

    void key(){EditText i=new EditText(this);i.setHint("gsk_…");i.setSingleLine(true);i.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);new AlertDialog.Builder(this).setTitle("Groq API raktas").setMessage("Raktas šifruojamas Android raktų saugykloje ir lieka šiame telefone.").setView(i).setNegativeButton("Atšaukti",null).setPositiveButton("Išsaugoti",(d,w)->{try{String k=i.getText().toString().trim();if(!k.startsWith("gsk_")||k.length()<20)throw new Exception();if(!k.startsWith("gsk_")||k.length()<20)throw new Exception();SecureKeyStore.save(this,k);show("settings");}catch(Exception e){Toast.makeText(this,"Nepavyko išsaugoti rakto",Toast.LENGTH_SHORT).show();}}).show();}
    void export(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").putExtra(Intent.EXTRA_TITLE,"vaeloria-save.json");startActivityForResult(i,EXPORT_REQ);}
    void importSave(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,IMPORT_REQ);}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;try{android.net.Uri u=data.getData();if(req==EXPORT_REQ){try(OutputStream os=getContentResolver().openOutputStream(u)){os.write(db.exportSave().getBytes(StandardCharsets.UTF_8));}Toast.makeText(this,"Išsaugojimas eksportuotas",Toast.LENGTH_SHORT).show();}else if(req==IMPORT_REQ){StringBuilder b=new StringBuilder();try(BufferedReader r=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(u),StandardCharsets.UTF_8))){String line;while((line=r.readLine())!=null)b.append(line);}if(db.importSave(b.toString())){state=db.loadState();feedback="Išsaugojimas importuotas";show("game");}else Toast.makeText(this,"Netinkamas Vaeloria išsaugojimas",Toast.LENGTH_SHORT).show();}}catch(Exception e){Toast.makeText(this,"Failo klaida: "+e.getMessage(),Toast.LENGTH_LONG).show();}}

    View errorView(Throwable e){ScrollView sv=new ScrollView(this);LinearLayout c=col();c.setPadding(dp(18),dp(18),dp(18),dp(18));sv.addView(c);c.addView(t("Ekrano klaida",20,RED,true));c.addView(t(e.getClass().getSimpleName()+"\n"+safe(e.getMessage(),"be papildomo aprašymo"),12,TEXT,false));Button b=accent("GRĮŽTI Į ŽAIDIMĄ");b.setOnClickListener(v->show("game"));c.addView(b,m(dp(12)));return sv;}

    LinearLayout card(){LinearLayout x=col();x.setPadding(dp(13),dp(12),dp(13),dp(12));x.setBackground(round(SUR,17,Color.rgb(36,57,68)));return x;} LinearLayout strong(){LinearLayout x=col();x.setPadding(dp(14),dp(13),dp(14),dp(13));x.setBackground(round(SUR2,19,Color.argb(130,214,182,107)));return x;} LinearLayout col(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);return x;} LinearLayout row(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.HORIZONTAL);return x;}
    TextView t(String s,int z,int color,boolean bold){TextView x=new TextView(this);x.setText(s);x.setTextSize(z);x.setTextColor(color);x.setTypeface(Typeface.create(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL));x.setLineSpacing(0,1.08f);return x;} TextView label(String s){TextView x=t(s,10,GOLD,true);x.setPadding(0,0,0,dp(6));return x;} TextView center(String s,int z,int color){TextView x=t(s,z,color,false);x.setGravity(Gravity.CENTER);return x;} TextView chip(String s){TextView x=t(s,9,TEAL,true);x.setGravity(Gravity.CENTER);x.setPadding(dp(9),0,dp(9),0);x.setBackground(round(Color.argb(55,82,177,167),18,Color.argb(90,82,177,167)));return x;}
    Button choice(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(12);b.setTextColor(TEXT);b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);b.setPadding(dp(18),dp(8),dp(14),dp(8));b.setMinHeight(dp(64));b.setBackground(round(SUR2,15,Color.rgb(47,70,81)));return b;} Button accent(String s){Button b=new Button(this);b.setText(s);b.setTextSize(11);b.setTextColor(BG);b.setTypeface(Typeface.DEFAULT_BOLD);b.setBackground(round(GOLD,13,GOLD));return b;} Button outline(String s){Button b=new Button(this);b.setText(s);b.setTextSize(10);b.setTextColor(TEXT);b.setBackground(round(Color.TRANSPARENT,13,Color.rgb(55,80,91)));return b;}
    LinearLayout res(String name,int v,int max,int color){LinearLayout x=col();x.setPadding(dp(2),0,dp(2),0);TextView a=center(name,8,MUT);x.addView(a);TextView n=center(v+"/"+max,12,TEXT);n.setTypeface(Typeface.DEFAULT_BOLD);x.addView(n);ProgressBar b=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);b.setMax(max);b.setProgress(v);b.setProgressTintList(android.content.res.ColorStateList.valueOf(color));b.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(34,49,57)));x.addView(b,new LinearLayout.LayoutParams(-1,dp(6)));return x;} View bar(String name,int v,int max,int color){LinearLayout x=col();x.addView(t(name+"  "+v+"/"+max,10,MUT,true));ProgressBar b=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);b.setMax(max);b.setProgress(v);b.setProgressTintList(android.content.res.ColorStateList.valueOf(color));x.addView(b,new LinearLayout.LayoutParams(-1,dp(7)));return x;} View obj(String icon,String s,boolean a){LinearLayout x=row();x.setPadding(0,dp(6),0,dp(6));x.addView(t(icon,14,a?GOLD:MUT,true),new LinearLayout.LayoutParams(dp(26),-2));x.addView(t(s,11,a?TEXT:MUT,a),new LinearLayout.LayoutParams(0,-2,1));return x;}
    GradientDrawable round(int fill,int r,int stroke){GradientDrawable g=new GradientDrawable();g.setColor(fill);g.setCornerRadius(dp(r));if(Color.alpha(stroke)>0)g.setStroke(dp(1),stroke);return g;} LinearLayout.LayoutParams m(int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,bottom);return p;} LinearLayout.LayoutParams w1(){return new LinearLayout.LayoutParams(0,dp(55),1);} int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    int rarity(String r){return "unique".equals(r)?Color.rgb(86,224,221):"ancient".equals(r)?Color.rgb(188,115,224):"mythic".equals(r)?Color.rgb(238,94,144):"legendary".equals(r)?GOLD:"epic".equals(r)?Color.rgb(196,92,220):"rare".equals(r)?Color.rgb(88,157,225):"uncommon".equals(r)?GREEN:TEXT;}
    String rarityLabel(String r){if(r==null)return"paprastas";switch(r){case"unique":return"unikalus";case"ancient":return"senovinis";case"mythic":return"mitinis";case"legendary":return"legendinis";case"epic":return"epinis";case"rare":return"retas";case"uncommon":return"neįprastas";default:return"paprastas";}}
    String categoryLabel(String s){if(s==null)return"Artefaktas";if("ring".equals(s))return"Žiedas";if("relic".equals(s))return"Relikvija";for(String x:VaeloriaDb.EQUIPMENT_SLOTS)if(x.equals(s))return VaeloriaDb.slotLabel(x);return s;}
    String abilityType(String t){if(t==null)return"Gebėjimas";return t.replace("gebėjimas_virš_ribos","Gebėjimas virš ribos").replace("tobulinimas_virš_ribos","Tobulinimas virš ribos").replace("principas_virš_ribos","Principas virš ribos").replace("technika_virš_ribos","Technika virš ribos").replace('_',' ');}
    String eventLabel(String e){if(e==null)return"";switch(e){case"combat":return"KOVA";case"combat_victory":return"PERGALĖ";case"combat_escape":case"combat_end":return"ATSIJUNGTA NUO KOVOS";case"discovery":return"ATRADIMAS";case"social":return"BENDRAVIMAS";case"travel":return"KELIONĖ";case"reward":return"ATLYGIS";case"setback":return"NESĖKMĖ";default:return"";}}
    String safe(String s,String d){return s==null||s.isEmpty()?d:s;} String clock(){long day=state.worldMinute/1440,mn=state.worldMinute%1440;return "Diena "+day+" · "+String.format(Locale.ROOT,"%02d:%02d",mn/60,mn%60);} 
    String lore(String n){if("Luminara".equals(n))return"Septynių plaukiojančių maginių žiedų metropolis.";if(n.contains("Drakono Pabudimo"))return"Drakonų teritorija ir itin pavojingi kalnai.";if(n.contains("Kharad"))return"Kalvystės miestas ir nuliui atsparaus amato centras.";if(n.contains("Tuščiavidur"))return"Erdvinės ir nulinės anomalijos čia persidengia.";if(n.contains("Žaliasis")||n.contains("Šventųjų")||n.contains("Šaltinio"))return"Gyvybės, senųjų kelių ir nestabilių gamtos jėgų regionas.";return"Kanoninė Vaeloria vieta, susieta su gyvu kelionių tinklu.";}
    void delta(StringBuilder b,String n,long d){if(d!=0){if(b.length()>0)b.append("  ");b.append(d>0?"+":"").append(d).append(" ").append(n);}}
}
