package com.example.geoapp.geostorage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.sql.Date;

/**
 * Created by bovchynnikov on 27.07.17.
 */
@Entity(tableName = "geotable_time", foreignKeys = @ForeignKey( entity = GeofenceTable.class,
                                                                parentColumns = "uid",
                                                                childColumns = "geotable_id"))
public class GeofenceTimeTable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "time")
    public Long time;

    @ColumnInfo(name = "type")
    public int type;

    @ColumnInfo(name = "geotable_id")
    public int geotable_id;

    @Ignore
    public GeofenceTimeTable(int geotable_id, Long time, int type) {
        this.geotable_id = geotable_id;
        this.time = time;
        this.type = type;
    }

    public GeofenceTimeTable() {
    }
}
