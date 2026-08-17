from pathlib import Path
import shutil

ROOT=Path('.')
SRC=ROOT/'app/src/main/java/lt/vaeloria/ooc'
RES=ROOT/'app/src/main/res/drawable-nodpi'
ASSETS=ROOT/'v082/assets'
RES.mkdir(parents=True,exist_ok=True)

for name in ['hero_einoras_v082.webp','scene_palace_v082.webp','quest_meridian_v082.webp','world_map_v082.webp']:
    src=ASSETS/name
    if not src.exists() or src.stat().st_size<10000:
        raise SystemExit('Missing v0.8.2 asset: '+name)
    shutil.copy2(src,RES/name)
    print('asset',name,src.stat().st_size)

shutil.copy2(ROOT/'v082/PremiumViewsV082.java',SRC/'PremiumViewsV082.java')


def req(s,old,new,label):
    if old not in s:
        raise SystemExit('Missing patch target: '+label)
    print(label)
    return s.replace(old,new)

p=SRC/'PolishedActivity.java'
s=p.read_text(encoding='utf-8')

# Header: remove developer-facing product wording, keep a small build identifier.
s=req(s,'TextView version=txt("OOC · LOCAL FIRST · v0.8.1",7,Color.rgb(112,135,142),true);version.setLetterSpacing(.09f);','TextView version=txt("v0.8.2",6,Color.rgb(100,122,130),true);version.setLetterSpacing(.14f);','visible v0.8.2 header')
s=req(s,'TextView l=txt(state.location.toUpperCase(Locale.forLanguageTag("lt-LT")),8,PARCH,true);l.setGravity(Gravity.END);TextView y=txt("METAI "+state.worldYear,7,SUB,false);y.setGravity(Gravity.END);loc.addView(l);loc.addView(y);top.addView(loc,new LinearLayout.LayoutParams(dp(126),dp(62)));','TextView l=txt(state.location.toUpperCase(Locale.forLanguageTag("lt-LT")),7,PARCH,true);l.setGravity(Gravity.END);l.setMaxLines(2);TextView y=txt("METAI "+state.worldYear,7,SUB,false);y.setGravity(Gravity.END);loc.addView(l);loc.addView(y);top.addView(loc,new LinearLayout.LayoutParams(dp(156),dp(62)));','long location header')
s=req(s,'tab070("journal","◆","UŽDUOTYS")','tab070("journal","◆","ŽURNALAS")','journal navigation label')

# Game scene + concise feedback.
s=req(s,'SceneV070View art=new SceneV070View(this);art.setScene(state.location,state.sceneTitle);c.addView(art,new LinearLayout.LayoutParams(-1,dp(292)));','SceneV082View art=new SceneV082View(this);art.setScene(state.location,state.sceneTitle);c.addView(art,new LinearLayout.LayoutParams(-1,dp(300)));','premium scene view')
s=req(s,'if(!feedback.isEmpty()){TextView f=txt(feedback,10,Color.rgb(216,230,223),true);f.setPadding(dp(11),dp(8),dp(11),dp(8));f.setBackground(round(Color.rgb(13,38,36),12,Color.argb(120,92,184,174)));c.addView(f,sp(dp(8)));}','if(!feedback.isEmpty())c.addView(feedbackCard082(),sp(dp(8)));','collapsed gameplay math')
s=s.replace('chip070(ai?"AI GM":"LOCAL",ai?V070_GREEN:V070_BLUE)','chip070(ai?"GYVAS PASAULIS":"VIETINIS",ai?V070_GREEN:V070_BLUE)')

