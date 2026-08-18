package lt.vaeloria.ooc;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/** Deterministinė veikėjo lygių, patirties ir talentų mechanika. */
final class ProgressionEngine {
    static final class TalentDef {
        final String id, name, branch, description, prerequisite;
        final int requiredLevel, cost;

        TalentDef(String id,String name,String branch,String description,int requiredLevel,int cost,String prerequisite){
            this.id=id;this.name=name;this.branch=branch;this.description=description;
            this.requiredLevel=requiredLevel;this.cost=cost;this.prerequisite=prerequisite;
        }
    }

    static final class Award {
        final int gained, levels, talentPoints;
        Award(int gained,int levels,int talentPoints){this.gained=gained;this.levels=levels;this.talentPoints=talentPoints;}
        String line(){
            StringBuilder out=new StringBuilder("+").append(gained).append(" patirties");
            if(levels>0)out.append(" · pasiektas ").append(levels==1?"naujas lygis":levels+" nauji lygiai")
                    .append(" · +").append(talentPoints).append(" talentų tšk.");
            return out.toString();
        }
    }

    static final TalentDef[] TALENTS = {
            new TalentDef("warrior_guard","Tvirta pozicija","KARYS","Pirmame kovos ėjime gauni +8 apsaugos.",1,1,""),
            new TalentDef("warrior_edge","Tikslus ašmuo","KARYS","Fizinės atakos gauna +6 puolimo galios.",3,1,"warrior_guard"),
            new TalentDef("warrior_counter","Kontratakos langas","KARYS","Sėkminga gynyba sustiprina kitą ataką.",6,1,"warrior_edge"),
            new TalentDef("warrior_last_stand","Paskutinis bastionas","KARYS","Kartą per kovą mirtina žala palieka 1 gyvybę.",10,2,"warrior_counter"),

            new TalentDef("scout_eye","Tyrėjo akis","ŽVALGAS","+5 tyrimo, sekimo ir pavojaus patikroms.",1,1,""),
            new TalentDef("scout_step","Tylus žingsnis","ŽVALGAS","+7 greičio ir išsisukimo skaičiavimams.",3,1,"scout_eye"),
            new TalentDef("scout_ambush","Pasala","ŽVALGAS","Pirmoji puolamoji ataka gauna +12 galios.",6,1,"scout_step"),
            new TalentDef("scout_escape","Dingstantis kelias","ŽVALGAS","Atsitraukimo tikimybė padidėja 20 proc. punktų.",10,2,"scout_ambush"),

            new TalentDef("arcane_focus","Arkaninis fokusas","ARKANISTAS","+5 magijos ir Meridiano patikroms.",1,1,""),
            new TalentDef("arcane_reserve","Gilioji talpa","ARKANISTAS","Maksimali mana padidėja 20.",3,1,"arcane_focus"),
            new TalentDef("arcane_efficiency","Taupus pynimas","ARKANISTAS","Koviniai burtai kainuoja 4 mana mažiau.",6,1,"arcane_reserve"),
            new TalentDef("arcane_echo","Rezonanso aidas","ARKANISTAS","Kas trečias kovinis burtas gauna +18 galios.",10,2,"arcane_efficiency"),

            new TalentDef("leader_voice","Patikimas balsas","LYDERIS","+5 diplomatijos, derybų ir vadovavimo patikroms.",1,1,""),
            new TalentDef("leader_bond","Bendras tikslas","LYDERIS","Aktyvaus kompaniono lojalumas kyla greičiau.",3,1,"leader_voice"),
            new TalentDef("leader_trade","Sąžininga kaina","LYDERIS","Pirkimo kainos mažėja 8 %, pardavimo pajamos didėja 8 %.",6,1,"leader_bond"),
            new TalentDef("leader_rally","Sutelkti","LYDERIS","Po pergalės atkuriama 10 ištvermės.",10,2,"leader_trade"),

            new TalentDef("craft_lore","Amato žinios","MEISTRAS","+5 amatų, planavimo ir žinių pritaikymo patikroms.",1,1,""),
            new TalentDef("craft_saver","Medžiagų taupymas","MEISTRAS","Gamybos mokestis sumažėja 20 %.",3,1,"craft_lore"),
            new TalentDef("craft_quality","Tikslus grūdinimas","MEISTRAS","Pagamintas daiktas suteikia papildomą meistriškumo patirtį.",6,1,"craft_saver"),
            new TalentDef("craft_masterpiece","Meistro ženklas","MEISTRAS","Atrakina aukščiausios pakopos receptų grandinę.",10,2,"craft_quality")
    };

