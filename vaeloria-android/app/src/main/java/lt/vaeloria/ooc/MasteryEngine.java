package lt.vaeloria.ooc;

import java.util.Map;

public final class MasteryEngine {
    public static int checkBonus(String primary,String secondary,Map<String,Integer> levels){
        int p=value(levels,primary),s=value(levels,secondary);
        int weighted=Math.round(p*.75f+s*.25f);
        return Math.max(0,Math.min(10,weighted/10));
    }

    public static int primaryXp(StatEngine.Check c){
        if(c==null)return 0;
        int gain=18+Math.max(0,c.difficulty)/5;
        if("išskirtinė sėkmė".equals(c.outcome))gain+=18;
        else if("rimta nesėkmė".equals(c.outcome))gain+=12;
        else if("nesėkmė".equals(c.outcome))gain+=7;
        return Math.min(80,gain);
    }

    public static int secondaryXp(StatEngine.Check c){return Math.max(8,primaryXp(c)*2/5);}

    public static String effectLine(int bonus,int primaryGain,int secondaryGain){
        return "Meistriškumas +"+bonus+" · patirtis +"+primaryGain+" pagrindinei / +"+secondaryGain+" pagalbinei savybei";
    }

    public static String outcomeAfterBonus(StatEngine.Check c,int bonus){
        int margin=c.total+bonus-c.difficulty;
        if(c.roll==100||margin>=45)return"išskirtinė sėkmė";
        if(margin>=0)return"sėkmė";
        if(margin>=-20)return"dalinė sėkmė";
        if(c.roll==1||margin<=-45)return"rimta nesėkmė";
        return"nesėkmė";
    }

    public static void apply(StatEngine.Check c,int bonus){
        if(c==null||bonus<=0)return;
        c.total+=bonus;
        c.outcome=outcomeAfterBonus(c,0);
        c.reason=(c.reason==null?"":c.reason)+"; post-cap meistriškumo modifikatorius +"+bonus;
    }

    private static int value(Map<String,Integer> m,String k){return m==null?70:Math.max(0,Math.min(100,m.getOrDefault(k,70)));}
    private MasteryEngine(){}
}
