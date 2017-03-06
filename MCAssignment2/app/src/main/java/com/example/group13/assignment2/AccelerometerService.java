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
import android.provider.Settings;
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
    final static String ACCELEROMETER_INTENET_ACTION = "PUSH_ACCELEROMETER_DATA";

    @Override
    public void onCreate(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        lastSaved = System.currentTimeMillis();
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
            if(System.currentTimeMillis() - lastSaved > timeDelay) {

                lastSaved = System.currentTimeMillis();
                getAccelerometer(sensorEvent, lastSaved);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub

        return START_STICKY;
    }

    private void getAccelerometer(SensorEvent event, long lastSaved)
    {
        float[] values = event.values;
        x_value = values[0];
        y_value = values[1];
        z_value = values[2];

        Intent intent = new Intent();
        intent.setAction(ACCELEROMETER_INTENET_ACTION );

        intent.putExtra("X", x_value);
        intent.putExtra("Y", y_value);
        intent.putExtra("Z", z_value);

        sendBroadcast(intent);

//        Log.d("sensorChanged", "X - "+ Float.toString(x_value) + " | Y - "+ Float.toString(y_value) + " | Z - "+ Float.toString(z_value));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
