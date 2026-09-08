package lt.vaeloria.ooc;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Vaeloria v1.1.0 premium mobile presentation over the persistent RPG systems. */
public class PolishedActivity extends PremiumActivity {
    private boolean onlyFavoriteInventory;

    private static final int LINE = Color.rgb(43, 59, 68);
    private static final int PANEL = Color.rgb(9, 19, 27);
    private static final int PANEL_2 = Color.rgb(13, 25, 34);
    private static final int BLUE = Color.rgb(91, 164, 207);
    private static final int GREEN = Color.rgb(95, 186, 139);
    private static final int PURPLE = Color.rgb(158, 117, 214);
    private String inventoryQuery="",inventoryRarity="all",inventorySort="rarity";
    private int inventoryPage=0;
    private static final String[][] ITEM_GROUPS_V092 = {
            {"GINKLAI", "weapon"}, {"SKYDAI IR FOKUSAI", "offhand"}, {"ŠALMAI IR GOBTUVAI", "head"},
            {"KRŪTINĖS ŠARVAI", "chest"}, {"PIRŠTINĖS", "hands"}, {"KELNĖS", "legs"},
            {"BATAI", "feet"}, {"DIRŽAI", "belt"}, {"ŽIEDAI", "ring"}, {"AMULETAI", "neck"},
            {"RELIKVIJOS", "relic"}, {"POTIONAI IR ELIKSYRAI", "potion"},
            {"KOVOS REIKMENYS", "combat_consumable"}, {"MAISTAS", "food"},
            {"MEDŽIAGOS", "material"}, {"ĮRANKIAI", "tool"}, {"NAUDINGI DAIKTAI", "utility"},
            {"UŽDUOČIŲ DAIKTAI", "quest"}, {"ARTEFAKTAI", "artifact"}
    };

    private static final String[][] NPCS_V090 = {
            {"Lyra Fen", "Meridiano lauko tyrėja", "Surenka patikrinamus vartų poslinkio matavimus ir nepasitiki patogiais paaiškinimais."},
            {"Kapitonas Kaelis", "Luminara sargybos vadas", "Saugo miestą, tikrina karavanų judėjimą ir vertina anomalijas kaip realią saugumo grėsmę."},
            {"Regentė Seraphine", "Luminara valdovė", "Bando išlaikyti miesto stabilumą, kai Meridiano krizė tampa politiniu ginklu."},
            {"Archyvaras Orinas", "Meridiano chartijų saugotojas", "Žino senas vartų taisykles ir tai, kurios jų buvo sąmoningai pamirštos."},
            {"Varekas", "Dravenn pasiuntinys", "Ieško būdo krizę paversti derybine ir karine persvara."},
            {"Mirel", "Gydytoja ir runų meistrė", "Tiria rezonanso traumas bei kuria praktines apsaugos priemones keliautojams."}
    };

    private static final String[][] CITY_NPCS_V090 = {
            {"Brynja Geležrankė", "Meistrė kalvė", "Kala ir taiso ginklus, vertina metalą pagal realų jo nuovargį, o ne legendą.", "ATVERTI KALVĘ", "Aplankyti Brynjos kalvę: apžiūrėti remontą, ginklų patobulinimus ir kainas pagal turimas kronas."},
            {"Tarenas Volas", "Šarvų meistras", "Pritaiko sluoksniuotus šarvus judėjimui ir nepriima užsakymo neįvertinęs kovos stiliaus.", "APŽIŪRĖTI ŠARVUS", "Paprašyti Tareno įvertinti mano šarvus ir pasiūlyti tik praktiškus patobulinimus su realia kaina."},
            {"Ysra Vey", "Runų amatininkė", "Graviruoja stabilias runas ir atsisako darbo, jei rezonanso rizika viršija naudą.", "TIRTI RUNAS", "Aptarti su Ysra saugias runas, galimus efektus, medžiagas ir nesėkmės riziką."},
            {"Elen Var", "Gydytoja ir vaistininkė", "Gydo žaizdas, ruošia eliksyrus ir skiria rezonanso nudegimą nuo paprasto išsekimo.", "GAUTI GYDYMĄ", "Kreiptis į Elen dėl apžiūros, gydymo ar eliksyrų; išskaičiuoti realią kainą tik jei paslauga suteikta."},
            {"Orenas Pelas", "Bendrasis prekybininkas", "Superka kelionių radinius, seka trūkumus ir keičia kainas pagal miesto pasiūlą.", "ATVERTI PREKYBĄ", "Peržiūrėti Oreno prekes, parduoti tinkamus radinius arba derėtis; nekeisti inventoriaus be aiškaus sandorio."},
            {"Mara Žibintė", "Užeigos šeimininkė", "Siūlo nakvynę, maistą ir informaciją, tačiau gandus aiškiai atskiria nuo patikrintų žinių.", "UŽEITI Į UŽEIGĄ", "Užeiti pas Marą: rinktis nakvynę, maistą arba klausti naujienų, aiškiai pažymint gandų patikimumą."},
            {"Sargė Ilyne", "Miesto teisės pareigūnė", "Prižiūri vartų tvarką, tiria pažeidimus ir žino, kurios miesto dalys uždarytos.", "KLAUSTI SARGYBOS", "Paklausti Ilyne apie miesto saugumą, galiojančius ribojimus ir atvirus tyrimus."},
            {"Darvenas Kelio", "Keliautojas", "Grįžo iš šiaurinių kelių ir gali palyginti oficialius žemėlapius su tuo, ką matė pats.", "KLAUSTI APIE KELIUS", "Palyginti Darveno kelionės liudijimą su turimu žemėlapiu ir pasižymėti tik patikimus maršrutus."},
            {"Bramas Akmenskeltis", "Statytojas ir mūrininkas", "Tvirtina sienas, tiltus bei dirbtuves ir tiksliai vertina darbų medžiagas bei laiką.", "APTARTI STATYBĄ", "Paklausti Bramo apie remontą ar statybą, gaunant medžiagų, laiko ir kainos įvertinimą."},
            {"Nesta Vale", "Arklidžių prižiūrėtoja", "Nuomoja žirgus, prižiūri pašarą ir neleidžia išvaryti pavargusio gyvūno į pavojingą kelią.", "ATVERTI ARKLIDES", "Aplankyti Nestos arklides: tikrinti žirgų būklę, nuomos kainą ir kelionei tinkamą pasirinkimą."},
            {"Emilis Rhyse", "Raštininkas ir tyrėjas", "Kopijuoja chartijas, tikrina antspaudus ir braižo Meridiano poslinkio diagramas.", "TIKRINTI ĮRAŠUS", "Paprašyti Emilio patikrinti dokumentą, archyvo įrašą arba Meridiano matavimų neatitikimą."},
            {"Korva Dain", "Gildijos tarpininkė", "Valdo sutartis, darbo pasiūlymus ir prekybinius ginčus tarp miesto cechų.", "ATVERTI GILDIJĄ", "Peržiūrėti Korvos siūlomas sutartis ir darbus; įsipareigojimą priimti tik aiškiai pasirinkus."}
    };

    private static final String[][] MONSTERS_V090 = {
            {"Meridiano vilkas", "LAUKINIAI", "5", "Šešiaakis plėšrūnas, kurio obsidianiniai nagai trumpam prapjauna erdvę."},
            {"Pelkių trolis", "LAUKINIAI", "4", "Masyvus pelkynų medžiotojas; lėtas, bet pavojingas siauroje ir klampioje vietoje."},
            {"Nuodų perų motina", "LAUKINIAI", "6", "Šarvuotas urvų voras, ginantis kiaušinius ir purškiantis Meridianu pakitusius nuodus."},
            {"Kristalų golemas", "LAUKINIAI", "7", "Runomis sužadinta akmens konstrukcija, atspindinti tiesioginius energijos smūgius."},
            {"Nakties harpija", "LAUKINIAI", "5", "Ore medžiojanti būtybė, mėginanti atskirti vieną taikinį nuo grupės."},
            {"Maitėdis drake'as", "LAUKINIAI", "6", "Kauliniais žandikauliais ginkluotas skraidantis roplys, sekantis šviežio mūšio pėdsakus."},
            {"Nuskendęs riteris", "ANOMALIJOS", "6", "Rūdijančiuose šarvuose įkalinta valia, iš kapų atsinešanti juodą vandenį."},
            {"Pelenų revenantas", "ANOMALIJOS", "7", "Sudegęs keršto pavidalas, byrantis pelenais ir vėl susirenkantis prie šilumos šaltinių."},
            {"Kaulų orakulas", "ANOMALIJOS", "7", "Miręs pranašas, skaitantis artimiausias baigtis iš ore kabančių kaulo šukių."},
            {"Tuštumos parazitas", "ANOMALIJOS", "8", "Beformė būtybė, kabinanti čiuptuvus prie magijos šaltinių ir siurbianti maną."},
            {"Maro kiautas", "ANOMALIJOS", "5", "Grybų kolonijos valdomas kūnas, skleidžiantis sporas uždaroje erdvėje."},
            {"Kapų kolosas", "ANOMALIJOS", "9", "Iš sarkofagų ir spektrinių grandinių sudėtas sargas, saugantis visą nekropolį."},
            {"Meridiano wyrmas", "PASAULIO BOSAI", "10", "Milžiniškas slibinas, kurio kūną skaido nestabilūs erdvės plyšiai."},
            {"Bekarūnis titanas", "PASAULIO BOSAI", "10", "Senovinis milžinas, nešantis grandinėmis prie krūtinės prirakintą žlugusios karalystės karūną."},
            {"Kraujšaknė Matriarchė", "PASAULIO BOSAI", "10", "Sąmoningas miškas viename kūne; šaknimis keičia mūšio lauką ir maitina save krauju."},
            {"Stiklo lichas", "PASAULIO BOSAI", "10", "Juodame kristale užsikonservavęs valdovas, skaldantis burtus į grįžtančias skeveldras."},
            {"Audros kolosas", "PASAULIO BOSAI", "10", "Kalno dydžio milžinas, kurio akmens oda sulaiko nuolatinę žaibų audrą."},
            {"Bedugnės šauklys", "PASAULIO BOSAI", "10", "Šarvuotas rifo pasiuntinys, atveriantis kovos lauką į raudonai juodą bedugnę."}
    };

