#!/usr/bin/env python3
"""Fail-fast source and raster acceptance audit for Vaeloria OOC v0.9.0."""

from __future__ import annotations

import hashlib
import json
import re
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app" / "src" / "main" / "java" / "lt" / "vaeloria" / "ooc"
RES = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL: {message}")
    print(f"OK: {message}")


def main() -> None:
    gradle = (ROOT / "app" / "build.gradle").read_text(encoding="utf-8")
    manifest = (ROOT / "app" / "src" / "main" / "AndroidManifest.xml").read_text(encoding="utf-8")
    activity = (JAVA / "PolishedActivity.java").read_text(encoding="utf-8")
    views = (JAVA / "PremiumViewsV090.java").read_text(encoding="utf-8")
    catalog = (JAVA / "VisualAssetCatalog.java").read_text(encoding="utf-8")
    game_state = (JAVA / "GameState.java").read_text(encoding="utf-8")
    local = (JAVA / "VaeloriaActivity.java").read_text(encoding="utf-8")
    groq = (JAVA / "GroqClient.java").read_text(encoding="utf-8")

    require("applicationId 'lt.vaeloria.ooc.personal'" in gradle, "package ID preserved")
    require("versionCode 30" in gradle and "versionName '0.9.0'" in gradle, "v0.9.0 build identity")
    require('android:label="Vaeloria OOC 0.9.0"' in manifest, "manifest label")
    require('android:name="lt.vaeloria.ooc.PolishedActivity"' in manifest, "premium launcher declared")
    require(manifest.count("android.intent.category.LAUNCHER") == 1, "single launcher activity")
    require(manifest.count("uses-permission") == 1 and "android.permission.INTERNET" in manifest, "permission surface unchanged")

    require("hero_einoras_v090" in activity and "hero_einoras_v083" not in activity, "new canonical hero is visible")
    require("SceneV090View" in activity and "CombatV090View" in activity and "scene_combat_v090" in views, "new exploration and illustrated combat scenes")
    require("quest_broken_meridian_v090" in activity, "new quest key art")
    require("WorldMapV090View(this, state)" in activity and "world_map_base_v090" in views, "new interactive atlas")
    require("drawFactionLayer" in views and "drawInfluence" in views and "state.asterraInfluence" in views, "separate dynamic faction overlay")
    require("ItemArtView" in activity and catalog.count("item_") >= 25, "real item-art catalog")
    require("NpcArtView" in activity and catalog.count("npc_") >= 18, "story and city NPC portraits")
    require("MonsterArtView" in activity and catalog.count("monster_") >= 18, "illustrated bestiary catalog")
    require("CITY_NPCS_V090" in activity and "MIESTO GYVENTOJAI IR PASLAUGOS · 12" in activity, "city population UI")
    require("MONSTERS_V090" in activity and "BESTIARIUMAS · 18 ILIUSTRUOTŲ GRĖSMIŲ" in activity, "bestiary UI")
    require("KAIP APSKAIČIUOTA?" in activity and ".setMessage(feedback)" in activity, "raw calculation collapsed by default")
    require("cardHeight = dp(48)" in views and "setMinHeight(dp(44))" in activity, "44dp minimum critical targets")
    require("setScaleType(ImageView.ScaleType.FIT_CENTER)" in activity and "LoadoutV090View" in activity, "hero art is separate from equipment")
    require("v0.8.3" not in activity and "v0.8.2" not in activity, "no stale visible UI version")

    for key in ("asterraInfluence", "dravennInfluence", "lysaraInfluence", "asterraRelation", "dravennRelation", "lysaraRelation"):
        require(f'o.put("{key}"' in game_state and (f'optInt("{key}"' in game_state or f'optString("{key}"' in game_state),
                f"save compatibility field {key}")
    require("asterra_delta" in local and "dravennDelta" in local and "lysaraDelta" in local, "offline resolver changes faction state")
    require("asterra_delta" in groq and "dravenn_delta" in groq and "lysara_delta" in groq, "AI resolver changes faction state")

    assets = json.loads((ROOT / "v090" / "assets_manifest.json").read_text(encoding="utf-8"))
    require(len(assets) == 70, "70 final v0.9.0 raster assets")
    require(len([name for name in assets if name.startswith("scene_")]) == 6, "six distinct scenes")
    require(len([name for name in assets if name.startswith("npc_")]) == 18, "eighteen NPC portraits")
    require(len([name for name in assets if name.startswith("monster_")]) == 18, "eighteen monster portraits")
    require(len([name for name in assets if name.startswith("item_")]) == 25, "twenty-five item icons")

    hashes: set[str] = set()
    for filename, expected in assets.items():
        path = RES / filename
        require(path.is_file(), f"asset exists: {filename}")
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        require(digest == expected["sha256"], f"asset hash: {filename}")
        require(digest not in hashes, f"asset is not a renamed duplicate: {filename}")
        hashes.add(digest)
        with Image.open(path) as image:
            image.load()
            require(image.format == "WEBP", f"asset decodes as WebP: {filename}")
            require(image.width == expected["width"] and image.height == expected["height"], f"asset dimensions: {filename}")

    require(assets["hero_einoras_v090.webp"]["height"] >= 1400, "hero source height is at least 1400px")
    for name in [name for name in assets if name.startswith("scene_")]:
        require(assets[name]["width"] >= 1600 and assets[name]["height"] >= 900, f"premium scene resolution: {name}")
    require(assets["world_map_base_v090.webp"]["width"] >= 1000, "atlas supports mobile zoom")
    require(assets["quest_broken_meridian_v090.webp"]["width"] / assets["quest_broken_meridian_v090.webp"]["height"] >= 1.9, "quest key-art banner ratio")

    resource_names = {path.stem for path in RES.glob("*.webp")}
    for reference in set(re.findall(r"R\.drawable\.([a-z0-9_]+)", activity + views + catalog)):
        require(reference in resource_names or (RES.parent / f"{reference}.xml").exists(), f"referenced drawable exists: {reference}")

    print("Vaeloria v0.9.0 source acceptance audit passed")


if __name__ == "__main__":
    main()
