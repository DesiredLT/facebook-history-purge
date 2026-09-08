package lt.vaeloria.ooc;

final class SideQuestCatalog {
    static final class QuestDef {
        final String id,giver,location,objective,action,kind,material,rewardItem;
        final int target,gold,xp;
        QuestDef(String id,String giver,String location,String objective,String action,String kind,String material,int target,int gold,int xp,String rewardItem){
            this.id=id;this.giver=giver;this.location=location;this.objective=objective;this.action=action;this.kind=kind;
            this.material=material;this.target=target;this.gold=gold;this.xp=xp;this.rewardItem=rewardItem;
        }
    }
    static final QuestDef[] ALL={
        new QuestDef("Q-SMITH","npc-brynja","Veyrhold","Apginti rūdos karavaną: laimėti kovą prie Veyrhold","Pradėti kovą ginant rūdos karavaną","combat","",1,650,450,"I092-201"),
        new QuestDef("Q-HEALER","npc-elen","Stiklo Giria","Surinkti tris patikrintų žolelių ryšulius","Rinkti patikrintas žoleles priešnuodžiui","gather","I092-277",3,420,400,"I092-203"),
        new QuestDef("Q-GUILD","npc-korva","Veyrhold","Patikrinti cechų tiekimo registrą","Ištirti cechų tiekimo registrą","investigate","",1,500,420,"I092-204"),
        new QuestDef("Q-ROADS","npc-darvenas","Kharad Vorn","Ištirti dingusio karavano stovyklavietę","Ištirti dingusio karavano stovyklavietę","investigate","",1,700,600,"I092-201"),
        new QuestDef("Q-ARCHIVE","npc-emilis","Asterio Karūna","Palyginti ištrintos chartijos kopijas","Palyginti ištrintos chartijos kopijas","investigate","",1,550,480,"I092-203")
    };
    static QuestDef byId(String id){for(QuestDef def:ALL)if(def.id.equals(id))return def;return null;}
    private SideQuestCatalog(){}
}
