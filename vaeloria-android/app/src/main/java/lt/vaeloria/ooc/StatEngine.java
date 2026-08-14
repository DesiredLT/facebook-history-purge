package lt.vaeloria.ooc;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

public final class StatEngine {
    private static final SecureRandom RNG = new SecureRandom();

    public static final class Check {
        public String primary;
        public String secondary;
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
                    "Pagrindinė savybė: "+primary+" = "+base+"/100.\n"+
                    "Pagalbinė savybė: "+secondary+".\n"+
                    "Patikros metimas: "+roll+"/100.\n"+
                    "Būsenos modifikatorius: "+signed(conditionModifier)+". Įrangos modifikatorius: "+signed(equipmentModifier)+". Gebėjimų modifikatorius: "+signed(abilityModifier)+".\n"+
                    "Galutinis balas: "+total+". Sunkumas: "+difficulty+".\n"+
                    "Rezultatas: "+outcome+". Priežastis: "+reason+".\n"+
                    "Scenos pasekmės PRIVALO atitikti šį rezultatą. Sėkmė nereiškia, kad pasaulio veikėjai praranda valią ar kad gaunamas nepagrįstas atlygis.";
        }

        public String compact() {
            StringBuilder b=new StringBuilder("🎲 ").append(primary).append(" ").append(base)
                    .append(" + metimas ").append(roll);
            if(conditionModifier!=0)b.append(" ").append(signed(conditionModifier)).append(" būsena");
            if(equipmentModifier!=0)b.append(" ").append(signed(equipmentModifier)).append(" įranga");
            if(abilityModifier!=0)b.append(" ").append(signed(abilityModifier)).append(" gebėjimai");
            b.append(" = ").append(total).append(" prieš ").append(difficulty).append(" → ").append(outcome);
            return b.toString();
        }
    }

    public static Check resolve(GameState s, String action, String equipped, List<String[]> abilities) {
        String a = norm(action);
        Check c = new Check();
        chooseStats(c,a);
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

    private static void chooseStats(Check c,String a){
        if(has(a,"įtik","derėt","kalb","įkalb","diplomat","susitart")){c.primary="Įtikinėjimas";c.secondary="Derybos";return;}
        if(has(a,"mel","apga","apsimest","suklaid")){c.primary="Apgaulė";c.secondary="Žmonių perpratimas";return;}
        if(has(a,"gras","baugin","įbaug")){c.primary="Bauginimas";c.secondary="Charizma";return;}
        if(has(a,"slėp","sėlin","nepasteb","tyliai")){c.primary="Slėpimasis";c.secondary="Pastabumas";return;}
        if(has(a,"sekt","pėdsak","surasti kelią","orient")){c.primary="Sekimas";c.secondary="Orientavimasis vietovėje";return;}
        if(has(a,"pasteb","apžiūr","iešk","tirti","ištirti","analiz","patikr","palygin")){c.primary="Analitinis mąstymas";c.secondary="Pastabumas";return;}
        if(has(a,"atsim","prisim","žini","istor","atpaž")){c.primary="Atmintis";c.secondary="Žinių pritaikymas";return;}
        if(has(a,"planu","strateg","spąst","taktik")){c.primary="Strateginis mąstymas";c.secondary="Planavimas";return;}
        if(has(a,"lauž","kelti","stum","plėš","smūgiuoti jėga")){c.primary="Jėga";c.secondary="Sprogstamoji jėga";return;}
        if(has(a,"bėg","vytis","sprukt","pabėg")){c.primary="Greitis";c.secondary="Ištvermė";return;}
        if(has(a,"šok","peršok")){c.primary="Šuolio galia";c.secondary="Pusiausvyra";return;}
        if(has(a,"lip","kopti")){c.primary="Laipiojimas";c.secondary="Suėmimo jėga";return;}
        if(has(a,"plauk")){c.primary="Plaukimas";c.secondary="Širdies ir kvėpavimo ištvermė";return;}
        if(has(a,"išsisuk","veng","atšok","išveng")){c.primary="Refleksai";c.secondary="Vikrumas";return;}
        if(has(a,"imtyn","parter","grum","sulaik")){c.primary="Imtynės";c.secondary="Kūno kontrolė";return;}
        if(has(a,"kard","ašmen","ginklu","pjaut","durti")){c.primary="Kardo meistriškumas";c.secondary="Atakos tikslumas";return;}
        if(has(a,"smūg","pulti","ataku","kovoti")){c.primary="Kovinė nuojauta";c.secondary="Kovinis laiko parinkimas";return;}
        if(has(a,"gint","bloku","pariru")){c.primary="Gynyba";c.secondary="Refleksai";return;}
        if(has(a,"gyd","atkurti kūną")){c.primary="Gydomoji magija";c.secondary="Manos kontrolė";return;}
        if(has(a,"laik","chron","praeit","ateit")){c.primary="Laiko magija";c.secondary="Burtų stabilumas";return;}
        if(has(a,"erdv","vart","meridian","teleport","perkel")){c.primary="Erdvinė magija";c.secondary="Relikvijų rezonansas";return;}
        if(has(a,"ardyti burt","nutraukti burt","išsklaid")){c.primary="Užkeikimų ardymas";c.secondary="Magijos jutimas";return;}
        if(has(a,"burti","magij","mana","runa","užkeik")){c.primary="Manos kontrolė";c.secondary="Burtų tikslumas";return;}
        if(has(a,"relikv","rezonans","artefakt")){c.primary="Relikvijų rezonansas";c.secondary="Magijos jutimas";return;}
        if(has(a,"vadov","įsak","komand")){c.primary="Vadovavimas";c.secondary="Charizma";return;}
        c.primary="Kovinė nuojauta";c.secondary="Sprendimų greitis";
    }

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
        if(has(a,"derėt","įtik","apga","baugin"))return "kitas veikėjas turi savus interesus ir valią";
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

    private static boolean isPhysical(String s){return has(norm(s),"jėg","ištverm","greit","vikrum","lankst","pusiaus","koordin","kūno","suėm","šuolio","kritimo","judesi","kojų","plaukim","laipioj","refleks","kova","smūgi","imtyn","parter","ginkl","kard","durkl","ieties","lanko","skydo","gynyb","atakos","kovin");}
    private static boolean isMagic(String s){return has(norm(s),"mana","burt","magij","element","gydom","erdvin","laiko","transmut","užkeik","relikv","rezonans");}
    private static boolean has(String text,String... needles){for(String n:needles)if(text.contains(n))return true;return false;}
    private static String norm(String s){return s==null?"":s.toLowerCase(Locale.forLanguageTag("lt-LT"));}
    private static String signed(int n){return n>=0?"+"+n:String.valueOf(n);}
    private StatEngine(){}
}