    static int experienceForNext(int level){
        int safe=Math.max(1,Math.min(99,level));
        return 250 + safe*100 + safe*safe*12;
    }

    static Award award(GameState state,String event,StatEngine.Check check,int enemyDanger){
        if(state==null)return new Award(0,0,0);
        String tag=event==null?"":event.toLowerCase(Locale.ROOT);
        int gained=12;
        if("discovery".equals(tag)||"social".equals(tag)||"reward".equals(tag))gained=28;
        else if("travel".equals(tag)||"rest".equals(tag))gained=10;
        else if("combat".equals(tag))gained=18+Math.max(0,enemyDanger)*2;
        else if("combat_victory".equals(tag))gained=55+Math.max(1,enemyDanger)*15;
        else if("setback".equals(tag)||"failure".equals(tag))gained=8;
        if(check!=null){
            if("išskirtinė sėkmė".equals(check.outcome))gained+=18;
            else if("sėkmė".equals(check.outcome))gained+=10;
            else if("dalinė sėkmė".equals(check.outcome))gained+=5;
        }
        if("story".equals(state.difficulty))gained=Math.max(1,Math.round(gained*.85f));
        if("hard".equals(state.difficulty))gained=Math.round(gained*1.12f);
        if("nightmare".equals(state.difficulty))gained=Math.round(gained*1.28f);
        if("legendary".equals(state.progressionMode))gained=Math.max(1,gained/2);

        state.experience+=gained;
        int levels=0,points=0;
        state.experienceNext=experienceForNext(state.level);
        while(state.level<100&&state.experience>=state.experienceNext){
            state.experience-=state.experienceNext;
            state.level++;
            levels++;
            int earned=state.level%5==0?2:1;
            state.talentPoints+=earned;points+=earned;
            state.experienceNext=experienceForNext(state.level);
            state.hpMax=Math.min(260,state.hpMax+4);state.hp=Math.min(state.hpMax,state.hp+4);
            state.staminaMax=Math.min(220,state.staminaMax+3);state.stamina=Math.min(state.staminaMax,state.stamina+3);
            if(state.manaMax>0){state.manaMax=Math.min(240,state.manaMax+2);state.mana=Math.min(state.manaMax,state.mana+2);}
        }
        if(state.level>=100){state.level=100;state.experience=0;state.experienceNext=1;}
        return new Award(gained,levels,points);
    }

    static TalentDef byId(String id){for(TalentDef value:TALENTS)if(value.id.equals(id))return value;return null;}
    static boolean has(Set<String> ids,String id){return ids!=null&&ids.contains(id);}

    static int checkBonus(Set<String> ids,String action,String stat){
        if(ids==null||ids.isEmpty())return 0;
        String text=((action==null?"":action)+" "+(stat==null?"":stat)).toLowerCase(Locale.forLanguageTag("lt-LT"));
        int result=0;
        if(has(ids,"scout_eye")&&contains(text,"tir","sek","pavoj","pastab","meridian"))result+=5;
        if(has(ids,"arcane_focus")&&contains(text,"mag","burt","mana","runa","relikv","meridian"))result+=5;
        if(has(ids,"leader_voice")&&contains(text,"diplom","deryb","vadov","įtikin","kalb","social"))result+=5;
        if(has(ids,"craft_lore")&&contains(text,"amat","gamin","plan","žini","taisy","kalv"))result+=5;
        return result;
    }

    static Set<String> immutable(Set<String> ids){return ids==null?Collections.emptySet():Collections.unmodifiableSet(new LinkedHashSet<>(ids));}
    static Set<String> set(String... ids){return new LinkedHashSet<>(Arrays.asList(ids));}
    private static boolean contains(String value,String... tokens){for(String token:tokens)if(value.contains(token))return true;return false;}
    private ProgressionEngine(){}
}
