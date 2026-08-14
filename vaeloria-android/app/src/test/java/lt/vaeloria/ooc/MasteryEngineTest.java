package lt.vaeloria.ooc;

import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class MasteryEngineTest {
    @Test public void masteryBonusUsesPrimary75Secondary25(){
        Map<String,Integer> m=new HashMap<>();m.put("Jėga",100);m.put("Kūno kontrolė",60);
        assertEquals(9,MasteryEngine.checkBonus("Jėga","Kūno kontrolė",m));
    }

    @Test public void baseline70GivesSevenPointBonus(){
        Map<String,Integer> m=new HashMap<>();m.put("Jėga",70);m.put("Kūno kontrolė",70);
        assertEquals(7,MasteryEngine.checkBonus("Jėga","Kūno kontrolė",m));
    }

    @Test public void masteryCanChangeBorderlineOutcome(){
        StatEngine.Check c=new StatEngine.Check();c.total=130;c.difficulty=135;c.roll=50;c.outcome="dalinė sėkmė";c.reason="testas";
        MasteryEngine.apply(c,7);
        assertEquals(137,c.total);assertEquals("sėkmė",c.outcome);assertTrue(c.reason.contains("+7"));
    }

    @Test public void difficultActionsGiveMoreXp(){
        StatEngine.Check easy=new StatEngine.Check();easy.difficulty=105;easy.outcome="sėkmė";
        StatEngine.Check hard=new StatEngine.Check();hard.difficulty=190;hard.outcome="sėkmė";
        assertTrue(MasteryEngine.primaryXp(hard)>MasteryEngine.primaryXp(easy));
        assertTrue(MasteryEngine.secondaryXp(hard)>0);
    }
}
