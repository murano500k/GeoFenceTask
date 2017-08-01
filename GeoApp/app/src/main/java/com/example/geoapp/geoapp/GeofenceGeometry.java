package com.example.geoapp.geoapp;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.internal.d;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by bovchynnikov on 21.07.17.
 */

public class GeofenceGeometry implements Parcelable {

    private LatLng mLatLng;
    private float mRadius;

    GeofenceGeometry(Location location, float radius){
        mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mRadius = radius;
    }

    GeofenceGeometry(LatLng latLng, float radius){
        mLatLng = latLng;
        mRadius = radius;
    }

    GeofenceGeometry(Parcel in){
        mLatLng = (LatLng) in.readParcelable(LatLng.class.getClassLoader());
        mRadius = (float) in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mLatLng, flags);
        dest.writeFloat(mRadius);
    }

    public static final Parcelable.Creator<GeofenceGeometry> CREATOR = new Parcelable.Creator<GeofenceGeometry>() {
        public GeofenceGeometry createFromParcel(Parcel in) {
            return new GeofenceGeometry(in);
        }

        public GeofenceGeometry[] newArray(int size) {

            return new GeofenceGeometry[size];
        }
    };

    public LatLng getmLatLng() {
        return mLatLng;
    }

    public void setmLatLng(LatLng mLatLng) {
        this.mLatLng = mLatLng;
    }

    public float getmRadius() {
        return mRadius;
    }

    public void setmRadius(float mRadius) {
        this.mRadius = mRadius;
    }
}
