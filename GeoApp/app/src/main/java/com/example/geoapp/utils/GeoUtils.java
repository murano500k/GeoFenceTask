package com.example.geoapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by bovchynnikov on 27.07.17.
 */

public class GeoUtils {
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
