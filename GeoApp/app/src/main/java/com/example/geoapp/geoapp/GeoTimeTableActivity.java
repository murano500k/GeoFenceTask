package com.example.geoapp.geoapp;

import android.icu.text.AlphabeticIndex;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.geoapp.geostorage.GeofenceTimeTable;

import java.util.ArrayList;
import java.util.List;

public class GeoTimeTableActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_time_table);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_time);

    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private ArrayList<GeofenceTimeTable> timeTable;

        public RecyclerViewAdapter(ArrayList<GeofenceTimeTable> timeTable) {
            this.timeTable = timeTable;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_item, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            GeofenceTimeTable record = timeTable.get(i);
            //viewHolder.name.setText(record.getName());
        }

        @Override
        public int getItemCount() {
            return timeTable.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView time_enter;
            private TextView time_exit;
            private ImageView icon;

            public ViewHolder(View itemView) {
                super(itemView);
                time_enter = (TextView) itemView.findViewById(R.id.info_text_time_enter);
                time_exit = (TextView) itemView.findViewById(R.id.info_text_time_exit);
                //icon = (ImageView) itemView.findViewById(R.id.recyclerViewItemIcon);
            }
        }
    }
}
