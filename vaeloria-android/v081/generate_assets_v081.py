from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter, ImageFont, ImageEnhance, ImageChops
import random, math

OUT=Path('app/src/main/res/drawable-nodpi'); OUT.mkdir(parents=True, exist_ok=True)
R=random.Random(801)
GOLD=(218,181,96,255); PARCH=(239,226,194,255); INK=(4,10,15,255)

def font(n,b=False,serif=False):
    cand=[]
    if serif:
        cand += ['/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf' if b else '/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf']
    cand += ['/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf' if b else '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf','/usr/share/fonts/truetype/liberation2/LiberationSans-Bold.ttf']
    for p in cand:
        try:return ImageFont.truetype(p,n)
        except:pass
    return ImageFont.load_default()

def gradient(size,top,bottom):
    w,h=size; im=Image.new('RGB',size); d=ImageDraw.Draw(im)
    for y in range(h):
        t=y/max(1,h-1); c=tuple(int(top[i]*(1-t)+bottom[i]*t) for i in range(3)); d.line((0,y,w,y),fill=c)
    return im.convert('RGBA')

def noise_layer(size,opacity=35,blur=1.4,scale=5):
    w,h=size; small=Image.effect_noise((max(2,w//scale),max(2,h//scale)),45).convert('L').resize(size,Image.Resampling.BICUBIC).filter(ImageFilter.GaussianBlur(blur))
    a=small.point(lambda v:int((v/255)*opacity))
    q=Image.new('RGBA',size,(255,244,213,0)); q.putalpha(a); return q

def blob(cx,cy,rx,ry,n=64,j=.13):
    return [(cx+math.cos(i*2*math.pi/n)*rx*(1+R.uniform(-j,j)),cy+math.sin(i*2*math.pi/n)*ry*(1+R.uniform(-j,j))) for i in range(n)]

def label(d,x,y,text,fill=PARCH,size=18,anchor='la'):
    f=font(size,True,True); box=d.textbbox((0,0),text,font=f); tw=box[2]-box[0]; th=box[3]-box[1]
    if anchor=='ra': x-=tw
    d.rounded_rectangle((x-7,y-5,x+tw+7,y+th+5),7,fill=(4,10,14,205),outline=(116,96,58,145),width=1)
    d.text((x,y),text,font=f,fill=fill)

def road(d,pts):
    d.line(pts,fill=(22,18,15,185),width=11,joint='curve'); d.line(pts,fill=(223,188,105,220),width=4,joint='curve'); d.line(pts,fill=(252,225,157,110),width=1,joint='curve')

def river(d,pts):
    d.line(pts,fill=(7,34,48,210),width=18,joint='curve'); d.line(pts,fill=(52,127,158,225),width=10,joint='curve'); d.line(pts,fill=(117,187,204,125),width=2,joint='curve')

def mountain(d,x,y,s=1,fire=False):
    base=(64,68,64,255) if not fire else (68,43,36,255); hi=(178,171,147,235) if not fire else (196,88,53,240)
    p=[(x-26*s,y+20*s),(x,y-31*s),(x+29*s,y+20*s)]; d.polygon(p,fill=(22,24,23,100)); d.polygon([(x-22*s,y+18*s),(x,y-27*s),(x+24*s,y+18*s)],fill=base)
    d.polygon([(x,y-27*s),(x+9*s,y-8*s),(x+3*s,y-11*s),(x-4*s,y-4*s),(x-10*s,y-8*s)],fill=hi)

def forest_patch(d,cx,cy,rx,ry,count=70):
    for _ in range(count):
        a=R.random()*math.tau; rr=math.sqrt(R.random()); x=cx+math.cos(a)*rx*rr; y=cy+math.sin(a)*ry*rr; r=R.choice([2,3,4,5]);
        col=R.choice([(23,58,39,220),(31,73,45,230),(41,88,51,225),(53,96,56,210)])
        d.ellipse((x-r,y-r,x+r,y+r),fill=col)

def city(d,x,y,name,lv,major=False):
    col={3:(92,196,145,255),4:(220,187,96,255),5:(221,154,74,255),6:(222,131,68,255),7:(213,92,69,255),8:(208,60,58,255)}.get(lv,GOLD)
    s=1.25 if major else .9
    d.ellipse((x-17*s,y-17*s,x+17*s,y+17*s),fill=(4,10,14,238),outline=col,width=max(2,int(3*s)))
    for dx,hh in [(-8,12),(0,18),(8,10)]:
        d.rectangle((x+dx*s-3*s,y-hh*s,x+dx*s+3*s,y+6*s),fill=(87,76,60,255)); d.polygon([(x+dx*s-4*s,y-hh*s),(x+dx*s,y-(hh+8)*s),(x+dx*s+4*s,y-hh*s)],fill=(119,96,65,255))
    d.ellipse((x-3*s,y-3*s,x+3*s,y+3*s),fill=col)
    anchor='la' if x<1050 else 'ra'; label(d,x+18 if anchor=='la' else x-18,y-12,name,size=15 if major else 13,anchor=anchor)

def world():
    W,H=1448,1086
    sea=gradient((W,H),(13,34,45),(4,21,31)); sea=Image.alpha_composite(sea,noise_layer((W,H),28,2,4)); d=ImageDraw.Draw(sea)
    for y in range(35,H,55):
        pts=[(x,y+7*math.sin(x/90+y/80)) for x in range(-20,W+40,24)]; d.line(pts,fill=(74,122,137,45),width=2)
    landmask=Image.new('L',(W,H),0); md=ImageDraw.Draw(landmask)
    A=blob(405,430,348,318);D=blob(1040,345,350,286);L=blob(900,805,430,245)
    for p in [A,D,L]: md.polygon(p,fill=255)
    md.ellipse((18,520,255,770),fill=220); md.ellipse((500,625,800,1060),fill=185); landmask=landmask.filter(ImageFilter.GaussianBlur(5))
    terrain=gradient((W,H),(112,102,72),(53,85,55)); n=Image.effect_noise((W//3,H//3),65).resize((W,H),Image.Resampling.BICUBIC).filter(ImageFilter.GaussianBlur(2))
    tint=Image.new('RGBA',(W,H),(34,69,43,0)); tint.putalpha(n.point(lambda v:int(30+v*.22))); terrain=Image.alpha_composite(terrain,tint)
    terrain=ImageEnhance.Contrast(terrain).enhance(1.15); terrain.putalpha(landmask); sea=Image.alpha_composite(sea,terrain); d=ImageDraw.Draw(sea)
    d.ellipse((10,515,265,775),fill=(124,100,58,135)); d.ellipse((505,630,810,1050),fill=(31,78,56,85)); d.ellipse((825,620,1380,1010),fill=(42,103,60,70)); d.ellipse((780,35,1370,470),fill=(91,60,49,65))
    river(d,[(510,115),(500,250),(470,350),(448,470),(455,590),(510,710),(575,840),(630,1030)])
    river(d,[(945,205),(915,330),(900,470),(870,585),(820,690),(760,810),(700,1010)])
    river(d,[(1180,165),(1140,275),(1105,395),(1060,505)])
    for i in range(42): mountain(d,805+i*13,R.randint(105,380),R.choice([.65,.8,1,1.15]),18<i<28)
    for i in range(25): mountain(d,95+i*19,R.randint(135,320),R.choice([.55,.7,.85]))
    for args in [(330,430,270,250,150),(760,820,250,180,120),(1110,760,220,180,105),(620,510,160,130,80)]:forest_patch(d,*args)
    routes=[[(388,405),(330,300),(270,183)],[(388,405),(450,290),(505,180)],[(388,405),(505,355),(620,326)],[(388,405),(480,455),(525,512)],[(388,405),(570,430),(705,513)],[(705,513),(820,410),(925,275)],[(925,275),(1060,320),(1180,365)],[(1180,365),(1210,240),(1175,90)],[(705,513),(840,555),(967,600)],[(967,600),(875,680),(770,741)],[(770,741),(690,825),(617,920)],[(770,741),(980,770),(1190,790)],[(110,613),(225,520),(388,405)]]
    for pts in routes: road(d,pts)
    outline=landmask.filter(ImageFilter.FIND_EDGES).filter(ImageFilter.GaussianBlur(1)); glow=Image.new('RGBA',(W,H),(205,181,113,0)); glow.putalpha(outline.point(lambda v:min(80,v//3))); sea=Image.alpha_composite(sea,glow); d=ImageDraw.Draw(sea)
    for x,y,t in [(170,560,'ASTERRA'),(1030,470,'DRAVENN'),(820,875,'LYSARA')]: d.text((x,y),t,font=font(44,True,True),fill=(237,219,175,145))
    cs=[(388,405,'Luminara',3,1),(270,183,'Asterio Karūna',4,0),(505,180,'Stiklo Giria',5,0),(620,326,'Veyrhold',3,0),(705,513,'Aureliono Pakraštys',4,0),(525,512,'Žvaigždėkritos Skliautas',7,0),(110,613,'Tuščiavidurė Smailė',8,0),(925,275,'Pelenų Karūnos Citadelė',7,0),(1180,365,'Kharad Vorn',5,0),(1175,90,'Drakono Pabudimo Viršūnės',8,0),(967,600,'Safyro Platybės',7,0),(770,741,'Amžinojo Šaltinio Slėnis',4,0),(617,920,'Šventųjų Pelkynas',6,0),(1190,790,'Žaliasis Labirintas',8,0)]
    for x,y,nm,lv,mj in cs: city(d,x,y,nm,lv,bool(mj))
    sea=ImageEnhance.Color(sea.convert('RGB')).enhance(.86); sea=ImageEnhance.Contrast(sea).enhance(1.08); sea.save(OUT/'world_map_v060.png',optimize=True)

def scene():
    W,H=1100,460; im=gradient((W,H),(30,43,62),(146,81,45)); im=Image.alpha_composite(im,noise_layer((W,H),18,2,6)); d=ImageDraw.Draw(im)
    for _ in range(35):
        x=R.randint(-80,W);y=R.randint(30,190);rx=R.randint(45,130);ry=R.randint(14,35);d.ellipse((x-rx,y-ry,x+rx,y+ry),fill=(20,27,36,R.randint(18,55)))
    d.ellipse((835,35,930,130),fill=(246,208,137,145))
    shore=310
    d.rectangle((0,shore,W,H),fill=(14,45,60,255))
    for _ in range(110):
        x=R.randint(0,W); y=R.randint(shore,H); col=R.choice([(215,155,76,55),(107,161,170,45),(240,192,110,42)]); d.line((x,y,x+R.randint(6,30),y),fill=col,width=1)
    for i in range(44):
        x=10+i*25+R.randint(-5,5); bh=R.randint(55,190); bw=R.randint(14,26); base=shore
        col=R.choice([(32,39,46,255),(43,47,50,255),(53,50,48,255)])
        d.rectangle((x,base-bh,x+bw,base),fill=col); d.polygon([(x-4,base-bh),(x+bw/2,base-bh-R.randint(18,48)),(x+bw+4,base-bh)],fill=(72,59,52,255))
        if R.random()<.45:
            for yy in range(base-bh+18,base-12,18): d.rectangle((x+5,yy,x+8,yy+5),fill=(226,174,83,180))
    cx=760
    for dx,bw,bh in [(-100,80,210),(-35,70,255),(35,80,230),(105,55,180)]:
        x=cx+dx;d.rectangle((x,shore-bh,x+bw,shore),fill=(47,45,45,255),outline=(108,84,61,180),width=2);d.polygon([(x-8,shore-bh),(x+bw/2,shore-bh-55),(x+bw+8,shore-bh)],fill=(81,62,50,255))
    d.line((120,330,650,305),fill=(89,72,53,210),width=8)
    for x in range(140,650,45):d.ellipse((x,316,x+5,321),fill=(240,185,83,230))
    vg=Image.new('L',(W,H),0);vd=ImageDraw.Draw(vg);vd.rectangle((0,0,W,H),fill=150);vd.ellipse((-120,-160,W+120,H+200),fill=0);vg=vg.filter(ImageFilter.GaussianBlur(60));dark=Image.new('RGBA',(W,H),(0,0,0,0));dark.putalpha(vg);im=Image.alpha_composite(im,dark)
    ImageEnhance.Contrast(im.convert('RGB')).enhance(1.08).save(OUT/'scene_luminara.png',optimize=True)

def combat():
    W,H=1100,660; im=gradient((W,H),(20,41,40),(4,12,16)); d=ImageDraw.Draw(im)
    for layer in range(5):
        alpha=115+layer*25; yy=70+layer*25
        for i in range(26):
            x=i*48+R.randint(-25,22); trunk=R.randint(210,560); w=R.randint(9,18); d.rectangle((x,yy,x+w,yy+trunk),fill=(20+layer*4,48+layer*6,40+layer*4,alpha));
            for _ in range(3):
                rx=R.randint(35,80);ry=R.randint(18,42);d.ellipse((x-rx,yy-R.randint(10,45),x+rx,yy+ry),fill=(25+layer*4,64+layer*6,46+layer*4,alpha))
    d.polygon([(350,H),(520,300),(650,300),(840,H)],fill=(74,65,47,205))
    for _ in range(100):
        x=R.randint(340,850); y=R.randint(340,H); r=R.randint(1,5); d.ellipse((x-r,y-r,x+r,y+r),fill=(128,111,72,R.randint(70,170)))
    def fighter(x,y,enemy=False):
        skin=(152,116,91,255); metal=(58,62,61,255) if not enemy else (46,48,48,255); accent=(214,184,101,255) if not enemy else (183,73,59,255)
        d.ellipse((x-20,y-125,x+20,y-85),fill=(29,28,27,255)); d.ellipse((x-13,y-118,x+13,y-92),fill=skin)
        d.polygon([(x-38,y-82),(x+34,y-82),(x+52,y+30),(x-45,y+40)],fill=metal); d.polygon([(x-44,y-75),(x-78,y-20),(x-62,y-10),(x-25,y-45)],fill=metal)
        d.line((x-25,y+30,x-45,y+115),fill=(31,31,30,255),width=18); d.line((x+22,y+30,x+46,y+115),fill=(31,31,30,255),width=18)
        if enemy:d.line((x-10,y-55,x-100,y+20),fill=accent,width=7)
        else:d.line((x+5,y-50,x+105,y+10),fill=accent,width=7)
        d.ellipse((x-43,y-88,x-22,y-67),fill=(94,91,80,255));d.ellipse((x+20,y-88,x+42,y-67),fill=(94,91,80,255))
    fighter(360,440,False); fighter(745,415,True)
    for _ in range(70):
        x=R.randint(0,W);y=R.randint(100,560);r=R.choice([1,1,2]);d.ellipse((x-r,y-r,x+r,y+r),fill=(111,186,152,R.randint(25,100)))
    ImageEnhance.Contrast(im.convert('RGB')).enhance(1.12).save(OUT/'combat_forest.png',optimize=True)

def hero():
    W,H=720,960; im=gradient((W,H),(24,37,43),(3,9,13)); d=ImageDraw.Draw(im)
    for r in range(285,75,-26):d.ellipse((W//2-r,H*.39-r,W//2+r,H*.39+r),outline=(205,174,101,max(18,115-r//4)),width=3)
    d.polygon([(225,325),(495,325),(590,900),(130,900)],fill=(18,25,29,255))
    d.polygon([(300,575),(350,575),(330,880),(275,880)],fill=(32,34,35,255));d.polygon([(370,575),(420,575),(450,880),(395,880)],fill=(32,34,35,255))
    d.polygon([(270,300),(450,300),(495,610),(225,610)],fill=(45,48,48,255),outline=(133,112,74,255))
    for yy,ww in [(350,190),(415,205),(480,218),(545,228)]:d.rounded_rectangle((W/2-ww/2,yy,W/2+ww/2,yy+48),12,fill=(68,70,67,255),outline=(184,151,83,230),width=3)
    for x in [245,475]:
        d.ellipse((x-62,285,x+20,380),fill=(55,57,55,255),outline=(170,142,82,210),width=3)
        for _ in range(20):
            xx=x+R.randint(-55,12); yy=R.randint(285,355);d.line((xx,yy,xx+R.randint(-10,10),yy+R.randint(12,28)),fill=(82,72,55,180),width=3)
    d.polygon([(220,350),(270,345),(245,610),(185,640)],fill=(43,45,45,255));d.polygon([(450,345),(500,355),(540,635),(480,610)],fill=(43,45,45,255))
    d.rectangle((335,255,385,322),fill=(148,108,82,255));d.ellipse((295,118,430,290),fill=(159,117,88,255))
    d.pieslice((278,92,445,285),185,355,fill=(37,31,27,255));d.polygon([(300,205),(420,205),(398,310),(360,335),(320,305)],fill=(50,38,30,240));
    d.ellipse((325,194,335,202),fill=(196,204,190,255));d.ellipse((390,194,400,202),fill=(196,204,190,255));d.line((362,198,355,235),fill=(100,70,55,255),width=3)
    d.rectangle((235,590,485,628),fill=(58,43,31,255),outline=(169,126,69,255),width=3);d.rounded_rectangle((340,585,385,635),8,fill=(93,72,45,255),outline=GOLD,width=3)
    d.line((535,620,650,160),fill=(210,196,151,255),width=10); d.line((506,545,565,570),fill=(151,107,60,255),width=13)
    vg=Image.new('L',(W,H),0);vd=ImageDraw.Draw(vg);vd.rectangle((0,0,W,H),fill=145);vd.ellipse((80,20,W-80,H+120),fill=0);vg=vg.filter(ImageFilter.GaussianBlur(70));shade=Image.new('RGBA',(W,H),(0,0,0,0));shade.putalpha(vg);im=Image.alpha_composite(im,shade)
    ImageEnhance.Contrast(im.convert('RGB')).enhance(1.08).save(OUT/'hero_einoras.png',optimize=True)

def quest():
    W,H=1000,500; im=gradient((W,H),(14,30,38),(3,9,13)); d=ImageDraw.Draw(im)
    for x in [115,240,760,885]:
        d.rectangle((x,90,x+65,430),fill=(46,48,46,255),outline=(127,108,76,200),width=3);d.rectangle((x-12,72,x+77,105),fill=(64,63,57,255))
    for i in range(12):
        y=325+i*16;d.line((0,y,W,y),fill=(69,65,52,max(20,100-i*6)),width=1)
    cx,cy=515,245
    for r in [145,118,92,68,45]:d.ellipse((cx-r,cy-r,cx+r,cy+r),outline=(84,202,225,210),width=8)
    for r in range(42,2,-4):d.ellipse((cx-r,cy-r,cx+r,cy+r),fill=(104,215,232,8+int((42-r)*3)))
    for _ in range(26):
        x=R.randint(250,760);y=R.randint(350,460);d.line((x,y,x+R.randint(-25,25),y+R.randint(5,30)),fill=(116,103,74,120),width=2)
    ImageEnhance.Contrast(im.convert('RGB')).enhance(1.1).save(OUT/'quest_meridian.png',optimize=True)

world(); scene(); combat(); hero(); quest(); print('v0.8.1 premium assets generated')
