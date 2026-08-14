from pathlib import Path

p = Path(__file__).resolve().parents[1] / "app/src/main/AndroidManifest.xml"
s = p.read_text(encoding="utf-8")
s = s.replace('        android:debuggable="false"\n', '')
p.write_text(s, encoding="utf-8")
print("Removed hardcoded manifest debuggable; release buildType remains debuggable=false")
