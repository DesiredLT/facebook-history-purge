package lt.vaeloria.ooc;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

/** Normalizuota gyvo pasaulio saugykla: užduotys, NPC, ekonomika, politika ir paslaugos. */
final class WorldRepository {
    static final String[] TABLES = {
            "quests", "quest_steps", "quest_evidence", "npcs", "shops", "shop_stock",
            "factions", "faction_relations", "settlements", "world_events", "economy", "recipes",
            "businesses", "hired_npcs", "companions", "talents", "locations"
    };

    static final class Quest {
        String id, title, type, status, outcome;
        int stage;
    }

    static final class QuestStep {
        String id, questId, title, status, branchKey;
        int position, progress, target;
    }

    static final class Npc {
        String id, name, role, location, service, lastTopic, memory;
        int relationship, trust, interactions;
        boolean available;
    }

    static final class Shop {
        String id, name, npcId, location;
        int markup, buyback;
    }

    static final class Stock {
        String shopId, catalogId, name, category, rarity;
        int quantity, price, level;
    }

    static final class Event {
        long id;
        String title, detail, region, type;
        int severity, priceModifier;
        long expiresMinute;
        boolean active;
    }

    static final class Faction {
        String id, name, relation;
        int influence, treasury, territory, tension;
    }

    static final class Settlement {
        String id, name, region, faction, rivalId;
        int prosperity, security, autonomy;
    }

    static final class Business {
        String id, name, type, location;
        int level, revenue, upkeep, price;
        long nextPayout;
        boolean owned;
    }

    static final class Recipe {
        String id, name, resultCatalogId, ingredientCatalogId, ingredientName;
        int ingredientQty, fee;
        boolean unlocked;
    }

    static final class Companion {
        String id, npcId, name, role, perk;
        int loyalty;
        boolean recruited, active;
    }

    static final class TalentState {
        ProgressionEngine.TalentDef definition;
        boolean unlocked;
        long unlockedMinute;
    }

    static final class LocationInfo {
        String id,name,region;
        int danger,x,y;
        boolean discovered,visited;
        long lastVisit;
    }

    static final class Hire {
        String npcId, name, job;
        int wage, loyalty;
        long nextPay;
        boolean active;
    }

    static final class TransactionResult {
        final boolean ok;
        final String message;
        TransactionResult(boolean ok,String message){this.ok=ok;this.message=message;}
    }

    private final VaeloriaDb owner;
    WorldRepository(VaeloriaDb owner){this.owner=owner;}

