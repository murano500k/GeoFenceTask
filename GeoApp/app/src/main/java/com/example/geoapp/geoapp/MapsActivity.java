package com.example.geoapp.geoapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.geoapp.geostorage.GeoDatabase;
import com.example.geoapp.geostorage.GeofenceDao;
import com.example.geoapp.geostorage.GeofenceTable;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import java.util.logging.LogRecord;

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
    static private CustomListAdapter customListAdapter;

    private static final String startUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";
    private static final String commaUrl = ",";
    private static final String endUrl = "&sensor=true";

    private ProgressDialog pDialog;
    private static int id = 0;
    GeoDatabase mDb;
    GeofenceDao mGeofenceDao;

    Handler hendlerInitListUI;
    Handler hendlerAddNewGeofence;


    private ArrayList<MyGeofence> MyGeofenceList;
            {
                MyGeofenceList = new ArrayList<MyGeofence>();
                MyGeofenceList.add(new MyGeofence(id++,  50.429557, 30.518141, 15, Geofence.GEOFENCE_TRANSITION_ENTER));
                MyGeofenceList.add(new MyGeofence(id++,  50.419827, 30.484082, 30, Geofence.GEOFENCE_TRANSITION_ENTER));
                MyGeofenceList.add(new MyGeofence(id++,  50.444993, 30.501386, 45, Geofence.GEOFENCE_TRANSITION_ENTER));
                MyGeofenceList.add(new MyGeofence(id++,  50.439664, 30.509769, 10, Geofence.GEOFENCE_TRANSITION_ENTER));
                MyGeofenceList.add(new MyGeofence(id++,  50.423350, 30.479781, 25, Geofence.GEOFENCE_TRANSITION_ENTER));
                MyGeofenceList.add(new MyGeofence(id++,  50.445824, 30.512964, 35, Geofence.GEOFENCE_TRANSITION_ENTER));
                MyGeofenceList.add(new MyGeofence(id++,  50.418313, 30.486337, 18, Geofence.GEOFENCE_TRANSITION_ENTER));
                MyGeofenceList.add(new MyGeofence(id++,  50.424849, 30.506372, 50, Geofence.GEOFENCE_TRANSITION_ENTER));
            }


    private ArrayList<GeofenceTable> listGeofenceTable;
    {
        listGeofenceTable = new ArrayList<GeofenceTable>();
    }



    ArrayList<String> itemname =new ArrayList<String>();
    /*{
        itemname.add("geofence");
        itemname.add("geofence");
        itemname.add("geofence");
        itemname.add("geofence");
        itemname.add("geofence");
        itemname.add("geofence");
        itemname.add("geofence");
        itemname.add("geofence");
    }*/


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


    void testInit() {
        listGeofenceTable.add(getGeofenceRow(id++,  50.429557, 30.518141, 15, Geofence.GEOFENCE_TRANSITION_ENTER, "Address 1"));
        listGeofenceTable.add(getGeofenceRow(id++,  50.419827, 30.484082, 30, Geofence.GEOFENCE_TRANSITION_ENTER, "Address 2"));
        listGeofenceTable.add(getGeofenceRow(id++,  50.444993, 30.501386, 45, Geofence.GEOFENCE_TRANSITION_ENTER, "Address 3"));
        listGeofenceTable.add(getGeofenceRow(id++,  50.439664, 30.509769, 10, Geofence.GEOFENCE_TRANSITION_ENTER, "Address 4"));
        listGeofenceTable.add(getGeofenceRow(id++,  50.423350, 30.479781, 25, Geofence.GEOFENCE_TRANSITION_ENTER, "Address 5"));
        listGeofenceTable.add(getGeofenceRow(id++,  50.445824, 30.512964, 35, Geofence.GEOFENCE_TRANSITION_ENTER, "Address 6"));
        listGeofenceTable.add(getGeofenceRow(id++,  50.418313, 30.486337, 18, Geofence.GEOFENCE_TRANSITION_ENTER, "Address 7"));
        listGeofenceTable.add(getGeofenceRow(id++,  50.424849, 30.506372, 50, Geofence.GEOFENCE_TRANSITION_ENTER, "Address 8"));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

       testInit();

        Thread threadInitDB = new Thread(new Runnable(){
            @Override
            public void run() {
                mDb = Room.databaseBuilder(getApplicationContext(),
                        GeoDatabase.class, "geo-database").build();
                mGeofenceDao = mDb.geofenceDao();


                GeofenceTable[] tempGeofenceTable = new GeofenceTable[mGeofenceDao.getAll().size()];
                tempGeofenceTable = mGeofenceDao.getAll().toArray(tempGeofenceTable);
                mGeofenceDao.deleteAll(tempGeofenceTable);

                //listGeofenceTable.addAll(mGeofenceDao.getAll());

                GeofenceTable []temp = new GeofenceTable[listGeofenceTable.size()];
                mGeofenceDao.insertAll(listGeofenceTable.toArray(temp));
                itemname.addAll(mGeofenceDao.loadAllAddresses());

                Log.d("Test_ltd", "hendlerInitListUI case 3 itemname size = " + itemname.size());
                hendlerInitListUI.sendEmptyMessage(0);
            }
        });
        threadInitDB.start();

        hendlerInitListUI = new Handler() {
            public void handleMessage(android.os.Message msg) {
                Log.d("Test_ltd", "hendlerInitListUI case 1");
                   if(msg.what == 0 && MapsActivity.customListAdapter != null) {
                       Log.d("Test_ltd", "hendlerInitListUI case 2");
                        MapsActivity.customListAdapter.notifyDataSetChanged();
                    }
            }
        };

        hendlerAddNewGeofence = new Handler() {
            public void handleMessage(android.os.Message msg) {
                Log.d("Test_ltd", "hendlerAddNewGeofence case 1");
                if(msg.what == 0 && MapsActivity.customListAdapter != null) {
                    Log.d("Test_ltd", "hendlerAddNewGeofence case 2");
                    MapsActivity.customListAdapter.notifyDataSetChanged();
                }
            }
        };

        customListAdapter = new CustomListAdapter(this, itemname, imgid);
        list = (ListView) findViewById(R.id.list_geofences);
        list.setAdapter(customListAdapter);

        //convertLatLngToAddress();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                float radius = listGeofenceTable.get(position).radius;
                LatLng geofenceLatLng = listGeofenceTable.get(position).getMyGeofence().getLatLng();
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

        addGeofenceButton = (FloatingActionButton)findViewById(R.id.add_geofence);
        addGeofenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, CurrentLocationActivity.class);
                startActivityForResult(intent, 1);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDb.close();
    }

    private String getUrl(LatLng latLng) {
        return startUrl + latLng.latitude + commaUrl + latLng.longitude + endUrl;
    }

    /*private void convertLatLngToAddress() {
        Log.d("Test_ltd", "case 2 customListAdapter == " + (customListAdapter != null));
        ArrayList<String> urlStr = new ArrayList<String>();

        for(MyGeofence g : MyGeofenceList) {
            urlStr.add(getUrl(g.getLatLng()));

        }
        String strs[] = new String[urlStr.size()];
        strs = urlStr.toArray(strs);
        new GetAddress().execute(strs);
    }*/

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
                final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                final float radius = 25;
