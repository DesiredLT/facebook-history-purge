from pathlib import Path
p=Path('app/src/main/java/lt/vaeloria/ooc/PolishedActivity.java')
s=p.read_text(encoding='utf-8')
# Generated source contains one literal backslash before each regex quote.
s=s.replace('Pattern.compile(\\"','Pattern.compile("')
s=s.replace('))\\").matcher','))").matcher')
# Generated source contains four backslashes for regex tokens; Java source needs two.
s=s.replace(r'\\\\s',r'\\s')
s=s.replace(r'\\\\d',r'\\d')
p.write_text(s,encoding='utf-8')
s=p.read_text(encoding='utf-8')
assert 'Pattern.compile(\\"' not in s
assert r'\\\\s' not in s and r'\\\\d' not in s
assert 'Pattern.compile("(?i)(Meistriškumas\\s*' in s
print('v0.8.2 Java regex source OK')
