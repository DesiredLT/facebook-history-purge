#!/usr/bin/env python3
"""Fail-fast source, systems and retained-asset audit for Vaeloria OOC v1.1.1."""

import hashlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "vaeloria-android" / "app"
JAVA = APP / "src" / "main" / "java" / "lt" / "vaeloria" / "ooc"
TEST = APP / "src" / "test" / "java" / "lt" / "vaeloria" / "ooc"
ANDROID_TEST = APP / "src" / "androidTest" / "java" / "lt" / "vaeloria" / "ooc"
RES = APP / "src" / "main" / "res" / "drawable-nodpi"
WORKFLOWS = ROOT / ".github" / "workflows"


def read(path: Path) -> str:
    if not path.is_file():
        raise SystemExit(f"Trūksta failo: {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"NEPRAĖJO: {message}")
    print(f"GERAI: {message}")


def webp_dimensions(path: Path) -> tuple[int, int]:
    """Read WebP canvas dimensions without optional image-processing packages."""
    data = path.read_bytes()
    if len(data) < 12 or data[:4] != b"RIFF" or data[8:12] != b"WEBP":
        raise SystemExit(f"NEPRAĖJO: netaisyklingas WebP failas {path.relative_to(ROOT)}")

    offset = 12
    while offset + 8 <= len(data):
        chunk_type = data[offset:offset + 4]
        chunk_size = int.from_bytes(data[offset + 4:offset + 8], "little")
        start = offset + 8
        end = start + chunk_size
        if end > len(data):
            break
        chunk = data[start:end]

        if chunk_type == b"VP8X" and len(chunk) >= 10:
            width = int.from_bytes(chunk[4:7], "little") + 1
            height = int.from_bytes(chunk[7:10], "little") + 1
            return width, height
        if chunk_type == b"VP8L" and len(chunk) >= 5 and chunk[0] == 0x2F:
            bits = int.from_bytes(chunk[1:5], "little")
            return (bits & 0x3FFF) + 1, ((bits >> 14) & 0x3FFF) + 1
        if chunk_type == b"VP8 " and len(chunk) >= 10 and chunk[3:6] == b"\x9d\x01\x2a":
            width = int.from_bytes(chunk[6:8], "little") & 0x3FFF
            height = int.from_bytes(chunk[8:10], "little") & 0x3FFF
            return width, height

        offset = end + (chunk_size & 1)

    raise SystemExit(f"NEPRAĖJO: nepavyko nustatyti WebP matmenų {path.relative_to(ROOT)}")


def unique_file_count(paths: list[Path]) -> int:
    return len({hashlib.sha256(path.read_bytes()).digest() for path in paths})


build = read(APP / "build.gradle")
manifest = read(APP / "src" / "main" / "AndroidManifest.xml")
state = read(JAVA / "GameState.java")
database = read(JAVA / "VaeloriaDb.java")
world = read(JAVA / "WorldRepository.java")
combat = read(JAVA / "CombatEngine.java")
equipment = read(JAVA / "EquipmentRules.java")
progression = read(JAVA / "ProgressionEngine.java")
activity = read(JAVA / "PolishedActivity.java")
base_activity = read(JAVA / "VaeloriaActivity.java")
views = read(JAVA / "PremiumViewsV090.java")
groq = read(JAVA / "GroqClient.java")
narration_policy = read(APP / "src/main/assets/ai/narration-policy.txt")
audio = read(JAVA / "VaeloriaAudio.java")
profile = read(JAVA / "CharacterCatalogV093.java")
language = read(JAVA / "LithuanianNarrative.java")
consumables = read(JAVA / "ConsumableRulesV110.java")

# Leidimo tapatybė ir saugumas.
require("versionCode 43" in build and "versionName '1.1.1'" in build, "Android versija yra 43 / 1.1.1")
require('android:label="Vaeloria OOC 1.1.1"' in manifest, "programėlės etiketė yra v1.1.1")
require("minifyEnabled true" in build and "shrinkResources true" in build, "release buildą optimizuoja R8")
require('android:allowBackup="false"' in manifest and 'android:usesCleartextTraffic="false"' in manifest, "atsarginės kopijos ir nešifruotas ryšys išjungti")
require(manifest.count("uses-permission") == 1 and "android.permission.INTERNET" in manifest, "šaltinyje prašomas tik interneto leidimas")
require("screenOrientation" not in manifest and 'android:resizeableActivity="true"' in manifest, "veikla adaptyvi ir neužrakinta portreto režimu")
for workflow in sorted(WORKFLOWS.glob("*.yml")):
    if workflow.name == "vaeloria-v100-release.yml":
        continue
    legacy = read(workflow)
    require("workflow_dispatch:" in legacy and "\n  push:" not in legacy and "\n  pull_request:" not in legacy,
            f"senasis {workflow.name} workflow paleidžiamas tik rankiniu būdu")

# P0 – autoritetingos mechanikos ir pilna atomarinė būsena.
for token in ("enemyAttack", "enemyDefense", "enemySpeed", '"player_guard"', "cooldown", "status"):
    require(token in combat, f"kovos variklis naudoja {token}")
require("equipmentStats" in base_activity and "static Stats calculate" in equipment and "applySet" in equipment, "kova naudoja įrangos ir setų mechaniką")
for snapshot_part in ('root.put("items"', 'root.put("abilities"', 'root.put("stats"', 'root.put("mastery"', 'root.put("world"'):
    require(snapshot_part in database, f"pilna kopija turi {snapshot_part.split(chr(34))[1]} būseną")
require("restoreCore" in database and 'snapshot_json' in database and "DELETE FROM checkpoints" in database, "undo yra atomarinė pilnos kopijos restauracija")

# P1 – struktūrizuotas gyvas pasaulis.
for table in ("quests", "quest_steps", "quest_evidence", "npcs", "shops", "shop_stock", "factions",
              "faction_relations", "settlements", "world_events", "economy", "recipes", "recipe_ingredients", "businesses", "hired_npcs"):
    require(f"CREATE TABLE IF NOT EXISTS {table}" in world, f"SQLite turi {table} sistemą")
require(world.count('seedStep(db,"QM-') == 6, "pagrindinė istorija turi 6 nuoseklius etapus")
require(world.count("seedSideQuest(") >= 6, "yra bent 5 šalutinės užduotys")
require("applyEndingConsequences" in world and all(value in world for value in ("priimta", "atmesta", "nepriklausoma")), "trys baigtys keičia pasaulio būseną")
require("recordNpcInteraction" in world and "memory_json" in world and "scheduleAvailable" in world, "NPC turi atmintį, santykius ir tvarkaraščius")

# P2 – progresija, talentai, sunkumas ir kompanionai.
require(progression.count("new TalentDef(") == 25, "talentų medyje yra 25 mechaniniai talentai iki 90 lygio")
for branch in ("KARYS", "ŽVALGAS", "ARKANISTAS", "LYDERIS", "MEISTRAS"):
    require(branch in progression, f"talentų medis turi šaką {branch}")
for mode in ("story", "normal", "hard", "nightmare"):
    require(f'"{mode}"' in activity, f"sąsajoje yra sunkumas {mode}")
require("progressionMode" in state and all(field in state for field in ("experienceNext", "talentPoints", "attributePoints", "storyEnding")), "progresija, savybių taškai ir baigtis išsaugomi GameState")
for companion in ("Lyra", "Kaelis", "Mirel"):
    require(companion in world, f"pasaulyje yra kompanionas {companion}")
require('companion(db,"comp-kaelis","npc-kaelis","Kapitonas Kaelis","Gynėjas","+6 gynybai pirmame kovos ėjime")' in world,
        "Kaelio kompaniono įrašas turi pilną vaidmenį ir mechaninį privalumą")
require("companionDefenseBonus" in world and "companionVictoryHealing" in world, "kompanionai realiai keičia kovą")

# P3 – moderni mobili sąsaja ir turinio pasiekiamumas.
require("canonicalEinoras" in activity and "CharacterAvatarV100View" in activity, "fiksuota Einoro iliustracija nerodoma kitiems sukurtiems veikėjams")
require("import android.widget.ProgressBar;" in activity, "progresijos juosta turi Android klasės importą")
for key in ("large_text", "colorblind", "animations", "haptics", "ambient_volume", "sfx_volume"):
    require(key in activity or key in audio, f"yra prieinamumo / garso nustatymas {key}")
require("saveToSlot" in database and "loadFromSlot" in database and "deleteSlot" in database, "veikia trys vardiniai išsaugojimo lizdai")
require("bestiarySearchDialog" in activity and "globalCatalogSearchDialog" in activity and "inventoryFilterDialog" in activity, "didelės kolekcijos turi paiešką, filtrus ir puslapiavimą")
require("locations" in world and "discovered" in world and "recordExploration" in world and "travelMinutes" in world, "atlasas turi atradimus, rūką ir kelionės laiką")
require("discovered.contains" in views and "postInvalidateDelayed(250)" in views, "žemėlapis slepia nežinomas vietas ir riboja perpiešimą")
require("cancelPendingAction" in base_activity and "activeAiTransport.cancel()" in base_activity, "ilgą DI užklausą galima saugiai atšaukti")
require("AudioTrack" in audio and "ToneGenerator" in audio, "aplinkos ir sąsajos garsai veikia be tinklo")

# Veikėjo kūrimas ir lietuviškas DI kontraktas.
require(profile.count("new Origin(") == 6 and profile.count("new Archetype(") == 6 and profile.count("new Trait(") == 12, "veikėjo kūrimas turi 6 kilmes, 6 archetipus ir 12 bruožų")
require("taisyklinga, natūralia ir rišlia lietuvių kalba" in narration_policy and "antruoju asmeniu" in narration_policy, "DI sutartis reikalauja aiškios rišlios lietuvių kalbos")
require("polishChoices" in language and "clearlyEnglish" in language, "vietinis filtras sutvarko kalbą ir pasirinkimus")
require("resolveNarration" in groq and "pendingResolvedTurn" in base_activity and "requestOpenAi" in base_activity, "abu DI tiekėjai naudoja pasakojimo sutartį virš vietinio rezultato")

# Duomenys, testai ir realūs bundled assetai.
require("private static final int VERSION = 13" in database and 'root.put("version",13)' in database, "SQLite ir eksporto schema yra 13")
require("migrateV11toV12" in database and "migrateV12toV13" in database and "legacyPersistedBonus" in profile, "v11→v12 ir v12→v13 migracijos išsaugo vientisumą")
require(world.count("seedRecipeV110(db,") >= 27 and "required_talent" in world and "station" in world, "yra bent 27 kelių reagentų, lygių, darbo vietų ir talentų receptai")
require("state.level<item.level" in database and "equipRequirement" in database and "state.level<selected.level" in world, "daiktų lygiai riboja naudojimą, įrangą ir pirkimą")
require(all(token in consumables for token in ("directDamage", "enemyEffectTurns", "poisonResistance", "usableInCombat", "description")), "potionai ir kovos reikmenys turi struktūrizuotą mechaniką")
policy = read(JAVA / "AiTurnPolicyV101.java")
require("knownLocationNames" in world and "checkAllowsProgress" in world and "exactItem" in policy, "DI pasekmes riboja vietos, patikros ir katalogo politika")
require("combatHeavyMitigationUsed" in state and "combatSpellCount" in state and "Atgauna kvapą" in combat, "kovos būsena saugo vienkartinius efektus ir neleidžia nemokamų atakų")
require((ANDROID_TEST / "V100PersistenceDeviceTest.java").is_file(), "yra tikro Android pilnos būsenos atkūrimo testas")
require("VersionThirteen" in read(ANDROID_TEST / "V083DatabaseMigrationDeviceTest.java"), "tikras Android testas tikrina v3→v13 migraciją")
require((TEST / "CombatEngineV100Test.java").is_file() and (TEST / "EquipmentRulesV100Test.java").is_file() and (TEST / "ProgressionEngineV100Test.java").is_file(), "yra v1.0 kovos, įrangos ir progresijos vienetiniai testai")
item_art = sorted(RES.glob("item_v092_*.webp"))
monster_art = sorted(RES.glob("monster_v091_*.webp"))
npc_art = sorted(RES.glob("npc_*_v090.webp"))
scene_art = sorted(RES.glob("scene_*_v090.webp"))
hero_art = RES / "hero_einoras_v090.webp"
quest_art = RES / "quest_broken_meridian_v090.webp"
map_art = RES / "world_map_base_v090.webp"

require(len(item_art) == 325, "išlaikytos 325 daiktų iliustracijos")
require(all(webp_dimensions(path) == (384, 384) for path in item_art),
        "visos 325 daiktų iliustracijos yra individualios 384×384 ikonos")
require(unique_file_count(item_art) == 325, "visos 325 daiktų iliustracijos yra unikalūs failai")
require(len(monster_art) == 200, "išlaikytos 200 regioninių monstrų iliustracijos")
require(all(webp_dimensions(path) == (384, 384) for path in monster_art),
        "visos 200 monstrų iliustracijos yra individualios 384×384 ikonos")
require(unique_file_count(monster_art) == 200, "visos 200 monstrų iliustracijos yra unikalūs failai")
require(len(npc_art) >= 18, "išlaikyta bent 18 individualių NPC iliustracijų")
require(all(width >= 384 and height >= 384 for width, height in map(webp_dimensions, npc_art)),
        "visų NPC iliustracijų matmenys yra bent 384×384")
require(len(scene_art) == 6, "išlaikytos 6 individualios scenų iliustracijos")
require(all(width >= 1_600 and height >= 900 for width, height in map(webp_dimensions, scene_art)),
        "visos scenų iliustracijos yra bent 1600×900")
require(webp_dimensions(hero_art)[1] >= 1_400, "herojaus šaltinis yra bent 1400 px aukščio")
require(webp_dimensions(quest_art)[0] >= 1_600 and webp_dimensions(quest_art)[1] >= 800,
        "užduoties iliustracija yra bent 1600×800")
require(webp_dimensions(map_art)[0] >= 1_000 and webp_dimensions(map_art)[1] >= 1_400,
        "atlaso iliustracija yra bent 1000×1400")

print("Vaeloria OOC v1.1.1 šaltinio struktūros ir išteklių auditas praėjo")
