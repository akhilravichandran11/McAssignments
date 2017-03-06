package com.example.group13.assignment2;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by ddeepak on 3/4/2017.
 */

public class AccelerometerService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    int accelerometerSamplingRate = 1000000;
    float x_value;
    float y_value;
    float z_value;
    public static int timeDelay = 1000;
    public long lastSaved;
    PatientInfo patientInfo;

    @Override
    public void onCreate(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensorAccelerometer, accelerometerSamplingRate);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        lastSaved = System.currentTimeMillis();
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if(System.currentTimeMillis() - lastSaved > timeDelay) {
                lastSaved = System.currentTimeMillis();
                getAccelerometer(sensorEvent, lastSaved);
            }
        }
    }

    private void getAccelerometer(SensorEvent event, long lastSaved)
    {
        float[] values = event.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];
        Log.d("sensorChanged", "X - "+ Float.toString(x) + " | Y - "+ Float.toString(y) + " | Z - "+ Float.toString(z));
        x_value=x;
        y_value=y;
        z_value=z;

        patientInfo.set_value(lastSaved, x_value, y_value, z_value);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
