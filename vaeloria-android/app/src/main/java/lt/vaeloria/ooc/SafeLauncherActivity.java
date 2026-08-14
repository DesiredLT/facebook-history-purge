package lt.vaeloria.ooc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class SafeLauncherActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(7,15,22));
        getWindow().setNavigationBarColor(Color.rgb(7,15,22));
        render();
    }

    private void render() {
        ScrollView sv = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22),dp(30),dp(22),dp(30));
        root.setBackgroundColor(Color.rgb(7,15,22));
        sv.addView(root);

        TextView title = text("VAELORIA OOC", 28, Color.rgb(214,182,107));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView status = text("SAFE START · v0.2.3", 12, Color.rgb(82,177,167));
        status.setGravity(Gravity.CENTER);
        status.setPadding(0,dp(8),0,dp(24));
        root.addView(status);

        TextView info = text("Programėlė paleista. Šis ekranas izoliuotas nuo žaidimo UI, todėl jei kuri nors RPG dalis nulūžtų, Vaeloria nebeturėtų tiesiog užsidaryti.", 15, Color.rgb(238,239,232));
        info.setLineSpacing(0,1.2f);
        root.addView(info);

        TextView checks = text(runChecks(), 13, Color.rgb(166,183,188));
        checks.setPadding(0,dp(18),0,dp(18));
        root.addView(checks);

        Button play = new Button(this);
        play.setText("ATIDARYTI ŽAIDIMĄ");
        play.setAllCaps(false);
        play.setTextSize(15);
        play.setTextColor(Color.rgb(7,15,22));
        play.setBackgroundColor(Color.rgb(214,182,107));
        play.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, VaeloriaActivity.class));
            } catch (Throwable t) {
                showInlineError(t);
            }
        });
        root.addView(play, new LinearLayout.LayoutParams(-1, dp(56)));

        Button retry = new Button(this);
        retry.setText("PAKARTOTI DIAGNOSTIKĄ");
        retry.setAllCaps(false);
        retry.setOnClickListener(v -> render());
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, dp(50));
        rp.setMargins(0,dp(12),0,0);
        root.addView(retry, rp);

        setContentView(sv);
    }

    private String runChecks() {
        StringBuilder s = new StringBuilder("STARTO DIAGNOSTIKA\n");
        try {
            VaeloriaDb db = new VaeloriaDb(this);
            GameState gs = db.loadState();
            s.append("✓ SQLite: OK\n");
            s.append("✓ State: ").append(gs.location).append(" · HP ").append(gs.hp).append("/").append(gs.hpMax).append("\n");
            s.append("✓ Items: ").append(db.getItems().size()).append("\n");
            s.append("✓ Abilities: ").append(db.getAbilities().size()).append("\n");
            db.close();
        } catch (Throwable t) {
            s.append("✕ SQLite/state: ").append(t.getClass().getSimpleName()).append(": ").append(safe(t.getMessage())).append("\n");
        }
        try {
            String key = SecureKeyStore.load(this);
            s.append("✓ Keystore: OK").append(key.isEmpty()?" (Groq raktas nenustatytas)":" (Groq raktas yra)").append("\n");
        } catch (Throwable t) {
            s.append("✕ Keystore: ").append(t.getClass().getSimpleName()).append(": ").append(safe(t.getMessage())).append("\n");
        }
        try {
            new SceneBannerView(this);
            new CombatBoardView(this);
            new WorldMapView(this);
            s.append("✓ Custom views: OK\n");
        } catch (Throwable t) {
            s.append("✕ Custom views: ").append(t.getClass().getSimpleName()).append(": ").append(safe(t.getMessage())).append("\n");
        }
        return s.toString();
    }

    private void showInlineError(Throwable t) {
        TextView e = text("Žaidimo paleidimo klaida:\n" + t.getClass().getName() + "\n" + safe(t.getMessage()), 14, Color.rgb(255,145,130));
        e.setPadding(dp(12),dp(16),dp(12),0);
        ((LinearLayout)((ScrollView)findViewById(android.R.id.content).getChildAt(0)).getChildAt(0)).addView(e);
    }

    private TextView text(String v,int size,int color){TextView t=new TextView(this);t.setText(v);t.setTextSize(size);t.setTextColor(color);return t;}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private String safe(String s){return s==null?"be žinutės":s;}
}
