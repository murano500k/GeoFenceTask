package com.example.geoapp.geoapp;

/**
 * Created by bovchynnikov on 24.07.17.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import com.example.geoapp.geostorage.GeofenceTable;
import com.google.android.gms.location.Geofence;


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String ACTIVE_PREF = "active_pref";
    private static final String NOTIF_PREF = "notif_pref";
    private static final String NOTIF_HIDE_PREF = "notif_hide_pref";
    private static final String RADIUS_PREF = "radius_pref";
    private static final String ENTER_PREF = "enter_pref";
    private static final String EXIT_PREF = "exit_pref";
    private static final String DWELL_PREF = "dwell_pref";
    private static final String HAS_VISITED = "hasVisited";
    public static final String APP_PREFERENCES = "geo_settings";
    public static final String SP_NOTIF = "sp_notif";
    public static final String SP_NOTIF_HIDE = "sp_notif_hide";
    private EditTextPreference etpRadius;
    private SharedPreferences mSharedPreferences;
    private GeofenceTable gt;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_headers);
        SwitchPreference cbpActive = (SwitchPreference)findPreference(ACTIVE_PREF);
        etpRadius = (EditTextPreference)findPreference(RADIUS_PREF);
        CheckBoxPreference cbpEnter = (CheckBoxPreference)findPreference(ENTER_PREF);
        CheckBoxPreference cbpExit = (CheckBoxPreference)findPreference(EXIT_PREF);
        CheckBoxPreference cbpDwell = (CheckBoxPreference)findPreference(DWELL_PREF);
        if(gt != null) {
            cbpActive.setChecked(gt.isActive);
            String rds = new Float(gt.radius).toString();
            etpRadius.setText(rds);
            etpRadius.setTitle("Radius: " + rds);
            cbpEnter.setChecked((gt.transitionType & Geofence.GEOFENCE_TRANSITION_ENTER) != 0);
            cbpExit.setChecked((gt.transitionType & Geofence.GEOFENCE_TRANSITION_EXIT) != 0);
            cbpDwell.setChecked((gt.transitionType & Geofence.GEOFENCE_TRANSITION_DWELL) != 0);
        }
        CheckBoxPreference cbpNotif = (CheckBoxPreference)findPreference(NOTIF_PREF);
        CheckBoxPreference cbpNotifHide = (CheckBoxPreference)findPreference(NOTIF_HIDE_PREF);
        mSharedPreferences = this.getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        boolean hasVisited = mSharedPreferences.getBoolean(HAS_VISITED, false);
        if(!hasVisited) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(SP_NOTIF, true);
            editor.putBoolean(SP_NOTIF_HIDE, false);
            editor.putBoolean(HAS_VISITED, true);
            editor.apply();
            cbpNotif.setChecked(true);
            cbpNotifHide.setChecked(false);
        } else {
            cbpNotif.setChecked(mSharedPreferences.getBoolean(SP_NOTIF, true));
            cbpNotifHide.setChecked(mSharedPreferences.getBoolean(SP_NOTIF_HIDE, false));
        }
    }

    public void initGeofenceTable(GeofenceTable gt) {
        this.gt = gt;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(ACTIVE_PREF)) {
            gt.isActive = ((SwitchPreference)findPreference(key)).isChecked();
        } else if(key.equals(ENTER_PREF)) {
            if(((CheckBoxPreference)findPreference(key)).isChecked())
                gt.transitionType |= Geofence.GEOFENCE_TRANSITION_ENTER;
            else {
                int i = ~0;
                i ^= Geofence.GEOFENCE_TRANSITION_ENTER;
                gt.transitionType &= i;
            }
        } else if(key.equals(EXIT_PREF)) {
            if(((CheckBoxPreference)findPreference(key)).isChecked())
                gt.transitionType |= Geofence.GEOFENCE_TRANSITION_EXIT;
            else {
                int i = ~0;
                i ^= Geofence.GEOFENCE_TRANSITION_EXIT;
                gt.transitionType &= i;
            }
        } else if(key.equals(DWELL_PREF)) {
            if(((CheckBoxPreference)findPreference(key)).isChecked())
                gt.transitionType |= Geofence.GEOFENCE_TRANSITION_DWELL;
            else {
                int i = ~0;
                i ^= Geofence.GEOFENCE_TRANSITION_DWELL;
                gt.transitionType &= i;
            }
        } else if(key.equals(RADIUS_PREF)) {
            String str = etpRadius.getText();
            gt.radius = Float.parseFloat(str);
            etpRadius.setTitle("Radius: " + str);
            etpRadius.setText(str);
        } else if(key.equals(NOTIF_PREF)) {
            mSharedPreferences = this.getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(SP_NOTIF, ((CheckBoxPreference)findPreference(NOTIF_PREF)).isChecked());
            editor.apply();
        } else if(key.equals(NOTIF_HIDE_PREF)) {
            mSharedPreferences = this.getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(SP_NOTIF_HIDE, ((CheckBoxPreference)findPreference(NOTIF_HIDE_PREF)).isChecked());
            editor.apply();
        }
    }
}