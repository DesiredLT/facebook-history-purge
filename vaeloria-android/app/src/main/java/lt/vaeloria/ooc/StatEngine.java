package lt.vaeloria.ooc;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class StatEngine {
    private static final SecureRandom RNG = new SecureRandom();

    public static final class Check {
        public String primary;
        public String secondary;
        public int primaryValue = 100;
        public int secondaryValue = 100;
        public int base = 100;
        public int roll;
        public int conditionModifier;
        public int equipmentModifier;
        public int abilityModifier;
        public int difficulty;
        public int total;
        public String outcome;
        public String reason;

        public String prompt() {
            return "PRIVALOMA SAVYBĖS PATIKRA (apskaičiuota telefone; nekeisk rezultato):\n"+
                    "Pagrindinė savybė: "+primary+" = "+primaryValue+"/100.\n"+
                    "Pagalbinė savybė: "+secondary+" = "+secondaryValue+"/100.\n"+
                    "Savybių pagrindas (75% pagrindinė + 25% pagalbinė): "+base+".\n"+
                    "Patikros metimas: "+roll+"/100.\n"+
                    "Būsenos modifikatorius: "+signed(conditionModifier)+". Įrangos modifikatorius: "+signed(equipmentModifier)+". Gebėjimų modifikatorius: "+signed(abilityModifier)+".\n"+
                    "Galutinis balas: "+total+". Sunkumas: "+difficulty+".\n"+
                    "Rezultatas: "+outcome+". Priežastis: "+reason+".\n"+
                    "Scenos pasekmės PRIVALO atitikti šį rezultatą. Sėkmė nereiškia, kad pasaulio veikėjai praranda valią ar kad gaunamas nepagrįstas atlygis.";
        }

        public String compact() {
            StringBuilder b=new StringBuilder("🎲 ").append(primary).append(" ").append(primaryValue)
                    .append(" + ").append(secondary).append(" ").append(secondaryValue)
                    .append(" → pagrindas ").append(base).append(" + metimas ").append(roll);
            if(conditionModifier!=0)b.append(" ").append(signed(conditionModifier)).append(" būsena");
            if(equipmentModifier!=0)b.append(" ").append(signed(equipmentModifier)).append(" įranga");
            if(abilityModifier!=0)b.append(" ").append(signed(abilityModifier)).append(" gebėjimai");
            b.append(" = ").append(total).append(" prieš ").append(difficulty).append(" → ").append(outcome);
            return b.toString();
        }
    }

    public static Check resolve(GameState s, String action, String equipped, List<String[]> abilities, Map<String,Integer> stats) {
        String a = norm(action);
        Check c = new Check();
        chooseStats(c,a);
        c.primaryValue = value(stats,c.primary);
        c.secondaryValue = value(stats,c.secondary);
        c.base = Math.round(c.primaryValue * 0.75f + c.secondaryValue * 0.25f);
        c.roll = 1 + RNG.nextInt(100);
        c.difficulty = difficulty(a,s);
        c.conditionModifier = conditionModifier(c.primary,s);
        c.equipmentModifier = equipmentModifier(c.primary,a,equipped);
        c.abilityModifier = abilityModifier(c.primary,a,abilities);
        c.total = c.base + c.roll + c.conditionModifier + c.equipmentModifier + c.abilityModifier;
        int margin = c.total - c.difficulty;
        if(c.roll==100 || margin>=45)c.outcome="išskirtinė sėkmė";
        else if(margin>=0)c.outcome="sėkmė";
        else if(margin>=-20)c.outcome="dalinė sėkmė";
        else if(c.roll==1 || margin<=-45)c.outcome="rimta nesėkmė";
        else c.outcome="nesėkmė";
        c.reason = difficultyReason(a,s);
        return c;
    }

    private static int value(Map<String,Integer> stats,String name){
        if(stats==null)return 100;
        return Math.max(0,Math.min(100,stats.getOrDefault(name,100)));
    }

    private static void chooseStats(Check c,String a){
        // MAGIJA IR RELIKVIJOS
        if(has(a,"relikv","rezonans","artefakt")){pick(c,"Relikvijų rezonansas","Magijos jutimas");return;}
        if(has(a,"meridian","erdv","kelionės vart","teleport","perkel")){pick(c,"Erdvinė magija","Relikvijų rezonansas");return;}
        if(has(a,"laiko mag","chron","laiko versij","praeitį","ateitį")){pick(c,"Laiko magija","Burtų stabilumas");return;}
        if(has(a,"ardyti burt","nutraukti burt","išsklaid","panaikinti užkeik")){pick(c,"Užkeikimų ardymas","Magijos jutimas");return;}
        if(has(a,"gyd","užgyd","atkurti kūną")){pick(c,"Gydomoji magija","Manos kontrolė");return;}
        if(has(a,"barjer","skydą mag","apsaugos burt","apsauginę mag")){pick(c,"Apsauginė magija","Burtų stabilumas");return;}
        if(has(a,"atsispirti mag","atlaikyti burt","maginei įtak")){pick(c,"Atsparumas magijai","Valia");return;}
        if(has(a,"pajausti mag","aptikti mag","magijos pėdsak")){pick(c,"Magijos jutimas","Pastabumas");return;}
        if(has(a,"transmut","paversti medžiag","keisti medžiag")){pick(c,"Transmutacija","Burtų tikslumas");return;}
        if(has(a,"ugn","led","žaib","vandens burt","oro burt","žemės burt","element")){pick(c,"Elementų valdymas","Burtų galia");return;}
        if(has(a,"labai galingą burt","maksimalią galią","sustiprinti burt")){pick(c,"Burtų galia","Manos talpa");return;}
        if(has(a,"tikslų burt","tiksliai burti","taiklų burt")){pick(c,"Burtų tikslumas","Manos kontrolė");return;}
        if(has(a,"greitai burti","žaibiškai burti","skubų burt")){pick(c,"Burtų greitis","Manos kontrolė");return;}
        if(has(a,"išlaikyti burt","stabilizuoti burt","nenutraukti burt")){pick(c,"Burtų stabilumas","Susikaupimas");return;}
        if(has(a,"taupyti man","efektyviai burti","mažiau manos")){pick(c,"Burtų efektyvumas","Manos kontrolė");return;}
        if(has(a,"atkurti man","regeneruoti man","pailsėti man")){pick(c,"Manos atkūrimas","Susikaupimas");return;}
        if(has(a,"sukaupti man","didelį manos kiek","manos rezerv")){pick(c,"Manos talpa","Manos kontrolė");return;}
        if(has(a,"burti","magij","mana","runa","užkeik")){pick(c,"Manos kontrolė","Burtų tikslumas");return;}

        // SOCIALINĖS IR PRAKTINĖS
        if(has(a,"įtik","įkalb","perkalb")){pick(c,"Įtikinėjimas","Charizma");return;}
        if(has(a,"derėt","kainą","sąlyg","sandor")){pick(c,"Derybos","Žmonių perpratimas");return;}
        if(has(a,"diplomat","taiką","tarp frakc","oficialiai tartis")){pick(c,"Diplomatija","Derybos");return;}
        if(has(a,"vadov","įsak","komand","suburti")){pick(c,"Vadovavimas","Charizma");return;}
        if(has(a,"užjaust","suprasti jaus","nuraminti","empat")){pick(c,"Empatija","Žmonių perpratimas");return;}
        if(has(a,"perprasti","nuspėti žmog","skaityti žmog","motyv")){pick(c,"Žmonių perpratimas","Empatija");return;}
        if(has(a,"ar meluoja","atpažinti mel","demaskuoti apga","patikrinti nuošird")){pick(c,"Apgaulės atpažinimas","Žmonių perpratimas");return;}
        if(has(a,"mel","apga","apsimest","suklaid")){pick(c,"Apgaulė","Charizma");return;}
        if(has(a,"gras","baugin","įbaug")){pick(c,"Bauginimas","Charizma");return;}
        if(has(a,"padaryti įspūd","sužav","charizm")){pick(c,"Charizma","Empatija");return;}
        if(has(a,"pagaminti","sukalti","sukonstruoti","amat","pataisyti daikt")){pick(c,"Amatų meistriškumas","Žinių pritaikymas");return;}
        if(has(a,"pritaikyti žini","panaudoti žini","praktinis sprend")){pick(c,"Žinių pritaikymas","Intelektas");return;}

        // PROTINĖS
        if(has(a,"išmok","mokytis","perprasti technik")){pick(c,"Mokymosi greitis","Atmintis");return;}
        if(has(a,"prisim","atsim","atkurti prisimin")){pick(c,"Atmintis","Intelektas");return;}
        if(has(a,"logiškai","deduk","išspręsti galvosūk","login")){pick(c,"Loginis mąstymas","Analitinis mąstymas");return;}
        if(has(a,"analiz","patikr","palygin","ištirti","tirti")){pick(c,"Analitinis mąstymas","Pastabumas");return;}
        if(has(a,"sugalvoti","improvizu","kūryb","netradicin")){pick(c,"Kūrybiškumas","Intelektas");return;}
        if(has(a,"susikaupt","koncentru","nekreipti dėmesio")){pick(c,"Susikaupimas","Valia");return;}
        if(has(a,"atsispirti valios","ištverti skausm","nepasiduoti","priversti save")){pick(c,"Valia","Psichologinis atsparumas");return;}
        if(has(a,"baim","panik","siaub","psichologinį spaud")){pick(c,"Psichologinis atsparumas","Valia");return;}
        if(has(a,"greitai nuspręsti","akimirksniu nuspręsti","staigus sprend")){pick(c,"Sprendimų greitis","Reakcijos greitis");return;}
        if(has(a,"planu","parengti plan","numatyti žingsn")){pick(c,"Planavimas","Strateginis mąstymas");return;}
        if(has(a,"strateg","ilgalaik","kampanij","didelio masto plan")){pick(c,"Strateginis mąstymas","Planavimas");return;}
        if(has(a,"suprasti","išsiaiškinti princip","intelekt")){pick(c,"Intelektas","Loginis mąstymas");return;}

        // JUTIMAI IR IŠGYVENIMAS
        if(has(a,"tolumoje","įžiūr","pamatyti","regėj")){pick(c,"Regėjimas","Pastabumas");return;}
        if(has(a,"klausyt","išgirst","gars","triukšm")){pick(c,"Klausa","Pastabumas");return;}
        if(has(a,"užuosti","kvap","uosl")){pick(c,"Uoslė","Sekimas");return;}
        if(has(a,"apčiuop","paliesti","lytėj","tekstūr")){pick(c,"Lytėjimo jautrumas","Pastabumas");return;}
        if(has(a,"pavoj","nujausti grėsm","pasala")){pick(c,"Pavojaus nuojauta","Kovinė nuojauta");return;}
        if(has(a,"pėdsak","sekt","atsekti")){pick(c,"Sekimas","Orientavimasis vietovėje");return;}
        if(has(a,"orient","rasti kelią","nepasiklysti","žemėlapi")){pick(c,"Orientavimasis vietovėje","Erdvinė orientacija");return;}
        if(has(a,"išgyvent","stovykl","rasti maisto","laukinėje gamtoje","prieglobst")){pick(c,"Išgyvenimas laukinėje gamtoje","Žinių pritaikymas");return;}
        if(has(a,"slėp","sėlin","nepasteb","tyliai")){pick(c,"Slėpimasis","Judesių tikslumas");return;}
        if(has(a,"apsidair","pasteb","iešk","apžiūr")){pick(c,"Pastabumas","Regėjimas");return;}

        // KOVA
        if(has(a,"be ginklo","kumšč","spyr","beginkl")){pick(c,"Beginklė kova","Smūgiavimo technika");return;}
        if(has(a,"smūg","smog","spirti")){pick(c,"Smūgiavimo technika","Kovinis laiko parinkimas");return;}
        if(has(a,"imtyn","pargriauti","klinč","grum","numesti")){pick(c,"Imtynės","Kūno kontrolė");return;}
        if(has(a,"parter","ant žemės","laužimą","smaug")){pick(c,"Kova parteryje","Imtynės");return;}
        if(has(a,"bet kokiu ginklu","nepažįstamą ginkl","ginklo valdym")){pick(c,"Ginklų valdymas","Atakos tikslumas");return;}
        if(has(a,"kard","ašmen","pjaut kardu")){pick(c,"Kardo meistriškumas","Atakos tikslumas");return;}
        if(has(a,"durkl","peiliu dur","trumpais ašmen")){pick(c,"Durklų meistriškumas","Atakos tikslumas");return;}
        if(has(a,"iet","smaigu gink","ilgu kotu")){pick(c,"Ieties meistriškumas","Kovinis laiko parinkimas");return;}
        if(has(a,"lank","strėl","šaudyti iš lanko")){pick(c,"Lanko meistriškumas","Atakos tikslumas");return;}
        if(has(a,"skydu","skydą","skydo")){pick(c,"Skydo valdymas","Gynyba");return;}
        if(has(a,"gint","bloku","pariru","apsiginti")){pick(c,"Gynyba","Refleksai");return;}
        if(has(a,"taikliai ataku","tiksliai smog","silpną vietą")){pick(c,"Atakos tikslumas","Kovinė nuojauta");return;}
        if(has(a,"tinkamu moment","laiku ataku","kontratak")){pick(c,"Kovinis laiko parinkimas","Reakcijos greitis");return;}
        if(has(a,"nujausti kov","skaityti kov","priešininko judes")){pick(c,"Kovinė nuojauta","Pavojaus nuojauta");return;}
        if(has(a,"kelis prieš","apsupt","daugybę prieš")){pick(c,"Kelių priešininkų kontrolė","Taktinis prisitaikymas");return;}
        if(has(a,"prisitaikyti kov","keisti taktik","netikėta taktika")){pick(c,"Taktinis prisitaikymas","Kovinė nuojauta");return;}
        if(has(a,"pulti","ataku","kovoti")){pick(c,"Kovinė nuojauta","Kovinis laiko parinkimas");return;}

        // JUDĖJIMAS IR KŪNAS
        if(has(a,"staiga sureagu","reakc","netikėtai")){pick(c,"Reakcijos greitis","Refleksai");return;}
        if(has(a,"išsisuk","veng","atšok","išveng")){pick(c,"Refleksai","Vikrumas");return;}
        if(has(a,"kojų darb","poziciją kovoje","judėti aplink prieš")){pick(c,"Kojų darbas","Krypties keitimo greitis");return;}
        if(has(a,"šok","peršok")){pick(c,"Šuolio galia","Pusiausvyra");return;}
        if(has(a,"krist","nusileisti","kritim")){pick(c,"Kritimo kontrolė","Kūno kontrolė");return;}
        if(has(a,"erdviškai orient","ore orient","aukštyn žemyn")){pick(c,"Erdvinė orientacija","Koordinacija");return;}
        if(has(a,"tiksliai jud","siauru taku","atsargiai ženg")){pick(c,"Judesių tikslumas","Pusiausvyra");return;}
        if(has(a,"staigiai keisti krypt","zigzag","apsisukti bėgant")){pick(c,"Krypties keitimo greitis","Vikrumas");return;}
        if(has(a,"plauk")){pick(c,"Plaukimas","Širdies ir kvėpavimo ištvermė");return;}
        if(has(a,"lip","kopti")){pick(c,"Laipiojimas","Suėmimo jėga");return;}
        if(has(a,"sprint","įsibėg","vytis","sprukt","pabėg")){pick(c,"Greitis","Pagreitis");return;}
        if(has(a,"ilgai bėg","maraton","ilgą kelią bėg")){pick(c,"Širdies ir kvėpavimo ištvermė","Raumenų ištvermė");return;}
        if(has(a,"ilgai laikyti","daug kartų kelti","raumenų ištverm")){pick(c,"Raumenų ištvermė","Jėga");return;}
        if(has(a,"lauž","kelti","stum","plėš","jėga")){pick(c,"Jėga","Sprogstamoji jėga");return;}
        if(has(a,"sprogtamą jėg","staigiai išplėšti","vienu šuoliu išjud")){pick(c,"Sprogstamoji jėga","Jėga");return;}
        if(has(a,"vikriai","akrobat","manevr")){pick(c,"Vikrumas","Koordinacija");return;}
        if(has(a,"lankst","prasisprausti","išsilaisvinti iš siaur")){pick(c,"Lankstumas","Kūno kontrolė");return;}
        if(has(a,"balansu","išlaikyti pusiaus","briauna")){pick(c,"Pusiausvyra","Kūno kontrolė");return;}
        if(has(a,"suderinti jud","koordin","sudėtingą judesių sek")){pick(c,"Koordinacija","Judesių tikslumas");return;}
        if(has(a,"kontroliuoti kūną","pozą","kūno kontrol")){pick(c,"Kūno kontrolė","Koordinacija");return;}
        if(has(a,"suimti","griebti","išlaikyti rankomis")){pick(c,"Suėmimo jėga","Raumenų ištvermė");return;}
        if(has(a,"atlaikyti smūg","kaul","nesulūžti")){pick(c,"Kaulų tvirtumas","Atsparumas traumoms");return;}
        if(has(a,"atsilaikyti prieš traum","nesusižeisti","traumos rizik")){pick(c,"Atsparumas traumoms","Kaulų tvirtumas");return;}

        pick(c,"Sprendimų greitis","Kovinė nuojauta");
    }

    private static void pick(Check c,String primary,String secondary){c.primary=primary;c.secondary=secondary;}

    private static int difficulty(String a,GameState s){
        int d=125;
        if(has(a,"apžiūr","klausyt","stebėt","perskaity","paklausti"))d=105;
        if(has(a,"keliaut","eiti į","vykti į"))d=115;
        if(has(a,"derėt","įtik","apga","slėp","sekt","lauž","šok","lip","plauk"))d=Math.max(d,135);
        if(has(a,"pulti","ataku","kovoti","išsisuk","gint","imtyn","kard"))d=Math.max(d,145);
        if(has(a,"magij","burti","runa","užkeik","erdv","laik","rezonans"))d=Math.max(d,150);
        if(has(a,"meridian","nežinom","anomal","priežasting","nulin","laiko versij"))d=Math.max(d,175);
        if(has(a,"sunaikinti","nužudyti","vienu smūgiu","akimirksniu"))d=Math.max(d,190);
        if(s.combatActive)d+=10;
        return Math.min(210,d);
    }

    private static String difficultyReason(String a,GameState s){
        if(has(a,"meridian","nežinom","anomal","priežasting","nulin"))return "veiksmas liečia ne iki galo suprastą pasaulio taisyklę";
        if(s.combatActive)return "aktyvus priešininkas gali priešintis ir keisti situaciją";
        if(has(a,"magij","burti","erdv","laik","rezonans"))return "maginis veiksmas reikalauja kontrolės ir stabilumo";
        if(has(a,"derėt","įtik","apga","baugin","diplomat"))return "kitas veikėjas turi savus interesus ir valią";
        return "įprasta rizikingo veiksmo patikra";
    }

    private static int conditionModifier(String stat,GameState s){
        int m=0;
        if(isPhysical(stat)){
            if(s.hp*100/Math.max(1,s.hpMax)<50)m-=15;
            else if(s.hp*100/Math.max(1,s.hpMax)<75)m-=7;
            if(s.stamina*100/Math.max(1,s.staminaMax)<25)m-=20;
            else if(s.stamina*100/Math.max(1,s.staminaMax)<50)m-=10;
        }
        if(isMagic(stat)){
            if(s.manaMax>0 && s.mana*100/Math.max(1,s.manaMax)<20)m-=20;
            else if(s.manaMax>0 && s.mana*100/Math.max(1,s.manaMax)<50)m-=10;
            if(stat.equals("Relikvijų rezonansas") && s.aeonic*100/Math.max(1,s.aeonicMax)<20)m-=10;
        }
        return m;
    }

    private static int equipmentModifier(String stat,String a,String equipped){
        String e=norm(equipped);int m=0;
        if((stat.contains("Kardo")||stat.contains("Atakos")||stat.contains("Kovinė"))&&e.contains("asteriono"))m+=10;
        if((stat.equals("Gynyba")||isPhysical(stat))&&e.contains("septynsluoksn"))m+=6;
        if((stat.contains("Erdvinė")||a.contains("keliaut"))&&e.contains("kelių klost"))m+=8;
        if(stat.equals("Relikvijų rezonansas")&&e.contains("rezonanso signet"))m+=12;
        if((stat.equals("Analitinis mąstymas")||stat.equals("Magijos jutimas"))&&e.contains("nulinio stiklo"))m+=10;
        if((stat.equals("Erdvinė magija")||a.contains("meridian"))&&e.contains("meridiano rakt"))m+=12;
        return Math.min(25,m);
    }

    private static int abilityModifier(String stat,String a,List<String[]> abilities){
        int m=0;
        for(String[] ab:abilities){String n=norm(ab[0]);
            if(stat.equals("Gynyba")&&n.contains("eoninis bastion"))m=Math.max(m,10);
            if((stat.equals("Refleksai")||stat.equals("Vikrumas"))&&n.contains("erdvinis išsisuk"))m=Math.max(m,10);
            if((stat.equals("Analitinis mąstymas")||stat.equals("Magijos jutimas"))&&n.contains("nežinomų taisyklių"))m=Math.max(m,8);
            if(stat.equals("Relikvijų rezonansas")&&n.contains("relikvijų simbioz"))m=Math.max(m,12);
            if(stat.equals("Laiko magija")&&n.contains("laiko paralakso"))m=Math.max(m,10);
        }
        return m;
    }

    private static boolean isPhysical(String s){return has(norm(s),"jėg","ištverm","greit","pagreit","vikrum","lankst","pusiaus","koordin","kūno","suėm","kaul","traum","šuolio","kritimo","judesi","kojų","plaukim","laipioj","reakc","refleks","kova","smūgi","imtyn","parter","ginkl","kard","durkl","ieties","lanko","skydo","gynyb","atakos","kovin");}
    private static boolean isMagic(String s){return has(norm(s),"mana","burt","magij","element","gydom","erdvin","laiko","transmut","užkeik","relikv","rezonans");}
    private static boolean has(String text,String... needles){for(String n:needles)if(text.contains(n))return true;return false;}
    private static String norm(String s){return s==null?"":s.toLowerCase(Locale.forLanguageTag("lt-LT"));}
    private static String signed(int n){return n>=0?"+"+n:String.valueOf(n);}
    private StatEngine(){}
}
