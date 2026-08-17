from pathlib import Path
p=Path('app/src/main/java/lt/vaeloria/ooc/PolishedActivity.java')
s=p.read_text(encoding='utf-8')
# patch_v082.py uses a raw Python block; remove the literal Java quote escapes it emitted.
s=s.replace(r'Pattern.compile(\"','Pattern.compile("') if r'Pattern.compile(\"' in s else s
s=s.replace(r'Pattern.compile(\"','Pattern.compile("')
# Exact one-backslash form actually produced by the raw block.
s=s.replace('Pattern.compile(\\"','Pattern.compile("')
s=s.replace('))\\").matcher','))").matcher')
# Convert four source backslashes to the two Java-source backslashes required by regex \s / \d.
s=s.replace(r'\\\\s',r'\\s').replace(r'\\\\d',r'\\d')
p.write_text(s,encoding='utf-8')
s=p.read_text(encoding='utf-8')
if 'Pattern.compile(\\"' in s: raise SystemExit('literal escaped Pattern.compile quote remains')
if r'\\\\s' in s or r'\\\\d' in s: raise SystemExit('regex still overescaped')
if 'Pattern.compile("(?i)(Meistriškumas\\s*' not in s: raise SystemExit('final regex signature missing')
print('v0.8.2 Java regex generation verified')
