from pathlib import Path
p=Path('app/src/main/java/lt/vaeloria/ooc/PolishedActivity.java')
s=p.read_text(encoding='utf-8')
start=s.find('    String feedbackChanges082(){')
end=s.find('    void feedbackDetails082(){',start)
if start<0 or end<0: raise SystemExit('feedbackChanges082 boundaries missing')
new='''    String feedbackChanges082(){
        String f=feedback==null?"":feedback;ArrayList<String> out=new ArrayList<>();
        for(String part:f.split("·")){String q=part.trim();String low=q.toLowerCase(Locale.ROOT);if(low.contains("meistriškumas")||low.contains("ištvermė")||low.contains("mana")||low.contains("gyvybė")||low.contains("eonas")){if(q.length()>42)q=q.substring(0,41)+"…";out.add(q);if(out.size()>=3)break;}}
        if(out.isEmpty())return "Pasaulio būsena atnaujinta.";return android.text.TextUtils.join(" · ",out);
    }
'''
s=s[:start]+new+s[end:]
p.write_text(s,encoding='utf-8')
s=p.read_text(encoding='utf-8')
assert 'String feedbackChanges082(){' in s
assert 'java.util.regex' not in s
print('v0.8.2 feedback parser OK')