    static void create(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS quests (id TEXT PRIMARY KEY, title TEXT NOT NULL, type TEXT NOT NULL, status TEXT NOT NULL, stage INTEGER NOT NULL DEFAULT 0, outcome TEXT NOT NULL DEFAULT '', updated_at INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE IF NOT EXISTS quest_steps (id TEXT PRIMARY KEY, quest_id TEXT NOT NULL, position INTEGER NOT NULL, title TEXT NOT NULL, status TEXT NOT NULL, progress INTEGER NOT NULL DEFAULT 0, target INTEGER NOT NULL DEFAULT 1, branch_key TEXT NOT NULL DEFAULT '')");
        db.execSQL("CREATE TABLE IF NOT EXISTS quest_evidence (quest_id TEXT NOT NULL, evidence_id TEXT NOT NULL, detail TEXT NOT NULL, source TEXT NOT NULL, created_minute INTEGER NOT NULL, PRIMARY KEY(quest_id,evidence_id))");
        db.execSQL("CREATE TABLE IF NOT EXISTS npcs (id TEXT PRIMARY KEY, name TEXT UNIQUE NOT NULL, role TEXT NOT NULL, location TEXT NOT NULL, service TEXT NOT NULL DEFAULT '', relationship INTEGER NOT NULL DEFAULT 0, trust INTEGER NOT NULL DEFAULT 10, available INTEGER NOT NULL DEFAULT 1, last_topic TEXT NOT NULL DEFAULT '', interactions INTEGER NOT NULL DEFAULT 0, memory_json TEXT NOT NULL DEFAULT '[]')");
        db.execSQL("CREATE TABLE IF NOT EXISTS shops (id TEXT PRIMARY KEY, name TEXT NOT NULL, npc_id TEXT NOT NULL, location TEXT NOT NULL, markup INTEGER NOT NULL DEFAULT 11000, buyback INTEGER NOT NULL DEFAULT 4500, stock_day INTEGER NOT NULL DEFAULT -1)");
        db.execSQL("CREATE TABLE IF NOT EXISTS shop_stock (shop_id TEXT NOT NULL, catalog_id TEXT NOT NULL, quantity INTEGER NOT NULL DEFAULT 1, base_price INTEGER NOT NULL, level INTEGER NOT NULL, PRIMARY KEY(shop_id,catalog_id))");
        db.execSQL("CREATE TABLE IF NOT EXISTS factions (id TEXT PRIMARY KEY, name TEXT NOT NULL, influence INTEGER NOT NULL, relation TEXT NOT NULL, treasury INTEGER NOT NULL, territory INTEGER NOT NULL, tension INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS faction_relations (faction_a TEXT NOT NULL, faction_b TEXT NOT NULL, standing INTEGER NOT NULL, tension INTEGER NOT NULL, treaty TEXT NOT NULL DEFAULT '', PRIMARY KEY(faction_a,faction_b))");
        db.execSQL("CREATE TABLE IF NOT EXISTS settlements (id TEXT PRIMARY KEY, name TEXT NOT NULL, region TEXT NOT NULL, faction TEXT NOT NULL, prosperity INTEGER NOT NULL, security INTEGER NOT NULL, autonomy INTEGER NOT NULL, rival_id TEXT NOT NULL DEFAULT '')");
        db.execSQL("CREATE TABLE IF NOT EXISTS world_events (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, detail TEXT NOT NULL, region TEXT NOT NULL, type TEXT NOT NULL, severity INTEGER NOT NULL, active INTEGER NOT NULL DEFAULT 1, price_modifier INTEGER NOT NULL DEFAULT 0, created_minute INTEGER NOT NULL, expires_minute INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS economy (id TEXT PRIMARY KEY, label TEXT NOT NULL, region TEXT NOT NULL, supply INTEGER NOT NULL, demand INTEGER NOT NULL, price_index INTEGER NOT NULL, updated_minute INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS recipes (id TEXT PRIMARY KEY, name TEXT NOT NULL, result_catalog_id TEXT NOT NULL, ingredient_catalog_id TEXT NOT NULL, ingredient_qty INTEGER NOT NULL, fee INTEGER NOT NULL, unlocked INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE IF NOT EXISTS businesses (id TEXT PRIMARY KEY, name TEXT NOT NULL, type TEXT NOT NULL, location TEXT NOT NULL, level INTEGER NOT NULL DEFAULT 1, revenue INTEGER NOT NULL, upkeep INTEGER NOT NULL, price INTEGER NOT NULL, next_payout INTEGER NOT NULL DEFAULT 0, owned INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE IF NOT EXISTS hired_npcs (npc_id TEXT PRIMARY KEY, job TEXT NOT NULL, wage INTEGER NOT NULL, loyalty INTEGER NOT NULL DEFAULT 25, active INTEGER NOT NULL DEFAULT 1, hired_minute INTEGER NOT NULL, next_pay INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE IF NOT EXISTS companions (id TEXT PRIMARY KEY, npc_id TEXT NOT NULL, name TEXT NOT NULL, role TEXT NOT NULL, perk TEXT NOT NULL, loyalty INTEGER NOT NULL DEFAULT 20, recruited INTEGER NOT NULL DEFAULT 0, active INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE IF NOT EXISTS talents (id TEXT PRIMARY KEY, unlocked INTEGER NOT NULL DEFAULT 0, unlocked_minute INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE IF NOT EXISTS locations (id TEXT PRIMARY KEY, name TEXT UNIQUE NOT NULL, region TEXT NOT NULL, danger INTEGER NOT NULL, x INTEGER NOT NULL, y INTEGER NOT NULL, discovered INTEGER NOT NULL DEFAULT 0, visited INTEGER NOT NULL DEFAULT 0, last_visit INTEGER NOT NULL DEFAULT 0)");
    }

    static void seed(SQLiteDatabase db, GameState state) {
        seedQuest(db,"Q-MERIDIAN","Lūžęs Meridianas","main","active");
        seedStep(db,"QM-01","Q-MERIDIAN",0,"Ištirti karavano manifestą","active",1);
        seedStep(db,"QM-02","Q-MERIDIAN",1,"Patikrinti Meridiano vartų žurnalą ir liudijimus","locked",1);
        seedStep(db,"QM-03","Q-MERIDIAN",2,"Užsitikrinti tris nepriklausomus atramos taškus","locked",3);
        seedStep(db,"QM-04","Q-MERIDIAN",3,"Pereiti Meridianą ir grįžti su Orisono duomenimis","locked",1);
        seedStep(db,"QM-05","Q-MERIDIAN",4,"Apsispręsti dėl Pirmosios Meridiano chartijos","locked",1);
        seedStep(db,"QM-06","Q-MERIDIAN",5,"Priimti savo sprendimo pasekmes","locked",1);
        seedSideQuest(db,"Q-SMITH","Kalvės skolų grandinė","Brynja prašo išspręsti rūdos tiekimo sabotažą.");
        seedSideQuest(db,"Q-HEALER","Rezonanso nudegimai","Elen reikia trijų patikrintų reagentų naujam priešnuodžiui.");
        seedSideQuest(db,"Q-GUILD","Gildijų tylusis karas","Korva ieško neutralaus tarpininko tarp trijų cechų.");
        seedSideQuest(db,"Q-ROADS","Dingęs šiaurinis karavanas","Darveno maršrute be pėdsako pradingo prekybininkai.");
        seedSideQuest(db,"Q-ARCHIVE","Ištrinta chartija","Emilis rado sąmoningai pašalintą Meridiano įrašo dalį.");

        seedNpc(db,"npc-lyra","Lyra Fen","Meridiano lauko tyrėja","Luminara","research");
        seedNpc(db,"npc-kaelis","Kapitonas Kaelis","Luminara sargybos vadas","Luminara","guard");
        seedNpc(db,"npc-seraphine","Regentė Seraphine","Luminara valdovė","Luminara","politics");
        seedNpc(db,"npc-orinas","Archyvaras Orinas","Meridiano chartijų saugotojas","Luminara","archive");
        seedNpc(db,"npc-varekas","Varekas","Dravenn pasiuntinys","Luminara","politics");
        seedNpc(db,"npc-mirel","Mirel","Gydytoja ir runų meistrė","Luminara","healer");
        seedNpc(db,"npc-brynja","Brynja Geležrankė","Meistrė kalvė","Luminara","shop:smith");
        seedNpc(db,"npc-tarenas","Tarenas Volas","Šarvų meistras","Luminara","shop:armor");
        seedNpc(db,"npc-ysra","Ysra Vey","Runų amatininkė","Luminara","shop:runes");
        seedNpc(db,"npc-elen","Elen Var","Gydytoja ir vaistininkė","Luminara","shop:pharmacy");
        seedNpc(db,"npc-orenas","Orenas Pelas","Bendrasis prekybininkas","Luminara","shop:general");
        seedNpc(db,"npc-mara","Mara Žibintė","Užeigos šeimininkė","Luminara","shop:inn");
        seedNpc(db,"npc-ilyne","Sargė Ilyne","Miesto teisės pareigūnė","Luminara","guard");
        seedNpc(db,"npc-darvenas","Darvenas Kelio","Keliautojas","Luminara","traveler");
        seedNpc(db,"npc-bramas","Bramas Akmenskeltis","Statytojas ir mūrininkas","Luminara","builder");
        seedNpc(db,"npc-nesta","Nesta Vale","Arklidžių prižiūrėtoja","Luminara","shop:stable");
        seedNpc(db,"npc-emilis","Emilis Rhyse","Raštininkas ir tyrėjas","Luminara","archive");
        seedNpc(db,"npc-korva","Korva Dain","Gildijos tarpininkė","Luminara","guild");

        seedShop(db,"smith","Brynjos kalvė","npc-brynja","Luminara",11200,5200,new String[]{"weapon","tool"});
        seedShop(db,"armor","Tareno šarvų dirbtuvė","npc-tarenas","Luminara",11400,5000,new String[]{"head","chest","hands","legs","feet","belt","offhand"});
        seedShop(db,"runes","Ysros runų namai","npc-ysra","Luminara",11800,4800,new String[]{"ring","neck","relic","material"});
        seedShop(db,"pharmacy","Elen vaistinė","npc-elen","Luminara",11000,4200,new String[]{"potion","combat_consumable"});
        seedShop(db,"general","Oreno prekyvietė","npc-orenas","Luminara",10800,4600,new String[]{"food","material","utility","tool"});
        seedShop(db,"inn","Žibinto poilsio užeiga","npc-mara","Luminara",10500,3000,new String[]{"food"});
        seedShop(db,"stable","Nestos arklidės","npc-nesta","Luminara",11000,3500,new String[]{"utility"});

        seedFaction(db,"asterra","Asterra",state==null?34:state.asterraInfluence,"NEUTRALI",850000,42,28);
        seedFaction(db,"dravenn","Dravenn",state==null?72:state.dravennInfluence,"ĮTAMPA",740000,35,66);
        seedFaction(db,"lysara","Lysara",state==null?24:state.lysaraInfluence,"SĄJUNGINĖ",610000,23,19);
        relation(db,"asterra","dravenn",-28,66,"");relation(db,"asterra","lysara",52,19,"Santarvės prekybos sutartis");relation(db,"dravenn","lysara",-12,45,"");
        settlement(db,"luminara","Luminara","Luminara","asterra",78,72,55,"veyrhold");settlement(db,"veyrhold","Veyrhold","Luminara","asterra",61,68,66,"luminara");settlement(db,"kharad","Kharad Vorn","GELEŽINĖS VIRŠŪNĖS","dravenn",70,81,72,"sapphire");settlement(db,"sapphire","Safyro Platybės","AUDRŲ ŠIAURĖ","dravenn",57,64,77,"kharad");settlement(db,"swamp","Šventųjų Pelkynas","KRAUJŠAKNĖS GIRIA","lysara",49,45,84,"maze");settlement(db,"maze","Žaliasis Labirintas","KRAUJŠAKNĖS GIRIA","lysara",55,52,91,"swamp");settlement(db,"necro","Pelenų Nekropolis","PELENŲ NEKROPOLIS","dravenn",24,31,63,"luminara");settlement(db,"coast","Bedugnės Pakrantė","BEDUGNĖS PAKRANTĖ","lysara",46,39,88,"sapphire");
        economy(db,"eco-luminara","Luminara turgus","Luminara",58,64,102);
        economy(db,"eco-iron","Kalnų metalai","GELEŽINĖS VIRŠŪNĖS",63,55,96);
        economy(db,"eco-root","Girios reagentai","KRAUJŠAKNĖS GIRIA",45,67,112);
        economy(db,"eco-meridian","Meridiano relikvijos","MERIDIANO TUŠTUMA",23,81,138);
        economy(db,"eco-north","Šiaurės reikmenys","AUDRŲ ŠIAURĖ",40,58,109);
        economy(db,"eco-necro","Nekropolio radiniai","PELENŲ NEKROPOLIS",18,49,131);
        economy(db,"eco-coast","Pakrantės prekės","BEDUGNĖS PAKRANTĖ",54,52,99);

        seedRecipe(db,"recipe-heal","Stipresnis gydymo potionas","I092-202","I092-276",2,90);
        seedRecipe(db,"recipe-mana","Manos potionas","I092-203","I092-278",2,100);
        seedRecipe(db,"recipe-fire","Liepsnos bomba","I092-227","I092-284",2,125);
        seedRecipe(db,"recipe-upgrade","Runų dviašmenis","I092-005","I092-277",3,280);
        seedLockedRecipe(db,"recipe-master-mythic","Liepsnos kardas","I092-022","I092-284",8,5200);
        seedLockedRecipe(db,"recipe-master-ancient","Šešėlių kardas","I092-024","I092-289",10,12500);
        seedLockedRecipe(db,"recipe-master-unique","Dangaus spindulio kardas","I092-025","I092-290",14,26000);
        business(db,"biz-forge","Mažoji kalvė","craft","Luminara",420,120,8500);
        business(db,"biz-caravan","Karavano dalis","trade","Luminara",760,260,14500);
        business(db,"biz-inn","Užeigos kambarys","hospitality","Luminara",330,95,6200);
        companion(db,"comp-lyra","npc-lyra","Lyra Fen","Tyrėja","+4 tyrimo ir Meridiano patikroms");
        companion(db,"comp-kaelis","npc-kaelis","Kapitonas Kaelis","+6 gynybai pirmame kovos ėjime");
        companion(db,"comp-mirel","npc-mirel","Mirel","Gydytoja","Po kovos atkuria 8 gyvybes");
        for(ProgressionEngine.TalentDef talent:ProgressionEngine.TALENTS)seedTalent(db,talent.id);
        seedLocation(db,"luminara","Luminara","Luminara",3,400,360,true);
        seedLocation(db,"asterio","Asterio Karūna","Luminara",4,290,170,true);
        seedLocation(db,"stiklo","Stiklo Giria","KRAUJŠAKNĖS GIRIA",5,220,570,false);
        seedLocation(db,"veyrhold","Veyrhold","Luminara",3,170,340,true);
        seedLocation(db,"aurelionas","Aureliono Pakraštys","MERIDIANO TUŠTUMA",4,620,240,false);
        seedLocation(db,"zvaigzdekrita","Žvaigždėkritos Skliautas","MERIDIANO TUŠTUMA",7,730,520,false);
        seedLocation(db,"tusciavidure","Tuščiavidurė Smailė","MERIDIANO TUŠTUMA",8,650,660,false);
        seedLocation(db,"pelenu","Pelenų Karūnos Citadelė","PELENŲ NEKROPOLIS",7,780,310,false);
        seedLocation(db,"kharad","Kharad Vorn","GELEŽINĖS VIRŠŪNĖS",5,140,390,false);
        seedLocation(db,"drakono","Drakono Pabudimo Viršūnės","AUDRŲ ŠIAURĖ",8,670,100,false);
        seedLocation(db,"safyro","Safyro Platybės","AUDRŲ ŠIAURĖ",7,570,460,false);
        seedLocation(db,"saltinio","Amžinojo Šaltinio Slėnis","KRAUJŠAKNĖS GIRIA",4,200,680,false);
        seedLocation(db,"labirintas","Žaliasis Labirintas","KRAUJŠAKNĖS GIRIA",8,730,720,false);
        seedLocation(db,"pelkynas","Šventųjų Pelkynas","KRAUJŠAKNĖS GIRIA",6,460,840,false);
    }

    List<Quest> quests() {
        ArrayList<Quest> result=new ArrayList<>();try(Cursor c=db().rawQuery("SELECT id,title,type,status,stage,outcome FROM quests ORDER BY CASE type WHEN 'main' THEN 0 ELSE 1 END,title",null)){while(c.moveToNext()){Quest q=new Quest();q.id=c.getString(0);q.title=c.getString(1);q.type=c.getString(2);q.status=c.getString(3);q.stage=c.getInt(4);q.outcome=c.getString(5);result.add(q);}}return result;
    }

    List<QuestStep> steps(String questId) {
        ArrayList<QuestStep> result=new ArrayList<>();try(Cursor c=db().rawQuery("SELECT id,quest_id,position,title,status,progress,target,branch_key FROM quest_steps WHERE quest_id=? ORDER BY position",new String[]{questId})){while(c.moveToNext()){QuestStep s=new QuestStep();s.id=c.getString(0);s.questId=c.getString(1);s.position=c.getInt(2);s.title=c.getString(3);s.status=c.getString(4);s.progress=c.getInt(5);s.target=c.getInt(6);s.branchKey=c.getString(7);result.add(s);}}return result;
    }

    Quest activeMainQuest(){for(Quest quest:quests())if("main".equals(quest.type)&&!"completed".equals(quest.status))return quest;for(Quest quest:quests())if("main".equals(quest.type))return quest;return null;}

    QuestStep activeMainStep(){for(QuestStep step:steps("Q-MERIDIAN"))if("active".equals(step.status))return step;return null;}

    void applyQuestToState(GameState state){Quest quest=activeMainQuest();QuestStep step=activeMainStep();if(quest!=null)state.questTitle=quest.title;if(step!=null)state.objective=step.title+(step.target>1?" ("+step.progress+"/"+step.target+")":"");}

    void applyStructuredChoices(GameState state){QuestStep step=activeMainStep();if(step==null||state.combatActive)return;ArrayList<String> choices=new ArrayList<>();if(step.position==0){choices.add("Ištirti karavano manifestą ir jo laiko žymas");choices.add("Klausti Kaelio apie karavano atvykimą");choices.add("Palyginti manifestą su archyvo įrašais");}else if(step.position==1){choices.add("Patikrinti Meridiano vartų žurnalą");choices.add("Paprašyti Lyros nepriklausomo matavimo");choices.add("Klausti Orino apie ištrintas chartijas");}else if(step.position==2){choices.add("Užfiksuoti Lyros lauko matavimus kaip atramos tašką");choices.add("Patvirtinti Kaelio sargybos žurnalą kaip atramos tašką");choices.add("Vykti į Veyrhold ir tikrinti tranzito įrašą");}else if(step.position==3){choices.add("Pereiti Meridianą ir užmegzti Orisono kontaktą");choices.add("Kalibruoti saugų grįžimo kelią");choices.add("Paprašyti kompaniono saugoti atramos tašką");}else if(step.position==4){choices.add("Priimti Pirmąją Meridiano chartiją");choices.add("Atmesti Pirmąją Meridiano chartiją");choices.add("Siūlyti nepriklausomą Meridiano valdymą");}else if(step.position==5){choices.add("Priimti savo sprendimo pasekmes ir tęsti");choices.add("Aplankyti frakcijų atstovus po sprendimo");choices.add("Patikrinti, kaip pasikeitė pasaulio ekonomika");}if(choices.size()==3){state.choices.clear();state.choices.addAll(choices);}}

    String progressStory(String action,String event,GameState state){
        QuestStep step=activeMainStep();if(step==null)return"";String q=norm(action);boolean success=event==null||(!event.contains("setback")&&!event.contains("failure"));if(!success)return"";String evidence=null,detail="";
        if(step.position==0&&q.contains("manifest")){completeAndActivate(step,1);detail="Manifesto laiko neatitikimas įtrauktas į įrodymų grandinę.";}
        else if(step.position==1&&(q.contains("vart")||q.contains("meridian")||q.contains("lyra")||q.contains("kaeli")||q.contains("orin"))){completeAndActivate(step,2);detail="Vartų žurnalas ir nepriklausomas liudijimas patvirtino poslinkį.";}
        else if(step.position==2){evidence=evidenceId(q);if(evidence!=null){ContentValues v=new ContentValues();v.put("quest_id","Q-MERIDIAN");v.put("evidence_id",evidence);v.put("detail",evidenceDetail(evidence));v.put("source",action);v.put("created_minute",state.worldMinute);long inserted=db().insertWithOnConflict("quest_evidence",null,v,SQLiteDatabase.CONFLICT_IGNORE);int count=evidenceCount();ContentValues p=new ContentValues();p.put("progress",Math.min(3,count));db().update("quest_steps",p,"id=?",new String[]{step.id});if(inserted!=-1)detail="Užfiksuotas nepriklausomas atramos taškas: "+evidenceDetail(evidence)+".";if(count>=3){completeAndActivate(step,3);detail+=" Atramos tinklas užbaigtas.";}}}
        else if(step.position==3&&(q.contains("orison")||q.contains("pereiti meridian")||q.contains("įžengti")||q.contains("izengti"))){completeAndActivate(step,4);detail="Sėkmingai grįžai su patikrinamais Orisono kontakto duomenimis.";}
        else if(step.position==4&&q.contains("chartij")){String outcome=q.contains("atmest")||q.contains("atsisak")?"atmesta":q.contains("nepriklaus")?"nepriklausoma":"priimta";completeAndActivate(step,5);ContentValues quest=new ContentValues();quest.put("outcome",outcome);db().update("quests",quest,"id='Q-MERIDIAN'",null);state.storyEnding=outcome;applyEndingConsequences(outcome,state);detail="Pirmosios Meridiano chartijos baigtis: "+outcome+". Sprendimas jau pakeitė frakcijų įtaką, gyvenviečių autonomiją ir rinkas.";}
        else if(step.position==5&&(q.contains("pasekm")||q.contains("tęsti")||q.contains("testi")||q.contains("grįž")||q.contains("griz"))){ContentValues done=new ContentValues();done.put("status","completed");done.put("progress",1);db().update("quest_steps",done,"id=?",new String[]{step.id});ContentValues quest=new ContentValues();quest.put("status","completed");quest.put("stage",6);quest.put("updated_at",state.worldMinute);db().update("quests",quest,"id='Q-MERIDIAN'",null);detail="Lūžusio Meridiano I dalis užbaigta. Pasaulis išsaugojo tavo pasirinktą baigtį.";}
        applyQuestToState(state);return detail;
    }

    List<Npc> npcs(String location,long worldMinute){ArrayList<Npc> result=new ArrayList<>();String query=location==null?"Luminara":location;try(Cursor c=db().rawQuery("SELECT id,name,role,location,service,relationship,trust,available,last_topic,interactions,memory_json FROM npcs WHERE location=? ORDER BY relationship DESC,name",new String[]{query})){while(c.moveToNext()){Npc n=new Npc();n.id=c.getString(0);n.name=c.getString(1);n.role=c.getString(2);n.location=c.getString(3);n.service=c.getString(4);n.relationship=c.getInt(5);n.trust=c.getInt(6);n.available=c.getInt(7)==1&&scheduleAvailable(n.service,worldMinute);n.lastTopic=c.getString(8);n.interactions=c.getInt(9);n.memory=c.getString(10);result.add(n);}}return result;}

    Npc findNpc(String value){return findNpc(value,720);}
    Npc findNpc(String value,long minute){String q=norm(value);for(Npc npc:npcs("Luminara",minute))if(q.contains(norm(npc.name))||q.contains(norm(npc.name).split(" ")[0]))return npc;return null;}

    String recordNpcInteraction(String action,String event,long minute){Npc npc=findNpc(action);if(npc==null)return"";int delta="setback".equals(event)?-2:"reward".equals(event)?4:2;JSONArray memory;try{memory=new JSONArray(npc.memory);}catch(Exception ignored){memory=new JSONArray();}memory.put(compact(action,120));while(memory.length()>8)removeFirst(memory);ContentValues v=new ContentValues();v.put("relationship",clamp(npc.relationship+delta,-100,100));v.put("trust",clamp(npc.trust+(delta>0?1:-1),0,100));v.put("last_topic",compact(action,80));v.put("interactions",npc.interactions+1);v.put("memory_json",memory.toString());db().update("npcs",v,"id=?",new String[]{npc.id});return npc.name+" prisimins šį pokalbį · santykis "+signed(delta);}

    Shop shopForNpc(String npcName){Npc npc=findNpc(npcName);if(npc==null||!npc.service.startsWith("shop:"))return null;String id=npc.service.substring(5);try(Cursor c=db().rawQuery("SELECT id,name,npc_id,location,markup,buyback FROM shops WHERE id=?",new String[]{id})){if(c.moveToFirst()){Shop s=new Shop();s.id=c.getString(0);s.name=c.getString(1);s.npcId=c.getString(2);s.location=c.getString(3);s.markup=c.getInt(4);s.buyback=c.getInt(5);return s;}}return null;}

    List<Stock> stock(Shop shop,GameState state){if(shop==null)return new ArrayList<>();restock(shop,state.worldMinute);int index=priceIndex(shop.location);Npc npc=npcById(shop.npcId);int discount=npc==null?0:Math.max(0,npc.relationship/5);boolean tradeTalent=unlockedTalentIds().contains("leader_trade");ArrayList<Stock> result=new ArrayList<>();try(Cursor c=db().rawQuery("SELECT s.shop_id,s.catalog_id,s.quantity,s.base_price,s.level FROM shop_stock s WHERE s.shop_id=? AND s.quantity>0 ORDER BY s.level,s.catalog_id",new String[]{shop.id})){while(c.moveToNext()){ItemCatalogV092.ItemDef item=ItemCatalogV092.byId(c.getString(1));if(item==null)continue;Stock stock=new Stock();stock.shopId=c.getString(0);stock.catalogId=c.getString(1);stock.quantity=c.getInt(2);long calculated=(long)c.getInt(3)*index*shop.markup/10000L/100L-discount;if(tradeTalent)calculated=calculated*92L/100L;stock.price=(int)Math.max(1,Math.min(Integer.MAX_VALUE,calculated));stock.level=c.getInt(4);stock.name=item.name;stock.category=item.category;stock.rarity=item.rarity;result.add(stock);}}return result;}

    TransactionResult buy(Shop shop,String catalogId,GameState state){if(shop==null)return new TransactionResult(false,"Parduotuvė nerasta");Stock selected=null;for(Stock stock:stock(shop,state))if(stock.catalogId.equals(catalogId)){selected=stock;break;}if(selected==null)return new TransactionResult(false,"Prekės nebėra sandėlyje");if(state.crowns<selected.price)return new TransactionResult(false,"Nepakanka karūnų · reikia "+selected.price);ItemCatalogV092.ItemDef item=ItemCatalogV092.byId(catalogId);if(item==null)return new TransactionResult(false,"Daikto aprašas nerastas");SQLiteDatabase db=db();db.beginTransaction();try{ContentValues q=new ContentValues();q.put("quantity",selected.quantity-1);db.update("shop_stock",q,"shop_id=? AND catalog_id=?",new String[]{shop.id,catalogId});owner.addCatalogLoot(item,1);state.crowns-=selected.price;owner.saveState(state);db.setTransactionSuccessful();return new TransactionResult(true,"Nupirkta: "+item.name+" · -"+selected.price+" karūnų");}finally{db.endTransaction();}}

    TransactionResult sell(Shop shop,String itemId,GameState state){VaeloriaDb.Item item=owner.getItem(itemId);if(shop==null||item==null)return new TransactionResult(false,"Sandoris negalimas");if(item.equipped||item.synced||"quest".equals(item.slot)||"quest".equals(item.type))return new TransactionResult(false,"Šio daikto parduoti negalima");int index=priceIndex(shop.location);long calculated=(long)item.value*index*shop.buyback/10000L/100L;if(unlockedTalentIds().contains("leader_trade"))calculated=calculated*108L/100L;int price=(int)Math.max(1,Math.min(Integer.MAX_VALUE,calculated));SQLiteDatabase db=db();db.beginTransaction();try{if(item.quantity>1){ContentValues v=new ContentValues();v.put("quantity",item.quantity-1);db.update("items",v,"id=?",new String[]{item.id});}else db.delete("items","id=?",new String[]{item.id});state.crowns+=price;owner.saveState(state);db.setTransactionSuccessful();return new TransactionResult(true,"Parduota: "+item.name+" · +"+price+" karūnų");}finally{db.endTransaction();}}

    List<Recipe> recipes(){ArrayList<Recipe> result=new ArrayList<>();boolean saver=unlockedTalentIds().contains("craft_saver");try(Cursor c=db().rawQuery("SELECT id,name,result_catalog_id,ingredient_catalog_id,ingredient_qty,fee,unlocked FROM recipes ORDER BY fee",null)){while(c.moveToNext()){Recipe r=new Recipe();r.id=c.getString(0);r.name=c.getString(1);r.resultCatalogId=c.getString(2);r.ingredientCatalogId=c.getString(3);r.ingredientQty=c.getInt(4);r.fee=saver?Math.max(1,c.getInt(5)*80/100):c.getInt(5);r.unlocked=c.getInt(6)==1;ItemCatalogV092.ItemDef ingredient=ItemCatalogV092.byId(r.ingredientCatalogId);r.ingredientName=ingredient==null?r.ingredientCatalogId:ingredient.name;result.add(r);}}return result;}

    int ownedCatalogQuantity(String catalogId){try(Cursor c=db().rawQuery("SELECT COALESCE(SUM(quantity),0) FROM items WHERE catalog_id=?",new String[]{catalogId})){return c.moveToFirst()?c.getInt(0):0;}}

    TransactionResult craft(String recipeId,GameState state){Recipe recipe=null;for(Recipe candidate:recipes())if(candidate.id.equals(recipeId)){recipe=candidate;break;}if(recipe==null||!recipe.unlocked)return new TransactionResult(false,"Receptas neatrakintas");if(ownedCatalogQuantity(recipe.ingredientCatalogId)<recipe.ingredientQty)return new TransactionResult(false,"Trūksta: "+recipe.ingredientName+" ×"+recipe.ingredientQty);if(state.crowns<recipe.fee)return new TransactionResult(false,"Nepakanka karūnų · reikia "+recipe.fee);ItemCatalogV092.ItemDef result=ItemCatalogV092.byId(recipe.resultCatalogId);if(result==null)return new TransactionResult(false,"Rezultato aprašas nerastas");boolean quality=unlockedTalentIds().contains("craft_quality");SQLiteDatabase db=db();db.beginTransaction();try{consumeCatalog(db,recipe.ingredientCatalogId,recipe.ingredientQty);state.crowns-=recipe.fee;owner.addCatalogLoot(result,1);if(quality)owner.awardMastery("Amatų meistriškumas",50);owner.saveState(state);db.setTransactionSuccessful();return new TransactionResult(true,"Pagaminta: "+result.name+" · sunaudota "+recipe.ingredientName+" ×"+recipe.ingredientQty+" · -"+recipe.fee+" karūnų"+(quality?" · +50 amatų meistriškumo patirties":""));}finally{db.endTransaction();}}

    List<Event> activeEvents(){ArrayList<Event> result=new ArrayList<>();try(Cursor c=db().rawQuery("SELECT id,title,detail,region,type,severity,active,price_modifier,expires_minute FROM world_events WHERE active=1 ORDER BY severity DESC,id DESC",null)){while(c.moveToNext()){Event e=new Event();e.id=c.getLong(0);e.title=c.getString(1);e.detail=c.getString(2);e.region=c.getString(3);e.type=c.getString(4);e.severity=c.getInt(5);e.active=c.getInt(6)==1;e.priceModifier=c.getInt(7);e.expiresMinute=c.getLong(8);result.add(e);}}return result;}

    List<Faction> factions(){ArrayList<Faction> result=new ArrayList<>();try(Cursor c=db().rawQuery("SELECT id,name,influence,relation,treasury,territory,tension FROM factions ORDER BY influence DESC",null)){while(c.moveToNext()){Faction f=new Faction();f.id=c.getString(0);f.name=c.getString(1);f.influence=c.getInt(2);f.relation=c.getString(3);f.treasury=c.getInt(4);f.territory=c.getInt(5);f.tension=c.getInt(6);result.add(f);}}return result;}

    List<Settlement> settlements(){ArrayList<Settlement> result=new ArrayList<>();try(Cursor c=db().rawQuery("SELECT id,name,region,faction,prosperity,security,autonomy,rival_id FROM settlements ORDER BY prosperity DESC",null)){while(c.moveToNext()){Settlement s=new Settlement();s.id=c.getString(0);s.name=c.getString(1);s.region=c.getString(2);s.faction=c.getString(3);s.prosperity=c.getInt(4);s.security=c.getInt(5);s.autonomy=c.getInt(6);s.rivalId=c.getString(7);result.add(s);}}return result;}

    List<LocationInfo> locations(){ArrayList<LocationInfo> result=new ArrayList<>();try(Cursor c=db().rawQuery("SELECT id,name,region,danger,x,y,discovered,visited,last_visit FROM locations ORDER BY discovered DESC,name",null)){while(c.moveToNext()){LocationInfo value=new LocationInfo();value.id=c.getString(0);value.name=c.getString(1);value.region=c.getString(2);value.danger=c.getInt(3);value.x=c.getInt(4);value.y=c.getInt(5);value.discovered=c.getInt(6)==1;value.visited=c.getInt(7)==1;value.lastVisit=c.getLong(8);result.add(value);}}return result;}

    Set<String> discoveredLocations(){LinkedHashSet<String> names=new LinkedHashSet<>();for(LocationInfo location:locations())if(location.discovered)names.add(location.name);return names;}
    boolean isDiscovered(String name){if(name==null)return false;for(LocationInfo location:locations())if(location.name.equalsIgnoreCase(name))return location.discovered;return true;}

    void initializeOriginDiscoveries(String originId){
        if("dravenn".equals(originId))discoverById("kharad");else if("pelkynai".equals(originId)){discoverById("pelkynas");discoverById("saltinio");}
        else if("pasienis".equals(originId)){discoverById("stiklo");discoverById("kharad");}
        else if("akademija".equals(originId))discoverById("aurelionas");
    }

    String recordExploration(String action,GameState state){
        String query=norm(action);String found="";for(LocationInfo location:locations()){String name=norm(location.name);String first=name.split(" ")[0];if((query.contains(name)||query.contains(first))&&!location.discovered){discoverById(location.id);found="Atrasta nauja atlaso vieta: "+location.name;}}
        for(LocationInfo location:locations())if(location.name.equalsIgnoreCase(state.location)){ContentValues values=new ContentValues();values.put("discovered",1);values.put("visited",1);values.put("last_visit",state.worldMinute);db().update("locations",values,"id=?",new String[]{location.id});break;}
        return found;
    }

    int travelMinutes(String from,String to){LocationInfo a=locationByName(from),b=locationByName(to);if(a==null||b==null||a.id.equals(b.id))return-1;double distance=Math.sqrt((a.x-b.x)*(double)(a.x-b.x)+(a.y-b.y)*(double)(a.y-b.y));return Math.max(35,(int)Math.round(28+distance*.48+b.danger*7));}
    private LocationInfo locationByName(String name){if(name==null)return null;for(LocationInfo value:locations())if(value.name.equalsIgnoreCase(name))return value;return null;}
    private void discoverById(String id){ContentValues values=new ContentValues();values.put("discovered",1);db().update("locations",values,"id=?",new String[]{id});}

    String advanceWorld(GameState state,String eventTag,int elapsedMinutes){
        SQLiteDatabase db=db();db.execSQL("UPDATE world_events SET active=0 WHERE active=1 AND expires_minute<=?",new Object[]{state.worldMinute});db.execSQL("UPDATE economy SET price_index=CASE WHEN price_index>100 THEN price_index-1 WHEN price_index<100 THEN price_index+1 ELSE 100 END,updated_minute=?",new Object[]{state.worldMinute});db.execSQL("UPDATE settlements SET prosperity=MAX(0,MIN(100,prosperity+CASE WHEN security>=50 THEN 1 ELSE -1 END)) WHERE ABS(?+LENGTH(id))%4=0",new Object[]{state.turnNumber});
        syncFactions(state);String created="";if(activeEvents().isEmpty()&&state.turnNumber>0&&state.turnNumber%5==0)created=createWorldEvent(state);applyBusinessPayouts(state);restockAll(state.worldMinute);return created;
    }

    String contextPrompt(GameState state){
        StringBuilder out=new StringBuilder();Quest quest=activeMainQuest();QuestStep step=activeMainStep();
        if(quest!=null){out.append("Pagrindinė užduotis: ").append(quest.title).append(" · būsena ").append(quest.status).append(" · etapas ").append(quest.stage).append('.');if(step!=null)out.append(" Dabartinis struktūrizuotas tikslas: ").append(step.title).append(" (").append(step.progress).append('/').append(step.target).append(").");if(!quest.outcome.isEmpty())out.append(" Baigtis: ").append(quest.outcome).append('.');}
        List<Event> events=activeEvents();if(!events.isEmpty()){out.append(" Aktyvūs pasaulio įvykiai: ");for(int i=0;i<Math.min(3,events.size());i++){if(i>0)out.append("; ");out.append(events.get(i).title).append(" [").append(events.get(i).region).append(']');}out.append('.');}
        ArrayList<String> memories=new ArrayList<>();try(Cursor c=db().rawQuery("SELECT name,relationship,last_topic FROM npcs WHERE interactions>0 ORDER BY interactions DESC LIMIT 5",null)){while(c.moveToNext())memories.add(c.getString(0)+" santykis "+c.getInt(1)+", prisimena: "+c.getString(2));}if(!memories.isEmpty())out.append(" NPC atmintis: ").append(String.join("; ",memories)).append('.');
        Companion companion=activeCompanion();if(companion!=null)out.append(" Aktyvus kompanionas: ").append(companion.name).append(" · lojalumas ").append(companion.loyalty).append(" · mechaninis privalumas: ").append(companion.perk).append('.');
        Set<String> talents=unlockedTalentIds();if(!talents.isEmpty())out.append(" Atrakinti talentai: ").append(String.join(", ",talents)).append('.');
        if(state!=null){out.append(" Veikėjo lygis ").append(state.level).append(", sunkumas ").append(state.difficulty).append('.');if(!state.storyEnding.isEmpty())out.append(" Išsaugota siužeto baigtis: ").append(state.storyEnding).append('.');}
        return out.toString();
    }

    List<Business> businesses(){ArrayList<Business> result=new ArrayList<>();try(Cursor c=db().rawQuery("SELECT id,name,type,location,level,revenue,upkeep,price,next_payout,owned FROM businesses ORDER BY owned DESC,price",null)){while(c.moveToNext()){Business b=new Business();b.id=c.getString(0);b.name=c.getString(1);b.type=c.getString(2);b.location=c.getString(3);b.level=c.getInt(4);b.revenue=c.getInt(5);b.upkeep=c.getInt(6);b.price=c.getInt(7);b.nextPayout=c.getLong(8);b.owned=c.getInt(9)==1;result.add(b);}}return result;}

    TransactionResult buyBusiness(String id,GameState state){Business selected=null;for(Business b:businesses())if(b.id.equals(id)){selected=b;break;}if(selected==null||selected.owned)return new TransactionResult(false,"Verslas nepasiekiamas");if(state.crowns<selected.price)return new TransactionResult(false,"Nepakanka karūnų · reikia "+selected.price);state.crowns-=selected.price;ContentValues v=new ContentValues();v.put("owned",1);v.put("next_payout",state.worldMinute+1440);db().update("businesses",v,"id=?",new String[]{id});owner.saveState(state);return new TransactionResult(true,"Įsigyta: "+selected.name+" · pirmas pelnas po vienos pasaulio dienos");}

    List<Companion> companions(){ArrayList<Companion> result=new ArrayList<>();try(Cursor c=db().rawQuery("SELECT id,npc_id,name,role,perk,loyalty,recruited,active FROM companions ORDER BY recruited DESC,name",null)){while(c.moveToNext()){Companion x=new Companion();x.id=c.getString(0);x.npcId=c.getString(1);x.name=c.getString(2);x.role=c.getString(3);x.perk=c.getString(4);x.loyalty=c.getInt(5);x.recruited=c.getInt(6)==1;x.active=c.getInt(7)==1;result.add(x);}}return result;}

    Companion activeCompanion(){for(Companion value:companions())if(value.active)return value;return null;}

    TransactionResult recruitCompanion(String id,GameState state){
        Companion selected=null;for(Companion value:companions())if(value.id.equals(id)){selected=value;break;}
        if(selected==null)return new TransactionResult(false,"Kompanionas nerastas");
        if(selected.recruited)return activateCompanion(id);
        Npc npc=npcById(selected.npcId);Quest main=activeMainQuest();int stage=main==null?0:main.stage;
        if(npc==null||(npc.relationship<4&&stage<2))return new TransactionResult(false,"Dar trūksta pasitikėjimo: pasikalbėk bent du kartus arba pasiek 3-ią Meridiano etapą");
        ContentValues recruit=new ContentValues();recruit.put("recruited",1);recruit.put("loyalty",Math.max(25,npc.trust+15));
        db().update("companions",recruit,"id=?",new String[]{id});
        return activateCompanion(id);
    }

    TransactionResult activateCompanion(String id){
        Companion selected=null;for(Companion value:companions())if(value.id.equals(id)){selected=value;break;}
        if(selected==null||!selected.recruited)return new TransactionResult(false,"Pirmiausia reikia prisivilioti šį kompanioną");
        SQLiteDatabase database=db();database.beginTransaction();try{
            database.execSQL("UPDATE companions SET active=0");
            ContentValues active=new ContentValues();active.put("active",1);database.update("companions",active,"id=?",new String[]{id});
            database.setTransactionSuccessful();
        }finally{database.endTransaction();}
        return new TransactionResult(true,"Aktyvus kompanionas: "+selected.name+" · "+selected.perk);
    }

    TransactionResult dismissCompanion(){
        Companion active=activeCompanion();if(active==null)return new TransactionResult(false,"Aktyvaus kompaniono nėra");
        ContentValues values=new ContentValues();values.put("active",0);db().update("companions",values,"id=?",new String[]{active.id});
        return new TransactionResult(true,active.name+" lieka sąjungininkas, bet dabar tavęs nelydi");
    }

    int companionCheckBonus(String action){
        Companion active=activeCompanion();if(active==null||!"comp-lyra".equals(active.id))return 0;
        String query=norm(action);return query.contains("tirt")||query.contains("iešk")||query.contains("iesk")||query.contains("meridian")||query.contains("analiz")?4:0;
    }

    int companionDefenseBonus(int round){Companion active=activeCompanion();return active!=null&&"comp-kaelis".equals(active.id)&&round<=1?6:0;}
    int companionVictoryHealing(){Companion active=activeCompanion();return active!=null&&"comp-mirel".equals(active.id)?8:0;}

    String recordCompanionTurn(String action,String event,GameState state){
        Companion active=activeCompanion();if(active==null)return"";int delta="setback".equals(event)?-2:("combat_victory".equals(event)||norm(action).contains(active.name.toLowerCase(Locale.forLanguageTag("lt-LT")))?2:1);
        if(unlockedTalentIds().contains("leader_bond")&&delta>0)delta++;
        ContentValues values=new ContentValues();values.put("loyalty",clamp(active.loyalty+delta,0,100));db().update("companions",values,"id=?",new String[]{active.id});
        return active.name+" lojalumas "+signed(delta);
    }

    List<TalentState> talents(){
        ArrayList<TalentState> result=new ArrayList<>();Set<String> known=new LinkedHashSet<>();
        try(Cursor c=db().rawQuery("SELECT id,unlocked,unlocked_minute FROM talents",null)){while(c.moveToNext()){ProgressionEngine.TalentDef definition=ProgressionEngine.byId(c.getString(0));if(definition==null)continue;TalentState state=new TalentState();state.definition=definition;state.unlocked=c.getInt(1)==1;state.unlockedMinute=c.getLong(2);result.add(state);known.add(definition.id);}}
        for(ProgressionEngine.TalentDef definition:ProgressionEngine.TALENTS)if(!known.contains(definition.id)){seedTalent(db(),definition.id);TalentState state=new TalentState();state.definition=definition;result.add(state);}
        result.sort((a,b)->{int branch=a.definition.branch.compareTo(b.definition.branch);return branch!=0?branch:Integer.compare(a.definition.requiredLevel,b.definition.requiredLevel);});return result;
    }

    Set<String> unlockedTalentIds(){LinkedHashSet<String> ids=new LinkedHashSet<>();try(Cursor c=db().rawQuery("SELECT id FROM talents WHERE unlocked=1",null)){while(c.moveToNext())ids.add(c.getString(0));}return ids;}

    TransactionResult unlockTalent(String id,GameState state){
        ProgressionEngine.TalentDef talent=ProgressionEngine.byId(id);if(talent==null)return new TransactionResult(false,"Talentas nerastas");
        Set<String> unlocked=unlockedTalentIds();if(unlocked.contains(id))return new TransactionResult(false,"Talentas jau atrakintas");
        if(state.level<talent.requiredLevel)return new TransactionResult(false,"Reikia "+talent.requiredLevel+" veikėjo lygio");
        if(!talent.prerequisite.isEmpty()&&!unlocked.contains(talent.prerequisite)){ProgressionEngine.TalentDef required=ProgressionEngine.byId(talent.prerequisite);return new TransactionResult(false,"Pirmiausia atrakink: "+(required==null?talent.prerequisite:required.name));}
        if(state.talentPoints<talent.cost)return new TransactionResult(false,"Trūksta talentų taškų · reikia "+talent.cost);
        state.talentPoints-=talent.cost;ContentValues values=new ContentValues();values.put("unlocked",1);values.put("unlocked_minute",state.worldMinute);db().update("talents",values,"id=?",new String[]{id});
        if("arcane_reserve".equals(id)){state.manaMax+=20;state.mana+=20;}
        if("craft_masterpiece".equals(id))db().execSQL("UPDATE recipes SET unlocked=1 WHERE id LIKE 'recipe-master-%'");
        owner.saveState(state);return new TransactionResult(true,"Atrakintas talentas: "+talent.name+" · "+talent.description);
    }

    List<Hire> hires(){ArrayList<Hire> result=new ArrayList<>();try(Cursor c=db().rawQuery("SELECT h.npc_id,n.name,h.job,h.wage,h.loyalty,h.active,h.next_pay FROM hired_npcs h JOIN npcs n ON n.id=h.npc_id ORDER BY h.active DESC,n.name",null)){while(c.moveToNext()){Hire h=new Hire();h.npcId=c.getString(0);h.name=c.getString(1);h.job=c.getString(2);h.wage=c.getInt(3);h.loyalty=c.getInt(4);h.active=c.getInt(5)==1;h.nextPay=c.getLong(6);result.add(h);}}return result;}

    List<Npc> hireCandidates(long minute){ArrayList<Npc> result=new ArrayList<>();for(Npc npc:npcs("Luminara",minute))if("traveler".equals(npc.service)||"builder".equals(npc.service)||"guild".equals(npc.service)||"research".equals(npc.service))result.add(npc);return result;}

    TransactionResult hire(String npcId,String job,int wage,GameState state){Npc npc=npcById(npcId);if(npc==null)return new TransactionResult(false,"Veikėjas nerastas");int daily=Math.max(40,wage);if(state.crowns<daily)return new TransactionResult(false,"Pirmai algai reikia "+daily+" karūnų");ContentValues v=new ContentValues();v.put("npc_id",npcId);v.put("job",job);v.put("wage",daily);v.put("loyalty",25);v.put("active",1);v.put("hired_minute",state.worldMinute);v.put("next_pay",state.worldMinute+1440);db().insertWithOnConflict("hired_npcs",null,v,SQLiteDatabase.CONFLICT_REPLACE);state.crowns-=daily;owner.saveState(state);return new TransactionResult(true,"Pasamdytas: "+npc.name+" · darbas: "+job+" · dienos alga "+daily);}

    JSONObject exportState(){JSONObject root=new JSONObject();try{for(String table:TABLES){JSONArray rows=new JSONArray();try(Cursor c=db().rawQuery("SELECT * FROM "+table,null)){String[] columns=c.getColumnNames();while(c.moveToNext()){JSONObject row=new JSONObject();for(int i=0;i<columns.length;i++){switch(c.getType(i)){case Cursor.FIELD_TYPE_NULL:row.put(columns[i],JSONObject.NULL);break;case Cursor.FIELD_TYPE_INTEGER:row.put(columns[i],c.getLong(i));break;case Cursor.FIELD_TYPE_FLOAT:row.put(columns[i],c.getDouble(i));break;default:row.put(columns[i],c.getString(i));}}rows.put(row);}}root.put(table,rows);}}catch(Exception ignored){}return root;}

    void restoreState(SQLiteDatabase db,JSONObject root)throws Exception{if(root==null)return;for(String table:TABLES){JSONArray rows=root.optJSONArray(table);if(rows==null)continue;db.delete(table,null,null);for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);ContentValues values=new ContentValues();JSONArray names=row.names();if(names==null)continue;for(int n=0;n<names.length();n++){String key=names.getString(n);Object value=row.opt(key);if(value==null||value==JSONObject.NULL)values.putNull(key);else if(value instanceof Integer)values.put(key,(Integer)value);else if(value instanceof Long)values.put(key,(Long)value);else if(value instanceof Double)values.put(key,(Double)value);else if(value instanceof Boolean)values.put(key,(Boolean)value?1:0);else values.put(key,String.valueOf(value));}db.insertOrThrow(table,null,values);}}}

    static void drop(SQLiteDatabase db){for(String table:TABLES)db.execSQL("DROP TABLE IF EXISTS "+table);}

    private void completeAndActivate(QuestStep step,int next){ContentValues done=new ContentValues();done.put("status","completed");done.put("progress",step.target);db().update("quest_steps",done,"id=?",new String[]{step.id});ContentValues active=new ContentValues();active.put("status","active");db().update("quest_steps",active,"quest_id='Q-MERIDIAN' AND position=?",new String[]{String.valueOf(next)});ContentValues quest=new ContentValues();quest.put("stage",next);quest.put("updated_at",System.currentTimeMillis());db().update("quests",quest,"id='Q-MERIDIAN'",null);}
    private void applyEndingConsequences(String outcome,GameState state){
        if("priimta".equals(outcome)){state.asterraInfluence=clamp(state.asterraInfluence+12,0,100);state.dravennInfluence=clamp(state.dravennInfluence-5,0,100);db().execSQL("UPDATE settlements SET security=MIN(100,security+8),autonomy=MAX(0,autonomy-7)");db().execSQL("UPDATE economy SET price_index=MAX(65,price_index-6)");}
        else if("atmesta".equals(outcome)){state.asterraInfluence=clamp(state.asterraInfluence-9,0,100);state.dravennInfluence=clamp(state.dravennInfluence+8,0,100);db().execSQL("UPDATE settlements SET security=MAX(0,security-5),autonomy=MIN(100,autonomy+4)");db().execSQL("UPDATE economy SET price_index=MIN(180,price_index+9)");}
        else{state.lysaraInfluence=clamp(state.lysaraInfluence+11,0,100);state.asterraInfluence=clamp(state.asterraInfluence+3,0,100);db().execSQL("UPDATE settlements SET autonomy=MIN(100,autonomy+10),prosperity=MIN(100,prosperity+3)");db().execSQL("UPDATE economy SET supply=MIN(100,supply+5),price_index=MAX(65,price_index-3)");}
        syncFactions(state);owner.saveState(state);
    }
    private int evidenceCount(){try(Cursor c=db().rawQuery("SELECT COUNT(*) FROM quest_evidence WHERE quest_id='Q-MERIDIAN'",null)){return c.moveToFirst()?c.getInt(0):0;}}
    private String evidenceId(String q){if(q.contains("lyra"))return"lyra";if(q.contains("kaeli")||q.contains("sargyb"))return"kaelis";if(q.contains("orin")||q.contains("archyv"))return"orinas";if(q.contains("veyrhold"))return"veyrhold";if(q.contains("kharad"))return"kharad";if(q.contains("lysar")||q.contains("pelkyn"))return"lysara";if(q.contains("atramos tašk")||q.contains("atramos task"))return"field-"+Math.abs(q.hashCode()%1000);return null;}
    private String evidenceDetail(String id){if("lyra".equals(id))return"Lyros lauko matavimai";if("kaelis".equals(id))return"Kaelio sargybos žurnalas";if("orinas".equals(id))return"Orino archyvo chartija";if("veyrhold".equals(id))return"Veyrhold tranzito įrašas";if("kharad".equals(id))return"Kharad kalibravimo stotis";if("lysara".equals(id))return"Lysaros gyvojo kelio stebėjimas";return"lauko atramos taškas";}
    private int priceIndex(String location){try(Cursor c=db().rawQuery("SELECT AVG(price_index) FROM economy WHERE region=? OR region='Luminara'",new String[]{location})){return c.moveToFirst()?Math.max(65,Math.min(180,c.getInt(0))):100;}}
    private Npc npcById(String id){try(Cursor c=db().rawQuery("SELECT id,name,role,location,service,relationship,trust,available,last_topic,interactions,memory_json FROM npcs WHERE id=?",new String[]{id})){if(c.moveToFirst()){Npc n=new Npc();n.id=c.getString(0);n.name=c.getString(1);n.role=c.getString(2);n.location=c.getString(3);n.service=c.getString(4);n.relationship=c.getInt(5);n.trust=c.getInt(6);n.available=c.getInt(7)==1;n.lastTopic=c.getString(8);n.interactions=c.getInt(9);n.memory=c.getString(10);return n;}}return null;}
    private void restock(Shop shop,long minute){long day=minute/1440;try(Cursor c=db().rawQuery("SELECT stock_day FROM shops WHERE id=?",new String[]{shop.id})){if(c.moveToFirst()&&c.getLong(0)==day)return;}db().execSQL("UPDATE shop_stock SET quantity=MIN(8,quantity+1+ABS(?+LENGTH(catalog_id))%3) WHERE shop_id=?",new Object[]{day,shop.id});ContentValues v=new ContentValues();v.put("stock_day",day);db().update("shops",v,"id=?",new String[]{shop.id});}
    private void restockAll(long minute){for(String id:new String[]{"smith","armor","runes","pharmacy","general","inn","stable"}){try(Cursor c=db().rawQuery("SELECT id,name,npc_id,location,markup,buyback FROM shops WHERE id=?",new String[]{id})){if(c.moveToFirst()){Shop s=new Shop();s.id=c.getString(0);s.name=c.getString(1);s.npcId=c.getString(2);s.location=c.getString(3);s.markup=c.getInt(4);s.buyback=c.getInt(5);restock(s,minute);}}}}
    private void syncFactions(GameState state){updateFaction("asterra",state.asterraInfluence,state.asterraRelation);updateFaction("dravenn",state.dravennInfluence,state.dravennRelation);updateFaction("lysara",state.lysaraInfluence,state.lysaraRelation);}
    private void updateFaction(String id,int influence,String relation){ContentValues v=new ContentValues();v.put("influence",influence);v.put("relation",relation);db().update("factions",v,"id=?",new String[]{id});}
    private String createWorldEvent(GameState state){String[][] templates={{"Karavanų vėlavimas","Meridiano poslinkis stabdo tiekimą ir kelia miesto kainas.","Luminara","trade","3","12"},{"Kalvių rūdos trūkumas","Geležinių Viršūnių kelias užvertas, todėl metalas brangsta.","GELEŽINĖS VIRŠŪNĖS","supply","4","18"},{"Gausus girios derlius","Žolininkai pargabeno daug saugių reagentų; potionai pinga.","KRAUJŠAKNĖS GIRIA","harvest","2","-10"},{"Dravenn pasienio įtampa","Patikros lėtina prekybą ir augina kovos reikmenų paklausą.","Luminara","politics","5","15"}};String[] t=templates[(int)(state.turnNumber%templates.length)];ContentValues v=new ContentValues();v.put("title",t[0]);v.put("detail",t[1]);v.put("region",t[2]);v.put("type",t[3]);v.put("severity",Integer.parseInt(t[4]));v.put("active",1);v.put("price_modifier",Integer.parseInt(t[5]));v.put("created_minute",state.worldMinute);v.put("expires_minute",state.worldMinute+720);db().insert("world_events",null,v);db().execSQL("UPDATE economy SET price_index=MAX(65,MIN(180,price_index+?)) WHERE region=? OR (?='Luminara' AND region='Luminara')",new Object[]{Integer.parseInt(t[5]),t[2],t[2]});return"Pasaulio įvykis: "+t[0];}
    private void applyBusinessPayouts(GameState state){boolean changed=false;for(Business b:businesses())if(b.owned&&b.nextPayout<=state.worldMinute){int net=Math.max(0,(b.revenue-b.upkeep)*priceIndex(b.location)/100);state.crowns+=net;ContentValues v=new ContentValues();v.put("next_payout",state.worldMinute+1440);db().update("businesses",v,"id=?",new String[]{b.id});changed=true;}for(Hire hire:hires())if(hire.active&&hire.nextPay<=state.worldMinute){ContentValues v=new ContentValues();if(state.crowns>=hire.wage){state.crowns-=hire.wage;v.put("loyalty",Math.min(100,hire.loyalty+1));v.put("next_pay",state.worldMinute+1440);}else{v.put("active",0);v.put("loyalty",Math.max(0,hire.loyalty-10));}db().update("hired_npcs",v,"npc_id=?",new String[]{hire.npcId});changed=true;}if(changed)owner.saveState(state);}
    private void consumeCatalog(SQLiteDatabase db,String catalogId,int amount){int left=amount;try(Cursor c=db.rawQuery("SELECT id,quantity FROM items WHERE catalog_id=? ORDER BY equipped ASC,quantity ASC",new String[]{catalogId})){while(c.moveToNext()&&left>0){String id=c.getString(0);int quantity=c.getInt(1);if(quantity<=left){db.delete("items","id=?",new String[]{id});left-=quantity;}else{ContentValues v=new ContentValues();v.put("quantity",quantity-left);db.update("items",v,"id=?",new String[]{id});left=0;}}}}
    private SQLiteDatabase db(){return owner.getWritableDatabase();}

    private static void seedQuest(SQLiteDatabase db,String id,String title,String type,String status){ContentValues v=new ContentValues();v.put("id",id);v.put("title",title);v.put("type",type);v.put("status",status);db.insertWithOnConflict("quests",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private static void seedSideQuest(SQLiteDatabase db,String id,String title,String detail){seedQuest(db,id,title,"side","available");seedStep(db,id+"-01",id,0,detail,"available",1);}
    private static void seedStep(SQLiteDatabase db,String id,String quest,int position,String title,String status,int target){ContentValues v=new ContentValues();v.put("id",id);v.put("quest_id",quest);v.put("position",position);v.put("title",title);v.put("status",status);v.put("target",target);db.insertWithOnConflict("quest_steps",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private static void seedNpc(SQLiteDatabase db,String id,String name,String role,String location,String service){ContentValues v=new ContentValues();v.put("id",id);v.put("name",name);v.put("role",role);v.put("location",location);v.put("service",service);db.insertWithOnConflict("npcs",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private static void seedShop(SQLiteDatabase db,String id,String name,String npc,String location,int markup,int buyback,String[] categories){ContentValues v=new ContentValues();v.put("id",id);v.put("name",name);v.put("npc_id",npc);v.put("location",location);v.put("markup",markup);v.put("buyback",buyback);db.insertWithOnConflict("shops",null,v,SQLiteDatabase.CONFLICT_IGNORE);int added=0;List<String> allowed=Arrays.asList(categories);for(ItemCatalogV092.ItemDef item:ItemCatalogV092.ALL){if(!allowed.contains(item.category)||"quest".equals(item.category))continue;ContentValues stock=new ContentValues();stock.put("shop_id",id);stock.put("catalog_id",item.id);stock.put("quantity",2+Math.abs(item.id.hashCode()%4));stock.put("base_price",Math.max(1,item.value));stock.put("level",item.level);db.insertWithOnConflict("shop_stock",null,stock,SQLiteDatabase.CONFLICT_IGNORE);if(++added>=14)break;}}
    private static void seedFaction(SQLiteDatabase db,String id,String name,int influence,String relation,int treasury,int territory,int tension){ContentValues v=new ContentValues();v.put("id",id);v.put("name",name);v.put("influence",influence);v.put("relation",relation);v.put("treasury",treasury);v.put("territory",territory);v.put("tension",tension);db.insertWithOnConflict("factions",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private static void relation(SQLiteDatabase db,String a,String b,int standing,int tension,String treaty){ContentValues v=new ContentValues();v.put("faction_a",a);v.put("faction_b",b);v.put("standing",standing);v.put("tension",tension);v.put("treaty",treaty);db.insertWithOnConflict("faction_relations",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private static void settlement(SQLiteDatabase db,String id,String name,String region,String faction,int prosperity,int security,int autonomy,String rival){ContentValues v=new ContentValues();v.put("id",id);v.put("name",name);v.put("region",region);v.put("faction",faction);v.put("prosperity",prosperity);v.put("security",security);v.put("autonomy",autonomy);v.put("rival_id",rival);db.insertWithOnConflict("settlements",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private static void economy(SQLiteDatabase db,String id,String label,String region,int supply,int demand,int index){ContentValues v=new ContentValues();v.put("id",id);v.put("label",label);v.put("region",region);v.put("supply",supply);v.put("demand",demand);v.put("price_index",index);db.insertWithOnConflict("economy",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private static void seedRecipe(SQLiteDatabase db,String id,String name,String result,String ingredient,int qty,int fee){ContentValues v=new ContentValues();v.put("id",id);v.put("name",name);v.put("result_catalog_id",result);v.put("ingredient_catalog_id",ingredient);v.put("ingredient_qty",qty);v.put("fee",fee);db.insertWithOnConflict("recipes",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private static void seedLockedRecipe(SQLiteDatabase db,String id,String name,String result,String ingredient,int qty,int fee){ContentValues v=new ContentValues();v.put("id",id);v.put("name",name);v.put("result_catalog_id",result);v.put("ingredient_catalog_id",ingredient);v.put("ingredient_qty",qty);v.put("fee",fee);v.put("unlocked",0);db.insertWithOnConflict("recipes",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private static void business(SQLiteDatabase db,String id,String name,String type,String location,int revenue,int upkeep,int price){ContentValues v=new ContentValues();v.put("id",id);v.put("name",name);v.put("type",type);v.put("location",location);v.put("revenue",revenue);v.put("upkeep",upkeep);v.put("price",price);db.insertWithOnConflict("businesses",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private static void companion(SQLiteDatabase db,String id,String npc,String name,String role,String perk){ContentValues v=new ContentValues();v.put("id",id);v.put("npc_id",npc);v.put("name",name);v.put("role",role);v.put("perk",perk);db.insertWithOnConflict("companions",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private static void seedTalent(SQLiteDatabase db,String id){ContentValues v=new ContentValues();v.put("id",id);db.insertWithOnConflict("talents",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private static void seedLocation(SQLiteDatabase db,String id,String name,String region,int danger,int x,int y,boolean discovered){ContentValues v=new ContentValues();v.put("id",id);v.put("name",name);v.put("region",region);v.put("danger",danger);v.put("x",x);v.put("y",y);v.put("discovered",discovered?1:0);db.insertWithOnConflict("locations",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private static boolean scheduleAvailable(String service,long minute){long hour=(minute%1440)/60;if(service.contains("guard"))return true;if(service.contains("inn"))return hour>=6||hour<2;return hour>=7&&hour<22;}
    private static void removeFirst(JSONArray array){JSONArray copy=new JSONArray();for(int i=1;i<array.length();i++)copy.put(array.opt(i));while(array.length()>0)array.remove(array.length()-1);for(int i=0;i<copy.length();i++)array.put(copy.opt(i));}
    private static String compact(String value,int max){String result=value==null?"":value.trim().replaceAll("\\s+"," ");return result.length()>max?result.substring(0,max-1)+"…":result;}
    private static String norm(String value){return value==null?"":value.toLowerCase(Locale.forLanguageTag("lt-LT"));}
    private static int clamp(int value,int min,int max){return Math.max(min,Math.min(max,value));}
    private static String signed(int value){return value>=0?"+"+value:String.valueOf(value);}
}
