package lt.vaeloria.ooc;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Collections;

/** Vietinis deterministinis kovos variklis. DI gali aprašyti pasaulį, bet nekeičia kovos matematikos. */
final class CombatEngine {
    static boolean handles(GameState state, String action) {
        return state != null && (state.combatActive || isCombatIntent(action));
    }

    static JSONObject resolve(GameState state, String action, StatEngine.Check check,
                              EquipmentRules.Stats gear, Map<String,Integer> stats) {
        return resolve(state,action,check,gear,stats,Collections.emptySet(),0,0);
    }

    static JSONObject resolve(GameState state, String action, StatEngine.Check check,
                              EquipmentRules.Stats gear, Map<String,Integer> stats,Set<String> talents,
                              int companionDefense,int companionHealing) {
        try {
            if (!state.combatActive) return begin(state, action);
            hydrate(state);
            return turn(state, action, check, gear == null ? new EquipmentRules.Stats() : gear, stats,
                    talents==null?Collections.emptySet():talents,companionDefense,companionHealing);
        } catch (Exception error) {
            return safeFailure(state, error);
        }
    }

    static void hydrate(GameState state) {
        if (state == null || !state.combatActive) return;
        EnemyCatalogV091.Enemy enemy = EnemyCatalogV091.find(state.enemyName);
        if (enemy == null) return;
        if (state.enemyHpMax <= 0) { state.enemyHpMax = enemy.hp; state.enemyHp = enemy.hp; }
        if (state.enemyAttack <= 0) state.enemyAttack = enemy.attack;
        if (state.enemyDefense <= 0) state.enemyDefense = enemy.defense;
        if (state.enemySpeed <= 0) state.enemySpeed = enemy.speed;
        if (state.enemyDanger <= 0) state.enemyDanger = enemy.danger;
        if (state.enemyRole.isEmpty()) state.enemyRole = enemy.role;
        if (state.enemyTrait.isEmpty()) state.enemyTrait = enemy.trait;
    }

    private static JSONObject begin(GameState state, String action) throws Exception {
        EnemyCatalogV091.Enemy requested = EnemyCatalogV091.find(action);
        EnemyCatalogV091.Enemy enemy = requested != null ? requested
                : EnemyCatalogV091.encounterFor(state.location, state.worldMinute + norm(action).hashCode());
        JSONArray choices = new JSONArray()
                .put("Smogti tikslia pagrindinio ginklo ataka")
                .put("Užimti gynybinę poziciją ir perskaityti ketinimą")
                .put("Panaudoti kovinį gebėjimą");
        return base(state, "Kova prasideda",
                "Tu inicijuoji kontaktą, tačiau " + enemy.name + " iškart persitvarko kovai. "
                        + "Priešo " + enemy.role.toLowerCase(Locale.forLanguageTag("lt-LT"))
                        + " elgsena ir bruožas „" + enemy.trait + "“ nulems jo kitą veiksmą.",
                choices, "combat", true)
                .put("time_minutes", 2).put("stamina_delta", -4)
                .put("enemy_name", enemy.name).put("enemy_status", "Budrus · gyvybė " + enemy.hp + "/" + enemy.hp)
                .put("enemy_telegraph", telegraph(enemy, 1, enemy.hp, enemy.hp))
                .put("combat_distance", openingDistance(enemy)).put("combat_hazard", hazardFor(enemy.region))
                .put("enemy_hp", enemy.hp).put("enemy_hp_max", enemy.hp)
                .put("enemy_attack", enemy.attack).put("enemy_defense", enemy.defense)
                .put("enemy_speed", enemy.speed).put("enemy_danger", enemy.danger)
                .put("enemy_role", enemy.role).put("enemy_trait", enemy.trait)
                .put("player_combat_status", "Pasiruošęs").put("enemy_combat_effects", "")
                .put("enemy_effect_turns",0)
                .put("player_guard", 0).put("combat_ability_cooldown", 0).put("combat_combo", 0).put("combat_spell_count", 0)
                .put("combat_heavy_mitigation_used",false).put("combat_dawn_barrier_used",false)
                .put("combat_cheat_death_used",false).put("combat_last_stand_used",false)
                .put("combat_round", 1);
    }

