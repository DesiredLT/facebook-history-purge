from pathlib import Path
import re

SRC=Path('app/src/main/java/lt/vaeloria/ooc')

def must(s,old,new,label):
    if old not in s:
        raise SystemExit('missing '+label)
    return s.replace(old,new)

# PolishedActivity: visible version, denser inventory, cleaner text hierarchy.
p=SRC/'PolishedActivity.java'; s=p.read_text(encoding='utf-8')
s=must(s,'OOC · LOCAL FIRST · v0.7.0','OOC · LOCAL FIRST · v0.8.1','visible version')
s=s.replace('new LinearLayout.LayoutParams(0,dp(186),1)','new LinearLayout.LayoutParams(0,dp(158),1)')
s=s.replace('x.addView(icon,new LinearLayout.LayoutParams(-1,dp(92)));','x.addView(icon,new LinearLayout.LayoutParams(-1,dp(68)));')
s=s.replace('TextView n=serif(it.name,13,PARCH,true);n.setMaxLines(1);','TextView n=serif(it.name,12,PARCH,true);n.setMaxLines(2);')
p.write_text(s,encoding='utf-8')

# PremiumViewsV070: show the full atlas by default and keep long location titles inside the screen.
p=SRC/'PremiumViewsV070.java'; s=p.read_text(encoding='utf-8')
s=s.replace('base=Math.max(getWidth()/(float)map.getWidth(),getHeight()/(float)map.getHeight());','base=Math.min(getWidth()/(float)map.getWidth(),getHeight()/(float)map.getHeight());')
s=s.replace('Bitmap b=q.contains("luminara")?luminara:(q.contains("giria")||q.contains("pelkyn")||q.contains("labir")||q.contains("peak"))?wild:map;', 'Bitmap b=q.contains("luminara")?luminara:wild;')
old='p.setTypeface(Typeface.create(Typeface.SERIF,Typeface.BOLD));p.setTextSize(dp(25));p.setColor(Color.rgb(245,229,194));p.setShadowLayer(dp(5),0,2,Color.BLACK);c.drawText(location.toUpperCase(Locale.forLanguageTag("lt-LT")),dp(16),h-dp(45),p);p.clearShadowLayer();'
new='p.setTypeface(Typeface.create(Typeface.SERIF,Typeface.BOLD));float locSize=25f;String locTitle=location.toUpperCase(Locale.forLanguageTag("lt-LT"));p.setTextSize(dp(locSize));while(locSize>15f&&p.measureText(locTitle)>w-dp(32)){locSize-=1f;p.setTextSize(dp(locSize));}p.setColor(Color.rgb(245,229,194));p.setShadowLayer(dp(5),0,2,Color.BLACK);c.drawText(locTitle,dp(16),h-dp(45),p);p.clearShadowLayer();'
s=must(s,old,new,'adaptive scene title')
p.write_text(s,encoding='utf-8')

