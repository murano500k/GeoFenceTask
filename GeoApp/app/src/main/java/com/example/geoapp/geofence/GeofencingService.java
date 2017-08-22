package com.example.geoapp.geofence;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.geoapp.geoapp.MapsActivity;
import com.example.geoapp.geostorage.GeoDatabaseManager;
import com.example.geoapp.geostorage.GeofenceTable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GeofencingService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static final String EXTRA_GEOFENCE = "geofence";
    public static final String EXTRA_ACTION = "action";
    public static final String GET_ADDRESS = new String("GET_ADDRESS").toLowerCase();

    private List<Geofence> mGeofenceListsToAdd = new ArrayList<>();
    private List<GeofenceTable> mGeofenceRows = new ArrayList<>();
    private List<String> mGeofenceListIds = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private int mTransitionType = 0;
    private String address = null;
    private Action mAction;
    private int transitionType;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mAction = (Action) intent.getSerializableExtra(EXTRA_ACTION);

        if (mAction == Action.ADD || mAction == mAction.REMOVE) {
            GeofenceEntity newGeofence = (GeofenceEntity) intent.getSerializableExtra(EXTRA_GEOFENCE);
            mTransitionType = newGeofence.getTransitionType();
            address = newGeofence.getAddress();
            mGeofenceListIds.add(newGeofence.getId());
            mGeofenceListsToAdd.add(newGeofence.toGeofence())  ;
            transitionType = newGeofence.getTransitionType();
            initGoogleApiClient();
        }

        if (mAction == Action.START_INIT) {
            mGeofenceRows.addAll((new GeoDatabaseManager(getApplication()).getAllActiveGeofenceRow()));
            if (mGeofenceRows.size() > 0) {
                for (GeofenceTable gt : mGeofenceRows) {
                    mGeofenceListsToAdd.add(gt.getGeofenceEntity().toGeofence());
                }
                initGoogleApiClient();
            } else {
                stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("GEO", "Location client connected");
        if (mAction == Action.ADD || mAction == Action.START_INIT) {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            Log.d("GEO", "Location client adds geofence");
            builder.setInitialTrigger(mTransitionType
                    /*transitionType == Geofence.GEOFENCE_TRANSITION_ENTER ? GeofencingRequest
                            .INITIAL_TRIGGER_ENTER : GeofencingRequest.INITIAL_TRIGGER_EXIT*/);
            builder.addGeofences(mGeofenceListsToAdd);
            GeofencingRequest build = builder.build();

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, build,
                        getPendingIntent())
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (status.isSuccess()) {
                                    String msg = "Geofences added: " + status.getStatusMessage();
                                    Log.e("GEO", msg);
                                    Toast.makeText(GeofencingService.this, msg, Toast.LENGTH_SHORT)
                                            .show();
                                }
                                GeofencingService.this.onResult(status);
                            }
                        });
            }
        } else if(mAction == Action.REMOVE) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, mGeofenceListIds).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            String msg = "Geofences remove: " + status.getStatusMessage();
                            Log.e("GEO", msg);
                            Toast.makeText(GeofencingService.this, msg, Toast.LENGTH_SHORT)
                                    .show();
                        }
                        GeofencingService.this.onResult(status);
                    }
                });
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("GEO", "onConnectionSuspended i = " + i);
    }

    private PendingIntent getPendingIntent() {
        Intent transitionService = new Intent(this, ReceiveTransitionsIntentService.class);
        transitionService.putExtra(GET_ADDRESS, address);
        return PendingIntent
                .getService(this, 0, transitionService, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("GEO", "Location client connection failed: " + connectionResult.getErrorCode());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("GEO", "Location service destroyed");
        super.onDestroy();
    }

    private void initGoogleApiClient () {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    public void onResult(@NonNull Status status) {
        Log.d("GEO", "Geofences onResult" + status.toString());
        if (status.isSuccess()) {
            mGoogleApiClient.disconnect();
            stopSelf();
        } else {
            String text = "Error while geofence: " + status.getStatusMessage();
            Log.e("GEO", text);
            Toast.makeText(GeofencingService.this, text, Toast.LENGTH_SHORT).show();
        }
    }

    public enum Action implements Serializable {ADD, REMOVE, START_INIT}

}