package com.example.geoapp.geostorage;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.HashMap;
import java.util.List;

/**
 * Created by bovchynnikov on 25.07.17.
 */
@Dao
public interface GeofenceDao {
    @Query("SELECT * FROM geotable")
    public List<GeofenceTable> getAll();

    @Query("SELECT * FROM geotable WHERE uid IN (:geofenceIds)")
    public List<GeofenceTable> loadAllByIds(int[] geofenceIds);

    @Query("SELECT * FROM geotable WHERE is_active IN (:active)")
    public List<GeofenceTable> loadAllActive(boolean active);

    @Query("SELECT address FROM geotable")
    public List<String> loadAllAddresses();

    @Insert
    public void insertAll(GeofenceTable... geotables);

    @Query("SELECT * FROM geotable WHERE address LIKE :addrs LIMIT 1")
    public GeofenceTable findByAddress(String addrs);

    @Query("SELECT * FROM geotable WHERE address LIKE (:url)")
    public List<GeofenceTable> findByUrlsInAddresses(String url);

    @Query("SELECT * FROM geotable WHERE uid LIKE :id LIMIT 1")
    public GeofenceTable findByUid(String id);

    @Query("SELECT uid, address FROM geotable")
    public List<GeoAddresesTable> loadUidsAndAddresses();

    @Update
    public void updateGeofenceTable(GeofenceTable... geotables);

    @Update
    public void updateGeofenceRow(GeofenceTable geotable);

    @Delete
    public void delete(GeofenceTable geotable);

    @Delete
    public void deleteAll(GeofenceTable... geotable);
}
