#!/usr/bin/env python3
"""Build the v0.9.1 enemy catalog and crop eight generated 5x5 art sheets."""

from __future__ import annotations

import hashlib
import json
import os
import tempfile
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE_DIR = ROOT / "generated-v091"
DRAWABLE_DIR = ROOT / "vaeloria-android/app/src/main/res/drawable-nodpi"
JAVA_FILE = ROOT / "vaeloria-android/app/src/main/java/lt/vaeloria/ooc/EnemyCatalogV091.java"
JSON_FILE = ROOT / "v091/enemies_v091.json"
MANIFEST_FILE = ROOT / "v091/assets_manifest_v091.json"


GROUPS = [
    {
        "region": "PELENŲ NEKROPOLIS",
        "habitat": "Pelenų nekropolio kapuose",
        "traits": ["pelenų šydas", "nekrotinis atsparumas", "žarijų šuolis", "kapų grandinės", "juodoji liepsna"],
        "names": [
            "Pelenų skalikas", "Anglies šmėkla", "Apdegęs sargybinis", "Kaulinė kapų žiurkė", "Dūmų ragana",
            "Kaulų piligrimas", "Žarijų spektras", "Pelenų riteris", "Sarkofago mimikas", "Suanglėjęs milžinas",
            "Nekropolio varpo žvėris", "Žarijų šikšnosparnių karalienė", "Laidotuvių kandis", "Lydytos kaukolės roplys", "Kapų grandinių kalėtojas",
            "Juodosios liepsnos kunigas", "Kapinių kaulų elnias", "Suodžių gargulis", "Krematoriumo išsigimėlis", "Pelenų skorpionas",
            "Obsidiano karsto golemas", "Pažeistas laužo feniksas", "Sulydytas skeletų legionierius", "Senovinis kapo globėjas", "Vulkaninis mirties drakonas",
        ],
    },
    {
        "region": "KRAUJŠAKNĖS GIRIA",
        "habitat": "Kraujšaknės girios pelkynuose",
        "traits": ["nuodų dygliai", "šaknų spąstai", "sporų debesis", "samanų šarvas", "gyvybės siurbimas"],
        "names": [
            "Erškėčių vilkas", "Samaninis šernas", "Grybų ragų elnias", "Vijoklių ragana", "Pelkės tykūnas",
            "Pelkynų hidra", "Plėšrioji orchidėja", "Žievės trolis", "Maro rupūžė", "Šaknų voras",
            "Gluosnio raudotoja", "Grybienos riteris", "Pelkių krokodilas drake'as", "Žibintinė kandis", "Šarvuotoji dėlių karalienė",
            "Raguotasis girios globėjas", "Liūno ragana", "Plėšrusis medis", "Pelkės baziliskas", "Grybų kolosas",
            "Jaunoji Kraujšaknė", "Nuskendęs entas", "Nuodingųjų žiedų titanas", "Senasis girios drakonas", "Gyvoji pelkės tvirtovė",
        ],
    },
    {
        "region": "GELEŽINĖS VIRŠŪNĖS",
        "habitat": "Geležinių Viršūnių perėjose",
        "traits": ["uolų šarvas", "lavinos smūgis", "kristalų atspindys", "kalvės kaitra", "audros krūvis"],
        "names": [
            "Uolinis avinas", "Blyškusis urvų siaubūnas", "Geležinis kalnų erelis", "Kristalinė angis", "Uolų trolis",
            "Rūdos vabalas", "Lavinos milžinas", "Magmos salamandra", "Audros vyvernas", "Akmeninis baziliskas",
            "Runų minotauras", "Kristalų harpija", "Grandinėtas ciklopas", "Obsidiano gręžikas", "Kalnų lichas",
            "Perkūno paukštis", "Kalvės demonas", "Kvarco golemas", "Granito hidra", "Viršūnių revenantas",
            "Gelmių titanas", "Dangaus laužytojas", "Gyvosios citadelės golemas", "Vulkaninis wyrmas", "Senovinis kalnų karalius",
        ],
    },
    {
        "region": "MERIDIANO TUŠTUMA",
        "habitat": "Meridiano Tuštumos plyšiuose",
        "traits": ["fazės poslinkis", "gravitacijos lūžis", "atminties vagystė", "nulinio stiklo oda", "paradokso laukas"],
        "names": [
            "Plyšio skalikas", "Veidrodžio šmėkla", "Fazinis voras", "Nulinio stiklo riteris", "Rezonanso ungurys",
            "Daugiakė anomalija", "Laiko revenantas", "Gyvasis aido mimikas", "Gravitacijos žvėris", "Astralinis parazitas",
            "Sklandantis šukių golemas", "Beveidis tuštumos vienuolis", "Anomalijos kandis", "Portalo gyvatė", "Skelto kūno persekiotojas",
            "Bežvaigždis angelas", "Paradokso hidra", "Atminties ėdikas", "Juodosios saulės kunigas", "Gardelės žvėris",
            "Meridiano orakulas", "Horizonto titanas", "Tuštumos banginis", "Suskilusio dievo avataras", "Plyšio drakonas",
        ],
    },
    {
        "region": "UŽMIRŠTOS DIRBTUVĖS",
        "habitat": "Užmirštų dirbtuvių griuvėsiuose",
        "traits": ["krumpliaračių spąstai", "runinis skydas", "magnetinė trauka", "krosnies kaitra", "mechaninis persitvarkymas"],
        "names": [
            "Mechaninis kranklys", "Bronzinis vilkas", "Runinis sargybinis", "Grandininis automatas", "Žibintgalvis golemas",
            "Ašmenų skarabėjų motinėlė", "Žalvarinis kentauras", "Apgulties mimikas", "Arkaninis bokštelis", "Geležinis vienuolis",
            "Prakeikta porceliano lėlė", "Laikrodinis voras", "Tušti gyvieji šarvai", "Krumpliaračių gyvatė", "Krosnies riteris",
            "Titano plaštaka", "Katedros automatas", "Alcheminis homunkulas", "Magnetinis šukių golemas", "Sudužusi orakulo mašina",
            "Senovinė karo mašina", "Mechaninis kolosas", "Vaikščiojanti tvirtovė", "Saulės kalvės serafas", "Griuvėsių metalinis drakonas",
        ],
    },
    {
        "region": "BEDUGNĖS PAKRANTĖ",
        "habitat": "Bedugnės Pakrantės vandenyse",
        "traits": ["skendimo prakeiksmas", "koralų šarvas", "bioliuminescencinė apgaulė", "potvynio smūgis", "gelmių spaudimas"],
        "names": [
            "Druskos lavonas", "Rifo skalikas", "Nuskendusio jūreivio šmėkla", "Kiauto krabų smogikas", "Plėšrioji sirena",
            "Gelmių žibintžuvė", "Kriauklėtas riteris", "Medūzos orakulas", "Šarvuotas ungurys", "Koralų golemas",
            "Krakeno palikuonis", "Potvynių ragana", "Ryklys revenantas", "Laivo nuolaužų mimikas", "Bedugnės medūza",
            "Leviatano kunigas", "Nuskendęs karalius", "Trišakio demonas", "Koralų hidra", "Juodvandenis begemotas",
            "Bedugnės leviatanas", "Vaikščiojantis švyturys", "Tvirtovinis krabas", "Gelmių drakonas", "Raudonosios įdubos dievaitis",
        ],
    },
    {
        "region": "AUDRŲ ŠIAURĖ",
        "habitat": "Audrų Šiaurės ledynuose",
        "traits": ["šerkšno šarvas", "pūgos šydas", "žaibo krūvis", "ledo spąstai", "pašvaistės blyksnis"],
        "names": [
            "Ledšarvis vilkas", "Šalčio goblinas", "Sniego harpija", "Šerkšno riteris", "Kristalinė lapė",
            "Ledynų trolis", "Pūgos ragana", "Sušalęs didysis voras", "Žaibo ragų elnias", "Krušos elementas",
            "Šalčio milžinas", "Audros vienuolis", "Perkūno driežas", "Ledo gręžikas", "Pašvaistės šmėkla",
            "Ledyno golemas", "Audros erelis", "Sušalęs senovės karalius", "Debesų gyvatė", "Perkūno oni",
            "Žiemos titanas", "Audrų katedros žvėris", "Pašvaistės drakonas", "Baltosios pūgos leviatanas", "Poliarinis pasaulių ėdikas",
        ],
    },
    {
        "region": "LUMINAROS ŠEŠĖLIAI",
        "habitat": "Luminaros požemiuose ir užtemusiuose kvartaluose",
        "traits": ["pasalos žingsnis", "kraujo ritualas", "miesto reljefo išnaudojimas", "baimės aura", "Meridiano kontrabanda"],
        "names": [
            "Mutavusi kanalizacijos žiurkė", "Skersgatvio gargulis", "Maro kaukės kultistas", "Gyvojo apsiausto vagis", "Kraujo alchemikas",
            "Kaukėtasis revenantas dvikovininkas", "Monetų skrynios mimikas", "Laikrodžio bokšto šikšnosparnis", "Kanalizacijos krokodilas", "Gildijos vykdytojo golemas",
            "Raudonojo kulto kunigas", "Šešėlių žudikas demonas", "Apsėsta marmuro statula", "Grandinėtas ogras", "Žvakių liepsnos šmėkla",
            "Rūmų veidrodžio demonas", "Varpinės šmėkla", "Gyvojo rašalo siaubūnas", "Teismo egzekutorius", "Katakombų vampyrų valdovas",
            "Kaukėtasis kulto hierofantas", "Miesto rijikas", "Kruvinasis puolęs angelas", "Senasis Meridiano vartų globėjas", "Užtemimo drakonas",
        ],
    },
]


