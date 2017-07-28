package com.example.geoapp.geostorage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Map;

/**
 * Created by bovchynnikov on 27.07.17.
 */

public class GeoAddresesTable {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "address")
    public String address;

    @Ignore
    public String toString() {
        return new String(address);
    }
}