# Hero art + less obstructive equipment presentation.
s=req(s,'ImageView art=image(R.drawable.hero_einoras);','ImageView art=image(R.drawable.hero_einoras_v082);','premium Einoras artwork')
s=s.replace('top.addView(art,new FrameLayout.LayoutParams(-1,dp(350)))','top.addView(art,new FrameLayout.LayoutParams(-1,dp(400)))')
s=s.replace('top.addView(shade,new FrameLayout.LayoutParams(-1,dp(350)))','top.addView(shade,new FrameLayout.LayoutParams(-1,dp(400)))')
s=s.replace('c.addView(top,new LinearLayout.LayoutParams(-1,dp(350)))','c.addView(top,new LinearLayout.LayoutParams(-1,dp(400)))')
s=req(s,'LoadoutV070View load=new LoadoutV070View(this);','LoadoutV082View load=new LoadoutV082View(this);','compact loadout view')
s=req(s,'c.addView(load,new LinearLayout.LayoutParams(-1,dp(560)));','c.addView(load,new LinearLayout.LayoutParams(-1,dp(430)));','compact loadout height')
s=req(s,'for(String[] a:db.getAbilities())ab.addView(abilityCard(a));','for(String[] a:db.getAbilities())ab.addView(abilityCard082(a));','ability hierarchy cards')

# Inventory: keep density, add a short identity descriptor.
s=s.replace('new LinearLayout.LayoutParams(0,dp(158),1)','new LinearLayout.LayoutParams(0,dp(164),1)')
s=req(s,'x.addView(txt(rarityLabel(it.rarity).toUpperCase(Locale.ROOT),8,col,true));String state=','x.addView(txt(rarityLabel(it.rarity).toUpperCase(Locale.ROOT),8,col,true));x.addView(txt(itemDescriptor082(it),7,SUB,false));String state=','inventory descriptors')

# Atlas: new premium map renderer and optional faction layer.
s=req(s,'WorldMapV070View world=new WorldMapV070View(this);','WorldMapV082View world=new WorldMapV082View(this);','premium atlas view')
old='bot.addView(txt("● žema   ● vidutinė   ● aukšta   ● labai aukšta",8,Color.rgb(193,205,206),true),new LinearLayout.LayoutParams(0,-2,1));Button center=small("CENTRUOTI");center.setOnClickListener(v->{world.resetView();haptic();});bot.addView(center);'
new='bot.addView(txt("GRĖSMĖ  ● žema · ● vid. · ● aukšta",7,Color.rgb(193,205,206),true),new LinearLayout.LayoutParams(0,-2,1));Button layers=small("FRAKCIJOS");layers.setOnClickListener(v->{world.toggleFactions();haptic();});bot.addView(layers,new LinearLayout.LayoutParams(dp(82),dp(44)));Button center=small("CENTRUOTI");center.setOnClickListener(v->{world.resetView();haptic();});bot.addView(center,new LinearLayout.LayoutParams(dp(82),dp(44)));'
s=req(s,old,new,'atlas controls')

# Journal key art and player-readable world thread language.
old='FrameLayout h=new FrameLayout(this);h.addView(image(R.drawable.quest_meridian),new FrameLayout.LayoutParams(-1,dp(185)));View sh=new View(this);sh.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{Color.argb(18,0,0,0),Color.argb(245,2,8,13)}));h.addView(sh,new FrameLayout.LayoutParams(-1,dp(185)));LinearLayout q=col();q.setPadding(dp(15),0,dp(15),dp(13));q.addView(section070("PAGRINDINĖ SIUŽETO LINIJA · I DALIS"));q.addView(serif("LŪŽĘS MERIDIANAS",24,PARCH,true));q.addView(txt("Atviri horizontai · vartų poslinkis · Orisono signalai",9,SUB,false));h.addView(q,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));c.addView(h,new LinearLayout.LayoutParams(-1,dp(185)));'
new='ImageView key=image(R.drawable.quest_meridian_v082);key.setScaleType(ImageView.ScaleType.CENTER_CROP);c.addView(key,new LinearLayout.LayoutParams(-1,dp(190)));'
s=req(s,old,new,'Meridian story key art')
s=req(s,'FactionStatusView fs=new FactionStatusView(this);','FactionStatusV082View fs=new FactionStatusV082View(this);','faction identity cards')
s=req(s,'chip070("P"+priority,priority>=90?GOLD2:V070_BLUE)','chip070("PRIORITETAS "+priority,priority>=90?GOLD2:V070_BLUE)','world thread priority label')
s=req(s,'txt("Heat "+heat+"  ·  Mystery "+mystery,8,SUB,false)','txt("Įtampa "+heat+"  ·  Paslaptis "+mystery,8,SUB,false)','world thread player language')

