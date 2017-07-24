package com.example.geoapp.geoapp;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    ListView list;
    private GoogleMap mMap;
    private static final int DEFAULT_ZOOM = 15;

    private ArrayList<MyGeofence> MyGeofenceList;
            {
                MyGeofenceList = new ArrayList<MyGeofence>();
                MyGeofenceList.add(new MyGeofence(0,  50.429557, 30.518141, 15, Geofence.GEOFENCE_TRANSITION_ENTER));
                MyGeofenceList.add(new MyGeofence(1,  50.419827, 30.484082, 30, Geofence.GEOFENCE_TRANSITION_ENTER));
                MyGeofenceList.add(new MyGeofence(2,  50.444993, 30.501386, 45, Geofence.GEOFENCE_TRANSITION_ENTER));
                MyGeofenceList.add(new MyGeofence(3,  50.439664, 30.509769, 10, Geofence.GEOFENCE_TRANSITION_ENTER));
                MyGeofenceList.add(new MyGeofence(4,  50.423350, 30.479781, 25, Geofence.GEOFENCE_TRANSITION_ENTER));
                MyGeofenceList.add(new MyGeofence(5,  50.445824, 30.512964, 35, Geofence.GEOFENCE_TRANSITION_ENTER));
                MyGeofenceList.add(new MyGeofence(6,  50.418313, 30.486337, 18, Geofence.GEOFENCE_TRANSITION_ENTER));
                MyGeofenceList.add(new MyGeofence(7,  50.424849, 30.506372, 50, Geofence.GEOFENCE_TRANSITION_ENTER));
            }



    String[] itemname ={
            "Geofance 1",
            "Geofance 2",
            "Geofance 3",
            "Geofance 4",
            "Geofance 5",
            "Geofance 6",
            "Geofance 7",
            "Geofance 8"
    };

    Integer[] imgid={
            R.drawable.google_maps_icon,
            R.drawable.google_maps_icon,
            R.drawable.google_maps_icon,
            R.drawable.google_maps_icon,
            R.drawable.google_maps_icon,
            R.drawable.google_maps_icon,
            R.drawable.google_maps_icon,
            R.drawable.google_maps_icon,
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        CustomListAdapter adapter=new CustomListAdapter(this, itemname, imgid);
        list=(ListView)findViewById(R.id.list_geofences);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                float radius = MyGeofenceList.get(position).getRadius();
                LatLng geofenceLatLng = MyGeofenceList.get(position).getLatLng();
                int opaqueRed = Color.argb(125, 0, 200, 0);
                mMap.addCircle(new CircleOptions().center(geofenceLatLng)
                        .radius(radius).fillColor(opaqueRed).strokeColor(Color.BLUE).strokeWidth(2));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geofenceLatLng, DEFAULT_ZOOM));
                mMap.addMarker(new MarkerOptions().position(geofenceLatLng));
                
            }
        });
    }

    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        mMap = googleMap;
       /* LatLng sydney = new LatLng(-33.852, 151.211);
        mMap.addMarker(new MarkerOptions().position(sydney)
                .title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }
}
