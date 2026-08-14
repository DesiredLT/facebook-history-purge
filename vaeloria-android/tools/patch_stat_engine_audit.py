from pathlib import Path

p=Path('app/src/main/java/lt/vaeloria/ooc/StatEngine.java')
s=p.read_text(encoding='utf-8')

# Every canonical catalog name must always resolve to itself, independently of natural-language rules.
needle='''    private static void chooseStats(Check c,String a){\n        // MAGIJA IR RELIKVIJOS.'''
repl='''    private static void chooseStats(Check c,String a){\n        String canonical=canonicalExact(a);\n        if(canonical!=null){pick(c,canonical,defaultSecondary(canonical));return;}\n        // Cross-word semantic rules: action intent must beat nouns that merely describe the threat/object.\n        if(has(a,"staiga sureaguoti","sureaguoti","sureaguoju","reakcijos greitis")){pick(c,"Reakcijos greitis","Refleksai");return;}\n        if(has(a,"kojų darbą","kojų darbas","naudoti kojų darbą")){pick(c,"Kojų darbas","Krypties keitimo greitis");return;}\n        if((has(a,"aktyvuoti","aktyvuoju","įjungti","įjungiu") && has(a,"artefaktą","artefaktas","artefakto"))){pick(c,"Relikvijų rezonansas","Magijos jutimas");return;}\n        if(has(a,"suderinti","suderinu") && has(a,"judesius","judesių")){pick(c,"Koordinacija","Judesių tikslumas");return;}\n        if(has(a,"išlaikyti","laikyti") && has(a,"rankomis","ranka","rankose")){pick(c,"Suėmimo jėga","Raumenų ištvermė");return;}\n        if(has(a,"valdyti","valdau") && has(a,"ginklą","ginklu","ginklus")){pick(c,"Ginklų valdymas","Atakos tikslumas");return;}\n        if(has(a,"suprasti","suprantu") && has(a,"principą","principo","principus")){pick(c,"Intelektas","Loginis mąstymas");return;}\n        if(has(a,"parengti","sudaryti") && has(a,"planą","plano")){pick(c,"Planavimas","Strateginis mąstymas");return;}\n        if(has(a,"pritaikyti","pritaikau") && has(a,"žinias","žinių")){pick(c,"Žinių pritaikymas","Intelektas");return;}\n        // MAGIJA IR RELIKVIJOS.'''
if 'String canonical=canonicalExact(a);' not in s:
    if needle not in s: raise SystemExit('chooseStats insertion point not found')
    s=s.replace(needle,repl,1)
elif 'kojų darbą' not in s:
    anchor='''        if(has(a,"staiga sureaguoti","sureaguoti","sureaguoju","reakcijos greitis")){pick(c,"Reakcijos greitis","Refleksai");return;}'''
    addition=anchor+'''\n        if(has(a,"kojų darbą","kojų darbas","naudoti kojų darbą")){pick(c,"Kojų darbas","Krypties keitimo greitis");return;}'''
    if anchor not in s: raise SystemExit('reaction priority anchor not found')
    s=s.replace(anchor,addition,1)

# Healing phrase where "mano" is a possessive pronoun, not mana.
s=s.replace(
    '"gydyti žaizdą","gydau žaizdą","užgydyti","išgydyti","atkurti kūną","atkurti sveikatą"',
    '"gydyti žaizdą","gydau žaizdą","užgydyti","išgydyti","atkurti kūną","atkurti sveikatą","atkurti mano sveikatą","atkurti savo sveikatą"'
)

# Natural first-person strike and dodge forms.
s=s.replace(
    '"smūgiavimo technika","smūgiuoti","smūgiuoju","smogti","spirti"',
    '"smūgiavimo technika","smūgiuoti","smūgiuoju","smogti","smogiu","spirti"'
)
s=s.replace(
    '"išsisukti","vengti","atšokti","išvengti","refleksai"',
    '"išsisukti","išsisuku","vengti","vengiu","atšokti","atšoku","išvengti","išvengiu","refleksai"'
)

# Helpers for exact canonical names and a sensible supporting stat.
marker='''    private static void pick(Check c,String primary,String secondary){c.primary=primary;c.secondary=secondary;}\n'''
helpers='''    private static String canonicalExact(String action){\n        String a=fold(action).trim();\n        for(BaseStatCatalog.Group g:BaseStatCatalog.GROUPS){\n            for(String stat:g.stats)if(fold(stat).equals(a))return stat;\n        }\n        return null;\n    }\n\n    private static String defaultSecondary(String primary){\n        if(inGroup(primary,"KŪNO SAVYBĖS"))return primary.equals("Kūno kontrolė")?"Koordinacija":"Kūno kontrolė";\n        if(inGroup(primary,"JUDĖJIMAS IR REFLEKSAI"))return primary.equals("Koordinacija")?"Refleksai":"Koordinacija";\n        if(inGroup(primary,"KOVOS MEISTRIŠKUMAS"))return primary.equals("Kovinė nuojauta")?"Kovinis laiko parinkimas":"Kovinė nuojauta";\n        if(inGroup(primary,"JUTIMAI IR IŠGYVENIMAS"))return primary.equals("Pastabumas")?"Regėjimas":"Pastabumas";\n        if(inGroup(primary,"PROTINĖS SAVYBĖS"))return primary.equals("Intelektas")?"Loginis mąstymas":"Intelektas";\n        if(inGroup(primary,"MAGINĖS SAVYBĖS"))return primary.equals("Manos kontrolė")?"Burtų tikslumas":"Manos kontrolė";\n        if(inGroup(primary,"SOCIALINĖS IR PRAKTINĖS SAVYBĖS"))return primary.equals("Žmonių perpratimas")?"Empatija":"Žmonių perpratimas";\n        return "Sprendimų greitis";\n    }\n\n'''
if 'private static String canonicalExact(String action)' not in s:
    if marker not in s: raise SystemExit('pick() marker not found')
    s=s.replace(marker,marker+'\n'+helpers,1)

p.write_text(s,encoding='utf-8')
