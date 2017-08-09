package com.example.geoapp.geostorage;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;


/**
 * Created by bovchynnikov on 25.07.17.
 */
@Database(entities = {GeofenceTable.class, GeofenceTimeTable.class}, version = 11)
@TypeConverters({Converters.class})
public abstract class GeoDatabase extends RoomDatabase {
    public abstract GeofenceDao geofenceDao();
}
