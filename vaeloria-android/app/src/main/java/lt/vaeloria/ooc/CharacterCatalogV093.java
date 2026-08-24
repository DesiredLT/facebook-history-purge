package lt.vaeloria.ooc;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/** v0.9.3 character origins, archetypes and mechanically active personality traits. */
final class CharacterCatalogV093 {
    static final class Origin {
        final String id, name, description, benefit;
        final String[] groups, stats;
        Origin(String id, String name, String description, String benefit, String[] groups, String[] stats) {
            this.id=id;this.name=name;this.description=description;this.benefit=benefit;this.groups=groups;this.stats=stats;
        }
    }

    static final class Archetype {
        final String id, name, description, benefit;
        final String[] groups, stats;
        Archetype(String id, String name, String description, String benefit, String[] groups, String[] stats) {
            this.id=id;this.name=name;this.description=description;this.benefit=benefit;this.groups=groups;this.stats=stats;
        }
    }

    static final class Trait {
        final String id, name, description, benefit, drawback;
        final String[] positiveStats, negativeStats;
        final int positive, negative;
        Trait(String id, String name, String description, String benefit, String drawback,
              int positive, String[] positiveStats, int negative, String[] negativeStats) {
            this.id=id;this.name=name;this.description=description;this.benefit=benefit;this.drawback=drawback;
            this.positive=positive;this.positiveStats=positiveStats;this.negative=negative;this.negativeStats=negativeStats;
        }
    }

    static final class Effect {
        final int value;
        final String explanation;
        Effect(int value,String explanation){this.value=value;this.explanation=explanation;}
    }

    static final Origin[] ORIGINS = {
            new Origin("luminara", "Luminara pilietis",
                    "Užaugai tarp Meridiano vartų, gildijų ir miesto politikos.",
                    "+3 socialinėms ir praktinėms patikroms.",
                    new String[]{"SOCIALINĖS IR PRAKTINĖS SAVYBĖS"}, new String[]{}),
            new Origin("pasienis", "Pasienio klajūnas",
                    "Pažįsti atokius kelius, prastą orą ir nepažymėtus pavojus.",
                    "+3 jutimų ir išgyvenimo patikroms.",
                    new String[]{"JUTIMAI IR IŠGYVENIMAS"}, new String[]{}),
            new Origin("akademija", "Didžiosios akademijos auklėtinis",
                    "Mokeisi teorijos, runų ir saugaus maginės galios valdymo.",
                    "+3 maginėms patikroms.",
                    new String[]{"MAGINĖS SAVYBĖS"}, new String[]{}),
            new Origin("gildija", "Gildijos amatininkas",
                    "Tavo vardą išugdė dirbtuvės, sutartys ir atsakomybė už darbo kokybę.",
                    "+3 amatams, deryboms ir žinių pritaikymui.",
                    new String[]{}, new String[]{"Amatų meistriškumas","Derybos","Žinių pritaikymas"}),
            new Origin("pelkynai", "Šventųjų pelkynų vaikas",
                    "Moki skaityti gyvą kraštovaizdį ir išlikti ten, kur kelias išnyksta.",
                    "+3 sekimui, išgyvenimui ir pavojaus nuojautai.",
                    new String[]{}, new String[]{"Sekimas","Išgyvenimas laukinėje gamtoje","Pavojaus nuojauta"}),
            new Origin("dravenn", "Dravenn tremtinys",
                    "Išgyvenai griežtą tvarką, kovinį spaudimą ir pavojingą politinę kainą.",
                    "+3 kovos meistriškumo patikroms.",
                    new String[]{"KOVOS MEISTRIŠKUMAS"}, new String[]{})
    };

