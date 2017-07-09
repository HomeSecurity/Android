package com.hosec.homesecurity.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.activities.dialogs.ChangeSystemDialog;
import com.hosec.homesecurity.model.Credentials;

/**
 * Settings fragment containing all preferences
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String defaultValue = Credentials.DEFAULT_VALUE;

        if(key.equals(Credentials.SYSTEM_PREF_KEY) &&
                sharedPreferences.getString(key,defaultValue).equals(defaultValue)){
            Intent intent = new Intent(this.getActivity(),LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            this.getActivity().startActivity(intent);

        }
    }
}
