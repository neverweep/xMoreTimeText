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
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class Setting extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private ListPreference lp;
    private EditTextPreference etp;
    private SwitchPreference sp;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        //监听sharedPreferences变化
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //注册监听事件
        prefs.registerOnSharedPreferenceChangeListener(this);

        lp = (ListPreference) findPreference("position");
        if(lp.getValue().toString().equals("true")){
            lp.setSummary(getString(R.string.position_summary_1) + getString(R.string.left_s) + getString(R.string.position_summary_2));
        }else{
            lp.setSummary(getString(R.string.position_summary_1) + getString(R.string.right_s) + getString(R.string.position_summary_2));
        }
        lp = (ListPreference) findPreference("position_date");
        if(lp.getValue().toString().equals("true")){
            lp.setSummary(getString(R.string.position_summary_1) + getString(R.string.left_s) + getString(R.string.position_summary_2));
        }else{
            lp.setSummary(getString(R.string.position_summary_1) + getString(R.string.right_s) + getString(R.string.position_summary_2));
        }
        lp = (ListPreference) findPreference("priority_date");
        if(lp.getValue().toString().equals("true")){
            lp.setSummary(R.string.basic_first);
        }else{
            lp.setSummary(lp.getEntry());
        }
        lp = (ListPreference) findPreference("size");
        if(lp.getValue().toString().equals("1.0") || "".equals(lp.getValue())){
            lp.setSummary(R.string.s10);
        }else{
            lp.setSummary(lp.getEntry());
        }
        lp = (ListPreference) findPreference("size_date");
        if(lp.getValue().toString().equals("1.0") || "".equals(lp.getValue())){
            lp.setSummary(R.string.s10);
        }else{
            lp.setSummary(lp.getEntry());
        }
        etp = (EditTextPreference) findPreference("format_date");
        if(!"".equals(etp.getText()) && etp.getText() != null){
            etp.setSummary(etp.getText());
        }else{
            etp.setSummary(getString(R.string.notset));
        }
        etp = (EditTextPreference) findPreference("surrounding_left");
        if(!"".equals(etp.getText()) && etp.getText() != null){
            etp.setSummary(etp.getText());
        }else{
            etp.setSummary(getString(R.string.notset));
        }
        etp = (EditTextPreference) findPreference("surrounding_right");
        if(!"".equals(etp.getText()) && etp.getText() != null){
            etp.setSummary(etp.getText());
        }else{
            etp.setSummary(getString(R.string.notset));
        }
        lp = (ListPreference) findPreference("size_surrounding");
        if(lp.getValue().toString().equals("1.0") || "".equals(lp.getValue())){
            lp.setSummary(R.string.s10);
        }else{
            lp.setSummary(lp.getEntry());
        }
        lp = (ListPreference) findPreference("surrounding_position");
        if(lp.getValue().equals("true")){
            lp.setSummary(R.string.surrounding_position_in);
        }else{
            lp.setSummary(lp.getEntry());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //设置有变化、则重新读取设置并显示在界面上
        if(key.equals("second")){
            sp = (SwitchPreference) findPreference("second");
            if(sp.isChecked()){
                Toast.makeText(this, getString(R.string.second_note), Toast.LENGTH_LONG).show();
            }
            return;
        }
        if(key.equals("clock")){
            sp = (SwitchPreference) findPreference("clock");
            if(sp.isChecked() == false){
                sp = (SwitchPreference) findPreference("second");
                sp.setChecked(false);
                sp = (SwitchPreference) findPreference("filter");
                sp.setChecked(false);
                sp = (SwitchPreference) findPreference("color_clock_s");
                sp.setChecked(false);
            }
            return;
        }
        if(key.equals("display_date")){
            sp = (SwitchPreference) findPreference("display_date");
            if(sp.isChecked() == false){
                sp = (SwitchPreference) findPreference("color_date_s");
                sp.setChecked(false);
            }
            return;
        }
        if(key.equals("surrounding")){
            sp = (SwitchPreference) findPreference("surrounding");
            if(sp.isChecked() == false){
                sp = (SwitchPreference) findPreference("color_surrounding_s");
                sp.setChecked(false);
            }
            return;
        }
        if(key.equals("basic_info")){
            sp = (SwitchPreference) findPreference("basic_info");
            if(sp.isChecked() == false){
                sp = (SwitchPreference) findPreference("color_info_s");
                sp.setChecked(false);
            }
            return;
        }
        if(key.equals("position")){
            lp = (ListPreference) findPreference("position");
            if(lp.getValue().toString().equals("true")){
                lp.setSummary(getString(R.string.position_summary_1) + getString(R.string.left) + getString(R.string.position_summary_2));
            }else{
                lp.setSummary(getString(R.string.position_summary_1) + getString(R.string.right) + getString(R.string.position_summary_2));
            }
            return;
        }
        if(key.equals("position_date")){
            lp = (ListPreference) findPreference("position_date");
            if(lp.getValue().toString().equals("true")){
                lp.setSummary(getString(R.string.position_summary_1) + getString(R.string.left) + getString(R.string.position_summary_2));
            }else{
                lp.setSummary(getString(R.string.position_summary_1) + getString(R.string.right) + getString(R.string.position_summary_2));
            }
            return;
        }
        if(key.equals("priority_date")){
            lp = (ListPreference) findPreference("priority_date");
            lp.setSummary(lp.getEntry());
            return;
        }
        if(key.equals("surrounding_position")){
            lp = (ListPreference) findPreference("surrounding_position");
            lp.setSummary(lp.getEntry());
            return;
        }
        if(key.equals("size")){
            lp = (ListPreference) findPreference("size");
            lp.setSummary(lp.getEntry());
            return;
        }
        if(key.equals("size_date")){
            lp = (ListPreference) findPreference("size_date");
            lp.setSummary(lp.getEntry());
            return;
        }
        if(key.equals("size_surrounding")){
            lp = (ListPreference) findPreference("size_surrounding");
            lp.setSummary(lp.getEntry());
            return;
        }
        if(key.equals("format_date")){
            etp = (EditTextPreference) findPreference("format_date");
            if(!"".equals(etp.getText()) && etp.getText() != null){
                etp.setSummary(etp.getText());
            }else{
                etp.setSummary(getString(R.string.notset));
            }
            return;
        }
        if(key.equals("surrounding_left")){
            etp = (EditTextPreference) findPreference("surrounding_left");
            if(!"".equals(etp.getText()) && etp.getText() != null){
                etp.setSummary(etp.getText());
            }else{
                etp.setSummary(getString(R.string.notset));
            }
            return;
        }
        if(key.equals("surrounding_right")){
            etp = (EditTextPreference) findPreference("surrounding_right");
            if(!"".equals(etp.getText()) && etp.getText() != null){
                etp.setSummary(etp.getText());
            }else{
                etp.setSummary(getString(R.string.notset));
            }
            return;
        }
    }

    //创建ActionBar右上角按钮
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu); //菜单选项调用 /menu/about.xml
        return true;
    }

    //按钮点击行为
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //关于
        builder.setIcon(R.drawable.ic_info); //图标资源调用 /drawable/ic_info
        builder.setTitle(R.string.about); //标题设为 @string/about
        builder.setView(inflater.inflate(R.layout.about, null)); //设置布局
        builder.setPositiveButton(R.string.ok, null); //设置按钮，仅设置一个确定按钮
        builder.show(); //显示对话框
        return true;
    }
}