ROLES = [
    "Medžiotojas", "Persekiotojas", "Sargas", "Pasalūnas", "Ritualistas",
    "Žvalgas", "Naikintojas", "Dvikovininkas", "Spąstų kūrėjas", "Smogikas",
    "Teritorijos sargas", "Skrendantis medžiotojas", "Silpnintojas", "Pramušėjas", "Kontrolierius",
    "Burtininkas", "Greitas puolėjas", "Aukštumų sargas", "Minios laužytojas", "Nuodytojas",
    "Elitinis sargas", "Atgimstantis plėšrūnas", "Būrio vadas", "Senovinis globėjas", "Pasaulio grėsmė",
]

DANGERS = [2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 5, 6, 6, 6, 7, 6, 7, 7, 7, 8, 8, 8, 9, 9, 10]


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def java_string(value: str) -> str:
    return value.replace("\\", "\\\\").replace('"', '\\"')


def build_entries() -> list[dict]:
    entries: list[dict] = []
    global_index = 0
    for group_index, group in enumerate(GROUPS):
        assert len(group["names"]) == 25
        source_path = SOURCE_DIR / f"sheet_{group_index + 1:02d}.png"
        if not source_path.is_file():
            raise FileNotFoundError(source_path)
        with Image.open(source_path) as source:
            source = source.convert("RGB")
            width, height = source.size
            assert width == height and width >= 1000, source.size
            for local_index, name in enumerate(group["names"]):
                global_index += 1
                row, column = divmod(local_index, 5)
                left = round(column * width / 5) + 3
                right = round((column + 1) * width / 5) - 3
                top = round(row * height / 5) + 3
                bottom = round((row + 1) * height / 5) - 3
                portrait = source.crop((left, top, right, bottom)).resize((384, 384), Image.Resampling.LANCZOS)
                filename = f"monster_v091_{global_index:03d}.webp"
                output_path = DRAWABLE_DIR / filename
                file_descriptor, temporary_name = tempfile.mkstemp(prefix=f"{filename}.", suffix=".webp")
                os.close(file_descriptor)
                temporary_path = Path(temporary_name)
                try:
                    portrait.save(temporary_path, "WEBP", quality=94, method=6)
                    os.replace(temporary_path, output_path)
                finally:
                    if temporary_path.exists():
                        temporary_path.unlink()
                    portrait.close()

                danger = DANGERS[local_index]
                role = ROLES[local_index]
                trait = group["traits"][local_index % len(group["traits"])]
                hp = 60 + danger * 42 + (local_index % 5) * 11 + group_index * 5
                attack = 12 + danger * 7 + (local_index % 4) * 3
                defense = 8 + danger * 6 + ((local_index + 1) % 5) * 3
                speed = min(95, 28 + danger * 5 + ((local_index * 7 + group_index * 3) % 17))
                description = (
                    f"{group['habitat']} sutinkamas priešas. Pavojingiausias jo bruožas – {trait}; "
                    f"kovoje jis veikia kaip {role.lower()}."
                )
                entries.append({
                    "id": f"V091-{global_index:03d}",
                    "name": name,
                    "region": group["region"],
                    "role": role,
                    "trait": trait,
                    "description": description,
                    "danger": danger,
                    "hp": hp,
                    "attack": attack,
                    "defense": defense,
                    "speed": speed,
                    "artwork": filename,
                    "artwork_resource": filename.removesuffix(".webp"),
                    "artwork_sha256": sha256(output_path),
                })
    assert global_index == 200
    return entries


