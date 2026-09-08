# Vaeloria OOC v1.2.0

Vietinis vieno žaidėjo fantastinis Android RPG. Telefonas apskaičiuoja kovą, progresiją, daiktus, keliones ir saugo SQLite būseną. Pasirenkamas OpenAI arba Groq pasakotojas aprašo patvirtintą rezultatą lietuviškai.

## Šio leidimo pakeitimai

- Pergalė, pralaimėjimas ir atsitraukimas atskiriami: grobis suteikiamas tik laimėjus. Kovos reikmenys naudoja bendrą žalos, gynybos, sunkumo ir vienkartinių apsaugų sistemą; visas panaudojimas įrašomas viena transakcija.
- Priešai turi skirtingus smūgius, gebėjimo kaupimą ir kontrolę. Sunkus smūgis nutraukia kaupimą, gynyba stipriau saugo nuo sunkaus smūgio, o pavojingiausi priešai turi paskutinę fazę. Įrengtas lankas atsižvelgia į atstumą.
- Penkios šalutinės užduotys turi priėmimą, sekimą, tris etapus ir vienkartinį atlygį. Reagentų užduočiai reikia surinkti ir atiduoti tikrus inventoriaus daiktus.
- Pagrindinės istorijos progresą riboja vietos, tikri įrodymai ir NPC prieinamumas. Veikia visi trys siūlomi istorijos baigčių veiksmai. Kelionėms reikia atrastų maršruto jungčių, ekonomika juda pagal praėjusį pasaulio laiką.
- Patikslintas lietuviškų veiksmų ir linksniuotų vardų atpažinimas. Nepradėti veiksmai nesuteikia patirties ir nekeičia laiko. Nauja lygių kreivė išsaugo seno veikėjo lygį ir santykinį progresą.
- Inventoriuje rodomas įrangos ir setų premijų palyginimas, pardavimo kaina bei patvirtinimas. Mėgstami daiktai apsaugoti nuo pardavimo; didelio teksto režimu turimų daiktų sąrašas tampa vieno stulpelio.
- DI keičia tik patvirtintos scenos pavadinimą ir pasakojimą. Veiksmų mygtukai išlieka vietinio žaidimo patikrinti pasirinkimai.

## Žaidime jau yra

325 iliustruoti daiktai, 8 retumo pakopos, 10 šešių dalių setų, 218 bestiarijaus įrašų, 27 kelių ingredientų receptai ir 25 talentai penkiose šakose. Veikėjas turi pasirenkamą kilmę, archetipą ir tris mechaninius bruožus; progresija apima 1–100 lygius bei paskirstomus savybių taškus. Veikia kelių ėjimų kova, prekyba, vartojami daiktai, kompanionai, pagrindinė šešių etapų istorija, atlasas, trys išsaugojimo lizdai ir iki 20 atšaukimo taškų.

Šio pataisymų etapo rezultatai ir likusios ribos aprašyti [2026-09-08 patikroje](v100/AUDIT-2026-09-08.md). Ilgų sesijų balansas, viso turinio meninė kokybė ir tikras mokamas DI generavimas dar nėra patvirtinti vien automatiniais testais.

## OpenAI prijungimas

Paslaugos paleidimas, slapti kintamieji, HTTPS ir Android nustatymai aprašyti [OpenAI prijungimo instrukcijoje](ai-service/README.md). Providerio raktas saugomas tik serveryje. Telefone naudojamas atskiras įrenginio prisijungimo kodas, užšifruotas Android Keystore ir neįtraukiamas į žaidimo eksportą.

Diegimas savaime neaktyvuoja mokamo API: reikalinga veikianti paslauga su tikru `OPENAI_API_KEY`, o telefone – jos HTTPS adresas ir įrenginio kodas. Be jų žaidimas veikia vietiniu režimu. Ankstesnis išsaugotas Groq raktas išlieka; tiekėją galima pasirinkti nustatymuose.

## Kūrimas ir patikra

- Java 17, Android minSdk 26 / targetSdk 35, SQLite schema 14, Gradle 8.11.1.
- `node --test ai-service/test/*.test.mjs` – vietinė HTTP paslauga su imituotu OpenAI atsakymu; mokamo API nekviečia.
- `python3 v100/audit_v100.py` – šaltinio struktūros, manifestų ir iliustracijų patikra.
- Android kataloge: `gradle :app:testDebugUnitTest :app:assembleRelease`.
- Android 26 ir 35 emuliatoriuose, 360dp pločiu: `gradle :app:connectedDebugAndroidTest`.

GitHub Actions vykdo serverio, Java, migracijų, Android sąsajos ir išsaugojimo testus, tada tikrina optimizuoto APK tapatybę, leidimus, išteklius, parašą bei paslapčių nebuvimą. Paketo ID: `lt.vaeloria.ooc.personal`. Leidimui būtinas ankstesnis pasirašymo raktas; jo savavališkai nekeisti (žr. [pasirašymą](vaeloria-android/SIGNING.md)).
