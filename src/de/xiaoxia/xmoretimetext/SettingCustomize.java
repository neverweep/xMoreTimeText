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

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class SettingCustomize extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    static ContentResolver cv;
    private TimePreference tp;
    private EditTextPreference etp;
    private String ps, pe, pt, tps;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_customize);

        //设置返回按钮
        getActionBar().setDisplayHomeAsUpEnabled(true);


        //获取ContentResolver，为TimePreference读取24小时制做准备
        cv = this.getContentResolver();

        //监听sharedPreferences变化
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //注册监听事件
        prefs.registerOnSharedPreferenceChangeListener(this);

        ps = " " + getString(R.string.ps);
        pe = " " + getString(R.string.pe);
        pt = " " + getString(R.string.pt);
        tps = getString(R.string.tp) + " ";

        for (int i = 0; i <= 9; i++) {
            etp = (EditTextPreference) findPreference("pt" + i);
            if (!"".equals(etp.getText()) && etp.getText() != null) {
                etp.setSummary(etp.getText());
            }
            etp.setTitle(tps + (i + 1) + pt);
            tp = (TimePreference) findPreference("ps" + i);
            tp.setSummary(prefs.getString("ps" + i, getString(R.string.notset)));
            tp.setTitle(tps + (i + 1) + ps);
            tp = (TimePreference) findPreference("pe" + i);
            tp.setSummary(prefs.getString("pe" + i, getString(R.string.notset)));
            tp.setTitle(tps + (i + 1) + pe);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //设置有变化、则重新读取设置并显示在界面上
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 0; i <= 9; i++) {
            if (key.equals("pt" + i)) {
                etp = (EditTextPreference) findPreference("pt" + i);
                if (!"".equals(etp.getText()) && etp.getText() != null) {
                    etp.setSummary(etp.getText());
                } else {
                    etp.setSummary(R.string.notset);
                }
                return;
            }
            if (key.equals("ps" + i)) {
                tp = (TimePreference) findPreference("ps" + i);
                tp.setSummary(prefs.getString("ps" + i, getString(R.string.notset)));
                return;
            }
            if (key.equals("pe" + i)) {
                tp = (TimePreference) findPreference("pe" + i);
                tp.setSummary(prefs.getString("pe" + i, getString(R.string.notset)));
                return;
            }
        }
    }

    //创建ActionBar右上角按钮
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_customize, menu);
        return true;
    }

    //按钮点击行为
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (item.getItemId()) {
            case android.R.id.home:
                SettingCustomize.this.finish();
                break;
            //帮助
            case R.id.help:
                builder.setIcon(R.drawable.ic_help);
                builder.setTitle(R.string.help);
                builder.setView(inflater.inflate(R.layout.help, null));
                builder.setPositiveButton(R.string.ok, null);
                builder.show(); //显示对话框
                break;
            //清空设置
            case R.id.clear:
                builder.setIcon(R.drawable.ic_clear);
                builder.setTitle(R.string.clear);
                builder.setMessage(R.string.clear_msg);
                //设置积极按键
                builder.setPositiveButton(R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //此处的内容为响应按钮按下后的动作
                        //删除时间段设置
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingCustomize.this);
                        SharedPreferences.Editor editor = prefs.edit();
                        for (int i = 0; i <= 9; i++) {
                            editor.remove("ps" + i);
                            editor.remove("pe" + i);
                            editor.remove("pt" + i);
                        }
                        editor.commit();
                        dialog.dismiss(); //销毁自身
                        SettingCustomize.this.finish();
                    }
                });
                //设置消极按键
                builder.setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
        }
        return true;
    }
}