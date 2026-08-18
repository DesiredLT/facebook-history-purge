#!/usr/bin/env python3
"""Generate the v0.9.2 catalog and crop thirteen 5x5 item-art sheets."""

from __future__ import annotations

import hashlib
import json
import os
import tempfile
from collections import Counter
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE_DIR = ROOT / "generated-v092"
DRAWABLE_DIR = ROOT / "vaeloria-android/app/src/main/res/drawable-nodpi"
JAVA_FILE = ROOT / "vaeloria-android/app/src/main/java/lt/vaeloria/ooc/ItemCatalogV092.java"
JSON_FILE = ROOT / "v092/items_v092.json"
MANIFEST_FILE = ROOT / "v092/assets_manifest_v092.json"

REGIONS = [
    "PELENŲ NEKROPOLIS", "KRAUJŠAKNĖS GIRIA", "GELEŽINĖS VIRŠŪNĖS", "MERIDIANO TUŠTUMA",
    "UŽMIRŠTOS DIRBTUVĖS", "BEDUGNĖS PAKRANTĖ", "AUDRŲ ŠIAURĖ", "LUMINAROS ŠEŠĖLIAI",
]

RARITY_PATTERN = [
    *(["common"] * 4), *(["uncommon"] * 4), *(["rare"] * 5), *(["epic"] * 5),
    *(["legendary"] * 3), *(["mythic"] * 2), "ancient", "unique",
]

RARITY_LEVELS = {
    "common": (1, 20), "uncommon": (10, 35), "rare": (25, 55), "epic": (45, 75),
    "legendary": (65, 90), "mythic": (80, 100), "ancient": (90, 100), "unique": (100, 100),
}
RARITY_POWER = {"common": 4, "uncommon": 8, "rare": 14, "epic": 22, "legendary": 34, "mythic": 48, "ancient": 62, "unique": 80}
RARITY_VALUE = {"common": 20, "uncommon": 65, "rare": 180, "epic": 520, "legendary": 1600, "mythic": 5200, "ancient": 16000, "unique": 50000}
RARITY_DROP_WEIGHT = {"common": 4400, "uncommon": 3000, "rare": 1600, "epic": 700, "legendary": 220, "mythic": 60, "ancient": 15, "unique": 5}


SETS = [
    {"id": "set_kelio_sargas", "name": "Kelio Sargo apdarai", "region": "LUMINAROS ŠEŠĖLIAI", "rarity": "common", "bonus2": "+5 ištvermės atkūrimas po kelionės", "bonus4": "+2 bendras įrangos patikros modifikatorius", "bonus6": "Pirmas kelionės nuovargis sumažinamas perpus"},
    {"id": "set_gelezies_priesaika", "name": "Geležies Priesaikos šarvai", "region": "GELEŽINĖS VIRŠŪNĖS", "rarity": "uncommon", "bonus2": "+8 fizinė gynyba", "bonus4": "+3 bendras įrangos patikros modifikatorius", "bonus6": "Kartą per kovą sušvelnina sunkų smūgį"},
    {"id": "set_plieno_avangardas", "name": "Plieno Avangardo šarvai", "region": "UŽMIRŠTOS DIRBTUVĖS", "rarity": "rare", "bonus2": "+10 gynyba", "bonus4": "+4 puolimo ir gynybos patikroms", "bonus6": "Blokas suteikia trumpą kontratakos langą"},
    {"id": "set_girios_seklys", "name": "Girios Seklio apdarai", "region": "KRAUJŠAKNĖS GIRIA", "rarity": "rare", "bonus2": "+8 greitis ir pėdsakų paieška", "bonus4": "+4 slaptumo patikroms", "bonus6": "Pirmoji pasala nepatiria vietovės baudos"},
    {"id": "set_nakties_asmuo", "name": "Nakties Ašmens apdarai", "region": "LUMINAROS ŠEŠĖLIAI", "rarity": "epic", "bonus2": "+12 kritinio smūgio galia", "bonus4": "+5 vikrumo patikroms", "bonus6": "Po išsisukimo kita ataka sustiprėja"},
    {"id": "set_meridiano_arkanistas", "name": "Meridiano Arkanisto rūbai", "region": "MERIDIANO TUŠTUMA", "rarity": "epic", "bonus2": "+20 mana", "bonus4": "+5 magijos patikroms", "bonus6": "Kas trečias burtas sunaudoja mažiau manos"},
    {"id": "set_ausros_paladinas", "name": "Aušros Paladino šarvai", "region": "LUMINAROS ŠEŠĖLIAI", "rarity": "legendary", "bonus2": "+15 gyvybė ir gynyba", "bonus4": "+6 valios bei gynybos patikroms", "bonus6": "Kritinė gyvybė sužadina Aušros barjerą"},
    {"id": "set_siaures_berserkas", "name": "Šiaurės Berserko apdarai", "region": "AUDRŲ ŠIAURĖ", "rarity": "legendary", "bonus2": "+18 puolimo galia", "bonus4": "+6 jėgos patikroms", "bonus6": "Mažėjant gyvybei auga smūgio galia"},
    {"id": "set_gyvasaknes_sergas", "name": "Gyvašaknės Sergo šarvai", "region": "KRAUJŠAKNĖS GIRIA", "rarity": "mythic", "bonus2": "+20 atsparumas nuodams", "bonus4": "+8 gamtos ir regeneracijos patikroms", "bonus6": "Po kovos lėtai atkuria gyvybę"},
    {"id": "set_kapu_valdovas", "name": "Kapų Valdovo šarvai", "region": "PELENŲ NEKROPOLIS", "rarity": "ancient", "bonus2": "+25 nekrotinis atsparumas", "bonus4": "+10 valios ir relikvijų patikroms", "bonus6": "Mirtinas smūgis kartą palieka 1 gyvybę"},
]