# Replace the placeholder crossed-circle item icon renderer with real category glyphs.
p=SRC/'PremiumViews.java'; s=p.read_text(encoding='utf-8')
start=s.find('class ItemIconView extends View {')
end=s.find('class FactionStatusView extends View {',start)
if start<0 or end<0: raise SystemExit('ItemIconView boundaries not found')
icon=r'''class ItemIconView extends View {
    final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);String name="",cat="",rarity="common";int accent=Color.rgb(120,140,148);
    ItemIconView(Context c){super(c);setLayerType(LAYER_TYPE_SOFTWARE,null);}void setItem(String n,String c,String r){name=n==null?"":n;cat=c==null?"":c;rarity=r==null?"common":r;accent=rarityColor(rarity);invalidate();}
    protected void onDraw(Canvas c){int w=getWidth(),h=getHeight();float cx=w/2f,cy=h*.48f,rr=Math.min(w,h)*.25f;RectF box=new RectF(dp(2),dp(2),w-dp(2),h-dp(2));p.setShader(new RadialGradient(cx,cy,Math.max(w,h)*.55f,Color.argb(68,Color.red(accent),Color.green(accent),Color.blue(accent)),Color.rgb(8,18,25),Shader.TileMode.CLAMP));c.drawRoundRect(box,dp(12),dp(12),p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeWidth(dp(2.4f));p.setColor(accent);p.setShadowLayer(dp(5),0,0,Color.argb(95,Color.red(accent),Color.green(accent),Color.blue(accent)));glyph(c,cx,cy,rr);p.clearShadowLayer();p.setStyle(Paint.Style.FILL);}
    void glyph(Canvas c,float x,float y,float r){String n=(name+" "+cat).toLowerCase(Locale.ROOT);
        if(n.contains("rakt")||n.contains("key")){c.drawCircle(x-r*.35f,y-r*.22f,r*.38f,p);c.drawLine(x-r*.04f,y,x+r*.85f,y+r*.76f,p);c.drawLine(x+r*.48f,y+r*.43f,x+r*.72f,y+r*.16f,p);c.drawLine(x+r*.67f,y+r*.62f,x+r*.88f,y+r*.38f,p);}
        else if(n.contains("ašmen")||n.contains("kard")||n.contains("weapon")||n.contains("blade")||n.contains("edge")){c.drawLine(x-r*.72f,y+r*.78f,x+r*.62f,y-r*.72f,p);c.drawLine(x-r*.86f,y+r*.25f,x-r*.22f,y+r*.86f,p);c.drawLine(x-r*.48f,y+r*.48f,x-r*.13f,y+r*.16f,p);}
        else if(n.contains("mant")||n.contains("šarv")||n.contains("armor")||n.contains("chest")){Path q=new Path();q.moveTo(x,y-r);q.lineTo(x+r*.9f,y-r*.45f);q.lineTo(x+r*.68f,y+r*.72f);q.lineTo(x,y+r);q.lineTo(x-r*.68f,y+r*.72f);q.lineTo(x-r*.9f,y-r*.45f);q.close();c.drawPath(q,p);c.drawLine(x,y-r*.8f,x,y+r*.78f,p);}
        else if(n.contains("prizm")||n.contains("crystal")||n.contains("žvaigžd")||n.contains("seed")||n.contains("sėkl")){Path q=new Path();q.moveTo(x,y-r);q.lineTo(x+r*.78f,y-r*.15f);q.lineTo(x+r*.5f,y+r*.85f);q.lineTo(x-r*.5f,y+r*.85f);q.lineTo(x-r*.78f,y-r*.15f);q.close();c.drawPath(q,p);c.drawLine(x,y-r,x,y+r*.85f,p);}
        else if(n.contains("signet")||n.contains("žied")||n.contains("ring")){c.drawCircle(x,y,r*.78f,p);c.drawCircle(x,y,r*.38f,p);Path q=new Path();q.moveTo(x,y-r*.96f);q.lineTo(x+r*.28f,y-r*.55f);q.lineTo(x,y-r*.28f);q.lineTo(x-r*.28f,y-r*.55f);q.close();c.drawPath(q,p);}
        else if(n.contains("astrolab")||n.contains("kompas")||n.contains("compass")){c.drawCircle(x,y,r,p);c.drawCircle(x,y,r*.38f,p);c.drawLine(x,y-r*.85f,x+r*.32f,y+r*.6f,p);c.drawLine(x-r*.75f,y,x+r*.75f,y,p);}
        else if(n.contains("šird")||n.contains("heart")){Path q=new Path();q.moveTo(x,y+r*.82f);q.cubicTo(x-r*1.2f,y,x-r*.55f,y-r,x,y-r*.35f);q.cubicTo(x+r*.55f,y-r,x+r*1.2f,y,x,y+r*.82f);c.drawPath(q,p);}
        else if(n.contains("krep")||n.contains("bag")||n.contains("utility")){c.drawRoundRect(new RectF(x-r,y-r*.35f,x+r,y+r*.78f),r*.22f,r*.22f,p);c.drawArc(new RectF(x-r*.55f,y-r,x+r*.55f,y+r*.1f),180,180,false,p);}
        else if(n.contains("runa")||n.contains("rune")||n.contains("seal")){c.drawCircle(x,y,r*.92f,p);c.drawLine(x-r*.58f,y-r*.52f,x+r*.55f,y+r*.55f,p);c.drawLine(x+r*.52f,y-r*.58f,x-r*.55f,y+r*.55f,p);c.drawCircle(x,y,r*.22f,p);}
        else {Path q=new Path();q.moveTo(x,y-r);q.lineTo(x+r,y);q.lineTo(x,y+r);q.lineTo(x-r,y);q.close();c.drawPath(q,p);c.drawCircle(x,y,r*.27f,p);}
    }
    int rarityColor(String r){if("unique".equals(r))return Color.rgb(222,113,205);if("legendary".equals(r))return Color.rgb(223,185,90);if("ancient".equals(r))return Color.rgb(149,113,216);if("rare".equals(r))return Color.rgb(79,158,221);if("uncommon".equals(r))return Color.rgb(92,187,126);return Color.rgb(126,144,151);}int dp(float v){return(int)(v*getResources().getDisplayMetrics().density+.5f);}
}

'''
s=s[:start]+icon+s[end:]
p.write_text(s,encoding='utf-8')

checks={
 'version':'v0.8.1' in (SRC/'PolishedActivity.java').read_text(encoding='utf-8'),
 'dense inventory':'dp(158)' in (SRC/'PolishedActivity.java').read_text(encoding='utf-8'),
 'adaptive title':'locSize=25f' in (SRC/'PremiumViewsV070.java').read_text(encoding='utf-8'),
 'item key glyph':'n.contains("rakt")' in (SRC/'PremiumViews.java').read_text(encoding='utf-8'),
 'item sword glyph':'n.contains("ašmen")' in (SRC/'PremiumViews.java').read_text(encoding='utf-8'),
}
for k,v in checks.items(): print(k,'OK' if v else 'FAIL')
if not all(checks.values()): raise SystemExit('v0.8.1 UI patch verification failed')
print('v0.8.1 UI patch complete')
