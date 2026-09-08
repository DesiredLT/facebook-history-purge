# Vaeloria APK pasirašymas

Ankstesni diegiami APK naudoja stabilų sertifikatą:

`07F9E852AF8F51562E2F621B270BB2097C9CA00E433B633D4F85A3EEAC746C88`

Raktas anksčiau buvo laikomas tik GitHub Actions talpykloje `vaeloria-android-debug-signing-v1`. 2026-09-07 CI joje rakto neberado. Talpykla nėra patikima ilgalaikė privataus rakto atsarginė kopija. Sertifikatas APK faile nėra privatus raktas ir negali jo pakeisti.

## Rakto atkūrimas

Jei turi originalų `debug.keystore`, patikrink jo sertifikatą (`androiddebugkey`, ankstesnis saugyklos slaptažodis `android`):

```sh
keytool -list -v -keystore /saugus/kelias/debug.keystore -alias androiddebugkey
```

Originalios saugyklos Base64 turinį įrašyk į repo Actions paslaptį `VAELORIA_KEYSTORE_B64`. Jo neskelbk pokalbyje ar Git. Workflow pirmiausia naudoja šią paslaptį, tada seną talpyklos kopiją, ir abiem atvejais reikalauja tikslaus sertifikato atitikmens. Netinkamas pateiktas raktas sustabdo darbą.

CI turi tikrą raktą laikyti atskirai nuo naujo laikino emuliatoriaus rakto. `VAELORIA_RELEASE_KEYSTORE` nurodo originalo failą, `VAELORIA_SIGNED_RELEASE=true` nustatomas tik po sertifikato patikros.

## Kai originalo nėra

Vienetiniai ir emuliatoriaus testai vykdomi su testavimo parašu. Release kodas surenkamas ir audituojamas kaip **nepasirašytas** `Vaeloria-OOC-v1.1.1-unsigned.apk`. Tai nėra diegiamas ankstesnės versijos atnaujinimas. Publikavimo žingsnis lieka išjungtas, o artefakto pavadinime yra `signed-false`.

Nepradėk automatiškai platinti nauju raktu pasirašyto to paties paketo. Android jo nepriims kaip seno žaidimo atnaujinimo. Jei originalas nebeatkuriamas, atskiram platinimui ir pažangos perkėlimui reikia atskiro sprendimo; šis pakeitimas jo neatlieka.
