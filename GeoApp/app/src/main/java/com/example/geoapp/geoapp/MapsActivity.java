package com.example.geoapp.geoapp;

import android.app.ProgressDialog;
import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geoapp.geostorage.GeoDatabase;
import com.example.geoapp.geostorage.GeoDatabaseManager;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends LifecycleActivity implements OnMapReadyCallback  {

    private static final String TAG = MapsActivity.class.getSimpleName();
    //ListView list;
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
    static private GeoTableAdapter customListAdapter;
    static private GeoTableAdapter customListAdapterForAll;
    static private GeoTableAdapter customListAdapterForActive;
    RecyclerView list;
    GeoTableAdapter geoTableAdapter;

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



    private GeoDatabaseManager mGeoDatabaseManager = null;


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


        mGeoDatabaseManager = ViewModelProviders.of(this).get(GeoDatabaseManager.class);
        Thread threadInitDB = new Thread(new Runnable() {
            @Override
            public void run() {
               /* mGeoDatabaseManager = GeoDatabaseManager.getInstanse(getApplication());
                mGeofenceDao = mGeoDatabaseManager.getGeofenceDao();
                mListGeofenceTableAll.addAll(mGeoDatabaseManager.mListGeofenceTableAll);
                mListGeofenceTableOnlyActive.addAll(mGeoDatabaseManager.mListGeofenceTableOnlyActive);
                hendlerInitListUI.sendEmptyMessage(0);*/
                mGeoDatabaseManager.testInit();
            }
        });
        threadInitDB.start();

        hendlerInitListUI = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MapsActivity.UPDATE_LIST) {
                    //updateList();
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



        //customListAdapter = customListAdapterForAll = new GeoTableAdapter(this, mListGeofenceTableAll);
        //customListAdapterForActive = new GeoTableAdapter(this, mListGeofenceTableOnlyActive);
        ///////////////////////////////////////////////////////////////////////////
        list = (RecyclerView) findViewById(R.id.recycler_view_geotable);
        list.setLayoutManager(new LinearLayoutManager(this));
        //list.setAdapter(customListAdapter);
        //geoTableAdapter = new GeoTableAdapter(MapsActivity.this, mListGeofenceTableAll);
        //list.setAdapter(geoTableAdapter);
        geoTableAdapter = new GeoTableAdapter(this, new ArrayList<GeofenceTable>());
        list.setAdapter(geoTableAdapter);
        mGeoDatabaseManager.getListGeofenceTable(showAllGeofenceInList).observe(MapsActivity.this,
                new Observer<List<GeofenceTable>>() {

                    @Override
                    public void onChanged(@Nullable List<GeofenceTable> geofenceTables) {
                        geoTableAdapter.addItems(geofenceTables);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if (requestCode == MapsActivity.RESULT_CREATE_NEW_GEOFENCE) {
            if(resultCode == RESULT_OK){
                GeofenceGeometry location = (GeofenceGeometry) data.getParcelableExtra(MapsActivity.GEOFENCE_GEOMETRY);
                if (location != null) {
                    final LatLng latLng = location.getmLatLng();
                    final float radius = location.getmRadius();
                    final String urlJson = getUrl(latLng);
                    final int ttype =  location.getmTmTransitionType();
                    /*String address = getAddress(urlJson);
                    mGeoDatabaseManager.insert(getGeofenceRow(latLng.latitude, latLng.longitude, radius,
                            ttype, urlJson));*/
                      Thread th = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String address = getAddressTest(urlJson);
                            mGeoDatabaseManager.insert(getGeofenceRow(latLng.latitude, latLng.longitude, radius,
                                    ttype, address));
                        }
                    });
                    th.start();
                  /*  Thread th = new Thread(new Runnable() {
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
                    th.start();*/
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
                        mListGeofenceTableAll.clear();
                        //mListGeofenceTableAll.addAll(GeoDatabaseManager.getGeofenceTables(true));
                        mListGeofenceTableAll.addAll(mGeofenceDao.getAll());
                        mListGeofenceTableOnlyActive.clear();
                        //mListGeofenceTableOnlyActive.addAll(GeoDatabaseManager.getGeofenceTables(false));
                        mListGeofenceTableOnlyActive.addAll(mGeofenceDao.getAllActive(true));
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

    private void sendGeo(MyGeofence myGeofence) {
        LifecycleActivity activity = this;
        Intent geofencingService = new Intent(activity, GeofencingService.class);
        geofencingService.setAction(String.valueOf(Math.random()));
        geofencingService.putExtra(GeofencingService.EXTRA_ACTION, GeofencingService.Action.ADD);
        geofencingService.putExtra(GeofencingService.EXTRA_GEOFENCE, myGeofence);

        activity.startService(geofencingService);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            /*if (MapsActivity.customListAdapter != null) {
                updateList();
            }*/

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }

    String getAddressTest(String... urls) {
        HttpHandler sh = new HttpHandler();
        String address = null;
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
                    address = c.getString("formatted_address");

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

        return address;
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
        mGeoDatabaseManager.close();
    }

    protected class UpdateGeoArraysFromDB extends AsyncTask<Boolean, Void, Void> {

        @Override
        protected Void doInBackground(Boolean... params) {
            if (params[0]) {
                mListGeofenceTableAll.clear();
                mListGeofenceTableAll.addAll(mGeofenceDao.getAll());
            } else {
                mListGeofenceTableOnlyActive.clear();
                mListGeofenceTableOnlyActive.addAll(mGeofenceDao.getAllActive(true));
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
            //updateList();
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
            //(new UpdateGeoArraysFromDB()).execute((showAllGeofenceInList = true));
        } else if (item.getItemId() == R.id.menu_active) {
            item.setChecked(true);
            //(new UpdateGeoArraysFromDB()).execute((showAllGeofenceInList = false));
        }
        return true;
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
                Log.d("Test_upd", "updateDB");
                mGeofenceDao.updateGeofenceTable(gt);
            }
        })).start();
    }


    public class GeoTableAdapter extends RecyclerView.Adapter<GeoTableAdapter.ItemViewHolder> {

        private List<GeofenceTable> geoTable;
        private Context context;

        public GeoTableAdapter(Context context, List<GeofenceTable> geoTable) {
            this.context = context;
            this.geoTable = geoTable;
        }


        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.geotable_item_recycleview, parent, false);
            return new ItemViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            if(geoTable == null)
                return;
            GeofenceTable gt = geoTable.get(position);
            holder.address.setText(gt.address);
            StringBuilder sb = new StringBuilder("Description: ");
            if((gt.transitionType & Geofence.GEOFENCE_TRANSITION_ENTER) != 0)
                sb.append(" Enter");
            if((gt.transitionType & Geofence.GEOFENCE_TRANSITION_EXIT) != 0)
                sb.append(", Exit");
            if((gt.transitionType & Geofence.GEOFENCE_TRANSITION_DWELL) != 0)
                sb.append(", Dwell");
            sb.append(", r = " + gt.radius);
            holder.info.setText(sb);
            holder.icon.setImageResource(gt.getBitmapId());
            holder.pos = position;
        }

        @Override
        public int getItemCount() {
            return (null != geoTable ? geoTable.size() : 0);
        }

        public void addItems(List<GeofenceTable> geofenceTable) {
            this.geoTable = geofenceTable;
            notifyDataSetChanged();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
            private TextView address;
            private TextView info;
            private ImageView icon;
            private int pos;

            public ItemViewHolder(View itemView) {
                super(itemView);
                address = (TextView) itemView.findViewById(R.id.geo_address);
                info = (TextView) itemView.findViewById(R.id.geo_info_tv);
                icon = (ImageView) itemView.findViewById(R.id.icon_active);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View v) {
                float radius = (showAllGeofenceInList ? mListGeofenceTableAll : mListGeofenceTableOnlyActive).get(pos).radius;
                LatLng geofenceLatLng = (showAllGeofenceInList ? mListGeofenceTableAll : mListGeofenceTableOnlyActive).
                        get(pos).getMyGeofence().getLatLng();
                int opaqueRed = Color.argb(125, 0, 200, 0);
                mMap.addCircle(new CircleOptions().center(geofenceLatLng)
                        .radius(radius).fillColor(opaqueRed).strokeColor(Color.BLUE).strokeWidth(2));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geofenceLatLng, DEFAULT_ZOOM));
                mMap.addMarker(new MarkerOptions().position(geofenceLatLng));
            }

            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
                intent.putExtra(MapsActivity.GEOFENCE_TABLE, (showAllGeofenceInList ? mListGeofenceTableAll : mListGeofenceTableOnlyActive).get(pos));
                startActivityForResult(intent, MapsActivity.RESULT_SETTINGS_UPDATE);
                return false;
            }
        }

    }

}
