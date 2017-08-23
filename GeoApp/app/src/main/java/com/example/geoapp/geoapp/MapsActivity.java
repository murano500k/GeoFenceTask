package com.example.geoapp.geoapp;

import android.app.ActivityOptions;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geoapp.geofence.GeofenceGeometry;
import com.example.geoapp.geofence.GeofencingService;
import com.example.geoapp.geofence.GeofenceEntity;
import com.example.geoapp.geostorage.GeoDatabaseManager;
import com.example.geoapp.geostorage.GeofenceTable;
import com.example.geoapp.geostorage.GeofenceTimeTable;
import com.example.geoapp.utils.GeoUtils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements LifecycleRegistryOwner, OnMapReadyCallback  {

    private static final String TAG = MapsActivity.class.getSimpleName();
    public static final String START_URL = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";
    public static final String COMMA_URL = ",";
    public static final String END_URL = "&sensor=true";
    public static final String GEOFENCE_GEOMETRY = "geofence_geometry";
    public static final String GEOFENCE_TABLE = "geofence_table";
    public static final int RESULT_SETTINGS_DELETE = 0;
    public static final int RESULT_SETTINGS_UPDATE = 1;
    public static final int RESULT_CREATE_NEW_GEOFENCE = 2;
    public static final int UPDATE_LIST = 3;
    public static final int SKIP_MAP = 4;
    private static final int DEFAULT_ZOOM = 15;
    private static final int SEND_GEOFENCE_ADD = 5;
    private static final int SEND_GEOFENCE_REMOVE = 6;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private GoogleMap mMap;
    private FloatingActionButton addGeofenceButton;
    private RecyclerView list;
    private GeoTableAdapter geoTableAdapter;
    private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);
    private ProgressDialog pDialog;
    private boolean showAllGeofenceInList = true;
    private boolean needUpdate = true;
    static MapsActivity instance;
    private GeoDatabaseManager mGeoDatabaseManager = null;
    private Handler hanhlerSendGeofence;
    private boolean mLocationPermissionGranted = false;
    private Location mLastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        instance = this;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGeoDatabaseManager = GeoDatabaseManager.getInstanse(this);

        //TODO: test_init
       /* Thread threadInitDB = new Thread(new Runnable() {
            @Override
            public void run() {
                //mGeoDatabaseManager.testInit();
                //mGeoDatabaseManager.cleanDB();
            }
        });
        threadInitDB.start();*/

        hanhlerSendGeofence = new Handler(Looper.myLooper()){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MapsActivity.SEND_GEOFENCE_ADD) {
                    Long id = (Long)msg.obj;
                    Long []param = new Long[2];
                    param[0] = id;
                    param[1] = 0l;
                    new SendGeofence().execute(param);
                } else if(msg.what == MapsActivity.SEND_GEOFENCE_REMOVE) {
                    Long id = (Long)msg.obj;
                    Long []param = new Long[2];
                    param[0] = id;
                    param[1] = 1l;
                    new SendGeofence().execute(param);
                }
                super.handleMessage(msg);
            }
        };

        list = (RecyclerView) findViewById(R.id.recycler_view_geotable);
        list.setLayoutManager(new LinearLayoutManager(this));
        geoTableAdapter = new GeoTableAdapter(this, new ArrayList<GeofenceTable>());
        list.setAdapter(geoTableAdapter);
        setListAdapter();

        addGeofenceButton = (FloatingActionButton) findViewById(R.id.add_geofence);
        addGeofenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!GeoUtils.isNetworkConnected(MapsActivity.this)) {
                    FragmentManager fm = getFragmentManager();
                    ConnectionDialogFragment dialogFragment = new ConnectionDialogFragment ();
                    dialogFragment.show(fm, "Dialog Connection");
                } else {
                    Intent intent = new Intent(MapsActivity.this, CurrentLocationActivity.class);
                    ActivityOptions ao = ActivityOptions.makeCustomAnimation(MapsActivity.this, R.anim.fadein, R.anim.fadeout);
                    //startActivityForResult(intent, MapsActivity.RESULT_SETTINGS_UPDATE, ao.toBundle());
                    overridePendingTransition(R.anim.fadeout, R.anim.fadein);
                    startActivityForResult(intent, MapsActivity.RESULT_CREATE_NEW_GEOFENCE, ao.toBundle());
                }

            }
        });

        registerReceiver(mUpdateReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        GeofencingService.Action action_init = GeofencingService.Action.START_INIT;
        Intent geofencingService = new Intent(this, GeofencingService.class);
        geofencingService.putExtra(GeofencingService.EXTRA_ACTION, action_init);
        this.startService(geofencingService);
    }

    @Override
    public void overridePendingTransition(int enterAnim, int exitAnim) {
        super.overridePendingTransition(enterAnim, exitAnim);
    }

    private String getUrl(LatLng latLng) {
        return START_URL + latLng.latitude + COMMA_URL + latLng.longitude + END_URL;
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(48.431226, 22.157590)).include(new LatLng(49.547834, 40.196753));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 100);
        mMap.moveCamera(cameraUpdate);
        updateLocationUI();
    }

    private void deleteGeofenceTable(final GeofenceTable... geofenceTable) {
        for(GeofenceTable gt : geofenceTable) {
            GeofenceTimeTable[] gttTemp = new GeofenceTimeTable[mGeoDatabaseManager.getDao().getTimeTableByGeofenceTable(gt.uid).size()];
            gttTemp =  mGeoDatabaseManager.getDao().getTimeTableByGeofenceTable(gt.uid).toArray(gttTemp);
            mGeoDatabaseManager.deleteGeoTime(gttTemp);
        }
        mGeoDatabaseManager.deleteGeoTable(geofenceTable);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null || resultCode == MapsActivity.SKIP_MAP) {
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
                    final boolean active = location.ismActive();
                    Thread th = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String address = getAddress(urlJson);
                            long id = mGeoDatabaseManager.insert(getGeofenceRow(latLng.latitude, latLng.longitude, radius,
                                    ttype, address, active));
                            if(active) {
                                Message msg = hanhlerSendGeofence.obtainMessage(MapsActivity.SEND_GEOFENCE_ADD, id);
                                hanhlerSendGeofence.sendMessage(msg);
                            }
                        }
                    });
                    th.start();
                }
            }
        } else if(requestCode == MapsActivity.RESULT_SETTINGS_UPDATE) {
            if(resultCode == MapsActivity.RESULT_SETTINGS_DELETE) {
                final GeofenceTable gt = data.getParcelableExtra(MapsActivity.GEOFENCE_TABLE);
                if(gt.isActive) {
                    Long id = new Long(gt.uid);
                    Long []param = new Long[2];
                    param[0] = id;
                    param[1] = 1l;
                    new SendGeofence().execute(param);
                }
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        deleteGeofenceTable(gt);
                    }
                });
                th.start();
            } else if(resultCode == MapsActivity.RESULT_SETTINGS_UPDATE) {
                final GeofenceTable gt = data.getParcelableExtra(MapsActivity.GEOFENCE_TABLE);
                final boolean changeActive = data.getBooleanExtra(SettingsActivity.CHANGE_ACTIVE, false);
                if(changeActive) {
                    int action = gt.isActive ? MapsActivity.SEND_GEOFENCE_ADD : MapsActivity.SEND_GEOFENCE_REMOVE;
                    Message msg = hanhlerSendGeofence.obtainMessage(action, new Long(gt.uid));
                    hanhlerSendGeofence.sendMessage(msg);
                }
                mGeoDatabaseManager.updateGeoTable(gt);
                setListAdapter();
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

    private void setListAdapter() {
        mGeoDatabaseManager.getListGeofenceTable(showAllGeofenceInList).observe(MapsActivity.this,
                new Observer<List<GeofenceTable>>() {

                    @Override
                    public void onChanged(@Nullable List<GeofenceTable> geofenceTables) {
                        geoTableAdapter.addItems(geofenceTables);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return mRegistry;
    }

    String getAddress(String url) {
        if(!GeoUtils.isNetworkConnected(getApplicationContext())) {
            GeoDatabaseManager.setNeedUpdate(true);
            return url;
        }

        HttpHandler sh = new HttpHandler();
        String address = null;
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
        return address;
    }

    GeofenceTable getGeofenceRow(double latitude, double longitude, float radius, int transitionType, String address, boolean active) {
        GeofenceTable geofenceRow = new GeofenceTable();
        geofenceRow.latitude = latitude;
        geofenceRow.longitude = longitude;
        geofenceRow.radius = radius;
        geofenceRow.transitionType = transitionType;
        geofenceRow.address = address;// != null ? address : getAddress(new LatLng(latitude, longitude));
        geofenceRow.isActive = active;
        return geofenceRow;
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
            showAllGeofenceInList = true;
            setListAdapter();
        } else if (item.getItemId() == R.id.menu_active) {
            item.setChecked(true);
            showAllGeofenceInList = false;
            setListAdapter();
        } else if (item.getItemId() == R.id.clear_map) {
            mMap.clear();
        }
        return true;
    }

    private void sendGeo(GeofenceEntity myGeofence, GeofencingService.Action action) {
        AppCompatActivity activity = this;
        Intent geofencingService = new Intent(activity, GeofencingService.class);
        geofencingService.putExtra(GeofencingService.EXTRA_ACTION, action);
        geofencingService.putExtra(GeofencingService.EXTRA_GEOFENCE, myGeofence);
        activity.startService(geofencingService);
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
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
                float radius = geoTable.get(getPosition()).radius;
                LatLng geofenceLatLng = geoTable.get(getPosition()).getGeofenceEntity().getLatLng();
                int opaqueRed = Color.argb(125, 0, 200, 0);
                mMap.addCircle(new CircleOptions().center(geofenceLatLng)
                        .radius(radius).fillColor(opaqueRed).strokeColor(Color.BLUE).strokeWidth(2));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geofenceLatLng, DEFAULT_ZOOM));
                mMap.addMarker(new MarkerOptions().position(geofenceLatLng));
            }

            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
                intent.putExtra(MapsActivity.GEOFENCE_TABLE, (geoTable.get(getPosition())));

                //MapsActivity.this.overridePendingTransition(R.anim.fadeout, R.anim.fadein);
                ActivityOptions ao = ActivityOptions.makeCustomAnimation(MapsActivity.this, R.anim.fadein,  R.anim.fadeout);
                startActivityForResult(intent, MapsActivity.RESULT_SETTINGS_UPDATE, ao.toBundle());
                return false;
            }
        }
    }

    private class SendGeofence extends AsyncTask<Long, Void, Void> {

        @Override
        protected Void doInBackground(Long... params) {
            GeofenceTable gt = mGeoDatabaseManager.getDao().loadByIds(params[0]);
            GeofencingService.Action action = params[1] == 0 ? GeofencingService.Action.ADD : GeofencingService.Action.REMOVE;
            sendGeo(gt.getGeofenceEntity(), action);
            return null;
        }
    }

    @SuppressWarnings("unused")
    protected class GetAddress extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... urls) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            GeofenceTable geofenceTable = null;
            for (String url : urls) {
                String jsonStr = sh.makeServiceCall(url);
                //Log.e(TAG, "Response from url: " + jsonStr);
                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);

                        // Getting JSON Array node
                        JSONArray contacts = jsonObj.getJSONArray("results");

                        // looping through All Contacts
                        //for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(0);
                        String address = c.getString("formatted_address");
                        geofenceTable = mGeoDatabaseManager.getDao().findByAddress(url);

                        if (geofenceTable != null) {
                            geofenceTable.address = address;
                            mGeoDatabaseManager.getDao().updateGeofenceRow(geofenceTable);
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

    public static class ConnectionDialogFragment extends DialogFragment implements View.OnClickListener{

        Button cancButton;
        Button enableWifiButton;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.dialog_fragment_connection, container, false);
            getDialog().setTitle("Need internet connection");
            cancButton = (Button)rootView.findViewById(R.id.dismiss_dialog_fragment);
            enableWifiButton = (Button)rootView.findViewById(R.id.wifi_connection);
            cancButton.setOnClickListener(this);
            enableWifiButton.setOnClickListener(this);
            getActivity().registerReceiver(mDismissReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            return rootView;
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.dismiss_dialog_fragment) {
                ConnectionDialogFragment.this.dismiss();
            } else if(v.getId() == R.id.wifi_connection) {
                ConnectionDialogFragment.this.dismiss();
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            getActivity().unregisterReceiver(mDismissReceiver);
        }

        BroadcastReceiver mDismissReceiver = new BroadcastReceiver () {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (GeoUtils.isNetworkConnected(context) && ConnectionDialogFragment.this.isVisible()) {
                    ConnectionDialogFragment.this.dismiss();
                }
            }
        };
    }

    BroadcastReceiver mUpdateReceiver = new BroadcastReceiver () {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (GeoUtils.isNetworkConnected(context) && GeoDatabaseManager.getNeedUpdate()) {
                if(mGeoDatabaseManager != null)
                    mGeoDatabaseManager.updateAddreses();
            }
        }
    };
}
