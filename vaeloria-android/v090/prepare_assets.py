#!/usr/bin/env python3
"""Prepare the immutable Vaeloria v0.9.0 bundled raster assets."""

from __future__ import annotations

import hashlib
import json
import sys
import tempfile
from pathlib import Path

from PIL import Image, ImageOps


ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else ROOT / "v090" / "source-art"
OUTPUT = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
MANIFEST = ROOT / "v090" / "assets_manifest.json"

WEBP_QUALITY = 91


def save_webp(image: Image.Image, name: str, size: tuple[int, int] | None = None) -> Path:
    image = image.convert("RGB")
    if size and image.size != size:
        image = ImageOps.fit(image, size, method=Image.Resampling.LANCZOS, centering=(0.5, 0.5))
    path = OUTPUT / f"{name}.webp"
    with tempfile.NamedTemporaryFile(prefix=f"vaeloria-{name}-", suffix=".webp", delete=False) as handle:
        temporary = Path(handle.name)
    try:
        image.save(temporary, "WEBP", quality=WEBP_QUALITY, method=6)
        if temporary.stat().st_size < 1024:
            raise RuntimeError(f"WebP encoder produced an invalid file: {temporary}")
        with Image.open(temporary) as check:
            check.load()
            if check.format != "WEBP" or check.width < 1 or check.height < 1:
                raise RuntimeError(f"WebP verification failed: {temporary}")
        temporary.replace(path)
    finally:
        temporary.unlink(missing_ok=True)
    return path


def crop_grid(source: Path, names: list[str], columns: int, rows: int, inset: int, size: int) -> list[Path]:
    image = Image.open(source).convert("RGB")
    width, height = image.size
    paths: list[Path] = []
    for index, name in enumerate(names):
        column, row = index % columns, index // columns
        left = round(column * width / columns) + inset
        top = round(row * height / rows) + inset
        right = round((column + 1) * width / columns) - inset
        bottom = round((row + 1) * height / rows) - inset
        paths.append(save_webp(image.crop((left, top, right, bottom)), name, (size, size)))
    return paths


def main() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for stale in OUTPUT.glob(".*.webp.tmp"):
        stale.unlink()
    paths: list[Path] = []

    direct = {
        "hero_einoras_v090.png": "hero_einoras_v090",
        "scene_luminara_v090.png": "scene_luminara_v090",
        "scene_palace_v090.png": "scene_palace_v090",
        "scene_road_v090.png": "scene_road_v090",
        "scene_forest_v090.png": "scene_forest_v090",
        "scene_combat_v090.png": "scene_combat_v090",
        "scene_meridian_v090.png": "scene_meridian_v090",
        "quest_broken_meridian_v090.png": "quest_broken_meridian_v090",
        "world_map_base_v090.png": "world_map_base_v090",
    }
    for filename, resource in direct.items():
        with Image.open(SOURCE / filename) as image:
            paths.append(save_webp(image, resource))

    npc_names = [
        "npc_lyra_v090",
        "npc_kael_v090",
        "npc_seraphine_v090",
        "npc_orin_v090",
        "npc_varek_v090",
        "npc_mirel_v090",
    ]
    paths.extend(crop_grid(SOURCE / "npc_portraits_v090_sheet.png", npc_names, 3, 2, 8, 512))

    city_service_names = [
        "npc_brynja_smith_v090",
        "npc_taren_armorer_v090",
        "npc_ysra_runes_v090",
        "npc_elen_healer_v090",
        "npc_oren_merchant_v090",
        "npc_mara_innkeeper_v090",
    ]
    paths.extend(crop_grid(SOURCE / "city_services_v090_sheet.png", city_service_names, 3, 2, 8, 384))

    city_inhabitant_names = [
        "npc_ilyne_guard_v090",
        "npc_darven_traveler_v090",
        "npc_bram_builder_v090",
        "npc_nesta_stablemaster_v090",
        "npc_emil_scribe_v090",
        "npc_corva_broker_v090",
    ]
    paths.extend(crop_grid(SOURCE / "city_inhabitants_v090_sheet.png", city_inhabitant_names, 3, 2, 8, 384))

    wild_monster_names = [
        "monster_meridian_wolf_v090",
        "monster_mire_troll_v090",
        "monster_venom_broodmother_v090",
        "monster_crystal_golem_v090",
        "monster_night_harpy_v090",
        "monster_carrion_drake_v090",
    ]
    paths.extend(crop_grid(SOURCE / "monster_wild_v090_sheet.png", wild_monster_names, 3, 2, 8, 384))

    anomaly_monster_names = [
        "monster_drowned_knight_v090",
        "monster_ash_revenant_v090",
        "monster_bone_oracle_v090",
        "monster_void_parasite_v090",
        "monster_plague_husk_v090",
        "monster_grave_colossus_v090",
    ]
    paths.extend(crop_grid(SOURCE / "monster_anomaly_v090_sheet.png", anomaly_monster_names, 3, 2, 8, 384))

    boss_monster_names = [
        "monster_meridian_wyrm_v090",
        "monster_crownless_titan_v090",
        "monster_bloodroot_matriarch_v090",
        "monster_glass_lich_v090",
        "monster_tempest_colossus_v090",
        "monster_abyss_herald_v090",
    ]
    paths.extend(crop_grid(SOURCE / "monster_boss_v090_sheet.png", boss_monster_names, 3, 2, 8, 384))

    item_names = [
        "item_asterion_blade_v090",
        "item_shadow_dagger_v090",
        "item_seven_layer_armor_v090",
        "item_resonance_gauntlet_v090",
        "item_guard_helm_v090",
        "item_meridian_key_v090",
        "item_fractured_key_v090",
        "item_relic_tablet_v090",
        "item_zero_glass_prism_v090",
        "item_orison_astrolabe_v090",
        "item_resonance_signet_v090",
        "item_obsidian_ring_v090",
        "item_living_rune_seed_v090",
        "item_concord_sigil_v090",
        "item_resonance_shard_v090",
        "item_obsidian_ore_v090",
        "item_alchemical_vial_v090",
        "item_healing_potion_v090",
        "item_smoke_flask_v090",
        "item_ward_talisman_v090",
        "item_route_map_v090",
        "item_rune_stone_v090",
        "item_chain_token_v090",
        "item_starfall_compass_v090",
        "item_quest_letter_v090",
    ]
    paths.extend(crop_grid(SOURCE / "item_icons_v090_sheet.png", item_names, 5, 5, 7, 384))

    manifest: dict[str, dict[str, object]] = {}
    for path in sorted(paths):
        with Image.open(path) as image:
            image.load()
            manifest[path.name] = {
                "width": image.width,
                "height": image.height,
                "format": image.format,
                "bytes": path.stat().st_size,
                "sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
            }
    MANIFEST.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Prepared {len(paths)} assets in {OUTPUT}")


if __name__ == "__main__":
    main()
