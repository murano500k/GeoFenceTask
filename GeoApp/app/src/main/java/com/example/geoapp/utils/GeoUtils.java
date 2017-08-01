package com.example.geoapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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
}
