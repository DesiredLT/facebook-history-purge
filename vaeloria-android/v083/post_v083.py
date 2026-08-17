from pathlib import Path
p=Path('app/src/main/java/lt/vaeloria/ooc/PremiumViewsV082.java')
s=p.read_text(encoding='utf-8')
# Fit optional faction overlay to the new 600x930 atlas. Keep it subtle and separate from geography.
s=s.replace('a.moveTo(10,35);a.lineTo(450,35);a.lineTo(520,390);a.lineTo(300,520);a.lineTo(10,460);','a.moveTo(22,70);a.lineTo(330,70);a.lineTo(360,470);a.lineTo(180,600);a.lineTo(20,500);')
s=s.replace('d.moveTo(520,15);d.lineTo(905,10);d.lineTo(900,430);d.lineTo(560,420);','d.moveTo(330,40);d.lineTo(585,40);d.lineTo(580,500);d.lineTo(360,470);')
s=s.replace('l.moveTo(250,380);l.lineTo(900,360);l.lineTo(900,725);l.lineTo(210,725);','l.moveTo(170,500);l.lineTo(580,480);l.lineTo(570,915);l.lineTo(130,915);')
s=s.replace('Color.argb(32,92,142,188)','Color.argb(22,92,142,188)').replace('Color.argb(32,203,92,65)','Color.argb(22,203,92,65)').replace('Color.argb(32,112,171,95)','Color.argb(22,112,171,95)')
# Ensure labels were not made invisible by an older build patch.
s=s.replace('p.setColor(Color.argb(0,3,9,13));','p.setColor(Color.argb(224,3,9,13));')
s=s.replace('p.setColor(Color.argb(0,235,230,214));c.drawText(n.name,lx,ly,p);','p.setColor(Color.rgb(235,230,214));c.drawText(n.name,lx,ly,p);')
p.write_text(s,encoding='utf-8')
assert 'a.moveTo(22,70)' in s and 'd.moveTo(330,40)' in s and 'l.moveTo(170,500)' in s
assert 'Color.argb(0,235,230,214)' not in s
print('v0.8.3 map overlay/label polish OK')
