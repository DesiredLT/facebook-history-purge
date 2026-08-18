#!/usr/bin/env python3
"""Fail-fast source and retained-asset acceptance audit for Vaeloria OOC v0.9.3."""

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
catalog = read(JAVA / "CharacterCatalogV093.java")
activity = read(JAVA / "PolishedActivity.java")
engine = read(JAVA / "StatEngine.java")
groq = read(JAVA / "GroqClient.java")
language = read(JAVA / "LithuanianNarrative.java")
database = read(JAVA / "VaeloriaDb.java")

require("versionCode 33" in build and "versionName '0.9.3'" in build, "Android versija yra 33 / 0.9.3")
require('android:label="Vaeloria OOC 0.9.3"' in manifest, "programėlės etiketė yra v0.9.3")
require(catalog.count("new Origin(") == 6, "kataloge yra 6 kilmės")
require(catalog.count("new Archetype(") == 6, "kataloge yra 6 archetipai")
require(catalog.count("new Trait(") == 12, "kataloge yra 12 bruožų su nauda ir kaina")
for field in ("characterCreated", "characterIdentity", "characterOriginId", "characterArchetypeId", "characterAppearance", "characterTraitIds"):
    require(field in state, f"būsenoje saugomas {field}")
require('"character".equals(id)' in activity and "SUKURTI VEIKĖJĄ IR PRADĖTI" in activity, "mobilus veikėjo kūrimo ekranas pasiekiamas prieš žaidimą")
require("PASIRINK LYGIAI 3" in activity and "applyCharacterProfile" in activity, "sąsaja reikalauja lygiai trijų bruožų")
require("profileModifier" in engine and "CharacterCatalogV093.effect" in engine, "profilio poveikis įtrauktas į savybės patikrą")
require("taisyklinga, natūralia ir rišlia lietuvių kalba" in groq and "antruoju asmeniu" in groq, "DI sutartis reikalauja aiškios rišlios lietuvių kalbos")
require("CharacterCatalogV093.profilePrompt(s)" in groq and "Einoras: 201" not in groq, "DI gauna dinaminį, o ne fiksuotą veikėjo profilį")
require("polishChoices" in language and "clearlyEnglish" in language and "Vart" not in language, "vietinis teksto filtras tvarko formatą, terminus ir pasirinkimus")
require("private static final int VERSION = 7" in database and 'root.put("version",7)' in database, "SQLite ir eksporto formatas pakelti iki 7")
require((TEST / "CharacterProfileV093Test.java").is_file() and (TEST / "LithuanianNarrativeV093Test.java").is_file() and (TEST / "GroqContractV093Test.java").is_file(), "yra profilio ir lietuvių kalbos vienetiniai testai")
require("freshInstallStartsWithCharacterCreatorAndProfilePersists" in read(ANDROID_TEST / "V091MobileDeviceTest.java"), "tikras Android testas tikrina veikėjo sukūrimą ir išsaugojimą")
require(len(list(RES.glob("item_v092_*.webp"))) == 325, "išlaikytos 325 daiktų iliustracijos")
require(len(list(RES.glob("monster_v091_*.webp"))) == 200, "išlaikytos 200 regioninių monstrų iliustracijos")
print("Vaeloria v0.9.3 veikėjo kūrimo ir lietuvių kalbos auditas praėjo")
