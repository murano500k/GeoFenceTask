package com.example.geoapp.geostorage;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.wearable.Asset;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by bovchynnikov on 07.08.17.
 */

public class GeoDatabaseManager extends AndroidViewModel{
    static private final Object mutex = new Object();
    private static final String TAG = "GeoDatabaseManager";

    private static GeoDatabase mDb;
    private GeofenceDao mGeofenceDao;
    {
        mDb = null;
        mGeofenceDao = null;
    }

    private LiveData<List<GeofenceTable>> listGeofenceTableAll;
    private LiveData<List<GeofenceTable>> listGeofenceTableOnlyActive;
    //private LiveData<List<GeofenceTimeTable>> listGeofenceTimeTable;

    static public String UPDATE_UI_LIST = "update_ui_list";

    private static GeoDatabaseManager instanse = null;

    public GeoDatabaseManager(Application application) {
        super(application);
        if(mDb == null)
            mDb = GeoDatabase.getInstance(application.getApplicationContext());
            mGeofenceDao = mDb.geofenceDao();
        //testInit();
        listGeofenceTableAll = mGeofenceDao.getAllLiveData();
        listGeofenceTableOnlyActive = mGeofenceDao.getAllActiveLiveData(true);
    }

    public LiveData<List<GeofenceTable>> getListGeofenceTable(boolean all) {
        return all ? listGeofenceTableAll : listGeofenceTableOnlyActive;
    }

   public static GeoDatabaseManager getInstanse(@NonNull FragmentActivity activity) {
        if(instanse == null)
            instanse = ViewModelProviders.of(activity).get(GeoDatabaseManager.class);
        return instanse;
    }

    public void deleteGeoTable(GeofenceTable... params) {
        (new DeleteGeoTableRow()).execute(params);
    }

    private class DeleteGeoTableRow extends AsyncTask<GeofenceTable, Void, Void> {

        @Override
        protected Void doInBackground(GeofenceTable... params) {
            if(params.length == 1)
                mGeofenceDao.delete(params[0]);
            else
                mGeofenceDao.deleteAll(params);
            return null;
        }
    }

    public void deleteGeoTime(GeofenceTimeTable... params) {
        (new DeleteGeoTimeRow()).execute(params);
    }

    private class DeleteGeoTimeRow extends AsyncTask<GeofenceTimeTable, Void, Void> {

        @Override
        protected Void doInBackground(GeofenceTimeTable... params) {
            if(params.length == 1)
                mGeofenceDao.delete(params[0]);
            else
                mGeofenceDao.deleteAll(params);
            return null;
        }
    }

    public void updateGeoTable(GeofenceTable... params) {
        (new UpdateGeoTableRow()).execute(params);
    }

    private class UpdateGeoTableRow extends AsyncTask<GeofenceTable, Void, Void> {

        @Override
        protected Void doInBackground(GeofenceTable... params) {
            if(params.length == 1)
                mGeofenceDao.updateGeofenceRow(params[0]);
            else
                mGeofenceDao.updateGeofenceTable(params);
            return null;
        }
    }

    public List<GeofenceTable> getAllActiveGeofenceRow() {
        List<GeofenceTable> gtList = null;
        try {
            gtList = (new GetOnlyActiveGeoRow()).execute().get();
        } catch (ExecutionException ee) {
            Log.d(TAG, "getAllActiveGeofenceTable: " + ee.getStackTrace());
        } catch (InterruptedException ie) {
            Log.d(TAG, "getAllActiveGeofenceTable: " + ie.getStackTrace());
        }
        return gtList;
    }

    private class GetOnlyActiveGeoRow extends AsyncTask<Void, Void, List<GeofenceTable>>{

        @Override
        protected List<GeofenceTable> doInBackground(Void... params) {
            return getDao().getAllActive(true);
        }
    }


    public long insert(GeofenceTable param) {
        return mGeofenceDao.insert(param);
    }

    public void insertAll(GeofenceTable... param) {
        mGeofenceDao.insertAll(param);
    }

    public GeofenceDao getDao() {
        if(mGeofenceDao == null)
            mGeofenceDao = mDb.geofenceDao();
        return mGeofenceDao;
    }

