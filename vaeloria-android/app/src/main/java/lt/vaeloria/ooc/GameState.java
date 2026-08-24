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
    public int level = 1, experience = 0, experienceNext = 350, talentPoints = 1, attributePoints = 0;
    public String temporaryEffectName = "";
    public int temporaryEffectTurns = 0;
    public int temporaryAttackBonus = 0, temporaryDefenseBonus = 0, temporarySpeedBonus = 0;
    public int temporaryMagicBonus = 0, temporaryCheckBonus = 0, temporaryCriticalBonus = 0;
    public int temporaryPoisonResistance = 0, temporaryNecroticResistance = 0;
    public int equipmentHpBonus = 0, equipmentManaBonus = 0;
    public String difficulty = "normal";
    public String progressionMode = "balanced";
    public long turnNumber = 0L;
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
    public int enemyAttack = 0;
    public int enemyDefense = 0;
    public int enemySpeed = 0;
    public int enemyDanger = 0;
    public String enemyRole = "";
    public String enemyTrait = "";
    public String playerCombatStatus = "";
    public String enemyCombatEffects = "";
    public int enemyEffectTurns = 0;
    public int playerGuard = 0;
    public int combatAbilityCooldown = 0;
    public int combatCombo = 0;
    public int combatSpellCount = 0;
    public int combatRound = 0;
    public boolean combatHeavyMitigationUsed = false;
    public boolean combatDawnBarrierUsed = false;
    public boolean combatCheatDeathUsed = false;
    public boolean combatLastStandUsed = false;
    public String storyEnding = "";
    public boolean tutorialComplete = false;
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
        o.put("level", level); o.put("experience", experience); o.put("experienceNext", experienceNext); o.put("talentPoints", talentPoints); o.put("attributePoints", attributePoints);
        o.put("temporaryEffectName", temporaryEffectName); o.put("temporaryEffectTurns", temporaryEffectTurns);
        o.put("temporaryAttackBonus", temporaryAttackBonus); o.put("temporaryDefenseBonus", temporaryDefenseBonus); o.put("temporarySpeedBonus", temporarySpeedBonus);
        o.put("temporaryMagicBonus", temporaryMagicBonus); o.put("temporaryCheckBonus", temporaryCheckBonus); o.put("temporaryCriticalBonus", temporaryCriticalBonus);
        o.put("temporaryPoisonResistance", temporaryPoisonResistance); o.put("temporaryNecroticResistance", temporaryNecroticResistance);
        o.put("equipmentHpBonus", equipmentHpBonus); o.put("equipmentManaBonus", equipmentManaBonus);
        o.put("difficulty", difficulty); o.put("progressionMode", progressionMode); o.put("turnNumber", turnNumber);
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
        o.put("enemyAttack", enemyAttack);
        o.put("enemyDefense", enemyDefense);
        o.put("enemySpeed", enemySpeed);
        o.put("enemyDanger", enemyDanger);
        o.put("enemyRole", enemyRole);
        o.put("enemyTrait", enemyTrait);
        o.put("playerCombatStatus", playerCombatStatus);
        o.put("enemyCombatEffects", enemyCombatEffects);
        o.put("enemyEffectTurns", enemyEffectTurns);
        o.put("playerGuard", playerGuard);
        o.put("combatAbilityCooldown", combatAbilityCooldown);
        o.put("combatCombo", combatCombo);
        o.put("combatSpellCount", combatSpellCount);
        o.put("combatRound", combatRound);
        o.put("combatHeavyMitigationUsed", combatHeavyMitigationUsed);
        o.put("combatDawnBarrierUsed", combatDawnBarrierUsed);
        o.put("combatCheatDeathUsed", combatCheatDeathUsed);
        o.put("combatLastStandUsed", combatLastStandUsed);
        o.put("storyEnding", storyEnding);
        o.put("tutorialComplete", tutorialComplete);
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
        s.characterName = compact(o.optString("characterName", s.characterName),32);
        s.chronologicalAge = clamp(o.optInt("chronologicalAge", s.chronologicalAge), 16, 999);
        s.biologicalAge = clamp(o.optInt("biologicalAge", s.biologicalAge), 16, 999);
        s.ageless = o.optBoolean("ageless", s.ageless);
        s.characterCreated = legacyProfile ? o.has("characterName") : o.optBoolean("characterCreated", false);
        s.characterIdentity = o.optString("characterIdentity", "nenurodyta");
        s.characterOriginId = o.optString("characterOriginId", "");
        s.characterArchetypeId = o.optString("characterArchetypeId", "");
        s.characterAppearance = compact(o.optString("characterAppearance", ""),500);
        s.characterTraitIds.clear();
        JSONArray traits = o.optJSONArray("characterTraitIds");
        if (traits != null) for (int i=0;i<traits.length()&&i<12;i++) s.characterTraitIds.add(traits.optString(i));
        if (legacyProfile && s.characterCreated) {
            if (s.characterOriginId.isEmpty()) s.characterOriginId = "luminara";
            if (s.characterArchetypeId.isEmpty()) s.characterArchetypeId = "sargybinis";
            if (s.characterTraitIds.isEmpty()) {
                s.characterTraitIds.add("ryztas");
                s.characterTraitIds.add("pastabumas");
                s.characterTraitIds.add("drausme");
            }
        }
        s.location = compact(o.optString("location", s.location),80);
        s.worldYear = clamp(o.optInt("worldYear", s.worldYear),0,9999);
        s.worldMinute = Math.max(0L,o.optLong("worldMinute", s.worldMinute));
        s.hpMax = clamp(o.optInt("hpMax", s.hpMax),1,10000);s.hp = clamp(o.optInt("hp", s.hp),0,s.hpMax);
        s.manaMax = clamp(o.optInt("manaMax", s.manaMax),0,10000);s.mana = clamp(o.optInt("mana", s.mana),0,s.manaMax);
        s.staminaMax = clamp(o.optInt("staminaMax", s.staminaMax),1,10000);s.stamina = clamp(o.optInt("stamina", s.stamina),0,s.staminaMax);
        s.aeonicMax = clamp(o.optInt("aeonicMax", s.aeonicMax),0,10000);s.aeonic = clamp(o.optInt("aeonic", s.aeonic),0,s.aeonicMax);
        s.crowns = Math.max(0L,Math.min(1_000_000_000L,o.optLong("crowns", s.crowns)));
        s.level = clamp(o.optInt("level", s.level), 1, 100);
        s.experience = Math.max(0, o.optInt("experience", s.experience));
        s.experienceNext = Math.max(100, o.optInt("experienceNext", s.experienceNext));
        s.talentPoints = Math.max(0, o.optInt("talentPoints", s.talentPoints));
        s.attributePoints = o.has("attributePoints")
                ? clamp(o.optInt("attributePoints", 0), 0, 1000)
                : ProgressionEngine.attributePointsThroughLevel(s.level);
        s.temporaryEffectName = compact(o.optString("temporaryEffectName", ""),120);
        s.temporaryEffectTurns = clamp(o.optInt("temporaryEffectTurns", 0),0,100);
        s.temporaryAttackBonus = clamp(o.optInt("temporaryAttackBonus", 0),-100,100);
        s.temporaryDefenseBonus = clamp(o.optInt("temporaryDefenseBonus", 0),-100,100);
        s.temporarySpeedBonus = clamp(o.optInt("temporarySpeedBonus", 0),-100,100);
        s.temporaryMagicBonus = clamp(o.optInt("temporaryMagicBonus", 0),-100,100);
        s.temporaryCheckBonus = clamp(o.optInt("temporaryCheckBonus", 0),-100,100);
        s.temporaryCriticalBonus = clamp(o.optInt("temporaryCriticalBonus", 0),-100,100);
        s.temporaryPoisonResistance = clamp(o.optInt("temporaryPoisonResistance", 0),0,90);
        s.temporaryNecroticResistance = clamp(o.optInt("temporaryNecroticResistance", 0),0,90);
        s.equipmentHpBonus = clamp(o.optInt("equipmentHpBonus", 0),0,1000);
        s.equipmentManaBonus = clamp(o.optInt("equipmentManaBonus", 0),0,1000);
        if (s.temporaryEffectTurns == 0) s.clearTemporaryEffect();
        s.difficulty = validDifficulty(o.optString("difficulty", s.difficulty));
        s.progressionMode = validProgression(o.optString("progressionMode", s.progressionMode));
        s.turnNumber = Math.max(0L, o.optLong("turnNumber", s.turnNumber));
        s.asterraInfluence = clamp(o.optInt("asterraInfluence", s.asterraInfluence),0,100);
        s.dravennInfluence = clamp(o.optInt("dravennInfluence", s.dravennInfluence),0,100);
        s.lysaraInfluence = clamp(o.optInt("lysaraInfluence", s.lysaraInfluence),0,100);
        s.asterraRelation = o.optString("asterraRelation", s.asterraRelation);
        s.dravennRelation = o.optString("dravennRelation", s.dravennRelation);
        s.lysaraRelation = o.optString("lysaraRelation", s.lysaraRelation);
        s.questTitle = compact(o.optString("questTitle", s.questTitle),160);
        s.objective = compact(o.optString("objective", s.objective),500);
        s.sceneTitle = compact(o.optString("sceneTitle", s.sceneTitle),160);
        s.scene = compact(o.optString("scene", s.scene),4000);
        s.combatActive = o.optBoolean("combatActive", false);
        s.enemyName = o.optString("enemyName", "");
        s.enemyStatus = o.optString("enemyStatus", "");
        s.enemyTelegraph = o.optString("enemyTelegraph", "");
        String distance=o.optString("combatDistance", "mid");s.combatDistance="close".equals(distance)||"far".equals(distance)?distance:"mid";
        s.combatHazard = o.optString("combatHazard", "");
        s.enemyHp = Math.max(0, o.optInt("enemyHp", 0));
        s.enemyHpMax = Math.max(s.enemyHp, o.optInt("enemyHpMax", s.enemyHp));
        s.enemyAttack = Math.max(0, o.optInt("enemyAttack", 0));
        s.enemyDefense = Math.max(0, o.optInt("enemyDefense", 0));
        s.enemySpeed = Math.max(0, o.optInt("enemySpeed", 0));
        s.enemyDanger = clamp(o.optInt("enemyDanger", 0), 0, 10);
        s.enemyRole = o.optString("enemyRole", "");
        s.enemyTrait = o.optString("enemyTrait", "");
        s.playerCombatStatus = o.optString("playerCombatStatus", "");
        s.enemyCombatEffects = o.optString("enemyCombatEffects", "");
        s.enemyEffectTurns = clamp(o.optInt("enemyEffectTurns", 0),0,20);
        s.playerGuard = Math.max(0, o.optInt("playerGuard", 0));
        s.combatAbilityCooldown = Math.max(0, o.optInt("combatAbilityCooldown", 0));
        s.combatCombo = Math.max(0, o.optInt("combatCombo", 0));
        s.combatSpellCount = Math.max(0, o.optInt("combatSpellCount", 0));
        s.combatRound = Math.max(0, o.optInt("combatRound", 0));
        s.combatHeavyMitigationUsed = o.optBoolean("combatHeavyMitigationUsed", false);
        s.combatDawnBarrierUsed = o.optBoolean("combatDawnBarrierUsed", false);
        s.combatCheatDeathUsed = o.optBoolean("combatCheatDeathUsed", false);
        s.combatLastStandUsed = o.optBoolean("combatLastStandUsed", false);
        s.storyEnding = o.optString("storyEnding", "");
        s.tutorialComplete = o.optBoolean("tutorialComplete", false);
        s.choices.clear();
        JSONArray c = o.optJSONArray("choices");
        if (c != null) for (int i=0;i<c.length()&&i<3;i++) s.choices.add(compact(c.optString(i),240));
        while (s.choices.size() < 3) s.choices.add("Apsidairyti ir surinkti daugiau informacijos");
        s.recentTurns.clear();
        JSONArray r = o.optJSONArray("recentTurns");
        if (r != null) for (int i=Math.max(0,r.length()-30);i<r.length();i++) s.recentTurns.add(compact(r.optString(i),500));
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
        turnNumber++;
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
        enemyAttack = Math.max(0, result.optInt("enemy_attack", combatActive ? enemyAttack : 0));
        enemyDefense = Math.max(0, result.optInt("enemy_defense", combatActive ? enemyDefense : 0));
        enemySpeed = Math.max(0, result.optInt("enemy_speed", combatActive ? enemySpeed : 0));
        enemyDanger = clamp(result.optInt("enemy_danger", combatActive ? enemyDanger : 0), 0, 10);
        enemyRole = result.optString("enemy_role", combatActive ? enemyRole : "");
        enemyTrait = result.optString("enemy_trait", combatActive ? enemyTrait : "");
        playerCombatStatus = result.optString("player_combat_status", combatActive ? playerCombatStatus : "");
        enemyCombatEffects = result.optString("enemy_combat_effects", combatActive ? enemyCombatEffects : "");
        enemyEffectTurns = clamp(result.optInt("enemy_effect_turns", combatActive ? enemyEffectTurns : 0),0,20);
        playerGuard = Math.max(0, result.optInt("player_guard", combatActive ? playerGuard : 0));
        combatAbilityCooldown = Math.max(0, result.optInt("combat_ability_cooldown", combatActive ? combatAbilityCooldown : 0));
        combatCombo = Math.max(0, result.optInt("combat_combo", combatActive ? combatCombo : 0));
        combatSpellCount = Math.max(0, result.optInt("combat_spell_count", combatActive ? combatSpellCount : 0));
        combatRound = Math.max(0, result.optInt("combat_round", combatActive ? combatRound : 0));
        combatHeavyMitigationUsed = result.optBoolean("combat_heavy_mitigation_used", combatActive && combatHeavyMitigationUsed);
        combatDawnBarrierUsed = result.optBoolean("combat_dawn_barrier_used", combatActive && combatDawnBarrierUsed);
        combatCheatDeathUsed = result.optBoolean("combat_cheat_death_used", combatActive && combatCheatDeathUsed);
        combatLastStandUsed = result.optBoolean("combat_last_stand_used", combatActive && combatLastStandUsed);
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
        combatDistance="mid";enemyHp=0;enemyHpMax=0;enemyAttack=0;enemyDefense=0;enemySpeed=0;enemyDanger=0;
        enemyRole="";enemyTrait="";playerCombatStatus="";enemyCombatEffects="";enemyEffectTurns=0;playerGuard=0;combatAbilityCooldown=0;combatCombo=0;combatSpellCount=0;combatRound=0;
        combatHeavyMitigationUsed=false;combatDawnBarrierUsed=false;combatCheatDeathUsed=false;combatLastStandUsed=false;
    }

    public void applyTemporaryEffect(ConsumableRulesV110.Effect effect) {
        if (effect == null || !effect.hasBuff()) return;
        temporaryEffectName = compact(effect.name,120);
        temporaryEffectTurns = clamp(effect.turns,1,100);
        temporaryAttackBonus = clamp(effect.attack,-100,100);
        temporaryDefenseBonus = clamp(effect.defense,-100,100);
        temporarySpeedBonus = clamp(effect.speed,-100,100);
        temporaryMagicBonus = clamp(effect.magic,-100,100);
        temporaryCheckBonus = clamp(effect.check,-100,100);
        temporaryCriticalBonus = clamp(effect.critical,-100,100);
        temporaryPoisonResistance = clamp(effect.poisonResistance,0,90);
        temporaryNecroticResistance = clamp(effect.necroticResistance,0,90);
    }

    public void tickTemporaryEffect() {
        if (temporaryEffectTurns <= 0) { clearTemporaryEffect(); return; }
        temporaryEffectTurns--;
        if (temporaryEffectTurns == 0) clearTemporaryEffect();
    }

    public void clearTemporaryEffect() {
        temporaryEffectName="";temporaryEffectTurns=0;temporaryAttackBonus=0;temporaryDefenseBonus=0;
        temporarySpeedBonus=0;temporaryMagicBonus=0;temporaryCheckBonus=0;temporaryCriticalBonus=0;
        temporaryPoisonResistance=0;temporaryNecroticResistance=0;
    }

    private static String relationFor(int v,boolean hostile){ if(hostile)return v>=70?"ĮTAMPA":v>=45?"ATSARGI":"NEUTRALI"; return v>=70?"SĄJUNGINĖ":v>=45?"PALANKI":"NEUTRALI"; }
    private static String validDifficulty(String value){return "story".equals(value)||"hard".equals(value)||"nightmare".equals(value)?value:"normal";}
    private static String validProgression(String value){return "legendary".equals(value)?value:"balanced";}
    private static String compact(String value,int max){String clean=value==null?"":value.trim().replaceAll("\\s+"," ");return clean.length()<=max?clean:clean.substring(0,max);}
    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
}
