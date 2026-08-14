package lt.vaeloria.ooc;

public final class BaseStatCatalog {
    public static final class Group {
        public final String name;
        public final String[] stats;
        public Group(String name, String... stats) { this.name = name; this.stats = stats; }
    }

    public static final Group[] GROUPS = new Group[]{
        new Group("KŪNO SAVYBĖS",
            "Jėga", "Sprogstamoji jėga", "Raumenų ištvermė", "Širdies ir kvėpavimo ištvermė",
            "Greitis", "Pagreitis", "Vikrumas", "Lankstumas", "Pusiausvyra", "Koordinacija",
            "Kūno kontrolė", "Suėmimo jėga", "Kaulų tvirtumas", "Atsparumas traumoms"),

        new Group("JUDĖJIMAS IR REFLEKSAI",
            "Reakcijos greitis", "Refleksai", "Kojų darbas", "Šuolio galia", "Kritimo kontrolė",
            "Erdvinė orientacija", "Judesių tikslumas", "Krypties keitimo greitis", "Plaukimas", "Laipiojimas"),

        new Group("KOVOS MEISTRIŠKUMAS",
            "Beginklė kova", "Smūgiavimo technika", "Imtynės", "Kova parteryje", "Ginklų valdymas",
            "Kardo meistriškumas", "Durklų meistriškumas", "Ieties meistriškumas", "Lanko meistriškumas",
            "Skydo valdymas", "Gynyba", "Atakos tikslumas", "Kovinis laiko parinkimas", "Kovinė nuojauta",
            "Kelių priešininkų kontrolė", "Taktinis prisitaikymas"),

        new Group("JUTIMAI IR IŠGYVENIMAS",
            "Regėjimas", "Klausa", "Uoslė", "Lytėjimo jautrumas", "Pavojaus nuojauta", "Sekimas",
            "Orientavimasis vietovėje", "Išgyvenimas laukinėje gamtoje", "Slėpimasis", "Pastabumas"),

        new Group("PROTINĖS SAVYBĖS",
            "Intelektas", "Atmintis", "Mokymosi greitis", "Loginis mąstymas", "Analitinis mąstymas",
            "Kūrybiškumas", "Susikaupimas", "Valia", "Psichologinis atsparumas", "Sprendimų greitis",
            "Planavimas", "Strateginis mąstymas"),

        new Group("MAGINĖS SAVYBĖS",
            "Manos talpa", "Manos atkūrimas", "Manos kontrolė", "Burtų galia", "Burtų tikslumas",
            "Burtų greitis", "Burtų stabilumas", "Burtų efektyvumas", "Elementų valdymas", "Apsauginė magija",
            "Gydomoji magija", "Erdvinė magija", "Laiko magija", "Transmutacija", "Užkeikimų ardymas",
            "Magijos jutimas", "Atsparumas magijai", "Relikvijų rezonansas"),

        new Group("SOCIALINĖS IR PRAKTINĖS SAVYBĖS",
            "Charizma", "Įtikinėjimas", "Derybos", "Vadovavimas", "Empatija", "Žmonių perpratimas",
            "Apgaulės atpažinimas", "Apgaulė", "Bauginimas", "Diplomatija", "Amatų meistriškumas", "Žinių pritaikymas")
    };

    public static int totalCount() {
        int n = 0;
        for (Group g : GROUPS) n += g.stats.length;
        return n;
    }

    private BaseStatCatalog() {}
}
