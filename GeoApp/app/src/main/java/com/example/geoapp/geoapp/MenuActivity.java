package com.example.geoapp.geoapp;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {
    Button mapButton, clButton;
    ListView listView;
    ArrayList<String> listLocation;
    ArrayAdapter<String> adapterListLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        mapButton = (Button) findViewById(R.id.menu_map_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
        clButton = (Button) findViewById(R.id.menu_current_location_button);
        clButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, CurrentLocationActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        listLocation = new ArrayList<String>();
        listView = (ListView) findViewById(R.id.lvMain);
        adapterListLocation = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, listLocation);
        listView.setAdapter(adapterListLocation);


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if(requestCode == 1) {
            Location location = (Location) data.getParcelableExtra("location");
            if(location != null) {
                android.util.Log.d("Test_cl", "location" + location.toString());
                listLocation.add(location.toString());
                adapterListLocation.notifyDataSetChanged();
            }
        }


    }
}
