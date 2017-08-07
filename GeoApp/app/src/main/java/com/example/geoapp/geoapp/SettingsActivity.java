package com.example.geoapp.geoapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.geoapp.geostorage.GeofenceTable;
import com.google.android.gms.location.Geofence;

public class SettingsActivity extends AppCompatActivity {

    private static GeofenceTable gt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        gt = (GeofenceTable)intent.getParcelableExtra(MapsActivity.GEOFENCE_TABLE);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public MenuInflater getMenuInflater() {
        return super.getMenuInflater();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings_geo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.item_delete) {
            Intent intent = new Intent();
            intent.putExtra(MapsActivity.GEOFENCE_TABLE, gt);
            intent.setExtrasClassLoader(GeofenceTable.class.getClassLoader());
            setResult(MapsActivity.RESULT_SETTINGS_DELETE, intent);
            finish();
        } else if(item.getItemId() == android.R.id.home) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*Intent intent = new Intent();
        intent.putExtra(MapsActivity.GEOFENCE_TABLE, gt);
        intent.setExtrasClassLoader(GeofenceTable.class.getClassLoader());
        setResult(MapsActivity.RESULT_SETTINGS_UPDATE, intent);
        finish();*/
        MapsActivity.updateDB(gt);
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final String ACTIVE_PREF = "active_pref";
        private final String RADIUS_PREF = "radius_pref";
        private final String ENTER_PREF = "enter_pref";
        private final String EXIT_PREF = "exit_pref";
        private final String DWELL_PREF = "dwell_pref";
        EditTextPreference etpRadius;

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
            }
        }
    }
}