//                Thread th = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
                        String address = getAddress(latLng);
 //                       synchronized (listGeofenceTable) {
                            listGeofenceTable.add(getGeofenceRow(id++,  latLng.latitude, latLng.longitude, radius,
                                    Geofence.GEOFENCE_TRANSITION_ENTER, address));
                            itemname.add(address);
                MapsActivity.customListAdapter.notifyDataSetChanged();
//                        }
//                        hendlerAddNewGeofence.sendEmptyMessage(0);
//                    }
//                });
//                th.start();
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
    protected class GetAddress extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            Log.d("Test_ltd", "onPreExecute");
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... urls) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            Integer urlsCount = urls.length;
            int k = 0;
            for(String url : urls) {

                String jsonStr = sh.makeServiceCall(url);

                Log.e(TAG, "Response from url: " + jsonStr);

                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);

                        // Getting JSON Array node
                        JSONArray contacts = jsonObj.getJSONArray("results");

                        // looping through All Contacts
                        //for (int i = 0; i < contacts.length(); i++) {
                            JSONObject c = contacts.getJSONObject(0);
                            String address = c.getString("formatted_address");
                            if(k < itemname.size()) {
                                itemname.set(k++, address);
                                Log.d("Test_ltd", "address = " + itemname.get(k - 1));
                            }
                        //}
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
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog

            if(MapsActivity.customListAdapter != null) {
                Log.d("Test_ltd", "onPostExecute");
                MapsActivity.customListAdapter.notifyDataSetChanged();
            }
            if (pDialog.isShowing()) {
                Log.d("Test_ltd", "onPostExecute case 1");
                pDialog.dismiss();
            }
            Log.d("Test_ltd", "onPostExecute case 2");

        }
    }

    GeofenceTable getGeofenceRow(int id, double latitude, double longitude, float radius, int transitionType, String address) {
        Log.d("Test_ltd", "getGeofenceRow case 1");
        GeofenceTable geofenceRow = new GeofenceTable();
        geofenceRow.latitude = latitude;
        geofenceRow.longitude = longitude;
        geofenceRow.radius = radius;
        geofenceRow.transitionType = transitionType;
        geofenceRow.address = address;
        return geofenceRow;
    }

    String getAddress(LatLng latLng) {
        String address = "";
        try {
            address = (new GetAddress()).execute(getUrl(latLng)).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return address;
    }
}
