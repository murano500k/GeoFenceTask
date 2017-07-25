package com.example.geoapp.geoapp;

import android.content.Intent;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {
    Button mapButton, clButton;
    ListView listView;
    ArrayList<String> listLocationStr;
    ArrayList<Location> listLocation;
    ArrayAdapter<String> adapterListLocation;
    private int mId = 0;
    float mRadius;
    static final String DRAW_GEOFENCE_ACTION = "draw_geofence_action";
    static final String LAT_LNG = "lat_lng";
    static final String RADIUS = "radius";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        mapButton = (Button) findViewById(R.id.menu_map_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
        clButton = (Button) findViewById(R.id.menu_current_location_button);
        clButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, CurrentLocationActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        mRadius = 150.f;
        listLocationStr = new ArrayList<String>();
        listLocation = new ArrayList<Location>();
        listView = (ListView) findViewById(R.id.lvMain);
        adapterListLocation = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, listLocationStr);
        listView.setAdapter(adapterListLocation);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //sendGeo(Geofence.GEOFENCE_TRANSITION_EXIT, position);
                Intent intent = new Intent(MenuActivity.this, CurrentLocationActivity.class);
                intent.setAction(DRAW_GEOFENCE_ACTION);
                LatLng latLng = new LatLng(listLocation.get(position).getLatitude(), listLocation.get(position).getLongitude());
                intent.putExtra(LAT_LNG, latLng);
                intent.putExtra(RADIUS, mRadius);
                startActivity(intent);
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if(requestCode == 1) {
            Location location = (Location) data.getParcelableExtra("location");
            if(location != null) {
                android.util.Log.d("Test_cl", "location" + location.toString());
                listLocationStr.add(location.toString());
                listLocation.add(location);
                adapterListLocation.notifyDataSetChanged();
            }
        }

    }

    private void sendGeo(int transitionType, int position) {
        AppCompatActivity activity = this;

        double latitude = listLocation.get(position).getLatitude();
        double longitude = listLocation.get(position).getLongitude();
        float radius = mRadius;


        MyGeofence myGeofence = new MyGeofence(mId, latitude, longitude, radius, transitionType);

        Intent geofencingService = new Intent(activity, GeofencingService.class);
        geofencingService.setAction(String.valueOf(Math.random()));
        geofencingService.putExtra(GeofencingService.EXTRA_ACTION, GeofencingService.Action.ADD);
        geofencingService.putExtra(GeofencingService.EXTRA_GEOFENCE, myGeofence);

        activity.startService(geofencingService);

        mId++;
    }
}