    private static JSONObject turn(GameState state, String action, StatEngine.Check check,
                                   EquipmentRules.Stats gear, Map<String,Integer> stats,Set<String> talents,
                                   int companionDefense,int companionHealing) throws Exception {
        String query = norm(action);
        EnemyCatalogV091.Enemy catalog = EnemyCatalogV091.find(state.enemyName);
        int round = Math.max(1, state.combatRound) + 1;
        Random random = new Random(state.worldMinute * 31L + state.turnNumber * 131L + round * 17L + query.hashCode());
        boolean escape = contains(query, "pabėg", "pabeg", "trauktis", "nutraukti kov", "pasitraukti");
        boolean defend = contains(query, "gint", "blok", "skyd", "pariru", "atremti", "gynybin");
        boolean dodge = contains(query, "išsisuk", "issisuk", "išveng", "isveng", "šokti į šalį", "sokti i sali");
        boolean spell = contains(query, "gebėj", "gebej", "burt", "magij", "eonin", "relikv", "runa");
        boolean heavy = contains(query, "sunk", "galing", "visa jėga", "visa jega", "pramuš", "pramus");
        boolean recover = contains(query,"atsikvėp","atsikvep","atgauti kvap","taupyti jėg","taupyti jeg");

        if(talents.contains("scout_predator"))gear.criticalChance=Math.min(75,gear.criticalChance+10);

        int playerSpeed = stat(stats, "Greitis", 45) / 3 + stat(stats, "Reakcijos greitis", 45) / 4 + gear.speed+state.temporarySpeedBonus+(talents.contains("scout_step")?7:0)+(talents.contains("scout_predator")?12:0);
        int playerDefense = stat(stats, "Gynyba", 45) / 2 + gear.defense+state.temporaryDefenseBonus+companionDefense+(talents.contains("warrior_guard")&&state.combatRound<=1?8:0)+(talents.contains("warrior_unbroken")?8:0)+(gear.ambushProtection&&state.combatRound<=1?10:0);
        int playerAttack = stat(stats, "Ginklų valdymas", 45) / 3 + stat(stats, "Atakos tikslumas", 45) / 4 + gear.attack+state.temporaryAttackBonus+(talents.contains("warrior_edge")?6:0);
        if(talents.contains("scout_ambush")&&state.combatRound<=1)playerAttack+=12;
        if(talents.contains("warrior_counter")&&state.playerCombatStatus.contains("Kontratakos langas"))playerAttack+=10;
        int enemyHp = state.enemyHp;
        int hpDelta = 0, manaDelta = 0, staminaDelta = 0, aeonicDelta = 0;
        int dealt = 0, received = 0, guard = 0;
        int cooldown = Math.max(0, state.combatAbilityCooldown - 1);
        int combo = state.combatCombo;
        int spellCount = state.combatSpellCount;
        boolean heavyMitigationUsed=state.combatHeavyMitigationUsed;
        boolean dawnBarrierUsed=state.combatDawnBarrierUsed;
        boolean cheatDeathUsed=state.combatCheatDeathUsed;
        boolean lastStandUsed=state.combatLastStandUsed;
        String playerStatus = "Kovoja";
        String enemyEffects = state.enemyCombatEffects;
        int enemyEffectTurns=state.enemyEffectTurns;
        StringBuilder scene = new StringBuilder();

        if(!recover&&escape&&state.stamina<5){escape=false;recover=true;scene.append("Ištvermės nepakanka saugiam atsitraukimui. ");}
        if(!recover&&dodge&&state.stamina<6){dodge=false;recover=true;scene.append("Ištvermės nepakanka išsisukimui. ");}
        if(!recover&&!escape&&!defend&&!dodge&&!spell&&heavy&&state.stamina<15){
            if(state.stamina>=7){heavy=false;scene.append("Ištvermės nepakanka sunkiam smūgiui, todėl renkiesi kontroliuojamą ataką. ");}
            else{recover=true;scene.append("Ištvermės nepakanka atakai. ");}
        }
        if(!recover&&!escape&&!defend&&!dodge&&!spell&&!heavy&&state.stamina<7){recover=true;scene.append("Ištvermės nepakanka atakai. ");}

        if(recover){
            int restored=Math.max(8,Math.min(18,state.staminaMax/8));
            staminaDelta+=restored;
            guard=Math.max(4,playerDefense/4);
            playerStatus="Atgauna kvapą · apsauga "+guard;
            scene.append("Sutvirtini poziciją, sureguliuoji kvėpavimą ir atgauni ištvermę. ");
        } else if (escape) {
            int chance = 48 + (playerSpeed - state.enemySpeed) / 2 + outcomeBonus(check)+(talents.contains("scout_escape")?20:0);
            if (random.nextInt(100) < clamp(chance, 15, 90)) {
                return base(state, "Sėkmingas atsitraukimas",
                        "Pasirenki saugų tarpą ir nutrauki kontaktą. Priešas trumpai seka, tačiau tavo maršruto neperima, todėl kova baigiasi be pergalės ir be grobio.",
                        new JSONArray().put("Atsigauti po kovos").put("Stebėti, ar priešas neseka").put("Grįžti prie užduoties"),
                        "combat_escape", false).put("time_minutes", 5).put("stamina_delta", -5);
            }
            scene.append("Bandai nutraukti kontaktą, bet priešas užkerta saugų kelią. ");
            playerStatus = "Atsitraukimas sutrukdytas";
            staminaDelta -= 5;
        } else if (defend) {
            guard = Math.max(8, playerDefense / 2 + outcomeBonus(check));
            staminaDelta -= Math.min(3,state.stamina);
            playerStatus = "Gynybinė pozicija · apsauga " + guard+(talents.contains("warrior_counter")||gear.blockCounter?" · Kontratakos langas":"");
            scene.append("Užimi gynybinę poziciją ir smūgį pasitinki pasiruošęs. ");
        } else if (dodge) {
            int chance = 55 + (playerSpeed - state.enemySpeed) / 2 + outcomeBonus(check);
            if (random.nextInt(100) < clamp(chance, 10, 92)) {
                guard = 999;
                combo = gear.dodgeEmpowersAttack ? Math.max(combo, 2) : combo;
                playerStatus = gear.dodgeEmpowersAttack ? "Išsisukta · kita ataka sustiprinta" : "Išsisukta";
                scene.append("Perskaitai priešo judesį ir visiškai pasitrauki iš smūgio trajektorijos. ");
            } else {
                guard = Math.max(3, playerDefense / 5);
                playerStatus = "Pavėluotas išsisukimas";
                scene.append("Pradedi išsisukimą, tačiau priešas pakoreguoja smūgio kryptį. ");
            }
            staminaDelta -= 6;
        } else if (spell) {
            int cost = 18;
            if(talents.contains("arcane_efficiency"))cost=Math.max(4,cost-4);
            if (gear.thirdSpellDiscount && (spellCount + 1) % 3 == 0) cost = 8;
            if (cooldown > 0) {
                scene.append("Kovinis gebėjimas dar neatsistatė, todėl pereini į paprastą puolimą. ");
                spell = false;
            } else if (state.mana >= cost || state.aeonic >= cost) {
                if (state.mana >= cost) manaDelta -= cost; else aeonicDelta -= cost;
                int magic = stat(stats, "Burtų galia", 45) / 2 + stat(stats, "Manos kontrolė", 45) / 4 + gear.magicPower+state.temporaryMagicBonus+(talents.contains("arcane_overflow")?15:0);
                if(talents.contains("arcane_echo")&&(spellCount+1)%3==0)magic+=18;
                dealt = damage(magic + 22, effectiveEnemyDefense(state.enemyDefense,enemyEffects,enemyEffectTurns), outcomeBonus(check), random, gear, state, true);
                enemyEffects = magicEffect(query, catalog == null ? state.enemyTrait : catalog.trait);
                enemyEffectTurns=3;
                cooldown = 2;
                spellCount++;
                scene.append("Sukoncentruoji energiją ir tiksliai paleidi kovinį gebėjimą. ");
            } else {
                scene.append("Energijos nepakanka gebėjimui, todėl smūgiuoji ginklu. ");
                spell = false;
            }
        }

        if (!recover && !escape && !defend && !dodge && !spell) {
            int attack = playerAttack + (heavy ? 18 : 0);
            if (heavy) staminaDelta -= 15; else staminaDelta -= 7;
            if (gear.berserk) attack += Math.max(0, (state.hpMax - state.hp) / 6);
            if (combo >= 2 && gear.dodgeEmpowersAttack) { attack += 18; combo = 0; }
            dealt = damage(attack, effectiveEnemyDefense(state.enemyDefense,enemyEffects,enemyEffectTurns), outcomeBonus(check), random, gear, state, false);
            combo++;
            scene.append(heavy ? "Sutelkęs jėgą mėgini pralaužti priešo gynybą. " : "Smūgiuoji kontroliuojamai ir saugai savo poziciją. ");
        }

        int effectDamage = enemyEffectTurns>0?damageOverTime(enemyEffects, state.enemyDanger):0;
        if (effectDamage > 0) { dealt += effectDamage; scene.append("Ankstesnis poveikis toliau žeidžia priešą. "); }
        enemyHp = Math.max(0, enemyHp - dealt);
        if (enemyHp == 0) {
            int heal = (gear.postCombatRegeneration?12:0)+Math.max(0,companionHealing);
            heal=Math.min(heal,state.hpMax-state.hp);
            int victoryStamina=staminaDelta+(talents.contains("leader_rally")?10:0);
            return base(state, "Priešas nugalėtas",
                    scene + "Paskutinis poveikis pralaužia gynybą. " + state.enemyName + " nebegali tęsti kovos; vietinis variklis dabar pritaiko jo grobio lentelę.",
                    new JSONArray().put("Apžiūrėti grobį").put("Atsigauti po kovos").put("Tęsti kelionę"),
                    "combat_victory", false).put("time_minutes", 4).put("hp_delta", heal)
                    .put("stamina_delta", victoryStamina).put("mana_delta", manaDelta).put("aeonic_delta", aeonicDelta);
        }

        int rawIncoming = enemyDamage(state, playerDefense, random);
        String normalizedEnemyEffect=norm(enemyEffects);
        if(enemyEffectTurns>0&&(normalizedEnemyEffect.contains("sulėt")||normalizedEnemyEffect.contains("sutrik")||normalizedEnemyEffect.contains("apak")))rawIncoming=Math.max(0,rawIncoming*65/100);
        if (guard >= 999) received = 0;
        else received = Math.max(0, rawIncoming - guard);
        if (gear.heavyHitMitigation && received >= 20 && !heavyMitigationUsed) {
            received = Math.max(1, received / 2);
            heavyMitigationUsed=true;
            playerStatus = playerStatus + " · Priesaikos apsauga panaudota";
        }
        if (gear.dawnBarrier && state.hp - received <= Math.max(1, state.hpMax / 4)
                && !dawnBarrierUsed) {
            received = Math.max(0, received / 3);
            dawnBarrierUsed=true;
            playerStatus = playerStatus + " · Aušros barjeras panaudotas";
        }
        int statusTick = playerStatusDamage(state.playerCombatStatus, gear, state, state.enemyDanger);
        received += statusTick;
        if (state.hp - received <= 0 && gear.cheatDeath && !cheatDeathUsed) {
            received = Math.max(0, state.hp - 1);
            cheatDeathUsed=true;
            playerStatus = playerStatus + " · Kapų apsauga panaudota";
        }
        if(state.hp-received<=0&&talents.contains("warrior_last_stand")&&!lastStandUsed){
            received=Math.max(0,state.hp-1);lastStandUsed=true;playerStatus=playerStatus+" · Paskutinis bastionas panaudotas";
        }
        hpDelta -= received;
        if(enemyEffectTurns>0){enemyEffectTurns--;if(enemyEffectTurns==0)enemyEffects="";}
        if (received == 0) scene.append("Priešo atsakas tavęs nepasiekia. ");
        else scene.append("Priešo atsakas padaro ").append(received).append(" žalos. ");
        if (state.hp + hpDelta <= 0) {
            playerStatus = "Kritiškai sužeistas";
            scene.append("Prarandi gebėjimą tęsti kovą; atsitraukimas įvyksta su rimta pasekme.");
            return base(state, "Pralaimėta kova", scene.toString(),
                    new JSONArray().put("Atsigauti saugioje vietoje").put("Įvertinti prarastą laiką").put("Keisti pasiruošimą"),
                    "setback", false).put("time_minutes", 180).put("hp_delta", 1 - state.hp)
                    .put("stamina_delta", -state.stamina).put("crowns_delta", -Math.min(state.crowns, 50L * Math.max(1, state.enemyDanger)));
        }

        EnemyCatalogV091.Enemy enemy = catalog;
        String telegraph = enemy == null ? genericTelegraph(state, round) : telegraph(enemy, round, enemyHp, state.enemyHpMax);
        JSONArray choices = combatChoices(telegraph, cooldown,state.stamina+staminaDelta);
        return base(state, "Kovos " + round + " ėjimas", scene.toString(), choices, "combat", true)
                .put("time_minutes", 2).put("hp_delta", hpDelta).put("mana_delta", manaDelta)
                .put("stamina_delta", staminaDelta).put("aeonic_delta", aeonicDelta)
                .put("enemy_name", state.enemyName)
                .put("enemy_status", statusLine(enemyHp, state.enemyHpMax, enemyEffects))
                .put("enemy_telegraph", telegraph).put("combat_distance", nextDistance(query, state.combatDistance))
                .put("combat_hazard", state.combatHazard).put("enemy_hp", enemyHp).put("enemy_hp_max", state.enemyHpMax)
                .put("enemy_attack", state.enemyAttack).put("enemy_defense", state.enemyDefense)
                .put("enemy_speed", state.enemySpeed).put("enemy_danger", state.enemyDanger)
                .put("enemy_role", state.enemyRole).put("enemy_trait", state.enemyTrait)
                .put("player_combat_status", playerStatus).put("enemy_combat_effects", enemyEffects)
                .put("enemy_effect_turns",enemyEffectTurns)
                .put("player_guard", guard).put("combat_ability_cooldown", cooldown).put("combat_combo", combo).put("combat_spell_count",spellCount)
                .put("combat_heavy_mitigation_used",heavyMitigationUsed).put("combat_dawn_barrier_used",dawnBarrierUsed)
                .put("combat_cheat_death_used",cheatDeathUsed).put("combat_last_stand_used",lastStandUsed)
                .put("combat_round", round);
    }

