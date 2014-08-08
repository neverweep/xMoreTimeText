/*
 * Copyright (C) 2014 XiaoXia(http://xiaoxia.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.xiaoxia.xmoretimetext;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.os.Build;
import android.os.Handler;
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
    private static final String CLASS_NAME   = "com.android.systemui.statusbar.policy.Clock";

    private static CharSequence info;
    private static CharSequence finalTextSpan;
    private static CharSequence finalClockSpan;
    private static SpannableString infoSpan;
    private static SpannableString dateSpan;
    private static SpannableString clockSpan;
    private static SpannableString leftSpan;
    private static SpannableString rightSpan;
    private static String[] preText;
    private static SimpleDateFormat sdf;
    private static SimpleDateFormat sdfSecond;
    private static SimpleDateFormat sdfInfo;
    private static SimpleDateFormat sdfMarker;
    private static String clockText;
    private static String date;
    private static Object vClock = null;
    private static Boolean isFormatOk;
    private static TextView textView;
    private static TextView mClock = null;
    private static ContentResolver cv;
    private static Handler mHandler; 
    private static Runnable mTicker ;
    private static String marker;
    private static Boolean hasMarker;
    private static Boolean markerAtHead;
    private static Boolean is24h;
    private static Boolean secondRun = false;

    //ʹ��Xposed�ṩ��XSharedPreferences��������ȡandroid���õ�SharedPreferences����
    private final static XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName());


    /*Basic*/
    //�Ƿ����
    protected final static Boolean _center = prefs.getBoolean("center", false);
    //ǿ�Ƹ���
    protected final static Boolean _force =  prefs.getBoolean("force", false);

    /*Clock*/
    //��ʾʱ��
    protected final static Boolean _clock = prefs.getBoolean("clock", false);
    //ɾ��ԭ�ַ�
    protected final static Boolean _filter = prefs.getBoolean("filter", false);
    //������ʾ
    protected final static Boolean _second = prefs.getBoolean("second", false);
    //�Զ���ԭ��ʱ����ɫ
    protected final static int _color_clock = prefs.getInt("color_clock", -16777216);
    //�Զ���ԭ��ʱ����ɫ����
    protected final static Boolean _color_clock_s = prefs.getBoolean("color_clock_s", false);

    /*Info*/
    //�Ƿ���ʾ��Ϣ
    protected final static Boolean _info = prefs.getBoolean("basic_info", false);
    //�Զ�����Ϣ
    protected final static Boolean _customize = prefs.getBoolean("customize", false);
    //�Զ�����Ϣλ��
    protected final static Boolean _position = prefs.getString("position", "true").equals("true");
    //�Զ�����Ϣ��С
    protected final static Float _size = Float.valueOf(prefs.getString("size", "1.0"));
    //�Զ�����Ϣ��ɫ
    protected final static int _color_info = prefs.getInt("color_info", -16777216);
    //�Զ�����Ϣ��ɫ����
    protected final static Boolean _color_info_s =  prefs.getBoolean("color_info_s", false);

    /*Surrounding*/
    //�Զ��廷�ƿ���
    protected static Boolean _surrounding =  prefs.getBoolean("surrounding", false);
    //�Զ��廷���ı�
    protected final static String _surrounding_left =  prefs.getString("surrounding_left", "");
    protected final static String _surrounding_right =  prefs.getString("surrounding_right", "");
    //�Զ��廷�ƴ�С
    protected final static Float _size_surrounding = Float.valueOf(prefs.getString("size_surrounding", "1.0"));
    //�Զ��廷����ɫ
    protected final static int _color_surrounding = prefs.getInt("color_surrounding", -16777216);
    //�Զ��廷����ɫ����
    protected final static Boolean _color_surrounding_s =  prefs.getBoolean("color_surrounding_s", false);
    //�Զ��廷��λ��
    protected final static Boolean _surrounding_position =  prefs.getString("surrounding_position", "true").equals("true");

    /*Date*/
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
    //�Զ���������ɫ
    protected final static int _color_date = prefs.getInt("color_date", -16777216);
    //�Զ���������ɫ����
    protected final static Boolean _color_date_s =  prefs.getBoolean("color_date_s", false);
    

    //�ж�ʱ����Ƿ���õĲ������� time period validation array 
    private static Boolean[] pValidaty = {
        false, false, false, false, false, false, false, false, false, false
    };
    //��ȡ�Զ������� custom period titles array
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
    //��ȡ�Զ�����ʼʱ�� custom period start time
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
    //��ȡ�Զ������ʱ��  custom period end time
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

    @SuppressLint("SimpleDateFormat")
    public void handleLoadPackage(final LoadPackageParam lpparam) {
        //�������systemui������ return if package is not systemui
        if (!lpparam.packageName.equals(PACKAGE_NAME))
            return;

        //��ȡ�������� load android area and lang settings
        String local = Locale.getDefault().getCountry();
        String lan = Locale.getDefault().getLanguage(); 
        //������������Ĭ������ set default period titles based on lang and area
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

        //��������������ĳ��ʱ����Ƿ���� set a boolean to know if the period is validated
        for (int i = 0; i <= 9; i++) {
            pValidaty[i] = !"".equals(pEnd[i]) && !"".equals(pStart[i]) && pEnd[i] != -1 && pStart[i] != -1;
        }

        //�ж����ڸ�ʽ�ַ����Ƿ���Ч��������Щ����û��Ҫ���� if the user custom date format is validated or not
        isFormatOk = !"".equals(_format_date) && _format_date != null;
        //��ʼ��SimpleDateFormat���� init sdf obeject
        if(isFormatOk)
            sdf = new SimpleDateFormat(_format_date);
        sdfInfo = new SimpleDateFormat("HHmm");
        sdfMarker = new SimpleDateFormat("a");

        _surrounding = _surrounding && (!"".equals(_surrounding_left) || !"".equals(_surrounding_right));
        if(_surrounding){
            leftSpan =  new SpannableString(_surrounding_left);
            rightSpan =  new SpannableString(_surrounding_right);
            leftSpan.setSpan(new RelativeSizeSpan(_size_surrounding), 0, leftSpan.length(), 0);
            rightSpan.setSpan(new RelativeSizeSpan(_size_surrounding), 0, rightSpan.length(), 0);
            if(_color_surrounding_s){
                leftSpan.setSpan(new ForegroundColorSpan(_color_surrounding), 0, leftSpan.length(), 0);
                rightSpan.setSpan(new ForegroundColorSpan(_color_surrounding), 0, rightSpan.length(), 0);
            }
        }

        if(_second){
            //�����Ҫ�����ʱ����clock���º���������ͬʱ����ÿ�����date��info  if enable seconds is set to true. keep updating date and info text every minute. not every second.
            findAndHookMethod(CLASS_NAME, lpparam.classLoader, "updateClock", new XC_MethodHook(){
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if(mClock == null)
                        mClock = (TextView) param.thisObject;

                    if (mClock != null){
                        if(secondRun == false){
                            secondRun = true;
                            String mText = mClock.getText().toString().trim();
                            markerAtHead = mText.indexOf(":") > 4 ? false : true;
                            hasMarker = mText.length() >= 7;
                            //����ϵͳ�Ƿ���24Сʱ�ƾ��������ʱ�ĸ�ʽ know if the android is 24h or 12h
                            cv = mClock.getContext().getContentResolver();
                            String strTimeFormat = android.provider.Settings.System.getString(cv, android.provider.Settings.System.TIME_12_24);
                            is24h = strTimeFormat != null && strTimeFormat.equals("24");
                            if (is24h) {
                                sdfSecond = new SimpleDateFormat("H:mm:ss");
                            }else{
                                sdfSecond = new SimpleDateFormat("h:mm:ss");
                            }
                            updateInfoAndDate();
                            timerStart();
                        }else{
                            timerStart();
                            if(!_filter)
                                marker = sdfMarker.format(System.currentTimeMillis());
                            updateInfoAndDate(); //��������ÿ���Ӹ���һ�Σ�����������Դ avoid cost much cpu resource
                            tick(false);
                        }
                    }
                }
            });
        }else{
            //����Clock���º�
            findAndHookMethod(CLASS_NAME, lpparam.classLoader, _force ? "updateClock" : "getSmallTime", new XC_MethodHook(){
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    //��ȡvClock���� get the vclock object
                    vClock = XposedHelpers.getAdditionalInstanceField(param.thisObject, "vClock");

                    //���vClock == null ˵����ʱ����չ״̬�������û������չ��ʾ�ı�Ҫ����ֱ�������������в��� if vClock == null, the statusbar is expended. no need to update clock text.
                    if(vClock != null){
                        //��ȡʱ�ӵ�����
                        if(_force){
                            textView = (TextView) param.thisObject; //����ֱ�ӻ�ȡ�������
                            clockText = (String) textView.getText().toString();
                        }else{
                            clockText = param.getResult().toString();
                        }

                        updateInfoAndDate();//����info��date��Ϣ update date and info text

                        if(_force){
                            //д��textView
                            textView.setText(textParse(clockText));
                        }else{
                            //д��param
                            param.setResult(textParse(clockText));
                        }
                    }
                }
            });
        }
    }

    //�˴�������Ϊ����systemui��ʼ����Դ�Ĺ����У���״̬��clock����һ��û�õ�vClock��Ȼ��Ϳ����ж��ǲ���Ϊ״̬����Clock�ˡ�
    @SuppressLint("SimpleDateFormat")
    public void handleInitPackageResources(final InitPackageResourcesParam resparam) {
        if (!resparam.packageName.equals(PACKAGE_NAME))
            return;

        /*
         * Center clock view and know if is expended status bar. 
         * Thanks for the work of GravityBox by C3C076@xda
         * https://github.com/GravityBox/GravityBox/blob/jellybean/src/com/ceco/gm2/gravitybox/ModStatusBar.java
         *
         *
         * Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
         * Licensed under the Apache License, Version 2.0 (the "License");
         * you may not use this file except in compliance with the License.
         * You may obtain a copy of the License at
         *
         *      http://www.apache.org/licenses/LICENSE-2.0
         *
         * Unless required by applicable law or agreed to in writing, software
         * distributed under the License is distributed on an "AS IS" BASIS,
         * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
         * See the License for the specific language governing permissions and
         * limitations under the License.
         */

        String layout = "lenovo_gemini_super_status_bar";
        try{
            resparam.res.hookLayout(PACKAGE_NAME, "layout", layout, new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {}
            });
        }catch(Throwable t){
            layout = Utils.hasGeminiSupport() ? "gemini_super_status_bar" : "super_status_bar";
        }

        resparam.res.hookLayout(PACKAGE_NAME, "layout", layout, new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam){
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
                mClock = (TextView) mIconArea.findViewById(liparam.res.getIdentifier("clock", "id", PACKAGE_NAME));
                if (mClock == null && mSbContents != null) {
                    mClock = (TextView) mSbContents.findViewById(liparam.res.getIdentifier("clock", "id", PACKAGE_NAME));
                    mClockInSbContents = mClock != null;
                }
                if(_center){
                    if (mClockInSbContents) {
                        mSbContents.removeView(mClock);
                    } else {
                        mIconArea.removeView(mClock);
                    }
                    mClock.setGravity(Gravity.CENTER);
                    mClock.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                    mClock.setPadding(0, 0, 0, 0);
                    mSbContents.removeView(mClock);
                    mLayoutClock.addView(mClock);
                }
                if(mClock != null)
                    XposedHelpers.setAdditionalInstanceField(mClock, "vClock", true);
            }
        });
    }

    private static void updateInfoAndDate(){
        int hm = Integer.parseInt(sdfInfo.format(System.currentTimeMillis())); //��ȡʱ��  read time string as "hmm" format.
        //������Զ��壬��
        if (_customize) {
            //ʱ���ж�
            info = "";
            for (int i = 0; i <= 9; i++) {
                if (pValidaty[i] && hm <= pEnd[i] && hm >= pStart[i]) {
                    info = pTitle[i];
                    break;
                }
            }
        } else if(_info){
            //ʱ���ж�
            if (hm < 600 && hm >= 0) {
                info = preText[0];
            } else if (hm < 800 && hm >= 600) {
                info = preText[1];
            } else if (hm < 1100 && hm >= 800) {
                info = preText[2];
            } else if (hm < 1200 && hm >= 1100) {
                info = preText[3];
            } else if (hm < 1400 && hm >= 1200) {
                info = preText[4];
            } else if (hm < 1730 && hm >= 1400) {
                info = preText[5];
            } else if (hm < 1930 && hm >= 1730) {
                info = preText[6];
            } else {
                info = preText[7];
            }
        }else{
            info = "";
        }

        if(_info & !"".equals(_info)){
            infoSpan = new SpannableString(info);
            infoSpan.setSpan(new RelativeSizeSpan(_size), 0, infoSpan.length(), 0);
            if(_color_info_s)
                infoSpan.setSpan(new ForegroundColorSpan(_color_info), 0, infoSpan.length(), 0);
        }else{
            infoSpan = new SpannableString("");
        }

        if(_display_date && isFormatOk){
            date = sdf.format(System.currentTimeMillis()).replace("##", info);
            dateSpan = new SpannableString(date);
            dateSpan.setSpan(new RelativeSizeSpan(_size_date), 0, dateSpan.length(), 0);
            if(_color_date_s)
                dateSpan.setSpan(new ForegroundColorSpan(_color_date), 0, dateSpan.length(), 0);
        }else{
            dateSpan = new SpannableString("");
        }
    }

    @SuppressLint("SimpleDateFormat")
    private static CharSequence textParse(String originalClockText){
        if(_clock){
            if(_second){
                //�����������ÿ��Ҫ���µ� update every second
                if(!(_filter && is24h)){
                    originalClockText = hasMarker ? markerAtHead ? marker + " " + sdfSecond.format(System.currentTimeMillis()) : sdfSecond.format(System.currentTimeMillis()) + " " + marker : sdfSecond.format(System.currentTimeMillis());
                }else{
                    originalClockText = sdfSecond.format(System.currentTimeMillis());
                }
            }else{
                if(_filter){
                    marker = sdfMarker.format(System.currentTimeMillis());
                    originalClockText = originalClockText.replaceAll(marker, "").trim(); //����򿪹���ȥ��ԭʼʱ���е���������
                }
                originalClockText = originalClockText.trim();
            }
            clockSpan =  new SpannableString(originalClockText);
            //�����������ʾԭ��ʱ�ӣ������������Զ�����ɫ������£����д���
            if(_color_clock_s)
                clockSpan.setSpan(new ForegroundColorSpan(_color_clock), 0, clockSpan.length(), 0);
            finalClockSpan = clockSpan;
        }else{
            clockSpan = new SpannableString("");
            finalClockSpan = clockSpan;
        }

        //������ƴ�����ʱ����� if surrounding on and set to be displayed out of clock text
        if(_surrounding && !_surrounding_position){
            finalClockSpan = TextUtils.concat(leftSpan, clockSpan, rightSpan);
        }

        if(_position){
            //�������־���
            //�����ʾ���ڣ��Ҹ�ʽ��ȷ
            if(_position_date){
                //�������
                if(_priority_date){
                    //���������������
                    finalTextSpan = TextUtils.concat(infoSpan, infoSpan.length() > 0 ? " " : "", dateSpan, dateSpan.length() > 0 ? " " : "", finalClockSpan);
                }else{
                    //���������������
                    finalTextSpan = TextUtils.concat(dateSpan, dateSpan.length() > 0 ? " " : "", infoSpan, infoSpan.length() > 0 ? " " : "", finalClockSpan);
                }
            }else{
                //�������
                finalTextSpan = TextUtils.concat(infoSpan, infoSpan.length() > 0 ? " " : "", finalClockSpan,dateSpan.length() > 0 ? " " : "", dateSpan);
            }
        }else{
            //�������־���
            if(_position_date){
                finalTextSpan = TextUtils.concat(dateSpan, dateSpan.length() > 0 ? " " : "", finalClockSpan, infoSpan.length() > 0 ? " " : "", infoSpan);
            }else{
                if(_priority_date){
                    finalTextSpan = TextUtils.concat(finalClockSpan, infoSpan.length() > 0 ? " " : "", infoSpan, dateSpan.length() > 0 ? " " : "", dateSpan);
                }else{
                    finalTextSpan = TextUtils.concat(finalClockSpan, dateSpan.length() > 0 ? " " : "", dateSpan, infoSpan.length() > 0 ? " " : "", infoSpan);
                }
            }
        }

        //������ƴ����������������if surrounding on and set to be displayed out of all text
        if(_surrounding && _surrounding_position){
            finalTextSpan = TextUtils.concat(leftSpan, finalTextSpan, rightSpan);
        }
        return finalTextSpan;
    }

    /*
     * Enable seconds
     * Thanks for the work of XuiMod by zst123
     * https://github.com/zst123/XuiMod/blob/master/src/com/zst/xposed/xuimod/mods/SecondsClockMod.java
     *
     *
     * Copyright (C) 2013 XuiMod
     * 
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    private static void timerStart() {
        mHandler = new Handler(mClock.getContext().getMainLooper());
        mTicker = new Runnable() {
            public void run() {
                tickOnThread(); 
                waitOneSecond();
            }
        };
        mHandler.postDelayed(mTicker, 800);
    }

    private static void waitOneSecond() { 
        mHandler.postDelayed(mTicker, 985);
    }

    private static void tickOnThread() {
        final Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                tick(true);
            }
        });
        thread.start(); 
    }

    private static void tick(boolean changeTextWithHandler) {
        vClock = XposedHelpers.getAdditionalInstanceField(mClock, "vClock");
        if(vClock != null){
            if (changeTextWithHandler){
                setClockTextOnHandler(textParse(""));
            }else{
                mClock.setText(textParse(""));
            }
        }
    }

    private static void setClockTextOnHandler(final CharSequence time) {
        if (mHandler == null) {
            mHandler = new Handler(mClock.getContext().getMainLooper());
        } 
        mHandler.post(new Runnable() {
            public void run() {
                mClock.setText(time);
            }
        });
    }
}