package lt.vaeloria.ooc;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;
import java.util.Map;

public class StatsActivity extends Activity {
    private static final int BG=Color.rgb(7,15,22),SUR=Color.rgb(16,28,37),SUR2=Color.rgb(23,38,49),TEXT=Color.rgb(238,239,232),MUT=Color.rgb(166,183,188),GOLD=Color.rgb(214,182,107),TEAL=Color.rgb(82,177,167),RED=Color.rgb(213,91,82);
    private LinearLayout list;
    private VaeloriaDb db;
    private Map<String,Integer> values;
    private Map<String,VaeloriaDb.Mastery> masteries;

    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);db=new VaeloriaDb(this);values=db.getStatValues();masteries=db.getMasteries();build();}
    @Override public void onDestroy(){if(db!=null)db.close();super.onDestroy();}

    private void build(){
        LinearLayout root=col();root.setBackgroundColor(BG);root.setOnApplyWindowInsetsListener((v,insets)->{int top,bottom;if(Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars());top=bars.top;bottom=bars.bottom;}else{top=insets.getSystemWindowInsetTop();bottom=insets.getSystemWindowInsetBottom();}v.setPadding(0,top,0,bottom);return insets;});
        LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(12),dp(7),dp(14),dp(7));Button back=new Button(this);back.setText("‹ VEIKĖJAS");back.setTextSize(10);back.setTextColor(TEXT);back.setAllCaps(false);back.setBackground(round(SUR2,13,Color.rgb(49,69,80)));back.setOnClickListener(v->finish());top.addView(back,new LinearLayout.LayoutParams(dp(108),dp(46)));TextView title=t("92 BAZINĖS SAVYBĖS",18,GOLD,true);title.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);top.addView(title,new LinearLayout.LayoutParams(0,dp(46),1));root.addView(top,new LinearLayout.LayoutParams(-1,dp(62)));

        int perfect=0,totalMastery=0;for(String stat:values.keySet()){if(values.getOrDefault(stat,100)>=100)perfect++;VaeloriaDb.Mastery m=masteries.get(stat);totalMastery+=m==null?70:m.level;}int avg=Math.round(totalMastery/92f);
        LinearLayout head=card();head.addView(t("EINORAS · "+perfect+" / 92 ties bazine riba",17,TEXT,true));head.addView(t("Vidutinis post-cap meistriškumas: "+avg+" / 100",13,GOLD,true));ProgressBar total=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);total.setMax(100);total.setProgress(avg);total.setProgressTintList(android.content.res.ColorStateList.valueOf(GOLD));total.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(34,49,57)));head.addView(total,new LinearLayout.LayoutParams(-1,dp(8)));TextView note=t("Bazė 100/100 nusako įgimtą ir ištreniruotą pajėgumą. Meistriškumas yra atskira post-cap specializacija: naudojama savybė gauna patirties, kyla jos meistriškumo lygis ir didėja situacinis patikros bonusas.",11,MUT,false);note.setPadding(0,dp(8),0,0);head.addView(note);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.setMargins(dp(14),0,dp(14),dp(10));root.addView(head,hp);

        EditText search=new EditText(this);search.setHint("Ieškoti savybės…");search.setSingleLine(true);search.setTextColor(TEXT);search.setHintTextColor(Color.rgb(112,136,145));search.setTextSize(13);search.setPadding(dp(13),0,dp(13),0);search.setBackground(round(Color.rgb(10,21,29),13,Color.rgb(43,65,76)));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(50));sp.setMargins(dp(14),0,dp(14),dp(10));root.addView(search,sp);
        ScrollView sv=new ScrollView(this);list=col();list.setPadding(dp(14),0,dp(14),dp(20));sv.addView(list);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){render(s.toString());}public void afterTextChanged(Editable e){}});render("");setContentView(root);root.requestApplyInsets();
    }

    private void render(String query){list.removeAllViews();String q=query==null?"":query.trim().toLowerCase(Locale.forLanguageTag("lt-LT"));int shown=0;for(BaseStatCatalog.Group g:BaseStatCatalog.GROUPS){LinearLayout box=card();TextView h=t(g.name+" · "+g.stats.length,10,GOLD,true);h.setPadding(0,0,0,dp(6));box.addView(h);int groupShown=0;for(String stat:g.stats){if(!q.isEmpty()&&!stat.toLowerCase(Locale.forLanguageTag("lt-LT")).contains(q))continue;box.addView(statRow(stat,values.getOrDefault(stat,100),masteries.get(stat)));groupShown++;shown++;}if(groupShown>0){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(9));list.addView(box,p);}}if(shown==0){LinearLayout empty=card();empty.addView(t("Nerasta savybių pagal „"+query+"“.",12,MUT,false));list.addView(empty);}}

    private LinearLayout statRow(String name,int value,VaeloriaDb.Mastery mastery){int v=Math.max(0,Math.min(100,value));VaeloriaDb.Mastery m=mastery==null?new VaeloriaDb.Mastery(70,0,1000):mastery;LinearLayout wrap=col();wrap.setPadding(0,dp(8),0,dp(8));LinearLayout r=row();r.setGravity(Gravity.CENTER_VERTICAL);TextView n=t(name,12,TEXT,true);r.addView(n,new LinearLayout.LayoutParams(0,-2,1));TextView val=t(v+" / 100",10,v==100?TEAL:(v>=75?GOLD:RED),true);val.setGravity(Gravity.END);r.addView(val,new LinearLayout.LayoutParams(dp(72),-2));wrap.addView(r);
        LinearLayout mr=row();mr.setGravity(Gravity.CENTER_VERTICAL);TextView ml=t("MEISTRIŠKUMAS "+m.level+"/100",9,GOLD,true);mr.addView(ml,new LinearLayout.LayoutParams(0,-2,1));TextView xp=t(m.xp+" / "+m.nextXp+" XP",9,MUT,false);xp.setGravity(Gravity.END);mr.addView(xp,new LinearLayout.LayoutParams(dp(105),-2));wrap.addView(mr);
        ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(Math.max(1,m.nextXp));p.setProgress(Math.min(m.xp,m.nextXp));p.setProgressTintList(android.content.res.ColorStateList.valueOf(GOLD));p.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(33,48,57)));LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(4));pp.setMargins(0,dp(4),0,0);wrap.addView(p,pp);return wrap;}

    private LinearLayout card(){LinearLayout x=col();x.setPadding(dp(13),dp(11),dp(13),dp(11));x.setBackground(round(SUR,17,Color.rgb(36,57,68)));return x;}private LinearLayout col(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);return x;}private LinearLayout row(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.HORIZONTAL);return x;}private TextView t(String s,int z,int color,boolean bold){TextView x=new TextView(this);x.setText(s);x.setTextSize(z);x.setTextColor(color);x.setTypeface(Typeface.create(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL));x.setLineSpacing(0,1.08f);return x;}private GradientDrawable round(int fill,int r,int stroke){GradientDrawable g=new GradientDrawable();g.setColor(fill);g.setCornerRadius(dp(r));if(Color.alpha(stroke)>0)g.setStroke(dp(1),stroke);return g;}private int dp(int v){return(int)(v*getResources().getDisplayMetrics().density+.5f);}
}
