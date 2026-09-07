package lt.vaeloria.ooc;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Final boundary for concise, readable Lithuanian player-facing narrative. */
final class LithuanianNarrative {
    private static final String[][] TERMS = {
            {"The Broken Meridian", "Lūžęs Meridianas"},
            {"Waygate Drift", "kelionės vartų poslinkis"},
            {"waygates", "kelionės vartai"}, {"waygate", "kelionės vartai"},
            {"gear", "įranga"}, {"loot", "grobis"}, {"combat", "kova"},
            {"discovery", "atradimas"}, {"quest", "užduotis"},
            {"story thread", "siužeto gija"}, {"thread", "siužeto gija"},
            {"potion", "eliksyras"}, {"level", "lygis"}, {"item", "daiktas"}
    };
    private static final Set<String> ENGLISH_MARKERS = new HashSet<>();
    static {
        for(String marker:new String[]{"the","you","your","with","from","this","that","then","enemy","attack","attacks","find","finds","found","move","moves","toward","towards","while","because","however","choose","choice"}) ENGLISH_MARKERS.add(marker);
    }

    static JSONObject polish(JSONObject result,GameState state){
        JSONObject out=result==null?new JSONObject():result;
        try{
            out.put("scene_title",clean(out.optString("scene_title","Veiksmo pasekmė"),"Veiksmo pasekmė",false,90));
            String fallback="Atlieki pasirinktą veiksmą. Pasaulis į jį atsako, o situacija aiškiai pasistūmėja pirmyn.";
            out.put("scene",clean(out.optString("scene",fallback),fallback,true,900));
            out.put("location",clean(out.optString("location",state==null?"Luminara":state.location),state==null?"Luminara":state.location,false,80));
            out.put("quest_note",clean(out.optString("quest_note",""),"",true,280));
            out.put("enemy_name",clean(out.optString("enemy_name",""),"",false,64));
            out.put("enemy_status",clean(out.optString("enemy_status",""),"",false,60));
            out.put("enemy_telegraph",clean(out.optString("enemy_telegraph",""),"",false,100));
            out.put("combat_hazard",clean(out.optString("combat_hazard",""),"",false,70));
            out.put("choices",polishChoices(out.optJSONArray("choices"),state!=null&&state.combatActive));
            JSONArray loot=out.optJSONArray("loot");
            if(loot!=null)for(int i=0;i<loot.length();i++){
                JSONObject item=loot.optJSONObject(i);if(item==null)continue;
                item.put("name",clean(item.optString("name","Nežinomas radinys"),"Nežinomas radinys",false,90));
                item.put("description",clean(item.optString("description",""),"",true,240));
            }
        }catch(Exception ignored){}
        return out;
    }

    static void polishState(GameState state){
        if(state==null)return;
        state.questTitle=clean(state.questTitle,"Lūžęs Meridianas",false,100);
        state.objective=clean(state.objective,"Ištirti Meridiano poslinkį.",true,320);
        state.sceneTitle=clean(state.sceneTitle,"Vėlyvieji keliai",false,100);
        state.scene=clean(state.scene,"Apsidairai ir įvertini padėtį.",true,1000);
        state.location=clean(state.location,"Luminara",false,80);
        state.enemyName=clean(state.enemyName,"",false,64);
        state.enemyStatus=clean(state.enemyStatus,"",false,60);
        state.enemyTelegraph=clean(state.enemyTelegraph,"",false,100);
        state.combatHazard=clean(state.combatHazard,"",false,70);
        JSONArray source=new JSONArray();for(String choice:state.choices)source.put(choice);
        JSONArray choices=polishChoices(source,state.combatActive);state.choices.clear();
        for(int i=0;i<choices.length();i++)state.choices.add(choices.optString(i));
        for(int i=0;i<state.recentTurns.size();i++)state.recentTurns.set(i,clean(state.recentTurns.get(i),"Ankstesnis ėjimas",false,240));
    }

    static String polishTextForTest(String value){return clean(value,"",true,1000);}

    private static JSONArray polishChoices(JSONArray raw,boolean combat){
        String[] fallback=combat
                ?new String[]{"Įvertinti priešo ketinimą ir gintis","Atakuoti iš palankesnės pozicijos","Atsitraukti ir ieškoti saugaus kelio"}
                :new String[]{"Apsidairyti ir surinkti daugiau informacijos","Pasikalbėti su artimiausiu liudininku","Tęsti pagrindinės užduoties tyrimą"};
        ArrayList<String> values=new ArrayList<>();HashSet<String> seen=new HashSet<>();
        if(raw!=null)for(int i=0;i<raw.length()&&values.size()<3;i++){
            String choice=clean(raw.optString(i,""),"",false,150);
            String key=choice.toLowerCase(Locale.forLanguageTag("lt-LT"));
            if(!choice.isEmpty()&&seen.add(key))values.add(choice);
        }
        for(String choice:fallback)if(values.size()<3&&seen.add(choice.toLowerCase(Locale.forLanguageTag("lt-LT"))))values.add(choice);
        JSONArray out=new JSONArray();for(String value:values)out.put(value);return out;
    }

    private static String clean(String raw,String fallback,boolean sentence,int max){
        String value=raw==null?"":raw;
        value=value.replace('\r',' ').replace('`',' ')
                .replace("**","").replace("__","")
                .replaceAll("(?m)^\\s*(?:#{1,6}\\s*|[-*+]\\s+|\\d+[.)]\\s+)","")
                .replaceAll("\\s+"," ").replaceAll("\\s+([,.;:!?])","$1").trim();
        value=value.replaceAll("(?iu)\\bcombat\\s+begins\\b","Prasideda kova")
                .replaceAll("(?iu)\\baction\\s+resolved\\b","Veiksmas išspręstas");
        for(String[] term:TERMS)value=replaceTerm(value,term[0],term[1]);
        if(clearlyEnglish(value))value=fallback;
        if(value.isEmpty())value=fallback==null?"":fallback;
        value=capitalize(value);
        if(value.length()>max)value=value.substring(0,Math.max(1,max-1)).trim()+"…";
        if(sentence&&!value.isEmpty()&&!value.endsWith(".")&&!value.endsWith("!")&&!value.endsWith("?")&&!value.endsWith("…"))value+=".";
        return value;
    }

    private static String replaceTerm(String text,String source,String replacement){
        Pattern pattern=Pattern.compile("(?iu)(?<![\\p{L}\\p{N}_])"+Pattern.quote(source)+"(?![\\p{L}\\p{N}_])");
        return pattern.matcher(text).replaceAll(Matcher.quoteReplacement(replacement));
    }

    static boolean clearlyEnglish(String text){
        if(text==null||text.isEmpty())return false;int count=0;
        String[] words=text.toLowerCase(Locale.ROOT).replaceAll("[^a-z ]"," ").split("\\s+");
        for(String word:words)if(ENGLISH_MARKERS.contains(word)&&++count>=4)return true;
        return false;
    }

    private static String capitalize(String value){
        if(value==null||value.isEmpty())return "";
        int first=value.offsetByCodePoints(0,1);
        return value.substring(0,first).toUpperCase(Locale.forLanguageTag("lt-LT"))+value.substring(first);
    }

    private LithuanianNarrative(){}
}
