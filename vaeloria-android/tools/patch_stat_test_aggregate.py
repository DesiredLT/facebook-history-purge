from pathlib import Path
import re

p=Path('app/src/test/java/lt/vaeloria/ooc/StatEngineTest.java')
s=p.read_text(encoding='utf-8')
old='''        for(String[] x:cases){
            expect(x[0],x[1]);
            assertTrue("Duplicate semantic coverage for "+x[1],covered.add(x[1]));
        }
        assertEquals(92,covered.size());'''
new='''        StringBuilder mismatches=new StringBuilder();
        for(String[] x:cases){
            String actual=StatEngine.classifyForTest(x[0])[0];
            if(!x[1].equals(actual)) mismatches.append("\\n").append(x[0]).append(" expected ").append(x[1]).append(" but was ").append(actual);
            assertTrue("Duplicate semantic coverage for "+x[1],covered.add(x[1]));
        }
        assertEquals(92,covered.size());
        assertEquals("Natural-action classifier mismatches:"+mismatches,"",mismatches.toString());'''
if old in s:
    s=s.replace(old,new,1)
elif 'StringBuilder mismatches=new StringBuilder();' not in s:
    raise SystemExit('92-action loop not found')
p.write_text(s,encoding='utf-8')