    @Override void shell() {
        root = col();
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(2, 8, 13), Color.rgb(5, 14, 21), Color.rgb(2, 8, 13)}));
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            int top;
            int bottom;
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                top = bars.top;
                bottom = bars.bottom;
            } else {
                top = insets.getSystemWindowInsetTop();
                bottom = insets.getSystemWindowInsetBottom();
            }
            view.setPadding(0, top, 0, bottom);
            return insets;
        });

        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), 0, dp(6), 0);
        header.setBackgroundColor(Color.rgb(3, 10, 16));

        LinearLayout brand = col();
        TextView name = serif("VAELORIA", 23, GOLD2, true);
        name.setLetterSpacing(.06f);
        brand.addView(name);
        TextView version = txt("OOC · v" + BuildConfig.VERSION_NAME, 7, Color.rgb(112, 134, 142), true);
        version.setLetterSpacing(.11f);
        brand.addView(version);
        header.addView(brand, new LinearLayout.LayoutParams(0, dp(64), 1));

        LinearLayout location = col();
        location.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        TextView locationName = txt(state.location.toUpperCase(Locale.forLanguageTag("lt-LT")), 7, PARCH, true);
        locationName.setGravity(Gravity.END);
        locationName.setMaxLines(2);
        TextView year = txt("METAI " + state.worldYear, 7, SUB, false);
        year.setGravity(Gravity.END);
        location.addView(locationName);
        location.addView(year);
        header.addView(location, new LinearLayout.LayoutParams(dp(142), dp(64)));

        TextView settings = txt("⚙", 21, PARCH, false);
        settings.setGravity(Gravity.CENTER);
        settings.setContentDescription("Nustatymai");
        settings.setOnClickListener(view -> {
            haptic();
            show("settings");
        });
        header.addView(settings, new LinearLayout.LayoutParams(dp(48), dp(56)));
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(64)));

        View line = new View(this);
        line.setBackgroundColor(Color.rgb(26, 40, 49));
        root.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));
        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        nav = row();
        nav.setPadding(dp(4), dp(3), dp(4), dp(5));
        nav.setBackgroundColor(Color.rgb(3, 10, 16));
        root.addView(nav, new LinearLayout.LayoutParams(-1, dp(74)));
        setContentView(root);
        root.requestApplyInsets();
    }

    @Override void nav() {
        nav.removeAllViews();
        tab("game", "✦", "ŽAISTI");
        tab("hero", "♙", "VEIKĖJAS");
        tab("items", "◇", "DAIKTAI");
        tab("map", "⌖", "ŽEMĖLAPIS");
        tab("journal", "◆", "ŽURNALAS");
    }

    @Override void tab(String id, String glyph, String label) {
        LinearLayout item = col();
        item.setGravity(Gravity.CENTER);
        item.setContentDescription(label);
        boolean active = id.equals(screen);
        if (active) item.setBackground(round(Color.rgb(18, 30, 38), 15, Color.argb(150, 221, 187, 104)));
        TextView icon = txt(glyph, 20, active ? GOLD2 : Color.rgb(118, 139, 146), true);
        icon.setGravity(Gravity.CENTER);
        item.addView(icon, new LinearLayout.LayoutParams(-1, dp(29)));
        TextView text = txt(label, 8, active ? PARCH : Color.rgb(132, 150, 156), true);
        text.setGravity(Gravity.CENTER);
        item.addView(text);
        item.setOnClickListener(view -> {
            haptic();
            sound(false);
            show(id);
        });
        nav.addView(item, new LinearLayout.LayoutParams(0, -1, 1));
    }

    @Override void show(String id) {
        if(!allowScreen(id))return;
        if (!state.characterCreated && !"character".equals(id)) id = "character";
        screen = id;
        nav();
        content.removeAllViews();
        View view;
        try {
            if ("character".equals(id)) view = character();
            else if ("game".equals(id)) view = game();
            else if ("hero".equals(id)) view = hero();
            else if ("items".equals(id)) view = items();
            else if ("map".equals(id)) view = map();
            else if ("journal".equals(id)) view = journal();
            else view = settings();
        } catch (Throwable error) {
            view = errorView(error);
        }
        content.addView(view);
        updatePendingControls();
        if(audio!=null)audio.startAmbient(state.location);
        if (pref("animations", true)) {
            view.setAlpha(0);
            view.setTranslationY(dp(7));
            view.setScaleX(.99f);
            view.setScaleY(.99f);
            view.animate().alpha(1).translationY(0).scaleX(1f).scaleY(1f).setDuration(190).start();
        }
    }

    @Override View character() {
        final boolean editing = state.characterCreated;
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = col();
        column.setPadding(dp(10), dp(7), dp(10), dp(24));
        scroll.addView(column);

        LinearLayout intro = panel(true);
        intro.addView(section(editing ? "VEIKĖJO PROFILIS" : "NAUJAS VEIKĖJAS"));
        intro.addView(serif(editing ? "Keisk profilį ir bruožus" : "Sukurk savo veikėją", 24, PARCH, true));
        intro.addView(txt("Pasirinkimai išsaugomi telefone. Kilmė, archetipas ir lygiai trys bruožai realiai keičia savybių patikras; kiekvienas bruožas turi naudą ir kainą.", 10, SUB, false));
        column.addView(intro, sp(dp(9)));

        LinearLayout basics = panel(false);
        basics.addView(section("PAGRINDINIAI DUOMENYS"));
        EditText name = profileInput("Veikėjo vardas", editing ? state.characterName : "", false, 32);
        basics.addView(name, sp(dp(7)));
        EditText age = profileInput("Amžius (16–999)", String.valueOf(editing ? state.chronologicalAge : 25), false, 3);
        age.setInputType(InputType.TYPE_CLASS_NUMBER);
        basics.addView(age, sp(dp(5)));
        Switch ageless = new Switch(this);
        ageless.setText("Biologinis amžius nekinta");ageless.setTextColor(PARCH);ageless.setTextSize(11);ageless.setMinHeight(dp(48));
        ageless.setChecked(editing && state.ageless);basics.addView(ageless);
        column.addView(basics, sp(dp(9)));

        LinearLayout identityPanel = panel(false);
        identityPanel.addView(section("TAPATYBĖ"));
        identityPanel.addView(txt("Atsakymuose žaidimo meistras kreipsis „tu“, todėl sakiniai išliks natūralūs nepriklausomai nuo pasirinkimo.", 9, SUB, false), sp(dp(5)));
        RadioGroup identities = new RadioGroup(this);identities.setOrientation(RadioGroup.VERTICAL);
        String currentIdentity = editing ? state.characterIdentity : "nenurodyta";
        identities.addView(profileRadio("vyras", "Vyras", "", "vyras".equals(currentIdentity)));
        identities.addView(profileRadio("moteris", "Moteris", "", "moteris".equals(currentIdentity)));
        identities.addView(profileRadio("nenurodyta", "Nenurodyta", "", "nenurodyta".equals(currentIdentity)));
        identityPanel.addView(identities);column.addView(identityPanel, sp(dp(9)));

        LinearLayout originPanel = panel(false);originPanel.addView(section("KILMĖ · PASIRINK VIENĄ"));
        RadioGroup origins = new RadioGroup(this);origins.setOrientation(RadioGroup.VERTICAL);
        String currentOrigin = CharacterCatalogV093.origin(state.characterOriginId)==null ? "luminara" : state.characterOriginId;
        for(CharacterCatalogV093.Origin origin:CharacterCatalogV093.ORIGINS)
            origins.addView(profileRadio(origin.id,origin.name,origin.description+" "+origin.benefit,origin.id.equals(currentOrigin)));
        originPanel.addView(origins);column.addView(originPanel, sp(dp(9)));

        LinearLayout archetypePanel = panel(false);archetypePanel.addView(section("ARCHETIPAS · PASIRINK VIENĄ"));
        RadioGroup archetypes = new RadioGroup(this);archetypes.setOrientation(RadioGroup.VERTICAL);
        String currentArchetype = CharacterCatalogV093.archetype(state.characterArchetypeId)==null ? "sargybinis" : state.characterArchetypeId;
        for(CharacterCatalogV093.Archetype archetype:CharacterCatalogV093.ARCHETYPES)
            archetypes.addView(profileRadio(archetype.id,archetype.name,archetype.description+" "+archetype.benefit,archetype.id.equals(currentArchetype)));
        archetypePanel.addView(archetypes);column.addView(archetypePanel, sp(dp(9)));

        LinearLayout progressionPanel=panel(false);progressionPanel.addView(section("GALIOS IR PAŽANGOS REŽIMAS"));
        progressionPanel.addView(txt(editing?"Pažangos režimas pasirenkamas kuriant veikėją ir vėliau nekeičiamas.":"Subalansuotas režimas skirtas pilnai RPG kelionei nuo 1 lygio. Legendinis režimas palieka ankstesnę Einoro galią ir turtus.",9,SUB,false),sp(dp(5)));
        RadioGroup progressionModes=new RadioGroup(this);progressionModes.setOrientation(RadioGroup.VERTICAL);
        String currentMode=editing?state.progressionMode:"balanced";
        RadioButton balanced=profileRadio("balanced","Subalansuotas","1 lygis · 40 bazinės savybės · riboti ištekliai · pilna pažanga","balanced".equals(currentMode));
        RadioButton legendary=profileRadio("legendary","Legendinis","100 lygis · 100 bazinės savybės · senosios relikvijos · galios fantazija","legendary".equals(currentMode));
        balanced.setEnabled(!editing);legendary.setEnabled(!editing);progressionModes.addView(balanced);progressionModes.addView(legendary);progressionPanel.addView(progressionModes);column.addView(progressionPanel,sp(dp(9)));

        LinearLayout appearancePanel = panel(false);appearancePanel.addView(section("IŠVAIZDA · NEPRIVALOMA"));
        EditText appearance = profileInput("Trumpai aprašyk išvaizdą, aprangą ar išskirtinį ženklą…", editing ? state.characterAppearance : "", true, 140);
        appearance.setMinLines(2);appearance.setMaxLines(4);appearancePanel.addView(appearance);column.addView(appearancePanel, sp(dp(9)));

        LinearLayout traitPanel = panel(true);traitPanel.addView(section("CHARAKTERIO BRUOŽAI · PASIRINK LYGIAI 3"));
        TextView traitCount = txt("Pasirinkta 0 iš 3", 10, GOLD2, true);traitPanel.addView(traitCount, sp(dp(5)));
        ArrayList<CheckBox> traitBoxes = new ArrayList<>();
        for(CharacterCatalogV093.Trait trait:CharacterCatalogV093.TRAITS){
            CheckBox box=new CheckBox(this);box.setTag(trait.id);box.setText(trait.name+"\n"+trait.description+"\nNauda: "+trait.benefit+" Kaina: "+trait.drawback);
            box.setTextColor(PARCH);box.setTextSize(10);box.setMinHeight(dp(82));box.setPadding(dp(4),dp(4),dp(4),dp(4));
            box.setChecked(editing&&state.characterTraitIds.contains(trait.id));traitBoxes.add(box);traitPanel.addView(box);
        }
        Runnable updateCount=()->traitCount.setText("Pasirinkta "+checkedTraits(traitBoxes).size()+" iš 3");
        for(CheckBox box:traitBoxes)box.setOnCheckedChangeListener((button,checked)->{
            if(checked&&checkedTraits(traitBoxes).size()>3){button.setChecked(false);Toast.makeText(this,"Galima pasirinkti lygiai tris bruožus",Toast.LENGTH_SHORT).show();return;}
            updateCount.run();
        });
        updateCount.run();column.addView(traitPanel, sp(dp(9)));

        Button save = gold(editing ? "IŠSAUGOTI PAKEITIMUS" : "SUKURTI VEIKĖJĄ IR PRADĖTI");save.setMinHeight(dp(52));
        save.setOnClickListener(view->{
            int parsedAge;try{parsedAge=Integer.parseInt(age.getText().toString().trim());}catch(Exception error){parsedAge=-1;}
            ArrayList<String> selected=checkedTraits(traitBoxes);
            if(selected.size()!=3){Toast.makeText(this,"Pasirink lygiai tris charakterio bruožus",Toast.LENGTH_LONG).show();return;}
            boolean ok=applyCharacterProfile(name.getText().toString(),parsedAge,ageless.isChecked(),selectedTag(identities),appearance.getText().toString(),selectedTag(origins),selectedTag(archetypes),selected,selectedTag(progressionModes));
            if(!ok){Toast.makeText(this,"Patikrink vardą, amžių ir visus profilio pasirinkimus",Toast.LENGTH_LONG).show();return;}
            haptic();show("game");
        });
        column.addView(save, sp(dp(7)));
        if(editing){Button cancel=dark("GRĮŽTI NEIŠSAUGOJUS");cancel.setTag("cancel_narration");cancel.setMinHeight(dp(48));cancel.setOnClickListener(view->show("hero"));column.addView(cancel);}
        return scroll;
    }

    private EditText profileInput(String hint,String value,boolean multiline,int maxLength){
        EditText input=new EditText(this);input.setHint(hint);input.setText(value);input.setHintTextColor(Color.rgb(102,124,132));input.setTextColor(PARCH);input.setTextSize(12);
        input.setSingleLine(!multiline);input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});input.setPadding(dp(11),dp(9),dp(11),dp(9));
        input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES|(multiline?InputType.TYPE_TEXT_FLAG_MULTI_LINE:0));
        input.setBackground(round(Color.rgb(6,15,22),12,Color.rgb(42,60,70)));input.setMinHeight(dp(48));return input;
    }

    private RadioButton profileRadio(String id,String title,String details,boolean checked){
        RadioButton button=new RadioButton(this);button.setId(View.generateViewId());button.setTag(id);button.setText(details.isEmpty()?title:title+"\n"+details);
        button.setTextColor(PARCH);button.setTextSize(10);button.setMinHeight(dp(details.isEmpty()?48:68));button.setPadding(dp(4),dp(4),dp(4),dp(4));button.setChecked(checked);return button;
    }

    private String selectedTag(RadioGroup group){View selected=group.findViewById(group.getCheckedRadioButtonId());return selected==null?"":String.valueOf(selected.getTag());}
    private ArrayList<String> checkedTraits(List<CheckBox> boxes){ArrayList<String> selected=new ArrayList<>();for(CheckBox box:boxes)if(box.isChecked())selected.add(String.valueOf(box.getTag()));return selected;}

    @Override View game() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout column = col();
        column.setPadding(dp(10), dp(7), dp(10), dp(18));
        scroll.addView(column);

        if (state.combatActive) {
            CombatV090View board = new CombatV090View(this);
            board.setState(state);
            column.addView(board, new LinearLayout.LayoutParams(-1, dp(330)));
        } else {
            SceneV090View scene = new SceneV090View(this);
            scene.setScene(state.location, state.sceneTitle);
            column.addView(scene, new LinearLayout.LayoutParams(-1, dp(285)));
        }
        column.addView(resources(), sp(dp(8)));
        if(!state.tutorialComplete){
            LinearLayout guide=panel(true);guide.addView(section("GREITA PRADŽIA · 1 MINUTĖ"));guide.addView(serif("Tavo sprendimai turi mechanines pasekmes",18,PARCH,true));
            guide.addView(txt("1. Rinkis vieną iš trijų veiksmų arba aprašyk savą.\n2. Patikra naudoja tavo savybes, įrangą, talentus ir būseną.\n3. Žurnale sek užduotis, o Veikėjo ekrane valdyk talentus bei kompanionus.\n4. Nustatymuose bet kada atšauk paskutinį ėjimą arba keisk sunkumą.",10,Color.rgb(221,226,220),false),sp(dp(7)));
            Button understood=gold("SUPRATAU · PRADĖTI KELIONĘ");understood.setOnClickListener(view->{state.tutorialComplete=true;db.saveState(state);feedback="Vedlys užbaigtas · visada gali grįžti per nustatymus";show("game");});guide.addView(understood);column.addView(guide,sp(dp(8)));
        }
        if (!feedback.isEmpty() && !busy) column.addView(feedbackCard(), sp(dp(8)));

        LinearLayout story = panel(true);
        story.addView(section("DABARTINĖ SCENA"));
        story.addView(serif(state.sceneTitle, 23, PARCH, true), sp(dp(6)));
        TextView body = txt(state.scene, 14, Color.rgb(226, 229, 222), false);
        body.setLineSpacing(dp(2), 1.10f);
        story.addView(body);
        column.addView(story, sp(dp(9)));

        LinearLayout quest = panel(false);
        LinearLayout questHeader = row();
        questHeader.setGravity(Gravity.CENTER_VERTICAL);
        questHeader.addView(section("PAGRINDINĖ UŽDUOTIS"), new LinearLayout.LayoutParams(0, -2, 1));
        questHeader.addView(chip("AKTYVU", GOLD2));
        quest.addView(questHeader);
        quest.addView(serif(state.questTitle, 16, PARCH, true));
        quest.addView(txt(state.objective, 10, SUB, false));
        quest.setOnClickListener(view -> show("journal"));
        column.addView(quest, sp(dp(10)));

        column.addView(section("KĄ DARAI?"), sp(dp(5)));
        for (int index = 0; index < Math.min(3, state.choices.size()); index++) {
            String action = state.choices.get(index);
            column.addView(choice(index + 1, action), sp(dp(6)));
        }

        if (!state.combatActive) {
            Button explore = outline("TYRINĖTI " + state.location.toUpperCase(Locale.forLanguageTag("lt-LT")));
            explore.setMinHeight(dp(48));
            explore.setOnClickListener(view -> {
                haptic();
                locationHub();
            });
            column.addView(explore, sp(dp(9)));
        }

        LinearLayout free = panel(false);
        LinearLayout freeHeader = row();
        freeHeader.setGravity(Gravity.CENTER_VERTICAL);
        freeHeader.addView(section("LAISVAS VEIKSMAS"), new LinearLayout.LayoutParams(0, -2, 1));
        boolean ai = !OpenAiSettings.LOCAL.equals(OpenAiSettings.provider(this));
        freeHeader.addView(chip(ai ? (OpenAiSettings.OPENAI.equals(OpenAiSettings.provider(this))?"OPENAI":"GROQ") : "VIETINIS", ai ? GREEN : BLUE));
        free.addView(freeHeader);
        EditText input = new EditText(this);
        input.setHint("Aprašyk bet kokį veiksmą…");
        input.setHintTextColor(Color.rgb(102, 124, 132));
        input.setTextColor(Color.rgb(238, 238, 231));
        input.setTextSize(13);
        input.setMinLines(2);
        input.setMaxLines(4);
        input.setPadding(dp(11), dp(9), dp(11), dp(9));
        input.setBackground(round(Color.rgb(6, 15, 22), 12, Color.rgb(42, 60, 70)));
        free.addView(input, sp(dp(7)));
        Button send = gold(busy ? "SPRENDŽIAMA…" : "ATLIKTI VEIKSMĄ  →");
        send.setMinHeight(dp(48));
        send.setEnabled(!busy);
        send.setOnClickListener(view -> {
            String action = input.getText().toString().trim();
            if (!action.isEmpty()) act(action);
        });
        free.addView(send);
        if(busy){Button cancel=outline("ATŠAUKTI SPRENDIMĄ");cancel.setTag("cancel_narration");cancel.setMinHeight(dp(48));cancel.setOnClickListener(view->cancelPendingAction());free.addView(cancel,sp(dp(5)));}
        column.addView(free);
        return scroll;
    }

    @Override View hero() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = col();
        column.setPadding(dp(10), dp(7), dp(10), dp(20));
        scroll.addView(column);

        FrameLayout hero = new FrameLayout(this);
        hero.setBackgroundColor(Color.rgb(2, 7, 11));
        boolean canonicalEinoras="Einoras".equalsIgnoreCase(state.characterName)
                &&"nenurodyta".equals(state.characterIdentity)
                &&(state.characterAppearance==null||state.characterAppearance.trim().isEmpty());
        if(canonicalEinoras){
            ImageView artwork = image(R.drawable.hero_einoras_v090);
            artwork.setContentDescription("Einoro, klasikinio Vaelorios herojaus, iliustracija");
            hero.addView(artwork, new FrameLayout.LayoutParams(-1, dp(510)));
        }else{
            CharacterAvatarV100View portrait = new CharacterAvatarV100View(this);
            portrait.setCharacter(state);
            hero.addView(portrait, new FrameLayout.LayoutParams(-1, dp(510)));
        }
        View shade = new View(this);
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.argb(0, 0, 0, 0), Color.argb(8, 0, 0, 0), Color.argb(244, 2, 8, 13)}));
        hero.addView(shade, new FrameLayout.LayoutParams(-1, dp(510)));
        LinearLayout overlay = col();
        overlay.setPadding(dp(16), 0, dp(16), dp(16));
        overlay.addView(section("VEIKĖJAS"));
        overlay.addView(serif(state.characterName.toUpperCase(Locale.forLanguageTag("lt-LT")), 30, PARCH, true));
        overlay.addView(txt(CharacterCatalogV093.ageLine(state), 9, SUB, false));
        LinearLayout tags = row();
        tags.setPadding(0, dp(7), 0, 0);
        tags.addView(chip("LYGIS "+state.level+" · "+("legendary".equals(state.progressionMode)?"LEGENDINIS":"SUBALANSUOTAS"), GOLD2));
        tags.addView(chip("3 BRUOŽAI", PURPLE));
        overlay.addView(tags);
        hero.addView(overlay, new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM));
        column.addView(hero, new LinearLayout.LayoutParams(-1, dp(510)));
        column.addView(resources(), sp(dp(8)));

        LinearLayout profile = panel(true);
        profile.addView(section("KILMĖ, ARCHETIPAS IR BRUOŽAI"));
        profile.addView(serif(CharacterCatalogV093.originName(state.characterOriginId)+" · "+CharacterCatalogV093.archetypeName(state.characterArchetypeId), 17, PARCH, true));
        profile.addView(txt("Tapatybė: "+CharacterCatalogV093.identityName(state.characterIdentity), 9, GOLD2, true), sp(dp(4)));
        if(state.characterAppearance!=null&&!state.characterAppearance.trim().isEmpty())profile.addView(txt("Išvaizda: "+state.characterAppearance, 10, SUB, false), sp(dp(5)));
        for(String traitId:CharacterCatalogV093.normalizedTraits(state.characterTraitIds)){
            CharacterCatalogV093.Trait trait=CharacterCatalogV093.trait(traitId);if(trait==null)continue;
            profile.addView(txt("◆ "+trait.name+" · "+trait.benefit+" "+trait.drawback, 9, Color.rgb(218,223,218), false), sp(dp(4)));
        }
        Button editProfile=outline("KEISTI PROFILĮ IR BRUOŽUS");editProfile.setMinHeight(dp(48));editProfile.setOnClickListener(view->show("character"));profile.addView(editProfile,sp(dp(2)));
        column.addView(profile,sp(dp(9)));

        LinearLayout progression=panel(true);progression.addView(section("LYGIS IR TALENTAI"));
        progression.addView(serif("Lygis "+state.level+" · "+state.experience+" / "+state.experienceNext+" patirties",18,PARCH,true));
        ProgressBar xp=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);xp.setMax(Math.max(1,state.experienceNext));xp.setProgress(state.experience);xp.setProgressTintList(android.content.res.ColorStateList.valueOf(GOLD2));xp.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(36,50,58)));progression.addView(xp,new LinearLayout.LayoutParams(-1,dp(8)));
        progression.addView(txt("Laisvi talentų taškai: "+state.talentPoints+" · savybių taškai: "+state.attributePoints+" · atrakinta "+db.world().unlockedTalentIds().size()+" / "+ProgressionEngine.TALENTS.length,10,SUB,false),sp(dp(6)));
        if(state.temporaryEffectTurns>0)progression.addView(txt("AKTYVUS POVEIKIS · "+state.temporaryEffectName+" · liko "+state.temporaryEffectTurns+" ėj.",9,GREEN,true),sp(dp(5)));
        Button talents=gold("ATVERTI TALENTŲ MEDĮ");talents.setMinHeight(dp(48));talents.setOnClickListener(view->talentDialog());progression.addView(talents);column.addView(progression,sp(dp(9)));

        LinearLayout companions=panel(false);companions.addView(section("KOMPANIONAI"));WorldRepository.Companion activeCompanion=db.world().activeCompanion();
        companions.addView(serif(activeCompanion==null?"Keliaująs vienas":activeCompanion.name+" · "+activeCompanion.role,16,PARCH,true));
        companions.addView(txt(activeCompanion==null?"Prisiviliok sąjungininką kurdamas pasitikėjimą ir vykdydamas Meridiano užduotį.":activeCompanion.perk+" · lojalumas "+activeCompanion.loyalty,10,SUB,false),sp(dp(6)));
        Button manageCompanions=outline("VALDYTI KOMPANIONUS");manageCompanions.setMinHeight(dp(48));manageCompanions.setOnClickListener(view->companionsDialog());companions.addView(manageCompanions);column.addView(companions,sp(dp(9)));

        LinearLayout mastery = panel(false);
        mastery.addView(section("MEISTRIŠKUMAS"));
        int masteryTotal=0;for(VaeloriaDb.Mastery value:db.getMasteries().values())masteryTotal+=value.level;int masteryAverage=masteryTotal/Math.max(1,db.getMasteries().size());
        mastery.addView(serif("Vidutinis meistriškumas "+masteryAverage+" / 100", 17, PARCH, true));
        mastery.addView(txt("Kiekvienas realiai atliekamas veiksmas ugdo pagrindinę ir pagalbinę savybę. Aukštesnis meistriškumas tiesiogiai gerina patikras.", 10, SUB, false));
        column.addView(mastery, sp(dp(9)));

        LoadoutV090View loadout = new LoadoutV090View(this);
        String[] slots = {"head", "neck", "weapon", "offhand", "chest", "utility", "hands", "belt",
                "legs", "feet", "ring_left", "ring_right", "relic_1", "relic_2", "relic_3", "relic_4"};
        for (String slot : slots) {
            VaeloriaDb.Item item = db.getEquippedAt(slot);
            loadout.put(slot, VaeloriaDb.slotLabel(slot), item == null ? "" : item.name,
                    item == null ? "" : item.rarity,
                    item == null ? Color.rgb(67, 84, 92) : rarity(item.rarity));
        }
        loadout.setListener(this::openSlot);
        column.addView(loadout, new LinearLayout.LayoutParams(-1, dp(552)));
        TextView gearHint = txt("Bakstelėk įrangos lauką, kad pakeistum daiktą.", 9, SUB, false);
        gearHint.setPadding(dp(4), dp(7), dp(4), dp(9));
        column.addView(gearHint);

        LinearLayout setStatus = panel(false);
        setStatus.addView(section("AKTYVŪS SETŲ BONUSAI"));
        List<ItemCatalogV092.ActiveSetBonus> activeSets = ItemCatalogV092.activeSetBonuses(db.getItems());
        if (activeSets.isEmpty()) setStatus.addView(txt("Užsidėk bent 2 to paties seto dalis, kad aktyvuotum pirmą bonusą.", 10, SUB, false));
        else for (ItemCatalogV092.ActiveSetBonus bonus : activeSets) {
            setStatus.addView(serif(bonus.set.name + " · " + bonus.pieces + "/6", 14, rarity(bonus.set.rarity), true));
            setStatus.addView(txt(bonus.text, 9, SUB, false), sp(dp(5)));
        }
        column.addView(setStatus, sp(dp(9)));

        LinearLayout abilities = panel(false);
        LinearLayout abilityHeader = row();
        abilityHeader.setGravity(Gravity.CENTER_VERTICAL);
        abilityHeader.addView(section("GEBĖJIMAI VIRŠ SAVYBIŲ RIBOS"), new LinearLayout.LayoutParams(0, -2, 1));
        abilityHeader.addView(chip(String.valueOf(db.getAbilities().size()), PURPLE));
        abilities.addView(abilityHeader);
        for (String[] ability : db.getAbilities()) abilities.addView(abilityCard(ability));
        column.addView(abilities, sp(dp(9)));

        Button stats = dark("ATVERTI PILNĄ STATISTIKĄ");
        stats.setMinHeight(dp(48));
        stats.setOnClickListener(view -> startActivity(new Intent(this, StatsActivity.class)));
        column.addView(stats);
        return scroll;
    }

    private void talentDialog(){
        ScrollView scroll=new ScrollView(this);LinearLayout column=col();column.setPadding(dp(14),dp(12),dp(14),dp(12));scroll.addView(column);
        column.addView(serif("TALENTŲ MEDIS",23,PARCH,true));column.addView(txt("Laisvi taškai "+state.talentPoints+" · lygis "+state.level+". Kiekviena šaka turi nuoseklias prielaidas ir mechaninį poveikį.",10,SUB,false),sp(dp(9)));
        final AlertDialog[] holder=new AlertDialog[1];String branch="";
        for(WorldRepository.TalentState talent:db.world().talents()){
            ProgressionEngine.TalentDef definition=talent.definition;if(!branch.equals(definition.branch)){branch=definition.branch;column.addView(section(branch),sp(dp(7)));}
            LinearLayout card=panel(talent.unlocked);card.addView(serif((talent.unlocked?"◆ ":"◇ ")+definition.name+" · L"+definition.requiredLevel,15,talent.unlocked?GREEN:PARCH,true));card.addView(txt(definition.description,10,SUB,false),sp(dp(4)));
            if(!talent.unlocked){ProgressionEngine.TalentDef prerequisite=ProgressionEngine.byId(definition.prerequisite);String requirement="Kaina "+definition.cost+" tšk."+(prerequisite==null?"":" · reikia "+prerequisite.name);Button unlock=gold("ATRAKINTI · "+requirement);unlock.setMinHeight(dp(44));unlock.setOnClickListener(view->{db.checkpoint("prieš talento atrakinimą",state);WorldRepository.TransactionResult result=db.world().unlockTalent(definition.id,state);feedback=result.message;Toast.makeText(this,result.message,Toast.LENGTH_LONG).show();if(result.ok){if(holder[0]!=null)holder[0].dismiss();show("hero");}});card.addView(unlock);}
            column.addView(card,sp(dp(6)));
        }
        holder[0]=new AlertDialog.Builder(this).setView(scroll).setNegativeButton("UŽDARYTI",null).create();holder[0].show();
    }

    private void companionsDialog(){
        ScrollView scroll=new ScrollView(this);LinearLayout column=col();column.setPadding(dp(14),dp(12),dp(14),dp(12));scroll.addView(column);
        column.addView(serif("KOMPANIONAI",23,PARCH,true));column.addView(txt("Vienu metu keliauja vienas sąjungininkas. Santykiai, pagrindinė užduotis ir bendri veiksmai atrakina naujus pasirinkimus.",10,SUB,false),sp(dp(9)));
        final AlertDialog[] holder=new AlertDialog[1];
        for(WorldRepository.Companion companion:db.world().companions()){
            LinearLayout card=panel(companion.active);card.addView(serif((companion.active?"◆ ":companion.recruited?"◇ ":"○ ")+companion.name,16,companion.active?GREEN:PARCH,true));card.addView(txt(companion.role+" · lojalumas "+companion.loyalty+"\n"+companion.perk,10,SUB,false),sp(dp(5)));
            Button action=companion.active?outline("LEISTI PAILSĖTI"):gold(companion.recruited?"PASIRINKTI AKTYVIU":"PRISIVILIOTI");action.setMinHeight(dp(46));action.setOnClickListener(view->{db.checkpoint("prieš kompaniono pakeitimą",state);WorldRepository.TransactionResult result=companion.active?db.world().dismissCompanion():companion.recruited?db.world().activateCompanion(companion.id):db.world().recruitCompanion(companion.id,state);feedback=result.message;Toast.makeText(this,result.message,Toast.LENGTH_LONG).show();if(result.ok){if(holder[0]!=null)holder[0].dismiss();show("hero");}});card.addView(action);column.addView(card,sp(dp(6)));
        }
        holder[0]=new AlertDialog.Builder(this).setView(scroll).setNegativeButton("UŽDARYTI",null).create();holder[0].show();
    }

    View items() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = col();
        column.setPadding(dp(10), dp(7), dp(10), dp(20));
        scroll.addView(column);
        LinearLayout header = panel(true);
        header.addView(section("INVENTORIUS"));
        header.addView(serif("Relikvijos ir įranga", 23, PARCH, true));
        List<VaeloriaDb.Item> allInventory = db.getItems();
        int equipped = 0;
        int synced = 0;
        int units = 0;
        for (VaeloriaDb.Item item : allInventory) {
            if (item.equipped) equipped++;
            if (item.synced) synced++;
            units += Math.max(1, item.quantity);
        }
        header.addView(txt(allInventory.size() + " rūšių · " + units + " vnt. · " + equipped + " įrengti · " + synced + " rezonuoja", 10, SUB, false));
        column.addView(header, sp(dp(9)));

        LinearLayout codex = panel(false);
        codex.addView(section("DAIKTŲ KODEKSAS · 325 ILIUSTRUOTI DAIKTAI"));
        codex.addView(serif("Ginklai, setai, potionai ir RPG reikmenys", 17, PARCH, true));
        codex.addView(txt("19 kategorijų · 8 retumo pakopos · 1–100 lygiai · kiekvienas daiktas turi savo iliustraciją ir mechaninį poveikį.", 9, SUB, false), sp(dp(7)));
        Button sets = outline("10 PILNŲ SETŲ · 2 / 4 / 6 DALIŲ BONUSAI");
        sets.setMinHeight(dp(48));sets.setOnClickListener(view -> setsDialog());codex.addView(sets, sp(dp(7)));
        Button catalogSearch=gold("IEŠKOTI VISAME 325 DAIKTŲ KODEKSE");catalogSearch.setOnClickListener(view->globalCatalogSearchDialog());codex.addView(catalogSearch,sp(dp(7)));
        for (String[] group : ITEM_GROUPS_V092) {
            int count = ItemCatalogV092.inCategory(group[1]).size();
            Button button = dark(group[0] + " · " + count);
            button.setMinHeight(dp(48));button.setOnClickListener(view -> catalogDialog(group[0], group[1], 0));codex.addView(button, sp(dp(5)));
        }

        Button favoritesFilter=outline(onlyFavoriteInventory?"RODYTI VISUS TURIMUS DAIKTUS":"★ RODYTI MĖGSTAMUS DAIKTUS");favoritesFilter.setOnClickListener(view->{onlyFavoriteInventory=!onlyFavoriteInventory;inventoryPage=0;show("items");});column.addView(favoritesFilter,sp(dp(8)));
        List<VaeloriaDb.Item> inventory=filteredInventory(allInventory);int pageSize=20,pageCount=Math.max(1,(inventory.size()+pageSize-1)/pageSize);inventoryPage=Math.max(0,Math.min(inventoryPage,pageCount-1));int start=inventoryPage*pageSize,end=Math.min(inventory.size(),start+pageSize);
        LinearLayout inventoryTools=panel(false);inventoryTools.addView(section("TURIMI DAIKTAI · "+inventory.size()+" REZULTATŲ · "+(inventoryPage+1)+"/"+pageCount));inventoryTools.addView(txt((inventoryQuery.isEmpty()?"Be teksto filtro":"Paieška: "+inventoryQuery)+" · retumas: "+(inventoryRarity.equals("all")?"visi":rarityLabel(inventoryRarity))+" · rikiavimas: "+("name".equals(inventorySort)?"pavadinimas":"level".equals(inventorySort)?"lygis":"value".equals(inventorySort)?"vertė":"retumas"),9,SUB,false),sp(dp(5)));Button filter=outline("IEŠKOTI · FILTRUOTI · RIKIUOTI");filter.setOnClickListener(view->inventoryFilterDialog());inventoryTools.addView(filter);column.addView(inventoryTools,sp(dp(7)));
        if(inventory.isEmpty())column.addView(txt("Pagal pasirinktą filtrą daiktų nerasta.",11,SUB,false),sp(dp(8)));

        boolean singleColumn=pref("large_text",false)||getResources().getConfiguration().fontScale>1.15f;
        for(int index=start;index<end;index+=singleColumn?1:2){
            LinearLayout line=row();line.addView(itemCard(inventory.get(index)),new LinearLayout.LayoutParams(0,-2,1));
            if(!singleColumn){line.addView(new Space(this),new LinearLayout.LayoutParams(dp(7),1));line.addView(index+1<end?itemCard(inventory.get(index+1)):new View(this),new LinearLayout.LayoutParams(0,-2,1));}
            column.addView(line,sp(dp(7)));
        }
        if(pageCount>1){LinearLayout pages=row();if(inventoryPage>0){Button previous=dark("← ANKSTESNIS");previous.setOnClickListener(view->{inventoryPage--;show("items");});pages.addView(previous,new LinearLayout.LayoutParams(0,dp(48),1));}if(inventoryPage+1<pageCount){Button next=gold("KITAS →");next.setOnClickListener(view->{inventoryPage++;show("items");});pages.addView(next,new LinearLayout.LayoutParams(0,dp(48),1));}column.addView(pages,sp(dp(5)));}
        column.addView(codex,sp(dp(10)));
        return scroll;
    }

    private View itemCard(VaeloriaDb.Item item) {
        LinearLayout card = col();
        card.setPadding(dp(9), dp(9), dp(9), dp(9));
        int color = rarity(item.rarity);
        card.setBackground(round(PANEL_2, 14,
                Color.argb(155, Color.red(color), Color.green(color), Color.blue(color))));
        card.setContentDescription(item.name + ". " + rarityLabel(item.rarity));
        ItemArtView icon = new ItemArtView(this);
        icon.setItem(item.name, item.slot == null ? item.type : item.slot, item.rarity);
        card.addView(icon, new LinearLayout.LayoutParams(-1, dp(92)));
        TextView name = serif((state.favoriteItemIds.contains(item.id)?"★ ":"")+item.name, 12, PARCH, true);
        card.addView(name, sp(dp(3)));
        card.addView(txt(rarityLabel(item.rarity).toUpperCase(Locale.ROOT), 8, color, true));
        card.addView(txt("L" + item.itemLevel + " · GALIA " + item.power + (item.quantity > 1 ? " · ×" + item.quantity : ""), 7, GOLD2, true));
        card.addView(txt(itemDescriptor(item), 7, SUB, false));
        String status = (item.equipped ? "● ĮRENGTA" : "○ INVENTORIUJE") + (item.synced ? " · REZONUOJA" : "");
        card.addView(txt(status, 7, item.synced ? GREEN : SUB, true));
        card.setOnClickListener(view -> {
            haptic();
            itemDialog(item);
        });
        return card;
    }

    private List<VaeloriaDb.Item> filteredInventory(List<VaeloriaDb.Item> source){
        ArrayList<VaeloriaDb.Item> result=new ArrayList<>();String query=inventoryQuery.trim().toLowerCase(Locale.forLanguageTag("lt-LT"));for(VaeloriaDb.Item item:source){if(onlyFavoriteInventory&&!state.favoriteItemIds.contains(item.id))continue;String searchable=(item.name+" "+item.description+" "+item.effect+" "+item.type).toLowerCase(Locale.forLanguageTag("lt-LT"));if(!query.isEmpty()&&!searchable.contains(query))continue;if(!"all".equals(inventoryRarity)&&!inventoryRarity.equals(item.rarity))continue;result.add(item);}
        if("name".equals(inventorySort)){java.text.Collator collator=java.text.Collator.getInstance(Locale.forLanguageTag("lt-LT"));result.sort((a,b)->collator.compare(a.name,b.name));}else if("level".equals(inventorySort))result.sort((a,b)->Integer.compare(b.itemLevel,a.itemLevel));else if("value".equals(inventorySort))result.sort((a,b)->Integer.compare(b.value,a.value));return result;
    }

    private void inventoryFilterDialog(){
        ScrollView scroll=new ScrollView(this);LinearLayout content=col();content.setPadding(dp(14),dp(12),dp(14),dp(10));scroll.addView(content);content.addView(serif("INVENTORIAUS FILTRAI",21,PARCH,true));EditText query=profileInput("Pavadinimas, poveikis ar tipas",inventoryQuery,false,60);content.addView(query,sp(dp(7)));content.addView(section("RETUMAS"));RadioGroup rarities=new RadioGroup(this);String[][] rarityValues={{"all","Visi"},{"common","Paprasti"},{"uncommon","Neįprasti"},{"rare","Reti"},{"epic","Epiniai"},{"legendary","Legendiniai"},{"mythic","Mitiniai"},{"ancient","Senoviniai"},{"unique","Unikalūs"}};for(String[] value:rarityValues)rarities.addView(profileRadio(value[0],value[1],"",value[0].equals(inventoryRarity)));content.addView(rarities);content.addView(section("RIKIAVIMAS"),sp(dp(5)));RadioGroup sorts=new RadioGroup(this);sorts.addView(profileRadio("rarity","Pagal retumą","","rarity".equals(inventorySort)));sorts.addView(profileRadio("level","Pagal lygį","","level".equals(inventorySort)));sorts.addView(profileRadio("value","Pagal vertę","","value".equals(inventorySort)));sorts.addView(profileRadio("name","Pagal pavadinimą","","name".equals(inventorySort)));content.addView(sorts);new AlertDialog.Builder(this).setView(scroll).setNegativeButton("ATŠAUKTI",null).setNeutralButton("VALYTI",(dialog,which)->{inventoryQuery="";inventoryRarity="all";inventorySort="rarity";inventoryPage=0;show("items");}).setPositiveButton("TAIKYTI",(dialog,which)->{inventoryQuery=query.getText().toString().trim();inventoryRarity=selectedTag(rarities);inventorySort=selectedTag(sorts);inventoryPage=0;show("items");}).show();
    }

    private void globalCatalogSearchDialog(){EditText query=profileInput("Pvz., nuodai, kardas, mitinis, mana…","",false,60);new AlertDialog.Builder(this).setTitle("Ieškoti daiktų kodekse").setMessage("Paieška tikrina pavadinimą, aprašą, poveikį, regioną, kategoriją ir retumą.").setView(query).setNegativeButton("ATŠAUKTI",null).setPositiveButton("IEŠKOTI",(dialog,which)->{String value=query.getText().toString().trim();if(!value.isEmpty())catalogSearchResults(value,0);}).show();}

    private void catalogSearchResults(String rawQuery,int page){
        String query=rawQuery.toLowerCase(Locale.forLanguageTag("lt-LT"));ArrayList<ItemCatalogV092.ItemDef> matches=new ArrayList<>();for(ItemCatalogV092.ItemDef item:ItemCatalogV092.ALL){String text=(item.name+" "+item.description+" "+item.effect+" "+item.region+" "+item.category+" "+item.rarity+" "+rarityLabel(item.rarity)+" "+ItemCatalogV092.categoryLabel(item.category)).toLowerCase(Locale.forLanguageTag("lt-LT"));if(text.contains(query))matches.add(item);}matches.sort((a,b)->Integer.compare(b.level,a.level));int pageSize=20,pageCount=Math.max(1,(matches.size()+pageSize-1)/pageSize),safe=Math.max(0,Math.min(page,pageCount-1)),start=safe*pageSize,end=Math.min(matches.size(),start+pageSize);ScrollView scroll=new ScrollView(this);LinearLayout column=col();column.setPadding(dp(12),dp(12),dp(12),dp(10));scroll.addView(column);column.addView(serif("„"+rawQuery+"“",21,PARCH,true));column.addView(txt(matches.size()+" rezultatų · puslapis "+(safe+1)+"/"+pageCount,9,SUB,false),sp(dp(8)));addCatalogCards(column,matches,start,end);AlertDialog.Builder builder=new AlertDialog.Builder(this).setView(scroll).setNegativeButton("UŽDARYTI",null);if(safe>0)builder.setNeutralButton("ANKSTESNIS",(dialog,which)->catalogSearchResults(rawQuery,safe-1));if(end<matches.size())builder.setPositiveButton("KITAS",(dialog,which)->catalogSearchResults(rawQuery,safe+1));builder.show();
    }

    private void catalogDialog(String title, String category, int page) {
        List<ItemCatalogV092.ItemDef> items = ItemCatalogV092.inCategory(category);
        int pageSize = 20;
        int pageCount = Math.max(1, (items.size() + pageSize - 1) / pageSize);
        int safePage = Math.max(0, Math.min(page, pageCount - 1));
        int start = safePage * pageSize;
        int end = Math.min(items.size(), start + pageSize);
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = col();column.setPadding(dp(12), dp(12), dp(12), dp(10));scroll.addView(column);
        column.addView(serif(title, 22, PARCH, true));
        column.addView(txt(items.size() + " daiktų · puslapis " + (safePage + 1) + "/" + pageCount, 9, SUB, false), sp(dp(9)));
        addCatalogCards(column,items,start,end);
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(scroll).setNegativeButton("UŽDARYTI", null);
        if (safePage > 0) builder.setNeutralButton("ANKSTESNIS", (dialog, which) -> catalogDialog(title, category, safePage - 1));
        if (end < items.size()) builder.setPositiveButton("KITAS", (dialog, which) -> catalogDialog(title, category, safePage + 1));
        builder.show();
    }

    private void addCatalogCards(LinearLayout column,List<ItemCatalogV092.ItemDef> items,int start,int end){
        boolean single=pref("large_text",false)||getResources().getConfiguration().fontScale>1.15f;
        for(int index=start;index<end;index+=single?1:2){
            LinearLayout line=row();line.addView(catalogCard(items.get(index)),new LinearLayout.LayoutParams(0,-2,1));
            if(!single){line.addView(new Space(this),new LinearLayout.LayoutParams(dp(7),1));line.addView(index+1<end?catalogCard(items.get(index+1)):new View(this),new LinearLayout.LayoutParams(0,-2,1));}
            column.addView(line,sp(dp(7)));
        }
    }

    private View catalogCard(ItemCatalogV092.ItemDef item) {
        LinearLayout card = col();int color = rarity(item.rarity);boolean locked=state.level<item.level;card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setBackground(round(PANEL_2, 13, Color.argb(160, Color.red(color), Color.green(color), Color.blue(color))));
        ItemArtView artwork = new ItemArtView(this);artwork.setItem(item.name, item.category, item.rarity);
        card.addView(artwork, new LinearLayout.LayoutParams(-1, dp(112)));
        TextView name = serif(item.name, 11, PARCH, true);card.addView(name, sp(dp(3)));
        card.addView(txt(rarityLabel(item.rarity).toUpperCase(Locale.ROOT) + " · L" + item.level+(locked?" · UŽRAKINTA":""), 7, locked?Color.rgb(215,154,91):color, true));
        card.addView(txt("GALIA " + item.power + " · " + ItemCatalogV092.categoryLabel(item.category), 7, SUB, false));
        card.setContentDescription(item.name + ". " + rarityLabel(item.rarity) + ". Lygis " + item.level);
        card.setOnClickListener(view -> catalogItemDialog(item));return card;
    }

    private void catalogItemDialog(ItemCatalogV092.ItemDef item) {
        ScrollView scroll = new ScrollView(this);LinearLayout content = col();content.setPadding(dp(16), dp(12), dp(16), dp(8));scroll.addView(content);
        ItemArtView artwork = new ItemArtView(this);artwork.setItem(item.name, item.category, item.rarity);content.addView(artwork, new LinearLayout.LayoutParams(-1, dp(250)));
        content.addView(serif(item.name, 21, PARCH, true), sp(dp(4)));
        content.addView(txt(rarityLabel(item.rarity).toUpperCase(Locale.ROOT) + " · LYGIS " + item.level + " · GALIA " + item.power, 9, rarity(item.rarity), true), sp(dp(6)));
        content.addView(txt(ItemCatalogV092.categoryLabel(item.category) + " · vertė " + item.value + " karūnų", 9, GOLD2, true), sp(dp(6)));
        content.addView(txt(item.description, 11, Color.rgb(218, 223, 218), false));
        if (!item.effect.isEmpty()) content.addView(txt("MECHANIKA · " + (item.consumable?ConsumableRulesV110.description(item):item.effect), 10, GREEN, true), sp(dp(7)));
        content.addView(txt(state.level<item.level?"UŽRAKINTA · galima naudoti ar įrengti nuo "+item.level+" lygio":"LYGIO REIKALAVIMAS ĮVYKDYTAS",9,state.level<item.level?Color.rgb(215,154,91):GREEN,true),sp(dp(6)));
        content.addView(txt("KILMĖ · " + item.region, 9, SUB, true), sp(dp(6)));
        if (!item.setId.isEmpty()) {ItemCatalogV092.SetDef set = ItemCatalogV092.setById(item.setId);if (set != null) content.addView(txt("SETO DALIS · " + set.name, 10, rarity(set.rarity), true), sp(dp(6)));}
        new AlertDialog.Builder(this).setView(scroll).setPositiveButton("UŽDARYTI", null).show();
    }

    private void setsDialog() {
        ScrollView scroll = new ScrollView(this);LinearLayout column = col();column.setPadding(dp(14), dp(12), dp(14), dp(10));scroll.addView(column);
        column.addView(serif("10 PILNŲ DAIKTŲ SETŲ", 22, PARCH, true));
        column.addView(txt("Kiekvieną setą sudaro 6 iliustruotos dalys. Bonusai aktyvuojami užsidėjus 2, 4 ir 6 dalis.", 9, SUB, false), sp(dp(9)));
        for (ItemCatalogV092.SetDef set : ItemCatalogV092.SETS) {
            Button button = dark(set.name + " · " + rarityLabel(set.rarity).toUpperCase(Locale.ROOT));button.setMinHeight(dp(48));
            button.setOnClickListener(view -> setDetailDialog(set));column.addView(button, sp(dp(5)));
        }
        new AlertDialog.Builder(this).setView(scroll).setNegativeButton("UŽDARYTI", null).show();
    }

    private void setDetailDialog(ItemCatalogV092.SetDef set) {
        ScrollView scroll = new ScrollView(this);LinearLayout column = col();column.setPadding(dp(12), dp(12), dp(12), dp(10));scroll.addView(column);
        column.addView(serif(set.name, 22, PARCH, true));column.addView(txt(set.region + " · " + rarityLabel(set.rarity).toUpperCase(Locale.ROOT), 9, rarity(set.rarity), true), sp(dp(7)));
        column.addView(txt("2 DALYS · " + set.bonus2 + "\n4 DALYS · " + set.bonus4 + "\n6 DALYS · " + set.bonus6, 10, GREEN, true), sp(dp(9)));
        ArrayList<ItemCatalogV092.ItemDef> pieces = new ArrayList<>();for (ItemCatalogV092.ItemDef item : ItemCatalogV092.ALL) if (set.id.equals(item.setId)) pieces.add(item);
        addCatalogCards(column,pieces,0,pieces.size());
        new AlertDialog.Builder(this).setView(scroll).setNegativeButton("UŽDARYTI", null).show();
    }

    @Override View map() {
        FrameLayout frame = new FrameLayout(this);
        WorldMapV090View world = new WorldMapV090View(this, state,db.world().discoveredLocations());
        world.setCurrentLocation(state.location);
        world.setListener(this::locationDialog);
        frame.addView(world, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout top = panel(false);
        LinearLayout topRow = row();
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout title = col();
        title.addView(section("PASAULIO ATLASAS"));
        TextView current = serif(state.location, 18, PARCH, true);
        current.setMaxLines(2);
        title.addView(current);
        topRow.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        String mapName=state.characterName.length()>12?state.characterName.substring(0,11)+"…":state.characterName;
        topRow.addView(chip("◆ "+mapName.toUpperCase(Locale.forLanguageTag("lt-LT")), GOLD2));
        top.addView(topRow);
        top.addView(txt("Žnybk · tempk · dukart bakstelėk · ? žymi dar neatrastą kryptį", 8, SUB, false));
        FrameLayout.LayoutParams topParams = new FrameLayout.LayoutParams(-1, -2, Gravity.TOP);
        topParams.setMargins(dp(10), dp(9), dp(10), 0);
        frame.addView(top, topParams);

        LinearLayout bottom = row();
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        bottom.setPadding(dp(10), dp(7), dp(10), dp(7));
        bottom.setBackground(round(Color.argb(232, 4, 12, 18), 14, LINE));
        TextView danger = txt("GRĖSMĖ  ● žema · ● vid. · ● aukšta", 7, Color.rgb(193, 205, 206), true);
        danger.setMaxLines(2);
        bottom.addView(danger, new LinearLayout.LayoutParams(0, -2, 1));
        Button locations=small("VIETOS");locations.setMinHeight(dp(44));locations.setContentDescription("Atverti prieinamą atlaso vietų sąrašą");locations.setOnClickListener(view->atlasListDialog());bottom.addView(locations,new LinearLayout.LayoutParams(dp(72),dp(44)));
        Button factions = small("FRAKCIJOS");
        factions.setMinHeight(dp(44));
        factions.setOnClickListener(view -> {
            world.toggleFactions();
            haptic();
        });
        bottom.addView(factions, new LinearLayout.LayoutParams(dp(76), dp(44)));
        Button center = small("CENTRUOTI");
        center.setMinHeight(dp(44));
        center.setOnClickListener(view -> {
            world.resetView();
            haptic();
        });
        bottom.addView(center, new LinearLayout.LayoutParams(dp(76), dp(44)));
        FrameLayout.LayoutParams bottomParams = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM);
        bottomParams.setMargins(dp(10), 0, dp(10), dp(10));
        frame.addView(bottom, bottomParams);
        return frame;
    }

    private void atlasListDialog(){
        ScrollView scroll=new ScrollView(this);LinearLayout column=col();column.setPadding(dp(14),dp(12),dp(14),dp(10));scroll.addView(column);column.addView(serif("ATLASO VIETOS",22,PARCH,true));column.addView(txt("Sąrašas pateikia tą pačią informaciją kaip interaktyvus žemėlapis ir yra patogus ekrano skaitytuvui.",10,SUB,false),sp(dp(8)));
        int unknown=0;for(WorldRepository.LocationInfo location:db.world().locations()){String label=location.discovered?location.name+" · "+location.region+" · pavojus "+location.danger:"Neatrasta vieta "+(++unknown)+" · pavojus "+location.danger;Button button=location.name.equalsIgnoreCase(state.location)?gold("◆ DABAR · "+label):dark("◇ "+label);button.setAllCaps(false);button.setOnClickListener(view->locationDialog(location.name,location.danger));column.addView(button,sp(dp(5)));}
        new AlertDialog.Builder(this).setView(scroll).setNegativeButton("UŽDARYTI",null).show();
    }

    @Override View journal() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = col();
        column.setPadding(dp(10), dp(7), dp(10), dp(20));
        scroll.addView(column);

        FrameLayout keyArt = new FrameLayout(this);
        ImageView artwork = image(R.drawable.quest_broken_meridian_v090);
        artwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
        artwork.setContentDescription("Lūžusio Meridiano siužeto iliustracija");
        keyArt.addView(artwork, new FrameLayout.LayoutParams(-1, dp(230)));
        View shade = new View(this);
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.argb(5, 0, 0, 0), Color.argb(235, 3, 9, 13)}));
        keyArt.addView(shade, new FrameLayout.LayoutParams(-1, dp(230)));
        LinearLayout overlay = col();
        overlay.setPadding(dp(15), 0, dp(15), dp(14));
        overlay.addView(section("PAGRINDINĖ SIUŽETO LINIJA · I DALIS"));
        TextView questTitle = serif(state.questTitle.toUpperCase(Locale.forLanguageTag("lt-LT")), 24, PARCH, true);
        questTitle.setMaxLines(2);
        overlay.addView(questTitle);
        overlay.addView(txt("Vartų poslinkis · Orisono signalai · politinė krizė", 9, SUB, false));
        keyArt.addView(overlay, new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM));
        column.addView(keyArt, new LinearLayout.LayoutParams(-1, dp(230)));

        LinearLayout progress = panel(true);
        progress.addView(section("TIKSLAI"));
        for(WorldRepository.QuestStep questStep:db.world().steps("Q-MERIDIAN")){
            String label=questStep.title+(questStep.target>1?" · "+questStep.progress+"/"+questStep.target:"");
            progress.addView(step("completed".equals(questStep.status),"active".equals(questStep.status),label));
        }
        Button trackMain=outline("SEKTI MERIDIANO TYRIMĄ");trackMain.setOnClickListener(view->{state.trackedQuestId="";db.world().applyQuestToState(state);db.world().applyStructuredChoices(state);db.saveState(state);show("game");});progress.addView(trackMain);
        column.addView(progress, sp(dp(9)));

        LinearLayout threads = panel(false);
        threads.addView(section("UŽDUOTYS IR GYVI PASAULIO ĮVYKIAI"));
        for(WorldRepository.Quest quest:db.world().quests())if("side".equals(quest.type)){
            SideQuestCatalog.QuestDef def=SideQuestCatalog.byId(quest.id);if(def==null)continue;
            LinearLayout card=panel(quest.id.equals(state.trackedQuestId));
            String status="completed".equals(quest.status)?"UŽBAIGTA":"ready".equals(quest.status)?"ATLYGIS PARUOŠTAS":"active".equals(quest.status)?"VYKDOMA":"GALIMA PRIIMTI";
            card.addView(serif(quest.title,16,PARCH,true));card.addView(txt(status+" · "+def.location,11,GOLD2,true));
            for(WorldRepository.QuestStep step:db.world().steps(quest.id))card.addView(txt(("completed".equals(step.status)?"✓ ":"active".equals(step.status)?"◆ ":"○ ")+step.title+(step.target>1?" · "+step.progress+"/"+step.target:""),11,SUB,false),sp(dp(4)));
            card.addView(txt("Atlygis: "+def.gold+" karūnų · "+def.xp+" patirties · "+ItemCatalogV092.byId(def.rewardItem).name+" ×2",11,GREEN,false),sp(dp(6)));
            if(!"completed".equals(quest.status)){
                Button action=gold("available".equals(quest.status)?"PRIIMTI UŽDUOTĮ":"ready".equals(quest.status)?"ATSIIMTI ATLYGĮ":"SEKTI UŽDUOTĮ");action.setMinHeight(dp(48));
                action.setOnClickListener(view->{
                    if("available".equals(quest.status))feedback=db.sideQuests().accept(quest.id,state).message;
                    else if("ready".equals(quest.status))feedback=db.sideQuests().claim(quest.id,state).message;
                    else{state.trackedQuestId=quest.id;db.world().applyStructuredChoices(state);db.saveState(state);feedback="Sekama: "+quest.title;}
                    show("active".equals(quest.status)?"game":"journal");Toast.makeText(this,feedback,Toast.LENGTH_LONG).show();
                });card.addView(action);}
            threads.addView(card,sp(dp(8)));
        }
        List<WorldRepository.Event> liveEvents=db.world().activeEvents();
        if(liveEvents.isEmpty())threads.addView(txt("Šiuo metu nėra aktyvios pasaulinės krizės. Ekonomika palaipsniui grįžta į pusiausvyrą.",9,SUB,false));
        else for(WorldRepository.Event worldEvent:liveEvents)
            threads.addView(threadCard(worldEvent.title,"PAVOJUS "+worldEvent.severity,worldEvent.detail+" Kainų pokytis "+(worldEvent.priceModifier>=0?"+":"")+worldEvent.priceModifier+"%.",worldEvent.priceModifier>0?Color.rgb(218,120,77):GREEN));
        column.addView(threads, sp(dp(9)));

        LinearLayout ownership=panel(false);ownership.addView(section("VERSLAI IR PASYVIOS PAJAMOS"));
        for(WorldRepository.Business business:db.world().businesses())ownership.addView(txt((business.owned?"◆ ":"○ ")+business.name+" · "+(business.owned?"valdoma":"kaina "+business.price)+" · grynoji dienos grąža "+Math.max(0,business.revenue-business.upkeep),10,business.owned?GREEN:SUB,business.owned),sp(dp(4)));
        Button manageBusinesses=outline("VALDYTI VERSLUS");manageBusinesses.setMinHeight(dp(48));manageBusinesses.setOnClickListener(view->businessesDialog());ownership.addView(manageBusinesses);column.addView(ownership,sp(dp(9)));

        column.addView(bestiaryPanel(), sp(dp(9)));

        FactionStatusV090View factions = new FactionStatusV090View(this, state);
        column.addView(factions, new LinearLayout.LayoutParams(-1, dp(178)));

        LinearLayout politics=panel(false);politics.addView(section("FRAKCIJOS, TERITORIJOS IR MIESTŲ KONKURENCIJA"));
        for(WorldRepository.Faction faction:db.world().factions())politics.addView(txt(faction.name.toUpperCase(Locale.forLanguageTag("lt-LT"))+" · "+faction.relation+" · įtaka "+faction.influence+" · teritorija "+faction.territory+" · įtampa "+faction.tension,9,faction.tension>=60?Color.rgb(218,120,77):GREEN,true),sp(dp(4)));
        for(WorldRepository.Settlement settlement:db.world().settlements())politics.addView(txt("• "+settlement.name+" · gerovė "+settlement.prosperity+" · saugumas "+settlement.security+" · autonomija "+settlement.autonomy,9,SUB,false),sp(dp(3)));
        column.addView(politics,sp(dp(9)));

        LinearLayout log = panel(false);
        log.addView(section("PASKUTINIAI ĖJIMAI"));
        if (state.recentTurns.isEmpty()) {
            log.addView(txt("Naujų vietinių ėjimų dar nėra.", 10, SUB, false));
        } else {
            for (int index = state.recentTurns.size() - 1; index >= 0; index--) {
                log.addView(txt("• " + state.recentTurns.get(index), 10, Color.rgb(216, 221, 216), false), sp(dp(4)));
            }
        }
        column.addView(log);
        return scroll;
    }

    @Override View settings(){
        ScrollView scroll=new ScrollView(this);LinearLayout column=col();column.setPadding(dp(10),dp(7),dp(10),dp(20));scroll.addView(column);
        LinearLayout profile=panel(true);profile.addView(section("VEIKĖJO PROFILIS"));
        profile.addView(serif(state.characterName,18,PARCH,true));
        profile.addView(txt(CharacterCatalogV093.originName(state.characterOriginId)+" · "+CharacterCatalogV093.archetypeName(state.characterArchetypeId),10,SUB,false));
        profile.addView(txt("Bruožai: "+CharacterCatalogV093.traitNames(state.characterTraitIds),9,GOLD2,true),sp(dp(6)));
        Button edit=gold("KEISTI PROFILĮ IR BRUOŽUS");edit.setMinHeight(dp(48));edit.setOnClickListener(view->show("character"));profile.addView(edit);column.addView(profile,sp(dp(9)));

        LinearLayout difficulty=panel(false);difficulty.addView(section("SUNKUMO REŽIMAS"));difficulty.addView(txt("Režimas keičia savybių patikrų slenksčius, priešų žalą ir gaunamą patirtį. Pasaulio taisyklės visuose režimuose lieka tos pačios.",9,SUB,false),sp(dp(6)));
        String[][] modes={{"story","ISTORIJA · lengvesnės patikros ir 28 % mažesnė žala"},{"normal","NORMALUS · numatytas balansas"},{"hard","SUNKUS · griežtesnės patikros ir 22 % didesnė žala"},{"nightmare","KOŠMARAS · ekstremalios patikros ir 48 % didesnė žala"}};
        for(String[] mode:modes){Button button=mode[0].equals(state.difficulty)?gold("◆ "+mode[1]):dark("◇ "+mode[1]);button.setAllCaps(false);button.setMinHeight(dp(48));button.setOnClickListener(view->{db.checkpoint("prieš sunkumo pakeitimą",state);state.difficulty=mode[0];db.saveState(state);feedback="Sunkumo režimas: "+mode[1];show("settings");});difficulty.addView(button,sp(dp(5)));}column.addView(difficulty,sp(dp(9)));

        column.addView(aiSettingsPanel(),sp(dp(9)));

        LinearLayout presentation=panel(false);presentation.addView(section("PATEIKIMAS IR PRIEINAMUMAS"));presentation.addView(toggle("Sklandūs ekranų perėjimai (išjungti mažesniam judesiui)","animations",true));presentation.addView(toggle("Haptinis grįžtamasis ryšys","haptics",true));
        Switch largeText=toggle("Didesnis tekstas","large_text",false);largeText.setOnCheckedChangeListener((button,checked)->{getSharedPreferences("vaeloria_visual",MODE_PRIVATE).edit().putBoolean("large_text",checked).apply();show("settings");});presentation.addView(largeText);
        Switch colorblind=toggle("Spalvų skyrimo paletė","colorblind",false);colorblind.setOnCheckedChangeListener((button,checked)->{getSharedPreferences("vaeloria_visual",MODE_PRIVATE).edit().putBoolean("colorblind",checked).apply();show("settings");});presentation.addView(colorblind);Button tutorial=dark("PAKARTOTI GREITOS PRADŽIOS VEDLĮ");tutorial.setOnClickListener(view->{state.tutorialComplete=false;db.saveState(state);show("game");});presentation.addView(tutorial,sp(dp(3)));column.addView(presentation,sp(dp(9)));

        LinearLayout audioPanel=panel(false);audioPanel.addView(section("GARSAS"));audioPanel.addView(toggle("Sąsajos ir kovos signalai","sounds",false));Switch ambient=toggle("Procedūrinis pasaulio fonas","ambient",false);ambient.setOnCheckedChangeListener((button,checked)->{getSharedPreferences("vaeloria_visual",MODE_PRIVATE).edit().putBoolean("ambient",checked).apply();if(audio!=null){if(checked)audio.startAmbient(state.location);else audio.stopAmbient();}});audioPanel.addView(ambient);
        audioPanel.addView(txt("APLINKOS GARSUMAS",9,SUB,true));audioPanel.addView(volumeSlider("ambient_volume",22,"Aplinkos garsumas"),sp(dp(4)));audioPanel.addView(txt("SIGNALŲ GARSUMAS",9,SUB,true));audioPanel.addView(volumeSlider("sfx_volume",24,"Sąsajos signalų garsumas"));column.addView(audioPanel,sp(dp(9)));

        LinearLayout saves=panel(false);saves.addView(section("IŠSAUGOJIMAS"));
        Map<Integer,VaeloriaDb.SaveSlot> slotMap=new HashMap<>();for(VaeloriaDb.SaveSlot saved:db.saveSlots())slotMap.put(saved.slot,saved);
        for(int slotNumber=1;slotNumber<=3;slotNumber++){final int slotId=slotNumber;VaeloriaDb.SaveSlot saved=slotMap.get(slotId);LinearLayout slotCard=panel(saved!=null);slotCard.addView(serif("LIZDAS "+slotId+(saved==null?" · TUŠČIAS":" · "+saved.name),14,saved==null?SUB:PARCH,true));if(saved!=null)slotCard.addView(txt(saved.location+" · lygis "+saved.level+" · "+new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.forLanguageTag("lt-LT")).format(new java.util.Date(saved.updatedAt)),9,SUB,false),sp(dp(4)));LinearLayout slotActions=row();Button saveSlot=outline(saved==null?"IŠSAUGOTI":"PERRAŠYTI");saveSlot.setOnClickListener(view->saveSlotDialog(slotId,saved==null?"":saved.name));slotActions.addView(saveSlot,new LinearLayout.LayoutParams(0,dp(48),1));if(saved!=null){Button loadSlot=gold("ATKURTI");loadSlot.setOnClickListener(view->new AlertDialog.Builder(this).setTitle("Atkurti lizdą "+slotId+"?").setMessage("Dabartinė būsena pirmiausia bus įrašyta į atšaukimo kontrolinį tašką.").setNegativeButton("NE",null).setPositiveButton("ATKURTI",(dialog,which)->{if(db.loadFromSlot(slotId)){state=db.loadState();feedback="Atkurtas "+saved.name;show("game");}}).show());slotActions.addView(loadSlot,new LinearLayout.LayoutParams(0,dp(48),1));Button deleteSlot=dark("TRINTI");deleteSlot.setOnClickListener(view->new AlertDialog.Builder(this).setTitle("Ištrinti lizdą?").setNegativeButton("NE",null).setPositiveButton("TRINTI",(dialog,which)->{db.deleteSlot(slotId);show("settings");}).show());slotActions.addView(deleteSlot,new LinearLayout.LayoutParams(0,dp(48),1));}slotCard.addView(slotActions);saves.addView(slotCard,sp(dp(6)));}
        Button undo=dark("ATŠAUKTI PASKUTINĮ ĖJIMĄ");undo.setMinHeight(dp(48));undo.setOnClickListener(view->{if(db.undo()){state=db.loadState();feedback="Atkurtas ankstesnis kontrolinis taškas";show(state.characterCreated?"game":"character");}else Toast.makeText(this,"Nėra ankstesnio kontrolinio taško",Toast.LENGTH_SHORT).show();});saves.addView(undo,sp(dp(5)));
        Button export=dark("EKSPORTUOTI IŠSAUGOJIMĄ");export.setMinHeight(dp(48));export.setOnClickListener(view->export());saves.addView(export,sp(dp(5)));
        Button importButton=dark("IMPORTUOTI IŠSAUGOJIMĄ");importButton.setMinHeight(dp(48));importButton.setOnClickListener(view->importSave());saves.addView(importButton,sp(dp(5)));
        Button reset=dark("ATKURTI PRADINĘ BŪSENĄ");reset.setMinHeight(dp(48));reset.setOnClickListener(view->new AlertDialog.Builder(this).setTitle("Atkurti pradinę būseną?").setMessage("Bus pašalintas veikėjo profilis, vietiniai ėjimai ir įrangos pakeitimai. DI prisijungimai liks telefone.").setNegativeButton("NE",null).setPositiveButton("ATKURTI",(dialog,which)->{db.reset();state=db.loadState();feedback="";show("character");}).show());saves.addView(reset);column.addView(saves,sp(dp(9)));

        LinearLayout about=panel(false);about.addView(section("APIE VERSIJĄ"));about.addView(serif("Vaeloria OOC · "+BuildConfig.VERSION_NAME,17,PARCH,true));
        about.addView(txt("Vietinė SQLite būsena · veikėjo kūrimas · mechaniniai bruožai · lietuviškų atsakymų kontrolė",10,SUB,false));
        about.addView(txt("Atsarginės sistemos kopijos išjungtos · ryšys tik per HTTPS",9,GREEN,true));column.addView(about);return scroll;
    }

    private SeekBar volumeSlider(String key,int fallback,String description){
        SeekBar slider=new SeekBar(this);slider.setMax(100);slider.setProgress(getSharedPreferences("vaeloria_visual",MODE_PRIVATE).getInt(key,fallback));slider.setMinimumHeight(dp(48));slider.setContentDescription(description+". "+slider.getProgress()+" procentų");
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar bar,int value,boolean fromUser){bar.setContentDescription(description+". "+value+" procentų");if(fromUser)getSharedPreferences("vaeloria_visual",MODE_PRIVATE).edit().putInt(key,value).apply();}public void onStartTrackingTouch(SeekBar bar){}public void onStopTrackingTouch(SeekBar bar){if(audio!=null&&"ambient_volume".equals(key)){audio.stopAmbient();audio.startAmbient(state.location);}}});return slider;
    }

    private void saveSlotDialog(int slot,String existingName){
        EditText name=profileInput("Išsaugojimo pavadinimas",existingName==null?"":existingName,false,32);new AlertDialog.Builder(this).setTitle("Išsaugoti į lizdą "+slot).setMessage("Bus išsaugota visa veikėjo, inventoriaus, pasaulio, užduočių, NPC ir ekonomikos būsena.").setView(name).setNegativeButton("ATŠAUKTI",null).setPositiveButton("IŠSAUGOTI",(dialog,which)->{String label=name.getText().toString().trim();if(label.isEmpty())label=state.characterName+" · "+state.location;if(db.saveToSlot(slot,label)){feedback="Išsaugota į lizdą "+slot;show("settings");}else Toast.makeText(this,"Nepavyko išsaugoti lizdo",Toast.LENGTH_LONG).show();}).show();
    }

    @Override void locationHub() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = col();
        column.setPadding(dp(14), dp(12), dp(14), dp(10));
        scroll.addView(column);
        ImageView scene = image(VisualAssetCatalog.sceneFor(state.location, state.sceneTitle, false));
        scene.setContentDescription(state.location);
        column.addView(scene, new LinearLayout.LayoutParams(-1, dp(160)));
        column.addView(serif(state.location.toUpperCase(Locale.forLanguageTag("lt-LT")), 22, PARCH, true), sp(dp(2)));
        column.addView(txt(faction(state.location) + " · gyva lokacija", 9, SUB, false), sp(dp(9)));
        column.addView(section("ŽINOMOS VIETOS"));
        String[] places = state.location.toLowerCase(Locale.ROOT).contains("luminara")
                ? new String[]{"Meridiano vartai", "Didžioji akademija", "Žibinto poilsio užeiga", "Centrinė rinka", "Rezonanso dirbtuvės"}
                : new String[]{"Vietos centras", "Sargybos postas", "Prekybos aikštė", "Senieji vartai", "Meridiano pėdsakas"};
        for (String place : places) {
            Button button = dark(place);
            button.setMinHeight(dp(44));
            button.setOnClickListener(view -> act("Vykti į " + place + " vietovėje " + state.location + " ir ištirti situaciją."));
            column.addView(button, sp(dp(4)));
        }
        column.addView(section("SVARBŪS VEIKĖJAI"), sp(dp(3)));
        for (String[] npc : NPCS_V090) column.addView(npcCard(npc), sp(dp(5)));
        if (state.location.toLowerCase(Locale.ROOT).contains("luminara")) {
            Button cityPeople = gold("MIESTO GYVENTOJAI IR PASLAUGOS · 12");
            cityPeople.setMinHeight(dp(48));
            cityPeople.setOnClickListener(view -> cityPeopleDialog());
            column.addView(cityPeople, sp(dp(10)));
        }
        new AlertDialog.Builder(this).setView(scroll).setNegativeButton("UŽDARYTI", null).show();
    }

    @Override View npcCard(String[] npc) {
        LinearLayout card = row();
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(8), dp(7), dp(9), dp(7));
        card.setMinimumHeight(dp(88));
        card.setBackground(round(Color.rgb(11, 22, 29), 12, Color.rgb(40, 57, 65)));
        NpcArtView portrait = new NpcArtView(this);
        portrait.setNpc(npc[0]);
        card.addView(portrait, new LinearLayout.LayoutParams(dp(68), dp(74)));
        LinearLayout text = col();
        text.setPadding(dp(10), 0, 0, 0);
        text.addView(serif(npc[0], 15, PARCH, true));
        text.addView(txt(npc[1], 9, SUB, false));
        WorldRepository.Npc stateful=db.world().findNpc(npc[0],state.worldMinute);
        if(stateful!=null)text.addView(txt((stateful.available?"● PASIEKIAMAS":"○ NEPASIEKIAMAS")+" · santykis "+(stateful.relationship>=0?"+":"")+stateful.relationship,8,stateful.available?GREEN:Color.rgb(196,124,92),true));
        card.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        card.setOnClickListener(view -> npcDialog(npc));
        return card;
    }

    @Override void npcDialog(String[] npc) {
        LinearLayout content = col();
        content.setPadding(dp(16), dp(12), dp(16), dp(8));
        NpcArtView portrait = new NpcArtView(this);
        portrait.setNpc(npc[0]);
        content.addView(portrait, new LinearLayout.LayoutParams(-1, dp(250)));
        content.addView(serif(npc[0], 22, PARCH, true), sp(dp(3)));
        content.addView(txt(npc[1].toUpperCase(Locale.ROOT), 8, GOLD2, true), sp(dp(8)));
        content.addView(txt(npc[2], 11, Color.rgb(217, 222, 217), false));
        WorldRepository.Npc stateful=db.world().findNpc(npc[0],state.worldMinute);
        if(stateful!=null){content.addView(txt("SANTYKIS "+(stateful.relationship>=0?"+":"")+stateful.relationship+" · PASITIKĖJIMAS "+stateful.trust+" · POKALBIAI "+stateful.interactions,9,stateful.relationship>=0?GREEN:Color.rgb(215,104,84),true),sp(dp(7)));if(!stateful.lastTopic.isEmpty())content.addView(txt("PRISIMENA · "+stateful.lastTopic,9,SUB,false),sp(dp(6)));if(!stateful.available)content.addView(txt("Šiuo paros metu veikėjas nepasiekiamas. Laikas pasaulyje juda po kiekvieno veiksmo.",9,Color.rgb(215,154,91),true),sp(dp(6)));}
        String actionLabel = npc.length > 3 ? npc[3] : "KLAUSTI APIE MERIDIANĄ";
        String action = npc.length > 4 ? npc[4]
                : "Pasikalbėti su " + npc[0] + " apie Lūžusį Meridianą ir išgirsti tik tai, ką šis žmogus realiai žino.";
        WorldRepository.Shop shop=db.world().shopForNpc(npc[0]);
        AlertDialog.Builder builder=new AlertDialog.Builder(this).setView(content).setNegativeButton("UŽDARYTI",null);
        if(stateful==null||stateful.available){if(shop!=null){builder.setPositiveButton("ATVERTI PARDUOTUVĘ",(dialog,which)->shopDialog(shop));builder.setNeutralButton("KALBĖTIS",(dialog,which)->act(action));}else builder.setPositiveButton(actionLabel,(dialog,which)->act(action));}
        builder.show();
    }

    private void cityPeopleDialog() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = col();
        column.setPadding(dp(14), dp(12), dp(14), dp(10));
        scroll.addView(column);
        column.addView(serif("LUMINAROS GYVENTOJAI", 23, PARCH, true));
        column.addView(txt("Čia susitinka miesto amatai, paslaugos, žinios ir kasdieniai interesai.", 10, SUB, false), sp(dp(10)));
        for (int index = 0; index < CITY_NPCS_V090.length; index++) {
            if (index == 0) column.addView(section("PREKYBA, AMATAI IR PASLAUGOS"));
            if (index == 6) column.addView(section("TVARKA, KELIAI IR MIESTO GYVENIMAS"), sp(dp(10)));
            column.addView(npcCard(CITY_NPCS_V090[index]), sp(dp(5)));
        }
        new AlertDialog.Builder(this).setView(scroll).setNegativeButton("UŽDARYTI", null).show();
    }

    private void shopDialog(WorldRepository.Shop shop){
        ScrollView scroll=new ScrollView(this);LinearLayout column=col();column.setPadding(dp(14),dp(12),dp(14),dp(10));scroll.addView(column);
        column.addView(serif(shop.name,22,PARCH,true));column.addView(txt("Turimos karūnos · "+state.crowns+" · kainos priklauso nuo pasiūlos, pasaulio įvykių ir santykio su pardavėju.",9,SUB,false),sp(dp(8)));
        LinearLayout actions=row();Button sell=outline("PARDUOTI");sell.setMinHeight(dp(48));sell.setOnClickListener(view->sellDialog(shop));actions.addView(sell,new LinearLayout.LayoutParams(0,dp(48),1));Space gap=new Space(this);actions.addView(gap,new LinearLayout.LayoutParams(dp(7),1));Button craft=outline("GAMINTI");craft.setMinHeight(dp(48));craft.setOnClickListener(view->craftDialog(shop));actions.addView(craft,new LinearLayout.LayoutParams(0,dp(48),1));column.addView(actions,sp(dp(9)));
        List<WorldRepository.Stock> stock=db.world().stock(shop,state);if(stock.isEmpty())column.addView(txt("Atsargos išpirktos. Parduotuvė pasipildys kitą pasaulio dieną.",10,SUB,false));
        for(WorldRepository.Stock entry:stock){boolean levelReady=state.level>=entry.level;LinearLayout item=panel(false);LinearLayout header=row();header.setGravity(Gravity.CENTER_VERTICAL);header.addView(serif(entry.name,13,PARCH,true),new LinearLayout.LayoutParams(0,-2,1));header.addView(chip(rarityLabel(entry.rarity).toUpperCase(Locale.ROOT),rarity(entry.rarity)));item.addView(header);item.addView(txt(ItemCatalogV092.categoryLabel(entry.category)+" · L"+entry.level+" · liko "+entry.quantity+(levelReady?"":" · REIKIA L"+entry.level),8,levelReady?SUB:Color.rgb(215,154,91),!levelReady));Button buy=gold(levelReady?"PIRKTI · "+entry.price+" KARŪNŲ":"UŽRAKINTA IKI L"+entry.level);buy.setMinHeight(dp(46));buy.setEnabled(levelReady&&state.crowns>=entry.price);buy.setOnClickListener(view->new AlertDialog.Builder(this).setTitle(entry.name).setMessage("Pirkti už "+entry.price+" karūnų?").setNegativeButton("NE",null).setPositiveButton("PIRKTI",(dialog,which)->{db.checkpoint("prieš pirkimą",state);WorldRepository.TransactionResult result=db.world().buy(shop,entry.catalogId,state);feedback=result.message;Toast.makeText(this,result.message,Toast.LENGTH_LONG).show();shopDialog(shop);}).show());item.addView(buy,sp(dp(2)));column.addView(item,sp(dp(6)));}
        new AlertDialog.Builder(this).setView(scroll).setNegativeButton("UŽDARYTI",null).show();
    }

    private void sellDialog(WorldRepository.Shop shop){ArrayList<VaeloriaDb.Item> sellable=new ArrayList<>();for(VaeloriaDb.Item item:db.getItems())if(!state.favoriteItemIds.contains(item.id)&&!item.equipped&&!item.synced&&item.value>0&&!"quest".equals(item.type))sellable.add(item);if(sellable.isEmpty()){Toast.makeText(this,"Nėra parduodamų daiktų",Toast.LENGTH_SHORT).show();return;}String[] names=new String[sellable.size()];for(int i=0;i<sellable.size();i++){VaeloriaDb.Item item=sellable.get(i);names[i]=item.name+(item.quantity>1?" ×"+item.quantity:"")+" · gausi "+db.world().sellPrice(shop,item,state)+" karūnų";}new AlertDialog.Builder(this).setTitle("Parduoti vieną daiktą").setItems(names,(dialog,which)->{VaeloriaDb.Item item=sellable.get(which);new AlertDialog.Builder(this).setTitle("Parduoti: "+item.name+"?").setMessage("Gausi "+db.world().sellPrice(shop,item,state)+" karūnų už vieną vienetą.").setNegativeButton("ATŠAUKTI",null).setPositiveButton("PARDUOTI",(confirmation,choice)->{db.checkpoint("prieš pardavimą",state);WorldRepository.TransactionResult result=db.world().sell(shop,item.id,state);feedback=result.message;Toast.makeText(this,result.message,Toast.LENGTH_LONG).show();show("items");}).show();}).setNegativeButton("UŽDARYTI",null).show();}

    private void craftDialog(WorldRepository.Shop shop){String station=stationForShop(shop);ScrollView scroll=new ScrollView(this);LinearLayout column=col();column.setPadding(dp(14),dp(12),dp(14),dp(10));scroll.addView(column);column.addView(serif("GAMYBA · "+WorldRepository.stationLabel(station).toUpperCase(Locale.forLanguageTag("lt-LT")),21,PARCH,true));column.addView(txt("Receptai tikrina veikėjo lygį, talentą, darbo vietą, visus reagentus ir karūnas vienoje atšaukiamoje operacijoje.",9,SUB,false),sp(dp(8)));int shown=0;for(WorldRepository.Recipe recipe:db.world().recipes()){if(!station.equals(recipe.station))continue;shown++;boolean ingredientsReady=true;StringBuilder ingredients=new StringBuilder();for(WorldRepository.Ingredient ingredient:recipe.ingredients){if(ingredients.length()>0)ingredients.append(" · ");ingredients.append(ingredient.name).append(' ').append(ingredient.owned).append('/').append(ingredient.quantity);if(ingredient.owned<ingredient.quantity)ingredientsReady=false;}boolean levelReady=state.level>=recipe.requiredLevel;boolean ready=recipe.unlocked&&levelReady&&ingredientsReady&&state.crowns>=recipe.fee;String lock=!recipe.unlocked?"REIKIA TALENTO":!levelReady?"REIKIA L"+recipe.requiredLevel:!ingredientsReady?"TRŪKSTA REAGENTŲ":state.crowns<recipe.fee?"TRŪKSTA KARŪNŲ":"PARUOŠTA";Button button=dark(recipe.name+(recipe.resultQuantity>1?" ×"+recipe.resultQuantity:"")+" · L"+recipe.requiredLevel+"\n"+ingredients+"\n"+recipe.fee+" karūnų · "+lock);button.setAllCaps(false);button.setMinHeight(dp(76));button.setEnabled(ready);button.setOnClickListener(view->{db.checkpoint("prieš gamybą",state);WorldRepository.TransactionResult result=db.world().craft(recipe.id,state,station);feedback=result.message;Toast.makeText(this,result.message,Toast.LENGTH_LONG).show();show("items");});column.addView(button,sp(dp(5)));}if(shown==0)column.addView(txt("Šioje darbo vietoje receptų nėra.",10,SUB,false));new AlertDialog.Builder(this).setView(scroll).setNegativeButton("UŽDARYTI",null).show();}

    private String stationForShop(WorldRepository.Shop shop){if(shop==null)return"field";if("smith".equals(shop.id)||"armor".equals(shop.id))return"forge";if("pharmacy".equals(shop.id))return"alchemy";if("runes".equals(shop.id))return"runic";return"field";}

    private void businessesDialog(){
        ScrollView scroll=new ScrollView(this);LinearLayout column=col();column.setPadding(dp(14),dp(12),dp(14),dp(10));scroll.addView(column);
        column.addView(serif("VERSLAI",22,PARCH,true));
        column.addView(txt("Valdomas verslas kartą per pasaulio dieną išmoka pajamas po išlaikymo sąnaudų. Ekonomikos indeksas keičia realią grąžą.",9,SUB,false),sp(dp(9)));
        for(WorldRepository.Business business:db.world().businesses()){
            LinearLayout card=panel(business.owned);card.addView(serif(business.name,15,PARCH,true));
            card.addView(txt("Lygis "+business.level+" · pajamos "+business.revenue+" · sąnaudos "+business.upkeep+" · grynoji bazė "+Math.max(0,business.revenue-business.upkeep),9,SUB,false));
            if(business.owned)card.addView(txt("VALDOMA · kita išmoka pasaulio minutę "+business.nextPayout,9,GREEN,true));
            else{Button buy=gold("PIRKTI · "+business.price+" KARŪNŲ");buy.setMinHeight(dp(46));buy.setEnabled(state.crowns>=business.price);buy.setOnClickListener(view->{db.checkpoint("prieš verslo pirkimą",state);WorldRepository.TransactionResult result=db.world().buyBusiness(business.id,state);feedback=result.message;Toast.makeText(this,result.message,Toast.LENGTH_LONG).show();show("journal");});card.addView(buy,sp(dp(3)));}
            column.addView(card,sp(dp(6)));
        }
        column.addView(section("SAMDOMI DARBUOTOJAI"),sp(dp(4)));
        List<WorldRepository.Hire> hires=db.world().hires();
        if(hires.isEmpty())column.addView(txt("Dar neturi samdomų darbuotojų.",9,SUB,false),sp(dp(5)));
        else for(WorldRepository.Hire hire:hires)column.addView(txt((hire.active?"◆ ":"○ ")+hire.name+" · "+hire.job+" · alga "+hire.wage+" · lojalumas "+hire.loyalty,10,hire.active?GREEN:SUB,true),sp(dp(4)));
        Button hire=outline("SAMDYTI NPC");hire.setMinHeight(dp(48));hire.setOnClickListener(view->hireDialog());column.addView(hire,sp(dp(5)));
        new AlertDialog.Builder(this).setView(scroll).setNegativeButton("UŽDARYTI",null).show();
    }

    private void hireDialog(){List<WorldRepository.Npc> candidates=db.world().hireCandidates(state.worldMinute);if(candidates.isEmpty()){Toast.makeText(this,"Šiuo metu nėra pasiekiamų kandidatų",Toast.LENGTH_SHORT).show();return;}String[] names=new String[candidates.size()];for(int i=0;i<candidates.size();i++){WorldRepository.Npc npc=candidates.get(i);int wage=60+Math.max(0,npc.trust);names[i]=npc.name+" · "+npc.role+" · "+wage+" karūnų per dieną";}new AlertDialog.Builder(this).setTitle("Samdyti darbuotoją").setItems(names,(dialog,which)->{WorldRepository.Npc npc=candidates.get(which);int wage=60+Math.max(0,npc.trust);db.checkpoint("prieš NPC samdymą",state);WorldRepository.TransactionResult result=db.world().hire(npc.id,npc.role,wage,state);feedback=result.message;Toast.makeText(this,result.message,Toast.LENGTH_LONG).show();show("journal");}).setNegativeButton("UŽDARYTI",null).show();}

    private View bestiaryPanel() {
        LinearLayout panel = panel(false);
        panel.addView(section("BESTIARIUMAS · 218 ILIUSTRUOTŲ GRĖSMIŲ"));
        panel.addView(serif("Pažintos Vaelorios būtybės", 17, PARCH, true));
        panel.addView(txt("Pasirink grėsmių grupę ir pasiruošk pagal jos pavojų, aplinką, elgseną bei tikslias grobio iškritimo tikimybes.", 9, SUB, false), sp(dp(7)));
        Button search=gold("IEŠKOTI VISAME BESTIARIUME");search.setOnClickListener(view->bestiarySearchDialog());panel.addView(search,sp(dp(7)));
        String[] categories = {"LAUKINIAI", "ANOMALIJOS", "PASAULIO BOSAI"};
        for (String category : categories) {
            Button button = dark(category + " · 6");
            button.setMinHeight(dp(48));
            button.setOnClickListener(view -> bestiaryDialog(category));
            panel.addView(button, sp(dp(5)));
        }
        panel.addView(section("REGIONINIAI ĮRAŠAI · 200"), sp(dp(10)));
        for (String region : EnemyCatalogV091.regions()) {
            Button button = dark(region + " · 25");
            button.setMinHeight(dp(48));
            button.setOnClickListener(view -> extendedBestiaryDialog(region));
            panel.addView(button, sp(dp(5)));
        }
        return panel;
    }

    private void bestiarySearchDialog() {
        LinearLayout content = col();
        content.setPadding(dp(18), dp(14), dp(18), dp(8));
        content.addView(serif("Bestiariumo paieška", 22, PARCH, true));
        content.addView(txt("Ieškok pagal būtybės vardą, regioną, grupę, kovos vaidmenį arba ypatingą savybę. Paieška nepaiso lietuviškų diakritinių ženklų.", 10, SUB, false), sp(dp(10)));
        EditText query = profileInput("Pvz., drakonas, pelkė, sargas…", "", false, 48);
        query.setSingleLine(true);
        content.addView(query);
        new AlertDialog.Builder(this)
                .setView(content)
                .setNegativeButton("UŽDARYTI", null)
                .setPositiveButton("IEŠKOTI", (dialog, which) ->
                        showBestiarySearchResults(query.getText().toString().trim(), 0))
                .show();
    }

    private void showBestiarySearchResults(String query, int requestedPage) {
        ArrayList<Object> matches = new ArrayList<>();
        String needle = searchable(query);
        for (String[] monster : MONSTERS_V090) {
            String haystack = searchable(monster[0] + " " + monster[1] + " " + monster[3]
                    + " pavojus " + monster[2]);
            if (containsEveryWord(haystack, needle)) matches.add(monster);
        }
        for (EnemyCatalogV091.Enemy enemy : EnemyCatalogV091.ALL) {
            String haystack = searchable(enemy.name + " " + enemy.region + " " + enemy.role + " "
                    + enemy.trait + " " + enemy.description + " pavojus " + enemy.danger);
            if (containsEveryWord(haystack, needle)) matches.add(enemy);
        }

        final int pageSize = 20;
        int pageCount = Math.max(1, (matches.size() + pageSize - 1) / pageSize);
        int page = Math.max(0, Math.min(requestedPage, pageCount - 1));
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = col();
        column.setPadding(dp(12), dp(12), dp(12), dp(10));
        scroll.addView(column);
        column.addView(serif(query.isEmpty() ? "Visas bestiariumas" : "Paieška · „" + query + "“", 21, PARCH, true));
        column.addView(txt("RASTA " + matches.size() + " IŠ 218 · PUSLAPIS " + (page + 1) + "/" + pageCount,
                9, matches.isEmpty() ? dangerColor(8) : GOLD2, true), sp(dp(8)));
        if (matches.isEmpty()) {
            column.addView(txt("Atitikmenų nėra. Pabandyk trumpesnį vardą, regioną arba bendresnį žodį.", 11, SUB, false), sp(dp(10)));
        } else {
            int start = page * pageSize;
            int end = Math.min(matches.size(), start + pageSize);
            for (int index = start; index < end; index += 2) {
                LinearLayout pair = row();
                Object first = matches.get(index);
                pair.addView(first instanceof String[] ? monsterCard((String[]) first)
                                : extendedMonsterCard((EnemyCatalogV091.Enemy) first),
                        new LinearLayout.LayoutParams(0, dp(226), 1));
                pair.addView(new Space(this), new LinearLayout.LayoutParams(dp(7), 1));
                if (index + 1 < end) {
                    Object second = matches.get(index + 1);
                    pair.addView(second instanceof String[] ? monsterCard((String[]) second)
                                    : extendedMonsterCard((EnemyCatalogV091.Enemy) second),
                            new LinearLayout.LayoutParams(0, dp(226), 1));
                } else {
                    pair.addView(new View(this), new LinearLayout.LayoutParams(0, dp(226), 1));
                }
                column.addView(pair, sp(dp(7)));
            }
        }

        LinearLayout navigation = row();
        Button previous = outline("← ANKSTESNIS");
        previous.setEnabled(page > 0);
        final int previousPage = page - 1;
        previous.setOnClickListener(view -> showBestiarySearchResults(query, previousPage));
        navigation.addView(previous, new LinearLayout.LayoutParams(0, dp(50), 1));
        navigation.addView(new Space(this), new LinearLayout.LayoutParams(dp(7), 1));
        Button next = outline("KITAS →");
        next.setEnabled(page + 1 < pageCount);
        final int nextPage = page + 1;
        next.setOnClickListener(view -> showBestiarySearchResults(query, nextPage));
        navigation.addView(next, new LinearLayout.LayoutParams(0, dp(50), 1));
        column.addView(navigation, sp(dp(6)));

        new AlertDialog.Builder(this)
                .setView(scroll)
                .setNegativeButton("UŽDARYTI", null)
                .setPositiveButton("NAUJA PAIEŠKA", (dialog, which) -> bestiarySearchDialog())
                .show();
    }

    private String searchable(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "").toLowerCase(Locale.forLanguageTag("lt-LT")).trim();
    }

    private boolean containsEveryWord(String haystack, String needle) {
        if (needle.isEmpty()) return true;
        for (String word : needle.split("\\s+")) if (!haystack.contains(word)) return false;
        return true;
    }

    private void bestiaryDialog(String category) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = col();
        column.setPadding(dp(12), dp(12), dp(12), dp(10));
        scroll.addView(column);
        column.addView(serif(category, 22, PARCH, true));
        column.addView(txt("Bakstelėk būtybę, kad pamatytum pilną portretą ir grėsmės aprašą.", 9, SUB, false), sp(dp(9)));
        List<String[]> matches = new ArrayList<>();
        for (String[] monster : MONSTERS_V090) if (category.equals(monster[1])) matches.add(monster);
        for (int index = 0; index < matches.size(); index += 2) {
            LinearLayout pair = row();
            pair.addView(monsterCard(matches.get(index)), new LinearLayout.LayoutParams(0, dp(206), 1));
            Space gap = new Space(this);
            pair.addView(gap, new LinearLayout.LayoutParams(dp(7), 1));
            if (index + 1 < matches.size()) {
                pair.addView(monsterCard(matches.get(index + 1)), new LinearLayout.LayoutParams(0, dp(206), 1));
            } else {
                pair.addView(new View(this), new LinearLayout.LayoutParams(0, dp(206), 1));
            }
            column.addView(pair, sp(dp(7)));
        }
        new AlertDialog.Builder(this).setView(scroll).setNegativeButton("UŽDARYTI", null).show();
    }

    private View monsterCard(String[] monster) {
        LinearLayout card = col();
        int danger = Integer.parseInt(monster[2]);
        int accent = dangerColor(danger);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setMinimumHeight(dp(196));
        card.setBackground(round(PANEL_2, 14,
                Color.argb(170, Color.red(accent), Color.green(accent), Color.blue(accent))));
        MonsterArtView art = new MonsterArtView(this);
        art.setMonster(monster[0]);
        card.addView(art, new LinearLayout.LayoutParams(-1, dp(126)));
        TextView name = serif(monster[0], 12, PARCH, true);
        name.setMaxLines(2);
        card.addView(name, sp(dp(3)));
        card.addView(txt("PAVOJUS · " + danger + "/10", 8, accent, true));
        card.setContentDescription(monster[0] + ". Pavojus " + danger + " iš 10");
        card.setOnClickListener(view -> monsterDialog(monster));
        return card;
    }

    private void monsterDialog(String[] monster) {
        LinearLayout content = col();
        content.setPadding(dp(16), dp(12), dp(16), dp(7));
        MonsterArtView art = new MonsterArtView(this);
        art.setMonster(monster[0]);
        content.addView(art, new LinearLayout.LayoutParams(-1, dp(260)));
        content.addView(serif(monster[0], 22, PARCH, true), sp(dp(4)));
        int danger = Integer.parseInt(monster[2]);
        content.addView(txt(monster[1] + " · PAVOJUS " + danger + "/10", 9, dangerColor(danger), true), sp(dp(7)));
        content.addView(txt(monster[3], 11, Color.rgb(217, 222, 217), false));
        content.addView(txt(dropPreview(monster[0]), 9, GOLD2, true), sp(dp(8)));
        new AlertDialog.Builder(this)
                .setView(content)
                .setNegativeButton("UŽDARYTI", null)
                .setPositiveButton("IEŠKOTI PĖDSAKŲ", (dialog, which) ->
                        act("Ieškoti " + monster[0] + " pėdsakų saugiai, neinicijuojant kovos be aiškaus mano sprendimo."))
                .show();
    }

    private void extendedBestiaryDialog(String region) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = col();
        column.setPadding(dp(12), dp(12), dp(12), dp(10));
        scroll.addView(column);
        column.addView(serif(region, 22, PARCH, true));
        column.addView(txt("25 iliustruotos grėsmės su kovos statistika. Bakstelėk įrašą išsamiai analizei.", 9, SUB, false), sp(dp(9)));
        List<EnemyCatalogV091.Enemy> matches = EnemyCatalogV091.inRegion(region);
        for (int index = 0; index < matches.size(); index += 2) {
            LinearLayout pair = row();
            pair.addView(extendedMonsterCard(matches.get(index)), new LinearLayout.LayoutParams(0, dp(222), 1));
            pair.addView(new Space(this), new LinearLayout.LayoutParams(dp(7), 1));
            if (index + 1 < matches.size()) {
                pair.addView(extendedMonsterCard(matches.get(index + 1)), new LinearLayout.LayoutParams(0, dp(222), 1));
            } else {
                pair.addView(new View(this), new LinearLayout.LayoutParams(0, dp(222), 1));
            }
            column.addView(pair, sp(dp(7)));
        }
        new AlertDialog.Builder(this).setView(scroll).setNegativeButton("UŽDARYTI", null).show();
    }

    private View extendedMonsterCard(EnemyCatalogV091.Enemy enemy) {
        LinearLayout card = col();
        int accent = dangerColor(enemy.danger);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setMinimumHeight(dp(212));
        card.setBackground(round(PANEL_2, 14,
                Color.argb(170, Color.red(accent), Color.green(accent), Color.blue(accent))));
        MonsterArtView art = new MonsterArtView(this);
        art.setMonster(enemy.name);
        card.addView(art, new LinearLayout.LayoutParams(-1, dp(126)));
        TextView name = serif(enemy.name, 12, PARCH, true);
        name.setMaxLines(2);
        card.addView(name, sp(dp(3)));
        card.addView(txt(enemy.role.toUpperCase(Locale.forLanguageTag("lt-LT")), 7, SUB, true));
        card.addView(txt("PAVOJUS · " + enemy.danger + "/10", 8, accent, true));
        card.setContentDescription(enemy.name + ". Pavojus " + enemy.danger + " iš 10");
        card.setOnClickListener(view -> extendedMonsterDialog(enemy));
        return card;
    }

    private void extendedMonsterDialog(EnemyCatalogV091.Enemy enemy) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = col();
        content.setPadding(dp(16), dp(12), dp(16), dp(8));
        scroll.addView(content);
        MonsterArtView art = new MonsterArtView(this);
        art.setMonster(enemy.name);
        content.addView(art, new LinearLayout.LayoutParams(-1, dp(260)));
        content.addView(serif(enemy.name, 22, PARCH, true), sp(dp(4)));
        content.addView(txt(enemy.region + " · PAVOJUS " + enemy.danger + "/10", 9,
                dangerColor(enemy.danger), true), sp(dp(6)));
        content.addView(txt(enemy.role.toUpperCase(Locale.forLanguageTag("lt-LT")) + " · " + enemy.trait,
                9, GOLD2, true), sp(dp(6)));
        content.addView(txt("GYVYBĖ " + enemy.hp + "   ATAKA " + enemy.attack + "   GYNYBA "
                + enemy.defense + "   GREITIS " + enemy.speed, 10, Color.rgb(205, 219, 218), true), sp(dp(7)));
        content.addView(txt(enemy.description, 11, Color.rgb(217, 222, 217), false));
        content.addView(txt(dropPreview(enemy.name), 9, GOLD2, true), sp(dp(8)));
        new AlertDialog.Builder(this)
                .setView(scroll)
                .setNegativeButton("UŽDARYTI", null)
                .setPositiveButton("IEŠKOTI PĖDSAKŲ", (dialog, which) ->
                        act("Ieškoti " + enemy.name + " pėdsakų saugiai, neinicijuojant kovos be aiškaus mano sprendimo."))
                .show();
    }

    private String dropPreview(String monsterName) {
        DropTableV092.MonsterProfile profile = DropTableV092.profileFor(monsterName);
        StringBuilder result = new StringBuilder("GROBIO RATAI · ").append(profile.drops.rolls).append(" met.")
                .append(" · iškritimas ").append(profile.drops.dropChanceBasisPoints / 100).append("% kiekvienam\n")
                .append(profile.drops.compactRates());
        List<ItemCatalogV092.ItemDef> samples = DropTableV092.sample(monsterName, 4);
        if (!samples.isEmpty()) {result.append("\nGALIMI RADINIAI · ");for (int index=0;index<samples.size();index++){if(index>0)result.append(", ");result.append(samples.get(index).name);}}
        return result.toString();
    }

    @Override void itemDialog(VaeloriaDb.Item item) {
        LinearLayout content = col();
        content.setPadding(dp(18), dp(14), dp(18), dp(6));
        ItemArtView artwork = new ItemArtView(this);
        artwork.setItem(item.name, item.slot == null ? item.type : item.slot, item.rarity);
        content.addView(artwork, new LinearLayout.LayoutParams(-1, dp(230)));
        content.addView(serif(item.name, 21, PARCH, true), sp(dp(4)));
        content.addView(txt(rarityLabel(item.rarity).toUpperCase(Locale.ROOT), 9, rarity(item.rarity), true), sp(dp(7)));
        content.addView(txt("LYGIS " + item.itemLevel + " · GALIA " + item.power + " · VERTĖ " + item.value + (item.quantity > 1 ? " · KIEKIS ×" + item.quantity : ""), 9, GOLD2, true), sp(dp(5)));
        content.addView(txt(itemDescriptor(item), 9, GOLD2, true), sp(dp(5)));
        content.addView(txt(item.description, 11, Color.rgb(218, 223, 218), false));
        if (item.effect != null && !item.effect.isEmpty()) content.addView(txt("MECHANIKA · " + (definitionFor(item)!=null&&definitionFor(item).consumable?ConsumableRulesV110.description(definitionFor(item)):item.effect), 10, GREEN, true), sp(dp(7)));
        if (item.setId != null && !item.setId.isEmpty()) {ItemCatalogV092.SetDef set=ItemCatalogV092.setById(item.setId);if(set!=null)content.addView(txt("SETAS · "+set.name,10,rarity(set.rarity),true),sp(dp(6)));}
        ItemCatalogV092.ItemDef definition=item.catalogId==null?ItemCatalogV092.find(item.name):ItemCatalogV092.byId(item.catalogId);
        ScrollView details=new ScrollView(this);details.addView(content);
        Button favorite=outline(state.favoriteItemIds.contains(item.id)?"★ PAŠALINTI IŠ MĖGSTAMŲ":"☆ PAŽYMĖTI KAIP MĖGSTAMĄ");
        favorite.setOnClickListener(view->{if(state.favoriteItemIds.contains(item.id))state.favoriteItemIds.remove(item.id);else state.favoriteItemIds.add(item.id);db.saveState(state);favorite.setText(state.favoriteItemIds.contains(item.id)?"★ PAŠALINTI IŠ MĖGSTAMŲ":"☆ PAŽYMĖTI KAIP MĖGSTAMĄ");});content.addView(favorite,sp(dp(8)));
        if(item.slot!=null){Button compare=gold("PALYGINTI IR APRŪPINTI");compare.setOnClickListener(view->compareEquipmentDialog(item));content.addView(compare,sp(dp(6)));}
        AlertDialog.Builder builder=new AlertDialog.Builder(this).setView(details).setNegativeButton("UŽDARYTI",null);
        if(definition!=null&&definition.consumable){boolean levelReady=state.level>=definition.level;builder.setPositiveButton(levelReady?"NAUDOTI":"REIKIA L"+definition.level,(dialog,which)->{if(!levelReady){feedback="Negalima naudoti · reikia "+definition.level+" veikėjo lygio";show("items");return;}boolean wasCombat=state.combatActive;String result=db.consumeItem(item.id,state);if(result!=null)feedback=result;show(wasCombat?"game":"items");});}
        builder.show();
    }

    private void compareEquipmentDialog(VaeloriaDb.Item item){
        ArrayList<String> targets=new ArrayList<>();for(String slot:VaeloriaDb.EQUIPMENT_SLOTS)if(slot.equals(item.slot)||slot.startsWith(item.slot+"_"))targets.add(slot);
        if(targets.isEmpty())return;
        String[] labels=new String[targets.size()];for(int i=0;i<targets.size();i++){VaeloriaDb.Item current=db.getEquippedAt(targets.get(i));labels[i]=VaeloriaDb.slotLabel(targets.get(i))+" · "+(current==null?"tuščia":current.name);}
        new AlertDialog.Builder(this).setTitle("Pasirink įrangos vietą").setItems(labels,(dialog,which)->{
            String target=targets.get(which);String comparison=EquipmentRules.comparison(db.getItems(),item,target);
            new AlertDialog.Builder(this).setTitle(item.name).setMessage(comparison+"\n\n"+EquipmentRules.itemMechanic(item)+"\n"+db.equipRequirement(item,state.level))
                    .setNegativeButton("ATŠAUKTI",null).setPositiveButton(state.level<item.itemLevel?"REIKIA L"+item.itemLevel:"APRŪPINTI",(confirm,choice)->{
                        if(state.level<item.itemLevel)return;db.checkpoint("prieš įrangos pakeitimą",state);
                        boolean equipped=db.equipToSlot(item.id,target,state.level);state=db.loadState();feedback=equipped?"Aprūpinta: "+item.name:"Įrangos pakeisti nepavyko";show("items");
                    }).show();
        }).show();
    }

    private ItemCatalogV092.ItemDef definitionFor(VaeloriaDb.Item item){return item.catalogId==null?ItemCatalogV092.find(item.name):ItemCatalogV092.byId(item.catalogId);}

    private LinearLayout panel(boolean gold) {
        LinearLayout panel = col();
        panel.setPadding(dp(13), dp(12), dp(13), dp(12));
        panel.setBackground(round(gold ? Color.rgb(12, 24, 32) : PANEL, 16,
                gold ? Color.argb(150, 221, 187, 104) : LINE));
        return panel;
    }

    @Override TextView section(String value) {
        TextView section = txt(value, 9, GOLD2, true);
        section.setLetterSpacing(.09f);
        section.setPadding(0, 0, 0, dp(6));
        return section;
    }

    private TextView chip(String value, int color) {
        TextView chip = txt(value, 8, color, true);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(8), dp(4), dp(8), dp(4));
        chip.setBackground(round(Color.argb(34, Color.red(color), Color.green(color), Color.blue(color)), 18,
                Color.argb(110, Color.red(color), Color.green(color), Color.blue(color))));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.setMargins(0, 0, dp(5), dp(4));
        chip.setLayoutParams(params);
        return chip;
    }

    @Override View choice(int number, String action) {
        LinearLayout row = row();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(10), dp(10), dp(10));
        row.setMinimumHeight(dp(58));
        row.setBackground(round(Color.rgb(11, 23, 31), 14, Color.rgb(43, 60, 69)));
        TextView index = serif(String.valueOf(number), 16, GOLD2, true);
        index.setGravity(Gravity.CENTER);
        index.setBackground(round(Color.rgb(23, 34, 38), 30, Color.argb(180, 221, 187, 104)));
        row.addView(index, new LinearLayout.LayoutParams(dp(38), dp(38)));
        TextView text = txt(action, 12, Color.rgb(233, 234, 228), false);
        text.setPadding(dp(10), 0, 0, 0);
        row.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        TextView arrow = txt("›", 22, Color.rgb(112, 131, 137), false);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(24), -2));
        row.setOnTouchListener((view, event) -> {
            if (pref("animations", true)) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    view.animate().scaleX(.985f).scaleY(.985f).setDuration(55).start();
                } else if (event.getAction() == android.view.MotionEvent.ACTION_UP
                        || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(85).start();
                }
            }
            return false;
        });
        row.setOnClickListener(view -> {
            haptic();
            sound(false);
            act(action);
        });
        return row;
    }

    @Override View abilityCard(String[] ability) {
        String rawType = ability.length > 1 ? ability[1] : "";
        String type = rawType.toLowerCase(Locale.ROOT);
        String label;
        int accent;
        if (type.contains("technique") || type.contains("technik")) {
            label = "TECHNIKA";
            accent = BLUE;
        } else if (type.contains("refinement") || type.contains("tobulin")) {
            label = "TOBULINIMAS VIRŠ RIBOS";
            accent = Color.rgb(79, 169, 204);
        } else if (type.contains("principle") || type.contains("princip")) {
            label = "PRINCIPAS VIRŠ RIBOS";
            accent = PURPLE;
        } else if (type.contains("unknown") || type.contains("nežinom")) {
            label = "NEŽINOMA KLASĖ";
            accent = Color.rgb(194, 92, 178);
        } else {
            label = "GEBĖJIMAS VIRŠ RIBOS";
            accent = GOLD2;
        }
        LinearLayout card = col();
        card.setPadding(dp(10), dp(9), dp(10), dp(9));
        card.setBackground(round(Color.rgb(12, 24, 32), 12,
                Color.argb(145, Color.red(accent), Color.green(accent), Color.blue(accent))));
        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = serif(ability[0], 13, PARCH, true);
        title.setMaxLines(2);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(chip(label, accent));
        card.addView(header);
        card.addView(txt(ability.length > 2 ? ability[2] : "", 9, SUB, false));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(6));
        card.setLayoutParams(params);
        return card;
    }

    private View threadCard(String title, String status, String detail, int accent) {
        LinearLayout card = col();
        card.setPadding(dp(10), dp(8), dp(10), dp(8));
        card.setBackground(round(Color.rgb(11, 23, 30), 11,
                Color.argb(135, Color.red(accent), Color.green(accent), Color.blue(accent))));
        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = txt(title, 10, PARCH, true);
        name.setMaxLines(2);
        header.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(chip(status, accent));
        card.addView(header);
        card.addView(txt(detail, 8, SUB, false));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(5));
        card.setLayoutParams(params);
        return card;
    }

    private View feedbackCard() {
        String lower = feedback.toLowerCase(Locale.ROOT);
        boolean failed = lower.contains("nesėkm") || lower.contains("nepavyk") || lower.contains("failure");
        int accent = failed ? Color.rgb(210, 102, 78) : GREEN;
        LinearLayout box = col();
        box.setPadding(dp(12), dp(9), dp(12), dp(9));
        box.setBackground(round(Color.rgb(11, 30, 29), 13,
                Color.argb(145, Color.red(accent), Color.green(accent), Color.blue(accent))));
        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(txt(failed ? "NESĖKMĖ" : "SĖKMĖ", 9, accent, true), new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(chip("REZULTATAS", accent));
        box.addView(header);
        box.addView(txt(failed ? "Veiksmas nepavyko; pasaulis pritaikė pasekmes."
                : "Veiksmas pavyko; pasaulio būsena atnaujinta.", 11, Color.rgb(229, 234, 226), true));
        box.addView(txt(feedbackChanges(), 8, SUB, false), sp(dp(3)));
        TextView details = txt("KAIP APSKAIČIUOTA?  ›", 7, GOLD2, true);
        details.setPadding(0, dp(5), 0, 0);
        box.addView(details);
        box.setOnClickListener(view -> feedbackDetails());
        return box;
    }

    private String feedbackChanges() {
        ArrayList<String> changes = new ArrayList<>();
        for (String part : feedback.split("·")) {
            String value = part.trim();
            String lower = value.toLowerCase(Locale.ROOT);
            if (lower.contains("meistriškumas") || lower.contains("ištvermė") || lower.contains("mana")
                    || lower.contains("gyvybė") || lower.contains("eonas") || lower.contains("karūnos")
                    || value.contains("📍") || value.contains("🎁")) {
                if (value.length() > 48) value = value.substring(0, 47) + "…";
                changes.add(value);
                if (changes.size() >= 3) break;
            }
        }
        return changes.isEmpty() ? "Scena ir pasaulio būsena atnaujintos." : android.text.TextUtils.join(" · ", changes);
    }

    private void feedbackDetails() {
        new AlertDialog.Builder(this)
                .setTitle("Kaip apskaičiuota?")
                .setMessage(feedback)
                .setPositiveButton("UŽDARYTI", null)
                .show();
    }

    @Override void finish(String action, org.json.JSONObject result, String warning) {
        if(!busy||pendingResolvedTurn==null)return;
        String event = pendingResolvedTurn.optString("event_tag", "");
        long turnBefore=state.turnNumber;
        super.finish(action, result, warning);
        if (audio != null && state.turnNumber>turnBefore) {
            VaeloriaAudio.Cue cue = "setback".equals(event) || "combat_escape".equals(event)
                    ? VaeloriaAudio.Cue.DANGER
                    : "combat_victory".equals(event) || "reward".equals(event)
                    ? VaeloriaAudio.Cue.SUCCESS : VaeloriaAudio.Cue.ACTION;
            audio.cue(cue);
        }
    }

    @Override public void onBackPressed(){
        if("character".equals(screen)){
            if(state.characterCreated)show("hero");else finish();
            return;
        }
        super.onBackPressed();
    }

    private String itemDescriptor(VaeloriaDb.Item item) {
        if (item.catalogId != null) {ItemCatalogV092.ItemDef definition=ItemCatalogV092.byId(item.catalogId);if(definition!=null)return ItemCatalogV092.categoryLabel(definition.category)+(definition.stackable?" · kaupiamas":"")+(definition.consumable?" · naudojamas":"");}
        String query = (item.name + " " + item.slot + " " + item.type).toLowerCase(Locale.ROOT);
        if (query.contains("weapon") || query.contains("ašmen") || query.contains("kard")) return "Ginklas · artimas nuotolis";
        if (query.contains("chest") || query.contains("head") || query.contains("armor") || query.contains("mant")) return "Šarvai · apsauga";
        if (query.contains("utility") || query.contains("krep")) return "Naudingasis daiktas";
        if (query.contains("ring") || query.contains("žied") || query.contains("signet")) return "Relikvija · rezonansas";
        if (query.contains("rakt") || query.contains("key")) return "Siužeto raktas";
        if (query.contains("prizm")) return "Arkaninis fokusas";
        if (query.contains("astrolab") || query.contains("kompas")) return "Navigacija · rezonansas";
        if (query.contains("sėkl") || query.contains("seed")) return "Senovinis artefaktas";
        if (query.contains("credential") || query.contains("antspaud")) return "Politinis įgaliojimas";
        return "Artefaktas · rezonansas";
    }
}
