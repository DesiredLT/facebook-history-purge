package lt.vaeloria.ooc;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class SafeGameActivity extends VaeloriaActivity {
    @Override public void onCreate(Bundle b) {
        try {
            super.onCreate(b);
        } catch (Throwable t) {
            showCrash(t);
        }
    }

    private void showCrash(Throwable t) {
        try {
            ScrollView sv = new ScrollView(this);
            LinearLayout root = new LinearLayout(this);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(dp2(22),dp2(30),dp2(22),dp2(30));
            root.setBackgroundColor(Color.rgb(7,15,22));
            sv.addView(root);

            TextView title = txt("VAELORIA · STARTO KLAIDA",22,Color.rgb(255,145,130));
            title.setGravity(Gravity.CENTER);
            root.addView(title);

            TextView body = txt(t.getClass().getName()+"\n\n"+(t.getMessage()==null?"be klaidos žinutės":t.getMessage()),14,Color.rgb(238,239,232));
            body.setPadding(0,dp2(18),0,dp2(18));
            body.setTextIsSelectable(true);
            root.addView(body);

            TextView hint = txt("Šis tekstas yra tikslus Android runtime gedimas. Programėlė nebeužsidaro tyliai.",12,Color.rgb(166,183,188));
            root.addView(hint);

            Button close = new Button(this);
            close.setText("GRĮŽTI Į SAFE START");
            close.setOnClickListener(v->finish());
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,dp2(52));
            p.setMargins(0,dp2(18),0,0);
            root.addView(close,p);
            setContentView(sv);
        } catch (Throwable ignored) {
            finish();
        }
    }

    private TextView txt(String s,int z,int c){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);return t;}
    private int dp2(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
}
