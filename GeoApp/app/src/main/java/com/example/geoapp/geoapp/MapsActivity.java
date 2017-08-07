package com.example.geoapp.geoapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.geoapp.geostorage.GeoDatabase;
import com.example.geoapp.geostorage.GeofenceDao;
import com.example.geoapp.geostorage.GeofenceTable;
import com.example.geoapp.geostorage.GeofenceTimeTable;
import com.example.geoapp.utils.GeoUtils;
import com.google.android.gms.location.Geofence;
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
import java.util.Date;
import java.util.concurrent.ExecutionException;

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
    static private CustomListAdapter customListAdapterForAll;
    static private CustomListAdapter customListAdapterForActive;

    private static final String startUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";
    private static final String commaUrl = ",";
    private static final String endUrl = "&sensor=true";

    private ProgressDialog pDialog;
    private static int id = 0;
    static GeoDatabase mDb;
    static GeofenceDao mGeofenceDao;

    Handler hendlerInitListUI;
    Handler hendlerAddNewGeofence;
    private boolean showAllGeofenceInList = true;
    private boolean needUpdate = true;

    static MapsActivity instance;

    static public final int RESULT_SETTINGS_DELETE = 0;
    static public final int RESULT_SETTINGS_UPDATE = 1;
    static public final int RESULT_CREATE_NEW_GEOFENCE = 2;
    static public final String GEOFENCE_GEOMETRY = "geofence_geometry";
    static public final String GEOFENCE_TABLE = "geofence_table";
    static public final int UPDATE_LIST = 3;

    private ArrayList<GeofenceTable> mListGeofenceTableAll;
    private ArrayList<GeofenceTable> mListGeofenceTableOnlyActive;
    {
        mListGeofenceTableAll = new ArrayList<GeofenceTable>();
        mListGeofenceTableOnlyActive = new ArrayList<GeofenceTable>();
    }

    GeofenceTimeTable testGeoAddresesTable;

    {
        //testGeoAddresesTable = new GeofenceTimeTable();
        //java.util.Date temp = new java.util.Date();
        //testGeoAddresesTable.time = (temp.getTime());
    }


    void testInit() {
        mListGeofenceTableAll.add(getGeofenceRowTest(id++, 50.429557, 30.518141, 15, Geofence.GEOFENCE_TRANSITION_ENTER, null, true));
        mListGeofenceTableAll.add(getGeofenceRowTest(id++, 50.419827, 30.484082, 30, Geofence.GEOFENCE_TRANSITION_ENTER, null, true));
        mListGeofenceTableAll.add(getGeofenceRowTest(id++, 50.444993, 30.501386, 45, Geofence.GEOFENCE_TRANSITION_ENTER, null, true));
        mListGeofenceTableAll.add(getGeofenceRowTest(id++, 50.439664, 30.509769, 10, Geofence.GEOFENCE_TRANSITION_ENTER, null, false));
        mListGeofenceTableAll.add(getGeofenceRowTest(id++, 50.423350, 30.479781, 25, Geofence.GEOFENCE_TRANSITION_ENTER, null, false));
        mListGeofenceTableAll.add(getGeofenceRowTest(id++, 50.445824, 30.512964, 35, Geofence.GEOFENCE_TRANSITION_ENTER, null, false));
        mListGeofenceTableAll.add(getGeofenceRowTest(id++, 50.418313, 30.486337, 18, Geofence.GEOFENCE_TRANSITION_ENTER, null, true));
        mListGeofenceTableAll.add(getGeofenceRowTest(id++, 50.424849, 30.506372, 50, Geofence.GEOFENCE_TRANSITION_ENTER, null, false));
    }

    GeofenceTable getGeofenceRowTest(int id, double latitude, double longitude, float radius, int transitionType, String address, Boolean is_active) {
        GeofenceTable geofenceRow = new GeofenceTable();
        geofenceRow.latitude = latitude;
        geofenceRow.longitude = longitude;
        geofenceRow.radius = radius;
        geofenceRow.transitionType = transitionType;
        geofenceRow.address = address != null ? address : startUrl + latitude + commaUrl + longitude + endUrl;
        geofenceRow.isActive = is_active;
        return geofenceRow;
    }

    private void updateList() {
        if (showAllGeofenceInList) {
            customListAdapter = customListAdapterForAll;
        } else {
            customListAdapter = customListAdapterForActive;
        }
        list.setAdapter(customListAdapter);
        customListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        instance = this;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        testInit();

        Thread threadInitDB = new Thread(new Runnable() {
            @Override
            public void run() {
                mDb = Room.databaseBuilder(getApplicationContext(),
                        GeoDatabase.class, "geo-database").build();
                mGeofenceDao = mDb.geofenceDao();



                GeofenceTable[] tempGeofenceTable = new GeofenceTable[mGeofenceDao.getAll().size()];
                tempGeofenceTable = mGeofenceDao.getAll().toArray(tempGeofenceTable);
                for(GeofenceTable tempGt : tempGeofenceTable) {
                    GeofenceTimeTable[] gttTemp = new GeofenceTimeTable[mGeofenceDao.getTimeTableByGeofenceTable(tempGt.uid).size()];
                    gttTemp =  mGeofenceDao.getTimeTableByGeofenceTable(tempGt.uid).toArray(gttTemp);
                    mGeofenceDao.deleteAll(gttTemp);
                }
                mGeofenceDao.deleteAll(tempGeofenceTable);

                //mListGeofenceTableAll.addAll(mGeofenceDao.getAll());

                GeofenceTable[] temp = new GeofenceTable[mListGeofenceTableAll.size()];
                mGeofenceDao.insertAll(mListGeofenceTableAll.toArray(temp));
                mListGeofenceTableAll.clear();
                mListGeofenceTableAll.addAll(mGeofenceDao.getAll());
                mListGeofenceTableOnlyActive.addAll(mGeofenceDao.loadAllActive(true));


                ArrayList<GeofenceTimeTable> gtt = new     ArrayList<GeofenceTimeTable>();
                gtt.add(new GeofenceTimeTable(mListGeofenceTableOnlyActive.get(mListGeofenceTableOnlyActive.size()-1).uid,
                        (new Date()).getTime(),
                        mListGeofenceTableOnlyActive.get(mListGeofenceTableOnlyActive.size()-1).transitionType));
                gtt.add(new GeofenceTimeTable(mListGeofenceTableOnlyActive.get(mListGeofenceTableOnlyActive.size()-1).uid,
                        (new Date()).getTime(),
                        mListGeofenceTableOnlyActive.get(mListGeofenceTableOnlyActive.size()-1).transitionType));

                GeofenceTimeTable [] arrgtt = new GeofenceTimeTable[gtt.size()];
                mGeofenceDao.insertAll(gtt.toArray(arrgtt));

                int st = mGeofenceDao.getTimeTableByGeofenceTable(mListGeofenceTableOnlyActive.get(mListGeofenceTableOnlyActive.size()-1).uid).size();
                Log.d("Test_tt", "st = " + st);

                hendlerInitListUI.sendEmptyMessage(0);
            }
        });
        threadInitDB.start();

        hendlerInitListUI = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MapsActivity.UPDATE_LIST) {
                    updateList();
                }
            }
        };

        hendlerAddNewGeofence = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 1) {
                    String url = (String) msg.obj;
                    if (GeoUtils.isNetworkConnected(MapsActivity.this))
                        getAddress(url);
                    else {
                        needUpdate = true;
                    }
                    Thread th = new Thread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            }
        };

        customListAdapter = customListAdapterForAll = new CustomListAdapter(this, mListGeofenceTableAll);
        customListAdapterForActive = new CustomListAdapter(this, mListGeofenceTableOnlyActive);
        list = (ListView) findViewById(R.id.list_geofences);
        list.setAdapter(customListAdapter);

        //convertLatLngToAddress();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                float radius = mListGeofenceTableAll.get(position).radius;
                LatLng geofenceLatLng = mListGeofenceTableAll.get(position).getMyGeofence().getLatLng();
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
                Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
                Log.d("Test_gt", "mapsact set gt uid = " + (showAllGeofenceInList ? mListGeofenceTableAll : mListGeofenceTableOnlyActive).get(pos).uid);
                intent.putExtra(MapsActivity.GEOFENCE_TABLE, (showAllGeofenceInList ? mListGeofenceTableAll : mListGeofenceTableOnlyActive).get(pos));
                startActivityForResult(intent, MapsActivity.RESULT_SETTINGS_UPDATE);
                return true;
            }
        });

        addGeofenceButton = (FloatingActionButton) findViewById(R.id.add_geofence);
        addGeofenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, CurrentLocationActivity.class);
                startActivityForResult(intent, MapsActivity.RESULT_CREATE_NEW_GEOFENCE);
            }
        });

    }

    private String getUrl(LatLng latLng) {
        return startUrl + latLng.latitude + commaUrl + latLng.longitude + endUrl;
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void deleteGeofenceTable(final GeofenceTable... geofenceTable) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(GeofenceTable gt : geofenceTable) {
                    GeofenceTimeTable[] gttTemp = new GeofenceTimeTable[mGeofenceDao.getTimeTableByGeofenceTable(gt.uid).size()];
                    gttTemp =  mGeofenceDao.getTimeTableByGeofenceTable(gt.uid).toArray(gttTemp);
                    mGeofenceDao.deleteAll(gttTemp);
                }
                mGeofenceDao.deleteAll(geofenceTable);
            }
        }).start();
    }

    void updateGeofenceTableList() {
        mListGeofenceTableAll.clear();
        mListGeofenceTableAll.addAll(mGeofenceDao.getAll());
        mListGeofenceTableOnlyActive.clear();
        mListGeofenceTableOnlyActive.addAll(mGeofenceDao.loadAllActive(true));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Test_up", "test 1 --------*");
        if (data == null) {
            Log.d("Test_up", "test 1 --------**");
            return;
        }
        Log.d("Test_up", "test 1 --------");
        if (requestCode == MapsActivity.RESULT_CREATE_NEW_GEOFENCE) {
            if(resultCode == RESULT_OK){
                GeofenceGeometry location = (GeofenceGeometry) data.getParcelableExtra(MapsActivity.GEOFENCE_GEOMETRY);
                if (location != null) {
                    final LatLng latLng = location.getmLatLng();
                    final float radius = location.getmRadius();
                    final String urlJson = getUrl(latLng);
                    final int ttype =  location.getmTmTransitionType();
                    String address = getAddress(urlJson);
                    Thread th = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            GeofenceTable temp = getGeofenceRow(latLng.latitude, latLng.longitude, radius,
                                    ttype, urlJson);
                            mGeofenceDao.insertAll(temp);
                            String tempUrl = urlJson;
                            android.os.Message msg = hendlerAddNewGeofence.obtainMessage(1, tempUrl);
                            hendlerAddNewGeofence.sendMessage(msg);
                        }
                    });
                    th.start();
                }
            }
        } else if(requestCode == MapsActivity.RESULT_SETTINGS_UPDATE) {
            Log.d("Test_up", "test 1");
            if(resultCode == MapsActivity.RESULT_SETTINGS_DELETE) {
                Log.d("Test_up", "test 2");
                final GeofenceTable gt = (GeofenceTable)data.getParcelableExtra(MapsActivity.GEOFENCE_TABLE);
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        deleteGeofenceTable(mGeofenceDao.findByUid((new Integer(gt.uid)).toString()));
                        updateGeofenceTableList();
                        hendlerInitListUI.sendEmptyMessage(MapsActivity.UPDATE_LIST);
                    }
                });
                th.start();
            } else if(resultCode == MapsActivity.RESULT_SETTINGS_UPDATE) {
                Log.d("Test_up", "test 3");
            }
        }

