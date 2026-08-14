from pathlib import Path

path = Path("app/src/main/java/lt/vaeloria/ooc/VaeloriaActivity.java")
text = path.read_text(encoding="utf-8")

if "PERŽIŪRĖTI VISAS 92 SAVYBES" in text:
    raise SystemExit(0)

needle = 'stats.addView(t("Toliau augama per meistriškumą, principus, technikas ir pasaulio pažinimą, o ne papildomus bazinių savybių taškus.",11,MUT,false));c.addView(stats,m(dp(9)));'
replacement = 'stats.addView(t("Toliau augama per meistriškumą, principus, technikas ir pasaulio pažinimą, o ne papildomus bazinių savybių taškus.",11,MUT,false));Button allStats=outline("PERŽIŪRĖTI VISAS 92 SAVYBES");allStats.setOnClickListener(v->startActivity(new Intent(this,StatsActivity.class)));stats.addView(allStats,m(dp(8)));c.addView(stats,m(dp(9)));'

if needle not in text:
    raise SystemExit("Nepavyko rasti PAŽANGOS bloko VaeloriaActivity.java")

path.write_text(text.replace(needle, replacement, 1), encoding="utf-8")
