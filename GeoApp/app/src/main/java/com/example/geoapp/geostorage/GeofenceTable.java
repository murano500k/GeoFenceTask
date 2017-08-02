package com.example.geoapp.geostorage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.geoapp.geoapp.MyGeofence;
import com.example.geoapp.geoapp.R;
import com.google.android.gms.location.Geofence;

import java.util.HashMap;

/**
 * Created by bovchynnikov on 25.07.17.
 */

@Entity(tableName = "geotable")
public class GeofenceTable  implements Parcelable {
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

    public GeofenceTable() {

    }

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

    @Ignore
    static final int defaultIconId = 3;

    @Ignore
    static final int activeIconId = R.drawable.maps_2_icon;

    @Ignore
    static final int notActiveIconId = R.drawable.maps_icon;

    @Ignore
    public int getBitmapId() {
        return isActive ? activeIconId : notActiveIconId;
    }

    @Ignore
    public GeofenceTable(Parcel in){
        this.radius = in.readFloat();
        int []arrI = new int[2];
        in.readIntArray(arrI);
        this.uid = arrI[0];
        this.transitionType = arrI[1];
        boolean []arrB = new boolean[1];
        in.readBooleanArray(arrB);
        this.isActive = arrB[0];
        this.address = in.readString();
        double []arrD = new double[2];
        in.readDoubleArray(arrD);
        this.latitude = arrD[0];
        this.longitude = arrD[1];
    }

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.radius);
        dest.writeIntArray(new int[]{this.uid, this.transitionType});
        dest.writeBooleanArray(new boolean[]{this.isActive});
        dest.writeString(this.address);
        dest.writeDoubleArray(new double[]{this.latitude, this.longitude});
    }

    @Ignore
    public static final Parcelable.Creator<GeofenceTable> CREATOR = new Parcelable.Creator<GeofenceTable>() {
        public GeofenceTable createFromParcel(Parcel in) {
            return new GeofenceTable(in);
        }

        public GeofenceTable[] newArray(int size) {

            return new GeofenceTable[size];
        }
    };


}
