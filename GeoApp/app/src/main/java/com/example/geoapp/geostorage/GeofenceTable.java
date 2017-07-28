package com.example.geoapp.geostorage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Intent;

import com.example.geoapp.geoapp.MyGeofence;
import com.google.android.gms.location.Geofence;

import java.util.HashMap;

/**
 * Created by bovchynnikov on 25.07.17.
 */

@Entity(tableName = "geotable")
public class GeofenceTable {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "radius")
    public float radius;

    @ColumnInfo(name = "address")
    public String address;

    @ColumnInfo(name = "transition_type")
    public int transitionType;

    @ColumnInfo(name = "is_active")
    public boolean isActive = true;

    @Ignore
    public MyGeofence myGeofence;

    @Ignore
    public MyGeofence getMyGeofence() {
        int id = this.uid;
        double latitude = this.latitude;
        double longitude = this.longitude;
        int transitionType = this.transitionType;
        float radius = this.radius;
        return new MyGeofence(id,  latitude, longitude, radius, transitionType);
    }

    @Ignore
    public String toString() {
        return new String("id = " + uid + ", address = " + address);
    }
}