    static final Archetype[] ARCHETYPES = {
            new Archetype("sargybinis", "Sargybinis",
                    "Laikai poziciją, saugai kitus ir valdai kovos tempą.",
                    "+5 gynybai, skydui, valiai ir atsparumui traumoms.",
                    new String[]{}, new String[]{"Gynyba","Skydo valdymas","Valia","Atsparumas traumoms"}),
            new Archetype("zvalgas", "Žvalgas",
                    "Pirmas pastebi grėsmę, randa kelią ir renkasi palankią poziciją.",
                    "+5 pastabumui, sekimui, slėpimuisi ir orientavimuisi.",
                    new String[]{}, new String[]{"Pastabumas","Sekimas","Slėpimasis","Orientavimasis vietovėje"}),
            new Archetype("arkanistas", "Arkanistas",
                    "Tyrinėji magijos sandarą ir saugiai valdai nestabilią energiją.",
                    "+5 manos kontrolei, magijos jutimui, burtų stabilumui ir relikvijų rezonansui.",
                    new String[]{}, new String[]{"Manos kontrolė","Magijos jutimas","Burtų stabilumas","Relikvijų rezonansas"}),
            new Archetype("diplomatas", "Diplomatas",
                    "Klausaisi interesų, deriesi ir ieškai sprendimo, kuris išliktų po pokalbio.",
                    "+5 diplomatijai, deryboms, įtikinėjimui ir žmonių perpratimui.",
                    new String[]{}, new String[]{"Diplomatija","Derybos","Įtikinėjimas","Žmonių perpratimas"}),
            new Archetype("amatininkas", "Amatininkas",
                    "Problemas sprendi žiniomis, įrankiais ir kruopščiai patikrintu darbu.",
                    "+5 amatams, analitiniam mąstymui, planavimui ir žinių pritaikymui.",
                    new String[]{}, new String[]{"Amatų meistriškumas","Analitinis mąstymas","Planavimas","Žinių pritaikymas"}),
            new Archetype("klajunas", "Klajūnas",
                    "Prisitaikai prie vietovės, taupai jėgas ir pasikliauji savo sprendimu.",
                    "+5 išgyvenimui, ištvermei, pusiausvyrai ir pavojaus nuojautai.",
                    new String[]{}, new String[]{"Išgyvenimas laukinėje gamtoje","Širdies ir kvėpavimo ištvermė","Pusiausvyra","Pavojaus nuojauta"})
    };

