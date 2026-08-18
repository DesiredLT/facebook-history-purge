#!/usr/bin/env python3
"""Fail-fast source, catalog and raster acceptance audit for Vaeloria OOC v0.9.2."""

from __future__ import annotations

import hashlib
import json
from collections import Counter
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "vaeloria-android"
JAVA = ANDROID / "app/src/main/java/lt/vaeloria/ooc"
RES = ANDROID / "app/src/main/res/drawable-nodpi"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL: {message}")
    print(f"OK: {message}")


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main() -> None:
    gradle = (ANDROID / "app/build.gradle").read_text(encoding="utf-8")
    manifest = (ANDROID / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
    activity = (JAVA / "PolishedActivity.java").read_text(encoding="utf-8")
    catalog_java = (JAVA / "ItemCatalogV092.java").read_text(encoding="utf-8")
    drop_java = (JAVA / "DropTableV092.java").read_text(encoding="utf-8")
    database = (JAVA / "VaeloriaDb.java").read_text(encoding="utf-8")
    state = (JAVA / "GameState.java").read_text(encoding="utf-8")
    groq = (JAVA / "GroqClient.java").read_text(encoding="utf-8")
    visual = (JAVA / "VisualAssetCatalog.java").read_text(encoding="utf-8")

    require("applicationId 'lt.vaeloria.ooc.personal'" in gradle, "package ID preserved")
    require("versionCode 32" in gradle and "versionName '0.9.2'" in gradle, "upgrade build identity")
    require('android:label="Vaeloria OOC 0.9.2"' in manifest, "manifest label")
    require('android:allowBackup="false"' in manifest and 'android:usesCleartextTraffic="false"' in manifest,
            "backup and cleartext protections")
    require(manifest.count("uses-permission") == 1 and "android.permission.INTERNET" in manifest,
            "permission surface remains INTERNET-only")

    document = json.loads((ROOT / "v092/items_v092.json").read_text(encoding="utf-8"))
    items = document["items"]
    sets = document["sets"]
    require(document["version"] == "0.9.2" and document["count"] == 325 and len(items) == 325,
            "machine-readable 325-item catalog")
    require(len({item["id"] for item in items}) == 325, "item IDs are unique")
    require(len({item["name"] for item in items}) == 325, "item names are unique")
    require(set(item["rarity"] for item in items) == {"common", "uncommon", "rare", "epic", "legendary", "mythic", "ancient", "unique"},
            "all eight rarity tiers")
    require(set(item["category"] for item in items) == {"weapon", "offhand", "head", "chest", "hands", "legs", "feet", "belt", "ring", "neck", "relic", "potion", "combat_consumable", "food", "material", "tool", "utility", "quest", "artifact"},
            "all 19 RPG categories")
    require(all(1 <= item["level"] <= 100 and item["power"] > 0 and item["value"] >= 0 for item in items),
            "levels, power and values are valid")
    require(all(item["description"] and item["effect"] and item["drop_weight"] > 0 for item in items),
            "every item has mechanics and description")
    require(sum(item["consumable"] for item in items) == 52, "52 usable consumables")
    require(sum(item["hp_restore"] + item["mana_restore"] + item["stamina_restore"] + item["aeonic_restore"] > 0 for item in items) == 36,
            "36 restorative items")

    require(len(sets) == 10, "ten complete sets")
    piece_counts = Counter(item["set_id"] for item in items if item["set_id"])
    require(piece_counts == Counter({item_set["id"]: 6 for item_set in sets}), "every set has exactly six pieces")
    require(all(item_set["bonus2"] and item_set["bonus4"] and item_set["bonus6"] for item_set in sets),
            "all sets have 2/4/6 bonuses")

    artwork_manifest = json.loads((ROOT / "v092/assets_manifest_v092.json").read_text(encoding="utf-8"))
    require(artwork_manifest["output_count"] == 325 and len(artwork_manifest["outputs"]) == 325,
            "artwork manifest count")
    hashes: set[str] = set()
    for output in artwork_manifest["outputs"]:
        path = ROOT / output["file"]
        require(path.is_file() and path.stat().st_size > 0, f"artwork exists: {path.name}")
        actual = digest(path)
        require(actual == output["sha256"] and actual not in hashes, f"artwork hash is valid and unique: {path.name}")
        hashes.add(actual)
        with Image.open(path) as image:
            image.load()
            require(image.format == "WEBP" and image.size == (384, 384), f"artwork dimensions: {path.name}")
    require(len(hashes) == 325, "325 distinct artwork payloads")
    require(catalog_java.count('new ItemDef("') == 325 and catalog_java.count("R.drawable.item_v092_") == 325,
            "generated Java maps every item directly to artwork")
    require("ItemCatalogV092.artFor(name)" in visual, "live item artwork resolver uses catalog first")

    for danger_profile in (
        (4400, 3000, 1600, 700, 220, 60, 15, 5),
        (2500, 3200, 2400, 1300, 450, 120, 25, 5),
        (1000, 2300, 3000, 2300, 900, 350, 120, 30),
        (400, 1200, 2700, 3000, 1700, 700, 240, 60),
        (0, 400, 1800, 3200, 2700, 1300, 500, 100),
    ):
        require(sum(danger_profile) == 10_000, f"drop profile sums to 100%: {danger_profile}")
    require("DropTableV092.roll" in (JAVA / "VaeloriaActivity.java").read_text(encoding="utf-8"),
            "combat victory uses authoritative drop engine")
    require("combat_victory" in drop_java + groq and "combat_escape" in groq, "victory and escape contracts")
    require("mythic" in groq and "enemy_hp" in groq and "combat_round" in groq, "AI contract includes v0.9.2 mechanics")

    for column in ("catalog_id", "item_level", "power", "set_id", "quantity", "value", "effect"):
        require(f"ADD COLUMN {column}" in database and column in database.split("CREATE TABLE items", 1)[1],
                f"SQLite v6 column: {column}")
    require("private static final int VERSION = 6" in database and 'root.put("version",6)' in database,
            "database and save format version 6")
    require("enemyHp" in state and "enemyHpMax" in state and "combatRound" in state, "combat progress persists")
    require("DAIKTŲ KODEKSAS · 325" in activity and "10 PILNŲ SETŲ" in activity,
            "catalog and sets are player-visible")
    require("HorizontalScrollView" not in activity, "primary v0.9.2 UI has no horizontal scrolling")

    require(len(list(RES.glob("monster_v091_*.webp"))) == 200, "all 200 v0.9.1 monsters retained")
    require(len(list(RES.glob("item_v092_*.webp"))) == 325, "all 325 v0.9.2 item images bundled")
    print("Vaeloria v0.9.2 source, catalog, drop and raster acceptance audit passed")


if __name__ == "__main__":
    main()
