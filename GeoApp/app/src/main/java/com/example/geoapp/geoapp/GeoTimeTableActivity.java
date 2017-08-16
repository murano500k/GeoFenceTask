package com.example.geoapp.geoapp;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.geoapp.geostorage.GeoDatabaseManager;
import com.example.geoapp.geostorage.GeofenceTable;
import com.example.geoapp.geostorage.GeofenceTimeTable;
import com.google.android.gms.location.Geofence;

import java.util.Date;


import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.widget.Toast;

public class GeoTimeTableActivity extends AppCompatActivity /*implements LifecycleRegistryOwner*/ {

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    //private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);
    private final static Object mutex = new Object();
    private List<GeofenceTimeTable> listTime = null;
    private final int EMPTY_TABLE = 0;
    private Handler finishActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_time_table);
        Intent intent = getIntent();
        listTime = new ArrayList<>();
        final int uid = intent.getIntExtra(SettingsActivity.GET_ID_FROM_INTENT, 0);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_time);

        finishActivity = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case EMPTY_TABLE:
                        Toast.makeText(GeoTimeTableActivity.this, "Empty table", Toast.LENGTH_SHORT).show();
                        GeoTimeTableActivity.this.finish();
                        break;
                }
            }
        };

        (new Thread(new Runnable() {
            @Override
            public void run() {
                listTime = (new GeoDatabaseManager(getApplication()).getDao().getTimeTableByGeofenceTable(uid));
                //new GeoDatabaseManager(getApplication()).getDao().
                /*if(listTime == null){
                    finishActivity.sendEmptyMessage(EMPTY_TABLE);
                }*/
                recyclerViewAdapter = new RecyclerViewAdapter(listTime);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                mRecyclerView.setLayoutManager(layoutManager);
                mRecyclerView.setAdapter(recyclerViewAdapter);
            }
        })).start();
            recyclerViewAdapter = new RecyclerViewAdapter(listTime);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setAdapter(recyclerViewAdapter);

    }

    /*@Override
    public LifecycleRegistry getLifecycle() {
        return mRegistry;
    }*/

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ItemViewHolder> {

        private List<GeofenceTimeTable> timeTable;

        public RecyclerViewAdapter(List<GeofenceTimeTable> timeTable) {
            this.timeTable = timeTable;

        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_item_time_table, viewGroup, false);
            return new ItemViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder viewHolder, int i) {
            GeofenceTimeTable record = timeTable.get(i);
            //viewHolder.name.setText(record.getName());
            String type = getTransitionTypeString(record.type);

            viewHolder.time_enter.setText("Time " + type + ": " + new Date(record.time).toString());
        }

        private String getTransitionTypeString(int transitionType) {
            switch (transitionType) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    return "enter";
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    return "exit";
                case Geofence.GEOFENCE_TRANSITION_DWELL:
                    return "dwell";
                default:
                    return "unknown";
            }
        }

        @Override
        public int getItemCount() {
            return timeTable.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            private TextView time_enter;
           /* private TextView time_exit;
            private TextView time_total;*/

            public ItemViewHolder(View itemView) {
                super(itemView);
                time_enter = (TextView) itemView.findViewById(R.id.info_text_time_enter);
               /* time_exit = (TextView) findViewById(R.id.info_text_time_exit);
                time_total = (TextView) findViewById(R.id.info_text_time_total);*/
            }
        }
    }
}
