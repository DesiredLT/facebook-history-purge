package lt.vaeloria.ooc;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Authoritative v0.9.2 monster loot tables. All percentages are per independent drop roll. */
final class DropTableV092 {
    static final class DropProfile {
        final int danger, rolls, dropChanceBasisPoints;
        final int[] rarityBasisPoints;

        DropProfile(int danger, int rolls, int dropChanceBasisPoints, int... rarityBasisPoints) {
            this.danger = danger;
            this.rolls = rolls;
            this.dropChanceBasisPoints = dropChanceBasisPoints;
            this.rarityBasisPoints = rarityBasisPoints;
        }

        int totalRarityBasisPoints() {
            int total = 0;
            for (int value : rarityBasisPoints) total += value;
            return total;
        }

        int rateFor(String rarity) {
            int rank = ItemCatalogV092.rarityRank(rarity);
            return rank >= 0 && rank < rarityBasisPoints.length ? rarityBasisPoints[rank] : 0;
        }

        String compactRates() {
            StringBuilder result = new StringBuilder();
            for (int index = 0; index < ItemCatalogV092.RARITIES.length; index++) {
                if (index > 0) result.append(" · ");
                result.append(shortRarity(ItemCatalogV092.RARITIES[index]))
                        .append(' ').append(percent(rarityBasisPoints[index]));
            }
            return result.toString();
        }
    }

    static final class MonsterProfile {
        final String name, region;
        final DropProfile drops;

        MonsterProfile(String name, String region, DropProfile drops) {
            this.name = name;
            this.region = region;
            this.drops = drops;
        }
    }

    private static final Map<String, Integer> LEGACY_DANGER = new LinkedHashMap<>();

    static {
        legacy("Meridiano vilkas", 5); legacy("Pelkių trolis", 4);
        legacy("Nuodų perų motina", 6); legacy("Kristalų golemas", 7);
        legacy("Nakties harpija", 5); legacy("Maitėdis drake'as", 6);
        legacy("Nuskendęs riteris", 6); legacy("Pelenų revenantas", 7);
        legacy("Kaulų orakulas", 7); legacy("Tuštumos parazitas", 8);
        legacy("Maro kiautas", 5); legacy("Kapų kolosas", 9);
        legacy("Meridiano wyrmas", 10); legacy("Bekarūnis titanas", 10);
        legacy("Kraujšaknė Matriarchė", 10); legacy("Stiklo lichas", 10);
        legacy("Audros kolosas", 10); legacy("Bedugnės šauklys", 10);
    }

    private DropTableV092() {}

    static MonsterProfile profileFor(String monsterName) {
        EnemyCatalogV091.Enemy enemy = EnemyCatalogV091.find(monsterName);
        if (enemy != null) return new MonsterProfile(enemy.name, enemy.region, forDanger(enemy.danger));
        String normalized = normalize(monsterName);
        for (Map.Entry<String, Integer> entry : LEGACY_DANGER.entrySet()) {
            if (!normalized.isEmpty() && (normalized.contains(entry.getKey()) || entry.getKey().contains(normalized))) {
                String region = legacyRegion(entry.getKey());
                return new MonsterProfile(monsterName, region, forDanger(entry.getValue()));
            }
        }
        return new MonsterProfile(monsterName == null ? "Nežinoma grėsmė" : monsterName,
                "LUMINAROS ŠEŠĖLIAI", forDanger(4));
    }

    static DropProfile forDanger(int rawDanger) {
        int danger = Math.max(1, Math.min(10, rawDanger));
        if (danger <= 3) return new DropProfile(danger, 1, 7200,
                4400, 3000, 1600, 700, 220, 60, 15, 5);
        if (danger <= 6) return new DropProfile(danger, danger >= 6 ? 2 : 1, 8800,
                2500, 3200, 2400, 1300, 450, 120, 25, 5);
        if (danger <= 8) return new DropProfile(danger, 2, 10000,
                1000, 2300, 3000, 2300, 900, 350, 120, 30);
        if (danger == 9) return new DropProfile(danger, 3, 10000,
                400, 1200, 2700, 3000, 1700, 700, 240, 60);
        return new DropProfile(danger, 3, 10000,
                0, 400, 1800, 3200, 2700, 1300, 500, 100);
    }

