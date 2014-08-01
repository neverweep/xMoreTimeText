package de.xiaoxia.xmoretimetext;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookLoadPackage {

    private static TextView textview;
    private Boolean[] en = {
        false, false, false, false, false, false, false, false, false, false
    };
    private static String time;
    private static String[] timeText;
    private static Calendar calendar;
    private static DecimalFormat df = new DecimalFormat("00");
    private static SpannableString ss;
    private static String str;

    //ʹ��Xposed�ṩ��XSharedPreferences��������ȡandroid���õ�SharedPreferences����
    private final static XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName());
    //ɾ��ԭ�ַ�
    protected final static Boolean _filter = prefs.getBoolean("filter_switch", false);
    //�Զ���
    protected final static Boolean _customize = prefs.getBoolean("customize_switch", false);
    //�Զ���λ��
    protected final static Boolean _position = prefs.getString("position", "true").equals("true");
    //�Զ����С
    protected final static Float _size = Float.valueOf(prefs.getString("size", "1.0"));
    //���û���Զ����С������������spannable����
    private final static Boolean _size_parse = !prefs.getString("size", "1.0").equals("1.0");
    //��ȡ�Զ�������
    protected final static String[] pt = {
        prefs.getString("pt0", "").trim(),
        prefs.getString("pt1", "").trim(),
        prefs.getString("pt2", "").trim(),
        prefs.getString("pt3", "").trim(),
        prefs.getString("pt4", "").trim(),
        prefs.getString("pt5", "").trim(),
        prefs.getString("pt6", "").trim(),
        prefs.getString("pt7", "").trim(),
        prefs.getString("pt8", "").trim(),
        prefs.getString("pt9", "").trim(),
    };
    //��ȡ�Զ�����ʼʱ��
    protected final static int[] ps = {
        Integer.parseInt(prefs.getString("ps0", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("ps1", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("ps2", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("ps3", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("ps4", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("ps5", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("ps6", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("ps7", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("ps8", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("ps9", "-1").replace(":", "")),
    };
    //��ȡ�Զ������ʱ��
    protected final static int[] pe = {
        Integer.parseInt(prefs.getString("pe0", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("pe1", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("pe2", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("pe3", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("pe4", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("pe5", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("pe6", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("pe7", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("pe8", "-1").replace(":", "")),
        Integer.parseInt(prefs.getString("pe9", "-1").replace(":", "")),
    };

    public void handleLoadPackage(final LoadPackageParam lpparam) {

        //�������systemui������
        if (!lpparam.packageName.equals("com.android.systemui")) return;

        //��ȡ��������
        String local = Locale.getDefault().getCountry();
        //������������Ĭ������
        if (local.contains("TW") || local.contains("HK") || local.contains("MO")) {
            timeText = new String[] {
                "�R��", "�糿", "����", "����", "����", "����", "����"
            };
        } else {
            timeText = new String[] {
                "�賿", "�糿", "����", "����", "����", "����", "����"
            };
        }

        //��������������ĳ��ʱ����Ƿ����
        for (int i = 0; i <= 9; i++) {
            en[i] = !"".equals(pe[i]) && !"".equals(ps[i]) && pe[i] != -1 && ps[i] != -1;
        }

        //��ʼ������ʵ��
        calendar = Calendar.getInstance();

        //����Clock���º�
        findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock", new XC_MethodHook(){
            @Override
            protected void afterHookedMethod(MethodHookParam param) {

                textview = (TextView) param.thisObject; //��ȡTextView�ؼ�

                str = textview.getText().toString();
                //����򿪹�������������ʽȥ��ԭʼʱ���е�����
                if (_filter) {
                    str = str.replaceAll("([����]��)|([AP]\\.?M\\.?)", "");
                }

                calendar.setTimeInMillis(System.currentTimeMillis()); //�趨�����ؼ�Ϊ��ǰʱ��
                int hm = Integer.parseInt(calendar.get(Calendar.HOUR_OF_DAY) + df.format(calendar.get(Calendar.MINUTE))); //��ȡʱ�䣬hhmm

                //������Զ��壬��
                if (_customize) {
                    //ʱ���ж�
                    for (int i = 0; i <= 9; i++) {
                        //XposedBridge.log(i + " -  EN:" + en[i] + " NOW:" + hm + " ED:" + pe[i] + " ST:" + ps[i]);
                        if (en[i] && hm <= pe[i] && hm >= ps[i]) {
                            time = pt[i];
                            break;
                        }
                    }
                } else {
                    //ʱ���ж�
                    if (hm <= 500 && hm >= 0) {
                        time = timeText[0];
                    } else if (hm < 800 && hm >= 600) {
                        time = timeText[1];
                    } else if (hm < 1100 && hm >= 800) {
                        time = timeText[2];
                    } else if (hm < 1400 && hm >= 1100) {
                        time = timeText[3];
                    } else if (hm < 1700 && hm >= 1400) {
                        time = timeText[4];
                    } else if (hm < 1930 && hm >= 1730) {
                        time = timeText[5];
                    } else {
                        time = timeText[6];
                    }
                }
                //д��TextView
                if(_position){
                    str = str.replaceAll("^ +", "");
                    if(_size_parse){
                        ss =  new SpannableString(time + " " + str);
                        ss.setSpan(new RelativeSizeSpan(_size), 0, time.length(), 0);
                        textview.setText(ss);
                    }else{
                        str = time + " " + str;
                        textview.setText(str);
                    }
                }else{
                    str = str.replaceAll(" +$", "");
                    if(_size_parse){
                        ss =  new SpannableString(str + " " + time);
                        ss.setSpan(new RelativeSizeSpan(_size), str.length(), ss.length(), 0);
                        textview.setText(ss);
                    }else{
                        str = str + " " + time;
                        textview.setText(str);
                    }
                }
            }
        });
    }
}