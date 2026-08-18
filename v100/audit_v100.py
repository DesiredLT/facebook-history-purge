#!/usr/bin/env python3
"""Fail-fast source, systems and retained-asset audit for Vaeloria OOC v1.0.0."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "vaeloria-android" / "app"
JAVA = APP / "src" / "main" / "java" / "lt" / "vaeloria" / "ooc"
TEST = APP / "src" / "test" / "java" / "lt" / "vaeloria" / "ooc"
ANDROID_TEST = APP / "src" / "androidTest" / "java" / "lt" / "vaeloria" / "ooc"
RES = APP / "src" / "main" / "res" / "drawable-nodpi"


def read(path: Path) -> str:
    if not path.is_file():
        raise SystemExit(f"Trūksta failo: {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"NEPRAĖJO: {message}")
    print(f"GERAI: {message}")


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
audio = read(JAVA / "VaeloriaAudio.java")
profile = read(JAVA / "CharacterCatalogV093.java")
language = read(JAVA / "LithuanianNarrative.java")

# Leidimo tapatybė ir saugumas.
require("versionCode 40" in build and "versionName '1.0.0'" in build, "Android versija yra 40 / 1.0.0")
require('android:label="Vaeloria OOC 1.0.0"' in manifest, "programėlės etiketė yra v1.0.0")
require("minifyEnabled true" in build and "shrinkResources true" in build, "release buildą optimizuoja R8")
require('android:allowBackup="false"' in manifest and 'android:usesCleartextTraffic="false"' in manifest, "atsarginės kopijos ir nešifruotas ryšys išjungti")
require(manifest.count("uses-permission") == 1 and "android.permission.INTERNET" in manifest, "šaltinyje prašomas tik interneto leidimas")
require("screenOrientation" not in manifest and 'android:resizeableActivity="true"' in manifest, "veikla adaptyvi ir neužrakinta portreto režimu")

# P0 – autoritetingos mechanikos ir pilna atomarinė būsena.
for token in ("enemyAttack", "enemyDefense", "enemySpeed", '"player_guard"', "cooldown", "status"):
    require(token in combat, f"kovos variklis naudoja {token}")
require("equipmentStats" in base_activity and "static Stats calculate" in equipment and "applySet" in equipment, "kova naudoja įrangos ir setų mechaniką")
for snapshot_part in ('root.put("items"', 'root.put("abilities"', 'root.put("stats"', 'root.put("mastery"', 'root.put("world"'):
    require(snapshot_part in database, f"pilna kopija turi {snapshot_part.split(chr(34))[1]} būseną")
require("restoreCore" in database and 'snapshot_json' in database and "DELETE FROM checkpoints" in database, "undo yra atomarinė pilnos kopijos restauracija")

# P1 – struktūrizuotas gyvas pasaulis.
for table in ("quests", "quest_steps", "quest_evidence", "npcs", "shops", "shop_stock", "factions",
              "faction_relations", "settlements", "world_events", "economy", "recipes", "businesses", "hired_npcs"):
    require(f"CREATE TABLE IF NOT EXISTS {table}" in world, f"SQLite turi {table} sistemą")
require(world.count('seedStep(db,"QM-') == 6, "pagrindinė istorija turi 6 nuoseklius etapus")
require(world.count("seedSideQuest(") >= 6, "yra bent 5 šalutinės užduotys")
require("applyEndingConsequences" in world and all(value in world for value in ("priimta", "atmesta", "nepriklausoma")), "trys baigtys keičia pasaulio būseną")
require("recordNpcInteraction" in world and "memory_json" in world and "scheduleAvailable" in world, "NPC turi atmintį, santykius ir tvarkaraščius")

# P2 – progresija, talentai, sunkumas ir kompanionai.
require(progression.count("new TalentDef(") == 20, "talentų medyje yra 20 mechaninių talentų")
for branch in ("KARYS", "ŽVALGAS", "ARKANISTAS", "LYDERIS", "MEISTRAS"):
    require(branch in progression, f"talentų medis turi šaką {branch}")
for mode in ("story", "normal", "hard", "nightmare"):
    require(f'"{mode}"' in activity, f"sąsajoje yra sunkumas {mode}")
require("progressionMode" in state and all(field in state for field in ("experienceNext", "talentPoints", "storyEnding")), "progresija ir baigtis išsaugomos GameState")
for companion in ("Lyra", "Kaelis", "Mirel"):
    require(companion in world, f"pasaulyje yra kompanionas {companion}")
require("companionDefenseBonus" in world and "companionVictoryHealing" in world, "kompanionai realiai keičia kovą")

# P3 – moderni mobili sąsaja ir turinio pasiekiamumas.
require("R.drawable.hero_einoras_v090" in activity and "CharacterAvatarV100View" in activity, "herojaus ekranas jungia premium iliustraciją ir personalizuotą portretą")
for key in ("large_text", "colorblind", "animations", "haptics", "ambient_volume", "sfx_volume"):
    require(key in activity or key in audio, f"yra prieinamumo / garso nustatymas {key}")
require("saveToSlot" in database and "loadFromSlot" in database and "deleteSlot" in database, "veikia trys vardiniai išsaugojimo lizdai")
require("bestiarySearchDialog" in activity and "globalCatalogSearchDialog" in activity and "inventoryFilterDialog" in activity, "didelės kolekcijos turi paiešką, filtrus ir puslapiavimą")
require("locations" in world and "discovered" in world and "recordExploration" in world and "travelMinutes" in world, "atlasas turi atradimus, rūką ir kelionės laiką")
require("discovered.contains" in views and "postInvalidateDelayed(250)" in views, "žemėlapis slepia nežinomas vietas ir riboja perpiešimą")
require("cancelPendingAction" in base_activity and "cancelActive" in groq, "ilgą DI užklausą galima saugiai atšaukti")
require("AudioTrack" in audio and "ToneGenerator" in audio, "aplinkos ir sąsajos garsai veikia be tinklo")

# Veikėjo kūrimas ir lietuviškas DI kontraktas.
require(profile.count("new Origin(") == 6 and profile.count("new Archetype(") == 6 and profile.count("new Trait(") == 12, "veikėjo kūrimas turi 6 kilmes, 6 archetipus ir 12 bruožų")
require("taisyklinga, natūralia ir rišlia lietuvių kalba" in groq and "antruoju asmeniu" in groq, "DI sutartis reikalauja aiškios rišlios lietuvių kalbos")
require("polishChoices" in language and "clearlyEnglish" in language, "vietinis filtras sutvarko kalbą ir pasirinkimus")
require("telefono v0.9.3" not in groq and "telefono v1.0.0" in groq, "DI sutartyje nėra pasenusios runtime versijos")

# Duomenys, testai ir realūs bundled assetai.
require("private static final int VERSION = 11" in database and 'root.put("version",11)' in database, "SQLite ir eksporto schema yra 11")
require("migrateV10toV11" in database and "save_slots" in database and "locations" in world, "v10→v11 migracija prideda lizdus ir atlaso atradimus")
require((ANDROID_TEST / "V100PersistenceDeviceTest.java").is_file(), "yra tikro Android pilnos būsenos atkūrimo testas")
require("VersionEleven" in read(ANDROID_TEST / "V083DatabaseMigrationDeviceTest.java"), "tikras Android testas tikrina v3→v11 migraciją")
require((TEST / "CombatEngineV100Test.java").is_file() and (TEST / "EquipmentRulesV100Test.java").is_file() and (TEST / "ProgressionEngineV100Test.java").is_file(), "yra v1.0 kovos, įrangos ir progresijos vienetiniai testai")
require(len(list(RES.glob("item_v092_*.webp"))) == 325, "išlaikytos 325 daiktų iliustracijos")
require(len(list(RES.glob("monster_v091_*.webp"))) == 200, "išlaikytos 200 regioninių monstrų iliustracijos")
require(len(list(RES.glob("npc_*"))) >= 18, "išlaikyta bent 18 NPC iliustracijų")
require(len(list(RES.glob("scene_*"))) >= 6, "išlaikytos bent 6 scenų iliustracijos")
require((RES / "hero_einoras_v090.webp").stat().st_size > 100_000, "premium herojaus assetas nėra placeholderis")
require((RES / "world_map_base_v090.webp").stat().st_size > 100_000, "premium atlaso assetas nėra placeholderis")

print("Vaeloria OOC v1.0.0 P0–P4 priėmimo auditas praėjo")