SET_GLOBALS: dict[int, tuple[int, str]] = {}
for set_index in range(10):
    for global_index, slot in [
        (76 + set_index, "head"), (101 + set_index, "chest"), (126 + set_index, "hands"),
        (141 + set_index, "belt"), (151 + set_index, "legs"), (166 + set_index, "feet"),
    ]:
        SET_GLOBALS[global_index] = (set_index, slot)

SLOT_PIECE = {"head": "šalmas", "chest": "krūtinės šarvai", "hands": "pirštinės", "belt": "diržas", "legs": "antblauzdžiai", "feet": "batai"}


def repeated(value: str, count: int = 25) -> list[str]:
    return [value] * count


GROUPS = [
    {
        "names": [
            "Keliauninko geležinis kardas", "Dykumų lenktasis kardas", "Safyro rapyra", "Obsidiano platusis kardas", "Runų dviašmenis",
            "Karo kirvis", "Dvimenis budelio kirvis", "Pusmėnulio kirvis", "Magmos skaldytojas", "Šerkšno kirvis",
            "Geležinė buožė", "Dygliuota aušrinė", "Aušros flanšinė buožė", "Kaukolės smogtuvas", "Perkūno kūjis",
            "Tvirtovės karo kūjis", "Uolų daužiklis", "Kalvės meistro kūjis", "Kristalų kūjis", "Kaulinė vėzduolė",
            "Gyvalapio ašmuo", "Liepsnos kardas", "Žaibo ašmuo", "Šešėlių kardas", "Dangaus spindulio kardas",
        ],
        "categories": repeated("weapon"), "subtypes": [*(["sword"] * 5), *(["axe"] * 5), *(["mace"] * 5), *(["hammer"] * 5), *(["sword"] * 5)], "slots": repeated("weapon"),
    },
    {
        "names": [
            "Žudiko durklas", "Lenktasis gaujos durklas", "Kaulų durklas", "Nuodų iltis", "Žvaigždės durklas",
            "Medžioklės lankas", "Karo lankas", "Šerkšno lankas", "Žarijų lankas", "Tuštumos lankas",
            "Geležinė ietis", "Gyvalapio ietis", "Kristalų ietis", "Pragaro trišakis", "Audros gleivija",
            "Keliaujančio mago lazda", "Kristalų arkanisto lazda", "Nekromanto lazda", "Girios žynio lazda", "Saulės lazda",
            "Runų lazdelė", "Liepsnos lazdelė", "Šalčio lazdelė", "Šešėlių lazdelė", "Perkūno lazdelė",
        ],
        "categories": repeated("weapon"), "subtypes": [*(["dagger"] * 5), *(["bow"] * 5), *(["spear"] * 5), *(["staff"] * 5), *(["wand"] * 5)], "slots": repeated("weapon"),
    },
    {
        "names": [
            "Geležinis apvalusis skydas", "Sargybos aitvaro skydas", "Tvirtovės skydas", "Dvikovininko kumštinis skydas", "Genties medinis skydas",
            "Šerkšno skydas", "Žarijų skydas", "Kristalų skydas", "Erškėčių skydas", "Tuštumos skydas",
            "Aušros skydas", "Kaukolės skydas", "Drakono žvynų skydas", "Audros skydas", "Veidrodžio skydas",
            "Arkaninių formulių knyga", "Mirusiųjų kodeksas", "Gyvašaknės grimuaras", "Dangaus kronika", "Kraujo apeigų tomas",
            "Žvaigždžių krištolo rutulys", "Liepsnos židinys", "Šalčio židinys", "Perkūno židinys", "Šešėlių žibintas",
        ],
        "categories": repeated("offhand"), "subtypes": [*(["shield"] * 15), *(["tome"] * 5), *(["focus"] * 5)], "slots": repeated("offhand"),
    },
    {
        "names": [
            "Geležinis šalmas", "Keliauninko gobtuvas", "Grandininis kapišonas", "Plieno šalmas", "Raguotasis šalmas",
            "Maro gydytojo kaukė", "Kaulų karūna", "Kristalų diadema", "Arkanisto gobtuvas", "Žudiko kapišonas",
            "Paladino didysis šalmas", "Girios ragų karūna", "Nekromanto kaukolės šalmas", "Šerkšno riterio šalmas", "Magmos šalmas",
            "Audros antveidis", "Dykumų šydas", "Koralų šalmas", "Laikrodinis šalmas", "Karališkoji karūna",
            "Sparnuotasis šalmas", "Drakono žvynų šalmas", "Tuštumos kaukė", "Dangaus aureolė", "Pasaulių Karaliaus šalmas",
        ],
        "categories": repeated("head"), "subtypes": repeated("armor"), "slots": repeated("head"),
    },
    {
        "names": [
            "Odinė liemenė", "Grandininiai marškiniai", "Geležinė krūtinplokštė", "Plieno kirasas", "Seklio apsiaustas",
            "Šešėlių tunika", "Arkanisto rūbas", "Šventiko apdaras", "Paladino plokštiniai šarvai", "Berserko kailiai",
            "Gyvašaknės šarvai", "Nekromanto kaulų rūbas", "Šerkšno kirasas", "Magmos plokštė", "Audros šarvai",
            "Dykumų žvynų apsiaustas", "Koralų šarvai", "Laikrodinė krūtinplokštė", "Karališkojo sargo šarvai", "Sparnuotieji šarvai",
            "Drakono žvynų krūtinplokštė", "Tuštumos audinio rūbas", "Dangaus aukso šarvai", "Senovinė runų plokštė", "Pasaulių Karaliaus šarvai",
        ],
        "categories": repeated("chest"), "subtypes": repeated("armor"), "slots": repeated("chest"),
    },
    {
        "names": [
            "Odinės pirštinės", "Grandininės pirštinės", "Plieno pirštinės", "Seklio riešinės", "Žudiko pirštinės",
            "Arkanisto pirštinės", "Paladino pirštinės", "Berserko apvijos", "Gyvašaknės pirštinės", "Nekromanto nagai",
            "Šerkšno pirštinės", "Magmos pirštinės", "Audros pirštinės", "Koralų pirštinės", "Laikrodinės pirštinės",
            "Paprastas odinis diržas", "Alchemiko potionų diržas", "Seklio įrankių diržas", "Riterio karo diržas", "Runų juosta",
            "Kaukolių diržas", "Šerkšno grandinių diržas", "Magmos plokščių diržas", "Dangaus brangakmenių diržas", "Drakono diržas",
        ],
        "categories": [*(["hands"] * 15), *(["belt"] * 10)], "subtypes": repeated("armor"), "slots": [*(["hands"] * 15), *(["belt"] * 10)],
    },
    {
        "names": [
            "Odinės kelnės", "Grandininės kelnės", "Plieno antblauzdžiai", "Seklio kelnės", "Žudiko antblauzdžiai",
            "Arkanisto rūbo apačia", "Paladino kojų plokštės", "Berserko kailinės kelnės", "Gyvašaknės antblauzdžiai", "Nekromanto kaulų kelnės",
            "Šerkšno antblauzdžiai", "Magmos kojų šarvai", "Audros kojų šarvai", "Koralų žvynų kelnės", "Laikrodiniai antblauzdžiai",
            "Odiniai batai", "Geležiniai sabatonai", "Seklio batai", "Žudiko batai", "Arkanisto batai",
            "Paladino sabatonai", "Šerkšno batai", "Magmos batai", "Sparnuotieji dangaus batai", "Drakono nagų batai",
        ],
        "categories": [*(["legs"] * 15), *(["feet"] * 10)], "subtypes": repeated("armor"), "slots": [*(["legs"] * 15), *(["feet"] * 10)],
    },
    {
        "names": [
            "Geležinis žiedas", "Sidabrinis žiedas", "Auksinis žiedas", "Smaragdo žiedas", "Rubino žiedas",
            "Safyro žiedas", "Kaukolės žiedas", "Erškėčių žiedas", "Šerkšno žiedas", "Liepsnos žiedas",
            "Audros žiedas", "Tuštumos žiedas", "Drakono signetas", "Dangaus žiedas", "Senovinis runų žiedas",
            "Odinis amuletas", "Vilko ilties pakabukas", "Kristalų pakabukas", "Saulės medalionas", "Mėnulio pakabukas",
            "Nekromanto talismanas", "Girios žynio amuletas", "Koralų vėrinys", "Laikrodinis amuletas", "Žvaigždžių vėrinys",
        ],
        "categories": [*(["ring"] * 15), *(["neck"] * 10)], "subtypes": repeated("jewelry"), "slots": [*(["ring"] * 15), *(["neck"] * 10)],
    },
    {
        "names": [
            "Mažasis gydymo potionas", "Didysis gydymo potionas", "Manos potionas", "Ištvermės potionas", "Eoninės energijos potionas",
            "Priešnuodžio potionas", "Apvalymo potionas", "Atsparumo ugniai eliksyras", "Atsparumo šalčiui eliksyras", "Atsparumo žaibui eliksyras",
            "Atsparumo šešėliams eliksyras", "Akmens odos eliksyras", "Jėgos eliksyras", "Greitumo eliksyras", "Arkaninės galios eliksyras",
            "Visiško atsigavimo kolba", "Fenikso ašaros", "Drakono kraujo eliksyras", "Šmėklos esencijos potionas", "Sėkmės tonikas",
            "Nematomumo potionas", "Kvėpavimo po vandeniu potionas", "Berserko gėrimas", "Senovinis ilgaamžiškumo eliksyras", "Žvaigždžių šviesos potionas",
        ],
        "categories": repeated("potion"), "subtypes": repeated("alchemy"), "slots": [None] * 25,
    },
    {
        "names": [
            "Liepsnos bomba", "Šerkšno bomba", "Perkūno bomba", "Nuodų bomba", "Dūmų bomba",
            "Šventinto vandens kolba", "Rūgšties kolba", "Mėtomų peilių ryšulys", "Geležiniai ežiai", "Medžioklės spąstai",
            "Liepsnos burtų ritinys", "Šerkšno burtų ritinys", "Perkėlimo ritinys", "Atpažinimo ritinys", "Prikėlimo ritinys",
            "Keliauninko duona", "Kepta žvėriena", "Brandintas sūris", "Raudonasis obuolys", "Grybų troškinys",
            "Džiovinta žuvis", "Medaus pyragas", "Užburtos uogos", "Drakono kepsnys", "Dangaus puotos skrynelė",
        ],
        "categories": [*(["combat_consumable"] * 15), *(["food"] * 10)], "subtypes": [*(["combat"] * 15), *(["food"] * 10)], "slots": [None] * 25,
    },
    {
        "names": [
            "Švytintis runų akmuo", "Senovinė įrašų lentelė", "Kristalų kaukolė", "Drakono širdis", "Fenikso plunksna",
            "Užrakinta siela", "Laiko smiltainis", "Žvaigždžių kompasas", "Dangaus astrolabija", "Tuštumos prizmė",
            "Prirakinta demono akis", "Aušros taurė", "Prakeikta karūna", "Pasaulio Medžio sėkla", "Leviatano perlas",
            "Audros šerdis", "Sušalusi liepsna", "Vulkaninė širdakmenė", "Laikrodinė sfera", "Veidrodžio šukė",
            "Mėnulio stabas", "Saulės stabas", "Plyšio raktas", "Kosminis kubas", "Pasaulių Karūnos brangakmenis",
        ],
        "categories": repeated("relic"), "subtypes": repeated("relic"), "slots": repeated("relic"),
    },
    {
        "names": [
            "Geležies rūda", "Sidabro rūda", "Aukso rūda", "Obsidiano rūda", "Safyro kristalai",
            "Rubino kristalai", "Žvėries iltis", "Žvėries nagai", "Drakono žvynas", "Tvirta žvėries oda",
            "Užburta mediena", "Vaistažolių ryšulys", "Švytintys grybai", "Alcheminiai milteliai", "Runų dulkės",
            "Kalvio įrankiai", "Kalnakasio kirtiklis", "Žvejo meškerė", "Spynų rinkinys", "Siuvėjo rinkinys",
            "Lobių žemėlapis", "Užantspauduotas laiškas", "Senovinis raktas", "Gildijos žetonas", "Karališkasis antspaudas",
        ],
        "categories": [*(["material"] * 15), *(["tool"] * 5), *(["quest"] * 5)], "subtypes": [*(["crafting"] * 15), *(["tool"] * 5), *(["quest"] * 5)], "slots": [*([None] * 15), *(["utility"] * 5), *([None] * 5)],
    },
    {
        "names": [
            "Pasaulio Plyšio didysis kardas", "Senovės pjautuvas", "Dangaus lankas", "Tuštumos lazda", "Drakono ietis",
            "Liūto Tvirtovės skydas", "Veidrodžių grimuaras", "Fenikso židinys", "Pasaulio Medžio lazdelė", "Leviatano trišakis",
            "Mitinis sparnuotasis šalmas", "Senovės drakono šarvai", "Dangaus pirštinės", "Tuštumos antblauzdžiai", "Fenikso batai",
            "Karūnos žiedas", "Galaktikos amuletas", "Laiko potionas", "Prikėlimo kristalas", "Matmenų bomba",
            "Legendinė lobių skrynia", "Užburtas kelioninis krepšys", "Portalų kompasas", "Pasaulio boso ragas", "Unikali pasaulio šukė",
        ],
        "categories": ["weapon", "weapon", "weapon", "weapon", "weapon", "offhand", "offhand", "offhand", "offhand", "weapon", "head", "chest", "hands", "legs", "feet", "ring", "neck", "potion", "relic", "combat_consumable", "artifact", "utility", "utility", "artifact", "relic"],
        "subtypes": ["greatsword", "scythe", "bow", "staff", "spear", "shield", "tome", "focus", "wand", "trident", "armor", "armor", "armor", "armor", "armor", "jewelry", "jewelry", "alchemy", "relic", "combat", "container", "bag", "navigation", "trophy", "relic"],
        "slots": ["weapon", "weapon", "weapon", "weapon", "weapon", "offhand", "offhand", "offhand", "offhand", "weapon", "head", "chest", "hands", "legs", "feet", "ring", "neck", None, "relic", None, None, "utility", "utility", None, "relic"],
        "rarities": [*(["mythic"] * 5), *(["legendary"] * 5), *(["mythic"] * 5), *(["ancient"] * 5), *(["legendary"] * 4), "unique"],
    },
]


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def java_string(value: str | None) -> str:
    if value is None:
        return ""
    return value.replace("\\", "\\\\").replace('"', '\\"')