//TODO: for debug
   /*     Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateAddresses();
            }
        });
        thread.start();*/
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
                    }).setNeutralButton("SET", null);
                    /*.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });*/
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    private void sendGeo(int id, int transitionType, LatLng position, float radius) {
        AppCompatActivity activity = this;
        MyGeofence myGeofence = new MyGeofence(id, position.latitude, position.longitude, radius, transitionType);
        Intent geofencingService = new Intent(activity, GeofencingService.class);
        geofencingService.setAction(String.valueOf(Math.random()));
        geofencingService.putExtra(GeofencingService.EXTRA_ACTION, GeofencingService.Action.ADD);
        geofencingService.putExtra(GeofencingService.EXTRA_GEOFENCE, myGeofence);

        activity.startService(geofencingService);
    }

    private void sendGeo(MyGeofence myGeofence) {
        AppCompatActivity activity = this;
        Intent geofencingService = new Intent(activity, GeofencingService.class);
        geofencingService.setAction(String.valueOf(Math.random()));
        geofencingService.putExtra(GeofencingService.EXTRA_ACTION, GeofencingService.Action.ADD);
        geofencingService.putExtra(GeofencingService.EXTRA_GEOFENCE, myGeofence);

        activity.startService(geofencingService);
    }

    protected class GetAddress extends AsyncTask<String, Void, String> {

        GetAddress(String str) {

        }

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
            GeofenceTable geofenceTable = null;
            for (String url : urls) {
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
                        geofenceTable = mGeofenceDao.findByAddress(url);

                        if (geofenceTable != null) {
                            geofenceTable.address = address;
                            mGeofenceDao.updateGeofenceRow(geofenceTable);
                            mListGeofenceTableAll.add(geofenceTable);
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
            }

//            if(geofenceTable != null && geofenceTable.isActive)
//                sendGeo(geofenceTable.getMyGeofence());

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (MapsActivity.customListAdapter != null) {
                MapsActivity.customListAdapter.notifyDataSetChanged();
            }
            // Dismiss the progress dialog
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }

    GeofenceTable getGeofenceRow(double latitude, double longitude, float radius, int transitionType, String address) {
        GeofenceTable geofenceRow = new GeofenceTable();
        geofenceRow.latitude = latitude;
        geofenceRow.longitude = longitude;
        geofenceRow.radius = radius;
        geofenceRow.transitionType = transitionType;
        geofenceRow.address = address;// != null ? address : getAddress(new LatLng(latitude, longitude));
        return geofenceRow;
    }

    String getAddress(String urlJson) {
        String address = "";
        try {
            address = (new GetAddress(null)).execute(urlJson/*getUrl(latLng)*/).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return address;
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
        if (mDb != null) {
            mDb.close();
        }
    }

    protected class UpdateGeoArraysFromDB extends AsyncTask<Boolean, Void, Void> {

        @Override
        protected Void doInBackground(Boolean... params) {
            if (params[0]) {
                mListGeofenceTableAll.clear();
                mListGeofenceTableAll.addAll(mGeofenceDao.getAll());
            } else {
                mListGeofenceTableOnlyActive.clear();
                mListGeofenceTableOnlyActive.addAll(mGeofenceDao.loadAllActive(true));
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateList();
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }


    /**
     * Sets up the options menu.
     *
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maps_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        (showAllGeofenceInList ? menu.findItem(R.id.menu_all) : menu.findItem(R.id.menu_active)).setChecked(true);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Handles a click on the menu option to get a place.
     *
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_gps_settings) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menu_all) {
            item.setChecked(true);
            (new UpdateGeoArraysFromDB()).execute((showAllGeofenceInList = true));
        } else if (item.getItemId() == R.id.menu_active) {
            item.setChecked(true);
            (new UpdateGeoArraysFromDB()).execute((showAllGeofenceInList = false));
        }
        return true;
    }

    boolean getShowAllInList() {
        return showAllGeofenceInList;
    }

    public static class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (GeoUtils.isNetworkConnected(context)) {

            } else {
            }
        }
    }

    void updateAddresses() {
        ArrayList<GeofenceTable> table = new ArrayList<GeofenceTable>();
        table.addAll(mGeofenceDao.findByUrlsInAddresses(startUrl+'%'));
        String[] str = new String[table.size()];
        int i = 0;
        for(GeofenceTable gt : table) {
            str[i] = gt.address;
        }
        //(new GetAddress(null)).execute(str);
    }

    static public void updateDB (final GeofenceTable... gt) {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                mGeofenceDao.updateGeofenceTable(gt);
            }
        })).start();
    }

}
