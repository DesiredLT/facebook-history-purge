package lt.vaeloria.ooc;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ProgressionEngineV100Test {
    @Test public void levelCurveAlwaysGrows(){
        int previous=0;for(int level=1;level<100;level++){int next=ProgressionEngine.experienceForNext(level);assertTrue(next>previous);previous=next;}
    }

    @Test public void awardCanLevelAndGrantTalentPoint(){
        GameState state=new GameState();state.experience=ProgressionEngine.experienceForNext(1)-1;state.experienceNext=ProgressionEngine.experienceForNext(1);
        ProgressionEngine.Award award=ProgressionEngine.award(state,"discovery",null,0);
        assertEquals(2,state.level);assertEquals(1,award.levels);assertTrue(award.talentPoints>=1);assertTrue(state.talentPoints>=2);
    }

    @Test public void talentCatalogHasUniqueProgressiveBranches(){
        Set<String> ids=new HashSet<>();Set<String> branches=new HashSet<>();
        for(ProgressionEngine.TalentDef talent:ProgressionEngine.TALENTS){assertTrue(ids.add(talent.id));assertTrue(talent.requiredLevel>=1);assertTrue(talent.cost>=1);branches.add(talent.branch);if(!talent.prerequisite.isEmpty())assertNotNull(ProgressionEngine.byId(talent.prerequisite));}
        assertEquals(5,branches.size());assertEquals(20,ids.size());
    }

    @Test public void specializedChecksReceiveOnlyRelevantBonus(){
        Set<String> ids=ProgressionEngine.set("scout_eye","arcane_focus","leader_voice","craft_lore");
        assertTrue(ProgressionEngine.checkBonus(ids,"Tirti Meridiano pėdsaką","Pastabumas")>=5);
        assertTrue(ProgressionEngine.checkBonus(ids,"Derėtis su gildija","Derybos")>=5);
        assertEquals(0,ProgressionEngine.checkBonus(ids,"Smogti kardu","Atakos tikslumas"));
    }
}
