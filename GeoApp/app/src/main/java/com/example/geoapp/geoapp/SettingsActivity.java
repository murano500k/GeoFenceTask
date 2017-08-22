package com.example.geoapp.geoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.geoapp.geostorage.GeofenceTable;

public class SettingsActivity extends AppCompatActivity {

    private static GeofenceTable gt = null;
    public static final String GET_ID_FROM_INTENT = "time_id";
    public static final String CHANGE_ACTIVE = "change_active";
    private boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        gt = intent.getParcelableExtra(MapsActivity.GEOFENCE_TABLE);
        active = gt.isActive;
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.initGeofenceTable(gt);
        getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
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
            Intent intent = new Intent();
            intent.putExtra(MapsActivity.GEOFENCE_TABLE, gt);
            intent.setExtrasClassLoader(GeofenceTable.class.getClassLoader());
            if(active == gt.isActive)
                intent.putExtra(CHANGE_ACTIVE, false);
            else
                intent.putExtra(CHANGE_ACTIVE, true);
            setResult(MapsActivity.RESULT_SETTINGS_UPDATE, intent);
            finish();
        } else if(item.getItemId() == R.id.item_time_table) {
            Intent intent = new Intent(SettingsActivity.this, GeoTimeTableActivity.class);
            intent.putExtra(GET_ID_FROM_INTENT, gt.uid);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
