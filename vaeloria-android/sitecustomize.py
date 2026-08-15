from pathlib import Path

# Vaeloria presentation compatibility patch.
# Python imports sitecustomize automatically during the GitHub Actions build,
# so the polished v0.7 mobile UX can reuse the stronger illustrated v0.6 art.
p = Path(__file__).resolve().parent / "v070" / "PremiumViewsV070.java"
if p.exists():
    s = p.read_text(encoding="utf-8")

    # 1) WORLD MAP: restore illustrated v0.6 atlas and its correct interaction geometry.
    s = s.replace("R.drawable.world_map_v070", "R.drawable.world_map_v060")
    old_nodes = 'add("Luminara",500,330,3);add("Asterio Karūna",350,195,4);add("Stiklo Giria",735,155,5);add("Veyrhold",665,255,3);add("Aureliono Pakraštys",760,405,4);add("Žvaigždėkritos Skliautas",540,495,7);add("Tuščiavidurė Smailė",320,555,8);add("Pelenų Karūnos Citadelė",965,210,7);add("Kharad Vorn",1140,280,5);add("Drakono Pabudimo Viršūnės",1330,155,8);add("Safyro Platybės",1010,535,7);add("Amžinojo Šaltinio Slėnis",915,650,4);add("Žaliasis Labirintas",1135,735,8);add("Šventųjų Pelkynas",790,755,6);'
    new_nodes = 'add("Luminara",388,405,3);add("Asterio Karūna",270,183,4);add("Stiklo Giria",505,180,5);add("Veyrhold",620,326,3);add("Aureliono Pakraštys",705,513,4);add("Žvaigždėkritos Skliautas",525,512,7);add("Tuščiavidurė Smailė",110,613,8);add("Pelenų Karūnos Citadelė",925,275,7);add("Kharad Vorn",1180,365,5);add("Drakono Pabudimo Viršūnės",1175,90,8);add("Safyro Platybės",967,600,7);add("Amžinojo Šaltinio Slėnis",770,741,4);add("Šventųjų Pelkynas",617,920,6);add("Žaliasis Labirintas",1190,790,8);'
    s = s.replace(old_nodes, new_nodes)
    s = s.replace(
        "base=Math.min(getWidth()/(float)map.getWidth(),getHeight()/(float)map.getHeight());",
        "base=Math.max(getWidth()/(float)map.getWidth(),getHeight()/(float)map.getHeight());",
    )
    s = s.replace("Math.min(4.7f,zoom*d.getScaleFactor())", "Math.min(4.5f,zoom*d.getScaleFactor())")
    s = s.replace("bd<76*76", "bd<70*70")
    s = s.replace("m=dp(60)", "m=dp(55)")

    # 2) PLAY / SCENE: remove the abstract v0.7 vector-looking art regression.
    # Keep the v0.7 scene renderer/UI, but feed it the stronger illustrated v0.6 assets.
    s = s.replace(
        "luminara=BitmapFactory.decodeResource(getResources(),R.drawable.scene_luminara_v070);wild=BitmapFactory.decodeResource(getResources(),R.drawable.scene_wild_v070);map=BitmapFactory.decodeResource(getResources(),R.drawable.world_map_v060);",
        "luminara=BitmapFactory.decodeResource(getResources(),R.drawable.scene_luminara);wild=BitmapFactory.decodeResource(getResources(),R.drawable.combat_forest);map=BitmapFactory.decodeResource(getResources(),R.drawable.world_map_v060);",
    )

    p.write_text(s, encoding="utf-8")

# Make the gameplay scene more cinematic on mobile without changing interaction flow.
a = Path(__file__).resolve().parent / "v070" / "PolishedActivity.java"
if a.exists():
    s = a.read_text(encoding="utf-8")
    s = s.replace("c.addView(art,new LinearLayout.LayoutParams(-1,dp(252)));", "c.addView(art,new LinearLayout.LayoutParams(-1,dp(292)));")
    a.write_text(s, encoding="utf-8")