# Append v0.8.2 helper methods before the activity's final brace.
helpers=r'''

    View feedbackCard082(){
        boolean fail=feedback.toLowerCase(Locale.ROOT).contains("nesėkm")||feedback.toLowerCase(Locale.ROOT).contains("nepavyk");
        int col=fail?Color.rgb(210,102,78):V070_GREEN;LinearLayout box=col();box.setPadding(dp(12),dp(9),dp(12),dp(9));box.setBackground(round(Color.rgb(11,30,29),13,Color.argb(145,Color.red(col),Color.green(col),Color.blue(col))));
        LinearLayout h=row();h.setGravity(Gravity.CENTER_VERTICAL);h.addView(txt(fail?"NESĖKMĖ":"SĖKMĖ",9,col,true),new LinearLayout.LayoutParams(0,-2,1));h.addView(chip070("REZULTATAS",col));box.addView(h);
        box.addView(txt((state.sceneTitle==null||state.sceneTitle.isEmpty()?"Veiksmas":state.sceneTitle)+(fail?" nepavyko.":" pavyko."),11,Color.rgb(229,234,226),true));
        box.addView(txt(feedbackChanges082(),8,SUB,false),sp(dp(3)));TextView d=txt("KAIP APSKAIČIUOTA?  ›",7,GOLD2,true);d.setPadding(0,dp(5),0,0);box.addView(d);box.setOnClickListener(v->feedbackDetails082());return box;
    }
    String feedbackChanges082(){
        ArrayList<String> out=new ArrayList<>();String f=feedback==null?"":feedback;
        java.util.regex.Matcher m=java.util.regex.Pattern.compile("(?i)(Meistriškumas\\s*[+-]\\d+|[+-]\\d+\\s*(?:ištvermė|mana|gyvybė|eonas))").matcher(f);
        while(m.find()&&out.size()<3)out.add(m.group(1));if(out.isEmpty())return "Pasaulio būsena atnaujinta.";return android.text.TextUtils.join(" · ",out);
    }
    void feedbackDetails082(){new AlertDialog.Builder(this).setTitle("Kaip apskaičiuota?").setMessage(feedback).setPositiveButton("UŽDARYTI",null).show();}
    String itemDescriptor082(VaeloriaDb.Item it){String n=(it.name+" "+it.slot).toLowerCase(Locale.ROOT);if(n.contains("weapon")||n.contains("ašmen")||n.contains("kard"))return "Ginklas · artimas nuotolis";if(n.contains("chest")||n.contains("head")||n.contains("armor")||n.contains("mant"))return "Šarvai · apsauga";if(n.contains("utility")||n.contains("krep"))return "Naudingasis daiktas";if(n.contains("ring")||n.contains("žied")||n.contains("signet"))return "Relikvija · rezonansas";if(n.contains("rakt")||n.contains("key"))return "Siužeto raktas";if(n.contains("prizm"))return "Arkaninis fokusas";if(n.contains("astrolab")||n.contains("kompas"))return "Navigacija · rezonansas";if(n.contains("sėkl")||n.contains("seed"))return "Senovinis artefaktas";return "Artefaktas · rezonansas";}
    View abilityCard082(String[] a){String raw=a.length>1?a[1]:"";String t=raw.toLowerCase(Locale.ROOT),label;int col;if(t.contains("technique")||t.contains("technik")){label="TECHNIKA";col=V070_BLUE;}else if(t.contains("refinement")||t.contains("tobulin")){label="TOBULINIMAS VIRŠ RIBOS";col=Color.rgb(79,169,204);}else if(t.contains("principle")||t.contains("princip")){label="PRINCIPAS VIRŠ RIBOS";col=V070_PURPLE;}else if(t.contains("unknown")||t.contains("nežinom")){label="NEŽINOMA KLASĖ";col=Color.rgb(194,92,178);}else{label="GEBĖJIMAS VIRŠ RIBOS";col=GOLD2;}LinearLayout r=col();r.setPadding(dp(10),dp(9),dp(10),dp(9));r.setBackground(round(Color.rgb(12,24,32),12,Color.argb(145,Color.red(col),Color.green(col),Color.blue(col))));LinearLayout h=row();h.setGravity(Gravity.CENTER_VERTICAL);h.addView(serif(a[0],13,PARCH,true),new LinearLayout.LayoutParams(0,-2,1));h.addView(chip070(label,col));r.addView(h);r.addView(txt(a.length>2?a[2]:"",9,SUB,false));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));r.setLayoutParams(lp);return r;}
'''
idx=s.rfind('\n}')
if idx<0: raise SystemExit('PolishedActivity final brace not found')
s=s[:idx]+helpers+s[idx:]
p.write_text(s,encoding='utf-8')

