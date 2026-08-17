from pathlib import Path
p=Path('app/src/main/java/lt/vaeloria/ooc/PolishedActivity.java')
s=p.read_text(encoding='utf-8')
s=s.replace(r'Pattern.compile(\"','Pattern.compile("')
s=s.replace(r'))\").matcher','))").matcher')
s=s.replace(r'\\\\s',r'\\s')
s=s.replace(r'\\\\d',r'\\d')
p.write_text(s,encoding='utf-8')
if r'Pattern.compile(\"' in s: raise SystemExit('escaped regex quote remains')
if r'\\\\s' in s or r'\\\\d' in s: raise SystemExit('overescaped regex remains')
assert 'Pattern.compile("(?i)(Meistriškumas\\s*' in s
print('v0.8.2 generated Java string fix OK')