def restoration(group_index: int, local_index: int, rarity: str) -> tuple[int, int, int, int]:
    if group_index == 8:
        fixed = {
            0: (25, 0, 0, 0), 1: (60, 0, 0, 0), 2: (0, 50, 0, 0), 3: (0, 0, 50, 0), 4: (0, 0, 0, 100),
            5: (10, 0, 10, 0), 6: (20, 10, 10, 0), 15: (100, 100, 100, 180), 16: (75, 40, 40, 80),
            17: (40, 20, 40, 60), 18: (30, 30, 30, 50), 19: (15, 15, 25, 0), 24: (100, 100, 100, 300),
        }
        return fixed.get(local_index, (0, 0, 12 + local_index, 0))
    if group_index == 9 and local_index >= 15:
        amount = 10 + (local_index - 15) * 5
        return (min(30, amount // 2), 0, amount, 0)
    if group_index == 12 and local_index == 17:
        return (100, 100, 100, 300)
    return (0, 0, 0, 0)


SUBTYPE_LABELS = {
    "sword": "kardas", "axe": "kirvis", "mace": "buožė", "hammer": "kūjis",
    "dagger": "durklas", "bow": "lankas", "spear": "ietis", "staff": "mago lazda",
    "wand": "burtų lazdelė", "shield": "skydas", "tome": "užkeikimų knyga",
    "focus": "arkaninis fokusas", "armor": "šarvai", "set_armor": "seto šarvai",
    "jewelry": "papuošalas", "alchemy": "alcheminis mišinys", "combat": "kovos reikmuo",
    "food": "maistas", "relic": "relikvija", "crafting": "amatų medžiaga", "tool": "įrankis",
    "quest": "užduoties daiktas", "greatsword": "didysis kardas", "scythe": "karo dalgis",
    "trident": "trišakis", "container": "lobių talpykla", "bag": "kelioninis krepšys",
    "navigation": "navigacijos įrankis", "trophy": "trofėjus",
}


def make_effect(category: str, power: int, restores: tuple[int, int, int, int], subtype: str) -> str:
    hp, mana, stamina, aeonic = restores
    parts = []
    if hp: parts.append(f"atkuria {hp} gyvybės")
    if mana: parts.append(f"atkuria {mana} manos")
    if stamina: parts.append(f"atkuria {stamina} ištvermės")
    if aeonic: parts.append(f"atkuria {aeonic} eoninės energijos")
    if parts: return ", ".join(parts).capitalize() + "."
    if category == "weapon": return f"Suteikia {power} puolimo galios; ginklo tipas – {SUBTYPE_LABELS.get(subtype, 'ginklas')}."
    if category in {"head", "chest", "hands", "legs", "feet", "belt", "offhand"}: return f"Suteikia {power} apsaugos arba kontrolės galios."
    if category in {"ring", "neck", "relic"}: return f"Suteikia {power} rezonanso galios."
    if category == "combat_consumable": return f"Vienkartinis kovos poveikis, kurio galia {power}."
    if category == "food": return f"Atkuria jėgas; maistinė galia {power}."
    if category == "material": return f"Amatų medžiaga, kokybės rodiklis {power}."
    if category == "tool": return f"Atrakina specializuotą veiksmą; įrankio kokybė {power}."
    if category == "quest": return "Siužetinis daiktas; jo negalima sunaudoti ar išmesti atsitiktinai."
    return f"Pasaulio artefaktas, kurio rezonanso galia {power}."


def build_entries() -> list[dict]:
    entries: list[dict] = []
    global_index = 0
    for group_index, group in enumerate(GROUPS):
        assert all(len(group[key]) == 25 for key in ("names", "categories", "subtypes", "slots")), group_index
        source_path = SOURCE_DIR / f"sheet_{group_index + 1:02d}.png"
        with Image.open(source_path) as source_file:
            source = source_file.convert("RGB")
            width, height = source.size
            assert (width, height) == (1254, 1254), source.size
            for local_index in range(25):
                global_index += 1
                row, column = divmod(local_index, 5)
                left = round(column * width / 5) + 3
                right = round((column + 1) * width / 5) - 3
                top = round(row * height / 5) + 3
                bottom = round((row + 1) * height / 5) - 3
                crop = source.crop((left, top, right, bottom)).resize((384, 384), Image.Resampling.LANCZOS)
                filename = f"item_v092_{global_index:03d}.webp"
                output = DRAWABLE_DIR / filename
                descriptor, temporary_name = tempfile.mkstemp(prefix=f"{filename}.", suffix=".webp")
                os.close(descriptor)
                temporary = Path(temporary_name)
                try:
                    crop.save(temporary, "WEBP", quality=93, method=6)
                    os.replace(temporary, output)
                finally:
                    if temporary.exists(): temporary.unlink()
                    crop.close()

                category = group["categories"][local_index]
                subtype = group["subtypes"][local_index]
                slot = group["slots"][local_index]
                rarity = group.get("rarities", RARITY_PATTERN)[local_index]
                name = group["names"][local_index]
                set_id = ""
                region = REGIONS[(global_index * 5 + group_index) % len(REGIONS)]
                if global_index in SET_GLOBALS:
                    set_index, piece_slot = SET_GLOBALS[global_index]
                    item_set = SETS[set_index]
                    set_id = item_set["id"]
                    rarity = item_set["rarity"]
                    region = item_set["region"]
                    category = slot = piece_slot
                    subtype = "set_armor"
                    name = f"{item_set['name']} · {SLOT_PIECE[piece_slot]}"

                minimum, maximum = RARITY_LEVELS[rarity]
                level = minimum if minimum == maximum else minimum + ((global_index * 7 + local_index) % (maximum - minimum + 1))
                power = RARITY_POWER[rarity] + level // 3 + (local_index % 5)
                value = RARITY_VALUE[rarity] + level * (4 + RARITY_POWER[rarity] // 4)
                restores = restoration(group_index, local_index, rarity)
                stackable = category in {"potion", "combat_consumable", "food", "material"}
                consumable = category in {"potion", "combat_consumable", "food"}
                effect = make_effect(category, power, restores, subtype)
                description = (
                    f"Kilmė – {region.title()}. Tipas – {SUBTYPE_LABELS.get(subtype, 'RPG daiktas')}. "
                    f"Daikto lygis {level}; {effect[0].lower() + effect[1:]}"
                )
                entries.append({
                    "id": f"I092-{global_index:03d}", "name": name, "category": category, "subtype": subtype,
                    "slot": slot, "rarity": rarity, "level": level, "power": power, "value": value,
                    "region": region, "set_id": set_id, "description": description, "effect": effect,
                    "stackable": stackable, "consumable": consumable, "hp_restore": restores[0],
                    "mana_restore": restores[1], "stamina_restore": restores[2], "aeonic_restore": restores[3],
                    "drop_weight": RARITY_DROP_WEIGHT[rarity], "artwork": filename,
                    "artwork_resource": filename.removesuffix(".webp"), "artwork_sha256": sha256(output),
                })
    assert global_index == 325
    return entries


def write_java(entries: list[dict]) -> None:
    item_rows = []
    for item in entries:
        strings = [item[key] for key in ("id", "name", "category", "subtype", "slot", "rarity", "region", "set_id", "description", "effect")]
        args = ", ".join("null" if value is None else f'"{java_string(value)}"' for value in strings)
        item_rows.append(
            f"            new ItemDef({args}, {item['level']}, {item['power']}, {item['value']}, "
            f"{str(item['stackable']).lower()}, {str(item['consumable']).lower()}, {item['hp_restore']}, {item['mana_restore']}, "
            f"{item['stamina_restore']}, {item['aeonic_restore']}, {item['drop_weight']}, R.drawable.{item['artwork_resource']})"
        )
    set_rows = []
    for item_set in SETS:
        values = [item_set[key] for key in ("id", "name", "region", "rarity", "bonus2", "bonus4", "bonus6")]
        set_rows.append("            new SetDef(" + ", ".join(f'"{java_string(value)}"' for value in values) + ")")

    java = f'''package lt.vaeloria.ooc;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Generated v0.9.2 catalog: 325 bundled, illustrated and mechanically usable RPG items. */
final class ItemCatalogV092 {{
    static final String[] RARITIES = new String[]{{"common", "uncommon", "rare", "epic", "legendary", "mythic", "ancient", "unique"}};
    static final String[] CATEGORIES = new String[]{{"weapon", "offhand", "head", "chest", "hands", "legs", "feet", "belt", "ring", "neck", "relic", "potion", "combat_consumable", "food", "material", "tool", "utility", "quest", "artifact"}};

    static final class ItemDef {{
        final String id, name, category, subtype, slot, rarity, region, setId, description, effect;
        final int level, power, value, hpRestore, manaRestore, staminaRestore, aeonicRestore, dropWeight, artwork;
        final boolean stackable, consumable;

        ItemDef(String id, String name, String category, String subtype, String slot, String rarity,
                String region, String setId, String description, String effect, int level, int power,
                int value, boolean stackable, boolean consumable, int hpRestore, int manaRestore,
                int staminaRestore, int aeonicRestore, int dropWeight, int artwork) {{
            this.id=id; this.name=name; this.category=category; this.subtype=subtype; this.slot=slot;
            this.rarity=rarity; this.region=region; this.setId=setId; this.description=description;
            this.effect=effect; this.level=level; this.power=power; this.value=value;
            this.stackable=stackable; this.consumable=consumable; this.hpRestore=hpRestore;
            this.manaRestore=manaRestore; this.staminaRestore=staminaRestore;
            this.aeonicRestore=aeonicRestore; this.dropWeight=dropWeight; this.artwork=artwork;
        }}
    }}

    static final class SetDef {{
        final String id, name, region, rarity, bonus2, bonus4, bonus6;
        SetDef(String id,String name,String region,String rarity,String bonus2,String bonus4,String bonus6) {{
            this.id=id;this.name=name;this.region=region;this.rarity=rarity;
            this.bonus2=bonus2;this.bonus4=bonus4;this.bonus6=bonus6;
        }}
    }}

    static final class ActiveSetBonus {{
        final SetDef set; final int pieces, modifier; final String text;
        ActiveSetBonus(SetDef set,int pieces,int modifier,String text) {{
            this.set=set;this.pieces=pieces;this.modifier=modifier;this.text=text;
        }}
    }}

    static final ItemDef[] ALL = new ItemDef[]{{
{',\n'.join(item_rows)}
    }};

    static final SetDef[] SETS = new SetDef[]{{
{',\n'.join(set_rows)}
    }};

    private static final Map<String,ItemDef> BY_ID=new HashMap<>();
    private static final Map<String,ItemDef> BY_NAME=new HashMap<>();
    private static final Map<String,SetDef> SET_BY_ID=new HashMap<>();
    static {{
        for(ItemDef item:ALL){{BY_ID.put(item.id,item);BY_NAME.put(normalize(item.name),item);}}
        for(SetDef set:SETS)SET_BY_ID.put(set.id,set);
    }}

    private ItemCatalogV092() {{}}

    static ItemDef byId(String id) {{ return id==null?null:BY_ID.get(id); }}

    static ItemDef find(String value) {{
        String query=normalize(value);ItemDef exact=BY_NAME.get(query);if(exact!=null||query.length()<4)return exact;
        for(ItemDef item:ALL){{String key=normalize(item.name);if(query.contains(key)||(query.length()>=8&&key.contains(query)))return item;}}
        return null;
    }}

    static int artFor(String name) {{ ItemDef item=find(name);return item==null?0:item.artwork; }}

    static SetDef setById(String id) {{ return id==null?null:SET_BY_ID.get(id); }}

    static List<ItemDef> inCategory(String category) {{
        ArrayList<ItemDef> out=new ArrayList<>();for(ItemDef item:ALL)if(item.category.equals(category))out.add(item);return out;
    }}

    static List<ItemDef> candidates(String region,String rarity,int minimumLevel,int maximumLevel) {{
        ArrayList<ItemDef> exact=new ArrayList<>(),global=new ArrayList<>();
        for(ItemDef item:ALL){{
            if(!item.rarity.equals(rarity)||item.level<minimumLevel||item.level>maximumLevel||"quest".equals(item.category))continue;
            if(item.region.equals(region))exact.add(item);else global.add(item);
        }}
        return exact.isEmpty()?global:exact;
    }}

    static List<ActiveSetBonus> activeSetBonuses(List<VaeloriaDb.Item> items) {{
        LinkedHashMap<String,Integer> counts=new LinkedHashMap<>();
        for(VaeloriaDb.Item item:items)if(item.equipped&&item.setId!=null&&!item.setId.isEmpty())counts.put(item.setId,counts.getOrDefault(item.setId,0)+1);
        ArrayList<ActiveSetBonus> out=new ArrayList<>();
        for(Map.Entry<String,Integer> entry:counts.entrySet()){{
            SetDef set=setById(entry.getKey());if(set==null)continue;int count=entry.getValue(),modifier=0;StringBuilder text=new StringBuilder();
            if(count>=2){{modifier+=1;text.append("2 dalys: ").append(set.bonus2);}}
            if(count>=4){{modifier+=2;if(text.length()>0)text.append(" · ");text.append("4 dalys: ").append(set.bonus4);}}
            if(count>=6){{modifier+=4;if(text.length()>0)text.append(" · ");text.append("6 dalys: ").append(set.bonus6);}}
            out.add(new ActiveSetBonus(set,count,modifier,text.toString()));
        }}
        return out;
    }}

    static int setModifier(List<VaeloriaDb.Item> items) {{ int total=0;for(ActiveSetBonus bonus:activeSetBonuses(items))total+=bonus.modifier;return Math.min(12,total); }}

    static String categoryLabel(String category) {{
        if(category==null)return"Kita";switch(category){{
            case"weapon":return"Ginklai";case"offhand":return"Skydai ir fokusai";case"head":case"chest":case"hands":case"legs":case"feet":case"belt":return"Šarvai";
            case"ring":case"neck":return"Papuošalai";case"relic":return"Relikvijos";case"potion":return"Potionai ir eliksyrai";
            case"combat_consumable":return"Kovos reikmenys";case"food":return"Maistas";case"material":return"Medžiagos";
            case"tool":case"utility":return"Įrankiai";case"quest":return"Užduočių daiktai";default:return"Artefaktai";
        }}
    }}

    static int rarityRank(String rarity) {{ for(int i=0;i<RARITIES.length;i++)if(RARITIES[i].equals(rarity))return i;return 0; }}

    private static String normalize(String value) {{
        String lower=value==null?"":value.toLowerCase(Locale.forLanguageTag("lt-LT"));
        return Normalizer.normalize(lower,Normalizer.Form.NFD).replaceAll("\\\\p{{M}}+","").replace('’','\\'').trim();
    }}
}}
'''
    JAVA_FILE.write_text(java, encoding="utf-8")


def main() -> None:
    DRAWABLE_DIR.mkdir(parents=True, exist_ok=True)
    entries = build_entries()
    assert len({item["id"] for item in entries}) == 325
    assert len({item["name"] for item in entries}) == 325
    assert len({item["artwork_sha256"] for item in entries}) == 325
    assert {item["rarity"] for item in entries} == set(RARITY_LEVELS)
    assert all(sum(1 for item in entries if item["set_id"] == item_set["id"]) == 6 for item_set in SETS)
    write_java(entries)
    JSON_FILE.write_text(json.dumps({
        "version": "0.9.2", "count": len(entries), "rarity_counts": Counter(item["rarity"] for item in entries),
        "sets": SETS, "items": entries,
    }, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    manifest = {
        "version": "0.9.2",
        "source_sheets": [{"file": f"generated-v092/sheet_{index:02d}.png", "sha256": sha256(SOURCE_DIR / f"sheet_{index:02d}.png")} for index in range(1, 14)],
        "output_count": len(entries),
        "outputs": [{"file": f"vaeloria-android/app/src/main/res/drawable-nodpi/{item['artwork']}", "sha256": item["artwork_sha256"]} for item in entries],
    }
    MANIFEST_FILE.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    total = sum((DRAWABLE_DIR / item["artwork"]).stat().st_size for item in entries)
    print(f"Generated {len(entries)} unique illustrated items ({total / 1024 / 1024:.1f} MiB)")
    print(Counter(item["category"] for item in entries))
    print(Counter(item["rarity"] for item in entries))


if __name__ == "__main__":
    main()
