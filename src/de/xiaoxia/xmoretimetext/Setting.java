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

        //����sharedPreferences�仯
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //ע������¼�
        prefs.registerOnSharedPreferenceChangeListener(this);

        lp = (ListPreference) findPreference("position");
        if(lp.getValue().toString().equals("true")){
            lp.setSummary(getString(R.string.position_summary_1) + getString(R.string.left) + getString(R.string.position_summary_2));
        }else{
            lp.setSummary(getString(R.string.position_summary_1) + getString(R.string.right) + getString(R.string.position_summary_2));
        }
        lp = (ListPreference) findPreference("position_date");
        if(lp.getValue().toString().equals("true")){
            lp.setSummary(getString(R.string.position_summary_1) + getString(R.string.left) + getString(R.string.position_summary_2));
        }else{
            lp.setSummary(getString(R.string.position_summary_1) + getString(R.string.right) + getString(R.string.position_summary_2));
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
        lp = (ListPreference) findPreference("size_expended");
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
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //�����б仯�������¶�ȡ���ò���ʾ�ڽ�����
        if(key.equals("clock")){
            sp = (SwitchPreference) findPreference("clock");
            if(sp.isChecked() == false){
                sp = (SwitchPreference) findPreference("filter");
                sp.setChecked(false);
                sp = (SwitchPreference) findPreference("color_clock_s");
                sp.setChecked(false);
            }
            return;
        }
        if(key.equals("display")){
            sp = (SwitchPreference) findPreference("display");
            lp = (ListPreference) findPreference("size_expended");
            lp.setEnabled(sp.isChecked());
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
        if(key.equals("basic_info")){
            sp = (SwitchPreference) findPreference("basic_info");
            if(sp.isChecked()){
                sp = (SwitchPreference) findPreference("display");
                lp = (ListPreference) findPreference("size_expended");
                lp.setEnabled(sp.isChecked());
            }else{
                sp = (SwitchPreference) findPreference("display");
                sp.setChecked(false);
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
        if(key.equals("size_expended")){
            lp = (ListPreference) findPreference("size_expended");
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
    }

    //����ActionBar���Ͻǰ�ť
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu); //�˵�ѡ����� /menu/about.xml
        return true;
    }

    //��ť�����Ϊ
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //����
        builder.setIcon(R.drawable.ic_info); //ͼ����Դ���� /drawable/ic_info
        builder.setTitle(R.string.about); //������Ϊ @string/about
        builder.setView(inflater.inflate(R.layout.about, null)); //���ò���
        builder.setPositiveButton(R.string.ok, null); //���ð�ť��������һ��ȷ����ť
        builder.show(); //��ʾ�Ի���
        return true;
    }
}