package com.example.geoapp.geoapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    ListView list;
    private GoogleMap mMap;
    private static final int DEFAULT_ZOOM = 15;
    FloatingActionButton addGeofenceButton;

    private int mId = 0;
    float mRadius;
    static final String DRAW_GEOFENCE_ACTION = "draw_geofence_action";
    static final String LAT_LNG = "lat_lng";
    static final String RADIUS = "radius";

    ArrayList<String> listLocationStr;
    ArrayList<Location> listLocation;
    ArrayAdapter<String> adapterListLocation;

    private static final String startUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";
    private static final String commaUrl = ",";
    private static final String endUrl = "&sensor=true";

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

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                // TODO Auto-generated method stub
                GeoInfoDialog gid = new GeoInfoDialog();
                FragmentManager fm = getFragmentManager();
                gid.show(fm, "Geofence info");
                return true;
            }
        });

        new GetAddress().execute(getUrl(MyGeofenceList.get(7).getLatLng()));


        addGeofenceButton = (FloatingActionButton)findViewById(R.id.add_geofence);
        addGeofenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, CurrentLocationActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    private String getUrl(LatLng latLng) {
        return startUrl + latLng.latitude + commaUrl + latLng.longitude + endUrl;
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if(requestCode == 1) {
            Location location = (Location) data.getParcelableExtra("location");
            if(location != null) {
                android.util.Log.d("Test_cl", "location" + location.toString());
                //listLocationStr.add(location.toString());
                //listLocation.add(location);
                //adapterListLocation.notifyDataSetChanged();
            }
        }

    }

    static public class GeoInfoDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Address: ------------\nTime enter: ----------\nTime Exit: ----------\n")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    });
                    /*.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });*/
            // Create the AlertDialog object and return it
            return builder.create();
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


    //////////////////////////////////////////
    private class GetAddress extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            /*pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();*/

        }

        @Override
        protected Void doInBackground(String... urls) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(urls[0]);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray contacts = jsonObj.getJSONArray("results");

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        String address = c.getString("formatted_address");
                        Log.d("Test_ltd", "address = " + address);

                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
       /*     if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
      /*      ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, contactList,
                    R.layout.list_item, new String[]{"name", "email",
                    "mobile"}, new int[]{R.id.name,
                    R.id.email, R.id.mobile});

            lv.setAdapter(adapter);*/
        }

    }
}
