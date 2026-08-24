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
        assertEquals(2,state.level);assertEquals(1,award.levels);assertTrue(award.talentPoints>=1);assertTrue(state.talentPoints>=2);assertEquals(1,award.attributePoints);assertEquals(1,state.attributePoints);
    }

    @Test public void talentCatalogHasUniqueProgressiveBranches(){
        Set<String> ids=new HashSet<>();Set<String> branches=new HashSet<>();
        int totalCost=0,maxLevel=0;for(ProgressionEngine.TalentDef talent:ProgressionEngine.TALENTS){assertTrue(ids.add(talent.id));assertTrue(talent.requiredLevel>=1);assertTrue(talent.cost>=1);totalCost+=talent.cost;maxLevel=Math.max(maxLevel,talent.requiredLevel);branches.add(talent.branch);if(!talent.prerequisite.isEmpty())assertNotNull(ProgressionEngine.byId(talent.prerequisite));}
        assertEquals(5,branches.size());assertEquals(25,ids.size());assertEquals(75,totalCost);assertTrue(maxLevel>=90);
    }

    @Test public void legacyCharactersReceiveDeterministicAttributeCatchup() throws Exception{
        GameState source=new GameState();source.level=25;org.json.JSONObject json=source.toJson();json.remove("attributePoints");
        GameState restored=GameState.fromJson(json);assertEquals(ProgressionEngine.attributePointsThroughLevel(25),restored.attributePoints);assertTrue(restored.attributePoints>25);
    }

    @Test public void specializedChecksReceiveOnlyRelevantBonus(){
        Set<String> ids=ProgressionEngine.set("scout_eye","arcane_focus","leader_voice","craft_lore");
        assertTrue(ProgressionEngine.checkBonus(ids,"Tirti Meridiano pėdsaką","Pastabumas")>=5);
        assertTrue(ProgressionEngine.checkBonus(ids,"Derėtis su gildija","Derybos")>=5);
        assertEquals(0,ProgressionEngine.checkBonus(ids,"Smogti kardu","Atakos tikslumas"));
    }
}