    private static int damage(int attack, int defense, int checkBonus, Random random,
                              EquipmentRules.Stats gear, GameState state, boolean magic) {
        int variance = random.nextInt(11) - 5;
        int value = attack + checkBonus + variance - Math.round(defense * (magic ? .28f : .42f));
        boolean critical = random.nextInt(100) < gear.criticalChance + state.temporaryCriticalBonus + Math.max(0, checkBonus / 3);
        if (critical) value += Math.max(5, Math.round(value * gear.criticalDamage / 100f));
        return Math.max(3, value);
    }

    private static int effectiveEnemyDefense(int defense,String effects,int turns){return turns>0&&norm(effects).contains("šarvai suardyti")?Math.max(0,defense-18):defense;}

    private static int enemyDamage(GameState state, int defense, Random random) {
        float difficulty = difficulty(state.difficulty);
        int value = Math.round((state.enemyAttack + random.nextInt(9) - 4 - defense * .24f) * difficulty);
        if (state.enemySpeed > 80 && random.nextInt(100) < 18) value += Math.max(3, state.enemyAttack / 4);
        return Math.max(1, value);
    }

    private static int playerStatusDamage(String status, EquipmentRules.Stats gear, GameState state, int danger) {
        String value = norm(status);
        if (value.contains("nuod")) return Math.max(0, Math.round((3 + danger) * (100 - Math.min(90,gear.poisonResistance+state.temporaryPoisonResistance)) / 100f));
        if (value.contains("nekrot")) return Math.max(0, Math.round((4 + danger) * (100 - Math.min(90,gear.necroticResistance+state.temporaryNecroticResistance)) / 100f));
        if (value.contains("dega")) return 3 + danger / 2;
        return 0;
    }