# Add a recognisable material/shard glyph to the existing v0.8.1 item renderer.
p=SRC/'PremiumViews.java';v=p.read_text(encoding='utf-8')
needle='else {Path q=new Path();q.moveTo(x,y-r);q.lineTo(x+r,y);q.lineTo(x,y+r);q.lineTo(x-r,y);q.close();c.drawPath(q,p);c.drawCircle(x,y,r*.27f,p);}'
replacement='else if(n.contains("medžiag")||n.contains("material")||n.contains("fragment")||n.contains("shard")){Path q=new Path();q.moveTo(x-r*.7f,y-r*.65f);q.lineTo(x+r*.25f,y-r);q.lineTo(x+r*.85f,y-r*.2f);q.lineTo(x+r*.55f,y+r*.8f);q.lineTo(x-r*.55f,y+r*.7f);q.lineTo(x-r*.9f,y-r*.05f);q.close();c.drawPath(q,p);c.drawLine(x-r*.35f,y-r*.25f,x+r*.45f,y+r*.35f,p);}else {Path q=new Path();q.moveTo(x,y-r);q.lineTo(x+r,y);q.lineTo(x,y+r);q.lineTo(x-r,y);q.close();c.drawPath(q,p);c.drawCircle(x,y,r*.27f,p);}'
if needle not in v: raise SystemExit('Item material glyph patch target missing')
v=v.replace(needle,replacement)
p.write_text(v,encoding='utf-8')

# Verification markers.
p7=(SRC/'PolishedActivity.java').read_text(encoding='utf-8');v82=(SRC/'PremiumViewsV082.java').read_text(encoding='utf-8')
checks={
 'clean header':'OOC · LOCAL FIRST' not in p7 and 'v0.8.2' in p7,
 'journal nav':'"ŽURNALAS"' in p7,
 'premium hero':'hero_einoras_v082' in p7 and 'LoadoutV082View' in p7,
 'premium scene':'SceneV082View' in p7,
 'collapsed math':'feedbackCard082' in p7 and 'KAIP APSKAIČIUOTA?' in p7,
 'premium atlas':'WorldMapV082View' in p7 and 'FRAKCIJOS' in p7,
 'quest key art':'quest_meridian_v082' in p7,
 'thread labels':'PRIORITETAS ' in p7 and 'Įtampa ' in p7,
 'ability classes':'PRINCIPAS VIRŠ RIBOS' in p7 and 'TECHNIKA' in p7,
 'factions':'FactionStatusV082View' in p7,
 'view source':'class WorldMapV082View' in v82 and 'class LoadoutV082View' in v82,
}
for k,val in checks.items():print(k,'OK' if val else 'FAIL')
bad=[k for k,val in checks.items() if not val]
if bad:raise SystemExit('v0.8.2 verification failed: '+', '.join(bad))
print('v0.8.2 patch complete')
