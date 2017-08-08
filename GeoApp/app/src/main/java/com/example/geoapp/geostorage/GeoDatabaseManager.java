package com.example.geoapp.geostorage;

import android.arch.persistence.room.Room;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bovchynnikov on 07.08.17.
 */

public class GeoDatabaseManager {
    static private final Object mutex = new Object();

    static private GeoDatabase mDb;
    static private GeofenceDao mGeofenceDao;

    static {
        mDb = null;
        mGeofenceDao = null;
    }

    static public String UPDATE_UI_LIST = "update_ui_list";

    static public void open(final Context context) {
        if(null == mDb)
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mutex) {
                        mDb = Room.databaseBuilder(context,
                                GeoDatabase.class, "geo-database").build();
                        mGeofenceDao = mDb.geofenceDao();
                    }
                }
            })).start();
    }

    static public List<GeofenceTable> getGeofenceTables(boolean all) {
        return all ? mGeofenceDao.getAll() : mGeofenceDao.loadAllActive(true);
    }

    static public void close() {
        if(null != mDb)
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mutex) {
                        mDb.close();
                        mGeofenceDao = null;
                        mDb = null;
                    }
                }
            })).start();
    }

}
