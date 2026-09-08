package lt.vaeloria.ooc;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.*;

final class SideQuestRepository {
    private final VaeloriaDb owner;
    SideQuestRepository(VaeloriaDb owner){this.owner=owner;}
    private SQLiteDatabase db(){return owner.getWritableDatabase();}

    static void seed(SQLiteDatabase db){
        for(SideQuestCatalog.QuestDef def:SideQuestCatalog.ALL){
            String[] titles={"Pasiekti vietą: "+def.location,def.objective,"Grįžti į Luminarą ir atsiimti atlygį"};
            for(int position=0;position<3;position++){
                ContentValues values=new ContentValues();values.put("id",stepId(def.id,position));values.put("quest_id",def.id);
                values.put("position",position);values.put("title",titles[position]);values.put("target",position==1?def.target:1);
                values.put("status",position==0?"available":"locked");
                db.insertWithOnConflict("quest_steps",null,values,SQLiteDatabase.CONFLICT_IGNORE);
                ContentValues title=new ContentValues();title.put("title",titles[position]);title.put("target",position==1?def.target:1);
                db.update("quest_steps",title,"id=?",new String[]{stepId(def.id,position)});
            }
        }
    }

    WorldRepository.Quest quest(String id){for(WorldRepository.Quest q:owner.world().quests())if(q.id.equals(id))return q;return null;}
    private boolean giverPresent(SideQuestCatalog.QuestDef def,GameState state){
        for(WorldRepository.Npc npc:owner.world().npcs(state.location,state.worldMinute))if(npc.id.equals(def.giver)&&npc.available)return true;
        return false;
    }
    WorldRepository.TransactionResult accept(String id,GameState original){
        SideQuestCatalog.QuestDef def=SideQuestCatalog.byId(id);WorldRepository.Quest quest=quest(id);
        if(def==null||quest==null||!"available".equals(quest.status))return result(false,"Ši užduotis jau priimta arba užbaigta");
        if(original.combatActive||!giverPresent(def,original))return result(false,"Užduoties davėjo dabar čia nėra. Aplankyk jį Luminaroje dienos metu.");
        try{
            GameState state=GameState.fromJson(original.toJson());SQLiteDatabase database=db();database.beginTransaction();
            try{
                owner.checkpoint("prieš užduoties priėmimą",original);setQuest(id,"active",0);setStep(id,0,"active",0);
                database.execSQL("UPDATE locations SET discovered=1 WHERE name=?",new Object[]{def.location});
                state.trackedQuestId=id;owner.world().applyStructuredChoices(state);owner.saveState(state);database.setTransactionSuccessful();
            }finally{database.endTransaction();}
            original.copyFrom(state);return result(true,"Priimta: "+quest.title+". Davėjas pažymėjo kelionės tikslą atlase.");
        }catch(Exception error){return result(false,"Užduoties priimti nepavyko. Pažanga nepasikeitė.");}
    }

    String record(String action,String event,GameState state,StatEngine.Check check){
        ArrayList<String> updates=new ArrayList<>();
        for(WorldRepository.Quest quest:owner.world().quests()){
            SideQuestCatalog.QuestDef def=SideQuestCatalog.byId(quest.id);
            if(def==null||!"active".equals(quest.status)||!def.location.equals(state.location))continue;
            if(quest.stage==0&&("travel".equals(event)||"discovery".equals(event))){
                setStep(def.id,0,"completed",1);setStep(def.id,1,"active",0);setQuest(def.id,"active",1);
                updates.add(quest.title+": "+def.objective);continue;
            }
            if(quest.stage!=1)continue;
            boolean success="combat".equals(def.kind)?"combat_victory".equals(event)
                    :"discovery".equals(event)&&!AiTurnPolicyV101.isFailure(check)&&ActionText.mentions(action,def.action);
            if(!success)continue;
            int progress=0;for(WorldRepository.QuestStep step:owner.world().steps(def.id))if(step.position==1)progress=step.progress;
            progress=Math.min(def.target,progress+1);
            if("gather".equals(def.kind))owner.addCatalogLoot(ItemCatalogV092.byId(def.material),1);
            setStep(def.id,1,progress>=def.target?"completed":"active",progress);
            if(progress>=def.target){setStep(def.id,2,"active",0);setQuest(def.id,"ready",2);updates.add(quest.title+": tikslas įvykdytas, grįžk atsiimti atlygio.");}
            else updates.add(quest.title+": "+progress+"/"+def.target);
        }
        return String.join(" · ",updates);
    }

