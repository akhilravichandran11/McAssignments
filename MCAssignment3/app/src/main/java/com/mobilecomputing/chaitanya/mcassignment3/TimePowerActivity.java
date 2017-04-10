package com.mobilecomputing.chaitanya.mcassignment3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by SAHIL on 09-04-2017.
 */

public class TimePowerActivity extends AppCompatActivity {
    double timeUsedMobile, powerUsedMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_power);

        Bundle bundle = getIntent().getExtras();
        timeUsedMobile = bundle.getDouble("timeUsed");
        powerUsedMobile = bundle.getDouble("powerUsed");

        

    }
}
