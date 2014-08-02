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

    //使用Xposed提供的XSharedPreferences方法来读取android内置的SharedPreferences设置
    private final static XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName());
    //删除原字符
    protected final static Boolean _filter = prefs.getBoolean("filter", false);
    //自定义
    protected final static Boolean _customize = prefs.getBoolean("customize", false);
    //是否在扩展状态栏显示
    protected final static Boolean _display = prefs.getBoolean("display", false);
    //自定义位置
    protected final static Boolean _position = prefs.getString("position", "true").equals("true");
    //自定义大小
    protected final static Float _size = Float.valueOf(prefs.getString("size", "1.0"));
    //是否显示日期
    protected final static Boolean _display_date = prefs.getBoolean("display_date", false);
    //自定义日期位置
    protected final static Boolean _position_date = prefs.getString("position_date", "true").equals("true");
    //自定义日期大小
    protected final static Boolean _priority_date = prefs.getString("priority_date", "true").equals("true");
    //自定义日期格式
    protected final static String _format_date = prefs.getString("format_date", "").trim();
    //自定义日期大小
    protected final static Float _size_date = Float.valueOf(prefs.getString("size_date", "1.0"));
    //自定义大小（扩展状态栏）
    protected final static Float _size_expended = Float.valueOf(prefs.getString("size_expended", "1.0"));
    //判断时间段是否可用的布尔数组
    private Boolean[] pValidaty = {
        false, false, false, false, false, false, false, false, false, false
    };
    //读取自定义名称
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
    //读取自定义起始时间
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
    //读取自定义结束时间
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

    //此处的作用为：在systemui初始化资源的过程中，向状态栏clock加入一个没用的vClock，然后就可以判断是不是为状态栏的Clock了。
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
        //如果不是systemui则跳过
        if (!lpparam.packageName.equals(PACKAGE_NAME))
            return;

        //读取语言设置
        String local = Locale.getDefault().getCountry();
        String lan = Locale.getDefault().getLanguage(); 
        //根据语言设置默认文字
        if (local.contains("TW") || local.contains("HK") || local.contains("MO")) {
            preText = new String[] {
                "淩晨", "早晨", "上午", "中午", "中午", "下午", "傍晚", "晚上"
            };
        } else if(lan.contains("zh")){
            preText = new String[] {
                "凌晨", "早晨", "上午", "中午", "中午", "下午", "傍晚", "晚上"
            };
        } else {
            preText = new String[] {
                "AM", "AM", "AM", "AM", "PM", "PM", "PM", "PM"
            };
        }

        //根据设置来设置某项时间段是否可用
        for (int i = 0; i <= 9; i++) {
            pValidaty[i] = !"".equals(pEnd[i]) && !"".equals(pStart[i]) && pEnd[i] != -1 && pStart[i] != -1;
        }

        //初始化日历实例
        calendar = Calendar.getInstance();

        //判断日期格式字符串是否有效，否则有些步骤没必要进行
        isFormatOk = !"".equals(_format_date) && _format_date != null;
        //初始化SimpleDateFormat对象
        if(isFormatOk)
            sdf = new SimpleDateFormat(_format_date);

        //勾在Clock更新后
        findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "getSmallTime", new XC_MethodHook(){
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                //获取vClock对象
                vClock = XposedHelpers.getAdditionalInstanceField(param.thisObject, "vClock");

                //如果vClock == null 说明此时是扩展状态栏，如果没有在扩展显示的必要，则直接跳过以下所有步骤
                if(vClock != null || _display){
                    //获取时钟的文字
                    originalText = param.getResult().toString();

                    //如果打开过滤则用正则表达式去除原始时间中的文字
                    if (_filter) {
                        originalText = originalText.replaceAll("([上下]午)|([AP]\\.?M\\.?)", "");
                    }

                    calendar.setTimeInMillis(System.currentTimeMillis()); //设定日历控件为当前时间
                    int hm = Integer.parseInt(calendar.get(Calendar.HOUR_OF_DAY) + df.format(calendar.get(Calendar.MINUTE))); //读取时间，hhmm

                    //如果打开自定义，则
                    if (_customize) {
                        //时间判断
                        for (int i = 0; i <= 9; i++) {
                            //XposedBridge.log(i + " -  EN:" + pValidaty[i] + " NOW:" + hm + " ED:" + pEnd[i] + " ST:" + pStart[i]);
                            if (pValidaty[i] && hm <= pEnd[i] && hm >= pStart[i]) {
                                time = pTitle[i];
                                break;
                            }
                        }
                    } else {
                        //时间判断
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

                    //写入param
                    originalText = originalText.trim();
                    if(_position){
                        //基本文字居左
                        if(vClock != null){
                            //如果显示状态栏
                            if(_display_date && isFormatOk){
                                //如果显示日期
                                temp = sdf.format(System.currentTimeMillis());
                                if(_position_date){
                                    //日期方向靠左
                                    if(_priority_date){
                                        //基本文字优先
                                        timeText = new SpannableString(time + " " + temp + " " + originalText);
                                        timeText.setSpan(new RelativeSizeSpan(_size), 0, time.length() + 1, 0);
                                        timeText.setSpan(new RelativeSizeSpan(_size_date), time.length() + 1, temp.length() + 1, 0);
                                    }else{
                                        //日期文字优先
                                        timeText = new SpannableString(temp + " " + time + " " + originalText);
                                        timeText.setSpan(new RelativeSizeSpan(_size_date), 0, temp.length() + 1, 0);
                                        timeText.setSpan(new RelativeSizeSpan(_size), temp.length() + 1, temp.length() + time.length() + 2, 0);
                                    }
                                }else{
                                    //日期方向靠右
                                    timeText = new SpannableString(time + " " + originalText + " " + temp);
                                    timeText.setSpan(new RelativeSizeSpan(_size), 0, temp.length() + 1, 0);
                                    timeText.setSpan(new RelativeSizeSpan(_size_date), time.length() + originalText.length() + 1, timeText.length(), 0);
                                }
                            }else{
                                timeText = new SpannableString(time + " " + originalText);
                                timeText.setSpan(new RelativeSizeSpan(_size), 0, time.length(), 0);
                            }
                        }else{
                            //如果扩展状态栏显示
                            timeText = new SpannableString(time + " " + originalText);
                            timeText.setSpan(new RelativeSizeSpan(_size_expended), 0, time.length(), 0);
                        }
                    }else{
                        if(vClock != null){
                            //状态栏显示
                            if(_display_date && isFormatOk){
                                //显示日期
                                temp = sdf.format(System.currentTimeMillis());
                                if(_position_date){
                                    //日期方向靠左
                                    timeText = new SpannableString(temp + " " + originalText + " " + time);
                                    timeText.setSpan(new RelativeSizeSpan(_size_date), 0, temp.length() + 1, 0);
                                    timeText.setSpan(new RelativeSizeSpan(_size), temp.length() + originalText.length() + 1, timeText.length(), 0);
                                }else{
                                    //日期方向靠右
                                    if(_priority_date){
                                        //基本文字优先
                                        timeText = new SpannableString(originalText + " " + time + " " + temp);
                                        timeText.setSpan(new RelativeSizeSpan(_size), originalText.length(), (originalText + time).length() + 1, 0);
                                        timeText.setSpan(new RelativeSizeSpan(_size_date), originalText.length() + time.length() + 2, timeText.length(), 0);
                                    }else{
                                        //日期文字优先
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
                            //扩展状态栏显示
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