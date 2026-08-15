from pathlib import Path

ROOT = Path('.')


def replace_required(text, old, new, label):
    count = text.count(old)
    if count == 0:
        raise SystemExit(f'MISSING required patch target: {label}')
    text = text.replace(old, new)
    print(f'{label}: {count} replacement(s)')
    return text


def replace_optional(text, old, new, label):
    count = text.count(old)
    if count:
        text = text.replace(old, new)
        print(f'{label}: {count} replacement(s)')
    else:
        print(f'{label}: already patched / not present')
    return text


# -----------------------------------------------------------------------------
# v0.7 premium drawing layer
# -----------------------------------------------------------------------------
p = ROOT / 'v070' / 'PremiumViewsV070.java'
s = p.read_text(encoding='utf-8')

s = replace_required(s, 'R.drawable.world_map_v070', 'R.drawable.world_map_v060', 'world map asset')

old_nodes = 'add("Luminara",500,330,3);add("Asterio Karūna",350,195,4);add("Stiklo Giria",735,155,5);add("Veyrhold",665,255,3);add("Aureliono Pakraštys",760,405,4);add("Žvaigždėkritos Skliautas",540,495,7);add("Tuščiavidurė Smailė",320,555,8);add("Pelenų Karūnos Citadelė",965,210,7);add("Kharad Vorn",1140,280,5);add("Drakono Pabudimo Viršūnės",1330,155,8);add("Safyro Platybės",1010,535,7);add("Amžinojo Šaltinio Slėnis",915,650,4);add("Žaliasis Labirintas",1135,735,8);add("Šventųjų Pelkynas",790,755,6);'
new_nodes = 'add("Luminara",388,405,3);add("Asterio Karūna",270,183,4);add("Stiklo Giria",505,180,5);add("Veyrhold",620,326,3);add("Aureliono Pakraštys",705,513,4);add("Žvaigždėkritos Skliautas",525,512,7);add("Tuščiavidurė Smailė",110,613,8);add("Pelenų Karūnos Citadelė",925,275,7);add("Kharad Vorn",1180,365,5);add("Drakono Pabudimo Viršūnės",1175,90,8);add("Safyro Platybės",967,600,7);add("Amžinojo Šaltinio Slėnis",770,741,4);add("Šventųjų Pelkynas",617,920,6);add("Žaliasis Labirintas",1190,790,8);'
s = replace_required(s, old_nodes, new_nodes, 'map node coordinates')
s = replace_required(s,
    'base=Math.min(getWidth()/(float)map.getWidth(),getHeight()/(float)map.getHeight());',
    'base=Math.max(getWidth()/(float)map.getWidth(),getHeight()/(float)map.getHeight());',
    'map initial crop')
s = replace_required(s, 'Math.min(4.7f,zoom*d.getScaleFactor())', 'Math.min(4.5f,zoom*d.getScaleFactor())', 'map max zoom')
s = replace_required(s, 'bd<76*76', 'bd<70*70', 'map hit radius')
s = replace_required(s, 'm=dp(60)', 'm=dp(55)', 'map clamp margin')

scene_old = 'luminara=BitmapFactory.decodeResource(getResources(),R.drawable.scene_luminara_v070);wild=BitmapFactory.decodeResource(getResources(),R.drawable.scene_wild_v070);map=BitmapFactory.decodeResource(getResources(),R.drawable.world_map_v060);'
scene_new = 'luminara=BitmapFactory.decodeResource(getResources(),R.drawable.scene_luminara);wild=BitmapFactory.decodeResource(getResources(),R.drawable.combat_forest);map=BitmapFactory.decodeResource(getResources(),R.drawable.world_map_v060);'
s = replace_required(s, scene_old, scene_new, 'scene art assets')
s = replace_required(s, 'R.drawable.hero_einoras_v070', 'R.drawable.hero_einoras', 'hero art asset')

