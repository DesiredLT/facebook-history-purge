package lt.vaeloria.ooc;

import java.util.Locale;

/** Struktūrizuota potionų, maisto ir kovos reikmenų mechanika. */
final class ConsumableRulesV110 {
    static final class Effect {
        String name = "";
        String enemyEffect = "";
        int hp, mana, stamina, aeonic;
        int directDamage, enemyEffectTurns, guard;
        int attack, defense, speed, magic, check, critical;
        int poisonResistance, necroticResistance, turns;
        boolean requiresCombat, usableInCombat = true, escape, cleanse;

        boolean hasRestoration() { return hp > 0 || mana > 0 || stamina > 0 || aeonic > 0; }
        boolean hasBuff() {
            return turns > 0 && (attack != 0 || defense != 0 || speed != 0 || magic != 0
                    || check != 0 || critical != 0 || poisonResistance != 0 || necroticResistance != 0);
        }
    }

    static Effect effect(ItemCatalogV092.ItemDef item) {
        Effect out = new Effect();
        if (item == null) return out;
        out.name = item.name;
        out.hp = item.hpRestore;
        out.mana = item.manaRestore;
        out.stamina = item.staminaRestore;
        out.aeonic = item.aeonicRestore;
        String id = item.id == null ? "" : item.id;
        String name = lower(item.name);

        if ("food".equals(item.category)) {
            out.usableInCombat = false;
            out.name = "Pasistiprinęs · " + item.name;
            out.defense = Math.max(2, item.power / 8);
            out.check = Math.max(1, item.power / 15);
            out.turns = 3;
            return out;
        }

        if ("potion".equals(item.category)) {
            potion(out, id, name, item.power);
            return out;
        }

        if ("combat_consumable".equals(item.category)) {
            out.requiresCombat = true;
            out.directDamage = 8 + Math.max(0, item.power);
            out.enemyEffectTurns = 1;
            if (contains(name, "liepsn", "ugn")) {
                out.enemyEffect = "Dega"; out.enemyEffectTurns = 3;
            } else if (contains(name, "šerkšn", "serksn", "šalčio", "salcio")) {
                out.enemyEffect = "Sulėtintas"; out.enemyEffectTurns = 2; out.guard = 10;
            } else if (contains(name, "perkūn", "perkun", "žaib", "zaib")) {
                out.enemyEffect = "Sutrikdytas"; out.enemyEffectTurns = 2;
            } else if (contains(name, "nuod")) {
                out.enemyEffect = "Apnuodytas"; out.enemyEffectTurns = 4;
            } else if (contains(name, "dūm", "dum")) {
                out.directDamage = 0; out.guard = 999; out.enemyEffect = "Apakintas"; out.enemyEffectTurns = 1;
            } else if (contains(name, "šventinto", "sventinto")) {
                out.enemyEffect = "Pašventintas nudegimas"; out.enemyEffectTurns = 2;
            } else if (contains(name, "rūgšt", "rugst")) {
                out.enemyEffect = "Šarvai suardyti"; out.enemyEffectTurns = 3;
            } else if (contains(name, "peili")) {
                out.enemyEffect = "Kraujuoja"; out.enemyEffectTurns = 3;
            } else if (contains(name, "ež", "ez", "spąst", "spast")) {
                out.enemyEffect = "Įkalintas"; out.enemyEffectTurns = 2; out.guard = 14;
            } else if (contains(name, "perkėlimo", "perkelimo")) {
                out.directDamage = 0; out.escape = true; out.guard = 999;
            } else if (contains(name, "atpažinimo", "atpazinimo")) {
                out.directDamage = 0; out.guard = 18; out.name = "Atpažinta silpnoji vieta";
                out.check = 12; out.critical = 10; out.turns = 3;
            } else if (contains(name, "prikėlimo", "prikelimo")) {
                out.directDamage = 0; out.hp = Math.max(out.hp, 55); out.guard = 22;
                out.name = "Prikėlimo apsauga"; out.defense = 18; out.turns = 3;
            } else if (contains(name, "matmenų", "matmenu")) {
                out.directDamage = 35 + item.power; out.enemyEffect = "Erdvė suardyta";
                out.enemyEffectTurns = 3; out.guard = 20;
            }
        }
        return out;
    }