    static final Trait[] TRAITS = {
            new Trait("ryztas", "Ryžtas", "Priimtą sprendimą gini net spaudžiamas.",
                    "+6 valiai, psichologiniam atsparumui ir bauginimui.",
                    "−3 empatijai ir deryboms.", 6,
                    new String[]{"Valia","Psichologinis atsparumas","Bauginimas"}, -3,
                    new String[]{"Empatija","Derybos"}),
            new Trait("pastabumas", "Pastabumas", "Pastebi mažus neatitikimus ir kūno kalbos ženklus.",
                    "+6 pastabumui, regėjimui ir apgaulės atpažinimui.",
                    "−3 sprogstamajai jėgai ir smūgiavimo technikai.", 6,
                    new String[]{"Pastabumas","Regėjimas","Apgaulės atpažinimas"}, -3,
                    new String[]{"Sprogstamoji jėga","Smūgiavimo technika"}),
            new Trait("atsargumas", "Atsargumas", "Pirmiausia ieškai rizikos ir atsitraukimo kelio.",
                    "+5 pavojaus nuojautai, gynybai ir planavimui.",
                    "−4 pagreičiui ir sprendimų greičiui.", 5,
                    new String[]{"Pavojaus nuojauta","Gynyba","Planavimas"}, -4,
                    new String[]{"Pagreitis","Sprendimų greitis"}),
            new Trait("karstakraujiskumas", "Karštakraujiškumas", "Veiki staigiai ir spaudi varžovą nepalikdamas jam ramybės.",
                    "+7 smūgiavimo technikai, bauginimui ir sprogstamajai jėgai.",
                    "−5 diplomatijai, planavimui ir susikaupimui.", 7,
                    new String[]{"Smūgiavimo technika","Bauginimas","Sprogstamoji jėga"}, -5,
                    new String[]{"Diplomatija","Planavimas","Susikaupimas"}),
            new Trait("atjauta", "Atjauta", "Lengvai atpažįsti skausmą ir kuri pasitikėjimą.",
                    "+7 empatijai, žmonių perpratimui ir gydomajai magijai.",
                    "−4 apgaulei ir bauginimui.", 7,
                    new String[]{"Empatija","Žmonių perpratimas","Gydomoji magija"}, -4,
                    new String[]{"Apgaulė","Bauginimas"}),
            new Trait("gudrumas", "Gudrumas", "Randi netiesioginį kelią ir greitai keiti planą.",
                    "+6 apgaulei, taktiniam prisitaikymui ir strateginiam mąstymui.",
                    "−3 jėgai ir kaulų tvirtumui.", 6,
                    new String[]{"Apgaulė","Taktinis prisitaikymas","Strateginis mąstymas"}, -3,
                    new String[]{"Jėga","Kaulų tvirtumas"}),
            new Trait("istverme", "Ištvermė", "Ilgai išlaikai pastangas ir nesutrinki nuo skausmo.",
                    "+6 abiem ištvermės savybėms ir atsparumui traumoms.",
                    "−3 greičiui ir pagreičiui.", 6,
                    new String[]{"Raumenų ištvermė","Širdies ir kvėpavimo ištvermė","Atsparumas traumoms"}, -3,
                    new String[]{"Greitis","Pagreitis"}),
            new Trait("vikrumas", "Vikrumas", "Lengvai keiti kūno padėtį ir išnaudoji mažą tarpą.",
                    "+6 refleksams, vikrumui ir slėpimuisi.",
                    "−3 jėgai ir suėmimo jėgai.", 6,
                    new String[]{"Refleksai","Vikrumas","Slėpimasis"}, -3,
                    new String[]{"Jėga","Suėmimo jėga"}),
            new Trait("smalsumas", "Smalsumas", "Noriai tikrini nepažįstamą reiškinį ir ieškai jo taisyklės.",
                    "+6 intelektui, analitiniam mąstymui ir magijos jutimui.",
                    "−3 pavojaus nuojautai ir gynybai.", 6,
                    new String[]{"Intelektas","Analitinis mąstymas","Magijos jutimas"}, -3,
                    new String[]{"Pavojaus nuojauta","Gynyba"}),
            new Trait("charizma", "Charizma", "Tavo kalba sutelkia dėmesį ir įkvepia sekti paskui.",
                    "+6 charizmai, įtikinėjimui ir vadovavimui.",
                    "−3 slėpimuisi ir apgaulės atpažinimui.", 6,
                    new String[]{"Charizma","Įtikinėjimas","Vadovavimas"}, -3,
                    new String[]{"Slėpimasis","Apgaulės atpažinimas"}),
            new Trait("drausme", "Drausmė", "Kartojimu ir taisykle išlaikai tikslią veiksmų seką.",
                    "+5 susikaupimui, ginklų valdymui ir manos kontrolei.",
                    "−3 kūrybiškumui ir taktiniam prisitaikymui.", 5,
                    new String[]{"Susikaupimas","Ginklų valdymas","Manos kontrolė"}, -3,
                    new String[]{"Kūrybiškumas","Taktinis prisitaikymas"}),
            new Trait("nepriklausomybe", "Nepriklausomybė", "Pasitiki savo sprendimu ir moki veikti be paramos.",
                    "+5 išgyvenimui, valiai ir orientavimuisi vietovėje.",
                    "−3 vadovavimui ir diplomatijai.", 5,
                    new String[]{"Išgyvenimas laukinėje gamtoje","Valia","Orientavimasis vietovėje"}, -3,
                    new String[]{"Vadovavimas","Diplomatija"})
    };

    static Origin origin(String id){for(Origin value:ORIGINS)if(value.id.equals(id))return value;return null;}
    static Archetype archetype(String id){for(Archetype value:ARCHETYPES)if(value.id.equals(id))return value;return null;}
    static Trait trait(String id){for(Trait value:TRAITS)if(value.id.equals(id))return value;return null;}

    static String originName(String id){Origin value=origin(id);return value==null?"Nepasirinkta":value.name;}
    static String archetypeName(String id){Archetype value=archetype(id);return value==null?"Nepasirinktas":value.name;}
    static String identityName(String id){
        if("moteris".equals(id))return "Moteris";
        if("vyras".equals(id))return "Vyras";
        return "Nenurodyta";
    }

    static ArrayList<String> normalizedTraits(List<String> raw){
        LinkedHashSet<String> unique=new LinkedHashSet<>();
        if(raw!=null)for(String id:raw)if(trait(id)!=null)unique.add(id);
        return new ArrayList<>(unique);
    }

