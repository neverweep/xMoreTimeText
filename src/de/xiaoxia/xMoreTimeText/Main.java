/**
 * Copyright (C) 2014 xiaoxia.de
 * 
 * @author xiaoxia.de
 * @date 2014
 * @license MIT
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 * 
 */

package de.xiaoxia.xmoretimetext;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookLoadPackage, IXposedHookInitPackageResources {

    private static final String PACKAGE_NAME = "com.android.systemui";
    private static CharSequence time = "";
    private static String[] preText;
    private static Calendar calendar;
    private static SimpleDateFormat sdf;
    private static DecimalFormat df = new DecimalFormat("00");
    private static SpannableString timeText;
    private static String originalText;
    private static Object vClock = null;
    private static String temp;
    private static Boolean isFormatOk;

    //ʹ��Xposed�ṩ��XSharedPreferences��������ȡandroid���õ�SharedPreferences����
    private final static XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName());
    //ɾ��ԭ�ַ�
    protected final static Boolean _filter = prefs.getBoolean("filter", false);
    //�Զ���
    protected final static Boolean _customize = prefs.getBoolean("customize", false);
    //�Ƿ�����չ״̬����ʾ
    protected final static Boolean _display = prefs.getBoolean("display", false);
    //�Զ���λ��
    protected final static Boolean _position = prefs.getString("position", "true").equals("true");
    //�Զ����С
    protected final static Float _size = Float.valueOf(prefs.getString("size", "1.0"));
    //�Ƿ���ʾ����
    protected final static Boolean _display_date = prefs.getBoolean("display_date", false);
    //�Զ�������λ��
    protected final static Boolean _position_date = prefs.getString("position_date", "true").equals("true");
    //�Զ������ڴ�С
    protected final static Boolean _priority_date = prefs.getString("priority_date", "true").equals("true");
    //�Զ������ڸ�ʽ
    protected final static String _format_date = prefs.getString("format_date", "").trim();
    //�Զ������ڴ�С
    protected final static Float _size_date = Float.valueOf(prefs.getString("size_date", "1.0"));
    //�Զ����С����չ״̬����
    protected final static Float _size_expended = Float.valueOf(prefs.getString("size_expended", "1.0"));
    //�ж�ʱ����Ƿ���õĲ�������
    private Boolean[] pValidaty = {
        false, false, false, false, false, false, false, false, false, false
    };
    //��ȡ�Զ�������
    protected final static String[] pTitle = {
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
    protected final static int[] pStart = {
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
    protected final static int[] pEnd = {
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

    //�˴�������Ϊ����systemui��ʼ����Դ�Ĺ����У���״̬��clock����һ��û�õ�vClock��Ȼ��Ϳ����ж��ǲ���Ϊ״̬����Clock�ˡ�
    public void handleInitPackageResources(final InitPackageResourcesParam resparam) {
        if (!resparam.packageName.equals(PACKAGE_NAME))
            return;

        resparam.res.hookLayout(PACKAGE_NAME, "layout", "super_status_bar", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam){
                TextView clock = (TextView) liparam.view.findViewById(liparam.res.getIdentifier("clock", "id", PACKAGE_NAME));
                if(clock != null)
                    XposedHelpers.setAdditionalInstanceField(clock, "vClock", true);
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    public void handleLoadPackage(final LoadPackageParam lpparam) {
        //�������systemui������
        if (!lpparam.packageName.equals(PACKAGE_NAME))
            return;

        //��ȡ��������
        String local = Locale.getDefault().getCountry();
        String lan = Locale.getDefault().getLanguage(); 
        //������������Ĭ������
        if (local.contains("TW") || local.contains("HK") || local.contains("MO")) {
            preText = new String[] {
                "�R��", "�糿", "����", "����", "����", "����", "����", "����"
            };
        } else if(lan.contains("zh")){
            preText = new String[] {
                "�賿", "�糿", "����", "����", "����", "����", "����", "����"
            };
        } else {
            preText = new String[] {
                "AM", "AM", "AM", "AM", "PM", "PM", "PM", "PM"
            };
        }

        //��������������ĳ��ʱ����Ƿ����
        for (int i = 0; i <= 9; i++) {
            pValidaty[i] = !"".equals(pEnd[i]) && !"".equals(pStart[i]) && pEnd[i] != -1 && pStart[i] != -1;
        }

        //��ʼ������ʵ��
        calendar = Calendar.getInstance();

        //�ж����ڸ�ʽ�ַ����Ƿ���Ч��������Щ����û��Ҫ����
        isFormatOk = !"".equals(_format_date) && _format_date != null;
        //��ʼ��SimpleDateFormat����
        if(isFormatOk)
            sdf = new SimpleDateFormat(_format_date);

        //����Clock���º�
        findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "getSmallTime", new XC_MethodHook(){
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                //��ȡvClock����
                vClock = XposedHelpers.getAdditionalInstanceField(param.thisObject, "vClock");

                //���vClock == null ˵����ʱ����չ״̬�������û������չ��ʾ�ı�Ҫ����ֱ�������������в���
                if(vClock != null || _display){
                    //��ȡʱ�ӵ�����
                    originalText = param.getResult().toString();

                    //����򿪹�������������ʽȥ��ԭʼʱ���е�����
                    if (_filter) {
                        originalText = originalText.replaceAll("([����]��)|([AP]\\.?M\\.?)", "");
                    }

                    calendar.setTimeInMillis(System.currentTimeMillis()); //�趨�����ؼ�Ϊ��ǰʱ��
                    int hm = Integer.parseInt(calendar.get(Calendar.HOUR_OF_DAY) + df.format(calendar.get(Calendar.MINUTE))); //��ȡʱ�䣬hhmm

                    //������Զ��壬��
                    if (_customize) {
                        //ʱ���ж�
                        for (int i = 0; i <= 9; i++) {
                            //XposedBridge.log(i + " -  EN:" + pValidaty[i] + " NOW:" + hm + " ED:" + pEnd[i] + " ST:" + pStart[i]);
                            if (pValidaty[i] && hm <= pEnd[i] && hm >= pStart[i]) {
                                time = pTitle[i];
                                break;
                            }
                        }
                    } else {
                        //ʱ���ж�
                        if (hm < 600 && hm >= 0) {
                            time = preText[0];
                        } else if (hm < 800 && hm >= 600) {
                            time = preText[1];
                        } else if (hm < 1100 && hm >= 800) {
                            time = preText[2];
                        } else if (hm < 1200 && hm >= 1100) {
                            time = preText[3];
                        } else if (hm < 1400 && hm >= 1200) {
                            time = preText[4];
                        } else if (hm < 1730 && hm >= 1400) {
                            time = preText[5];
                        } else if (hm < 1930 && hm >= 1730) {
                            time = preText[6];
                        } else {
                            time = preText[7];
                        }
                    }

                    //д��param
                    originalText = originalText.trim();
                    if(_position){
                        //�������־���
                        if(vClock != null){
                            //�����ʾ״̬��
                            if(_display_date && isFormatOk){
                                //�����ʾ����
                                temp = sdf.format(System.currentTimeMillis());
                                if(_position_date){
                                    //���ڷ�����
                                    if(_priority_date){
                                        //������������
                                        timeText = new SpannableString(time + " " + temp + " " + originalText);
                                        timeText.setSpan(new RelativeSizeSpan(_size), 0, time.length() + 1, 0);
                                        timeText.setSpan(new RelativeSizeSpan(_size_date), time.length() + 1, temp.length() + 1, 0);
                                    }else{
                                        //������������
                                        timeText = new SpannableString(temp + " " + time + " " + originalText);
                                        timeText.setSpan(new RelativeSizeSpan(_size_date), 0, temp.length() + 1, 0);
                                        timeText.setSpan(new RelativeSizeSpan(_size), temp.length() + 1, temp.length() + time.length() + 2, 0);
                                    }
                                }else{
                                    //���ڷ�����
                                    timeText = new SpannableString(time + " " + originalText + " " + temp);
                                    timeText.setSpan(new RelativeSizeSpan(_size), 0, temp.length() + 1, 0);
                                    timeText.setSpan(new RelativeSizeSpan(_size_date), time.length() + originalText.length() + 1, timeText.length(), 0);
                                }
                            }else{
                                timeText = new SpannableString(time + " " + originalText);
                                timeText.setSpan(new RelativeSizeSpan(_size), 0, time.length(), 0);
                            }
                        }else{
                            //�����չ״̬����ʾ
                            timeText = new SpannableString(time + " " + originalText);
                            timeText.setSpan(new RelativeSizeSpan(_size_expended), 0, time.length(), 0);
                        }
                    }else{
                        if(vClock != null){
                            //״̬����ʾ
                            if(_display_date && isFormatOk){
                                //��ʾ����
                                temp = sdf.format(System.currentTimeMillis());
                                if(_position_date){
                                    //���ڷ�����
                                    timeText = new SpannableString(temp + " " + originalText + " " + time);
                                    timeText.setSpan(new RelativeSizeSpan(_size_date), 0, temp.length() + 1, 0);
                                    timeText.setSpan(new RelativeSizeSpan(_size), temp.length() + originalText.length() + 1, timeText.length(), 0);
                                }else{
                                    //���ڷ�����
                                    if(_priority_date){
                                        //������������
                                        timeText = new SpannableString(originalText + " " + time + " " + temp);
                                        timeText.setSpan(new RelativeSizeSpan(_size), originalText.length(), (originalText + time).length() + 1, 0);
                                        timeText.setSpan(new RelativeSizeSpan(_size_date), originalText.length() + time.length() + 2, timeText.length(), 0);
                                    }else{
                                        //������������
                                        timeText = new SpannableString(originalText + " " + temp + " " + time);
                                        timeText.setSpan(new RelativeSizeSpan(_size_date), originalText.length(), originalText.length() + temp.length() + 1, 0);
                                        timeText.setSpan(new RelativeSizeSpan(_size), originalText.length() + temp.length() + 2, timeText.length(), 0);
                                    }
                                }
                            }else{
                                timeText = new SpannableString(originalText + " " + time);
                                timeText.setSpan(new RelativeSizeSpan(_size), originalText.length(), timeText.length(), 0);
                            }
                        }else{
                            //��չ״̬����ʾ
                            timeText = new SpannableString(originalText + " " + time);
                            timeText.setSpan(new RelativeSizeSpan(_size), originalText.length(), timeText.length(), 0);
                        }
                    }
                    param.setResult(timeText);
                }
            }
        });
    }
}