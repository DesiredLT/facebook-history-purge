from pathlib import Path
import base64, random, math
from PIL import Image, ImageDraw, ImageFilter, ImageEnhance

ROOT=Path('.')
AS=ROOT/'v083/assets'
RES=ROOT/'app/src/main/res/drawable-nodpi'
RES.mkdir(parents=True,exist_ok=True)

# Decode premium art from connector-safe chunks.
expected={
    'hero_einoras_v083.webp':(450,600),
    'scene_palace_v083.webp':(600,250),
    'quest_meridian_v083.webp':(600,300),
}
for name,sz in expected.items():
    parts=sorted(AS.glob(name+'.b64*'))
    if not parts: raise SystemExit('Missing asset chunks: '+name)
    text=''.join(p.read_text(encoding='utf-8').strip() for p in parts)
    raw=base64.b64decode(text,validate=True)
    q=RES/name;q.write_bytes(raw)
    with Image.open(q) as im:
        im.load();assert im.format=='WEBP' and im.size==sz,(name,im.format,im.size,sz)
        colors=len(set(im.resize((32,32)).convert('RGB').getdata()))
        assert colors>120,(name,colors)
    print('DECODE OK',name,sz,len(raw))

# Deterministic fantasy atlas: no faction blobs baked into the geography.
W,H=600,930
rnd=random.Random(923083)
img=Image.new('RGB',(W,H),(7,19,29));d=ImageDraw.Draw(img,'RGBA')
# Sea depth and paper/noise texture.
for y in range(H):
    t=y/H; col=(7+int(4*t),19+int(8*t),29+int(10*t),255); d.line((0,y,W,y),fill=col)
for _ in range(2600):
    x=rnd.randrange(W);y=rnd.randrange(H);a=rnd.randrange(5,19);v=rnd.choice([(105,143,151,a),(188,163,105,a),(35,72,87,a)])
    d.point((x,y),fill=v)
for y in range(35,H,38): d.line((0,y,W,y+rnd.randint(-3,3)),fill=(92,132,144,18),width=1)

# Land mask with non-geometric coastline.
land=Image.new('L',(W,H),0);ld=ImageDraw.Draw(land)
poly=[(38,70),(155,42),(272,73),(353,46),(490,80),(560,155),(545,246),(582,335),(548,430),(571,545),(528,650),(550,765),(486,875),(355,910),(236,880),(145,904),(62,824),(73,711),(31,616),(55,503),(26,402),(51,302),(27,206)]
ld.polygon(poly,fill=255)
for cx,cy,rx,ry in [(155,165,110,105),(300,170,150,120),(430,240,135,160),(195,380,170,170),(395,435,170,190),(245,635,190,210),(430,730,145,175)]:
    ld.ellipse((cx-rx,cy-ry,cx+rx,cy+ry),fill=255)
land=land.filter(ImageFilter.GaussianBlur(5))
shadow=land.filter(ImageFilter.GaussianBlur(18));sh=Image.new('RGBA',(W,H),(0,0,0,0));sh.putalpha(shadow.point(lambda p:int(p*.32)));img=Image.alpha_composite(img.convert('RGBA'),sh)
landfill=Image.new('RGBA',(W,H),(54,73,61,0));landfill.putalpha(land)
img=Image.alpha_composite(img,landfill);d=ImageDraw.Draw(img,'RGBA')
# Coast rims.
coast=land.filter(ImageFilter.FIND_EDGES).filter(ImageFilter.GaussianBlur(1));rim=Image.new('RGBA',(W,H),(185,154,91,0));rim.putalpha(coast.point(lambda p:min(130,p)))
img=Image.alpha_composite(img,rim);d=ImageDraw.Draw(img,'RGBA')

# Terrain regions as subtle natural shading.
d.polygon([(45,80),(260,55),(330,300),(270,520),(52,510)],fill=(75,93,67,55))
d.polygon([(315,60),(555,125),(545,510),(350,470)],fill=(88,65,55,52))
d.polygon([(160,500),(550,455),(520,875),(250,900),(95,760)],fill=(43,91,63,62))
# Marshes and highlands.
for _ in range(65):
    x=rnd.randint(235,360);y=rnd.randint(780,900);r=rnd.randint(2,5);d.ellipse((x-r,y-r,x+r,y+r),fill=(53,104,86,70))
for _ in range(45):
    x=rnd.randint(410,555);y=rnd.randint(95,280);r=rnd.randint(5,11);d.ellipse((x-r,y-r,x+r,y+r),fill=(107,67,53,35))

# Mountain chains.
def mountain(x,y,s=1.0):
    w=13*s;h=19*s
    d.polygon([(x-w,y+h*.6),(x,y-h),(x+w,y+h*.6)],fill=(28,31,32,220),outline=(184,164,122,120))
    d.line((x,y-h,x+2*s,y-h*.15),fill=(222,214,186,130),width=max(1,int(2*s)))
