package com.example.geoapp.geoapp;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by bovchynnikov on 21.07.17.
 */

public class GeoArea implements Parcelable {

    LatLng mLatLng;
    float mRadius;

    GeoArea(Location location, float radius){
        mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mRadius = radius;
    }


    GeoArea(Parcel in){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static final Parcelable.Creator<GeoArea> CREATOR = new Parcelable.Creator<GeoArea>() {
        // распаковываем объект из Parcel
        public GeoArea createFromParcel(Parcel in) {

            return new GeoArea(in);
        }

        public GeoArea[] newArray(int size) {
            return new GeoArea[size];
        }
    };
}
