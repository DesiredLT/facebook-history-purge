from PIL import Image, ImageDraw, ImageFont, ImageFilter
import math, random
from pathlib import Path

OUT=Path('app/src/main/res/drawable-nodpi'); OUT.mkdir(parents=True,exist_ok=True)
random.seed(92307)

def font(size,bold=False,serif=False):
    paths=[]
    if serif and bold: paths=['/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf']
    elif serif: paths=['/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf']
    elif bold: paths=['/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf']
    else: paths=['/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf']
    for p in paths:
        try:return ImageFont.truetype(p,size)
        except:pass
    return ImageFont.load_default()

def gradient(size,top,bottom):
    w,h=size; im=Image.new('RGB',size,top); px=im.load()
    for y in range(h):
        t=y/(h-1)
        c=tuple(int(top[i]*(1-t)+bottom[i]*t) for i in range(3))
        for x in range(w): px[x,y]=c
    return im

def add_noise(im,amount=10,alpha=22):
    layer=Image.new('RGBA',im.size,(0,0,0,0)); d=ImageDraw.Draw(layer)
    w,h=im.size
    for _ in range((w*h)//220):
        x=random.randrange(w);y=random.randrange(h);v=random.randint(-amount,amount)
        col=(max(0,130+v),max(0,150+v),max(0,155+v),random.randint(3,alpha))
        d.point((x,y),fill=col)
    return Image.alpha_composite(im.convert('RGBA'),layer)

def glow_dot(layer,xy,r,color):
    g=Image.new('RGBA',layer.size,(0,0,0,0)); d=ImageDraw.Draw(g);x,y=xy
    for rr,a in [(r*4,18),(r*2,45),(r,210)]:d.ellipse((x-rr,y-rr,x+rr,y+rr),fill=(*color,a))
    return Image.alpha_composite(layer,g.filter(ImageFilter.GaussianBlur(max(1,r//2))))

def qcurve(p1,p2,bend=.12,n=40):
    x1,y1=p1;x2,y2=p2;mx=(x1+x2)/2;my=(y1+y2)/2-math.hypot(x2-x1,y2-y1)*bend
    pts=[]
    for i in range(n+1):
        t=i/n;u=1-t
        pts.append((u*u*x1+2*u*t*mx+t*t*x2,u*u*y1+2*u*t*my+t*t*y2))
    return pts

W,H=1600,900
im=gradient((W,H),(6,18,29),(8,13,20)).convert('RGBA'); im=add_noise(im,8,16);d=ImageDraw.Draw(im,'RGBA')
for k in range(10):
    y=80+k*78
    pts=[]
    for x in range(-60,W+60,30): pts.append((x,y+math.sin(x/130+k)*8))
    d.line(pts,fill=(70,123,142,24),width=1)
asterra=[(150,220),(230,135),(380,105),(575,130),(730,160),(825,250),(850,365),(825,475),(730,560),(630,600),(490,635),(330,610),(210,540),(130,445),(110,330)]
dravenn=[(870,130),(1000,70),(1180,55),(1340,105),(1460,160),(1510,280),(1450,390),(1370,480),(1220,515),(1070,485),(930,450),(860,350),(850,240)]
lysara=[(760,560),(875,505),(1010,515),(1130,555),(1260,585),(1405,620),(1480,710),(1435,815),(1265,850),(1110,835),(945,850),(785,820),(720,730),(710,625)]
for poly,fill,edge in [(asterra,(31,50,58,255),(75,92,91,255)),(dravenn,(56,44,42,255),(98,74,64,255)),(lysara,(34,54,45,255),(69,93,74,255))]:
    d.polygon(poly,fill=fill);d.line(poly+[poly[0]],fill=edge,width=3)
d.ellipse((245,105,650,360),fill=(46,67,60,45));d.ellipse((620,100,790,280),fill=(29,75,55,85));d.ellipse((1040,55,1460,270),fill=(76,57,48,70));d.ellipse((740,680,1240,865),fill=(35,83,56,70));d.ellipse((690,700,930,860),fill=(45,79,72,95))
for center,count in [((725,165),38),((1120,735),55),((900,650),28)]:
    cx,cy=center
    for _ in range(count):
        x=cx+random.randint(-95,95);y=cy+random.randint(-75,75);s=random.randint(4,8)
        d.polygon([(x,y-s),(x-s,y+s),(x+s,y+s)],fill=(68,113,76,135))
for center,count in [((1280,150),34),((980,220),17),((330,190),16)]:
    cx,cy=center
    for _ in range(count):
        x=cx+random.randint(-130,130);y=cy+random.randint(-65,65);s=random.randint(5,11)
        d.line([(x-s,y+s),(x,y-s),(x+s,y+s)],fill=(181,166,142,150),width=2)
        if s>8:d.line([(x-2,y-s+4),(x,y-s),(x+3,y-s+5)],fill=(224,220,199,120),width=1)
for _ in range(35):
    x=790+random.randint(-105,105);y=755+random.randint(-55,65);d.line((x,y,x+random.randint(-3,3),y-random.randint(5,12)),fill=(77,137,119,120),width=2)
L={
'Luminara':(500,330,3),'Veyrhold':(665,255,3),'Aureliono Pakraštys':(760,405,4),'Asterio Karūna':(350,195,4),'Žvaigždėkritos Skliautas':(540,495,7),'Stiklo Giria':(735,155,5),'Tuščiavidurė Smailė':(320,555,8),'Kharad Vorn':(1140,280,5),'Drakono Pabudimo Viršūnės':(1330,155,8),'Pelenų Karūnos Citadelė':(965,210,7),'Safyro Platybės':(1010,535,7),'Amžinojo Šaltinio Slėnis':(915,650,4),'Žaliasis Labirintas':(1135,735,8),'Šventųjų Pelkynas':(790,755,6)}
routes=[('Luminara','Veyrhold',.1,False),('Luminara','Asterio Karūna',.12,False),('Luminara','Žvaigždėkritos Skliautas',.08,False),('Luminara','Tuščiavidurė Smailė',-.08,False),('Veyrhold','Aureliono Pakraštys',.1,False),('Aureliono Pakraštys','Kharad Vorn',.06,True),('Kharad Vorn','Drakono Pabudimo Viršūnės',-.08,False),('Kharad Vorn','Safyro Platybės',.1,False),('Safyro Platybės','Amžinojo Šaltinio Slėnis',-.07,False),('Amžinojo Šaltinio Slėnis','Žaliasis Labirintas',.07,False),('Amžinojo Šaltinio Slėnis','Šventųjų Pelkynas',-.06,False),('Aureliono Pakraštys','Safyro Platybės',.12,True)]
for a,b,bend,way in routes:
    pts=qcurve(L[a][:2],L[b][:2],bend)
    if way:
        for j in range(0,len(pts)-1,4): d.line(pts[j:j+3],fill=(87,164,199,160),width=3)
    else:d.line(pts,fill=(131,119,96,125),width=2)
for text,xy in [('ASTERRA',(390,405)),('DRAVENN',(1120,360)),('LYSARA',(1080,695))]:
    d.text(xy,text,font=font(34,True,True),fill=(222,187,104,55),anchor='mm')
for name,(x,y,danger) in L.items():
    col=(221,187,104,255) if name=='Luminara' else ((222,92,78,230) if danger>=8 else (231,144,77,220) if danger>=6 else (165,191,176,220))
    r=8 if name=='Luminara' else 6
    d.ellipse((x-r-2,y-r-2,x+r+2,y+r+2),fill=(2,7,11,220));d.ellipse((x-r,y-r,x+r,y+r),fill=col)
    offsets={'Drakono Pabudimo Viršūnės':(-5,20),'Aureliono Pakraštys':(10,14),'Šventųjų Pelkynas':(10,12),'Žaliasis Labirintas':(10,12),'Tuščiavidurė Smailė':(10,14),'Asterio Karūna':(10,-30)}
    ox,oy=offsets.get(name,(10,-28))
    d.text((x+ox,y+oy),name,font=font(16,True),fill=(226,229,218,225),stroke_width=2,stroke_fill=(3,8,12,210))
cx,cy=1475,760
d.ellipse((cx-46,cy-46,cx+46,cy+46),outline=(221,187,104,100),width=2)
for ang,label in [(0,'N'),(90,'E'),(180,'S'),(270,'W')]:
    a=math.radians(ang-90);x=cx+math.cos(a)*35;y=cy+math.sin(a)*35;d.line((cx,cy,x,y),fill=(221,187,104,110),width=2);d.text((x,y),label,font=font(12,True),fill=(232,209,156,180),anchor='mm')
d.rounded_rectangle((8,8,W-8,H-8),radius=28,outline=(111,102,83,95),width=2)
im.convert('RGB').save(OUT/'world_map_v070.png',quality=95)

W,H=1440,900
im=gradient((W,H),(14,24,47),(15,11,24)).convert('RGBA');d=ImageDraw.Draw(im,'RGBA')
for _ in range(300):
    x=random.randrange(W);y=random.randrange(int(H*.64));a=random.randint(35,140);r=random.choice([1,1,1,2]);d.ellipse((x-r,y-r,x+r,y+r),fill=(180,215,229,a))
for r,a in [(250,10),(180,18),(110,35),(64,70)]:d.ellipse((1040-r,260-r,1040+r,260+r),fill=(92,173,218,a))
for i in range(7):
    x=270+i*125;y=245+(i%3)*45;rx=58+(i%2)*18;ry=20+(i%2)*5
    d.ellipse((x-rx,y-ry,x+rx,y+ry),outline=(91,169,214,160 if i!=3 else 220),width=4)
    d.ellipse((x-rx+8,y-ry+4,x+rx-8,y+ry-4),outline=(220,184,101,80),width=2)
haze=Image.new('RGBA',(W,H),(0,0,0,0));hd=ImageDraw.Draw(haze)
for r,a in [(480,12),(330,20),(230,28)]:hd.ellipse((720-r,500-r*.35,720+r,500+r*.35),fill=(222,183,96,a))
haze=haze.filter(ImageFilter.GaussianBlur(32));im=Image.alpha_composite(im,haze);d=ImageDraw.Draw(im,'RGBA')
for layer,(base,col) in enumerate([(650,(18,27,42,210)),(720,(9,17,29,240)),(800,(4,10,18,255))]):
    pts=[(0,H),(0,base)]
    x=0
    while x<W:
        bw=random.randint(24,55);bh=random.randint(30,150)*(1 if layer>0 else .6);pts += [(x,base),(x+bw*.35,base-bh),(x+bw*.65,base-bh),(x+bw,base)];x+=bw+random.randint(3,12)
    pts += [(W,H)];d.polygon(pts,fill=col)
cx=720;by=775
for off,col,wid in [(0,(38,56,76,255),22),(-2,(221,187,104,210),5)]:
    box=(cx-165+off,by-330+off,cx+165-off,by+6-off);d.arc(box,195,345,fill=col,width=wid)
d.polygon([(0,780),(1440,760),(1440,900),(0,900)],fill=(4,9,17,255))
for _ in range(60):
    x=random.randrange(W);y=random.randrange(760,900);col=random.choice([(87,161,208,70),(221,187,104,55)]);d.line((x,y,x+random.randint(5,30),y),fill=col,width=random.choice([1,2]))
for _ in range(180):
    x=random.randrange(W);y=random.randrange(520,805);r=random.choice([1,1,2]);col=random.choice([(221,187,104,180),(89,164,210,150)]);d.ellipse((x-r,y-r,x+r,y+r),fill=col)
d.polygon([(90,900),(90,460),(150,390),(210,460),(210,900)],fill=(3,8,14,255));d.polygon([(1230,900),(1230,500),(1290,415),(1355,500),(1355,900)],fill=(3,8,14,255))
im=add_noise(im,10,12).convert('RGB');im.save(OUT/'scene_luminara_v070.png',quality=95)

W,H=1440,900
im=gradient((W,H),(12,30,37),(5,11,15)).convert('RGBA');d=ImageDraw.Draw(im,'RGBA')
for _ in range(170):
    x=random.randrange(W);y=random.randrange(80,520);r=random.choice([1,1,2]);d.ellipse((x-r,y-r,x+r,y+r),fill=(138,192,178,random.randint(40,130)))
for yy,a in [(520,32),(610,42),(700,55)]:d.ellipse((-250,yy-80,W+250,yy+120),fill=(91,138,126,a))
pts=[(0,610)]
for x in range(0,W+150,150):pts.append((x,430-random.randint(0,150)));pts.append((min(W,x+120),610))
pts += [(W,H),(0,H)];d.polygon(pts,fill=(13,31,33,230))
for _ in range(90):
    x=random.randrange(W);base=random.randrange(620,900);hh=random.randrange(100,330);ww=hh*.28
    col=random.choice([(5,17,17,255),(7,23,21,250),(9,28,24,245)]);d.rectangle((x-3,base-hh*.6,x+3,base),fill=col);d.polygon([(x,base-hh),(x-ww,base-hh*.32),(x+ww,base-hh*.32)],fill=col);d.polygon([(x,base-hh*.72),(x-ww*.8,base-hh*.08),(x+ww*.8,base-hh*.08)],fill=col)
d.polygon([(560,900),(880,900),(770,580),(690,580)],fill=(32,42,37,230))
for x,y in [(690,530),(760,500),(815,565)]:
    for r,a in [(70,18),(35,40),(8,210)]:d.ellipse((x-r,y-r,x+r,y+r),fill=(83,174,193,a))
im=add_noise(im,10,13).convert('RGB');im.save(OUT/'scene_wild_v070.png',quality=95)

W,H=900,1200
im=Image.new('RGBA',(W,H),(0,0,0,0));d=ImageDraw.Draw(im,'RGBA');cx=W//2
for r,a in [(330,12),(250,22),(170,35)]:d.ellipse((cx-r,370-r,cx+r,370+r),fill=(80,163,201,a))
cloak=[(cx-110,345),(cx-210,620),(cx-170,1050),(cx+185,1050),(cx+205,620),(cx+105,345)]
d.polygon(cloak,fill=(25,39,55,245));d.line(cloak+[cloak[0]],fill=(78,93,110,210),width=5)
d.polygon([(cx-70,720),(cx-10,720),(cx-28,1090),(cx-108,1090)],fill=(28,39,53,255));d.polygon([(cx+10,720),(cx+70,720),(cx+110,1090),(cx+28,1090)],fill=(28,39,53,255))
d.rounded_rectangle((cx-125,1055,cx-20,1120),20,fill=(13,20,30,255));d.rounded_rectangle((cx+20,1055,cx+125,1120),20,fill=(13,20,30,255))
armor=[(cx-95,360),(cx-140,560),(cx-90,745),(cx+90,745),(cx+140,560),(cx+95,360)]
d.polygon(armor,fill=(48,62,80,255));d.line(armor+[armor[0]],fill=(177,150,93,220),width=5)
for y in [470,535,600,665]:d.arc((cx-105,y-35,cx+105,y+35),10,170,fill=(119,126,132,180),width=3)
d.ellipse((cx-180,350,cx-65,465),fill=(52,67,86,255),outline=(177,150,93,210),width=4);d.ellipse((cx+65,350,cx+180,465),fill=(52,67,86,255),outline=(177,150,93,210),width=4)
d.ellipse((cx-70,215,cx+70,365),fill=(43,50,64,255));d.polygon([(cx-85,325),(cx,175),(cx+85,325)],fill=(22,31,43,245));d.line((cx-38,305,cx+38,305),fill=(221,187,104,220),width=5)
d.polygon([(cx-150,430),(cx-205,730),(cx-150,750),(cx-70,500)],fill=(38,51,68,255));d.polygon([(cx+150,430),(cx+205,730),(cx+150,750),(cx+70,500)],fill=(38,51,68,255))
g=Image.new('RGBA',(W,H),(0,0,0,0));gd=ImageDraw.Draw(g,'RGBA');gd.line((cx+125,290,cx+320,1000),fill=(221,187,104,80),width=26);g=g.filter(ImageFilter.GaussianBlur(15));im=Image.alpha_composite(im,g);d=ImageDraw.Draw(im,'RGBA');d.line((cx+125,290,cx+320,1000),fill=(228,211,170,230),width=12);d.line((cx+119,304,cx+147,292),fill=(221,187,104,255),width=18)
d.ellipse((cx-34,675,cx+34,743),outline=(221,187,104,230),width=5);d.line((cx-20,709,cx+20,709),fill=(221,187,104,200),width=3);d.line((cx,689,cx,729),fill=(221,187,104,200),width=3)
im.save(OUT/'hero_einoras_v070.png')

print('v0.7 assets:', *(p.name for p in OUT.glob('*v070*.png')))
