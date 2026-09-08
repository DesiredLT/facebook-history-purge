# Vaeloria OOC v1.1.1

Vietinis vieno žaidėjo fantastinis Android RPG. Telefonas apskaičiuoja kovą, progresiją, daiktus, keliones ir saugo SQLite būseną. Pasirenkamas OpenAI arba Groq pasakotojas aprašo patvirtintą rezultatą lietuviškai.

## Šio leidimo pakeitimai

- OpenAI Responses API integracija per atskirą žaidimo paslaugą. Numatytas serverio modelis `gpt-6-astra`; jį galima pakeisti `OPENAI_MODEL` konfigūracijoje.
- Abu DI tiekėjai gali grąžinti tik scenos pavadinimą, tekstą ir tris pasirinkimus. Modelis nebegali suteikti pinigų, grobio, patirties, kelionių ar kovos pergalių.
- Užklausa naudoja atskirą, atšaukiamą HTTP jungtį ir nekintamą būsenos kopiją. Laukiant pasakojimo sustabdomi kiti būseną keičiantys veiksmai; pavėluotas atsakymas nebepritaikomas.
- Visas ėjimas įrašomas viena SQLite transakcija. Klaida nepalieka tik dalies atlygio ar meistriškumo pakeitimų.
- Pataisytas kelionės tikslo parinkimas, grįžimas į Luminara, neatrastų vietų kelionės, NPC pokalbių vietos ir darbo laiko patikra, seno JSON importo profilio migracija ir failo dydžio ribojimas skaitant.

## Žaidime jau yra

325 iliustruoti daiktai, 8 retumo pakopos, 10 šešių dalių setų, 218 bestiarijaus įrašų, 27 kelių ingredientų receptai ir 25 talentai penkiose šakose. Veikėjas turi pasirenkamą kilmę, archetipą ir tris mechaninius bruožus; progresija apima 1–100 lygius bei paskirstomus savybių taškus. Veikia kelių ėjimų kova, prekyba, vartojami daiktai, kompanionai, pagrindinė šešių etapų istorija, atlasas, trys išsaugojimo lizdai ir iki 20 atšaukimo taškų.

Šalutinių užduočių ir pasaulio simuliacijos sistemos dar neužbaigtos. Tikslus likusių darbų sąrašas pateiktas [2026-09-07 audite](v100/AUDIT-2026-09-07.md). Šaltinio struktūros patikra nėra viso P0–P4 plano užbaigimo įrodymas.

## OpenAI prijungimas

Paslaugos paleidimas, slapti kintamieji, HTTPS ir Android nustatymai aprašyti [OpenAI prijungimo instrukcijoje](ai-service/README.md). Providerio raktas saugomas tik serveryje. Telefone naudojamas atskiras įrenginio prisijungimo kodas, užšifruotas Android Keystore ir neįtraukiamas į žaidimo eksportą.

Diegimas savaime neaktyvuoja mokamo API: reikalinga veikianti paslauga su tikru `OPENAI_API_KEY`, o telefone – jos HTTPS adresas ir įrenginio kodas. Be jų žaidimas veikia vietiniu režimu. Ankstesnis išsaugotas Groq raktas išlieka; tiekėją galima pasirinkti nustatymuose.

## Kūrimas ir patikra

- Java 17, Android minSdk 26 / targetSdk 35, SQLite schema 13, Gradle 8.11.1.
- `node --test ai-service/test/*.test.mjs` – vietinė HTTP paslauga su imituotu OpenAI atsakymu; mokamo API nekviečia.
- `python3 v100/audit_v100.py` – šaltinio struktūros, manifestų ir iliustracijų patikra.
- Android kataloge: `gradle :app:testDebugUnitTest :app:assembleRelease`.
- Android 35 emuliatoriuje, 360dp pločiu: `gradle :app:connectedDebugAndroidTest`.

GitHub Actions vykdo serverio, Java, migracijų, Android sąsajos ir išsaugojimo testus, tada tikrina optimizuoto APK tapatybę, leidimus, išteklius, parašą bei paslapčių nebuvimą. Paketo ID: `lt.vaeloria.ooc.personal`. Leidimui būtinas ankstesnis pasirašymo raktas; jo savavališkai nekeisti (žr. [pasirašymą](vaeloria-android/SIGNING.md)).