    WorldRepository.TransactionResult claim(String id,GameState original){
        SideQuestCatalog.QuestDef def=SideQuestCatalog.byId(id);WorldRepository.Quest quest=quest(id);
        if(def==null||quest==null||!"ready".equals(quest.status))return result(false,"Atlygis dar neparuoštas arba jau atsiimtas");
        if(original.combatActive||!giverPresent(def,original))return result(false,"Atlygį atsiimk pas užduoties davėją Luminaroje dienos metu.");
        if(!def.material.isEmpty()&&owner.world().ownedCatalogQuantity(def.material)<def.target)return result(false,"Atnešk visus surinktus reagentus.");
        try{
            GameState state=GameState.fromJson(original.toJson());SQLiteDatabase database=db();database.beginTransaction();
            try{
                owner.checkpoint("prieš užduoties atlygį",original);
                if(!def.material.isEmpty())owner.world().consumeCatalog(database,def.material,def.target);
                setStep(id,2,"completed",1);setQuest(id,"completed",3);state.crowns+=def.gold;ProgressionEngine.grant(state,def.xp);
                owner.addCatalogLoot(ItemCatalogV092.byId(def.rewardItem),2);
                database.execSQL("UPDATE npcs SET relationship=MIN(100,relationship+8),trust=MIN(100,trust+5) WHERE id=?",new Object[]{def.giver});
                if(id.equals(state.trackedQuestId))state.trackedQuestId="";
                state.sceneTitle="Užduotis užbaigta";state.scene="Užbaigi užduotį „"+quest.title+"“. Davėjas priima tavo darbo rezultatą ir perduoda sutartą atlygį.";
                owner.world().applyStructuredChoices(state);owner.saveState(state);database.setTransactionSuccessful();
            }finally{database.endTransaction();}
            original.copyFrom(state);return result(true,"Užduotis užbaigta · +"+def.gold+" karūnų · +"+def.xp+" patirties · "+ItemCatalogV092.byId(def.rewardItem).name+" ×2");
        }catch(Exception error){return result(false,"Atlygio išsaugoti nepavyko. Ankstesnė pažanga išliko.");}
    }

    boolean choices(GameState state){
        if(state.combatActive)return false;
        WorldRepository.Quest quest=quest(state.trackedQuestId);SideQuestCatalog.QuestDef def=SideQuestCatalog.byId(state.trackedQuestId);
        if(quest==null||def==null||"completed".equals(quest.status)||"available".equals(quest.status))return false;
        state.questTitle=quest.title;state.objective=quest.stage<2?(quest.stage==0?"Pasiekti vietą: "+def.location:def.objective):"Atsiimti atlygį Luminaroje";
        state.choices.clear();
        if(quest.stage>=2)state.choices.add("Luminara".equals(state.location)?"Pranešti apie užduoties įvykdymą":"Grįžti į Luminarą");
        else state.choices.add(def.location.equals(state.location)?def.action:"Keliauti į "+def.location);
        state.choices.add("Pailsėti saugioje vietoje");state.choices.add("Apsidairyti ir įvertinti aplinką");return true;
    }

    private void setQuest(String id,String status,int stage){ContentValues v=new ContentValues();v.put("status",status);v.put("stage",stage);db().update("quests",v,"id=?",new String[]{id});}
    private void setStep(String id,int position,String status,int progress){ContentValues v=new ContentValues();v.put("status",status);v.put("progress",progress);db().update("quest_steps",v,"id=?",new String[]{stepId(id,position)});}
    private static String stepId(String id,int position){return id+"-0"+(position+1);}
    private WorldRepository.TransactionResult result(boolean ok,String message){return new WorldRepository.TransactionResult(ok,message);}
}
