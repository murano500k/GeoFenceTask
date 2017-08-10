package com.example.geoapp.geostorage;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;


/**
 * Created by bovchynnikov on 25.07.17.
 */
@Database(entities = {GeofenceTable.class, GeofenceTimeTable.class}, version = 11)
@TypeConverters({Converters.class})
public abstract class GeoDatabase extends RoomDatabase {
    private static GeoDatabase INSTANCE = null;
    public static GeoDatabase getInstance(Context context) {
        if(INSTANCE == null)
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    GeoDatabase.class, "geo-database").build();
        return INSTANCE;
    }
    public abstract GeofenceDao geofenceDao();
}