npc_class = r'''

class NpcPortraitV070 extends View {
    final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); String name="",role="";
    NpcPortraitV070(Context c){super(c);setLayerType(LAYER_TYPE_SOFTWARE,null);}
    void setNpc(String n,String r){name=n==null?"":n;role=r==null?"":r;invalidate();}
    protected void onDraw(Canvas c){
        float w=getWidth(),h=getHeight(),cx=w*.5f;int seed=Math.abs(name.hashCode());
        int ar=95+(seed%70),ag=92+((seed/7)%65),ab=78+((seed/13)%70);int accent=Color.rgb(ar,ag,ab);
        p.setShader(new LinearGradient(0,0,w,h,Color.rgb(11,22,29),Color.rgb(4,9,14),Shader.TileMode.CLAMP));c.drawRoundRect(0,0,w,h,Math.min(w,h)*.16f,Math.min(w,h)*.16f,p);p.setShader(null);
        p.setColor(Color.argb(70,Color.red(accent),Color.green(accent),Color.blue(accent)));c.drawCircle(cx,h*.37f,Math.min(w,h)*.42f,p);
        p.setColor(Color.rgb(20+(seed%18),27+((seed/5)%18),31+((seed/9)%20)));Path body=new Path();body.moveTo(w*.12f,h);body.quadTo(w*.20f,h*.68f,cx,h*.65f);body.quadTo(w*.80f,h*.68f,w*.88f,h);body.close();c.drawPath(body,p);
        p.setColor(Color.rgb(174+seed%34,142+(seed/3)%30,111+(seed/5)%25));c.drawRoundRect(w*.42f,h*.48f,w*.58f,h*.70f,w*.06f,w*.06f,p);c.drawOval(new RectF(w*.30f,h*.15f,w*.70f,h*.58f),p);
        p.setColor(Color.rgb(18+(seed%24),19+((seed/4)%23),19+((seed/8)%24)));Path hair=new Path();hair.moveTo(w*.27f,h*.39f);hair.quadTo(w*.28f,h*.08f,cx,h*.09f);hair.quadTo(w*.76f,h*.11f,w*.72f,h*.46f);hair.lineTo(w*.63f,h*.35f);hair.quadTo(w*.58f,h*.20f,w*.47f,h*.19f);hair.quadTo(w*.35f,h*.22f,w*.34f,h*.42f);hair.close();c.drawPath(hair,p);
        p.setColor(Color.rgb(216,224,211));c.drawCircle(w*.40f,h*.34f,Math.max(1,w*.018f),p);c.drawCircle(w*.60f,h*.34f,Math.max(1,w*.018f),p);
        p.setColor(Color.argb(120,80,53,40));p.setStrokeWidth(Math.max(1,w*.012f));c.drawLine(cx,h*.34f,cx-w*.018f,h*.43f,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1,w*.025f));p.setColor(accent);c.drawRoundRect(w*.05f,h*.05f,w*.95f,h*.95f,w*.14f,w*.14f,p);p.setStyle(Paint.Style.FILL);
    }
}
'''
if 'class NpcPortraitV070' not in s:
    s += npc_class
    print('NPC portrait canvas: appended')
else:
    print('NPC portrait canvas: already present')

required_v70 = [
    'R.drawable.world_map_v060',
    'R.drawable.scene_luminara);wild=BitmapFactory.decodeResource(getResources(),R.drawable.combat_forest)',
    'R.drawable.hero_einoras',
    'class NpcPortraitV070',
    'add("Luminara",388,405,3)',
    'base=Math.max(getWidth()/(float)map.getWidth(),getHeight()/(float)map.getHeight())',
]
for marker in required_v70:
    if marker not in s:
        raise SystemExit('PremiumViewsV070 verification failed: '+marker)
if 'R.drawable.world_map_v070' in s or 'R.drawable.scene_luminara_v070' in s or 'R.drawable.scene_wild_v070' in s or 'R.drawable.hero_einoras_v070' in s:
    raise SystemExit('PremiumViewsV070 still references regressed v0.7 art')
p.write_text(s, encoding='utf-8')
print('patched', p)


# -----------------------------------------------------------------------------
# v0.7 mobile activity
# -----------------------------------------------------------------------------
p = ROOT / 'v070' / 'PolishedActivity.java'
s = p.read_text(encoding='utf-8')
s = replace_required(s,
    'c.addView(art,new LinearLayout.LayoutParams(-1,dp(252)));',
    'c.addView(art,new LinearLayout.LayoutParams(-1,dp(292)));',
    'scene hero height')
s = replace_required(s,
    'c.addView(b,new LinearLayout.LayoutParams(-1,dp(300)));',
    'c.addView(b,new LinearLayout.LayoutParams(-1,dp(340)));',
    'combat canvas height')
s = replace_required(s,
    'ImageView art=image(R.drawable.hero_einoras_v070);',
    'ImageView art=image(R.drawable.hero_einoras);',
    'character header hero')
s = replace_required(s,
    'ItemGlyphV070 icon=new ItemGlyphV070(this);icon.setItem(it.name,it.rarity);x.addView(icon,new LinearLayout.LayoutParams(-1,dp(92)));',
    'ItemIconView icon=new ItemIconView(this);icon.setItem(it.name,it.slot,it.rarity);x.addView(icon,new LinearLayout.LayoutParams(-1,dp(92)));',
    'inventory icon renderer')
