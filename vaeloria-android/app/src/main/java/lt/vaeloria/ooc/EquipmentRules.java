package lt.vaeloria.ooc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Autoritetingas v1.0 įrangos ir setų poveikių vertimas į žaidimo skaičiavimus. */
final class EquipmentRules {
    private static final Pattern NUMBER = Pattern.compile("(\\d+)");

    static final class Stats {
        int attack;
        int defense;
        int speed;
        int magicPower;
        int resonance;
        int criticalChance = 5;
        int criticalDamage = 50;
        int maxHp;
        int maxMana;
        int poisonResistance;
        int necroticResistance;
        int travelRecovery;
        int checkBonus;
        boolean travelFatigueHalf;
        boolean heavyHitMitigation;
        boolean blockCounter;
        boolean ambushProtection;
        boolean dodgeEmpowersAttack;
        boolean thirdSpellDiscount;
        boolean dawnBarrier;
        boolean berserk;
        boolean postCombatRegeneration;
        boolean cheatDeath;

        String compact() {
            StringBuilder value = new StringBuilder("Puolimas +").append(attack)
                    .append(" · gynyba +").append(defense)
                    .append(" · greitis +").append(speed)
                    .append(" · magija +").append(magicPower)
                    .append(" · kritinis ").append(criticalChance).append('%');
            if (maxHp > 0) value.append(" · gyvybė +").append(maxHp);
            if (maxMana > 0) value.append(" · mana +").append(maxMana);
            if (checkBonus > 0) value.append(" · patikros +").append(checkBonus);
            return value.toString();
        }
    }

    static Stats calculate(List<VaeloriaDb.Item> items) {
        Stats result = new Stats();
        Map<String,Integer> setPieces = new LinkedHashMap<>();
        if (items == null) return result;
        for (VaeloriaDb.Item item : items) {
            if (item == null || !item.equipped) continue;
            applyItem(result, item);
            if (item.setId != null && !item.setId.isEmpty()) {
                setPieces.put(item.setId, setPieces.getOrDefault(item.setId, 0) + 1);
            }
        }
        for (Map.Entry<String,Integer> entry : setPieces.entrySet()) applySet(result, entry.getKey(), entry.getValue());
        result.criticalChance = clamp(result.criticalChance, 0, 60);
        result.poisonResistance = clamp(result.poisonResistance, 0, 90);
        result.necroticResistance = clamp(result.necroticResistance, 0, 90);
        return result;
    }

    static String itemMechanic(VaeloriaDb.Item item) {
        if (item == null) return "Poveikis neaktyvus";
        int value = effectValue(item);
        String category = item.slot == null ? "" : item.slot;
        if ("weapon".equals(category)) return "+" + legacyOr(value, item.power, legacyAttack(item)) + " puolimo galios";
        if (isArmor(category) || "offhand".equals(category)) return "+" + legacyOr(value, item.power, legacyDefense(item)) + " fizinės gynybos";
        if ("ring".equals(category) || "neck".equals(category) || "relic".equals(category)) return "+" + Math.max(1, legacyOr(value, item.power, 8)) + " rezonanso galios";
        if (item.effect != null && !item.effect.isEmpty()) return item.effect;
        return "Galia " + Math.max(0, item.power) + " naudojama žaidimo patikrose";
    }

    private static void applyItem(Stats result, VaeloriaDb.Item item) {
        String category = item.slot == null ? "" : item.slot;
        int value = effectValue(item);
        if ("weapon".equals(category)) {
            result.attack += legacyOr(value, item.power, legacyAttack(item));
            String name = lower(item.name);
            if (name.contains("durkl")) { result.speed += 5; result.criticalChance += 4; }
            else if (name.contains("lank")) result.criticalChance += 3;
            else if (name.contains("kūj") || name.contains("kuj") || name.contains("kirv")) result.criticalDamage += 12;
        } else if (isArmor(category) || "offhand".equals(category)) {
            result.defense += legacyOr(value, item.power, legacyDefense(item));
            if ("feet".equals(category) || "hands".equals(category)) result.speed += Math.max(1, item.power / 18);
        } else if ("ring".equals(category) || "neck".equals(category) || "relic".equals(category)) {
            int resonance = legacyOr(value, item.power, legacyResonance(item));
            result.resonance += resonance;
            result.magicPower += Math.max(1, resonance / 3);
        } else if ("utility".equals(category)) {
            result.speed += Math.max(2, item.power / 12);
            result.checkBonus += Math.max(1, item.power / 30);
        }
    }