    public void open(final Context context) {
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

    public List<GeofenceTable> getGeofenceTables(boolean all) {
        return all ? mGeofenceDao.getAll() : mGeofenceDao.getAllActive(true);
    }

    public void close() {
        mDb.close();
        mGeofenceDao = null;
        mDb = null;
    }

    public void cleanDB() {
        GeofenceTable[] tempGeofenceTable = new GeofenceTable[mGeofenceDao.getAll().size()];
        tempGeofenceTable = mGeofenceDao.getAll().toArray(tempGeofenceTable);
        for(GeofenceTable tempGt : tempGeofenceTable) {
            GeofenceTimeTable[] gttTemp = new GeofenceTimeTable[mGeofenceDao.getTimeTableByGeofenceTable(tempGt.uid).size()];
            gttTemp =  mGeofenceDao.getTimeTableByGeofenceTable(tempGt.uid).toArray(gttTemp);
            mGeofenceDao.deleteAll(gttTemp);
        }
        mGeofenceDao.deleteAll(tempGeofenceTable);
    }

    /// TODO: only for testing
    /// init_test start
    public ArrayList<GeofenceTable> mListGeofenceTableAll;
    public ArrayList<GeofenceTable> mListGeofenceTableOnlyActive;
    {
        mListGeofenceTableAll = new ArrayList<GeofenceTable>();
        mListGeofenceTableOnlyActive = new ArrayList<GeofenceTable>();
    }
    private static final String startUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";
    private static final String commaUrl = ",";
    private static final String endUrl = "&sensor=true";
    private int id = 0;
    public void testInit() {
        mListGeofenceTableAll.add(getGeofenceRowTest(50.429557, 30.518141, 15, Geofence.GEOFENCE_TRANSITION_ENTER, null, true));
        mListGeofenceTableAll.add(getGeofenceRowTest(50.419827, 30.484082, 30, Geofence.GEOFENCE_TRANSITION_ENTER, null, true));
        mListGeofenceTableAll.add(getGeofenceRowTest(50.444993, 30.501386, 45, Geofence.GEOFENCE_TRANSITION_ENTER, null, true));
        mListGeofenceTableAll.add(getGeofenceRowTest(50.439664, 30.509769, 10, Geofence.GEOFENCE_TRANSITION_ENTER, null, false));
        mListGeofenceTableAll.add(getGeofenceRowTest(50.423350, 30.479781, 25, Geofence.GEOFENCE_TRANSITION_ENTER, null, false));
        mListGeofenceTableAll.add(getGeofenceRowTest(50.445824, 30.512964, 35, Geofence.GEOFENCE_TRANSITION_ENTER, null, false));
        mListGeofenceTableAll.add(getGeofenceRowTest(50.418313, 30.486337, 18, Geofence.GEOFENCE_TRANSITION_ENTER, null, true));
        mListGeofenceTableAll.add(getGeofenceRowTest(50.424849, 30.506372, 50, Geofence.GEOFENCE_TRANSITION_ENTER, null, false));

        GeofenceTable[] tempGeofenceTable = new GeofenceTable[mGeofenceDao.getAll().size()];
        tempGeofenceTable = mGeofenceDao.getAll().toArray(tempGeofenceTable);
        for(GeofenceTable tempGt : tempGeofenceTable) {
            GeofenceTimeTable[] gttTemp = new GeofenceTimeTable[mGeofenceDao.getTimeTableByGeofenceTable(tempGt.uid).size()];
            gttTemp =  mGeofenceDao.getTimeTableByGeofenceTable(tempGt.uid).toArray(gttTemp);
            mGeofenceDao.deleteAll(gttTemp);
        }
        mGeofenceDao.deleteAll(tempGeofenceTable);

        //mListGeofenceTableAll.addAll(mGeofenceDao.getAll());

        GeofenceTable[] temp = new GeofenceTable[mListGeofenceTableAll.size()];
        mGeofenceDao.insertAll(mListGeofenceTableAll.toArray(temp));
        mListGeofenceTableAll.clear();
        mListGeofenceTableAll.addAll(mGeofenceDao.getAll());
        mListGeofenceTableOnlyActive.addAll(mGeofenceDao.getAllActive(true));


        ArrayList<GeofenceTimeTable> gtt = new     ArrayList<GeofenceTimeTable>();
        gtt.add(new GeofenceTimeTable(mListGeofenceTableOnlyActive.get(mListGeofenceTableOnlyActive.size()-1).uid,
                (new Date()).getTime(),
                mListGeofenceTableOnlyActive.get(mListGeofenceTableOnlyActive.size()-1).transitionType));
        gtt.add(new GeofenceTimeTable(mListGeofenceTableOnlyActive.get(mListGeofenceTableOnlyActive.size()-1).uid,
                (new Date()).getTime(),
                mListGeofenceTableOnlyActive.get(mListGeofenceTableOnlyActive.size()-1).transitionType));

        GeofenceTimeTable [] arrgtt = new GeofenceTimeTable[gtt.size()];
        arrgtt = gtt.toArray(arrgtt);
        mGeofenceDao.insertAll(arrgtt);

        int st = mGeofenceDao.getTimeTableByGeofenceTable(mListGeofenceTableOnlyActive.get(mListGeofenceTableOnlyActive.size()-1).uid).size();
        Log.d("Test_tt", "st = " + st + "        uid = " + (mListGeofenceTableOnlyActive.get(mListGeofenceTableOnlyActive.size()-1).uid)
        + "              arrgtt = " + arrgtt.length);
    }

    GeofenceTable getGeofenceRowTest(double latitude, double longitude, float radius, int transitionType, String address, Boolean is_active) {
        GeofenceTable geofenceRow = new GeofenceTable();
        geofenceRow.latitude = latitude;
        geofenceRow.longitude = longitude;
        geofenceRow.radius = radius;
        geofenceRow.transitionType = transitionType;
        geofenceRow.address = address != null ? address : startUrl + latitude + commaUrl + longitude + endUrl;
        geofenceRow.isActive = is_active;
        return geofenceRow;
    }

    public GeofenceDao getGeofenceDao() {
        return mGeofenceDao;
    }

    ////////////////////////for_testing
    public synchronized List<GeofenceTable> getAll_OnlyForTest(boolean param) {
        return param ? mGeofenceDao.getAll() : mGeofenceDao.getAllActive(true);
    }
    /// TODO: init_test end
}