    private static int damageOverTime(String effects, int danger) {
        String value = norm(effects);
        if (value.contains("dega")) return 4 + danger;
        if (value.contains("nuod")) return 3 + danger / 2;
        if (value.contains("krauju")) return 3 + danger / 3;
        return 0;
    }

    private static String magicEffect(String action, String trait) {
        if (contains(action, "ugn", "lieps", "žarij", "zarij")) return "Dega";
        if (contains(action, "nuod", "rūgšt", "rugst")) return "Apnuodytas";
        if (contains(action, "led", "šerkšn", "serksn")) return "Sulėtintas";
        if (contains(action, "žaib", "zaib", "perkūn", "perkun")) return "Sutrikdytas";
        return norm(trait).contains("atspar") ? "Rezonansas nestabilus" : "Rezonanso žaizda";
    }

    private static JSONArray combatChoices(String telegraph, int cooldown,int stamina) {
        JSONArray choices = new JSONArray();
        choices.put(stamina<7?"Atgauti kvapą ir saugoti poziciją":"Atakuoti ir išlaikyti spaudimą");
        choices.put(norm(telegraph).contains("sunk") || norm(telegraph).contains("smūg")
                ? "Blokuoti paruoštą sunkų smūgį" : "Išsisukti iš numatytos atakos");
        choices.put(cooldown == 0 ? "Panaudoti kovinį gebėjimą" : "Atsitraukti iš kovos");
        return choices;
    }

