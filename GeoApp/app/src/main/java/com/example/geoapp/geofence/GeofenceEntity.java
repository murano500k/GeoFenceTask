package com.example.geoapp.geofence;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;
import com.example.geoapp.geostorage.GeofenceTable;

import java.io.Serializable;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;

/**
 * Created by bovchynnikov on 19.07.17.
 */

public class GeofenceEntity implements Serializable {
    private static final int ONE_MINUTE = 60000;

    private int id;
    private double latitude;
    private double longitude;
    private float radius;
    private int transitionType;
    private GeofenceTable rowGeofence;

    public GeofenceEntity(int id, double latitude, double longitude, float radius, int transitionType) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.transitionType = transitionType;
    }

    public Geofence toGeofence() {
        return new Geofence.Builder()
                .setRequestId(String.valueOf(id))
                .setTransitionTypes(transitionType)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(NEVER_EXPIRE)
                .build();
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    public float getRadius() {
        return radius;
    }

    public int getTransitionType() {
        return transitionType;
    }
}
