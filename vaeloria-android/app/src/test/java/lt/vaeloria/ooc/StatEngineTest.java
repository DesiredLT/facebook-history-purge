package lt.vaeloria.ooc;

import org.junit.Test;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class StatEngineTest {
    @Test public void catalogHasExactly92UniqueStats(){
        assertEquals(92, BaseStatCatalog.totalCount());
        assertEquals(7, BaseStatCatalog.GROUPS.length);
        int[] expected={14,10,16,10,12,18,12};
        Set<String> names=new HashSet<>();
        for(int i=0;i<BaseStatCatalog.GROUPS.length;i++){
            assertEquals(expected[i],BaseStatCatalog.GROUPS[i].stats.length);
            for(String s:BaseStatCatalog.GROUPS[i].stats)assertTrue("Duplicate stat: "+s,names.add(s));
        }
        assertEquals(92,names.size());
    }

    @Test public void everyCanonicalStatNameSelectsItselfAsPrimary(){
        for(BaseStatCatalog.Group g:BaseStatCatalog.GROUPS){
            for(String stat:g.stats){
                assertEquals("Canonical stat is shadowed or unreachable: "+stat,stat,StatEngine.classifyForTest(stat)[0]);
            }
        }
    }

    private static void expect(String action,String primary){
        assertEquals(action,primary,StatEngine.classifyForTest(action)[0]);
    }

    @Test public void oneNaturalActionForEveryStat(){
        String[][] cases={
            {"naudoju jėgą durims išlaužti","Jėga"},
            {"staigiai išplėšti įstrigusią svirtį","Sprogstamoji jėga"},
            {"daug kartų kelti sunkų akmenį","Raumenų ištvermė"},
            {"ilgai bėgti be sustojimo","Širdies ir kvėpavimo ištvermė"},
            {"sprintuoti per kiemą","Greitis"},
            {"staigiai įsibėgėti iš vietos","Pagreitis"},
            {"vikriai manevruoti tarp kliūčių","Vikrumas"},
            {"lankstytis ir prasisprausti pro plyšį","Lankstumas"},
            {"balansuoti ant siauros briaunos","Pusiausvyra"},
            {"suderinti rankų ir kojų judesius","Koordinacija"},
            {"kontroliuoti kūną ore","Kūno kontrolė"},
            {"išlaikyti sunkų daiktą rankomis","Suėmimo jėga"},
            {"atlaikyti smūgį nesulūžus","Kaulų tvirtumas"},
            {"atsilaikyti prieš traumą","Atsparumas traumoms"},

            {"staiga sureaguoti į strėlę","Reakcijos greitis"},
            {"išvengti netikėto smūgio","Refleksai"},
            {"naudoti kojų darbą dvikovoje","Kojų darbas"},
            {"peršokti platų griovį","Šuolio galia"},
            {"nusileisti po aukšto kritimo","Kritimo kontrolė"},
            {"erdviškai orientuotis visiškoje tamsoje","Erdvinė orientacija"},
            {"tiksliai judėti siauru taku","Judesių tikslumas"},
            {"staigiai keisti kryptį bėgant","Krypties keitimo greitis"},
            {"plaukti per upę","Plaukimas"},
            {"kopti į bokštą","Laipiojimas"},

            {"kovoju be ginklo","Beginklė kova"},
            {"smūgiuoti į taikinį","Smūgiavimo technika"},
            {"pargriauti priešininką imtynėmis","Imtynės"},
            {"kovoti parteryje ant žemės","Kova parteryje"},
            {"valdyti nepažįstamą ginklą","Ginklų valdymas"},
            {"atakuoti kardu","Kardo meistriškumas"},
            {"atakuoti durklu","Durklų meistriškumas"},
            {"kovoti ietimi","Ieties meistriškumas"},
            {"šaudyti iš lanko","Lanko meistriškumas"},
            {"blokuoti skydu","Skydo valdymas"},
            {"pariruoti priešininko smūgį","Gynyba"},
            {"tiksliai smogti į silpną vietą","Atakos tikslumas"},
            {"kontratakuoti tinkamu momentu","Kovinis laiko parinkimas"},
            {"skaityti kovą ir priešininko judesius","Kovinė nuojauta"},
            {"kontroliuoti kelis priešininkus vienu metu","Kelių priešininkų kontrolė"},
            {"prisitaikyti kovoje ir keisti taktiką","Taktinis prisitaikymas"},

            {"pamatyti siluetą tolumoje","Regėjimas"},
            {"išgirsti tylų garsą","Klausa"},
            {"užuosti keistą kvapą","Uoslė"},
            {"apčiuopti nematomą paviršių","Lytėjimo jautrumas"},
            {"nujausti grėsmę prieš pasalą","Pavojaus nuojauta"},
            {"sekti priešą pagal pėdsakus","Sekimas"},
            {"rasti kelią nepažįstamoje vietovėje","Orientavimasis vietovėje"},
            {"rasti maisto laukinėje gamtoje","Išgyvenimas laukinėje gamtoje"},
            {"slėptis ir judėti nepastebėtam","Slėpimasis"},
            {"apsidairyti ir pastebėti detalę","Pastabumas"},

            {"suprasti sudėtingą principą","Intelektas"},
            {"prisiminti seną pokalbį","Atmintis"},
            {"išmokti naują techniką","Mokymosi greitis"},
            {"išspręsti galvosūkį logiškai","Loginis mąstymas"},
            {"analizuoti surinktus įrodymus","Analitinis mąstymas"},
            {"improvizuoti netradicinį sprendimą","Kūrybiškumas"},
            {"susikaupti nepaisant triukšmo","Susikaupimas"},
            {"nepasiduoti stipriam skausmui","Valia"},
            {"ištverti psichologinį spaudimą","Psichologinis atsparumas"},
            {"greitai nuspręsti ką daryti","Sprendimų greitis"},
            {"parengti detalų planą","Planavimas"},
            {"strategiškai suplanuoti ilgalaikę kampaniją","Strateginis mąstymas"},

            {"sukaupti didelį manos kiekį","Manos talpa"},
            {"atkurti maną po kovos","Manos atkūrimas"},
            {"naudoti magiją tiksliai valdant maną","Manos kontrolė"},
            {"sustiprinti burtą maksimalia galia","Burtų galia"},
            {"tiksliai burti į mažą taikinį","Burtų tikslumas"},
            {"greitai burti prieš ataką","Burtų greitis"},
            {"stabilizuoti burtą audros metu","Burtų stabilumas"},
            {"taupyti maną efektyviai buriant","Burtų efektyvumas"},
            {"aptikti magiją kambaryje","Magijos jutimas"},
            {"atsispirti priešo magijai","Atsparumas magijai"},
            {"valdyti ugnį kaip elementą","Elementų valdymas"},
            {"gydyti žaizdą magija","Gydomoji magija"},
            {"sukurti maginį barjerą","Apsauginė magija"},
            {"teleportuoti save per erdvę","Erdvinė magija"},
            {"sustabdyti laiką akimirkai","Laiko magija"},
            {"transmutuoti metalą į kitą medžiagą","Transmutacija"},
            {"išsklaidyti priešo burtą","Užkeikimų ardymas"},
            {"aktyvuoti relikviją rezonansu","Relikvijų rezonansas"},

            {"sužavėti susirinkusius žmones","Charizma"},
            {"įtikinti sargybinį mane praleisti","Įtikinėjimas"},
            {"derėtis dėl geresnės kainos","Derybos"},
            {"siekti taikos tarp frakcijų diplomatija","Diplomatija"},
            {"vadovauti grupei mūšyje","Vadovavimas"},
            {"užjausti sužeistą žmogų","Empatija"},
            {"perprasti žmogaus motyvą","Žmonių perpratimas"},
            {"atpažinti ar pašnekovas meluoja","Apgaulės atpažinimas"},
            {"meluoti sargybiniui","Apgaulė"},
            {"grasinti ir įbauginti priešą","Bauginimas"},
            {"pagaminti sudėtingą įrankį","Amatų meistriškumas"},
            {"pritaikyti turimas žinias praktikoje","Žinių pritaikymas"}
        };
        assertEquals(92,cases.length);
        Set<String> covered=new HashSet<>();
        for(String[] x:cases){
            expect(x[0],x[1]);
            assertTrue("Duplicate semantic coverage for "+x[1],covered.add(x[1]));
        }
        assertEquals(92,covered.size());
    }

    @Test public void regressionCasesDoNotCollide(){
        expect("pamatyti duris","Regėjimas");
        expect("stovėti vietoje","Sprendimų greitis");
        expect("lankstytis","Lankstumas");
        expect("atkurti mano sveikatą","Gydomoji magija");
        expect("manau, kad reikia palaukti","Sprendimų greitis");
        expect("lipdyti molį","Amatų meistriškumas");
        expect("atlaikyti smūgį","Kaulų tvirtumas");
        expect("tiksliai smogti į silpną vietą","Atakos tikslumas");
        expect("erdvinė orientacija tamsoje","Erdvinė orientacija");
        expect("analizuoju artefaktą","Analitinis mąstymas");
        expect("smogiu durklu ašmenimis","Durklų meistriškumas");
        expect("šaudau iš lanko","Lanko meistriškumas");
        expect("kovosiu ietimi","Ieties meistriškumas");
        expect("meldžiuosi šventykloje","Sprendimų greitis");
        expect("einu į kitą sektorių","Sprendimų greitis");
        expect("garsiai šaukiu","Sprendimų greitis");
        expect("blokuoju smūgį skydu","Skydo valdymas");
        expect("blokuoju smūgį","Gynyba");
        expect("smūgiuoju kumščiu","Beginklė kova");
        expect("smūgiavimo technika","Smūgiavimo technika");
        expect("pagreitėju","Pagreitis");
        expect("sprintuoju","Greitis");
        expect("prisimenu praeitį","Atmintis");
        expect("perkelti dėžę","Jėga");
        expect("ištiriu magiją","Analitinis mąstymas");
        expect("aktyvuoju Meridiano Raktą","Erdvinė magija");
        expect("aktyvuoju artefaktą","Relikvijų rezonansas");
        expect("smogiu relikvija","Smūgiavimo technika");
    }

    @Test public void inputWithoutLithuanianDiacriticsWorks(){
        expect("slepiuosi seselyje","Slėpimasis");
        expect("isvengti smugio","Refleksai");
        expect("naudoju jega durims islauzti","Jėga");
        expect("saudau is lanko","Lanko meistriškumas");
        expect("lipti i boksta","Laipiojimas");
        expect("pamatyti priesa tamsoje","Regėjimas");
        expect("atkurti mana","Manos atkūrimas");
    }

    @Test public void resourcePenaltiesFollowCatalogGroupsNotWordFragments(){
        GameState s=new GameState();
        s.hp=10;s.hpMax=100;s.stamina=10;s.staminaMax=100;s.mana=10;s.manaMax=100;
        StatEngine.Check spatial=StatEngine.resolve(s,"Erdvinė orientacija","",Collections.emptyList(),null);
        assertEquals("Erdvinė orientacija",spatial.primary);
        assertEquals(-35,spatial.conditionModifier);
        assertEquals(135,spatial.difficulty);
        StatEngine.Check magic=StatEngine.resolve(s,"Erdvinė magija","",Collections.emptyList(),null);
        assertEquals("Erdvinė magija",magic.primary);
        assertEquals(-20,magic.conditionModifier);
        assertEquals(150,magic.difficulty);
        StatEngine.Check ordinary=StatEngine.resolve(s,"laikau duris","",Collections.emptyList(),null);
        assertEquals("Sprendimų greitis",ordinary.primary);
        assertEquals(125,ordinary.difficulty);
    }
}