def write_java(entries: list[dict]) -> None:
    regions = ", ".join(f'"{java_string(group["region"])}"' for group in GROUPS)
    rows = []
    for entry in entries:
        text_fields = [entry[key] for key in ("id", "name", "region", "role", "trait", "description")]
        args = ", ".join(f'"{java_string(value)}"' for value in text_fields)
        rows.append(
            f"            new Enemy({args}, {entry['danger']}, {entry['hp']}, {entry['attack']}, "
            f"{entry['defense']}, {entry['speed']}, R.drawable.{entry['artwork_resource']})"
        )
    body = ",\n".join(rows)
    java = f'''package lt.vaeloria.ooc;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Generated v0.9.1 catalog: 200 additional bundled, illustrated enemies. */
final class EnemyCatalogV091 {{
    static final class Enemy {{
        final String id, name, region, role, trait, description;
        final int danger, hp, attack, defense, speed, artwork;

        Enemy(String id, String name, String region, String role, String trait, String description,
              int danger, int hp, int attack, int defense, int speed, int artwork) {{
            this.id = id;
            this.name = name;
            this.region = region;
            this.role = role;
            this.trait = trait;
            this.description = description;
            this.danger = danger;
            this.hp = hp;
            this.attack = attack;
            this.defense = defense;
            this.speed = speed;
            this.artwork = artwork;
        }}
    }}

    private static final String[] REGIONS = new String[]{{{regions}}};

    static final Enemy[] ALL = new Enemy[]{{
{body}
    }};

    private static final Map<String, Enemy> BY_NAME = new HashMap<>();

    static {{
        for (Enemy enemy : ALL) BY_NAME.put(normalize(enemy.name), enemy);
    }}

    private EnemyCatalogV091() {{}}

    static String[] regions() {{ return REGIONS.clone(); }}

    static List<Enemy> inRegion(String region) {{
        ArrayList<Enemy> result = new ArrayList<>();
        for (Enemy enemy : ALL) if (enemy.region.equals(region)) result.add(enemy);
        return result;
    }}

    static Enemy find(String value) {{
        String query = normalize(value);
        Enemy exact = BY_NAME.get(query);
        if (exact != null || query.length() < 4) return exact;
        for (Enemy enemy : ALL) {{
            String key = normalize(enemy.name);
            if (query.contains(key) || (query.length() >= 8 && key.contains(query))) return enemy;
            String stem = key.length() > 10 ? key.substring(0, key.length() - 2) : key;
            if (query.contains(stem)) return enemy;
        }}
        return null;
    }}

    static int artFor(String name) {{
        Enemy enemy = find(name);
        return enemy == null ? 0 : enemy.artwork;
    }}

    static Enemy encounterFor(String location, long seed) {{
        String region = regionForLocation(location, seed);
        List<Enemy> candidates = inRegion(region);
        int safePool = Math.min(20, candidates.size());
        int index = (int) Math.floorMod(seed, safePool);
        return candidates.get(index);
    }}

    static String promptRoster() {{
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < ALL.length; index++) {{
            if (index > 0) result.append(", ");
            result.append(ALL[index].name);
        }}
        return result.toString();
    }}

    private static String regionForLocation(String location, long seed) {{
        String query = normalize(location);
        if (containsAny(query, "pelkyn", "labirint", "giria", "misk", "stiklo")) return REGIONS[1];
        if (containsAny(query, "kaln", "kharad", "veyrhold", "asterio", "virsun")) return REGIONS[2];
        if (containsAny(query, "meridian", "vart", "anomal", "orison", "tustum")) return REGIONS[3];
        if (containsAny(query, "dirbtuv", "griuv", "tiglio", "forge")) return REGIONS[4];
        if (containsAny(query, "pakrant", "jura", "safyro", "bedugn", "rif")) return REGIONS[5];
        if (containsAny(query, "siaur", "led", "salc", "audr")) return REGIONS[6];
        if (containsAny(query, "luminara", "miest", "rum", "turgaus")) return REGIONS[7];
        return REGIONS[(int) Math.floorMod(seed, REGIONS.length)];
    }}

    private static String normalize(String value) {{
        String lower = value == null ? "" : value.toLowerCase(Locale.forLanguageTag("lt-LT"));
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\\\p{{M}}+", "")
                .replace('’', '\\'')
                .trim();
    }}

    private static boolean containsAny(String value, String... needles) {{
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }}
}}
'''
    JAVA_FILE.write_text(java, encoding="utf-8")


