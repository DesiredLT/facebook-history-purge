package lt.vaeloria.ooc;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

/**
 * Autoritetinga riba tarp pasakojimo modelio ir vietinės žaidimo būsenos.
 * Modelis gali pasiūlyti sceną, tačiau skaitinės pasekmės, kelionė, grobis ir
 * kova priimami tik pagal telefono patikrą bei žinomus katalogus.
 */
final class AiTurnPolicyV101 {
    private AiTurnPolicyV101() {}

    static JSONObject sanitize(JSONObject raw,GameState state,StatEngine.Check check,String action,
                               Set<String> knownLocations,boolean authoritativeCombat){
        if(raw==null)raw=new JSONObject();
        if(state==null)state=new GameState();
        if(authoritativeCombat)return raw;
        if(knownLocations==null)knownLocations=Collections.emptySet();
        try{
            JSONObject out=new JSONObject(raw.toString());
            boolean failed=isFailure(check);
            String event=canonicalEvent(out.optString("event_tag","none"));
            if(event.startsWith("combat"))event="none";
            if(failed)event="setback";
            out.put("event_tag",event);

            String requested=out.optString("location",state.location);
            String canonical=canonicalLocation(requested,knownLocations);
            boolean explicitTravel=!failed&&!canonical.isEmpty()&&!canonical.equalsIgnoreCase(state.location)
                    &&isTravelIntent(action)&&mentions(action,canonical);
            out.put("location",explicitTravel?canonical:state.location);
            out.put("time_minutes",clamp(out.optInt("time_minutes",0),0,240));

            int hp=clamp(out.optInt("hp_delta",0),-35,35);
            int mana=clamp(out.optInt("mana_delta",0),-35,35);
            int stamina=clamp(out.optInt("stamina_delta",0),-35,35);
            int aeonic=clamp(out.optInt("aeonic_delta",0),-120,120);
            long crowns=clamp(out.optLong("crowns_delta",0),-5000L,5000L);
            boolean restorative=isRestorativeIntent(action)||"reward".equals(event);
            if(failed||!restorative){hp=Math.min(0,hp);mana=Math.min(0,mana);stamina=Math.min(0,stamina);aeonic=Math.min(0,aeonic);}
            if(failed||!"reward".equals(event))crowns=Math.min(0,crowns);
            out.put("hp_delta",hp).put("mana_delta",mana).put("stamina_delta",stamina)
                    .put("aeonic_delta",aeonic).put("crowns_delta",crowns);

            boolean factionEvent="social".equals(event)||"reward".equals(event);
            out.put("asterra_delta",failed||!factionEvent?0:clamp(out.optInt("asterra_delta",0),-5,5));
            out.put("dravenn_delta",failed||!factionEvent?0:clamp(out.optInt("dravenn_delta",0),-5,5));
            out.put("lysara_delta",failed||!factionEvent?0:clamp(out.optInt("lysara_delta",0),-5,5));
            out.put("quest_note",state.objective);

            JSONArray safeLoot=new JSONArray();
            if(!failed&&("reward".equals(event)||"discovery".equals(event))){
                JSONArray supplied=out.optJSONArray("loot");
                if(supplied!=null)for(int i=0;i<supplied.length()&&safeLoot.length()<3;i++){
                    JSONObject candidate=supplied.optJSONObject(i);if(candidate==null)continue;
                    ItemCatalogV092.ItemDef item=exactItem(candidate.optString("name",""));
                    if(item==null)continue;
                    safeLoot.put(new JSONObject().put("name",item.name).put("category",item.category)
                            .put("rarity",item.rarity).put("description",item.description));
                }
            }
            out.put("loot",safeLoot);

            out.put("combat_active",false).put("enemy_name","").put("enemy_status","")
                    .put("enemy_telegraph","").put("combat_distance","mid").put("combat_hazard","")
                    .put("enemy_hp",0).put("enemy_hp_max",0).put("combat_round",0);
            if(failed){
                String stat=check==null||check.primary==null?"savybės":check.primary;
                String consequence=check!=null&&check.outcome!=null&&check.outcome.contains("rimta")
                        ?"Situacija pablogėja, todėl dabartinis tikslas nepasistūmėja."
                        :"Dabartinis tikslas nepasistūmėja, tačiau gali rinktis kitą pagrįstą veiksmą.";
                out.put("scene_title","Bandymas nepavyko");
                out.put("scene","Telefono patikra parodo, kad „"+stat+"“ šiam bandymui nepakanka. "+consequence);
            }
            return out;
        }catch(Exception ignored){
            return safeFallback(state,check);
        }
    }

    static boolean isFailure(StatEngine.Check check){
        if(check==null||check.outcome==null)return false;
        return "nesėkmė".equals(check.outcome)||"rimta nesėkmė".equals(check.outcome);
    }

    private static JSONObject safeFallback(GameState state,StatEngine.Check check){
        try{return new JSONObject().put("scene_title","Veiksmas sustabdytas")
                .put("scene","Telefono variklis atmetė nepatvirtintą būsenos pakeitimą. Gali pasirinkti kitą veiksmą.")
                .put("choices",new JSONArray().put("Apsidairyti ir įvertinti padėtį").put("Patikrinti įrangą ir užrašus").put("Grįžti prie dabartinio tikslo"))
                .put("location",state.location).put("time_minutes",0).put("hp_delta",0).put("mana_delta",0)
                .put("stamina_delta",0).put("aeonic_delta",0).put("crowns_delta",0).put("quest_note",state.objective)
                .put("asterra_delta",0).put("dravenn_delta",0).put("lysara_delta",0)
                .put("event_tag",isFailure(check)?"setback":"none").put("combat_active",false)
                .put("enemy_name","").put("enemy_status","").put("enemy_telegraph","").put("combat_distance","mid")
                .put("combat_hazard","").put("enemy_hp",0).put("enemy_hp_max",0).put("combat_round",0).put("loot",new JSONArray());
        }catch(Exception impossible){return new JSONObject();}
    }

    private static ItemCatalogV092.ItemDef exactItem(String name){
        if(name==null)return null;String clean=name.trim();
        for(ItemCatalogV092.ItemDef item:ItemCatalogV092.ALL)if(item.name.equalsIgnoreCase(clean))return item;
        return null;
    }

    private static String canonicalLocation(String requested,Set<String> known){
        if(requested!=null)for(String location:known)if(location.equalsIgnoreCase(requested.trim()))return location;
        return "";
    }
    private static boolean isTravelIntent(String action){return contains(action,"keliaut","vykti","eiti į","eiti i","važiuoti","vaziuoti");}
    private static boolean isRestorativeIntent(String action){return contains(action,"poils","mieg","stovykl","gyd","potion","eliksyr","atsigauti","atkurti");}
    private static boolean mentions(String action,String value){return !norm(value).isEmpty()&&norm(action).contains(norm(value));}
    private static boolean contains(String value,String...needles){String text=norm(value);for(String needle:needles)if(text.contains(norm(needle)))return true;return false;}
    private static String norm(String value){return value==null?"":value.toLowerCase(Locale.forLanguageTag("lt-LT"));}
    private static String canonicalEvent(String value){
        if("dialogue".equals(value))return"social";if("action".equals(value))return"none";
        for(String allowed:new String[]{"none","combat","combat_victory","combat_escape","discovery","social","travel","reward","setback","rest"})if(allowed.equals(value))return value;
        return"none";
    }
    private static int clamp(int value,int min,int max){return Math.max(min,Math.min(max,value));}
    private static long clamp(long value,long min,long max){return Math.max(min,Math.min(max,value));}
}
