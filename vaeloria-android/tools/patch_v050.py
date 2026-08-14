from pathlib import Path

p = Path('app/src/main/java/lt/vaeloria/ooc/VaeloriaActivity.java')
s = p.read_text(encoding='utf-8')

old = 'o.put("time_minutes",1);'
new = 'o.put("time_minutes",0);'
if old not in s:
    raise SystemExit('Expected local fallback time mutation not found')
s = s.replace(old, new, 1)

old = 'if(k.length()<10)throw new Exception();SecureKeyStore.save(this,k);'
new = 'if(!k.startsWith("gsk_")||k.length()<20)throw new Exception();SecureKeyStore.save(this,k);'
if old not in s:
    raise SystemExit('Expected API key validation pattern not found')
s = s.replace(old, new, 1)

p.write_text(s, encoding='utf-8')

# Build-time assertions: local fallback must preserve canonical time and the key must look like a Groq key.
check = p.read_text(encoding='utf-8')
assert 'o.put("time_minutes",0);' in check
assert 'k.startsWith("gsk_")' in check
print('v0.5 reliability patch applied')
