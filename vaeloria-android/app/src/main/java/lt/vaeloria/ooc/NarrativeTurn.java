package lt.vaeloria.ooc;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashSet;
import java.util.Locale;

/** AI owns words only. Every mechanical field is copied from the local result. */
final class NarrativeTurn {
    static JSONObject merge(JSONObject resolved, JSONObject narration, GameState state) throws Exception {
        validate(narration);
        JSONObject result = new JSONObject(resolved.toString());
        JSONObject polished = LithuanianNarrative.polish(new JSONObject(narration.toString()), state);
        result.put("scene_title", polished.getString("scene_title"));
        result.put("scene", polished.getString("scene"));
        // Buttons retain their approved actions even if a model suggests an unavailable move.
        result.put("choices", new JSONArray(resolved.getJSONArray("choices").toString()));
        return result;
    }

    static void validate(JSONObject value) throws Exception {
        if (value == null) throw new IllegalArgumentException("Trūksta pasakojimo.");
        text(value.opt("scene_title"), 100);
        text(value.opt("scene"), 1000);
        JSONArray choices = value.optJSONArray("choices");
        if (choices == null || choices.length() != 3) throw new IllegalArgumentException("Reikia trijų pasirinkimų.");
        HashSet<String> seen = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            String choice = text(choices.opt(i), 150);
            if (!seen.add(choice.toLowerCase(Locale.forLanguageTag("lt-LT"))))
                throw new IllegalArgumentException("Pasirinkimai kartojasi.");
        }
    }

    private static String text(Object raw, int max) {
        if (!(raw instanceof String)) throw new IllegalArgumentException("Netinkamas teksto laukas.");
        String value = ((String) raw).trim();
        if (value.isEmpty() || value.length() > max || LithuanianNarrative.clearlyEnglish(value))
            throw new IllegalArgumentException("Netinkamas lietuviškas pasakojimas.");
        return value;
    }

    static String context(GameState state, String action, String equipment, java.util.List<String[]> abilities,
                          StatEngine.Check check, String world, JSONObject resolved) {
        return userPrompt(state, action, equipment, abilities, check, world)
                + "\n\nPATVIRTINTAS VIETINIO VARIKLIO REZULTATAS (nekeisti faktų):\n" + resolved;
    }

    static String userPrompt(GameState s,String action,String equipped,java.util.List<String[]> abilities,StatEngine.Check check,String worldContext){
        StringBuilder abilityNames=new StringBuilder();
        if(abilities!=null)for(String[] ability:abilities){if(ability==null||ability.length==0)continue;if(abilityNames.length()>0)abilityNames.append("; ");abilityNames.append(ability[0]);}
        String recent=s.recentTurns.isEmpty()?"nėra":String.join(" | ",s.recentTurns.subList(Math.max(0,s.recentTurns.size()-6),s.recentTurns.size()));
        return "ŽAIDIMO BŪSENA\n"
                +"Veikėjas. "+CharacterCatalogV093.profilePrompt(s)+" Amžius: "+CharacterCatalogV093.ageLine(s)+". Progresijos režimas: "+("legendary".equals(s.progressionMode)?"legendinis, 92 bazinės savybės pasiekusios ribą":"subalansuotas, bazinės savybės prasideda nuo 40, o profilis ir meistriškumas taikomi atskirai")+"; veikėjo lygis "+s.level+", patirtis "+s.experience+"/"+s.experienceNext+", sunkumas "+s.difficulty+". Meistriškumas yra atskira naudojamų savybių pažanga.\n"
                +"Vieta: "+s.location+". Metai: "+s.worldYear+". Pasaulio minutė: "+s.worldMinute+".\n"
                +"Ištekliai: gyvybė "+s.hp+"/"+s.hpMax+", mana "+s.mana+"/"+s.manaMax+", ištvermė "+s.stamina+"/"+s.staminaMax+", eoninė energija "+s.aeonic+"/"+s.aeonicMax+", karūnos "+s.crowns+".\n"
                +"Kova: "+(s.combatActive?(s.enemyName+" · gyvybė "+s.enemyHp+"/"+s.enemyHpMax+" · ėjimas "+s.combatRound):"neaktyvi")+".\n"
                +"Siužetas: "+s.questTitle+". Dabartinis tikslas: "+s.objective+"\n"
                +"Frakcijos: Asterra "+s.asterraInfluence+" ("+s.asterraRelation+"), Dravenn "+s.dravennInfluence+" ("+s.dravennRelation+"), Lysara "+s.lysaraInfluence+" ("+s.lysaraRelation+").\n"
                +"Dėvima įranga: "+(equipped==null?"nėra":equipped)+"\nGebėjimai: "+(abilityNames.length()==0?"nėra":abilityNames)+"\n"
                +"Struktūrizuota pasaulio atmintis: "+(worldContext==null||worldContext.isEmpty()?"nėra papildomų įrašų":worldContext)+"\n"
                +"Dabartinė scena: "+s.scene+"\nPaskutiniai ėjimai: "+recent+"\n\n"
                +"ŽAIDĖJO VEIKSMAS: "+(action==null?"":action)+"\n\n"+(check==null?"":check.prompt());
    }

    private NarrativeTurn() {}
}
