from pathlib import Path
import random, math
from PIL import Image, ImageDraw, ImageFilter, ImageEnhance

ROOT=Path('.')
RES=ROOT/'app/src/main/res/drawable-nodpi'
RES.mkdir(parents=True,exist_ok=True)
rnd=random.Random(8303)

def save_webp(im,name,size):
    im=im.convert('RGB').resize(size,Image.Resampling.LANCZOS)
    q=RES/name
    im.save(q,'WEBP',quality=88,method=6)
    with Image.open(q) as ck:
        ck.load(); assert ck.format=='WEBP' and ck.size==size,(name,ck.format,ck.size)
        colors=len(set(ck.resize((32,32)).convert('RGB').getdata()))
        assert colors>120,(name,colors)
    print('ASSET OK',name,size,q.stat().st_size)

# EINORAS key art — original deterministic painterly silhouette, not reused v0.8.2 art.
W,H=540,720
im=Image.new('RGB',(W,H),(8,13,20)); d=ImageDraw.Draw(im,'RGBA')
for y in range(H):
    t=y/H; d.line((0,y,W,y),fill=(8+int(7*t),13+int(10*t),20+int(14*t),255))
for _ in range(900):
    x=rnd.randrange(W); y=rnd.randrange(H); a=rnd.randrange(5,24)
    d.ellipse((x,y,x+rnd.randrange(1,4),y+rnd.randrange(1,4)),fill=(70,100,112,a))
