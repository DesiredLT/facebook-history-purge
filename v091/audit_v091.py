#!/usr/bin/env python3
"""Fail-fast source, catalog and raster acceptance audit for Vaeloria OOC v0.9.1."""

from __future__ import annotations

import hashlib
import json
import re
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


def decode(path: Path, expected_format: str | None = None) -> tuple[int, int]:
    with Image.open(path) as image:
        image.load()
        if expected_format:
            require(image.format == expected_format, f"{path.name} decodes as {expected_format}")
        return image.size


def main() -> None:
    gradle = (ANDROID / "app/build.gradle").read_text(encoding="utf-8")
    manifest = (ANDROID / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
    activity = (JAVA / "PolishedActivity.java").read_text(encoding="utf-8")
    premium = (JAVA / "PremiumActivity.java").read_text(encoding="utf-8")
    views = (JAVA / "PremiumViewsV090.java").read_text(encoding="utf-8")
    catalog = (JAVA / "VisualAssetCatalog.java").read_text(encoding="utf-8")
    enemies_java = (JAVA / "EnemyCatalogV091.java").read_text(encoding="utf-8")
    state = (JAVA / "GameState.java").read_text(encoding="utf-8")
    local = (JAVA / "VaeloriaActivity.java").read_text(encoding="utf-8")
    groq = (JAVA / "GroqClient.java").read_text(encoding="utf-8")

    require("applicationId 'lt.vaeloria.ooc.personal'" in gradle, "package ID preserved")
    require("versionCode 31" in gradle and "versionName '0.9.1'" in gradle, "real upgrade build identity")
    require("testInstrumentationRunner 'androidx.test.runner.AndroidJUnitRunner'" in gradle,
            "supported AndroidX device test runner configured")
    require("androidx.test:runner:1.6.2" in gradle and "androidx.test.ext:junit:1.2.1" in gradle,
            "instrumented JUnit4 dependencies configured")
    require('android:label="Vaeloria OOC 0.9.1"' in manifest, "manifest label updated")
    require('android:allowBackup="false"' in manifest and 'android:usesCleartextTraffic="false"' in manifest,
            "backup and cleartext protections remain enabled")
    require(manifest.count("android.intent.category.LAUNCHER") == 1, "single launcher activity")
    require(manifest.count("uses-permission") == 1 and "android.permission.INTERNET" in manifest,
            "permission surface remains INTERNET-only")

    require("BESTIARIUMAS · 218 ILIUSTRUOTŲ GRĖSMIŲ" in activity, "218-enemy bestiary is player-visible")
    require("REGIONINIAI ĮRAŠAI · 200" in activity and "extendedBestiaryDialog" in activity,
            "200 additional enemies are navigable")
    require(enemies_java.count("new Enemy(") == 200, "generated Java catalog contains 200 enemies")
    require(enemies_java.count("R.drawable.monster_v091_") == 200, "every additional enemy has direct artwork")
    require("EnemyCatalogV091.artFor(name)" in catalog, "live visual resolver checks the extended catalog first")
    require("EnemyCatalogV091.encounterFor" in local and "EnemyCatalogV091.find(a)" in local,
            "offline resolver can initiate encounters from the extended catalog")
    require("EnemyCatalogV091.promptRoster()" in groq, "AI game master receives the extended illustrated roster")

    require('containsAny(query, "oren pel", "orenas pel")' in catalog, "Orenas Pelas maps to the merchant portrait")
    require('"corva", "korva"' in catalog, "Korva Dain maps to the broker portrait")
    require('"nuodų perų", "nuodu peru"' in catalog, "venom broodmother Lithuanian name maps exactly")
    require('"city".equals(node.type)' in views and '"unknown".equals(node.type)' in views,
            "city and unknown map markers have separate branches")
    city_branch = views.split('"city".equals(node.type)', 1)[1].split('"unknown".equals(node.type)', 1)[0]
    unknown_branch = views.split('"unknown".equals(node.type)', 1)[1].split("} else {", 1)[0]
    require("drawRect" in city_branch and "Path unknown" in unknown_branch,
            "city and unknown map markers are visually distinct")

    combined_ui = activity + premium + views
    forbidden = ["GYVA PRIEŠO ILIUSTRACIJA", "raster art", "Release režimas",
                 "Katalogas kraunamas po vieną grupę", "iliustracija nuo įrangos sąsajos atskirta"]
    for phrase in forbidden:
        require(phrase.lower() not in combined_ui.lower(), f"developer phrase removed: {phrase}")
    require("HorizontalScrollView" not in combined_ui, "no horizontal scrolling in primary premium UI")
    combat_source = views.split("class CombatV090View", 1)[1].split("class LoadoutV090View", 1)[0]
    loadout_source = views.split("class LoadoutV090View", 1)[1].split("class WorldMapV090View", 1)[0]
    require("substring(" not in combat_source and "substring(" not in loadout_source,
            "combat and equipment canvas text is not hard-truncated")
    require("drawWrapped" in combat_source and "drawItemName" in loadout_source,
            "combat and equipment canvas text wraps/adapts")
    require("new LinearLayout.LayoutParams(-1, dp(552))" in activity, "full equipment grid has enough vertical space")
    require("str(64)" in groq and "str(60)" in groq and "str(100)" in groq and "str(70)" in groq,
            "AI combat strings have mobile-safe schema limits")
    require("v0.9.0" not in activity + premium, "no stale visible v0.9.0 label")

    for field in ("asterraInfluence", "dravennInfluence", "lysaraInfluence",
                  "asterraRelation", "dravennRelation", "lysaraRelation"):
        require(f'o.put("{field}"' in state and (f'optInt("{field}"' in state or f'optString("{field}"' in state),
                f"save-compatible field: {field}")
    require("asterra_delta" in local and "dravennDelta" in local and "lysaraDelta" in local,
            "offline resolver still changes real faction state")
    require("asterra_delta" in groq and "dravenn_delta" in groq and "lysara_delta" in groq,
            "AI resolver still changes real faction state")

    entries_doc = json.loads((ROOT / "v091/enemies_v091.json").read_text(encoding="utf-8"))
    entries = entries_doc["enemies"]
    require(entries_doc["version"] == "0.9.1" and entries_doc["count"] == 200 and len(entries) == 200,
            "machine-readable enemy catalog count")
    require(len({entry["id"] for entry in entries}) == 200, "enemy IDs are unique")
    require(len({entry["name"] for entry in entries}) == 200, "enemy names are unique")
    require(Counter(entry["region"] for entry in entries) == Counter({region: 25 for region in {
        entry["region"] for entry in entries}}), "each of eight regions contains 25 enemies")
    require(len({entry["region"] for entry in entries}) == 8, "eight bestiary regions")
    for entry in entries:
        require(1 <= entry["danger"] <= 10, f"danger range: {entry['id']}")
        require(all(entry[key] > 0 for key in ("hp", "attack", "defense", "speed")), f"positive stats: {entry['id']}")
        require(entry["speed"] <= 100, f"speed cap: {entry['id']}")

    manifest_v091 = json.loads((ROOT / "v091/assets_manifest_v091.json").read_text(encoding="utf-8"))
    require(manifest_v091["output_count"] == 200 and len(manifest_v091["outputs"]) == 200,
            "v0.9.1 artwork manifest count")
    artwork_hashes: set[str] = set()
    for output in manifest_v091["outputs"]:
        path = ROOT / output["file"]
        require(path.is_file(), f"artwork exists: {path.name}")
        actual = digest(path)
        require(actual == output["sha256"], f"artwork hash: {path.name}")
        require(actual not in artwork_hashes, f"artwork is unique: {path.name}")
        artwork_hashes.add(actual)
        require(decode(path, "WEBP") == (384, 384), f"artwork dimensions: {path.name}")

    require(len(manifest_v091["source_sheets"]) == 8, "eight generated source sheets")
    source_paths = [ROOT / source["file"] for source in manifest_v091["source_sheets"]]
    present_sources = [path for path in source_paths if path.is_file()]
    require(not present_sources or len(present_sources) == 8, "source sheets are either local-only or present as a complete set")
    for source, path in zip(manifest_v091["source_sheets"], source_paths):
        if path.is_file():
            require(digest(path) == source["sha256"], f"source sheet hash: {path.name}")
            require(decode(path, "PNG") == (1254, 1254), f"source sheet dimensions: {path.name}")

    base_manifest = json.loads((ANDROID / "v090/assets_manifest.json").read_text(encoding="utf-8"))
    require(len(base_manifest) == 70, "all 70 premium v0.9.0 assets retained")
    for filename, expected in base_manifest.items():
        path = RES / filename
        require(path.is_file() and digest(path) == expected["sha256"], f"retained base asset: {filename}")
        require(decode(path, "WEBP") == (expected["width"], expected["height"]), f"retained asset dimensions: {filename}")

    mobile_test = ANDROID / "app/src/androidTest/java/lt/vaeloria/ooc/V091MobileDeviceTest.java"
    migration_test = ANDROID / "app/src/androidTest/java/lt/vaeloria/ooc/V083DatabaseMigrationDeviceTest.java"
    require(mobile_test.is_file() and "360dp" in mobile_test.read_text(encoding="utf-8"), "360dp device acceptance test exists")
    require(migration_test.is_file() and "setVersion(3)" in migration_test.read_text(encoding="utf-8"),
            "real SQLite v3-to-v5 migration test exists")
    workflow = (ROOT / ".github/workflows/vaeloria-v091-release.yml").read_text(encoding="utf-8")
    require("wm size 360x800" in workflow and "wm density 160" in workflow
            and "connectedDebugAndroidTest" in workflow, "release CI executes the real 360dp device suite")

    resource_names = {path.stem for path in RES.iterdir() if path.is_file()}
    source = activity + views + catalog + enemies_java
    for reference in set(re.findall(r"R\.drawable\.([a-z0-9_]+)", source)):
        require(reference in resource_names or (RES.parent / f"{reference}.xml").exists(),
                f"referenced drawable exists: {reference}")

    print("Vaeloria v0.9.1 source, catalog and raster acceptance audit passed")


if __name__ == "__main__":
    main()
