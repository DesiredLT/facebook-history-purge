package lt.vaeloria.ooc;

import java.util.Locale;

/** Central mapping from live game data to immutable bundled v0.9.x artwork. */
final class VisualAssetCatalog {
    private VisualAssetCatalog() {}

    static int sceneFor(String location, String sceneTitle, boolean combat) {
        if (combat) return R.drawable.scene_combat_v090;
        String query = normalize(location + " " + sceneTitle);
        if (containsAny(query, "rum", "rūm", "sale", "salė", "sarg", "akadem", "audienc")) {
            return R.drawable.scene_palace_v090;
        }
        if (containsAny(query, "meridian", "vart", "anomal", "poslink", "rezonans")) {
            return R.drawable.scene_meridian_v090;
        }
        if (containsAny(query, "kelias", "kelion", "karavan", "vėlyv", "velyv", "pakraš", "pakras")) {
            return R.drawable.scene_road_v090;
        }
        if (containsAny(query, "giria", "mišk", "misk", "labirint", "pelkyn", "slėn", "slen")) {
            return R.drawable.scene_forest_v090;
        }
        if (query.contains("luminara")) return R.drawable.scene_luminara_v090;
        return R.drawable.scene_forest_v090;
    }

    static int npcFor(String name) {
        String query = normalize(name);
        if (query.contains("kael")) return R.drawable.npc_kael_v090;
        if (query.contains("seraph")) return R.drawable.npc_seraphine_v090;
        if (containsAny(query, "oren pel", "orenas pel")) return R.drawable.npc_oren_merchant_v090;
        if (query.contains("orin") || query.contains("oren")) return R.drawable.npc_orin_v090;
        if (query.contains("varek")) return R.drawable.npc_varek_v090;
        if (query.contains("mirel") || query.contains("mira")) return R.drawable.npc_mirel_v090;
        if (containsAny(query, "brynja", "kalv", "smith")) return R.drawable.npc_brynja_smith_v090;
        if (containsAny(query, "taren", "šarv", "sarv", "armorer")) return R.drawable.npc_taren_armorer_v090;
        if (containsAny(query, "ysra", "runų", "runu", "rune artisan")) return R.drawable.npc_ysra_runes_v090;
        if (containsAny(query, "elen", "vaistin", "apot", "healer")) return R.drawable.npc_elen_healer_v090;
        if (containsAny(query, "prekybin", "merchant")) return R.drawable.npc_oren_merchant_v090;
        if (containsAny(query, "mara", "užeig", "uzeig", "innkeeper")) return R.drawable.npc_mara_innkeeper_v090;
        if (containsAny(query, "ilyne", "miesto sarg", "law officer")) return R.drawable.npc_ilyne_guard_v090;
        if (containsAny(query, "darven", "keliauto", "traveler")) return R.drawable.npc_darven_traveler_v090;
        if (containsAny(query, "bram", "staty", "mason", "builder")) return R.drawable.npc_bram_builder_v090;
        if (containsAny(query, "nesta", "arklid", "stablemaster")) return R.drawable.npc_nesta_stablemaster_v090;
        if (containsAny(query, "emil", "raštin", "rastin", "scribe")) return R.drawable.npc_emil_scribe_v090;
        if (containsAny(query, "corva", "korva", "gild", "broker")) return R.drawable.npc_corva_broker_v090;
        return R.drawable.npc_lyra_v090;
    }

    static int monsterFor(String name) {
        int extendedArtwork = EnemyCatalogV091.artFor(name);
        if (extendedArtwork != 0) return extendedArtwork;
        String query = normalize(name);
        if (containsAny(query, "meridiano wyrm", "meridian wyrm", "meridiano slibin")) {
            return R.drawable.monster_meridian_wyrm_v090;
        }
        if (containsAny(query, "bekarūn", "bekarun", "crownless titan")) {
            return R.drawable.monster_crownless_titan_v090;
        }
        if (containsAny(query, "kraujšakn", "kraujšakn", "bloodroot")) {
            return R.drawable.monster_bloodroot_matriarch_v090;
        }
        if (containsAny(query, "stiklo lich", "glass lich")) return R.drawable.monster_glass_lich_v090;
        if (containsAny(query, "audros kolos", "tempest coloss")) return R.drawable.monster_tempest_colossus_v090;
        if (containsAny(query, "bedugnės šauk", "bedugnes sauk", "abyss herald")) {
            return R.drawable.monster_abyss_herald_v090;
        }
        if (containsAny(query, "nuskend", "drowned knight")) return R.drawable.monster_drowned_knight_v090;
        if (containsAny(query, "pelenų reven", "pelenu reven", "ash reven")) {
            return R.drawable.monster_ash_revenant_v090;
        }
        if (containsAny(query, "kaulų orakul", "kaulu orakul", "bone oracle")) {
            return R.drawable.monster_bone_oracle_v090;
        }
        if (containsAny(query, "tuštumos parazit", "tustumos parazit", "void parasite")) {
            return R.drawable.monster_void_parasite_v090;
        }
        if (containsAny(query, "maro kiaut", "plague husk")) return R.drawable.monster_plague_husk_v090;
        if (containsAny(query, "kapų kolos", "kapu kolos", "grave coloss")) {
            return R.drawable.monster_grave_colossus_v090;
        }
        if (containsAny(query, "pelkių trol", "pelkiu trol", "mire troll", "troll")) {
            return R.drawable.monster_mire_troll_v090;
        }
        if (containsAny(query, "nuodų motin", "nuodu motin", "nuodų perų", "nuodu peru", "broodmother", "vor", "spider")) {
            return R.drawable.monster_venom_broodmother_v090;
        }
        if (containsAny(query, "kristalų golem", "kristalu golem", "crystal golem")) {
            return R.drawable.monster_crystal_golem_v090;
        }
        if (containsAny(query, "nakties harp", "night harp")) return R.drawable.monster_night_harpy_v090;
        if (containsAny(query, "maitėd", "maited", "carrion drake", "drake")) {
            return R.drawable.monster_carrion_drake_v090;
        }
        if (containsAny(query, "vilk", "wolf", "skalikas", "hound")) {
            return R.drawable.monster_meridian_wolf_v090;
        }
        return R.drawable.monster_meridian_wolf_v090;
    }