    private static void applySet(Stats result, String setId, int pieces) {
        if ("set_kelio_sargas".equals(setId)) {
            if (pieces >= 2) result.travelRecovery += 5;
            if (pieces >= 4) result.checkBonus += 2;
            if (pieces >= 6) result.travelFatigueHalf = true;
        } else if ("set_gelezies_priesaika".equals(setId)) {
            if (pieces >= 2) result.defense += 8;
            if (pieces >= 4) result.checkBonus += 3;
            if (pieces >= 6) result.heavyHitMitigation = true;
        } else if ("set_plieno_avangardas".equals(setId)) {
            if (pieces >= 2) result.defense += 10;
            if (pieces >= 4) { result.attack += 4; result.defense += 4; }
            if (pieces >= 6) result.blockCounter = true;
        } else if ("set_girios_seklys".equals(setId)) {
            if (pieces >= 2) result.speed += 8;
            if (pieces >= 4) result.checkBonus += 4;
            if (pieces >= 6) result.ambushProtection = true;
        } else if ("set_nakties_asmuo".equals(setId)) {
            if (pieces >= 2) result.criticalDamage += 12;
            if (pieces >= 4) result.speed += 5;
            if (pieces >= 6) result.dodgeEmpowersAttack = true;
        } else if ("set_meridiano_arkanistas".equals(setId)) {
            if (pieces >= 2) result.maxMana += 20;
            if (pieces >= 4) { result.magicPower += 5; result.checkBonus += 2; }
            if (pieces >= 6) result.thirdSpellDiscount = true;
        } else if ("set_ausros_paladinas".equals(setId)) {
            if (pieces >= 2) { result.maxHp += 15; result.defense += 15; }
            if (pieces >= 4) { result.defense += 6; result.checkBonus += 3; }
            if (pieces >= 6) result.dawnBarrier = true;
        } else if ("set_siaures_berserkas".equals(setId)) {
            if (pieces >= 2) result.attack += 18;
            if (pieces >= 4) result.attack += 6;
            if (pieces >= 6) result.berserk = true;
        } else if ("set_gyvasaknes_sergas".equals(setId)) {
            if (pieces >= 2) result.poisonResistance += 20;
            if (pieces >= 4) result.checkBonus += 8;
            if (pieces >= 6) result.postCombatRegeneration = true;
        } else if ("set_kapu_valdovas".equals(setId)) {
            if (pieces >= 2) result.necroticResistance += 25;
            if (pieces >= 4) { result.magicPower += 10; result.checkBonus += 5; }
            if (pieces >= 6) result.cheatDeath = true;
        }
    }

    private static int effectValue(VaeloriaDb.Item item) {
        Matcher matcher = NUMBER.matcher(item.effect == null ? "" : item.effect);
        return matcher.find() ? parse(matcher.group(1)) : 0;
    }

    private static int legacyAttack(VaeloriaDb.Item item) {
        String value = lower(item.name);
        if (value.contains("asteriono")) return 55;
        return 0;
    }

    private static int legacyDefense(VaeloriaDb.Item item) {
        String value = lower(item.name);
        if (value.contains("septynsluoksn")) return 42;
        return 0;
    }

    private static int legacyResonance(VaeloriaDb.Item item) {
        String value = lower(item.name);
        if (value.contains("meridiano rakt")) return 24;
        if (value.contains("prizm")) return 18;
        if (value.contains("signet")) return 16;
        if (value.contains("santarvės žvyn")) return 22;
        return 8;
    }

    private static int legacyOr(int parsed, int power, int legacy) {
        if (parsed > 0) return parsed;
        if (power > 0) return power;
        return Math.max(0, legacy);
    }

    private static boolean isArmor(String category) {
        return "head".equals(category) || "chest".equals(category) || "hands".equals(category)
                || "legs".equals(category) || "feet".equals(category) || "belt".equals(category);
    }

    private static int parse(String value) { try { return Integer.parseInt(value); } catch (Exception ignored) { return 0; } }
    private static String lower(String value) { return value == null ? "" : value.toLowerCase(Locale.forLanguageTag("lt-LT")); }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private EquipmentRules() {}
}
