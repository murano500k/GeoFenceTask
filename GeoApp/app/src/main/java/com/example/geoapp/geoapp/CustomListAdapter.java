package com.example.geoapp.geoapp;

/**
 * Created by bovchynnikov on 24.07.17.
 */

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<String> itemname;
    private final Integer[] imgid;

    public CustomListAdapter(Activity context, ArrayList<String> itemname, Integer[] imgid) {

        super(context, R.layout.item_list_layout_geofence, itemname);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.itemname=itemname;
        this.imgid=imgid;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.item_list_layout_geofence, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView extratxt = (TextView) rowView.findViewById(R.id.textView1);

        txtTitle.setText(itemname.get(position));
        //imageView.setImageResource(imgid[position]);
        imageView.setImageResource(R.drawable.google_maps_icon);
        extratxt.setText("Description "+itemname.get(position));
        return rowView;
    }
}
