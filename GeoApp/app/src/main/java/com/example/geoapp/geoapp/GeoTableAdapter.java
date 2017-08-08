package com.example.geoapp.geoapp;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.geoapp.geostorage.GeofenceTable;

import java.util.ArrayList;

/**
 * Created by bovchynnikov on 08.08.17.
 */

public class GeoTableAdapter extends RecyclerView.Adapter<GeoTableAdapter.ViewHolder> {

    private final ArrayList<GeofenceTable> geoTable;
    public GeoTableAdapter(ArrayList<GeofenceTable> geoTable) {
        this.geoTable = geoTable;
    }


    @Override
    public GeoTableAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(GeoTableAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        //private TextView name;
        //private ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            //name = (TextView) itemView.findViewById(R.id.recyclerViewItemName);
            //icon = (ImageView) itemView.findViewById(R.id.recyclerViewItemIcon);
        }
    }

}
