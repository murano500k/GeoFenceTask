package com.example.geoapp.geostorage;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;


/**
 * Created by bovchynnikov on 25.07.17.
 */
@Database(entities = {GeofenceTable.class}, version = 1)
public abstract class GeoDatabase extends RoomDatabase {
    public abstract GeofenceDao geofenceDao();
}