# Meridian halo and distant shards.
for r,a in [(95,35),(130,22),(165,14)]: d.ellipse((W//2-r,105-r,W//2+r,105+r),outline=(70,195,205,a),width=3)
for _ in range(24):
    x=rnd.randint(20,W-20); y=rnd.randint(60,H-40); l=rnd.randint(8,28)
    d.line((x,y,x+rnd.randint(-4,4),y+l),fill=(55,154,168,rnd.randint(22,70)),width=1)
# Cloak/body.
d.polygon([(270,120),(205,180),(175,315),(150,570),(390,570),(365,315),(335,180)],fill=(24,31,38,255),outline=(106,120,126,180))
d.polygon([(214,185),(270,145),(326,185),(305,270),(235,270)],fill=(44,52,59,255),outline=(156,145,117,190))
# Head/hood.
d.ellipse((226,94,314,178),fill=(22,25,29,255),outline=(130,135,135,150),width=2)
d.polygon([(226,120),(270,78),(314,120),(296,164),(244,164)],fill=(27,32,38,255),outline=(120,130,134,140))
# Layered steel plates.
for yy,w in [(255,95),(292,108),(332,118),(374,126)]:
    d.rounded_rectangle((270-w//2,yy,270+w//2,yy+38),8,fill=(54,65,72,245),outline=(168,159,130,190),width=2)
    d.line((270-w//2+12,yy+9,270+w//2-12,yy+9),fill=(190,182,150,80),width=2)
# Shoulder guards.
d.polygon([(205,216),(150,248),(168,292),(226,272)],fill=(51,61,67,250),outline=(171,158,122,190))
d.polygon([(335,216),(390,248),(372,292),(314,272)],fill=(51,61,67,250),outline=(171,158,122,190))
# Belt and leather.
d.rectangle((197,408,343,432),fill=(62,43,29,255),outline=(152,112,67,180)); d.rectangle((257,407,283,433),fill=(139,113,65,255))
# Asterion Edge sword.
d.line((342,188,205,620),fill=(14,20,25,255),width=16)
d.line((340,190,203,618),fill=(183,193,193,255),width=7)
d.line((337,192,201,616),fill=(67,198,210,180),width=2)
d.line((324,228,370,244),fill=(171,131,71,255),width=8)
d.ellipse((190,604,220,634),fill=(92,64,39,255),outline=(178,143,82,180))
# Boots and grounded shadow.
d.ellipse((120,585,420,675),fill=(0,0,0,95)); d.rectangle((200,520,248,635),fill=(29,34,38,255)); d.rectangle((292,520,340,635),fill=(29,34,38,255))
# Cyan chest resonance.
for r,a in [(7,255),(15,110),(28,45)]: d.ellipse((270-r,315-r,270+r,315+r),fill=(69,201,211,a))
im=im.filter(ImageFilter.GaussianBlur(.35)); im=ImageEnhance.Contrast(im).enhance(1.08)
save_webp(im,'hero_einoras_v083.webp',(540,720))

# LUMINARA PALACE scene — cinematic architecture, separate artwork.
W,H=720,300
im=Image.new('RGB',(W,H),(7,15,23)); d=ImageDraw.Draw(im,'RGBA')
for y in range(H):
    t=y/H; d.line((0,y,W,y),fill=(7+int(8*t),15+int(10*t),23+int(10*t),255))
# moon/glow
for r,a in [(110,10),(78,18),(52,30)]: d.ellipse((575-r,64-r,575+r,64+r),fill=(90,184,202,a))
# palace silhouette + towers
base=[(70,250),(70,165),(135,150),(155,88),(175,150),(250,135),(285,60),(315,135),(405,135),(445,82),(470,135),(565,145),(590,112),(610,145),(650,165),(650,250)]
d.polygon(base,fill=(22,31,39,255),outline=(132,118,87,140))
for x,y,w,h in [(135,145,42,105),(270,125,55,125),(430,130,52,120),(565,145,44,105)]:
    d.rectangle((x,y,x+w,y+h),fill=(28,38,46,255),outline=(112,105,82,120))
    for wy in range(y+20,y+h-10,28): d.rectangle((x+10,wy,x+w-10,wy+9),fill=(79,184,195,125))
# bridge/steps and water reflection
d.polygon([(275,250),(445,250),(410,300),(310,300)],fill=(37,43,45,255),outline=(149,130,89,120))
for yy in range(258,300,9): d.line((300-(yy-258)//3,yy,420+(yy-258)//3,yy),fill=(174,147,94,90),width=1)
for _ in range(55):
    x=rnd.randint(230,500); y=rnd.randint(250,298); l=rnd.randint(4,22)
    d.line((x,y,x+l,y),fill=(65,178,190,rnd.randint(16,65)),width=1)
# foreground columns frame
for x in [18,676]: d.rectangle((x,30,x+26,300),fill=(14,20,26,255)); d.rectangle((x-5,30,x+31,46),fill=(49,45,39,255))
save_webp(im,'scene_palace_v083.webp',(720,300))

# BROKEN MERIDIAN quest key art.
W,H=720,360
im=Image.new('RGB',(W,H),(8,14,20)); d=ImageDraw.Draw(im,'RGBA')
for y in range(H): d.line((0,y,W,y),fill=(8+int(10*y/H),14+int(8*y/H),20+int(6*y/H),255))
cx,cy=360,178
for r,a in [(130,34),(104,52),(78,78)]: d.ellipse((cx-r,cy-r,cx+r,cy+r),outline=(67,190,204,a),width=4)
# broken ring segments
for start,end in [(8,62),(82,142),(170,226),(248,304)]: d.arc((cx-92,cy-92,cx+92,cy+92),start,end,fill=(174,203,200,230),width=10)
# central fracture
pts=[(355,74),(330,130),(348,164),(320,205),(344,234),(332,295)]
d.line(pts,fill=(15,20,24,255),width=17); d.line(pts,fill=(84,211,222,245),width=5)
# shards
for _ in range(36):
    ang=rnd.random()*math.tau; rr=rnd.randint(70,150); x=cx+math.cos(ang)*rr; y=cy+math.sin(ang)*rr*.72; s=rnd.randint(4,14)
    d.polygon([(x,y-s),(x+s*.6,y),(x,y+s),(x-s*.5,y)],fill=(70,151,161,rnd.randint(45,125)),outline=(162,202,198,80))
# horizon ruins
d.rectangle((0,285,W,360),fill=(12,18,22,255));
for x in range(20,720,55):
    h=rnd.randint(20,65); d.rectangle((x,285-h,x+18,285),fill=(24,31,35,255)); d.polygon([(x-4,285-h),(x+9,270-h),(x+22,285-h)],fill=(24,31,35,255))
save_webp(im,'quest_meridian_v083.webp',(720,360))

# WORLD ATLAS — portrait mobile map with geography only; faction influence is rendered separately in Android.
W,H=600,930
im=Image.new('RGB',(W,H),(7,19,29)); d=ImageDraw.Draw(im,'RGBA')
for y in range(H):
    t=y/H; d.line((0,y,W,y),fill=(7+int(4*t),19+int(8*t),29+int(10*t),255))
for _ in range(2200):
    x=rnd.randrange(W);y=rnd.randrange(H);a=rnd.randrange(5,18);d.point((x,y),fill=rnd.choice([(105,143,151,a),(188,163,105,a),(35,72,87,a)]))
land=Image.new('L',(W,H),0); ld=ImageDraw.Draw(land)
poly=[(38,70),(155,42),(272,73),(353,46),(490,80),(560,155),(545,246),(582,335),(548,430),(571,545),(528,650),(550,765),(486,875),(355,910),(236,880),(145,904),(62,824),(73,711),(31,616),(55,503),(26,402),(51,302),(27,206)]
ld.polygon(poly,fill=255)
for cx0,cy0,rx,ry in [(155,165,110,105),(300,170,150,120),(430,240,135,160),(195,380,170,170),(395,435,170,190),(245,635,190,210),(430,730,145,175)]: ld.ellipse((cx0-rx,cy0-ry,cx0+rx,cy0+ry),fill=255)
land=land.filter(ImageFilter.GaussianBlur(5)); fill=Image.new('RGBA',(W,H),(54,73,61,0)); fill.putalpha(land); im=Image.alpha_composite(im.convert('RGBA'),fill); d=ImageDraw.Draw(im,'RGBA')
coast=land.filter(ImageFilter.FIND_EDGES).filter(ImageFilter.GaussianBlur(1)); rim=Image.new('RGBA',(W,H),(185,154,91,0)); rim.putalpha(coast.point(lambda p:min(130,p))); im=Image.alpha_composite(im,rim); d=ImageDraw.Draw(im,'RGBA')
# region shading, mountains, forests
for pts,col in [([(45,80),(260,55),(330,300),(270,520),(52,510)],(75,93,67,55)), ([(315,60),(555,125),(545,510),(350,470)],(88,65,55,52)), ([(160,500),(550,455),(520,875),(250,900),(95,760)],(43,91,63,62))]: d.polygon(pts,fill=col)
def mountain(x,y,s=1.0):
    w=13*s;h=19*s;d.polygon([(x-w,y+h*.6),(x,y-h),(x+w,y+h*.6)],fill=(28,31,32,220),outline=(184,164,122,120));d.line((x,y-h,x+2*s,y-h*.15),fill=(222,214,186,130),width=max(1,int(2*s)))
for pts in [[(92,108),(112,128),(132,100),(152,138),(172,112),(195,145)],[(390,120),(416,150),(440,120),(466,165),(492,135),(520,178)],[(370,280),(398,305),(430,270),(460,310),(490,285)]]:
    for i,(x,y) in enumerate(pts): mountain(x,y,1.0+(i%3)*.12)
def tree(x,y,s=1): d.rectangle((x-1*s,y+5*s,x+1*s,y+10*s),fill=(73,53,36,180)); d.polygon([(x,y-9*s),(x-7*s,y+6*s),(x+7*s,y+6*s)],fill=(29,79,57,220),outline=(87,126,83,80))
for box,count in [((120,190,285,360),90),((335,510,515,720),105),((130,590,300,760),70)]:
    x1,y1,x2,y2=box
    for _ in range(count): tree(rnd.randint(x1,x2),rnd.randint(y1,y2),rnd.choice([.6,.75,.9]))
def river(points,width=6): d.line(points,fill=(7,17,23,210),width=width+5,joint='curve');d.line(points,fill=(42,126,164,230),width=width,joint='curve');d.line(points,fill=(110,184,205,90),width=max(1,width//3),joint='curve')
river([(245,65),(240,150),(250,240),(238,330),(255,425),(245,520),(268,610),(290,710),(315,900)],7);river([(465,70),(450,155),(455,245),(430,330),(448,425),(420,520),(400,610),(390,720),(410,900)],5);river([(250,330),(205,360),(165,410)],3);river([(430,330),(480,365),(535,420)],3)
nodes={'L':(215,340),'A':(120,115),'G':(225,170),'V':(325,300),'E':(325,455),'S':(280,430),'H':(72,470),'C':(450,250),'K':(505,335),'D':(510,130),'P':(450,555),'W':(400,690),'M':(470,805),'B':(290,865)}
def road(a,b):
    p1=nodes[a];p2=nodes[b];mx=(p1[0]+p2[0])//2+rnd.randint(-20,20);my=(p1[1]+p2[1])//2+rnd.randint(-20,20);pts=[p1,(mx,my),p2];d.line(pts,fill=(20,18,15,210),width=5,joint='curve');d.line(pts,fill=(210,177,104,210),width=2,joint='curve')
for a,b in [('L','A'),('L','G'),('L','V'),('L','E'),('L','S'),('L','H'),('V','C'),('C','D'),('C','K'),('E','P'),('P','W'),('W','M'),('W','B')]: road(a,b)
for key,(x,y) in nodes.items(): d.ellipse((x-7,y-7,x+7,y+7),fill=(7,15,19,235),outline=(219,181,96,220),width=2); d.ellipse((x-2,y-2,x+2,y+2),fill=(226,205,139,255))
for r,a in [(26,80),(43,55),(61,32)]: d.ellipse((215-r,340-r,215+r,340+r),outline=(67,178,192,a),width=2)
im=ImageEnhance.Contrast(im.convert('RGB')).enhance(1.06)
save_webp(im,'world_map_v083.webp',(600,930))

# Remove obsolete base64 loader from the patch script inside the workflow checkout.
ap=ROOT/'v083/apply_v083.py'; s=ap.read_text(encoding='utf-8'); start=s.find('assets={'); end=s.find('def must(',start)
if start>=0 and end>start: s=s[:start]+'# v0.8.3 assets generated by prepare_v083_assets.py\n\n'+s[end:]
ap.write_text(s,encoding='utf-8')
print('v0.8.3 deterministic asset preparation complete')
