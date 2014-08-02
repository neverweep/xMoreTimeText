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

public class Setting extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    static ContentResolver cv;
    private TimePreference tp;
    private ListPreference lp;
    private EditTextPreference etp;
    private String ps, pe, pt, tps;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        //��ȡContentResolver��ΪTimePreference��ȡ24Сʱ����׼��
        cv = this.getContentResolver();

        //����sharedPreferences�仯
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //ע������¼�
        prefs.registerOnSharedPreferenceChangeListener(this);

        ps = " " + getString(R.string.ps);
        pe = " " + getString(R.string.pe);
        pt = " " + getString(R.string.pt);
        tps = getString(R.string.tp) + " ";

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
        //�����б仯�������¶�ȡ���ò���ʾ�ڽ�����
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
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

        switch (item.getItemId()) {
            //����
            case R.id.about:
                builder.setIcon(R.drawable.ic_info); //ͼ����Դ���� /drawable/ic_info
                builder.setTitle(R.string.about); //������Ϊ @string/about
                builder.setView(inflater.inflate(R.layout.about, null)); //���ò���
                builder.setPositiveButton(R.string.ok, null); //���ð�ť��������һ��ȷ����ť
                builder.show(); //��ʾ�Ի���
                break;
            //����
            case R.id.help:
                builder.setIcon(R.drawable.ic_help);
                builder.setTitle(R.string.help);
                builder.setView(inflater.inflate(R.layout.help, null));
                builder.setPositiveButton(R.string.ok, null);
                builder.show(); //��ʾ�Ի���
                break;
            //�������
            case R.id.clear:
                builder.setIcon(R.drawable.ic_clear);
                builder.setTitle(R.string.clear);
                builder.setMessage(R.string.clear_msg);
                //���û�������
                builder.setPositiveButton(R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //�˴�������Ϊ��Ӧ��ť���º�Ķ���
                        //ɾ��ʱ�������
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Setting.this);
                        SharedPreferences.Editor editor = prefs.edit();
                        for (int i = 0; i <= 9; i++) {
                            editor.remove("ps" + i);
                            editor.remove("pe" + i);
                            editor.remove("pt" + i);
                        }
                        editor.commit();
                        dialog.dismiss(); //��������
                        Setting.this.finish();
                    }
                });
                //������������
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