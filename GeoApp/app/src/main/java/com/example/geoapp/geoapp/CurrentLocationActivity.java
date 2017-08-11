package com.example.geoapp.geoapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geoapp.geofence.GeofenceGeometry;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;

public class CurrentLocationActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnPoiClickListener, GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener {

    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private Location mLastKnownLocation;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private CameraPosition mCameraPosition;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final int DEFAULT_ZOOM = 15;

    private GoogleApiClient mGoogleApiClient;

    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private ArrayList<LatLng> latLngList;

    private FloatingActionButton floatingActionButton;
    private float mRadius = 0.f;
    private LatLng geofenceLatLng;
    private boolean drawGeofence = false;

    private SeekBar mSeekBarRadius;
    private Circle circleDrawSeekBar = null;
    private LatLng chosenPoint = null;
    private int startRadius = 15;
    private boolean active = true;
    private final int fillCircleColor = Color.argb(125, 0, 200, 0);

    private int mTransitionType = Geofence.GEOFENCE_TRANSITION_ENTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location);

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        floatingActionButton=(FloatingActionButton)findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                GeofenceGeometry geofenceGeometry = new GeofenceGeometry(chosenPoint, startRadius, mTransitionType, active);
                intent.putExtra(MapsActivity.GEOFENCE_GEOMETRY, geofenceGeometry);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        latLngList = new ArrayList<LatLng>();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
        mSeekBarRadius = (SeekBar) findViewById(R.id.set_radius_seek_bar);
        android.support.constraint.ConstraintLayout.LayoutParams oldLp =
                (android.support.constraint.ConstraintLayout.LayoutParams) mSeekBarRadius.getLayoutParams();
        int seelBarWitch = (int)(getWindowManager().getDefaultDisplay().getWidth() * 0.75);
        android.support.constraint.ConstraintLayout.LayoutParams ablp =
                new android.support.constraint.ConstraintLayout.LayoutParams(seelBarWitch, oldLp.height);
        mSeekBarRadius.setLayoutParams(ablp);
        mSeekBarRadius.setMax(400);
        mSeekBarRadius.setProgress(5);
        mSeekBarRadius.incrementProgressBy(5);
        mSeekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(chosenPoint == null)
                    return;
                if(circleDrawSeekBar != null )
                    circleDrawSeekBar.remove();
                circleDrawSeekBar = mMap.addCircle(new CircleOptions().center(chosenPoint)
                        .radius(progress).fillColor(fillCircleColor).strokeColor(Color.BLUE).strokeWidth(2));
                mRadius = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startRadius = seekBar.getProgress();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        drawGeofence = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTransitionType = Geofence.GEOFENCE_TRANSITION_ENTER;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_cl);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Do other setup activities here too, as described elsewhere in this tutorial.

        // Do other setup activities here too, as described elsewhere in this tutorial.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, null);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                // TODO Auto-generated method stub
                /*latLngList.add(point);
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(point));

                if(latLngList.size() > 2) {
                    Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                            .clickable(true)
                            .addAll(latLngList));
                }*/

                if(circleDrawSeekBar != null) {
                    circleDrawSeekBar.remove();
                    circleDrawSeekBar = null;
                }

               // mSeekBarRadius = (SeekBar) findViewById(R.id.set_radius_seek_bar);
                //mSeekBarRadius.setProgress(startRadius);
                chosenPoint = point;
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(chosenPoint));
                circleDrawSeekBar = mMap.addCircle(new CircleOptions().center(chosenPoint)
                        .radius(startRadius).fillColor(fillCircleColor).strokeColor(Color.BLUE).strokeWidth(2));

            }
        });
        if(drawGeofence) {
             mMap.addCircle(new CircleOptions().center(geofenceLatLng)
                    .radius(mRadius).fillColor(fillCircleColor).strokeColor(Color.BLUE).strokeWidth(2));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geofenceLatLng, DEFAULT_ZOOM));
        }


        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            showDialogOption();
        } else if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            setResult(MapsActivity.SKIP_MAP, intent);
            finish();
        }
        return true;
    }

    private void showDialogOption() {
        final Dialog dialog = new Dialog(CurrentLocationActivity.this);
        dialog.setContentView(R.layout.dialog_options);
        dialog.setTitle("Options");
        final EditText editRadius = (EditText)dialog.findViewById(R.id.edit_text_radius_set_dialog_options);
        editRadius.setText((new Float(mRadius)).toString());
        final CheckBox checkEnter = (CheckBox)dialog.findViewById(R.id.cb_transition_enter_dialog_options);
        final CheckBox checkExit = (CheckBox)dialog.findViewById(R.id.cb_transition_exit_dialog_options);
        final CheckBox checkDwell = (CheckBox)dialog.findViewById(R.id.cb_transition_dwell_dialog_options);
        final CheckBox checkActive = (CheckBox)dialog.findViewById(R.id.cb_active_dialog_options);
        checkEnter.setChecked((mTransitionType & Geofence.GEOFENCE_TRANSITION_ENTER) != 0);
        checkExit.setChecked((mTransitionType & Geofence.GEOFENCE_TRANSITION_EXIT) != 0);
        checkDwell.setChecked((mTransitionType & Geofence.GEOFENCE_TRANSITION_DWELL) != 0);
        checkActive.setChecked(true);
        Button btOk = (Button) dialog.findViewById(R.id.dialog_options_ok);
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRadius = Float.valueOf(editRadius.getText().toString());

                if(mRadius < 0)
                    mRadius = 0;
                else if(mRadius > 400)
                    mRadius = 400;

                mSeekBarRadius.setProgress((int)mRadius);
                mTransitionType = 0;
                if(checkEnter.isChecked())
                    mTransitionType = mTransitionType | Geofence.GEOFENCE_TRANSITION_ENTER;
                if(checkExit.isChecked())
                    mTransitionType = mTransitionType | Geofence.GEOFENCE_TRANSITION_EXIT;
                if(checkDwell.isChecked())
                    mTransitionType = mTransitionType | Geofence.GEOFENCE_TRANSITION_DWELL;
                active = checkActive.isChecked();
                dialog.dismiss();
            }
        });

        Button btCancel = (Button) dialog.findViewById(R.id.dialog_options_cancel);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

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

    private void getDeviceLocation() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    /*
     * Before getting the device location, you must check location
     * permission, as described earlier in the tutorial. Then:
     * Get the best and most recent location of the device, which may be
     * null in rare cases when a location is not available.
     */
        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null && !drawGeofence) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            if(!drawGeofence)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            if(!drawGeofence)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
        chosenPoint = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        Toast.makeText(getApplicationContext(), "Clicked: " +
                        poi.name + "\nPlace ID:" + poi.placeId +
                        "\nLatitude:" + poi.latLng.latitude +
                        " Longitude:" + poi.latLng.longitude,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPolygonClick(Polygon polygon) {

    }

    @Override
    public void onPolylineClick(Polyline polyline) {

    }
}
