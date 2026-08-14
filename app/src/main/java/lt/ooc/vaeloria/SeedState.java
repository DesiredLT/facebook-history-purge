package lt.ooc.vaeloria;

import org.json.JSONArray;
import org.json.JSONObject;

public final class SeedState {
    private SeedState() {}

    public static JSONObject create() {
        try {
            JSONObject s = new JSONObject();
            s.put("save_version", 1);
            s.put("character", new JSONObject()
                    .put("name", "Einoras")
                    .put("chronological_age", 201)
                    .put("biological_age", 20)
                    .put("biological_agelessness", true)
                    .put("role", "independent sovereign-scale strategic actor")
                    .put("base_stats", "92/92 baziniai stat'ai = 100/100")
                    .put("post_cap_mastery", "century_perfected_plus_thirty_year_peer_refinement"));
            s.put("resources", new JSONObject()
                    .put("hp", 100).put("hp_max", 100)
                    .put("mana", 0).put("mana_max", 100)
                    .put("stamina", 100).put("stamina_max", 100)
                    .put("aeonic", 180).put("aeonic_max", 900));
            s.put("crowns", 1062400);
            s.put("world", new JSONObject()
                    .put("era_year", 923)
                    .put("world_minute", 95358680L)
                    .put("day", 66222)
                    .put("hour", 9)
                    .put("minute", 20)
                    .put("location", "Luminara")
                    .put("danger", 3));

            JSONArray abilities = new JSONArray();
            addAbility(abilities,"Aeonic Bastion","Autonomous layered physical, elemental, arcane, spatial and hostile-transmutation defenses.");
            addAbility(abilities,"Continuity Lattice","Identity and neural continuity anchors permit recovery if a coherent anchor survives.");
            addAbility(abilities,"Catastrophic Regeneration","Rebuilds extreme trauma from surviving structure, mana and time; not instant.");
            addAbility(abilities,"Null-Adaptive Physiology","Legendary physical body remains functional in anti-magic while magical layers are suppressed.");
            addAbility(abilities,"Reflexive Spatial Evasion","Automatic displacement, vector redirection and partial shunting under threat.");
            addAbility(abilities,"Adaptive Counterweaving","Defenses adapt after exposure to hostile principles.");
            addAbility(abilities,"Relic Symbiosis","Coordinates synchronized relics subject to resonance bandwidth.");
            addAbility(abilities,"Temporal Parallax Discrimination","Distinguishes local continuity from cross-branch timing echoes after observation.");
            addAbility(abilities,"Unknown-Rule Calibration","Forms safer provisional models faster after first contact with unfamiliar rules.");
            addAbility(abilities,"Decentered Mastery","Permits competent peers to override Einoras decisions without reducing coordination.");
            addAbility(abilities,"Conditional Causality Framing","Restructures some magic into anchored condition-to-consequence bindings.");
            addAbility(abilities,"Concordance Adapter Framing","Designs provisional adapters translating understood causal forms into sandbox-compatible representations.");
            s.put("abilities", abilities);

            JSONArray inv = new JSONArray();
            addItem(inv,"Asterion Edge","weapon","legendary",true,"primary_weapon");
            addItem(inv,"Sevenfold Mantle","armor_system","legendary",true,"armor_system");
            addItem(inv,"Resonance Signet","major_relic","legendary",true,"");
            addItem(inv,"Wayfold Satchel","utility_item","rare",false,"utility");
            addItem(inv,"Starfall Compass","artifact","ancient",false,"");
            addItem(inv,"Heart of Still Thunder","artifact","ancient",false,"");
            addItem(inv,"Nullglass Prism","major_relic","legendary",true,"");
            addItem(inv,"Living Rune Seed","artifact","ancient",false,"");
            addItem(inv,"Meridian Key","major_relic","legendary",true,"");
            addItem(inv,"Dragonwake Concord Scale","major_relic","legendary",true,"");
            addItem(inv,"Orison Astrolabe","artifact","ancient",false,"");
            addItem(inv,"Arkforge Seed","anchored_artifact","ancient",false,"");
            addItem(inv,"Triune Concordance Seal","credential","unique",false,"");
            s.put("inventory", inv);

            JSONObject quest = new JSONObject()
                    .put("title", "The Broken Meridian")
                    .put("type", "main_arc")
                    .put("status", "active")
                    .put("stakes", "Sukurti fiziškai stabilų, politiškai teisėtą ir paprastiems žmonėms naudojamą maršrutų tinklą, nepaverčiant Concordance monopoliu.")
                    .put("starting_hook", "Karavano manifestas pasiekė Luminarą anksčiau už jį gabenusį karavaną.");
            JSONArray objectives = new JSONArray();
            objectives.put(objective("Ištirti pirmą naują Waygate Drift incidentą ir atskirti faktus nuo prielaidų.","active"));
            objectives.put(objective("Užtikrinti tris nepriklausomai prižiūrimus maršruto inkarus.","locked"));
            objectives.put(objective("Atlikti kontroliuojamą perėjimą į Orison koridorių ir grįžti su patikrinamais kontakto duomenimis.","locked"));
            objectives.put(objective("Suderėti First Meridian Charter arba sąmoningai pasirinkti kitą pagrįstą modelį.","locked"));
            quest.put("objectives", objectives);
            s.put("quest", quest);

            JSONArray threads = new JSONArray();
            threads.put(thread("Open Horizons: The Broken Meridian",96,58,82));
            threads.put(thread("Orison Contact Protocol",90,55,85));
            threads.put(thread("Waygate Drift",82,50,78));
            threads.put(thread("Concordance Stewardship Governance",76,62,38));
            threads.put(thread("Draconic Succession",74,46,52));
            s.put("threads", threads);

            JSONArray consequences = new JSONArray();
            consequences.put("Asterian, Kharad Vorn ir Meridian institucijos reikalaus formalaus ribotos Concordance prieigos proceso.");
            consequences.put("Archyvų sutikrinimas atskleis miegančias Meridian priežiūros vietas, kurių turinys ir lojalumas nežinomi.");
            consequences.put("Mira ir Kaelis patirs didesnį verbavimo, patronažo ir politinio spaudimo srautą.");
            s.put("pending_consequences", consequences);

            JSONArray locations = new JSONArray();
            String[] names={"Luminara","Veyrhold","Aureliono Pakraštys","Asterio Karūna","Žvaigždėkritos Skilautas","Stiklo Giria","Tuščiavidurė Smailė","Kharad Vorn","Drakono Pabudimo Viršūnės","Pelenų Karūnos Citadelė","Safyro Plynatės","Amžinojo Šaltinio Slėnis","Šventųjų Pelkynas","Žaliasis Labirintas"};
            for(String n:names) locations.put(n);
            s.put("known_locations", locations);

            s.put("scene", new JSONObject()
                    .put("title", "Vėlyvieji keliai")
                    .put("location", "Luminara")
                    .put("type", "investigation")
                    .put("danger", 3)
                    .put("text", "Luminaros Meridian registras pažymėjo neatitikimą: karavano manifestas jau patvirtintas mieste, tačiau pats karavanas dar neatvyko. Tai pirmas šviežias Waygate Drift incidentas, o prieš priimant bet kokią hipotezę reikia lauko įrodymų.")
                    .put("choices", new JSONArray()
                            .put(choice("Patikrinti manifestą ir jo laiko žymas", "Kruopščiai patikrinu manifestą, jo registravimo laiką, parašus ir visus galimus neatitikimus."))
                            .put(choice("Vykti prie Waygate ir ieškoti fizinių pėdsakų", "Vykstu prie susijusio Waygate ir pirmiausia renku fizinius bei maginius pėdsakus, nedarydamas išankstinių išvadų."))
                            .put(choice("Surasti paskutinius karavano liudininkus", "Surandu žmones, kurie paskutiniai matė karavaną prieš jo dingimą, ir atskirai sutikrinu jų parodymus."))));
            s.put("journal", new JSONArray().put(new JSONObject()
                    .put("world_minute",95358680L)
                    .put("entry","Checkpoint importuotas iš kanoninės Vaeloria būsenos. The Broken Meridian prasideda Luminara.")));
            return s;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static void addAbility(JSONArray a,String name,String desc) throws Exception { a.put(new JSONObject().put("name",name).put("description",desc).put("status","active")); }
    private static void addItem(JSONArray a,String name,String type,String rarity,boolean synced,String slot) throws Exception { a.put(new JSONObject().put("name",name).put("type",type).put("rarity",rarity).put("resonance_synced",synced).put("equipped_slot",slot)); }
    private static JSONObject objective(String d,String status) throws Exception { return new JSONObject().put("description",d).put("status",status); }
    private static JSONObject thread(String title,int priority,int heat,int mystery) throws Exception { return new JSONObject().put("title",title).put("priority",priority).put("heat",heat).put("mystery",mystery); }
    private static JSONObject choice(String label,String action) throws Exception { return new JSONObject().put("label",label).put("action",action); }
}
