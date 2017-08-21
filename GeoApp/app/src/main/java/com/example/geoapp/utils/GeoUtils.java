package com.example.geoapp.utils;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by bovchynnikov on 27.07.17.
 */

public class GeoUtils {
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
        boolean isConnectedWiFi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
        return isConnected || isConnectedWiFi;
    }

    public static boolean isGPSEnabled(Context context) {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (Exception ex) {
            Log.d("GeoUtils", ex.getStackTrace().toString());
        }
        return gps_enabled;
    }
}