    static String description(ItemCatalogV092.ItemDef item) {
        Effect effect = effect(item);
        StringBuilder out = new StringBuilder();
        append(out, effect.hp, "gyvybės");
        append(out, effect.mana, "manos");
        append(out, effect.stamina, "ištvermės");
        append(out, effect.aeonic, "eoninės energijos");
        if (effect.directDamage > 0) append(out, effect.directDamage, "tiesioginės žalos");
        if (!effect.enemyEffect.isEmpty()) add(out, effect.enemyEffect + " · " + effect.enemyEffectTurns + " ėj.");
        if (effect.guard >= 999) add(out, "išvengiamas kitas atsakas");
        else if (effect.guard > 0) add(out, "+" + effect.guard + " apsaugos šiam ėjimui");
        if (effect.escape) add(out, "saugiai nutraukia kovą");
        if (effect.cleanse) add(out, "pašalina žalingą būseną");
        if (effect.hasBuff()) {
            StringBuilder buff = new StringBuilder(effect.turns + " ėj.: ");
            appendSigned(buff, effect.attack, "puolimui");
            appendSigned(buff, effect.defense, "gynybai");
            appendSigned(buff, effect.speed, "greičiui");
            appendSigned(buff, effect.magic, "magijai");
            appendSigned(buff, effect.check, "patikroms");
            appendSigned(buff, effect.critical, "krit. tikimybei");
            appendSigned(buff, effect.poisonResistance, "atsparumui nuodams");
            appendSigned(buff, effect.necroticResistance, "nekrotiniam atsparumui");
            add(out, buff.toString().replaceFirst("[, ]+$", ""));
        }
        return out.length() == 0 ? item.effect : out.toString();
    }

    static boolean useful(Effect effect, GameState state) {
        if (effect == null || state == null) return false;
        if (effect.requiresCombat && !state.combatActive) return false;
        if (!effect.usableInCombat && state.combatActive) return false;
        if (effect.directDamage > 0 || effect.escape || effect.hasBuff() || effect.cleanse) return true;
        return effect.hp > 0 && state.hp < state.hpMax
                || effect.mana > 0 && state.mana < state.manaMax
                || effect.stamina > 0 && state.stamina < state.staminaMax
                || effect.aeonic > 0 && state.aeonic < state.aeonicMax;
    }

    private static void potion(Effect out, String id, String name, int power) {
        out.turns = 4;
        if ("I092-206".equals(id) || "I092-207".equals(id)) {
            out.cleanse = true; out.poisonResistance = 30; out.necroticResistance = "I092-207".equals(id) ? 25 : 0;
        } else if ("I092-208".equals(id)) { out.defense = 14; out.name = "Atsparumas ugniai";
        } else if ("I092-209".equals(id)) { out.defense = 12; out.speed = 6; out.name = "Atsparumas šalčiui";
        } else if ("I092-210".equals(id)) { out.speed = 12; out.check = 5; out.name = "Atsparumas žaibui";
        } else if ("I092-211".equals(id)) { out.necroticResistance = 45; out.name = "Atsparumas šešėliams";
        } else if ("I092-212".equals(id)) { out.defense = 24; out.name = "Akmens oda";
        } else if ("I092-213".equals(id)) { out.attack = 22; out.name = "Sustiprėjęs";
        } else if ("I092-214".equals(id)) { out.speed = 26; out.critical = 6; out.name = "Pagreitėjęs";
        } else if ("I092-215".equals(id)) { out.magic = 26; out.check = 8; out.name = "Arkaninė galia";
        } else if ("I092-218".equals(id)) { out.attack = 18; out.critical = 7; out.name = "Drakono kraujas";
        } else if ("I092-219".equals(id)) { out.magic = 20; out.necroticResistance = 35; out.name = "Šmėklos esencija";
        } else if ("I092-220".equals(id)) { out.check = 16; out.critical = 12; out.name = "Sėkmės tonikas";
        } else if ("I092-221".equals(id)) { out.speed = 22; out.check = 12; out.name = "Nematomas";
        } else if ("I092-222".equals(id)) { out.check = 14; out.poisonResistance = 25; out.name = "Vandens kvėpavimas"; out.turns = 8;
        } else if ("I092-223".equals(id)) { out.attack = 32; out.defense = -10; out.critical = 14; out.name = "Berserkas";
        } else if ("I092-224".equals(id)) { out.defense = 28; out.poisonResistance = 35; out.necroticResistance = 35; out.name = "Ilgaamžiškumo apsauga"; out.turns = 6;
        } else if ("I092-225".equals(id)) { out.attack = 22; out.defense = 22; out.speed = 22; out.magic = 22; out.check = 15; out.critical = 10; out.name = "Žvaigždžių šviesa"; out.turns = 6;
        } else if ("I092-318".equals(id)) { out.attack = 25; out.defense = 25; out.speed = 25; out.magic = 25; out.check = 18; out.critical = 12; out.name = "Laiko pagreitis"; out.turns = 5;
        } else {
            out.turns = 0;
        }
        if (out.turns > 0 && !out.hasBuff()) out.turns = 0;
        if (out.name.isEmpty()) out.name = name.isEmpty() ? "Potiono poveikis" : out.name;
    }

    private static void append(StringBuilder out, int value, String label) {
        if (value > 0) add(out, "+" + value + " " + label);
    }

    private static void appendSigned(StringBuilder out, int value, String label) {
        if (value == 0) return;
        if (out.length() > 0 && out.charAt(out.length() - 1) != ' ') out.append(", ");
        out.append(value > 0 ? "+" : "").append(value).append(' ').append(label);
    }

    private static void add(StringBuilder out, String value) {
        if (value == null || value.isEmpty()) return;
        if (out.length() > 0) out.append(" · ");
        out.append(value);
    }

    private static boolean contains(String value, String... needles) {
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.forLanguageTag("lt-LT"));
    }

    private ConsumableRulesV110() {}
}
