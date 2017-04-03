package com.mobilecomputing.chaitanya.mcassignment3;

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

import java.util.ArrayList;

/**
 * Created by chaitanya on 4/1/17.
 */

public class AccelerometerService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    ArrayList<Float> valueList;
    int count = 0;
    public static int timeDelay = 100;
    public long lastSaved;
    final static String ACCELEROMETER_INTENET_ACTION = "PUSH_ACCELEROMETER_DATA";

    @Override
    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        lastSaved = System.currentTimeMillis();
        valueList = new ArrayList<Float>();
        Log.d("accelerometerService", "Created");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (System.currentTimeMillis() - lastSaved > timeDelay) {

                lastSaved = System.currentTimeMillis();
                getAccelerometer(sensorEvent);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub

        return START_STICKY;
    }

    private void getAccelerometer(SensorEvent event) {

        float[] values = event.values;
        valueList.add(values[0]);
        valueList.add(values[1]);
        valueList.add(values[2]);
        count += 1;

        if(count == 50) {
            count = 0;
            Intent intent = new Intent();
            intent.setAction(ACCELEROMETER_INTENET_ACTION);
            intent.putExtra("value_list", valueList);
            sendBroadcast(intent);
        }
        Log.d("sensorChanged", "X - " + Float.toString(values[0]) + " | Y - " + Float.toString(values[1]) + " | Z - " + Float.toString(values[2]));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}