    static List<ItemCatalogV092.ItemDef> roll(String monsterName, long seed) {
        MonsterProfile monster = profileFor(monsterName);
        DropProfile profile = monster.drops;
        ArrayList<ItemCatalogV092.ItemDef> result = new ArrayList<>();
        long state = mix(seed ^ (monsterName == null ? 0 : monsterName.hashCode()) ^ profile.danger * 0x9E3779B97F4A7C15L);
        for (int roll = 0; roll < profile.rolls; roll++) {
            state = next(state);
            if (bounded(state, 10000) >= profile.dropChanceBasisPoints) continue;
            state = next(state);
            String rarity = rarityAt(profile, bounded(state, 10000));
            int center = Math.max(1, profile.danger * 10);
            int minimum = Math.max(1, center - 22);
            int maximum = Math.min(100, center + 18);
            List<ItemCatalogV092.ItemDef> candidates = ItemCatalogV092.candidates(monster.region, rarity, minimum, maximum);
            if (candidates.isEmpty()) candidates = ItemCatalogV092.candidates(monster.region, rarity, 1, 100);
            if (candidates.isEmpty()) continue;
            state = next(state);
            ItemCatalogV092.ItemDef selected = weighted(candidates, state);
            if (selected != null) result.add(selected);
        }
        return result;
    }

    static List<ItemCatalogV092.ItemDef> sample(String monsterName, int count) {
        MonsterProfile monster = profileFor(monsterName);
        ArrayList<ItemCatalogV092.ItemDef> result = new ArrayList<>();
        for (String rarity : ItemCatalogV092.RARITIES) {
            if (monster.drops.rateFor(rarity) == 0) continue;
            List<ItemCatalogV092.ItemDef> candidates = ItemCatalogV092.candidates(
                    monster.region, rarity, 1, 100);
            if (!candidates.isEmpty()) result.add(candidates.get(Math.floorMod(monster.name.hashCode() + result.size(), candidates.size())));
            if (result.size() >= count) break;
        }
        return result;
    }

    private static ItemCatalogV092.ItemDef weighted(List<ItemCatalogV092.ItemDef> candidates, long state) {
        long total = 0;
        for (ItemCatalogV092.ItemDef item : candidates) total += Math.max(1, item.dropWeight);
        long target = Math.floorMod(state, Math.max(1, total));
        for (ItemCatalogV092.ItemDef item : candidates) {
            target -= Math.max(1, item.dropWeight);
            if (target < 0) return item;
        }
        return candidates.get(candidates.size() - 1);
    }

    private static String rarityAt(DropProfile profile, int value) {
        int running = 0;
        for (int index = 0; index < profile.rarityBasisPoints.length; index++) {
            running += profile.rarityBasisPoints[index];
            if (value < running) return ItemCatalogV092.RARITIES[index];
        }
        return "common";
    }

    private static void legacy(String name, int danger) { LEGACY_DANGER.put(normalize(name), danger); }

    private static String legacyRegion(String normalizedName) {
        if (containsAny(normalizedName, "pelki", "krauj", "maro", "nuodu")) return "KRAUJŠAKNĖS GIRIA";
        if (containsAny(normalizedName, "audros", "kristalu")) return "GELEŽINĖS VIRŠŪNĖS";
        if (containsAny(normalizedName, "meridiano", "tustumos", "stiklo")) return "MERIDIANO TUŠTUMA";
        if (containsAny(normalizedName, "pelen", "kapu", "kaulu", "reven", "nuskend")) return "PELENŲ NEKROPOLIS";
        if (containsAny(normalizedName, "bedugnes", "maitedis")) return "BEDUGNĖS PAKRANTĖ";
        return "LUMINAROS ŠEŠĖLIAI";
    }

    private static String percent(int basisPoints) {
        if (basisPoints % 100 == 0) return (basisPoints / 100) + "%";
        if (basisPoints % 10 == 0) return String.format(Locale.ROOT, "%.1f%%", basisPoints / 100.0);
        return String.format(Locale.ROOT, "%.2f%%", basisPoints / 100.0);
    }

    private static String shortRarity(String rarity) {
        switch (rarity) {
            case "uncommon": return "neįpr.";
            case "rare": return "retas";
            case "epic": return "epinis";
            case "legendary": return "legend.";
            case "mythic": return "mitinis";
            case "ancient": return "senov.";
            case "unique": return "unik.";
            default: return "papr.";
        }
    }

    private static long next(long value) { return value * 6364136223846793005L + 1442695040888963407L; }
    private static long mix(long value) { value ^= value >>> 33; value *= 0xff51afd7ed558ccdL; value ^= value >>> 33; return value; }
    private static int bounded(long value, int bound) { return (int) Math.floorMod(value, bound); }
    private static String normalize(String value) {
        String lower = value == null ? "" : value.toLowerCase(Locale.forLanguageTag("lt-LT"));
        return Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").replace('’', '\'').trim();
    }
    private static boolean containsAny(String value, String... needles) { for (String needle : needles) if (value.contains(needle)) return true; return false; }
}
