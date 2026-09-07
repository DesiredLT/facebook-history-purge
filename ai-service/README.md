# OpenAI prijungimas prie Vaeloria

Paslauga paruošta OpenAI Responses API. Jos raktas lieka serveryje. Android siunčia trumpą kontekstą su jau apskaičiuotu ėjimu ir gauna lietuvišką pasakojimą; žaidimo mechanikos modelis nekeičia.

## 1. Serverio konfigūracija

Naudok Node.js 22 arba naujesnį. Nukopijuok `.env.example` į `ai-service/.env` ir užpildyk privačioje serverio aplinkoje:

| Kintamasis | Reikšmė |
| --- | --- |
| `OPENAI_API_KEY` | Tikras OpenAI projekto API raktas. Neįkelti į GitHub, APK ar pokalbį. |
| `OPENAI_MODEL` | Numatytasis `gpt-6-astra`; modelį parenka administratorius. |
| `VAELORIA_CLIENT_TOKENS` | Atskiri atsitiktiniai kodai įrenginiams, atskirti kableliais. |
| `HOST`, `PORT` | Docker pavyzdyje `0.0.0.0`, `8080`. |

Vienam įrenginiui sugeneruok kodą ir išsaugok jį privačiai:

```sh
node -e "process.stdout.write(require('node:crypto').randomBytes(32).toString('base64url'))"
```

Atskiras kodas leidžia atšaukti vieną įrenginį. Atskleistą kodą pašalink iš sąrašo ir perkrauk paslaugą. Bendras visiems naudotojams kodas į APK neįrašomas.

Paleidimas iš repo šaknies:

```sh
node --env-file=ai-service/.env ai-service/server.mjs
```

Arba Docker:

```sh
docker build -f ai-service/Dockerfile -t vaeloria-ai .
docker run --name vaeloria-ai --restart unless-stopped --env-file ai-service/.env -p 127.0.0.1:8080:8080 vaeloria-ai
```

`.env` nepatenka nei į Git, nei į Docker atvaizdą. npm priklausomybės nereikalingos. Be būtino rakto ir įrenginių kodų procesas nepasileidžia.

## 2. HTTPS adresas

Paslaugą publikuok turimame serveryje arba konteinerių platformoje su galiojančiu HTTPS sertifikatu. Caddy pavyzdys su savo domenu:

```caddyfile
tavo-di-domenas.lt {
    reverse_proxy 127.0.0.1:8080
}
```

`GET /health` tikrina procesą; OpenAI nekviečia ir rakto galiojimo nepatvirtina. Tikras generavimas vyksta per autentifikuotą `POST /v1/turn`. Android atmeta HTTP, URL su prisijungimo duomenimis, query/fragmentus ir peradresavimus. Įvesk bazinį adresą be `/v1/turn` priesagos.

## 3. Android prijungimas

1. Įdiek v1.1.1 ir atidaryk **Nustatymai → Prijungti OpenAI**.
2. Įvesk paslaugos HTTPS adresą ir atskirą įrenginio kodą.
3. Spausk **Patikrinti OpenAI ryšį**. Tai vienas tikras, pagal API kainodarą apmokestinamas generavimas, nekeičiantis pažangos.
4. Atlik veiksmą žaidime. Kilus API klaidai, panaudojamas to paties vietinio rezultato atsarginis pasakojimas.

OpenAI raktas į Android lauką nevedamas. ChatGPT pokalbis ir prenumerata nėra šios paslaugos API prisijungimas. API projekto raktas ir prieiga prie modelio turi būti sutvarkyti serverio paskyroje.

## Sutartis ir ribos

- `POST /v1/turn`: `Authorization: Bearer <įrenginio kodas>`, JSON `{ "context": "…" }`. Kliento modeliai, URL ar sisteminės instrukcijos nepriimami kaip konfigūracija.
- Atsakymas: `{ "narration": { "scene_title": "…", "scene": "…", "choices": ["…", "…", "…"] }, "request_id": "…" }`.
- Abiejų tiekėjų politika ir schema: `vaeloria-android/app/src/main/assets/ai/`.
- 64 KiB įvestis, iki 24 000 konteksto simbolių, 128 KiB providerio atsakymas. Viena užklausa, `reasoning.effort=low`, iki 4096 išvesties žetonų, be automatinio pakartojimo ir įrankių.
- 8 užklausos per minutę ir 100 per UTC dieną vienam kodui, vienas jo aktyvus generavimas, iki 4 bendrų generavimų. Ribos skaičiuojamos viename procese ir perkrovus prasideda nuo nulio. Kelioms serverio kopijoms ir viešam registravimui reikalinga bendra kvotų saugykla bei naudotojų autentifikacija; šis leidimas skirtas asmeniniam / mažos grupės naudojimui.
- 35 s OpenAI laukimo terminas. Atšaukimas nutraukia laukimą, tačiau negarantuoja jau pradėtos užklausos mokesčio panaikinimo.
- `store:false` taikomas Responses objekto saugojimui; tai nėra pažadas, kad tiekėjas neturi kitų saugojimo taisyklių. Žurnaluose lieka tik užklausos ID, statusas ir trukmė; scenos ir raktai nefiksuojami.
- Griežta schema patikrina formą. Mobilus lietuvių kalbos filtras nėra pilnas gramatikos ar semantikos tikrintuvas.

Klaidos: 401 – įrenginio kodas; 400/413/415 – įvestis; 422 – modelio atsisakymas; 429 – kvota; 502 – providerio / atsakymo klaida; 503 – užimta paslauga; 504 – laukimo terminas.

## Patikra ir šaltiniai

`node --test ai-service/test/*.test.mjs` tikrina vietinį HTTP srautą su imituotu provideriu, limitais, atšaukimu ir klaidomis. CI taip pat vykdo Android testus. Tikro rakto prieigą prie Astra patikrina 3 žingsnis jau paleistoje paslaugoje.

Integracija remiasi oficialia [Structured Outputs dokumentacija](https://developers.openai.com/api/docs/guides/structured-outputs), [API autentifikacija](https://developers.openai.com/api/reference/overview#authentication) ir [GPT-6 Astra modeliu](https://developers.openai.com/api/docs/models/gpt-6-astra).
