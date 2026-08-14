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
    public String location = "Luminara";
    public int worldYear = 923;
    public long worldMinute = 95358680L;
    public int hp = 100, hpMax = 100;
    public int mana = 0, manaMax = 100;
    public int stamina = 100, staminaMax = 100;
    public int aeonic = 180, aeonicMax = 900;
    public long crowns = 1_062_400L;
    public String questTitle = "The Broken Meridian";
    public String objective = "Investigate the first fresh waygate drift incident and establish what is known versus merely assumed.";
    public String sceneTitle = "The Late Roads";
    public String scene = "Luminara gauna karavano manifestą anksčiau, negu į miestą atvyksta pats karavanas. Laiko neatitikimas sutampa su nauju Waygate Drift incidentu, todėl vien dokumento data jau yra lauko įrodymas, o ne gandas.";
    public boolean combatActive = false;
    public String enemyName = "";
    public String enemyStatus = "";
    public String enemyTelegraph = "";
    public String combatDistance = "mid";
    public String combatHazard = "";
    public final List<String> choices = new ArrayList<>();
    public final List<String> recentTurns = new ArrayList<>();

    public GameState() {
        choices.add("Ištirti manifestą dėl laiko ir meridiano anomalijų");
        choices.add("Susisiekti su Mira ir Kaelis bei palyginti jų laiko stebėjimus");
        choices.add("Vykti tiesiai prie paveikto waygate ir rinkti lauko įrodymus");
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("characterName", characterName);
        o.put("chronologicalAge", chronologicalAge);
        o.put("biologicalAge", biologicalAge);
        o.put("ageless", ageless);
        o.put("location", location);
        o.put("worldYear", worldYear);
        o.put("worldMinute", worldMinute);
        o.put("hp", hp); o.put("hpMax", hpMax);
        o.put("mana", mana); o.put("manaMax", manaMax);
        o.put("stamina", stamina); o.put("staminaMax", staminaMax);
        o.put("aeonic", aeonic); o.put("aeonicMax", aeonicMax);
        o.put("crowns", crowns);
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
        s.characterName = o.optString("characterName", s.characterName);
        s.chronologicalAge = o.optInt("chronologicalAge", s.chronologicalAge);
        s.biologicalAge = o.optInt("biologicalAge", s.biologicalAge);
        s.ageless = o.optBoolean("ageless", s.ageless);
        s.location = o.optString("location", s.location);
        s.worldYear = o.optInt("worldYear", s.worldYear);
        s.worldMinute = o.optLong("worldMinute", s.worldMinute);
        s.hp = o.optInt("hp", s.hp); s.hpMax = o.optInt("hpMax", s.hpMax);
        s.mana = o.optInt("mana", s.mana); s.manaMax = o.optInt("manaMax", s.manaMax);
        s.stamina = o.optInt("stamina", s.stamina); s.staminaMax = o.optInt("staminaMax", s.staminaMax);
        s.aeonic = o.optInt("aeonic", s.aeonic); s.aeonicMax = o.optInt("aeonicMax", s.aeonicMax);
        s.crowns = o.optLong("crowns", s.crowns);
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
        combatActive = result.optBoolean("combat_active", combatActive);
        enemyName = result.optString("enemy_name", combatActive ? enemyName : "");
        enemyStatus = result.optString("enemy_status", combatActive ? enemyStatus : "");
        enemyTelegraph = result.optString("enemy_telegraph", combatActive ? enemyTelegraph : "");
        combatDistance = result.optString("combat_distance", combatActive ? combatDistance : "mid");
        combatHazard = result.optString("combat_hazard", combatActive ? combatHazard : "");
        if (!combatActive) { enemyName=""; enemyStatus=""; enemyTelegraph=""; combatHazard=""; combatDistance="mid"; }
        String note = result.optString("quest_note", "").trim();
        if (!note.isEmpty()) objective = note;
        choices.clear();
        JSONArray c = result.optJSONArray("choices");
        if (c != null) for (int i=0;i<c.length() && i<3;i++) choices.add(c.optString(i));
        while (choices.size() < 3) choices.add("Stebėti situaciją ir rinkti įrodymus");
    }

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
}
