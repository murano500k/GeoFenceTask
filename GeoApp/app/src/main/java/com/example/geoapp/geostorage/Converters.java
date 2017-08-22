package com.example.geoapp.geostorage;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Created by bovchynnikov on 27.07.17.
 */

public class Converters {
    @SuppressWarnings("unused")
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @SuppressWarnings("unused")
    @TypeConverter
    public static Date fromTimeTable(GeofenceTimeTable gtt) {
        return gtt == null ? null : new Date(gtt.time);
    }

    @SuppressWarnings("unused")
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
