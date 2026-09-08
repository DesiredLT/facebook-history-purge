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
    VaeloriaDb db; GameState state; FrameLayout content; LinearLayout nav,root; String screen="game",feedback=""; boolean busy=false,destroyed=false; StatEngine.Check pendingCheck; int pendingMasteryBonus=0,pendingPrimaryXp=0,pendingSecondaryXp=0; final ExecutorService pool=Executors.newSingleThreadExecutor();Future<?> activeRequest;long requestGeneration=0;
    AiHttpClient activeAiTransport, setupTransport;
    ProgressDialog setupDialog;
    JSONObject pendingResolvedTurn;
    long pendingCheckpointId=-1;
    final Set<View> pendingDisabled=Collections.newSetFromMap(new IdentityHashMap<>());

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        db=new VaeloriaDb(this);state=db.loadState();shell();show(state.characterCreated?"game":"character");
    }
    @Override public void onDestroy(){destroyed=true;requestGeneration++;if(activeAiTransport!=null)activeAiTransport.cancel();if(setupTransport!=null)setupTransport.cancel();if(setupDialog!=null)setupDialog.dismiss();if(activeRequest!=null)activeRequest.cancel(true);pool.shutdownNow();if(db!=null){if(pendingCheckpointId>=0)db.discardCheckpoint(pendingCheckpointId);db.close();}super.onDestroy();}

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
    void show(String id){if(!allowScreen(id))return;screen=id;nav();content.removeAllViews();try{content.addView("character".equals(id)?character():"game".equals(id)?game():"hero".equals(id)?hero():"map".equals(id)?map():"journal".equals(id)?journal():settings());}catch(Throwable e){content.addView(errorView(e));}updatePendingControls();}

    View character(){return hero();}

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
        LinearLayout top=strong();top.addView(label("VEIKĖJAS"));top.addView(t(state.characterName.toUpperCase(Locale.forLanguageTag("lt-LT")),26,TEXT,true));top.addView(t(CharacterCatalogV093.ageLine(state),11,MUT,false));top.addView(t(CharacterCatalogV093.originName(state.characterOriginId)+" · "+CharacterCatalogV093.archetypeName(state.characterArchetypeId),11,GOLD,true));top.addView(t("Bruožai: "+CharacterCatalogV093.traitNames(state.characterTraitIds),11,MUT,false));LinearLayout tags=row();boolean legendary="legendary".equals(state.progressionMode);tags.addView(chip(legendary?"LEGENDINĖ BAZĖ":"LYGIS "+state.level));Space gap=new Space(this);tags.addView(gap,new LinearLayout.LayoutParams(dp(7),1));tags.addView(chip(legendary?"92 / 92 · 100/100":"SUBALANSUOTA PROGRESIJA"));top.addView(tags,m(dp(4)));c.addView(top,m(dp(10)));

        Map<String,Integer> mastery=db.getMasteryLevels();int sum=0;for(String stat:mastery.keySet())sum+=mastery.get(stat);int avg=mastery.isEmpty()?70:Math.round(sum/(float)mastery.size());
        LinearLayout stats=card();stats.addView(label("PAŽANGA"));stats.addView(t((legendary?"Bazinės savybės pasiekė ribą":"Bazinės savybės auga su veikėjo lygiu")+" · vidutinis meistriškumas "+avg+"/100",15,TEXT,true));stats.addView(bar("VIDUTINIS MEISTRIŠKUMAS",avg,100,GOLD));stats.addView(t("Kiekviena naudojama savybė gauna atskirą patirtį. Meistriškumas realiai keičia patikros rezultatą, o subalansuotame režime bazė auga kartu su veikėjo progresija.",11,MUT,false));Button allStats=outline("PERŽIŪRĖTI VISAS 92 SAVYBES IR PATIRTĮ");allStats.setOnClickListener(v->startActivity(new Intent(this,StatsActivity.class)));stats.addView(allStats,m(dp(7)));c.addView(stats,m(dp(9)));

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
        String[] names=new String[usable.size()];for(int i=0;i<usable.size();i++){VaeloriaDb.Item it=usable.get(i);String requirement=db.equipRequirement(it,state.level);names[i]=it.name+" · "+rarityLabel(it.rarity)+(target.equals(it.equippedSlot)?" · UŽDĖTA":requirement.isEmpty()?"":" · UŽRAKINTA IKI L"+it.itemLevel);}
        AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle(VaeloriaDb.slotLabel(target)).setItems(names,(d,which)->{VaeloriaDb.Item it=usable.get(which);String requirement=db.equipRequirement(it,state.level);if(!requirement.isEmpty()){feedback=requirement;Toast.makeText(this,requirement,Toast.LENGTH_LONG).show();show("hero");return;}db.checkpoint("prieš įrangos pakeitimą",state);if(db.equipToSlot(it.id,target,state.level))feedback="⚙ Užsidėta: "+it.name;else feedback="Įrangos pakeisti nepavyko";state=db.loadState();show("hero");});
        if(current!=null)b.setNegativeButton("NUIMTI",(d,w)->{db.checkpoint("prieš įrangos pakeitimą",state);db.unequipSlot(target);feedback="⚙ Nuimta: "+current.name;state=db.loadState();show("hero");});b.setNeutralButton("Uždaryti",null).show();
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
        LinearLayout sav=card();sav.addView(label("IŠSAUGOJIMAS IR ATKŪRIMAS"));Button profile=outline("KEISTI VEIKĖJO PROFILĮ IR BRUOŽUS");profile.setOnClickListener(v->show("character"));sav.addView(profile,m(dp(5)));Button undo=outline("↶ ATŠAUKTI PASKUTINĮ ĖJIMĄ");undo.setOnClickListener(v->{if(db.undo()){state=db.loadState();feedback="↶ Atkurtas ankstesnis kontrolinis taškas";show("game");}else Toast.makeText(this,"Nėra ankstesnio kontrolinio taško",Toast.LENGTH_SHORT).show();});sav.addView(undo,m(dp(5)));Button ex=outline("KOPIJUOTI IŠSAUGOJIMĄ");ex.setOnClickListener(v->export());sav.addView(ex,m(dp(5)));Button im=outline("IMPORTUOTI IŠSAUGOJIMĄ");im.setOnClickListener(v->importSave());sav.addView(im,m(dp(5)));Button reset=outline("ATKURTI PRADINĘ BŪSENĄ");reset.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Atkurti pradinę būseną?").setMessage("Vietiniai ėjimai, veikėjo profilis ir įrangos pakeitimai bus panaikinti. API raktas liks telefone.").setNegativeButton("Ne",null).setPositiveButton("Atkurti",(d,w)->{db.reset();state=db.loadState();feedback="";show("character");}).show());sav.addView(reset);c.addView(sav,m(dp(9)));
        LinearLayout tech=card();tech.addView(label("VEIKIMO PRINCIPAS"));tech.addView(t("Telefonas → vietinė SQLite duomenų bazė → Groq tik naujai scenai",13,TEXT,true));tech.addView(t("Žaidimo būsena, įranga, žemėlapis ir ėjimų istorija lieka telefone. Be Groq rakto pagrindinės vietinės funkcijos ir atsarginis scenos sprendimas veikia toliau.",11,MUT,false));c.addView(tech,m(dp(8)));return sv;
    }

    void act(String rawAction){
        if(busy||destroyed)return;
        if(!state.characterCreated){show("character");return;}
        String action=rawAction==null?"":rawAction.trim();
        if(action.isEmpty())return;
        if("Tęsti kelionę".equals(action)){show("map");return;}
        if(java.util.Arrays.asList("Apžiūrėti grobį","Apžiūrėti gautą grobį","Patikrinti įrangą ir užrašus","Keisti pasiruošimą","Peržiūrėti kuprinę").contains(action)){show("items");return;}
        if(java.util.Arrays.asList("Grįžti prie užduoties","Grįžti prie pagrindinės užduoties","Grįžti prie Meridiano tyrimo","Tęsti pagrindinę užduotį","Tęsti tyrimą").contains(action)){show("journal");return;}
        if("Pranešti apie užduoties įvykdymą".equals(action)){
            feedback=db.sideQuests().claim(state.trackedQuestId,state).message;show("game");return;
        }
        if(action.length()>1200){Toast.makeText(this,"Sutrumpink veiksmą iki 1200 simbolių.",Toast.LENGTH_LONG).show();return;}
        try{
            GameState snapshot=GameState.fromJson(state.toJson());
            String equipped=db.equippedSummary();List<String[]> abilities=db.getAbilities();Map<String,Integer> stats=db.getStatValues();
            EquipmentRules.Stats gear=db.equipmentStats();String world=db.world().contextPrompt(snapshot);
            Set<String> talents=db.world().unlockedTalentIds();
            pendingCheck=StatEngine.resolve(snapshot,action,equipped,abilities,stats);
            int talentBonus=ProgressionEngine.checkBonus(talents,action,pendingCheck.primary);
            int companionBonus=db.world().companionCheckBonus(action);
            StatEngine.applyExternalModifier(pendingCheck,talentBonus,talentBonus==0?"":"talentų specializacija");
            StatEngine.applyExternalModifier(pendingCheck,companionBonus,companionBonus==0?"":"Lyros tyrimo pagalba");
            StatEngine.applyExternalModifier(pendingCheck,snapshot.temporaryCheckBonus,snapshot.temporaryEffectName);
            pendingMasteryBonus=MasteryEngine.checkBonus(pendingCheck.primary,pendingCheck.secondary,db.getMasteryLevels());
            MasteryEngine.apply(pendingCheck,pendingMasteryBonus);
            pendingPrimaryXp=MasteryEngine.primaryXp(pendingCheck);pendingSecondaryXp=MasteryEngine.secondaryXp(pendingCheck);
            boolean combat=CombatEngine.handles(snapshot,action);
            JSONObject localResult=combat?CombatEngine.resolve(snapshot,action,pendingCheck,gear,stats,talents,
                    db.world().companionDefenseBonus(snapshot.combatRound),db.world().companionVictoryHealing())
                    :LocalTurnResolver.resolve(snapshot,action,pendingCheck,db.world().discoveredLocations());
            pendingResolvedTurn=AiTurnPolicyV101.sanitize(localResult,snapshot,pendingCheck,action,db.world().discoveredLocations(),combat);
            db.world().validateAction(snapshot,action,pendingResolvedTurn);
            if(pendingResolvedTurn.optBoolean("blocked")){
                feedback=pendingResolvedTurn.optString("scene","Veiksmas nepradėtas.");clearPending();show("game");return;
            }
            // The model sees the actual journey duration, including the engine's map calculation.
            if(!snapshot.location.equals(pendingResolvedTurn.optString("location",snapshot.location))){
                int minutes=db.world().travelMinutes(snapshot.location,pendingResolvedTurn.getString("location"));
                if(minutes>0)pendingResolvedTurn.put("time_minutes",minutes);
            }
            applyEquipmentTravel(pendingResolvedTurn,pendingResolvedTurn.optString("event_tag"));
            final JSONObject resolved=new JSONObject(pendingResolvedTurn.toString());
            final String context=NarrativeTurn.context(snapshot,action,equipped,abilities,pendingCheck,world,resolved);
            final String provider=OpenAiSettings.provider(this),groqKey=SecureKeyStore.load(this);
            final OpenAiSettings config=OpenAiSettings.load(this);
            pendingCheckpointId=db.checkpoint("prieš veiksmą",state);
            busy=true;feedback="Ruošiamas pasakojimas…";show("game");
            long generation=++requestGeneration;
            if(OpenAiSettings.LOCAL.equals(provider)){finish(action,resolved,null);return;}
            final String policy=AiHttpClient.readBounded(getAssets().open("ai/narration-policy.txt"),16000);
            final JSONObject schema=new JSONObject(AiHttpClient.readBounded(getAssets().open("ai/narration-schema.json"),8000));
            final AiHttpClient transport=new AiHttpClient();activeAiTransport=transport;
            activeRequest=pool.submit(()->{
                JSONObject result=resolved;String warning=null;
                try{
                    JSONObject narration=OpenAiSettings.OPENAI.equals(provider)
                            ?requestOpenAi(transport,config,context)
                            :GroqClient.resolveNarration(transport,groqKey,context,policy,schema);
                    result=NarrativeTurn.merge(resolved,narration,snapshot);
                }catch(Exception error){warning=AiHttpClient.userMessage(error)+" Panaudotas vietinis pasakojimas.";}
                if(transport.isCancelled()||Thread.currentThread().isInterrupted())return;
                final JSONObject finished=result;final String message=warning;
                runOnUiThread(()->{if(!destroyed&&generation==requestGeneration)finish(action,finished,message);});
            });
        }catch(Exception error){
            cancelRequest();feedback="Veiksmo paruošti nepavyko. Pažanga nepasikeitė.";show("game");
        }
    }

    private static JSONObject requestOpenAi(AiHttpClient transport,OpenAiSettings config,String context)throws Exception{
        if(config.url.isEmpty()||config.token.isEmpty())throw new AiHttpClient.ServiceException(503);
        return transport.post(config.url+"/v1/turn",config.token,new JSONObject().put("context",context)).getJSONObject("narration");
    }

    void cancelPendingAction(){
        if(!busy)return;cancelRequest();feedback="Veiksmas atšauktas. Pažanga nepasikeitė.";show("game");
    }
    private void cancelRequest(){
        requestGeneration++;if(activeAiTransport!=null)activeAiTransport.cancel();if(activeRequest!=null)activeRequest.cancel(true);
        if(pendingCheckpointId>=0)db.discardCheckpoint(pendingCheckpointId);
        clearPending();
    }
    private void clearPending(){
        busy=false;pendingCheck=null;pendingResolvedTurn=null;pendingCheckpointId=-1;
        pendingMasteryBonus=0;pendingPrimaryXp=0;pendingSecondaryXp=0;activeRequest=null;activeAiTransport=null;
    }
    boolean allowScreen(String id){
        if(busy&&!"game".equals(id)){Toast.makeText(this,"Palauk pasakojimo arba atšauk veiksmą.",Toast.LENGTH_SHORT).show();return false;}
        return true;
    }
    void updatePendingControls(){
        for(View view:pendingDisabled)view.setEnabled(true);pendingDisabled.clear();
        if(busy)lockPendingControls(root);
    }
    private void lockPendingControls(View view){
        if(view.isEnabled()&&(view.hasOnClickListeners()||view instanceof EditText)&&!"cancel_narration".equals(view.getTag())){
            view.setEnabled(false);pendingDisabled.add(view);
        }
        if(view instanceof android.view.ViewGroup){android.view.ViewGroup group=(android.view.ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++)lockPendingControls(group.getChildAt(i));}
    }

    void finish(String action,JSONObject narration,String warn){
        if(pendingResolvedTurn==null||!busy)return;
        JSONObject result=pendingResolvedTurn;
        try{result=NarrativeTurn.merge(pendingResolvedTurn,narration,state);}catch(Exception ignored){}
        android.database.sqlite.SQLiteDatabase database=db.getWritableDatabase();boolean success=false;
        try{
            database.beginTransaction();
            try{finishResolved(action,result,warn);database.setTransactionSuccessful();}
            finally{database.endTransaction();}
            success=true;
        }catch(Exception error){feedback="Ėjimo išsaugoti nepavyko. Ankstesnė pažanga išliko.";}
        if(!success){state=db.loadState();if(pendingCheckpointId>=0)db.discardCheckpoint(pendingCheckpointId);}
        clearPending();show("game");
    }

    private void finishResolved(String action,JSONObject r,String warn){
        int hp=state.hp,ma=state.mana,st=state.stamina,ae=state.aeonic;long cr=state.crowns,oldMinute=state.worldMinute;String old=state.location;String event=r.optString("event_tag","none");boolean wasCombat=state.combatActive;String defeatedEnemy=state.enemyName;int defeatedDanger=state.enemyDanger;
        state.applyTurn(r);if(!old.equals(state.location)){int authoritativeTravel=db.world().travelMinutes(old,state.location);if(authoritativeTravel>0)state.worldMinute+=authoritativeTravel-Math.max(0,r.optInt("time_minutes",0));}CombatEngine.hydrate(state);if(pendingCheck!=null){db.awardMastery(pendingCheck.primary,pendingPrimaryXp);db.awardMastery(pendingCheck.secondary,pendingSecondaryXp);}ArrayList<String> gained=new ArrayList<>();boolean victory=CombatEngine.isVictory(wasCombat,r);if(victory&&!defeatedEnemy.isEmpty()){for(ItemCatalogV092.ItemDef drop:DropTableV092.roll(defeatedEnemy,state.worldMinute+action.hashCode())){db.addCatalogLoot(drop,1);gained.add(drop.name);}}else{JSONArray loot=r.optJSONArray("loot");if(loot!=null)for(int i=0;i<loot.length();i++){JSONObject o=loot.optJSONObject(i);if(o==null)continue;ItemCatalogV092.ItemDef item=exactCatalogItem(o.optString("name",""));if(item==null)continue;db.addCatalogLoot(item,1);gained.add(item.name);}}
        ProgressionEngine.Award progression=ProgressionEngine.award(state,event,pendingCheck,defeatedDanger);String questUpdate=db.world().progressStory(action,event,state,pendingCheck,oldMinute);String npcUpdate=db.world().recordNpcInteraction(action,event,state,oldMinute);String sideUpdate=db.sideQuests().record(action,event,state,pendingCheck);String companionUpdate=db.world().recordCompanionTurn(action,event,state);String discoveryUpdate=WorldRepository.checkAllowsProgress(pendingCheck,event)?db.world().recordExploration(action,state):"";String worldUpdate=db.world().advanceWorld(state,event,r.optInt("time_minutes",0));db.world().applyQuestToState(state);if("travel".equals(event)||!questUpdate.isEmpty()||!sideUpdate.isEmpty()||!state.trackedQuestId.isEmpty())db.world().applyStructuredChoices(state);String sceneMemory=state.scene==null?"":state.scene.replace('\n',' ').trim();if(sceneMemory.length()>150)sceneMemory=sceneMemory.substring(0,149)+"…";state.recentTurns.add(action+" → "+state.sceneTitle+" · "+sceneMemory+(pendingCheck==null?"":" · "+pendingCheck.primary+": "+pendingCheck.outcome));while(state.recentTurns.size()>30)state.recentTurns.remove(0);String effectName=state.temporaryEffectName;int effectTurns=state.temporaryEffectTurns;state.tickTemporaryEffect();String effectUpdate=effectTurns<=0?"":state.temporaryEffectTurns==0?effectName+" poveikis baigėsi":effectName+" · liko "+state.temporaryEffectTurns+" ėj.";db.saveState(state);
        StringBuilder f=new StringBuilder();if(pendingCheck!=null)f.append(pendingCheck.compact()).append(" · ").append(MasteryEngine.effectLine(pendingMasteryBonus,pendingPrimaryXp,pendingSecondaryXp));if(progression.gained>0){if(f.length()>0)f.append("  ");f.append("★ ").append(progression.line());}String ev=eventLabel(event);if(!ev.isEmpty()){if(f.length()>0)f.append("  ");f.append("◆ ").append(ev);}delta(f,"gyvybė",state.hp-hp);delta(f,"mana",state.mana-ma);delta(f,"ištvermė",state.stamina-st);delta(f,"eoninė energija",state.aeonic-ae);delta(f,"karūnos",state.crowns-cr);if(!old.equals(state.location)){if(f.length()>0)f.append("  ");f.append("📍 ").append(state.location);}if(!gained.isEmpty()){if(f.length()>0)f.append("  ");f.append("🎁 Gauta: ").append(String.join(", ",gained));}for(String update:new String[]{questUpdate,sideUpdate,npcUpdate,companionUpdate,discoveryUpdate,worldUpdate,effectUpdate})if(update!=null&&!update.isEmpty()){if(f.length()>0)f.append("  ");f.append("◆ ").append(update);}if(warn!=null){if(f.length()>0)f.append("  ");f.append(warn);}feedback=f.length()==0?"✓ Ėjimas išspręstas":f.toString();
    }

    private void applyEquipmentTravel(JSONObject result,String event){if(!"travel".equals(event))return;EquipmentRules.Stats gear=db.equipmentStats();int stamina=result.optInt("stamina_delta",0);if(stamina<0&&gear.travelFatigueHalf)stamina=Math.round(stamina/2f);stamina+=gear.travelRecovery;try{result.put("stamina_delta",stamina);}catch(Exception ignored){}}

    JSONObject local(String action,StatEngine.Check check){
        try{
            GameState snapshot=GameState.fromJson(state.toJson());
            return CombatEngine.handles(snapshot,action)?CombatEngine.resolve(snapshot,action,check,db.equipmentStats(),db.getStatValues(),db.world().unlockedTalentIds(),db.world().companionDefenseBonus(snapshot.combatRound),db.world().companionVictoryHealing())
                    :LocalTurnResolver.resolve(snapshot,action,check,db.world().discoveredLocations());
        }catch(Exception error){return new JSONObject();}
    }

    boolean applyCharacterProfile(String name,int age,boolean ageless,String identity,String appearance,
                                  String originId,String archetypeId,List<String> traitIds){
        return applyCharacterProfile(name,age,ageless,identity,appearance,originId,archetypeId,traitIds,state==null?"balanced":state.progressionMode);
    }

    boolean applyCharacterProfile(String name,int age,boolean ageless,String identity,String appearance,
                                  String originId,String archetypeId,List<String> traitIds,String progressionMode){
        String cleanName=name==null?"":name.trim().replaceAll("\\s+"," ");
        ArrayList<String> traits=CharacterCatalogV093.normalizedTraits(traitIds);
        if(!cleanName.matches("[\\p{L}\\p{M}'’ -]{2,32}")||age<16||age>999
                ||!CharacterCatalogV093.validProfile(cleanName,identity,originId,archetypeId,traits))return false;
        String cleanAppearance=appearance==null?"":appearance.trim().replaceAll("\\s+"," ");
        if(cleanAppearance.length()>140)cleanAppearance=cleanAppearance.substring(0,140).trim();
        boolean first=!state.characterCreated;
        state.characterName=cleanName;
        state.chronologicalAge=age;
        state.ageless=ageless;
        state.biologicalAge=ageless?Math.min(age,25):age;
        state.characterIdentity=identity;
        state.characterAppearance=cleanAppearance;
        state.characterOriginId=originId;
        state.characterArchetypeId=archetypeId;
        state.characterTraitIds.clear();state.characterTraitIds.addAll(traits);
        state.characterCreated=true;
        if(first){
            db.initializeCharacterProgression(state,progressionMode,originId,archetypeId);
            state.sceneTitle="Kelionės pradžia";
            state.scene="Luminara tave pasitinka kelionės vartų gaudesiu ir neramiomis žiniomis. Tavo kilmė, archetipas ir pasirinkti bruožai nuo šiol keis kiekvieną svarbią savybės patikrą. Karavano manifesto laiko neatitikimas tampa pirmuoju tikru išbandymu.";
            state.choices.clear();
            state.choices.add("Ištirti karavano manifestą");
            state.choices.add("Susipažinti su Luminara gyventojais");
            state.choices.add("Patikrinti įrangą ir pasiruošti kelionei");
        }
        db.world().applyQuestToState(state);db.world().applyStructuredChoices(state);
        db.saveState(state);
        feedback="Veikėjo profilis išsaugotas · "+CharacterCatalogV093.archetypeName(archetypeId)+" · "+CharacterCatalogV093.traitNames(traits);
        return true;
    }

    View aiSettingsPanel(){
        LinearLayout panel=card();panel.addView(label("DI ŽAIDIMO MEISTRAS"));
        panel.addView(t(OpenAiSettings.label(this),13,TEXT,true));
        panel.addView(t("DI kuria lietuvišką pasakojimą pagal žaidime apskaičiuotą rezultatą. Siunčiamas veiksmas, veikėjo aprašas ir trumpa pasaulio bei paskutinių ėjimų santrauka.",11,MUT,false));
        Button openai=accent("PRIJUNGTI OPENAI");openai.setMinHeight(dp(48));openai.setOnClickListener(v->openAiDialog());panel.addView(openai,m(dp(5)));
        if(!OpenAiSettings.load(this).url.isEmpty()){
            Button use=outline("NAUDOTI OPENAI");use.setOnClickListener(v->{OpenAiSettings.select(this,OpenAiSettings.OPENAI);show("settings");});panel.addView(use,m(dp(5)));
            Button test=outline("PATIKRINTI OPENAI RYŠĮ");test.setMinHeight(dp(48));test.setOnClickListener(v->testOpenAi());panel.addView(test,m(dp(5)));
            panel.addView(t("Ryšio patikra sugeneruoja vieną bandomąjį atsakymą. Jai taikoma paslaugos API kaina; žaidimo pažanga nesikeičia.",10,MUT,false));
            Button forget=outline("PAŠALINTI OPENAI PRISIJUNGIMĄ");forget.setOnClickListener(v->{SecureKeyStore.clearSecret(this,OpenAiSettings.SECRET);OpenAiSettings.select(this,OpenAiSettings.LOCAL);show("settings");});panel.addView(forget,m(dp(5)));
        }
        Button local=outline("ŽAISTI BE DI");local.setMinHeight(dp(48));local.setOnClickListener(v->{OpenAiSettings.select(this,OpenAiSettings.LOCAL);show("settings");});panel.addView(local,m(dp(5)));
        Button groq=outline("GROQ NUSTATYMAI");groq.setOnClickListener(v->key());panel.addView(groq,m(dp(5)));
        if(!SecureKeyStore.load(this).isEmpty()){
            Button use=outline("NAUDOTI IŠSAUGOTĄ GROQ RAKTĄ");use.setOnClickListener(v->{OpenAiSettings.select(this,OpenAiSettings.GROQ);show("settings");});panel.addView(use,m(dp(5)));
            Button forget=outline("PAŠALINTI GROQ RAKTĄ");forget.setOnClickListener(v->{SecureKeyStore.clear(this);if(OpenAiSettings.GROQ.equals(OpenAiSettings.provider(this)))OpenAiSettings.select(this,OpenAiSettings.LOCAL);show("settings");});panel.addView(forget);
        }
        return panel;
    }

    void openAiDialog(){
        OpenAiSettings previous=OpenAiSettings.load(this);
        LinearLayout form=col();form.setPadding(dp(18),dp(8),dp(18),dp(4));
        TextView urlLabel=t("DI paslaugos adresas",12,TEXT,true);form.addView(urlLabel);
        EditText url=new EditText(this);url.setSingleLine(true);url.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
        url.setHint("https://tavo-paslauga.lt");url.setText(previous.url);form.addView(url);
        form.addView(t("Prisijungimo kodas",12,TEXT,true));
        EditText token=new EditText(this);token.setSingleLine(true);token.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        token.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);token.setHint(previous.token.isEmpty()?"Paslaugos prisijungimo kodas":"Palik tuščią, jei kodas nesikeičia");form.addView(token);
        TextView error=t("",11,RED,false);form.addView(error);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("OpenAI žaidimo meistras")
                .setMessage("Įvesk žaidimo DI paslaugos adresą ir jai skirtą prisijungimo kodą. Juos gausi iš paslaugos administratoriaus.")
                .setView(form).setNegativeButton("ATŠAUKTI",null).setPositiveButton("IŠSAUGOTI",null).create();
        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            try{
                String secret=token.getText().toString().trim();
                if(secret.isEmpty()&&url.getText().toString().trim().equals(previous.url))secret=previous.token;
                OpenAiSettings.save(this,url.getText().toString(),secret);dialog.dismiss();show("settings");
            }catch(Exception failure){error.setText(failure instanceof IllegalArgumentException?failure.getMessage():"Prisijungimo išsaugoti nepavyko.");}
        }));dialog.show();
    }

    void testOpenAi(){
        final OpenAiSettings config=OpenAiSettings.load(this);
        final AiHttpClient transport=new AiHttpClient();setupTransport=transport;
        ProgressDialog dialog=ProgressDialog.show(this,"OpenAI ryšio patikra","Laukiama bandomojo pasakojimo…",true,true);setupDialog=dialog;
        dialog.setOnCancelListener(d->transport.cancel());
        pool.submit(()->{
            String message;
            try{
                JSONObject result=requestOpenAi(transport,config,"RYŠIO PATIKRA. Patvirtintas rezultatas: veikėjas Luminara mieste apžiūri vartus; kova nevyksta, atlygio nėra, pažanga nesikeičia. Aprašyk šią sceną ir pasiūlyk tris veiksmus.");
                NarrativeTurn.validate(result);message="OpenAI ryšys veikia.\n\n"+result.getString("scene");
            }catch(Exception failure){message=AiHttpClient.userMessage(failure);}
            final String text=message;
            runOnUiThread(()->{if(destroyed||transport.isCancelled())return;dialog.dismiss();setupTransport=null;setupDialog=null;
                new AlertDialog.Builder(this).setTitle("Ryšio patikros rezultatas").setMessage(text).setPositiveButton("GERAI",null).show();});
        });
    }

    void key(){EditText i=new EditText(this);i.setHint("gsk_…");i.setSingleLine(true);i.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);new AlertDialog.Builder(this).setTitle("Groq API raktas").setMessage("Raktas šifruojamas Android raktų saugykloje ir lieka šiame telefone.").setView(i).setNegativeButton("Atšaukti",null).setPositiveButton("Išsaugoti",(d,w)->{try{String k=i.getText().toString().trim();if(!k.startsWith("gsk_")||k.length()<20)throw new Exception();SecureKeyStore.save(this,k);OpenAiSettings.select(this,OpenAiSettings.GROQ);show("settings");}catch(Exception e){Toast.makeText(this,"Nepavyko išsaugoti rakto",Toast.LENGTH_SHORT).show();}}).show();}
    void export(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").putExtra(Intent.EXTRA_TITLE,"vaeloria-save.json");startActivityForResult(i,EXPORT_REQ);}
    void importSave(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,IMPORT_REQ);}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;try{android.net.Uri u=data.getData();if(req==EXPORT_REQ){try(OutputStream os=getContentResolver().openOutputStream(u)){os.write(db.exportSave().getBytes(StandardCharsets.UTF_8));}Toast.makeText(this,"Išsaugojimas eksportuotas",Toast.LENGTH_SHORT).show();}else if(req==IMPORT_REQ){if(busy)cancelRequest();String raw=AiHttpClient.readBounded(getContentResolver().openInputStream(u),5_000_000);if(db.importSave(raw)){state=db.loadState();feedback="Išsaugojimas importuotas";show(state.characterCreated?"game":"character");}else Toast.makeText(this,"Netinkamas Vaeloria išsaugojimas",Toast.LENGTH_SHORT).show();}}catch(Exception e){Toast.makeText(this,"Failo klaida: "+e.getMessage(),Toast.LENGTH_LONG).show();}}

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
    ItemCatalogV092.ItemDef exactCatalogItem(String name){if(name==null)return null;String clean=name.trim();for(ItemCatalogV092.ItemDef item:ItemCatalogV092.ALL)if(item.name.equalsIgnoreCase(clean))return item;return null;}
    String safe(String s,String d){return s==null||s.isEmpty()?d:s;} String clock(){long day=state.worldMinute/1440,mn=state.worldMinute%1440;return "Diena "+day+" · "+String.format(Locale.ROOT,"%02d:%02d",mn/60,mn%60);} 
    String lore(String n){if("Luminara".equals(n))return"Septynių plaukiojančių maginių žiedų metropolis.";if(n.contains("Drakono Pabudimo"))return"Drakonų teritorija ir itin pavojingi kalnai.";if(n.contains("Kharad"))return"Kalvystės miestas ir nuliui atsparaus amato centras.";if(n.contains("Tuščiavidur"))return"Erdvinės ir nulinės anomalijos čia persidengia.";if(n.contains("Žaliasis")||n.contains("Šventųjų")||n.contains("Šaltinio"))return"Gyvybės, senųjų kelių ir nestabilių gamtos jėgų regionas.";return"Kanoninė Vaeloria vieta, susieta su gyvu kelionių tinklu.";}
    void delta(StringBuilder b,String n,long d){if(d!=0){if(b.length()>0)b.append("  ");b.append(d>0?"+":"").append(d).append(" ").append(n);}}
}
