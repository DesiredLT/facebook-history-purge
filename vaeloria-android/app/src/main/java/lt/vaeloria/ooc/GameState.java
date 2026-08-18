package lt.vaeloria.ooc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    public String characterName = "Einoras";
    public int chronologicalAge = 201;
    public int biologicalAge = 20;
    public boolean ageless = true;
    public boolean characterCreated = false;
    public String characterIdentity = "nenurodyta";
    public String characterOriginId = "";
    public String characterArchetypeId = "";
    public String characterAppearance = "";
    public final List<String> characterTraitIds = new ArrayList<>();
    public String location = "Luminara";
    public int worldYear = 923;
    public long worldMinute = 95358680L;
    public int hp = 100, hpMax = 100;
    public int mana = 0, manaMax = 100;
    public int stamina = 100, staminaMax = 100;
    public int aeonic = 180, aeonicMax = 900;
    public long crowns = 1_062_400L;
    public int asterraInfluence = 34, dravennInfluence = 72, lysaraInfluence = 24;
    public String asterraRelation = "NEUTRALI", dravennRelation = "ĮTAMPA", lysaraRelation = "SĄJUNGINĖ";
    public String questTitle = "Lūžęs Meridianas";
    public String objective = "Ištirti pirmą naują kelionės vartų poslinkio atvejį ir atskirti, kas žinoma, nuo to, kas tik numanoma.";
    public String sceneTitle = "Vėlyvieji keliai";
    public String scene = "Luminara gauna karavano manifestą anksčiau, negu į miestą atvyksta pats karavanas. Laiko neatitikimas sutampa su nauju kelionės vartų poslinkiu, todėl vien dokumento data jau yra lauko įrodymas, o ne gandas.";
    public boolean combatActive = false;
    public String enemyName = "";
    public String enemyStatus = "";
    public String enemyTelegraph = "";
    public String combatDistance = "mid";
    public String combatHazard = "";
    public int enemyHp = 0;
    public int enemyHpMax = 0;
    public int combatRound = 0;
    public final List<String> choices = new ArrayList<>();
    public final List<String> recentTurns = new ArrayList<>();

    public GameState() {
        choices.add("Ištirti manifestą dėl laiko ir Meridiano anomalijų");
        choices.add("Susisiekti su Lyra ir Kaeliu bei palyginti jų laiko stebėjimus");
        choices.add("Vykti tiesiai prie paveiktų kelionės vartų ir rinkti lauko įrodymus");
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("characterName", characterName);
        o.put("chronologicalAge", chronologicalAge);
        o.put("biologicalAge", biologicalAge);
        o.put("ageless", ageless);
        o.put("characterCreated", characterCreated);
        o.put("characterIdentity", characterIdentity);
        o.put("characterOriginId", characterOriginId);
        o.put("characterArchetypeId", characterArchetypeId);
        o.put("characterAppearance", characterAppearance);
        JSONArray traits = new JSONArray();
        for (String id : characterTraitIds) traits.put(id);
        o.put("characterTraitIds", traits);
        o.put("location", location);
        o.put("worldYear", worldYear);
        o.put("worldMinute", worldMinute);
        o.put("hp", hp); o.put("hpMax", hpMax);
        o.put("mana", mana); o.put("manaMax", manaMax);
        o.put("stamina", stamina); o.put("staminaMax", staminaMax);
        o.put("aeonic", aeonic); o.put("aeonicMax", aeonicMax);
        o.put("crowns", crowns);
        o.put("asterraInfluence", asterraInfluence); o.put("dravennInfluence", dravennInfluence); o.put("lysaraInfluence", lysaraInfluence);
        o.put("asterraRelation", asterraRelation); o.put("dravennRelation", dravennRelation); o.put("lysaraRelation", lysaraRelation);
        o.put("questTitle", questTitle);
        o.put("objective", objective);
        o.put("sceneTitle", sceneTitle);
        o.put("scene", scene);
        o.put("combatActive", combatActive);
        o.put("enemyName", enemyName);
        o.put("enemyStatus", enemyStatus);
        o.put("enemyTelegraph", enemyTelegraph);
        o.put("combatDistance", combatDistance);
        o.put("combatHazard", combatHazard);
        o.put("enemyHp", enemyHp);
        o.put("enemyHpMax", enemyHpMax);
        o.put("combatRound", combatRound);
        JSONArray c = new JSONArray();
        for (String s : choices) c.put(s);
        o.put("choices", c);
        JSONArray r = new JSONArray();
        for (String s : recentTurns) r.put(s);
        o.put("recentTurns", r);
        return o;
    }

    public static GameState fromJson(JSONObject o) throws JSONException {
        GameState s = new GameState();
        boolean legacyProfile = !o.has("characterCreated");
        s.characterName = o.optString("characterName", s.characterName);
        s.chronologicalAge = clamp(o.optInt("chronologicalAge", s.chronologicalAge), 16, 999);
        s.biologicalAge = clamp(o.optInt("biologicalAge", s.biologicalAge), 16, 999);
        s.ageless = o.optBoolean("ageless", s.ageless);
        s.characterCreated = legacyProfile ? o.has("characterName") : o.optBoolean("characterCreated", false);
        s.characterIdentity = o.optString("characterIdentity", "nenurodyta");
        s.characterOriginId = o.optString("characterOriginId", "");
        s.characterArchetypeId = o.optString("characterArchetypeId", "");
        s.characterAppearance = o.optString("characterAppearance", "");
        s.characterTraitIds.clear();
        JSONArray traits = o.optJSONArray("characterTraitIds");
        if (traits != null) for (int i=0;i<traits.length();i++) s.characterTraitIds.add(traits.optString(i));
        if (legacyProfile && s.characterCreated) {
            if (s.characterOriginId.isEmpty()) s.characterOriginId = "luminara";
            if (s.characterArchetypeId.isEmpty()) s.characterArchetypeId = "sargybinis";
            if (s.characterTraitIds.isEmpty()) {
                s.characterTraitIds.add("ryztas");
                s.characterTraitIds.add("pastabumas");
                s.characterTraitIds.add("drausme");
            }
        }
        s.location = o.optString("location", s.location);
        s.worldYear = o.optInt("worldYear", s.worldYear);
        s.worldMinute = o.optLong("worldMinute", s.worldMinute);
        s.hp = o.optInt("hp", s.hp); s.hpMax = o.optInt("hpMax", s.hpMax);
        s.mana = o.optInt("mana", s.mana); s.manaMax = o.optInt("manaMax", s.manaMax);
        s.stamina = o.optInt("stamina", s.stamina); s.staminaMax = o.optInt("staminaMax", s.staminaMax);
        s.aeonic = o.optInt("aeonic", s.aeonic); s.aeonicMax = o.optInt("aeonicMax", s.aeonicMax);
        s.crowns = o.optLong("crowns", s.crowns);
        s.asterraInfluence = clamp(o.optInt("asterraInfluence", s.asterraInfluence),0,100);
        s.dravennInfluence = clamp(o.optInt("dravennInfluence", s.dravennInfluence),0,100);
        s.lysaraInfluence = clamp(o.optInt("lysaraInfluence", s.lysaraInfluence),0,100);
        s.asterraRelation = o.optString("asterraRelation", s.asterraRelation);
        s.dravennRelation = o.optString("dravennRelation", s.dravennRelation);
        s.lysaraRelation = o.optString("lysaraRelation", s.lysaraRelation);
        s.questTitle = o.optString("questTitle", s.questTitle);
        s.objective = o.optString("objective", s.objective);
        s.sceneTitle = o.optString("sceneTitle", s.sceneTitle);
        s.scene = o.optString("scene", s.scene);
        s.combatActive = o.optBoolean("combatActive", false);
        s.enemyName = o.optString("enemyName", "");
        s.enemyStatus = o.optString("enemyStatus", "");
        s.enemyTelegraph = o.optString("enemyTelegraph", "");
        s.combatDistance = o.optString("combatDistance", "mid");
        s.combatHazard = o.optString("combatHazard", "");
        s.enemyHp = Math.max(0, o.optInt("enemyHp", 0));
        s.enemyHpMax = Math.max(s.enemyHp, o.optInt("enemyHpMax", s.enemyHp));
        s.combatRound = Math.max(0, o.optInt("combatRound", 0));
        s.choices.clear();
        JSONArray c = o.optJSONArray("choices");
        if (c != null) for (int i=0;i<c.length();i++) s.choices.add(c.optString(i));
        while (s.choices.size() < 3) s.choices.add("Apsidairyti ir surinkti daugiau informacijos");
        s.recentTurns.clear();
        JSONArray r = o.optJSONArray("recentTurns");
        if (r != null) for (int i=0;i<r.length();i++) s.recentTurns.add(r.optString(i));
        return s;
    }

    public void applyTurn(JSONObject result) {
        sceneTitle = result.optString("scene_title", sceneTitle);
        scene = result.optString("scene", scene);
        location = result.optString("location", location);
        int minutes = result.optInt("time_minutes", 0);
        if (minutes > 0) worldMinute += minutes;
        hp = clamp(hp + result.optInt("hp_delta", 0), 0, hpMax);
        mana = clamp(mana + result.optInt("mana_delta", 0), 0, manaMax);
        stamina = clamp(stamina + result.optInt("stamina_delta", 0), 0, staminaMax);
        aeonic = clamp(aeonic + result.optInt("aeonic_delta", 0), 0, aeonicMax);
        crowns = Math.max(0, crowns + result.optLong("crowns_delta", 0));
        asterraInfluence = clamp(asterraInfluence + result.optInt("asterra_delta",0),0,100);
        dravennInfluence = clamp(dravennInfluence + result.optInt("dravenn_delta",0),0,100);
        lysaraInfluence = clamp(lysaraInfluence + result.optInt("lysara_delta",0),0,100);
        asterraRelation = result.optString("asterra_relation", relationFor(asterraInfluence,false));
        dravennRelation = result.optString("dravenn_relation", relationFor(dravennInfluence,true));
        lysaraRelation = result.optString("lysara_relation", relationFor(lysaraInfluence,false));
        combatActive = result.optBoolean("combat_active", combatActive);
        enemyName = result.optString("enemy_name", combatActive ? enemyName : "");
        enemyStatus = result.optString("enemy_status", combatActive ? enemyStatus : "");
        enemyTelegraph = result.optString("enemy_telegraph", combatActive ? enemyTelegraph : "");
        combatDistance = result.optString("combat_distance", combatActive ? combatDistance : "mid");
        combatHazard = result.optString("combat_hazard", combatActive ? combatHazard : "");
        enemyHp = Math.max(0, result.optInt("enemy_hp", combatActive ? enemyHp : 0));
        enemyHpMax = Math.max(enemyHp, result.optInt("enemy_hp_max", combatActive ? enemyHpMax : 0));
        combatRound = Math.max(0, result.optInt("combat_round", combatActive ? combatRound : 0));
        if (!combatActive) endCombat();
        String note = result.optString("quest_note", "").trim();
        if (!note.isEmpty()) objective = note;
        choices.clear();
        JSONArray c = result.optJSONArray("choices");
        if (c != null) for (int i=0;i<c.length() && i<3;i++) choices.add(c.optString(i));
        while (choices.size() < 3) choices.add("Stebėti situaciją ir rinkti įrodymus");
    }

    public void endCombat() {
        combatActive=false;enemyName="";enemyStatus="";enemyTelegraph="";combatHazard="";
        combatDistance="mid";enemyHp=0;enemyHpMax=0;combatRound=0;
    }

    private static String relationFor(int v,boolean hostile){ if(hostile)return v>=70?"ĮTAMPA":v>=45?"ATSARGI":"NEUTRALI"; return v>=70?"SĄJUNGINĖ":v>=45?"PALANKI":"NEUTRALI"; }
    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
}
