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
        c.difficulty = difficulty(a,s,c.primary);
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
        c.reason = difficultyReason(a,s,c.primary);
        return c;
    }

    static String[] classifyForTest(String action){
        Check c=new Check();chooseStats(c,norm(action));return new String[]{c.primary,c.secondary};
    }

    private static int value(Map<String,Integer> stats,String name){
        if(stats==null)return 100;
        return Math.max(0,Math.min(100,stats.getOrDefault(name,100)));
    }

    private static void chooseStats(Check c,String a){
        String canonical=canonicalExact(a);
        if(canonical!=null){pick(c,canonical,defaultSecondary(canonical));return;}
        // High-priority semantic rules: action intent must beat nouns or shorter overlapping phrases.
        if(has(a,"staiga sureaguoti","sureaguoti","sureaguoju","reakcijos greitis")){pick(c,"Reakcijos greitis","Refleksai");return;}
        if(has(a,"kojų darbą","kojų darbas","naudoti kojų darbą")){pick(c,"Kojų darbas","Krypties keitimo greitis");return;}
        if(has(a,"erdviškai orientuotis","erdviškai orientuojuosi","erdvinė orientacija")){pick(c,"Erdvinė orientacija","Koordinacija");return;}
        if(has(a,"strategiškai","ilgalaikę kampaniją","ilgalaikė kampanija","strateginis mąstymas")){pick(c,"Strateginis mąstymas","Planavimas");return;}
        if(has(a,"atsispirti","atsilaikyti") && has(a,"magijai","magiją","maginė įtaka")){pick(c,"Atsparumas magijai","Valia");return;}
        if(has(a,"išsklaidyti","išsklaidau","panaikinti") && has(a,"burtą","burtus","užkeikimą")){pick(c,"Užkeikimų ardymas","Magijos jutimas");return;}
        if(has(a,"perprasti","perprantu") && has(a,"žmogų","žmogaus","motyvą","motyvus")){pick(c,"Žmonių perpratimas","Empatija");return;}
        if(has(a,"atpažinti","nustatyti","suprasti") && has(a,"meluoja","melą","apgaulę")){pick(c,"Apgaulės atpažinimas","Žmonių perpratimas");return;}
        if((has(a,"aktyvuoti","aktyvuoju","įjungti","įjungiu") && has(a,"artefaktą","artefaktas","artefakto"))){pick(c,"Relikvijų rezonansas","Magijos jutimas");return;}
        if(has(a,"suderinti","suderinu") && has(a,"judesius","judesių")){pick(c,"Koordinacija","Judesių tikslumas");return;}
        if(has(a,"išlaikyti","laikyti") && has(a,"rankomis","ranka","rankose")){pick(c,"Suėmimo jėga","Raumenų ištvermė");return;}
        if(has(a,"valdyti","valdau") && has(a,"ginklą","ginklu","ginklus")){pick(c,"Ginklų valdymas","Atakos tikslumas");return;}
        if(has(a,"suprasti","suprantu") && has(a,"principą","principo","principus")){pick(c,"Intelektas","Loginis mąstymas");return;}
        if(has(a,"parengti","sudaryti") && has(a,"planą","plano")){pick(c,"Planavimas","Strateginis mąstymas");return;}
        if(has(a,"pritaikyti","pritaikau") && has(a,"žinias","žinių")){pick(c,"Žinių pritaikymas","Intelektas");return;}
        // MAGIJA IR RELIKVIJOS. Specifinės intencijos yra aukščiau už bendrus objektų pavadinimus.
        if(has(a,"meridian","erdvinė magija","erdvinę magiją","erdvės magija","erdvę iškreipti","erdvę sulenkti","kelionės vartai","teleportuoti","teleportuoju","portalą")){pick(c,"Erdvinė magija","Relikvijų rezonansas");return;}
        if(has(a,"rezonuoti su relikvija","rezonansas","aktyvuoti relikviją","aktyvuoju relikviją","naudoti relikviją","susiderinti su artefaktu","aktyvuoti artefaktą","pajausti artefaktą")){pick(c,"Relikvijų rezonansas","Magijos jutimas");return;}
        if(has(a,"laiko magija","laiko magiją","chronomant","laiko versija","keliauti laiku","atsukti laiką","sustabdyti laiką","pažvelgti į ateitį magija")){pick(c,"Laiko magija","Burtų stabilumas");return;}
        if(has(a,"ardyti burtą","nutraukti burtą","išsklaidyti burtą","panaikinti užkeikimą")){pick(c,"Užkeikimų ardymas","Magijos jutimas");return;}
        if(has(a,"gydyti žaizdą","gydau žaizdą","užgydyti","išgydyti","atkurti kūną","atkurti sveikatą","atkurti mano sveikatą","atkurti savo sveikatą")){pick(c,"Gydomoji magija","Manos kontrolė");return;}
        if(has(a,"maginį barjerą","maginį skydą","apsaugos burtą","apsauginę magiją")){pick(c,"Apsauginė magija","Burtų stabilumas");return;}
        if(has(a,"atsispirti magijai","atlaikyti burtą","maginei įtakai")){pick(c,"Atsparumas magijai","Valia");return;}
        if(has(a,"pajausti magiją","aptikti magiją","magijos pėdsaką","magijos pėdsakus")){pick(c,"Magijos jutimas","Pastabumas");return;}
        if(has(a,"transmutuoti","transmutacija","paversti medžiagą","keisti medžiagą")){pick(c,"Transmutacija","Burtų tikslumas");return;}
        if(has(a,"valdyti ugnį","valdyti ledą","valdyti žaibą","vandens burtą","oro burtą","žemės burtą","elementų valdymas")){pick(c,"Elementų valdymas","Burtų galia");return;}
        if(has(a,"labai galingą burtą","maksimali burtų galia","sustiprinti burtą","burtų galia")){pick(c,"Burtų galia","Manos talpa");return;}
        if(has(a,"tikslų burtą","tiksliai burti","taiklų burtą","burtų tikslumas")){pick(c,"Burtų tikslumas","Manos kontrolė");return;}
        if(has(a,"greitai burti","žaibiškai burti","skubų burtą","burtų greitis")){pick(c,"Burtų greitis","Manos kontrolė");return;}
        if(has(a,"išlaikyti burtą","stabilizuoti burtą","nenutraukti burto","burtų stabilumas")){pick(c,"Burtų stabilumas","Susikaupimas");return;}
        if(has(a,"taupyti maną","efektyviai burti","mažiau manos","burtų efektyvumas")){pick(c,"Burtų efektyvumas","Manos kontrolė");return;}
        if(has(a,"atkurti maną","atkurčiau maną","regeneruoti maną","papildyti maną","manos atkūrimas")){pick(c,"Manos atkūrimas","Susikaupimas");return;}
        if(has(a,"sukaupti maną","didelį manos kiekį","manos rezervą","manos talpa")){pick(c,"Manos talpa","Manos kontrolė");return;}
        if(has(a,"naudoti magiją","naudoju magiją","panaudoti magiją","burti","runa","užkeikimas","manos kontrolė") || word(a,"mana","maną","manos")){pick(c,"Manos kontrolė","Burtų tikslumas");return;}

        // SOCIALINĖS IR PRAKTINĖS
        if(has(a,"įtikinti","įkalbėti","perkalbėti","įtikinėjimas")){pick(c,"Įtikinėjimas","Charizma");return;}
        if(has(a,"derėtis","kainą","sąlygas","sandorį","derybos")){pick(c,"Derybos","Žmonių perpratimas");return;}
        if(has(a,"diplomatija","siekti taikos","tarp frakcijų","oficialiai tartis")){pick(c,"Diplomatija","Derybos");return;}
        if(has(a,"vadovauti","įsakyti","komanduoti","suburti","vadovavimas")){pick(c,"Vadovavimas","Charizma");return;}
        if(has(a,"užjausti","suprasti jausmus","nuraminti","empatija")){pick(c,"Empatija","Žmonių perpratimas");return;}
        if(has(a,"perprasti žmogų","nuspėti žmogų","skaityti žmogų","suprasti motyvą","žmonių perpratimas")){pick(c,"Žmonių perpratimas","Empatija");return;}
        if(has(a,"ar meluoja","atpažinti melą","demaskuoti apgaulę","patikrinti nuoširdumą","apgaulės atpažinimas")){pick(c,"Apgaulės atpažinimas","Žmonių perpratimas");return;}
        if(has(a,"meluoti","meluoju","melą","melagingai","apgauti","apgaunu","apsimesti","suklaidinti","apgaulė")){pick(c,"Apgaulė","Charizma");return;}
        if(has(a,"grasinti","bauginimas","įbauginti","įbauginu")){pick(c,"Bauginimas","Charizma");return;}
        if(has(a,"padaryti įspūdį","sužavėti","charizma")){pick(c,"Charizma","Empatija");return;}
        if(has(a,"pagaminti","sukalti","sukonstruoti","amatų meistriškumas","meistrauti","pataisyti daiktą","lipdyti molį")){pick(c,"Amatų meistriškumas","Žinių pritaikymas");return;}
        if(has(a,"pritaikyti žinias","panaudoti žinias","praktinis sprendimas","žinių pritaikymas")){pick(c,"Žinių pritaikymas","Intelektas");return;}

        // PROTINĖS
        if(has(a,"išmokti","mokytis","perprasti techniką","mokymosi greitis")){pick(c,"Mokymosi greitis","Atmintis");return;}
        if(has(a,"prisiminti","prisimenu","atsiminti","atkurti prisiminimą","atmintis")){pick(c,"Atmintis","Intelektas");return;}
        if(has(a,"logiškai","dedukcija","išspręsti galvosūkį","loginis mąstymas")){pick(c,"Loginis mąstymas","Analitinis mąstymas");return;}
        if(has(a,"analizuoti","analizuoju","patikrinti","palyginti","ištirti","ištiriu","tirti","analitinis mąstymas")){pick(c,"Analitinis mąstymas","Pastabumas");return;}
        if(has(a,"sugalvoti","improvizuoti","kūrybiškai","netradicinis sprendimas","kūrybiškumas")){pick(c,"Kūrybiškumas","Intelektas");return;}
        if(has(a,"susikaupti","koncentruotis","nekreipti dėmesio","susikaupimas")){pick(c,"Susikaupimas","Valia");return;}
        if(has(a,"atsispirti valios spaudimui","ištverti skausmą","nepasiduoti","priversti save","valia")){pick(c,"Valia","Psichologinis atsparumas");return;}
        if(has(a,"baimė","panika","siaubas","psichologinį spaudimą","psichologinis atsparumas")){pick(c,"Psichologinis atsparumas","Valia");return;}
        if(has(a,"greitai nuspręsti","akimirksniu nuspręsti","staigus sprendimas","sprendimų greitis")){pick(c,"Sprendimų greitis","Reakcijos greitis");return;}
        if(has(a,"planuoti","parengti planą","numatyti žingsnius","planavimas")){pick(c,"Planavimas","Strateginis mąstymas");return;}
        if(has(a,"strategija","strategiškai","ilgalaikis planas","kampanija","didelio masto planas","strateginis mąstymas")){pick(c,"Strateginis mąstymas","Planavimas");return;}
        if(has(a,"suprasti principą","išsiaiškinti principą","intelektas")){pick(c,"Intelektas","Loginis mąstymas");return;}

        // JUTIMAI IR IŠGYVENIMAS
        if(has(a,"tolumoje","įžiūrėti","pamatyti","regėjimas")){pick(c,"Regėjimas","Pastabumas");return;}
        if(has(a,"klausytis","išgirsti","garsą","garsus","garso šaltinį","triukšmą","klausa")){pick(c,"Klausa","Pastabumas");return;}
        if(has(a,"užuosti","kvapą","uoslė")){pick(c,"Uoslė","Sekimas");return;}
        if(has(a,"apčiuopti","paliesti","lytėjimas","tekstūra","lytėjimo jautrumas")){pick(c,"Lytėjimo jautrumas","Pastabumas");return;}
        if(has(a,"pavojus","nujausti grėsmę","pasala","pavojaus nuojauta")){pick(c,"Pavojaus nuojauta","Kovinė nuojauta");return;}
        if(has(a,"pėdsakai","pėdsaką","sekti priešą","seku priešą","atsekti","sekimas")){pick(c,"Sekimas","Orientavimasis vietovėje");return;}
        if(has(a,"orientuotis","rasti kelią","nepasiklysti","žemėlapis","žemėlapį","orientavimasis vietovėje")){pick(c,"Orientavimasis vietovėje","Erdvinė orientacija");return;}
        if(has(a,"išgyventi","stovykla","rasti maisto","laukinėje gamtoje","prieglobstis","išgyvenimas laukinėje gamtoje")){pick(c,"Išgyvenimas laukinėje gamtoje","Žinių pritaikymas");return;}
        if(has(a,"slėptis","slėpiuosi","sėlinti","sėlinu","nepastebėtam","tyliai judėti","slėpimasis")){pick(c,"Slėpimasis","Judesių tikslumas");return;}
        if(has(a,"apsidairyti","pastebėti","ieškoti","apžiūrėti","pastabumas")){pick(c,"Pastabumas","Regėjimas");return;}

        // KOVA. Specifinis ginklas / gynyba / tikslas turi pirmenybę prieš bendrą smūgį.
        if(has(a,"durklas","durklu","durklo","durklą","peiliu durti","trumpais ašmenimis","durklų meistriškumas")){pick(c,"Durklų meistriškumas","Atakos tikslumas");return;}
        if(has(a,"kardas","kardu","kardo","kardą","pjauti kardu","kardo ašmenimis","kardo meistriškumas")){pick(c,"Kardo meistriškumas","Atakos tikslumas");return;}
        if(word(a,"ietis","ietimi","ieties","ietį") || has(a,"smaigiu ginklu","ilgu kotu","ieties meistriškumas")){pick(c,"Ieties meistriškumas","Kovinis laiko parinkimas");return;}
        if(word(a,"lankas","lanku","lanko","lanką") || has(a,"strėlė","strėlę","šaudyti iš lanko","šaudau iš lanko","lanko meistriškumas")){pick(c,"Lanko meistriškumas","Atakos tikslumas");return;}
        if(has(a,"skydu","skydą","skydo","skydo valdymas")){pick(c,"Skydo valdymas","Gynyba");return;}
        if(has(a,"gintis","blokuoti","blokuoju","pariruoti","apsiginti","gynyba")){pick(c,"Gynyba","Refleksai");return;}
        if(has(a,"taikliai atakuoti","tiksliai smogti","silpną vietą","atakos tikslumas")){pick(c,"Atakos tikslumas","Kovinė nuojauta");return;}
        if(has(a,"tinkamu momentu","laiku atakuoti","kontratakuoti","kovinis laiko parinkimas")){pick(c,"Kovinis laiko parinkimas","Reakcijos greitis");return;}
        if(has(a,"atlaikyti smūgį","nesulūžti","kaulų tvirtumas")){pick(c,"Kaulų tvirtumas","Atsparumas traumoms");return;}
        if(has(a,"imtynės","pargriauti","klinčas","grumtis","numesti priešininką")){pick(c,"Imtynės","Kūno kontrolė");return;}
        if(has(a,"parteris","parteryje","ant žemės","laužimas","smaugimas","kova parteryje")){pick(c,"Kova parteryje","Imtynės");return;}
        if(has(a,"kovoju be ginklo","beginklė kova","kumščiu","kumščiais","spyris")){pick(c,"Beginklė kova","Smūgiavimo technika");return;}
        if(has(a,"smūgiavimo technika","smūgiuoti","smūgiuoju","smogti","smogiu","spirti")){pick(c,"Smūgiavimo technika","Kovinis laiko parinkimas");return;}
        if(has(a,"nepažįstamas ginklas","bet kokiu ginklu","ginklo valdymas","ginklų valdymas")){pick(c,"Ginklų valdymas","Atakos tikslumas");return;}
        if(has(a,"nujausti kovą","skaityti kovą","priešininko judesiai","kovinė nuojauta")){pick(c,"Kovinė nuojauta","Pavojaus nuojauta");return;}
        if(has(a,"kelis priešininkus","apsuptas","daugybė priešininkų","kelių priešininkų kontrolė")){pick(c,"Kelių priešininkų kontrolė","Taktinis prisitaikymas");return;}
        if(has(a,"prisitaikyti kovoje","keisti taktiką","netikėta taktika","taktinis prisitaikymas")){pick(c,"Taktinis prisitaikymas","Kovinė nuojauta");return;}
        if(has(a,"pulti","atakuoti","kovoti")){pick(c,"Kovinė nuojauta","Kovinis laiko parinkimas");return;}

        // JUDĖJIMAS IR KŪNAS
        if(has(a,"staiga sureaguoti","reakcijos greitis","netikėtai sureaguoti")){pick(c,"Reakcijos greitis","Refleksai");return;}
        if(has(a,"išsisukti","išsisuku","vengti","vengiu","atšokti","atšoku","išvengti","išvengiu","refleksai")){pick(c,"Refleksai","Vikrumas");return;}
        if(has(a,"kojų darbas","pozicija kovoje","judėti aplink priešininką")){pick(c,"Kojų darbas","Krypties keitimo greitis");return;}
        if(has(a,"šokti","šoku","peršokti","šuolis","šuolio galia")){pick(c,"Šuolio galia","Pusiausvyra");return;}
        if(has(a,"kristi","nusileisti","kritimas","kritimo kontrolė")){pick(c,"Kritimo kontrolė","Kūno kontrolė");return;}
        if(has(a,"erdvinė orientacija","erdviškai orientuotis","ore orientuotis","aukštyn žemyn")){pick(c,"Erdvinė orientacija","Koordinacija");return;}
        if(has(a,"tiksliai judėti","siauru taku","atsargiai žengti","judesių tikslumas")){pick(c,"Judesių tikslumas","Pusiausvyra");return;}
        if(has(a,"staigiai keisti kryptį","zigzagas","apsisukti bėgant","krypties keitimo greitis")){pick(c,"Krypties keitimo greitis","Vikrumas");return;}
        if(has(a,"plaukti","plaukiu","plaukimas")){pick(c,"Plaukimas","Širdies ir kvėpavimo ištvermė");return;}
        if(has(a,"lipti","lipu","kopti","užlipti","nulipti","laipiojimas")){pick(c,"Laipiojimas","Suėmimo jėga");return;}
        if(has(a,"pagreitėti","pagreitėju","staigiai įsibėgėti","akimirksniu įsibėgėti","pagreitis")){pick(c,"Pagreitis","Greitis");return;}
        if(has(a,"sprintuoti","sprintuoju","bėgti visu greičiu","vytis","sprukti","pabėgti","greitis")){pick(c,"Greitis","Pagreitis");return;}
        if(has(a,"ilgai bėgti","maratonas","ilgą kelią bėgti","širdies ir kvėpavimo ištvermė")){pick(c,"Širdies ir kvėpavimo ištvermė","Raumenų ištvermė");return;}
        if(has(a,"ilgai laikyti","daug kartų kelti","raumenų ištvermė")){pick(c,"Raumenų ištvermė","Jėga");return;}
        if(has(a,"sprogstamoji jėga","staigiai išplėšti","vienu šuoliu išjudinti")){pick(c,"Sprogstamoji jėga","Jėga");return;}
        if(has(a,"laužti","kelti","stumti","plėšti","naudoti jėgą","jėga")){pick(c,"Jėga","Sprogstamoji jėga");return;}
        if(has(a,"vikriai","akrobatika","manevruoti","vikrumas")){pick(c,"Vikrumas","Koordinacija");return;}
        if(has(a,"lankstytis","lankstumas","prasisprausti","išsilaisvinti iš siauros vietos")){pick(c,"Lankstumas","Kūno kontrolė");return;}
        if(has(a,"balansuoti","išlaikyti pusiausvyrą","briauna","pusiausvyra")){pick(c,"Pusiausvyra","Kūno kontrolė");return;}
        if(has(a,"suderinti judesius","koordinacija","sudėtinga judesių seka")){pick(c,"Koordinacija","Judesių tikslumas");return;}
        if(has(a,"kontroliuoti kūną","poza","kūno kontrolė")){pick(c,"Kūno kontrolė","Koordinacija");return;}
        if(has(a,"suimti","griebti daiktą","išlaikyti rankomis","suėmimo jėga")){pick(c,"Suėmimo jėga","Raumenų ištvermė");return;}
        if(has(a,"atsilaikyti prieš traumą","nesusižeisti","traumos rizika","atsparumas traumoms")){pick(c,"Atsparumas traumoms","Kaulų tvirtumas");return;}

        pick(c,"Sprendimų greitis","Kovinė nuojauta");
    }

    private static void pick(Check c,String primary,String secondary){c.primary=primary;c.secondary=secondary;}

    private static String canonicalExact(String action){
        String a=fold(action).trim();
        for(BaseStatCatalog.Group g:BaseStatCatalog.GROUPS){
            for(String stat:g.stats)if(fold(stat).equals(a))return stat;
        }
        return null;
    }

    private static String defaultSecondary(String primary){
        if(inGroup(primary,"KŪNO SAVYBĖS"))return primary.equals("Kūno kontrolė")?"Koordinacija":"Kūno kontrolė";
        if(inGroup(primary,"JUDĖJIMAS IR REFLEKSAI"))return primary.equals("Koordinacija")?"Refleksai":"Koordinacija";
        if(inGroup(primary,"KOVOS MEISTRIŠKUMAS"))return primary.equals("Kovinė nuojauta")?"Kovinis laiko parinkimas":"Kovinė nuojauta";
        if(inGroup(primary,"JUTIMAI IR IŠGYVENIMAS"))return primary.equals("Pastabumas")?"Regėjimas":"Pastabumas";
        if(inGroup(primary,"PROTINĖS SAVYBĖS"))return primary.equals("Intelektas")?"Loginis mąstymas":"Intelektas";
        if(inGroup(primary,"MAGINĖS SAVYBĖS"))return primary.equals("Manos kontrolė")?"Burtų tikslumas":"Manos kontrolė";
        if(inGroup(primary,"SOCIALINĖS IR PRAKTINĖS SAVYBĖS"))return primary.equals("Žmonių perpratimas")?"Empatija":"Žmonių perpratimas";
        return "Sprendimų greitis";
    }


    private static int difficulty(String a,GameState s,String primary){
        int d=125;
        if(has(a,"apžiūrėti","klausytis","stebėti","perskaityti","paklausti"))d=105;
        if(has(a,"keliauti","eiti į","vykti į"))d=115;
        if(isPhysical(primary))d=Math.max(d,135);
        if(inGroup(primary,"SOCIALINĖS IR PRAKTINĖS SAVYBĖS"))d=Math.max(d,135);
        if(inGroup(primary,"KOVOS MEISTRIŠKUMAS"))d=Math.max(d,145);
        if(isMagic(primary))d=Math.max(d,150);
        if(has(a,"meridian","nežinoma taisyklė","anomalija","priežastingumas","nulinė sąveika","laiko versija"))d=Math.max(d,175);
        if(has(a,"sunaikinti","nužudyti","vienu smūgiu","akimirksniu"))d=Math.max(d,190);
        if(s.combatActive)d+=10;
        return Math.min(210,d);
    }

    private static String difficultyReason(String a,GameState s,String primary){
        if(has(a,"meridian","nežinoma taisyklė","anomalija","priežastingumas","nulinė sąveika"))return "veiksmas liečia ne iki galo suprastą pasaulio taisyklę";
        if(s.combatActive)return "aktyvus priešininkas gali priešintis ir keisti situaciją";
        if(isMagic(primary))return "maginis veiksmas reikalauja kontrolės ir stabilumo";
        if(inGroup(primary,"SOCIALINĖS IR PRAKTINĖS SAVYBĖS"))return "kitas veikėjas ar praktinė situacija turi savo apribojimus";
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
        if((stat.contains("Kardo")||stat.contains("Atakos")||stat.contains("Kovinė"))&&has(e,"asteriono"))m+=10;
        if((stat.equals("Gynyba")||isPhysical(stat))&&has(e,"septynsluoksn"))m+=6;
        if((stat.equals("Erdvinė magija")||has(a,"keliauti"))&&has(e,"kelių klostės"))m+=8;
        if(stat.equals("Relikvijų rezonansas")&&has(e,"rezonanso signetas"))m+=12;
        if((stat.equals("Analitinis mąstymas")||stat.equals("Magijos jutimas"))&&has(e,"nulinio stiklo"))m+=10;
        if((stat.equals("Erdvinė magija")||has(a,"meridian"))&&has(e,"meridiano raktas"))m+=12;
        return Math.min(25,m);
    }

    private static int abilityModifier(String stat,String a,List<String[]> abilities){
        int m=0;
        if(abilities==null)return 0;
        for(String[] ab:abilities){
            if(ab==null||ab.length==0)continue;
            String n=norm(ab[0]);
            if(stat.equals("Gynyba")&&has(n,"eoninis bastionas"))m=Math.max(m,10);
            if((stat.equals("Refleksai")||stat.equals("Vikrumas"))&&has(n,"erdvinis išsisukimas"))m=Math.max(m,10);
            if((stat.equals("Analitinis mąstymas")||stat.equals("Magijos jutimas"))&&has(n,"nežinomų taisyklių"))m=Math.max(m,8);
            if(stat.equals("Relikvijų rezonansas")&&has(n,"relikvijų simbiozė"))m=Math.max(m,12);
            if(stat.equals("Laiko magija")&&has(n,"laiko paralakso"))m=Math.max(m,10);
        }
        return m;
    }

    private static boolean isPhysical(String stat){
        return inGroup(stat,"KŪNO SAVYBĖS") || inGroup(stat,"JUDĖJIMAS IR REFLEKSAI") || inGroup(stat,"KOVOS MEISTRIŠKUMAS");
    }

    private static boolean isMagic(String stat){return inGroup(stat,"MAGINĖS SAVYBĖS");}

    private static boolean inGroup(String stat,String groupName){
        for(BaseStatCatalog.Group g:BaseStatCatalog.GROUPS){
            if(!g.name.equals(groupName))continue;
            for(String s:g.stats)if(s.equals(stat))return true;
        }
        return false;
    }

    private static boolean has(String text,String... needles){
        String t=fold(text);
        for(String n:needles)if(t.contains(fold(n)))return true;
        return false;
    }

    private static boolean word(String text,String... words){
        String t=fold(text);
        for(String w:words){
            String q=fold(w);int from=0;
            while((from=t.indexOf(q,from))>=0){
                int end=from+q.length();
                boolean left=from==0||!Character.isLetterOrDigit(t.charAt(from-1));
                boolean right=end==t.length()||!Character.isLetterOrDigit(t.charAt(end));
                if(left&&right)return true;
                from++;
            }
        }
        return false;
    }

    private static String norm(String s){return s==null?"":s.toLowerCase(Locale.forLanguageTag("lt-LT"));}
    private static String fold(String s){
        return norm(s).replace('ą','a').replace('č','c').replace('ę','e').replace('ė','e').replace('į','i')
                .replace('š','s').replace('ų','u').replace('ū','u').replace('ž','z');
    }
    private static String signed(int n){return n>=0?"+"+n:String.valueOf(n);}
    private StatEngine(){}
}
