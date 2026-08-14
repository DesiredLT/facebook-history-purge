from pathlib import Path
from PIL import Image,ImageDraw,ImageFilter,ImageFont
import random,math

OUT=Path('app/src/main/res/drawable-nodpi');OUT.mkdir(parents=True,exist_ok=True);R=random.Random(6070)
def font(n,b=False):
    for p in ['/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf' if b else '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf','/usr/share/fonts/truetype/liberation2/LiberationSans-Bold.ttf']:
        try:return ImageFont.truetype(p,n)
        except:pass
    return ImageFont.load_default()
def tex(im,k=12):
    q=Image.new('RGBA',im.size,(0,0,0,0));d=ImageDraw.Draw(q)
    for _ in range(im.width*im.height//1300):
        x=R.randrange(im.width);y=R.randrange(im.height);r=R.choice([1,1,2,3]);d.ellipse((x-r,y-r,x+r,y+r),fill=R.choice([(255,236,190,R.randrange(2,k)),(10,8,5,R.randrange(2,k)),(70,110,85,R.randrange(2,k))]))
    return Image.alpha_composite(im.convert('RGBA'),q).filter(ImageFilter.GaussianBlur(.3))
def grad(size,a,b):
    im=Image.new('RGB',size);d=ImageDraw.Draw(im)
    for y in range(size[1]):
        t=y/max(1,size[1]-1);d.line((0,y,size[0],y),fill=tuple(int(a[i]*(1-t)+b[i]*t) for i in range(3)))
    return im.convert('RGBA')
def blob(cx,cy,rx,ry,n=44,j=.16):
    return [(cx+math.cos(i*2*math.pi/n)*rx*(1+R.uniform(-j,j)),cy+math.sin(i*2*math.pi/n)*ry*(1+R.uniform(-j,j))) for i in range(n)]
def tree(d,x,y,s=1):
    d.rectangle((x-2*s,y+4*s,x+2*s,y+14*s),fill=(65,46,31,255));d.ellipse((x-10*s,y-5*s,x+10*s,y+11*s),fill=(28,75+R.randrange(18),47,255));d.ellipse((x-7*s,y-11*s,x+7*s,y+4*s),fill=(46,100+R.randrange(20),60,255))
def mountain(d,x,y,s=1,fire=False):
    d.polygon([(x-18*s,y+15*s),(x,y-22*s),(x+20*s,y+15*s)],fill=(88,79,70,255) if not fire else (82,49,39,255));d.polygon([(x,y-22*s),(x+7*s,y-7*s),(x+1*s,y-9*s),(x-5*s,y-3*s),(x-8*s,y-7*s)],fill=(178,165,140,255) if not fire else (201,96,55,255))
def city(d,x,y,name,lv,major=False):
    col={3:(95,195,144,255),4:(218,186,95,255),5:(222,156,76,255),6:(222,135,73,255),7:(215,100,72,255),8:(210,64,63,255)}[lv];s=1.3 if major else 1;d.ellipse((x-12*s,y-12*s,x+12*s,y+12*s),fill=(8,15,18,235),outline=col,width=max(2,int(3*s)));d.ellipse((x-4*s,y-4*s,x+4*s,y+4*s),fill=col);f=font(18 if major else 14,True);w=d.textbbox((0,0),name,font=f)[2];bx=x+16 if x<1080 else x-w-18;d.rounded_rectangle((bx-5,y-12,bx+w+5,y+10),5,fill=(7,14,18,205));d.text((bx,y-10),name,font=f,fill=(241,231,203,255))

def world():
    W,H=1448,1086;im=grad((W,H),(13,43,58),(18,60,75));d=ImageDraw.Draw(im)
    for y in range(35,H,48):d.line([(x,y+5*math.sin(x/75+y/90)) for x in range(-20,W+20,25)],fill=(82,128,142,45),width=2)
    A=blob(390,430,330,310);D=blob(1035,350,330,270);L=blob(890,790,420,235)
    for p,c,e in [(A,(86,119,76,255),(53,80,60,255)),(D,(119,92,70,255),(76,61,52,255)),(L,(87,126,75,255),(52,88,57,255))]:d.polygon(p,fill=c);d.line(p+[p[0]],fill=e,width=8)
    d.ellipse((20,525,250,760),fill=(126,106,62,235));d.ellipse((470,645,790,1035),fill=(55,103,74,160));d.ellipse((810,620,1360,1010),fill=(55,116,67,105))
    for pts in [[(510,180),(500,260),(470,345),(440,450),(425,560),(470,675)],[(905,480),(880,565),(850,640),(790,710),(720,780),(655,950)],[(1130,165),(1080,255),(1050,350),(1020,465)]]:d.line(pts,fill=(24,67,81,180),width=12);d.line(pts,fill=(67,150,174,220),width=6)
    routes=[((388,405),(270,183)),((388,405),(505,180)),((388,405),(620,326)),((388,405),(525,512)),((388,405),(705,513)),((705,513),(925,275)),((925,275),(1180,365)),((1180,365),(1175,90)),((705,513),(967,600)),((967,600),(770,741)),((770,741),(617,920)),((770,741),(1190,790)),((110,613),(388,405))]
    for a,b in routes:d.line((a,b),fill=(53,43,31,120),width=7);d.line((a,b),fill=(202,170,93,205),width=3)
    for _ in range(160):
        if R.random()<.55:x,y=R.randint(80,660),R.randint(170,650)
        else:x,y=R.randint(560,1280),R.randint(650,940)
        tree(d,x,y,R.choice([.7,.8,1]))
    for i in range(45):mountain(d,860+i*11,R.randint(135,390),R.choice([.7,.85,1]),20<i<31)
    for i in range(24):mountain(d,120+i*18,R.randint(150,300),R.choice([.6,.8,.9]))
    for r in range(6):d.ellipse((1130-r*10,750-r*8,1230+r*10,845+r*8),outline=(127,176,93,160-r*18),width=3)
    for x,y,t in [(230,525,'ASTERRA'),(1040,470,'DRAVENN'),(840,850,'LYSARA')]:d.text((x,y),t,font=font(42,True),fill=(236,220,177,125))
    cs=[(388,405,'Luminara',3,1),(270,183,'Asterio Karūna',4,0),(505,180,'Stiklo Giria',5,0),(620,326,'Veyrhold',3,0),(705,513,'Aureliono Pakraštys',4,0),(525,512,'Žvaigždėkritos Skliautas',7,0),(110,613,'Tuščiavidurė Smailė',8,0),(925,275,'Pelenų Karūnos Citadelė',7,0),(1180,365,'Kharad Vorn',5,0),(1175,90,'Drakono Pabudimo Viršūnės',8,0),(967,600,'Safyro Platybės',7,0),(770,741,'Amžinojo Šaltinio Slėnis',4,0),(617,920,'Šventųjų Pelkynas',6,0),(1190,790,'Žaliasis Labirintas',8,0)]
    for x,y,n,l,m in cs:city(d,x,y,n,l,bool(m))
    tex(im,14).convert('RGB').save(OUT/'world_map_v060.png',optimize=True)
def scene():
    W,H=900,375;im=grad((W,H),(38,52,71),(159,96,55));d=ImageDraw.Draw(im);d.ellipse((690,30,770,110),fill=(255,212,130,170));d.rectangle((0,245,W,H),fill=(26,64,79,255))
    for i in range(28):x=20+i*34;bh=R.randint(55,165);d.rectangle((x,245-bh,x+25,245),fill=R.choice([(45,57,64,255),(56,66,70,255),(67,70,69,255)]));d.polygon([(x-4,245-bh),(x+12,245-bh-R.randint(20,48)),(x+29,245-bh)],fill=(92,82,70,255))
    gx,gy=610,170
    for r in range(78,25,-10):d.ellipse((gx-r,gy-r*.55,gx+r,gy+r*.55),outline=(94,203,220,max(40,210-r*2)),width=4)
    d.ellipse((gx-18,gy-18,gx+18,gy+18),fill=(235,203,116,230));d.polygon([(0,310),(380,270),(900,310),(900,375),(0,375)],fill=(44,38,34,255));tex(im,10).convert('RGB').save(OUT/'scene_luminara.png',optimize=True)
def combat():
    W,H=900,540;im=grad((W,H),(20,42,39),(6,14,18));d=ImageDraw.Draw(im)
    for layer in range(4):
        for i in range(22):x=i*45+R.randint(-20,20);y=100+layer*30;hh=R.randint(200,430);d.rectangle((x,y,x+12,y+hh),fill=(22+layer*5,55+layer*8,45+layer*4,255));d.ellipse((x-35,y-25,x+45,y+40),fill=(30+layer*5,72+layer*8,52+layer*5,220))
    for x,y,flip,col in [(300,365,0,(222,186,100,255)),(610,330,1,(210,79,70,255))]:d.ellipse((x-14,y-92,x+14,y-64),fill=(30,28,27,255));d.polygon([(x-25,y-58),(x+20,y-60),(x+30,y+10),(x-20,y+12)],fill=(38,37,35,255));d.line((x,y-45,x+(-55 if flip else 55),y-5),fill=col,width=5)
    for r in range(8,90,13):d.ellipse((455-r,285-r,455+r,285+r),outline=(133,114,225,max(40,190-r)),width=4)
    tex(im,12).convert('RGB').save(OUT/'combat_forest.png',optimize=True)
def hero():
    W,H=540,720;im=grad((W,H),(26,42,49),(4,10,14));d=ImageDraw.Draw(im)
    for r in range(190,40,-22):d.ellipse((W//2-r,H//2-r,W//2+r,H//2+r),outline=(214,182,107,max(15,120-r//2)),width=3)
    d.ellipse((250,92,290,138),fill=(45,43,42,255));d.polygon([(210,145),(330,145),(382,610),(160,610)],fill=(31,36,40,255));d.polygon([(224,160),(316,160),(338,400),(202,400)],fill=(65,70,72,255))
    for y in [190,240,290,340]:d.rounded_rectangle((214,y,326,y+34),8,fill=(83,87,86,255),outline=(197,166,96,220),width=2)
    d.line((392,360,460,105),fill=(222,192,113,255),width=7);d.line((375,350,413,370),fill=(172,133,75,255),width=8);tex(im,8).convert('RGB').save(OUT/'hero_einoras.png',optimize=True)
def npc():
    W,H=480,675;im=grad((W,H),(52,62,67),(9,15,19));d=ImageDraw.Draw(im);d.polygon([(80,675),(118,420),(360,420),(410,675)],fill=(44,55,62,255));d.rectangle((205,355,275,455),fill=(186,147,122,255));d.ellipse((145,105,335,390),fill=(197,158,132,255));hair=(211,213,207,255);d.ellipse((125,78,350,300),fill=hair);d.polygon([(130,160),(168,420),(202,260),(220,420),(260,255),(294,420),(346,150),(335,80)],fill=hair);d.ellipse((170,130,315,380),fill=(197,158,132,255));d.ellipse((204,236,214,246),fill=(60,72,72,255));d.ellipse((278,236,288,246),fill=(60,72,72,255));d.arc((215,295,282,338),0,180,fill=(110,73,68,255),width=3);d.ellipse((228,480,252,504),fill=(71,193,183,255),outline=(222,188,100,255),width=3);tex(im,7).convert('RGB').save(OUT/'npc_lyra.png',optimize=True)
def quest():
    W,H=720,416;im=grad((W,H),(15,31,40),(4,10,14));d=ImageDraw.Draw(im);cx,cy=520,210
    for r in range(140,35,-18):d.ellipse((cx-r,cy-r,cx+r,cy+r),outline=(104,197,213,max(40,210-r)),width=5)
    for x,y,w,h in [(380,70,45,250),(610,80,42,240),(410,60,70,38),(560,68,70,36)]:d.rectangle((x,y,x+w,y+h),fill=(72,75,71,255),outline=(144,132,107,255),width=2)
    d.polygon([(0,315),(720,285),(720,416),(0,416)],fill=(25,29,29,255));tex(im,9).convert('RGB').save(OUT/'quest_meridian.png',optimize=True)
world();scene();combat();hero();npc();quest();print('v0.6.0 assets generated')
