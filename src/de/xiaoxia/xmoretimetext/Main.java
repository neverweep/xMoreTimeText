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
import android.os.Build;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
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
    private static CharSequence timeText;
    private static SpannableString timeSpan;
    private static SpannableString dateSpan;
    private static SpannableString timeExpendedSpan;
    private static SpannableString originalTextSpan;
    private static String[] preText;
    private static Calendar calendar;
    private static SimpleDateFormat sdf;
    private static DecimalFormat df = new DecimalFormat("00");
    private static String originalText;
    private static String date;
    private static Object vClock = null;
    private static Boolean isFormatOk;
    private static TextView textView;

    //ʹ��Xposed�ṩ��XSharedPreferences��������ȡandroid���õ�SharedPreferences����
    private final static XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName());
    //�Ƿ���ʾ��Ϣ
    protected final static Boolean _center = prefs.getBoolean("center", false);
    //�Ƿ���ʾ��Ϣ
    protected final static Boolean _info = prefs.getBoolean("basic_info", true);
    //��ʾʱ��
    protected final static Boolean _clock = prefs.getBoolean("clock", false);
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
    //�Զ���ԭ��ʱ����ɫ
    protected final static int _color_clock = prefs.getInt("color_clock", -16777216);
    //�Զ���ԭ��ʱ����ɫ����
    protected final static Boolean _color_clock_s = prefs.getBoolean("color_clock_s", false);
    //�Զ�����Ϣ��ɫ
    protected final static int _color_info = prefs.getInt("color_info", -16777216);
    //�Զ�����Ϣ��ɫ����
    protected final static Boolean _color_info_s =  prefs.getBoolean("color_info_s", false);
    //�Զ���������ɫ
    protected final static int _color_date = prefs.getInt("color_date", -16777216);
    //�Զ���������ɫ����
    protected final static Boolean _color_date_s =  prefs.getBoolean("color_date_s", false);
    //ǿ�Ƹ���
    protected final static Boolean _force =  prefs.getBoolean("force", false);
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
                /*
                /  Center clock view and know if is expended status bar. 
                /  Thanks for the work of GravityBox by C3C0@XDA
                /  https://github.com/GravityBox/GravityBox/blob/jellybean/src/com/ceco/gm2/gravitybox/ModStatusBar.java
                */
                Boolean mClockInSbContents = false;

                String iconAreaId = Build.VERSION.SDK_INT > 16 ? "system_icon_area" : "icons";
                ViewGroup mIconArea = (ViewGroup) liparam.view.findViewById(liparam.res.getIdentifier(iconAreaId, "id", PACKAGE_NAME));
                if (mIconArea == null)
                    return;

                ViewGroup mRootView = (ViewGroup) liparam.view.findViewById(liparam.res.getIdentifier("status_bar", "id", PACKAGE_NAME));
                if (mRootView == null)
                    return;

                LinearLayout mLayoutClock = new LinearLayout(liparam.view.getContext());
                mLayoutClock.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                mLayoutClock.setGravity(Gravity.CENTER);
                mRootView.addView(mLayoutClock);
                
                ViewGroup mSbContents = Build.VERSION.SDK_INT > 16 ? (ViewGroup) liparam.view.findViewById(liparam.res.getIdentifier("status_bar_contents", "id", PACKAGE_NAME)) : mIconArea;
                TextView clock = (TextView) mIconArea.findViewById(liparam.res.getIdentifier("clock", "id", PACKAGE_NAME));
                if (clock == null && mSbContents != null) {
                    clock = (TextView) mSbContents.findViewById(liparam.res.getIdentifier("clock", "id", PACKAGE_NAME));
                    mClockInSbContents = clock != null;
                }
                if(_center){
                    if (mClockInSbContents) {
                        mSbContents.removeView(clock);
                    } else {
                        mIconArea.removeView(clock);
                    }
                    clock.setGravity(Gravity.CENTER);
                    clock.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                    clock.setPadding(0, 0, 0, 0);
                    mSbContents.removeView(clock);
                    mLayoutClock.addView(clock);
                }
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
        findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, _force ? "updateClock" : "getSmallTime", new XC_MethodHook(){
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                //��ȡvClock����
                vClock = XposedHelpers.getAdditionalInstanceField(param.thisObject, "vClock");

                //���vClock == null ˵����ʱ����չ״̬�������û������չ��ʾ�ı�Ҫ����ֱ�������������в���
                if(vClock != null || _display){
                    //��ȡʱ�ӵ�����
                    if(_force){
                        textView = (TextView) param.thisObject; //����ֱ�ӻ�ȡ�������
                        originalText = (String) textView.getText().toString();
                    }else{
                        originalText = param.getResult().toString();
                    }

                    //����򿪹�������������ʽȥ��ԭʼʱ���е�����
                    if(_filter)
                        originalText = originalText.replaceAll("([����]��)|([AP]\\.?M\\.?)", "");

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
                    } else if(_info){
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
                    }else{
                        time = "";
                    }

                    if(_position){
                        //�������־���
                        if(_info){
                            timeSpan = new SpannableString(time + " ");
                            timeSpan.setSpan(new RelativeSizeSpan(_size), 0, timeSpan.length(), 0);
                            if(_color_info_s){
                                timeSpan.setSpan(new ForegroundColorSpan(_color_info), 0, timeSpan.length(), 0);
                            }
                        }else{
                            timeSpan = new SpannableString("");
                        }
                        if(vClock != null){
                            //�����ʾ״̬��
                            originalText = _clock ? originalText.trim() : "";
                            originalTextSpan =  new SpannableString(originalText);
                            //�����������ʾԭ��ʱ�ӣ������������Զ�����ɫ������£����д���
                            if(_clock && _color_clock_s){
                                originalTextSpan.setSpan(new ForegroundColorSpan(_color_clock), 0, originalTextSpan.length(), 0);
                            }
                            //�����ʾ���ڣ��Ҹ�ʽ��ȷ
                            if(_display_date && isFormatOk){
                                //�����ʾ�����Ҹ�ʽ��ȷ
                                date = sdf.format(System.currentTimeMillis()).replace("##", time);
                                if(_position_date){
                                    //�������
                                    dateSpan = new SpannableString(date + " ");
                                    dateSpan.setSpan(new RelativeSizeSpan(_size_date), 0, dateSpan.length(), 0);
                                    if(_color_date_s){
                                        dateSpan.setSpan(new ForegroundColorSpan(_color_date), 0, dateSpan.length(), 0);
                                    }
                                    if(_priority_date){
                                        //���������������
                                        timeText = TextUtils.concat(timeSpan, dateSpan, originalTextSpan);
                                    }else{
                                        timeText = TextUtils.concat(dateSpan, timeSpan, originalTextSpan);
                                    }
                                }else{
                                    //�������
                                    dateSpan = new SpannableString(" " + date);
                                    dateSpan.setSpan(new RelativeSizeSpan(_size_date), 0, dateSpan.length(), 0);
                                    //��������˶��������ֵ���Ⱦ
                                    if(_color_date_s){
                                        dateSpan.setSpan(new ForegroundColorSpan(_color_date), 0, dateSpan.length(), 0);
                                    }
                                    timeText = TextUtils.concat(timeSpan, originalTextSpan, dateSpan);
                                }
                            }else{
                                //û�������ʾ������Ϣ
                                timeText = TextUtils.concat(timeSpan, originalTextSpan);
                            }
                        }else if(_display){
                            //������չ״̬��
                            timeExpendedSpan = new SpannableString(time + " ");
                            timeExpendedSpan.setSpan(new RelativeSizeSpan(_size_expended), 0, timeExpendedSpan.length(), 0);
                            timeText = TextUtils.concat(timeExpendedSpan, originalText);
                        }
                    }else{
                        //�������־���
                        if(_info){
                            timeSpan = new SpannableString(" " + time);
                            timeSpan.setSpan(new RelativeSizeSpan(_size), 0, timeSpan.length(), 0);
                            if(_color_info_s){
                                timeSpan.setSpan(new ForegroundColorSpan(_color_info), 0, timeSpan.length(), 0);
                            }
                        }else{
                            timeSpan = new SpannableString("");
                        }
                        if(vClock != null){
                            originalText = _clock ? originalText.trim() : "";
                            originalTextSpan =  new SpannableString(originalText);
                            if(_clock && _color_clock_s){
                                originalTextSpan.setSpan(new ForegroundColorSpan(_color_clock), 0, originalTextSpan.length(), 0);
                            }
                            if(_display_date && isFormatOk){
                                date = sdf.format(System.currentTimeMillis()).replace("##", time);
                                if(_position_date){
                                    dateSpan = new SpannableString(date + " ");
                                    dateSpan.setSpan(new RelativeSizeSpan(_size_date), 0, dateSpan.length(), 0);
                                    if(_color_date_s){
                                        dateSpan.setSpan(new ForegroundColorSpan(_color_date), 0, dateSpan.length(), 0);
                                    }
                                    timeText = TextUtils.concat(dateSpan, originalTextSpan, timeSpan);
                                }else{
                                    dateSpan = new SpannableString(" " + date);
                                    dateSpan.setSpan(new RelativeSizeSpan(_size_date), 0, dateSpan.length(), 0);
                                    if(_color_date_s){
                                        dateSpan.setSpan(new ForegroundColorSpan(_color_date), 0, dateSpan.length(), 0);
                                    }
                                    if(_priority_date){
                                        timeText = TextUtils.concat(originalTextSpan, timeSpan, dateSpan);
                                    }else{
                                        timeText = TextUtils.concat(originalTextSpan, dateSpan, timeSpan);
                                    }
                                }
                            }else{
                                timeText = TextUtils.concat(originalTextSpan, timeSpan);
                            }
                        }else if(_display){
                            timeExpendedSpan = new SpannableString(" " + time);
                            timeExpendedSpan.setSpan(new RelativeSizeSpan(_size_expended), 0, timeExpendedSpan.length(), 0);
                            timeText = TextUtils.concat(originalText, timeExpendedSpan);
                        }
                    }
                    if(_force){
                        //д��textView
                        textView.setText(timeText);
                    }else{
                        //д��param
                        param.setResult(timeText);
                    }
                }
            }
        });
    }
}