    private static JSONObject base(GameState state, String title, String scene, JSONArray choices,
                                   String event, boolean combat) throws Exception {
        return new JSONObject().put("scene_title", title).put("scene", scene).put("choices", choices)
                .put("location", state.location).put("time_minutes", 0).put("hp_delta", 0)
                .put("mana_delta", 0).put("stamina_delta", 0).put("aeonic_delta", 0).put("crowns_delta", 0)
                .put("quest_note", state.objective).put("asterra_delta", 0).put("dravenn_delta", 0).put("lysara_delta", 0)
                .put("event_tag", event).put("combat_active", combat).put("enemy_name", "")
                .put("enemy_status", "").put("enemy_telegraph", "").put("combat_distance", "mid")
                .put("combat_hazard", "").put("enemy_hp", 0).put("enemy_hp_max", 0).put("combat_round", 0)
                .put("loot", new JSONArray());
    }

    private static JSONObject safeFailure(GameState state, Exception error) {
        try {
            return base(state, "Kovos veiksmas sustabdytas",
                    "Vietinis kovos variklis neatliko nepatvirtinto būsenos pakeitimo. Gali pakartoti veiksmą arba sąmoningai atsitraukti.",
                    new JSONArray().put("Pakartoti ataką").put("Užimti gynybinę poziciją").put("Atsitraukti iš kovos"),
                    "setback", state.combatActive);
        } catch (Exception ignored) { return new JSONObject(); }
    }

