package com.example.geoapp.geoapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {

    private Button btnStart;
    TextView tv;
    TextView tvSysAb;
    TextView tvSysBl;
    int color = Color.BLUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        tv = (TextView) findViewById(R.id.on_air_ls);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/mexcellent3d.ttf");
        //"fonts/NEON.TTF");
        tv.setTypeface(face);

        tvSysAb = (TextView) findViewById(R.id.on_lin_ab_ls);
        tvSysBl = (TextView) findViewById(R.id.on_lin_bl_ls);
        Typeface faceSys = Typeface.createFromAsset(getAssets(),
                "fonts/NEON.TTF");

        tvSysAb.setTypeface(faceSys);
        tvSysBl.setTypeface(faceSys);
        tvSysBl.setRotation(180);


        init();
    }

    private void init() {
        final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation.setDuration(750); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setAnimation(animation);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }
}
