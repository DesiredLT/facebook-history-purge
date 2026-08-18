# Vaeloria OOC v1.0.0

„Vaeloria OOC“ – vietinis vieno žaidėjo dark-fantasy RPG, kurio pasaulį gali valdyti DI žaidimo meistras. Žaidimas yra gimtoji „Android“ programėlė: pagrindinės taisyklės, kovos, inventorius, progresija ir išsaugojimai veikia telefone, o Groq integracija yra pasirenkamas naratyvo sluoksnis.

## v1.0 žaidimo sistemos

- Deterministinė kelių ėjimų kova su priešo statistika, ketinimais, būsenomis, įrangos, setų, talentų ir kompanionų mechanika.
- 325 iliustruoti daiktai, 19 kategorijų, 8 retumo pakopos, 10 šešių dalių setų, veikiantys potionai, gamyba, prekyba ir tikros grobio lentelės.
- 218 iliustruotų monstrų su paieška, regionais, vaidmenimis, statistika ir tiksliomis iškritimo tikimybėmis.
- Veikėjo kūrimas: vardas, tapatybė, išvaizda, 6 kilmės, 6 archetipai ir lygiai 3 iš 12 mechaninių bruožų.
- Subalansuota 1–100 progresija arba pasirenkama legendinė pradžia, 20 talentų penkiose šakose ir 4 sunkumo režimai.
- Pagrindinė šešių etapų istorija su trimis ilgalaikėmis baigtimis, šalutinės užduotys, įrodymai ir struktūrizuoti tikslai.
- NPC atmintis, santykiai, paros prieinamumas, parduotuvės, ekonomikos indeksai, pasaulio įvykiai, frakcijos, miestų būsena, verslai ir samdomi darbuotojai.
- Lyra, Kaelis ir Mirel kaip prisiviliojami kompanionai su lojalumu ir realiu poveikiu kovai bei pasirinkimams.
- Atlaso atradimai, nežinomų vietų rūkas, autoritetingas kelionės laikas, interaktyvus žemėlapis ir ekrano skaitytuvui skirtas vietų sąrašas.
- Automatinis išsaugojimas, 20 pilnų atšaukimo kontrolinių taškų, 3 vardiniai išsaugojimo lizdai ir JSON eksportas / importas.
- Didesnio teksto, mažesnio judesio, spalvų skyrimo, haptikos bei garsumo nustatymai; procedūrinis neprisijungus veikiantis aplinkos garsas.

## Architektūra ir saugumas

- Java 17, gimtoji „Android“ UI ir SQLite 11 schema.
- Vietinis resolveris leidžia žaisti be interneto ir be API rakto.
- Pasirenkamas Groq `openai/gpt-oss-120b` žaidimo meistras su griežta JSON Schema sutartimi.
- Groq raktas šifruojamas „Android Keystore“, neįtraukiamas į eksportus ar atsargines kopijas.
- Tik HTTPS ryšys, vienintelis leidimas – `android.permission.INTERNET`, `allowBackup=false`, release buildas ne-debug ir optimizuojamas R8.
- Išlaikytas paketo ID `lt.vaeloria.ooc.personal` ir stabilus atnaujinimų pasirašymo sertifikatas.

## Build ir kokybės vartai

GitHub Actions su „Android 35“ ir „Gradle 8.11.1“ vykdo vienetinius testus, v3→v11 SQLite migraciją, pilno undo bei išsaugojimo lizdų patikras, realų 360dp įrenginio UI testą, release APK kompiliavimą, išteklių, leidimų, parašo ir įterptų paslapčių auditą. Patikrintas APK publikuojamas GitHub Releases kaip `Vaeloria-OOC-v1.0.0.apk`.
