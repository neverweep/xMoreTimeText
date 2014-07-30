package de.xiaoxia.xmoretimetext;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import java.util.Date;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookLoadPackage{
    private static TextView textview;
    private String time;
    public void handleLoadPackage(final LoadPackageParam lpparam){
        if (!lpparam.packageName.equals("com.android.systemui"))
            return;
        findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param){
                textview = (TextView) param.thisObject;
                Date d = new Date();
                @SuppressWarnings("deprecation")
                int hour = (int) d.getHours();
                if(hour <= 5 && hour >= 0){
                    time = "Áè³¿";
                }else if(hour < 8 && hour >= 6){
                    time = "Ôç³¿";
                }else if(hour < 11 && hour >= 8){
                    time = "ÉÏÎç";
                }else if(hour < 14 && hour >= 11){
                    time = "ÖÐÎç";
                }else if(hour < 17 && hour >= 14){
                    time = "ÏÂÎç";
                }else if(hour < 20 && hour >= 17){
                    time = "°øÍí";
                }else{
                    time = "ÍíÉÏ";
                }
                textview.setText(time + textview.getText().toString());
            }
        });
    }
}