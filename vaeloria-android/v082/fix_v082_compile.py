from pathlib import Path
p=Path('app/src/main/java/lt/vaeloria/ooc/PolishedActivity.java')
s=p.read_text(encoding='utf-8')
old='int col=fail?Color.rgb(210,102,78):V070_GREEN;LinearLayout box=col();box.setPadding(dp(12),dp(9),dp(12),dp(9));box.setBackground(round(Color.rgb(11,30,29),13,Color.argb(145,Color.red(col),Color.green(col),Color.blue(col))));'
new='int accent=fail?Color.rgb(210,102,78):V070_GREEN;LinearLayout box=col();box.setPadding(dp(12),dp(9),dp(12),dp(9));box.setBackground(round(Color.rgb(11,30,29),13,Color.argb(145,Color.red(accent),Color.green(accent),Color.blue(accent))));'
if old not in s: raise SystemExit('feedback shadow target missing')
s=s.replace(old,new)
s=s.replace('h.addView(txt(fail?"NESĖKMĖ":"SĖKMĖ",9,col,true),new LinearLayout.LayoutParams(0,-2,1));h.addView(chip070("REZULTATAS",col));','h.addView(txt(fail?"NESĖKMĖ":"SĖKMĖ",9,accent,true),new LinearLayout.LayoutParams(0,-2,1));h.addView(chip070("REZULTATAS",accent));')
old2='String t=raw.toLowerCase(Locale.ROOT),label;int col;if(t.contains("technique")||t.contains("technik")){label="TECHNIKA";col=V070_BLUE;}else if(t.contains("refinement")||t.contains("tobulin")){label="TOBULINIMAS VIRŠ RIBOS";col=Color.rgb(79,169,204);}else if(t.contains("principle")||t.contains("princip")){label="PRINCIPAS VIRŠ RIBOS";col=V070_PURPLE;}else if(t.contains("unknown")||t.contains("nežinom")){label="NEŽINOMA KLASĖ";col=Color.rgb(194,92,178);}else{label="GEBĖJIMAS VIRŠ RIBOS";col=GOLD2;}LinearLayout r=col();r.setPadding(dp(10),dp(9),dp(10),dp(9));r.setBackground(round(Color.rgb(12,24,32),12,Color.argb(145,Color.red(col),Color.green(col),Color.blue(col))));'
new2='String t=raw.toLowerCase(Locale.ROOT),label;int accent;if(t.contains("technique")||t.contains("technik")){label="TECHNIKA";accent=V070_BLUE;}else if(t.contains("refinement")||t.contains("tobulin")){label="TOBULINIMAS VIRŠ RIBOS";accent=Color.rgb(79,169,204);}else if(t.contains("principle")||t.contains("princip")){label="PRINCIPAS VIRŠ RIBOS";accent=V070_PURPLE;}else if(t.contains("unknown")||t.contains("nežinom")){label="NEŽINOMA KLASĖ";accent=Color.rgb(194,92,178);}else{label="GEBĖJIMAS VIRŠ RIBOS";accent=GOLD2;}LinearLayout r=col();r.setPadding(dp(10),dp(9),dp(10),dp(9));r.setBackground(round(Color.rgb(12,24,32),12,Color.argb(145,Color.red(accent),Color.green(accent),Color.blue(accent))));'
if old2 not in s: raise SystemExit('ability shadow target missing')
s=s.replace(old2,new2)
s=s.replace('h.addView(chip070(label,col));r.addView(h);','h.addView(chip070(label,accent));r.addView(h);')
p.write_text(s,encoding='utf-8')
assert 'int col=fail?' not in s
assert 'String t=raw.toLowerCase(Locale.ROOT),label;int col;' not in s
print('v0.8.2 compile shadow fix OK')
