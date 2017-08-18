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
    private String address;

    public GeofenceEntity(int id, double latitude, double longitude, float radius, int transitionType, String address) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.transitionType = transitionType;
        this.address = address;
    }

    public Geofence toGeofence() {
        Geofence.Builder builder = new Geofence.Builder();
        builder.setRequestId(String.valueOf(id))
                .setTransitionTypes(transitionType)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(NEVER_EXPIRE);

        if((transitionType & Geofence.GEOFENCE_TRANSITION_DWELL) != 0)
            builder.setLoiteringDelay(1000000);

        return builder.build();
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

    public String getId() {
        return String.valueOf(id);
    }

    public String getAddress() {
        return address;
    }
}