def main() -> None:
    DRAWABLE_DIR.mkdir(parents=True, exist_ok=True)
    entries = build_entries()
    names = [entry["name"] for entry in entries]
    hashes = [entry["artwork_sha256"] for entry in entries]
    if len(set(names)) != 200 or len(set(hashes)) != 200:
        raise RuntimeError("Enemy names and cropped artwork must both be unique")

    write_java(entries)
    JSON_FILE.write_text(json.dumps({"version": "0.9.1", "count": 200, "enemies": entries}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    manifest = {
        "version": "0.9.1",
        "source_sheets": [
            {"file": f"generated-v091/sheet_{index:02d}.png", "sha256": sha256(SOURCE_DIR / f"sheet_{index:02d}.png")}
            for index in range(1, 9)
        ],
        "output_count": len(entries),
        "outputs": [
            {"file": f"vaeloria-android/app/src/main/res/drawable-nodpi/{entry['artwork']}", "sha256": entry["artwork_sha256"]}
            for entry in entries
        ],
    }
    MANIFEST_FILE.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    total_bytes = sum((DRAWABLE_DIR / entry["artwork"]).stat().st_size for entry in entries)
    print(f"Generated {len(entries)} unique portraits ({total_bytes / 1024 / 1024:.1f} MiB)")
    print(JAVA_FILE.relative_to(ROOT))
    print(JSON_FILE.relative_to(ROOT))
    print(MANIFEST_FILE.relative_to(ROOT))


if __name__ == "__main__":
    main()
