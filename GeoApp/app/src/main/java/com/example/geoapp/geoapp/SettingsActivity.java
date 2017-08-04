package com.example.geoapp.geoapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.geoapp.geostorage.GeofenceTable;
import com.google.android.gms.location.Geofence;

public class SettingsActivity extends AppCompatActivity {
    EditText radius;
    GeofenceTable gt;
    ToggleButton active;
    CheckBox cbEnter, cbExit, cbDwell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.menu_settings_geo);
        Intent intent = getIntent();
        gt = (GeofenceTable)intent.getParcelableExtra(MapsActivity.GEOFENCE_TABLE);
        ((TextView)findViewById(R.id.text_lat_set_var)).setText((new Double(gt.latitude)).toString());
        ((TextView)findViewById(R.id.text_long_set_var)).setText((new Double(gt.longitude)).toString());
        (radius = (EditText)findViewById(R.id.edit_text_radius_set)).setText((new Float(gt.radius)).toString());
        (active = (ToggleButton)findViewById(R.id.active_geofence_toggle)).setChecked(gt.isActive);
        (cbEnter = (CheckBox)findViewById(R.id.cb_transition_enter)).setChecked((gt.transitionType & Geofence.GEOFENCE_TRANSITION_ENTER) != 0);
        (cbExit = (CheckBox)findViewById(R.id.cb_transition_exit)).setChecked((gt.transitionType & Geofence.GEOFENCE_TRANSITION_EXIT) != 0);
        (cbDwell = (CheckBox)findViewById(R.id.cb_transition_dwell)).setChecked((gt.transitionType & Geofence.GEOFENCE_TRANSITION_DWELL) != 0);

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
        }
        return super.onOptionsItemSelected(item);
    }
}
