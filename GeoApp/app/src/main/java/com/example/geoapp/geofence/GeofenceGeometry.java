package com.example.geoapp.geofence;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by bovchynnikov on 21.07.17.
 */

public class GeofenceGeometry implements Parcelable {

    private LatLng mLatLng;
    private float mRadius;
    private int mTmTransitionType;
    private boolean mActive;

    public GeofenceGeometry(Location location, float radius, int transitionType, boolean active){
        mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mRadius = radius;
        mTmTransitionType = transitionType;
        mActive = active;
    }

    public GeofenceGeometry(LatLng latLng, float radius, int transitionType, boolean active){
        mLatLng = latLng;
        mRadius = radius;
        mTmTransitionType = transitionType;
        mActive = active;
    }

    GeofenceGeometry(Parcel in){
        mLatLng = (LatLng) in.readParcelable(LatLng.class.getClassLoader());
        mRadius = (float) in.readFloat();
        mTmTransitionType = (int) in.readInt();
        boolean []arr = new boolean[1];
        in.readBooleanArray(arr);
        mActive = arr[0];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mLatLng, flags);
        dest.writeFloat(mRadius);
        dest.writeInt(mTmTransitionType);
        dest.writeBooleanArray(new boolean[]{mActive});
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

    public int getmTmTransitionType() {
        return mTmTransitionType;
    }

    public void setmTmTransitionType(int mTmTransitionType) {
        this.mTmTransitionType = mTmTransitionType;
    }

    public boolean ismActive() {
        return mActive;
    }

    public void setmActive(boolean mActive) {
        this.mActive = mActive;
    }
}