    private static String statusLine(int hp, int max, String effects) {
        String base = hp <= max / 4 ? "Kritiškai sužeistas" : hp <= max / 2 ? "Sužeistas" : "Kovoja";
        return base + " · gyvybė " + hp + "/" + max + (effects == null || effects.isEmpty() ? "" : " · " + effects);
    }

    private static String telegraph(EnemyCatalogV091.Enemy enemy, int round, int hp, int max) {
        if (hp <= max / 3 && enemy.danger >= 8) return "Kaupia paskutinės fazės gebėjimą: " + enemy.trait;
        String role = norm(enemy.role);
        if (role.contains("burt") || role.contains("ritual")) return "Kaupia energiją gebėjimui: " + enemy.trait;
        if (role.contains("kontrol") || role.contains("spąst")) return "Bando apriboti judėjimą naudodamas: " + enemy.trait;
        if (role.contains("greit") || role.contains("pasal")) return "Ruošia greitą ataką iš šono";
        return round % 3 == 0 ? "Ruošia sunkų tiesioginį smūgį" : "Tikrina tavo gynybą ir ruošia kontrataką";
    }

    private static String genericTelegraph(GameState state, int round) {
        return state.enemyTrait.isEmpty() ? (round % 2 == 0 ? "Ruošia sunkų smūgį" : "Ruošia greitą kontrataką")
                : "Ruošiasi panaudoti: " + state.enemyTrait;
    }

