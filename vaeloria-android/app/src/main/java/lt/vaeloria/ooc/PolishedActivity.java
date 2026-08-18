package lt.vaeloria.ooc;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Vaeloria v0.9.1 premium mobile presentation over the existing game and save systems. */
public class PolishedActivity extends PremiumActivity {
    private static final int LINE = Color.rgb(43, 59, 68);
    private static final int PANEL = Color.rgb(9, 19, 27);
    private static final int PANEL_2 = Color.rgb(13, 25, 34);
    private static final int BLUE = Color.rgb(91, 164, 207);
    private static final int GREEN = Color.rgb(95, 186, 139);
    private static final int PURPLE = Color.rgb(158, 117, 214);

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
        screen = id;
        nav();
        content.removeAllViews();
        View view;
        try {
            if ("game".equals(id)) view = game();
            else if ("hero".equals(id)) view = hero();
            else if ("items".equals(id)) view = items();
            else if ("map".equals(id)) view = map();
            else if ("journal".equals(id)) view = journal();
            else view = settings();
        } catch (Throwable error) {
            view = errorView(error);
        }
        content.addView(view);
        if (pref("animations", true)) {
            view.setAlpha(0);
            view.setTranslationY(dp(7));
            view.setScaleX(.99f);
            view.setScaleY(.99f);
            view.animate().alpha(1).translationY(0).scaleX(1f).scaleY(1f).setDuration(190).start();
        }
    }

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
        boolean ai = !SecureKeyStore.load(this).isEmpty();
        freeHeader.addView(chip(ai ? "GYVAS PASAULIS" : "VIETINIS", ai ? GREEN : BLUE));
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
        ImageView artwork = image(R.drawable.hero_einoras_v090);
        artwork.setScaleType(ImageView.ScaleType.FIT_CENTER);
        artwork.setContentDescription("Einoras su Asteriono Ašmenimis");
        hero.addView(artwork, new FrameLayout.LayoutParams(-1, dp(510)));
        View shade = new View(this);
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.argb(0, 0, 0, 0), Color.argb(8, 0, 0, 0), Color.argb(244, 2, 8, 13)}));
        hero.addView(shade, new FrameLayout.LayoutParams(-1, dp(510)));
        LinearLayout overlay = col();
        overlay.setPadding(dp(16), 0, dp(16), dp(16));
        overlay.addView(section("VEIKĖJAS"));
        overlay.addView(serif("EINORAS", 30, PARCH, true));
        overlay.addView(txt("201 m. chronologinis · 20 m. biologinis · amžius nekinta", 9, SUB, false));
        LinearLayout tags = row();
        tags.setPadding(0, dp(7), 0, 0);
        tags.addView(chip("92/92 · 100/100", GOLD2));
        tags.addView(chip("POST-CAP", PURPLE));
        overlay.addView(tags);
        hero.addView(overlay, new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM));
        column.addView(hero, new LinearLayout.LayoutParams(-1, dp(510)));
        column.addView(resources(), sp(dp(8)));

        LinearLayout mastery = panel(false);
        mastery.addView(section("MEISTRIŠKUMAS"));
        mastery.addView(serif("Legendary bazė · kokybinis progresas", 17, PARCH, true));
        mastery.addView(txt("Skaitinis cap pasiektas. Toliau augama per principus, technikas, nežinomų taisyklių kalibraciją ir lygiaverčių meistrų praktiką.", 10, SUB, false));
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

        LinearLayout abilities = panel(false);
        LinearLayout abilityHeader = row();
        abilityHeader.setGravity(Gravity.CENTER_VERTICAL);
        abilityHeader.addView(section("POST-CAP GEBĖJIMAI"), new LinearLayout.LayoutParams(0, -2, 1));
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

    View items() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = col();
        column.setPadding(dp(10), dp(7), dp(10), dp(20));
        scroll.addView(column);
        LinearLayout header = panel(true);
        header.addView(section("INVENTORIUS"));
        header.addView(serif("Relikvijos ir įranga", 23, PARCH, true));
        List<VaeloriaDb.Item> inventory = db.getItems();
        int equipped = 0;
        int synced = 0;
        for (VaeloriaDb.Item item : inventory) {
            if (item.equipped) equipped++;
            if (item.synced) synced++;
        }
        header.addView(txt(inventory.size() + " objektų · " + equipped + " įrengti · " + synced + " rezonuoja", 10, SUB, false));
        column.addView(header, sp(dp(9)));

        for (int index = 0; index < inventory.size(); index += 2) {
            LinearLayout row = row();
            row.addView(itemCard(inventory.get(index)), new LinearLayout.LayoutParams(0, dp(198), 1));
            Space gap = new Space(this);
            row.addView(gap, new LinearLayout.LayoutParams(dp(7), 1));
            if (index + 1 < inventory.size()) {
                row.addView(itemCard(inventory.get(index + 1)), new LinearLayout.LayoutParams(0, dp(198), 1));
            } else {
                row.addView(new View(this), new LinearLayout.LayoutParams(0, dp(198), 1));
            }
            column.addView(row, sp(dp(7)));
        }
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
        TextView name = serif(item.name, 12, PARCH, true);
        name.setMaxLines(2);
        card.addView(name, sp(dp(3)));
        card.addView(txt(rarityLabel(item.rarity).toUpperCase(Locale.ROOT), 8, color, true));
        card.addView(txt(itemDescriptor(item), 7, SUB, false));
        String status = (item.equipped ? "● ĮRENGTA" : "○ INVENTORIUJE") + (item.synced ? " · SYNC" : "");
        card.addView(txt(status, 7, item.synced ? GREEN : SUB, true));
        card.setOnClickListener(view -> {
            haptic();
            itemDialog(item);
        });
        return card;
    }

    @Override View map() {
        FrameLayout frame = new FrameLayout(this);
        WorldMapV090View world = new WorldMapV090View(this, state);
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
        topRow.addView(chip("◆ EINORAS", GOLD2));
        top.addView(topRow);
        top.addView(txt("Žnybk · tempk · dukart bakstelėk · paliesk lokaciją", 8, SUB, false));
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
        Button factions = small("FRAKCIJOS");
        factions.setMinHeight(dp(44));
        factions.setOnClickListener(view -> {
            world.toggleFactions();
            haptic();
        });
        bottom.addView(factions, new LinearLayout.LayoutParams(dp(82), dp(44)));
        Button center = small("CENTRUOTI");
        center.setMinHeight(dp(44));
        center.setOnClickListener(view -> {
            world.resetView();
            haptic();
        });
        bottom.addView(center, new LinearLayout.LayoutParams(dp(82), dp(44)));
        FrameLayout.LayoutParams bottomParams = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM);
        bottomParams.setMargins(dp(10), 0, dp(10), dp(10));
        frame.addView(bottom, bottomParams);
        return frame;
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
        progress.addView(step(true, false, "Manifestas pasiekė Luminara prieš karavaną"));
        progress.addView(step(false, true, state.objective));
        progress.addView(step(false, false, "Užsitikrinti tris nepriklausomai prižiūrimus kelio atramos taškus"));
        progress.addView(step(false, false, "Pereiti Meridianą ir grįžti su patikrinamais Orisono kontakto duomenimis"));
        progress.addView(step(false, false, "Derėtis dėl Pirmosios Meridiano chartijos arba ją atmesti"));
        column.addView(progress, sp(dp(9)));

        LinearLayout threads = panel(false);
        threads.addView(section("AKTYVIOS PASAULIO GIJOS"));
        threads.addView(threadCard("Kelionės vartų poslinkis", "KRITINĖ", "Keičia maršrutų laiką ir įrodymų patikimumą.", Color.rgb(218, 120, 77)));
        threads.addView(threadCard("Orisono ryšio protokolas", "TYRIMAS", "Reikia nepriklausomai patvirtinti signalo kilmę.", BLUE));
        threads.addView(threadCard("Santarvės priežiūros valdymas", "POLITINĖ", "Frakcijos varžosi dėl vartų kontrolės.", PURPLE));
        threads.addView(threadCard("Drakoniškoji įpėdinystė", "STEBIMA", "Kol kas netiesiogiai susieta su anomalija.", GREEN));
        column.addView(threads, sp(dp(9)));

        column.addView(bestiaryPanel(), sp(dp(9)));

        FactionStatusV090View factions = new FactionStatusV090View(this, state);
        column.addView(factions, new LinearLayout.LayoutParams(-1, dp(178)));

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
        String actionLabel = npc.length > 3 ? npc[3] : "KLAUSTI APIE MERIDIANĄ";
        String action = npc.length > 4 ? npc[4]
                : "Pasikalbėti su " + npc[0] + " apie Lūžusį Meridianą ir išgirsti tik tai, ką šis žmogus realiai žino.";
        new AlertDialog.Builder(this)
                .setView(content)
                .setNegativeButton("UŽDARYTI", null)
                .setPositiveButton(actionLabel, (dialog, which) -> act(action))
                .show();
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

    private View bestiaryPanel() {
        LinearLayout panel = panel(false);
        panel.addView(section("BESTIARIUMAS · 218 ILIUSTRUOTŲ GRĖSMIŲ"));
        panel.addView(serif("Pažintos Vaelorios būtybės", 17, PARCH, true));
        panel.addView(txt("Pasirink grėsmių grupę ir pasiruošk pagal jos pavojų, aplinką bei elgseną.", 9, SUB, false), sp(dp(7)));
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
        new AlertDialog.Builder(this)
                .setView(scroll)
                .setNegativeButton("UŽDARYTI", null)
                .setPositiveButton("IEŠKOTI PĖDSAKŲ", (dialog, which) ->
                        act("Ieškoti " + enemy.name + " pėdsakų saugiai, neinicijuojant kovos be aiškaus mano sprendimo."))
                .show();
    }

    @Override void itemDialog(VaeloriaDb.Item item) {
        LinearLayout content = col();
        content.setPadding(dp(18), dp(14), dp(18), dp(6));
        ItemArtView artwork = new ItemArtView(this);
        artwork.setItem(item.name, item.slot == null ? item.type : item.slot, item.rarity);
        content.addView(artwork, new LinearLayout.LayoutParams(-1, dp(230)));
        content.addView(serif(item.name, 21, PARCH, true), sp(dp(4)));
        content.addView(txt(rarityLabel(item.rarity).toUpperCase(Locale.ROOT), 9, rarity(item.rarity), true), sp(dp(7)));
        content.addView(txt(itemDescriptor(item), 9, GOLD2, true), sp(dp(5)));
        content.addView(txt(item.description, 11, Color.rgb(218, 223, 218), false));
        new AlertDialog.Builder(this).setView(content).setPositiveButton("UŽDARYTI", null).show();
    }

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

    private String itemDescriptor(VaeloriaDb.Item item) {
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