s = replace_required(s,
    'if(pref("animations",true)){v.setAlpha(0);v.setTranslationY(dp(7));v.animate().alpha(1).translationY(0).setDuration(170).start();}',
    'if(pref("animations",true)){v.setAlpha(0);v.setTranslationY(dp(7));v.setScaleX(.99f);v.setScaleY(.99f);v.animate().alpha(1).translationY(0).scaleX(1f).scaleY(1f).setDuration(190).start();}',
    'screen transition polish')
s = replace_required(s,
    'r.setOnClickListener(v->act(a));return r;',
    'r.setOnTouchListener((v,e)->{if(pref("animations",true)){if(e.getAction()==MotionEvent.ACTION_DOWN)v.animate().scaleX(.985f).scaleY(.985f).setDuration(55).start();else if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL)v.animate().scaleX(1f).scaleY(1f).setDuration(85).start();}return false;});r.setOnClickListener(v->{haptic();sound(false);act(a);});return r;',
    'choice tactile polish')

for marker in ['dp(292)', 'dp(340)', 'ItemIconView icon=new ItemIconView', 'scaleX(.985f)', 'R.drawable.hero_einoras']:
    if marker not in s:
        raise SystemExit('PolishedActivity verification failed: '+marker)
p.write_text(s, encoding='utf-8')
print('patched', p)


# -----------------------------------------------------------------------------
# v0.6 drawing primitives used by v0.8
# -----------------------------------------------------------------------------
p = ROOT / 'v060' / 'PremiumViews.java'
s = p.read_text(encoding='utf-8')
s = replace_required(s,
    'RectF box=new RectF(dp(12),h-dp(62),w-dp(12),h-dp(10));',
    'RectF box=new RectF(dp(12),h-dp(90),w-dp(12),h-dp(10));',
    'combat info panel height')
old = 'c.drawText(trim(tel,54),box.left+dp(10),box.top+dp(39),p);}'
new = 'c.drawText(trim(tel,54),box.left+dp(10),box.top+dp(39),p);p.setTextSize(dp(8));p.setColor(Color.rgb(182,198,194));String st=state.enemyStatus==null||state.enemyStatus.isEmpty()?"Būsena nežinoma":state.enemyStatus;c.drawText("BŪSENA · "+trim(st,42),box.left+dp(10),box.top+dp(58),p);String hz=state.combatHazard==null||state.combatHazard.isEmpty()?"Aplinka stabili":state.combatHazard;p.setColor(Color.rgb(221,187,104));c.drawText("APLINKA · "+trim(hz,42),box.left+dp(10),box.top+dp(75),p);}'
s = replace_required(s, old, new, 'combat status and hazard')
for marker in ['BŪSENA ·', 'APLINKA ·', 'class ItemIconView']:
    if marker not in s:
        raise SystemExit('PremiumViews verification failed: '+marker)
p.write_text(s, encoding='utf-8')
print('patched', p)


# -----------------------------------------------------------------------------
# v0.6 inherited NPC hub/dialogue
# -----------------------------------------------------------------------------
p = ROOT / 'v060' / 'PremiumActivity.java'
s = p.read_text(encoding='utf-8')
old_card = 'if("Lyra Fen".equals(n[0]))r.addView(image(R.drawable.npc_lyra),new LinearLayout.LayoutParams(dp(54),dp(64)));else{TextView i=serif(n[0].substring(0,1),20,GOLD2,true);i.setGravity(Gravity.CENTER);i.setBackground(round(Color.rgb(24,34,38),40,Color.argb(170,221,187,104)));r.addView(i,new LinearLayout.LayoutParams(dp(54),dp(54)));}'
new_card = 'NpcPortraitV070 i=new NpcPortraitV070(this);i.setNpc(n[0],n[1]);r.addView(i,new LinearLayout.LayoutParams(dp(58),dp(68)));'
s = replace_required(s, old_card, new_card, 'NPC list portraits')
old_dialog = 'if("Lyra Fen".equals(n[0]))c.addView(image(R.drawable.npc_lyra),new LinearLayout.LayoutParams(-1,dp(220)));'
new_dialog = 'NpcPortraitV070 portrait=new NpcPortraitV070(this);portrait.setNpc(n[0],n[1]);c.addView(portrait,new LinearLayout.LayoutParams(-1,dp(220)));'
s = replace_required(s, old_dialog, new_dialog, 'NPC dialogue portrait')
if s.count('NpcPortraitV070') < 2:
    raise SystemExit('PremiumActivity NPC portrait verification failed')
p.write_text(s, encoding='utf-8')
print('patched', p)

print('v0.8 presentation patch complete')