    static int itemFor(String name, String category) {
        String query = normalize(name + " " + category);
        if (containsAny(query, "asteriono", "ašmen", "asmen", "kard", "sword", "weapon")) {
            return R.drawable.item_asterion_blade_v090;
        }
        if (containsAny(query, "dagger", "durkl")) return R.drawable.item_shadow_dagger_v090;
        if (containsAny(query, "mantija", "armor", "chest", "šarv", "sarv")) {
            return R.drawable.item_seven_layer_armor_v090;
        }
        if (containsAny(query, "gauntlet", "pirštin", "pirstin", "hands")) {
            return R.drawable.item_resonance_gauntlet_v090;
        }
        if (containsAny(query, "helm", "šalm", "salm", "head")) return R.drawable.item_guard_helm_v090;
        if (containsAny(query, "meridiano rakt", "meridian key")) return R.drawable.item_meridian_key_v090;
        if (containsAny(query, "rakt", "key")) return R.drawable.item_fractured_key_v090;
        if (containsAny(query, "prizm", "prism")) return R.drawable.item_zero_glass_prism_v090;
        if (containsAny(query, "astrolab")) return R.drawable.item_orison_astrolabe_v090;
        if (containsAny(query, "kompas", "compass", "žvaigždėkrit", "zvaigzdekrit")) {
            return R.drawable.item_starfall_compass_v090;
        }
        if (containsAny(query, "signet", "žied", "zied", "ring")) {
            return query.contains("obsidian")
                    ? R.drawable.item_obsidian_ring_v090
                    : R.drawable.item_resonance_signet_v090;
        }
        if (containsAny(query, "sėkl", "sekl", "seed")) return R.drawable.item_living_rune_seed_v090;
        if (containsAny(query, "antspaud", "sigil", "santarv", "credential")) {
            return R.drawable.item_concord_sigil_v090;
        }
        if (containsAny(query, "šuk", "suk", "shard", "žvyn", "zvyn")) {
            return R.drawable.item_resonance_shard_v090;
        }
        if (containsAny(query, "šerdis", "serdis", "ore", "material", "obsidian")) {
            return R.drawable.item_obsidian_ore_v090;
        }
        if (containsAny(query, "heal", "gydym", "raudon", "potion")) return R.drawable.item_healing_potion_v090;
        if (containsAny(query, "smoke", "dūm", "dum", "stealth")) return R.drawable.item_smoke_flask_v090;
        if (containsAny(query, "vial", "alchem", "reagent")) return R.drawable.item_alchemical_vial_v090;
        if (containsAny(query, "talisman", "ward", "amulet")) return R.drawable.item_ward_talisman_v090;
        if (containsAny(query, "žemėlap", "zemelap", "map", "scroll", "krepš", "kreps", "utility")) {
            return R.drawable.item_route_map_v090;
        }
        if (containsAny(query, "runa", "rune")) return R.drawable.item_rune_stone_v090;
        if (containsAny(query, "chain", "grandin", "token")) return R.drawable.item_chain_token_v090;
        if (containsAny(query, "laišk", "laisk", "letter", "quest")) return R.drawable.item_quest_letter_v090;
        if (containsAny(query, "relic", "relikv", "tablet", "artifact", "artefakt")) {
            return R.drawable.item_relic_tablet_v090;
        }
        return R.drawable.item_relic_tablet_v090;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.forLanguageTag("lt-LT"));
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }
}
