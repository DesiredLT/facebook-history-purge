from pathlib import Path

path = Path("app/src/main/java/lt/vaeloria/ooc/VaeloriaActivity.java")
text = path.read_text(encoding="utf-8")

bad = 'feedback="⟳ Sprendžiama…' + chr(10) + '"+pendingCheck.compact()'
good = 'feedback="⟳ Sprendžiama…\\n"+pendingCheck.compact()'

if bad in text:
    text = text.replace(bad, good)

if 'feedback="⟳ Sprendžiama…' + chr(10) in text:
    raise SystemExit("Sugeneruotame Java faile liko neteisingas eilutės lūžis")

path.write_text(text, encoding="utf-8")
