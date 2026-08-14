package lt.vaeloria.ooc;

public final class AbilityMechanics {
    public static String displayName(String n){
        if(n==null)return"Gebėjimas";
        switch(n){
            case"Tęstinumo Gardelė":return"Tęstinumo Tinklas";
            case"Nuliui Prisitaikanti Fiziologija":return"Nulinės Aplinkos Prisitaikymas";
            case"Refleksinis Erdvinis Išsisukimas":return"Erdvinis Refleksas";
            case"Laiko Paralakso Atskyrimas":return"Laiko Šakų Atskyrimas";
            case"Nežinomų Taisyklių Kalibravimas":return"Nežinomų Dėsnių Kalibravimas";
            case"Pasidalytas Meistriškumas":return"Paskirstytas Meistriškumas";
            case"Sąlyginio Priežastingumo Struktūra":return"Sąlyginis Priežastingumas";
            case"Santarvės Adapterio Struktūra":return"Santarvės Derintuvas";
            default:return n;
        }
    }

    public static String mechanic(String n){
        if(n==null)return"Situacinis poveikis sprendžiamas pagal sceną.";
        switch(n){
            case"Eoninis Bastionas":return"Gynybos patikroms iki +10, kai aktyvi daugiasluoksnė apsauga.";
            case"Tęstinumo Gardelė":return"Mirtinas vietinis pažeidimas nebūtinai nutraukia tęstinumą, jei išlieka atrama; atsikūrimas nėra nemokamas ar momentinis.";
            case"Katastrofinė Regeneracija":return"Leidžia atkurti ekstremalius sužalojimus per laiką ir išteklius; neanuliuoja žalos tame pačiame ėjime.";
            case"Nuliui Prisitaikanti Fiziologija":return"Mažina antimaginės ir nulinės aplinkos funkcinius apribojimus; nepadaro imuniteto nežinomoms taisyklėms.";
            case"Refleksinis Erdvinis Išsisukimas":return"Refleksų ir vikrumo patikroms iki +10, kai grėsmė leidžia erdvinį poslinkį.";
            case"Prisitaikantis Kontrapynimas":return"Po kontakto su tuo pačiu priešišku principu vėlesnė gynyba gali gauti situacinį pranašumą.";
            case"Relikvijų Simbiozė":return"Relikvijų rezonanso patikroms iki +12, kai naudojamos susietos relikvijos.";
            case"Laiko Paralakso Atskyrimas":return"Laiko magijos ir laiko anomalijų analizės patikroms iki +10 po pakankamo stebėjimo.";
            case"Nežinomų Taisyklių Kalibravimas":return"Nežinomų reiškinių analizės ar magijos jutimo patikroms iki +8 po pirmojo kontakto.";
            case"Pasidalytas Meistriškumas":return"Leidžia perduoti dalį sprendimo sąjungininkui neprarandant koordinacijos; poveikis priklauso nuo jo kompetencijos.";
            case"Sąlyginio Priežastingumo Struktūra":return"Leidžia kurti sąlyga→pasekmė ryšius, jei pasaulio taisyklės juos priima; DI žaidimo meistras privalo taikyti kainą ir ribas.";
            case"Santarvės Adapterio Struktūra":return"Leidžia laikinai suderinti jau suprastas priežastines formas; neveikia nežinomos sistemos be kalibravimo.";
            default:return"Situacinis poveikis virš bazinės ribos; žaidimo variklis ir žaidimo meistras jį taiko tik tada, kai sąlygos pagrįstos.";
        }
    }

    public static String tag(String type){
        if(type==null)return"VIRŠ RIBOS";
        if(type.contains("principas"))return"PRINCIPAS";
        if(type.contains("technika"))return"TECHNIKA";
        if(type.contains("tobulinimas"))return"TOBULINIMAS";
        return"GEBĖJIMAS";
    }
    private AbilityMechanics(){}
}
