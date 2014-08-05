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

    //使用Xposed提供的XSharedPreferences方法来读取android内置的SharedPreferences设置
    private final static XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName());
    //是否显示信息
    protected final static Boolean _center = prefs.getBoolean("center", false);
    //是否显示信息
    protected final static Boolean _info = prefs.getBoolean("basic_info", true);
    //显示时钟
    protected final static Boolean _clock = prefs.getBoolean("clock", false);
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
    //自定义原生时钟颜色
    protected final static int _color_clock = prefs.getInt("color_clock", -16777216);
    //自定义原生时钟颜色开关
    protected final static Boolean _color_clock_s = prefs.getBoolean("color_clock_s", false);
    //自定义信息颜色
    protected final static int _color_info = prefs.getInt("color_info", -16777216);
    //自定义信息颜色开关
    protected final static Boolean _color_info_s =  prefs.getBoolean("color_info_s", false);
    //自定义日期颜色
    protected final static int _color_date = prefs.getInt("color_date", -16777216);
    //自定义日期颜色开关
    protected final static Boolean _color_date_s =  prefs.getBoolean("color_date_s", false);
    //强制更新
    protected final static Boolean _force =  prefs.getBoolean("force", false);
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
        findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, _force ? "updateClock" : "getSmallTime", new XC_MethodHook(){
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                //获取vClock对象
                vClock = XposedHelpers.getAdditionalInstanceField(param.thisObject, "vClock");

                //如果vClock == null 说明此时是扩展状态栏，如果没有在扩展显示的必要，则直接跳过以下所有步骤
                if(vClock != null || _display){
                    //获取时钟的文字
                    if(_force){
                        textView = (TextView) param.thisObject; //所以直接获取这个对象
                        originalText = (String) textView.getText().toString();
                    }else{
                        originalText = param.getResult().toString();
                    }

                    //如果打开过滤则用正则表达式去除原始时间中的文字
                    if(_filter)
                        originalText = originalText.replaceAll("([上下]午)|([AP]\\.?M\\.?)", "");

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
                    } else if(_info){
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
                    }else{
                        time = "";
                    }

                    if(_position){
                        //基本文字居左
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
                            //如果显示状态栏
                            originalText = _clock ? originalText.trim() : "";
                            originalTextSpan =  new SpannableString(originalText);
                            //如果开启了显示原生时钟，且在设置了自定义颜色的情况下，进行处理
                            if(_clock && _color_clock_s){
                                originalTextSpan.setSpan(new ForegroundColorSpan(_color_clock), 0, originalTextSpan.length(), 0);
                            }
                            //如果显示日期，且格式正确
                            if(_display_date && isFormatOk){
                                //如果显示日期且格式正确
                                date = sdf.format(System.currentTimeMillis()).replace("##", time);
                                if(_position_date){
                                    //如果靠左
                                    dateSpan = new SpannableString(date + " ");
                                    dateSpan.setSpan(new RelativeSizeSpan(_size_date), 0, dateSpan.length(), 0);
                                    if(_color_date_s){
                                        dateSpan.setSpan(new ForegroundColorSpan(_color_date), 0, dateSpan.length(), 0);
                                    }
                                    if(_priority_date){
                                        //如果基本文字优先
                                        timeText = TextUtils.concat(timeSpan, dateSpan, originalTextSpan);
                                    }else{
                                        timeText = TextUtils.concat(dateSpan, timeSpan, originalTextSpan);
                                    }
                                }else{
                                    //如果靠右
                                    dateSpan = new SpannableString(" " + date);
                                    dateSpan.setSpan(new RelativeSizeSpan(_size_date), 0, dateSpan.length(), 0);
                                    //如果开启了对日期文字的渲染
                                    if(_color_date_s){
                                        dateSpan.setSpan(new ForegroundColorSpan(_color_date), 0, dateSpan.length(), 0);
                                    }
                                    timeText = TextUtils.concat(timeSpan, originalTextSpan, dateSpan);
                                }
                            }else{
                                //没有则仅显示基本信息
                                timeText = TextUtils.concat(timeSpan, originalTextSpan);
                            }
                        }else if(_display){
                            //设置扩展状态栏
                            timeExpendedSpan = new SpannableString(time + " ");
                            timeExpendedSpan.setSpan(new RelativeSizeSpan(_size_expended), 0, timeExpendedSpan.length(), 0);
                            timeText = TextUtils.concat(timeExpendedSpan, originalText);
                        }
                    }else{
                        //基本文字居右
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
                        //写入textView
                        textView.setText(timeText);
                    }else{
                        //写入param
                        param.setResult(timeText);
                    }
                }
            }
        });
    }
}