    static boolean validProfile(String name,String identity,String originId,String archetypeId,List<String> traits){
        String clean=name==null?"":name.trim().replaceAll("\\s+"," ");
        boolean identityValid="vyras".equals(identity)||"moteris".equals(identity)||"nenurodyta".equals(identity);
        return clean.length()>=2&&clean.length()<=32&&identityValid&&origin(originId)!=null&&archetype(archetypeId)!=null&&normalizedTraits(traits).size()==3;
    }

    static Effect effect(GameState state,String stat){
        if(state==null||!state.characterCreated)return new Effect(0,"");
        int value=0;ArrayList<String> sources=new ArrayList<>();
        Origin origin=origin(state.characterOriginId);
        if(origin!=null&&matches(stat,origin.groups,origin.stats)){value+=3;sources.add(origin.name+" +3");}
        Archetype archetype=archetype(state.characterArchetypeId);
        if(archetype!=null&&matches(stat,archetype.groups,archetype.stats)){value+=5;sources.add(archetype.name+" +5");}
        for(String id:normalizedTraits(state.characterTraitIds)){
            Trait trait=trait(id);if(trait==null)continue;
            if(contains(trait.positiveStats,stat)){value+=trait.positive;sources.add(trait.name+" +"+trait.positive);}
            else if(contains(trait.negativeStats,stat)){value+=trait.negative;sources.add(trait.name+" "+trait.negative);}
        }
        value=Math.max(-15,Math.min(20,value));
        return new Effect(value,String.join(", ",sources));
    }

    /**
     * Grąžina tik v1.0.0 subalansuoto profilio kūrimo metu į bazines savybes
     * klaidingai įrašytą premiją. v1.0.1 migracija ją pašalina, nes tos pačios
     * kilmės ir archetipo premijos autoritetingai taikomos {@link #effect}.
     */
    static int legacyPersistedBonus(String originId,String archetypeId,String stat,String group){
        int bonus=0;
        Origin origin=origin(originId);
        if(origin!=null){
            if(contains(origin.groups,group))bonus+=4;
            if(contains(origin.stats,stat))bonus+=4;
        }
        Archetype archetype=archetype(archetypeId);
        if(archetype!=null){
            if(contains(archetype.groups,group))bonus+=8;
            if(contains(archetype.stats,stat))bonus+=8;
        }
        return bonus;
    }

    static String traitNames(List<String> ids){
        ArrayList<String> names=new ArrayList<>();for(String id:normalizedTraits(ids)){Trait t=trait(id);if(t!=null)names.add(t.name);}return names.isEmpty()?"nepasirinkti":String.join(", ",names);
    }

    static String profilePrompt(GameState state){
        if(state==null)return "Veikėjo profilis nepateiktas.";
        String appearance=state.characterAppearance==null||state.characterAppearance.trim().isEmpty()?"neaprašyta":state.characterAppearance.trim();
        return "Vardas: "+state.characterName+". Tapatybė: "+identityName(state.characterIdentity)+". "
                +"Kilmė: "+originName(state.characterOriginId)+". Archetipas: "+archetypeName(state.characterArchetypeId)+". "
                +"Bruožai: "+traitNames(state.characterTraitIds)+". Išvaizda: "+appearance+".";
    }

    static String ageLine(GameState state){
        if(state==null)return "Amžius nenurodytas";
        if(state.ageless)return state.chronologicalAge+" m. chronologinis · "+state.biologicalAge+" m. biologinis · biologinis amžius nekinta";
        return state.chronologicalAge+" m. amžiaus";
    }

    private static boolean matches(String stat,String[] groups,String[] stats){
        if(contains(stats,stat))return true;
        for(String group:groups)if(inGroup(stat,group))return true;
        return false;
    }

    private static boolean inGroup(String stat,String group){
        if(stat==null)return false;
        for(BaseStatCatalog.Group candidate:BaseStatCatalog.GROUPS){
            if(!candidate.name.equals(group))continue;
            for(String name:candidate.stats)if(name.equals(stat))return true;
        }
        return false;
    }

    private static boolean contains(String[] values,String expected){
        if(values==null||expected==null)return false;
        for(String value:values)if(expected.equals(value))return true;
        return false;
    }

    private CharacterCatalogV093(){}
}
