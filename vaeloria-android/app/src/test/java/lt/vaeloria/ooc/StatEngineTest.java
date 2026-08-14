package lt.vaeloria.ooc;

import org.junit.Test;
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
}
