/*
* Copyright (C) 2016 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.omnirom.device;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.util.Log;
import androidx.preference.PreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import static org.omnirom.device.Constants.*;

public class DeviceSettings extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private ListPreference mAssistantKeyPref;
    private final static String mAssistantKey = "assistant_button";

    private ListPreference mFPTap;
    private ListPreference mFPHold;
    private ListPreference mFPLeft;
    private ListPreference mFPRight;

    private ListPreference mFPTapOff;
    private ListPreference mFPHoldOff;
    private ListPreference mFPLeftOff;
    private ListPreference mFPRightOff;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.main_panel);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        mAssistantKeyPref = (ListPreference) findPreference(mAssistantKey);
        Integer action = Settings.Global.getInt(getContext().getContentResolver(), mAssistantKey, 0);
        mAssistantKeyPref.setValue(Integer.toString(action));
        mAssistantKeyPref.setOnPreferenceChangeListener(this);

        mFPTap = (ListPreference) findPreference(FP_KEYS);
        mFPTap.setValue(Integer.toString(Settings.Global.getInt(getContext().getContentResolver(), FP_KEYS, 0)));
        mFPTap.setOnPreferenceChangeListener(this);

        mFPHold = (ListPreference) findPreference(FP_KEY_HOLD);
        mFPHold.setValue(Integer.toString(Settings.Global.getInt(getContext().getContentResolver(), FP_KEY_HOLD, 0)));
        mFPHold.setOnPreferenceChangeListener(this);

        mFPLeft = (ListPreference) findPreference(FP_KEY_LEFT);
        mFPLeft.setValue(Integer.toString(Settings.Global.getInt(getContext().getContentResolver(), FP_KEY_LEFT, 0)));
        mFPLeft.setOnPreferenceChangeListener(this);

        mFPRight = (ListPreference) findPreference(FP_KEY_RIGHT);
        mFPRight.setValue(Integer.toString(Settings.Global.getInt(getContext().getContentResolver(), FP_KEY_RIGHT, 0)));
        mFPRight.setOnPreferenceChangeListener(this);

        mFPTapOff = (ListPreference) findPreference(FP_KEYS_OFF);
        mFPTapOff.setValue(Integer.toString(Settings.Global.getInt(getContext().getContentResolver(), FP_KEYS_OFF, 0)));
        mFPTapOff.setOnPreferenceChangeListener(this);

        mFPHoldOff = (ListPreference) findPreference(FP_KEY_HOLD_OFF);
        mFPHoldOff.setValue(
                Integer.toString(Settings.Global.getInt(getContext().getContentResolver(), FP_KEY_HOLD_OFF, 0)));
        mFPHoldOff.setOnPreferenceChangeListener(this);

        mFPLeftOff = (ListPreference) findPreference(FP_KEY_LEFT_OFF);
        mFPLeftOff.setValue(
                Integer.toString(Settings.Global.getInt(getContext().getContentResolver(), FP_KEY_LEFT_OFF, 0)));
        mFPLeftOff.setOnPreferenceChangeListener(this);

        mFPRightOff = (ListPreference) findPreference(FP_KEY_RIGHT_OFF);
        mFPRightOff.setValue(
                Integer.toString(Settings.Global.getInt(getContext().getContentResolver(), FP_KEY_RIGHT_OFF, 0)));
        mFPRightOff.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAssistantKeyPref) {
            Settings.Global.putInt(getContext().getContentResolver(), mAssistantKey,
                    Integer.parseInt((String) newValue));
        } else if (preference == mFPTap) {
            Settings.Global.putInt(getContext().getContentResolver(), FP_KEYS, Integer.parseInt((String) newValue));
        } else if (preference == mFPHold) {
            Settings.Global.putInt(getContext().getContentResolver(), FP_KEY_HOLD, Integer.parseInt((String) newValue));
        } else if (preference == mFPLeft) {
            Settings.Global.putInt(getContext().getContentResolver(), FP_KEY_LEFT, Integer.parseInt((String) newValue));
        } else if (preference == mFPRight) {
            Settings.Global.putInt(getContext().getContentResolver(), FP_KEY_RIGHT,
                    Integer.parseInt((String) newValue));
        } else if (preference == mFPTapOff) {
            Settings.Global.putInt(getContext().getContentResolver(), FP_KEYS_OFF, Integer.parseInt((String) newValue));
        } else if (preference == mFPHoldOff) {
            Settings.Global.putInt(getContext().getContentResolver(), FP_KEY_HOLD_OFF,
                    Integer.parseInt((String) newValue));
        } else if (preference == mFPLeftOff) {
            Settings.Global.putInt(getContext().getContentResolver(), FP_KEY_LEFT_OFF,
                    Integer.parseInt((String) newValue));
        } else if (preference == mFPRightOff) {
            Settings.Global.putInt(getContext().getContentResolver(), FP_KEY_RIGHT_OFF,
                    Integer.parseInt((String) newValue));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