    private static String openingDistance(EnemyCatalogV091.Enemy enemy) {
        String role = norm(enemy.role);
        return role.contains("skrend") || role.contains("burt") ? "far" : role.contains("greit") || role.contains("pasal") ? "close" : "mid";
    }

    private static String nextDistance(String action, String current) {
        if (contains(action, "lank", "tolim", "atsitrauk")) return "far";
        if (contains(action, "smog", "pulti", "ataku", "durkl", "imtyn")) return "close";
        return current == null || current.isEmpty() ? "mid" : current;
    }

    private static String hazardFor(String region) {
        String value = norm(region);
        if (value.contains("pelk") || value.contains("gir")) return "Klampi žemė ir nuodinga augmenija";
        if (value.contains("virš") || value.contains("audr")) return "Slidus akmuo ir stiprūs vėjo gūsiai";
        if (value.contains("tušt") || value.contains("dirbt")) return "Nestabilus rezonansas ir judančios kliūtys";
        if (value.contains("nekropol")) return "Pelenai mažina matomumą";
        return "Ribota erdvė ir nelygus pagrindas";
    }

    private static int outcomeBonus(StatEngine.Check check) {
        if (check == null || check.outcome == null) return 0;
        if (check.outcome.contains("išskirtinė")) return 22;
        if ("sėkmė".equals(check.outcome)) return 12;
        if (check.outcome.contains("dalinė")) return 4;
        if (check.outcome.contains("rimta")) return -18;
        return -8;
    }

    private static int stat(Map<String,Integer> stats, String name, int fallback) {
        return stats == null ? fallback : clamp(stats.getOrDefault(name, fallback), 0, 100);
    }

    private static float difficulty(String value) {
        if ("story".equals(value)) return .72f;
        if ("hard".equals(value)) return 1.22f;
        if ("nightmare".equals(value)) return 1.48f;
        return 1f;
    }

    private static boolean isCombatIntent(String action) {
        return contains(norm(action), "ataku", "ataka", "pulti", "puolu", "smūg", "smug", "pradėti kov", "pradeti kov", "kovoti");
    }

    private static boolean contains(String value, String... needles) {
        String normalized = norm(value);
        for (String needle : needles) if (normalized.contains(norm(needle))) return true;
        return false;
    }

    private static String norm(String value) { return value == null ? "" : value.toLowerCase(Locale.forLanguageTag("lt-LT")); }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private CombatEngine() {}
}
