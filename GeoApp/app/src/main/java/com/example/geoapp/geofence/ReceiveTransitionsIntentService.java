package com.example.geoapp.geofence;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.geoapp.geoapp.MapsActivity;
import com.example.geoapp.geoapp.R;
import com.example.geoapp.geostorage.GeoDatabaseManager;
import com.example.geoapp.geostorage.GeofenceTimeTable;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.geoapp.geoapp.SettingsActivity.APP_PREFERENCES;
import static com.example.geoapp.geoapp.SettingsActivity.SP_NOTIF;
import static com.example.geoapp.geoapp.SettingsActivity.SP_NOTIF_HIDE;

public class ReceiveTransitionsIntentService extends IntentService {
    private static final String TAG = "ReceiveTransitionsInten";
    public static final String TRANSITION_INTENT_SERVICE = "TransitionsService";
    private boolean notifi = true;
    private boolean notifi_hide = false;

    public ReceiveTransitionsIntentService() {
        super(TRANSITION_INTENT_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TRANSITION_INTENT_SERVICE, "Location Services error: " + geofencingEvent.getErrorCode());
            return;
        }

        int transitionType = geofencingEvent.getGeofenceTransition();

        List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();

        for (Geofence geofence : triggeredGeofences) {
            Log.d("GEO", "onHandle:" + geofence.getRequestId());
            processGeofence(geofence, transitionType);
        }
    }

    private void processGeofence(Geofence geofence, int transitionType) {
        Log.d(TAG, "processGeofence: "+geofence.toString());
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());

        PendingIntent openActivityIntetnt = PendingIntent.getActivity(this, 0, new Intent(this, MapsActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        int id = Integer.parseInt(geofence.getRequestId());

        SharedPreferences preferences = ReceiveTransitionsIntentService.this.getApplication().getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        notifi = preferences.getBoolean(SP_NOTIF, true);
        notifi_hide = preferences.getBoolean(SP_NOTIF_HIDE, false);

        String transitionTypeString = getTransitionTypeString(transitionType);
        notificationBuilder
                .setSmallIcon(R.drawable.ic_person_pin_circle_white_24dp)
                .setContentTitle("Geofence id: " + id)
                .setContentText("Transition type: " + transitionTypeString)
                .setVibrate(new long[]{500, 500})
                .setContentIntent(openActivityIntetnt)
                .setAutoCancel(true)
                .setOngoing(notifi_hide);

        if (notifi) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(transitionType * 100 + id, notificationBuilder.build());
        }

        //// TODO: add to DB
        GeofenceTimeTable geofenceTimeTable = new GeofenceTimeTable(id, (new Date()).getTime(), transitionType);
       (new GeoDatabaseManager(getApplication())).getDao().insertAll(geofenceTimeTable);

        Log.d("GEO", String.format("notification built:%d %s", id, transitionTypeString));
    }

    private String getTransitionTypeString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "enter";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "exit";
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return "dwell";
            default:
                return "unknown";
        }
    }


}