for pts in [[(92,108),(112,128),(132,100),(152,138),(172,112),(195,145)],[(390,120),(416,150),(440,120),(466,165),(492,135),(520,178)],[(370,280),(398,305),(430,270),(460,310),(490,285)]]:
    for i,(x,y) in enumerate(pts): mountain(x,y,1.0+(i%3)*.12)

# Forests.
def tree(x,y,s=1):
    d.rectangle((x-1*s,y+5*s,x+1*s,y+10*s),fill=(73,53,36,180));d.polygon([(x,y-9*s),(x-7*s,y+6*s),(x+7*s,y+6*s)],fill=(29,79,57,220),outline=(87,126,83,80))
for box,count in [((120,190,285,360),95),((335,510,515,720),110),((130,590,300,760),75)]:
    x1,y1,x2,y2=box
    for _ in range(count): tree(rnd.randint(x1,x2),rnd.randint(y1,y2),rnd.choice([.6,.75,.9]))

# Rivers: under-stroke + cyan water.
def river(points,width=6):
    d.line(points,fill=(7,17,23,210),width=width+5,joint='curve');d.line(points,fill=(42,126,164,230),width=width,joint='curve');d.line(points,fill=(110,184,205,90),width=max(1,width//3),joint='curve')
river([(245,65),(240,150),(250,240),(238,330),(255,425),(245,520),(268,610),(290,710),(315,900)],7)
river([(465,70),(450,155),(455,245),(430,330),(448,425),(420,520),(400,610),(390,720),(410,900)],5)
# Branches.
river([(250,330),(205,360),(165,410)],3);river([(430,330),(480,365),(535,420)],3)

# Roads between actual node coordinates.
nodes={'L':(215,340),'A':(120,115),'G':(225,170),'V':(325,300),'E':(325,455),'S':(280,430),'H':(72,470),'C':(450,250),'K':(505,335),'D':(510,130),'P':(450,555),'W':(400,690),'M':(470,805),'B':(290,865)}
def road(a,b):
    p1=nodes[a];p2=nodes[b];mx=(p1[0]+p2[0])//2+rnd.randint(-20,20);my=(p1[1]+p2[1])//2+rnd.randint(-20,20);pts=[p1,(mx,my),p2]
    d.line(pts,fill=(20,18,15,210),width=5,joint='curve');d.line(pts,fill=(210,177,104,210),width=2,joint='curve')
for a,b in [('L','A'),('L','G'),('L','V'),('L','E'),('L','S'),('L','H'),('V','C'),('C','D'),('C','K'),('E','P'),('P','W'),('W','M'),('W','B')]:road(a,b)

# Landmarks without labels; Android overlay adds interactive labels/types.
for key,(x,y) in nodes.items():
    d.ellipse((x-7,y-7,x+7,y+7),fill=(7,15,19,235),outline=(219,181,96,220),width=2)
    d.ellipse((x-2,y-2,x+2,y+2),fill=(226,205,139,255))
# Ancient Meridian rings around Luminara and anomaly marks.
for r,a in [(26,80),(43,55),(61,32)]:d.ellipse((215-r,340-r,215+r,340+r),outline=(67,178,192,a),width=2)
for x,y in [(225,170),(280,430),(510,130)]:
    d.arc((x-18,y-18,x+18,y+18),15,300,fill=(87,198,210,150),width=2)

# Vignette and final contrast.
vig=Image.new('L',(W,H),0);vd=ImageDraw.Draw(vig);vd.ellipse((-W*.2,-H*.08,W*1.2,H*1.08),fill=15);vig=vig.filter(ImageFilter.GaussianBlur(80));overlay=Image.new('RGBA',(W,H),(0,0,0,0));overlay.putalpha(Image.eval(vig,lambda p:max(0,145-p)));img=Image.alpha_composite(img,overlay).convert('RGB')
img=ImageEnhance.Contrast(img).enhance(1.06);img=ImageEnhance.Sharpness(img).enhance(1.08)
q=RES/'world_map_v083.webp';img.save(q,'WEBP',quality=82,method=6)
with Image.open(q) as im: im.load();assert im.format=='WEBP' and im.size==(W,H)
print('GENERATED MAP OK',q.stat().st_size)

# Connector-safe build patch: assets are already prepared, so remove old single-file loader.
ap=ROOT/'v083/apply_v083.py';s=ap.read_text(encoding='utf-8');start=s.find('assets={');end=s.find('def must(',start)
if start<0 or end<0: raise SystemExit('apply_v083 asset loader boundaries missing')
s=s[:start]+'# Assets prepared by prepare_v083_assets.py\n\n'+s[end:]
# Keep interactive node labels visible; the new atlas intentionally contains no baked labels.
old="s=s.replace('p.setColor(Color.argb(230,3,9,13));','p.setColor(Color.argb(0,3,9,13));').replace('p.setColor(Color.rgb(235,230,214));c.drawText(n.name,lx,ly,p);','p.setColor(Color.argb(0,235,230,214));c.drawText(n.name,lx,ly,p);')"
s=s.replace(old,"# Keep Android-rendered location labels visible on v0.8.3 atlas")
ap.write_text(s,encoding='utf-8')
print('v0.8.3 asset preparation complete')
