package com.example.group13.assignment2;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import java.util.*;

import static android.hardware.SensorManager.GRAVITY_EARTH;

public class Assignment2 extends AppCompatActivity implements SensorEventListener{
    GraphView g;
    LinearLayout graph;
    List<Float> alValuesX, alValuesY, alValuesZ;
    float[] values, valuesy, valuesz;
    int valueArraySize = 10;
    Thread movingGraph = null;
    int threadSleepTime = 1000;
    Boolean flag = null;

    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    int accelerometerSamplingRate = 1000000;

    EditText widgetPatientName;
    EditText widgetPatientID;
    EditText widgetPatientAge;
    RadioGroup widgetPatientSex;
    String patientNameText;
    String patientIDText;
    String patientAgeText;
    String patientSexText;

    Handler threadHandle = new Handler(){
        @Override
        public void handleMessage(Message msg){
            Log.d("threadHandlerCalled", "collectionSizeBefore - "+Integer.toString(alValuesX.size()));

            for (int i = 0; i < 9; i++) {
                values[i] = values[i+1];
                valuesy[i] = valuesy[i+1];
                valuesz[i] = valuesz[i+1];
            }
            int collectionSize = alValuesX.size();
            values[9]= alValuesX.remove(collectionSize-1);
            valuesy[9]= alValuesY.remove(collectionSize-1);
            valuesz[9]= alValuesZ.remove(collectionSize-1);

            alValuesX.clear();alValuesY.clear();alValuesZ.clear();
            Log.d("threadHandlerCalled", "collectionSizeAfter - "+Integer.toString(alValuesX.size()));
            g.invalidate();
            g.setValues(values, valuesy, valuesz);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment2);

        init();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensorAccelerometer, accelerometerSamplingRate);

        widgetPatientName = (EditText) findViewById(R.id.PNameText);
        widgetPatientAge = (EditText) findViewById(R.id.PAgeText);
        widgetPatientID = (EditText) findViewById(R.id.PIDText);
//        widgetPatientSex = (RadioGroup) findViewById(R.id.rBMale);


        Button buttonRun= (Button)findViewById(R.id.buttonRun);
        buttonRun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(flag == null){
                    flag = true;


                    movingGraph.start();
                    Toast.makeText(Assignment2.this, "Graph Started", Toast.LENGTH_SHORT).show();
                } else if(!flag){
                    flag = true;
                    Toast.makeText(Assignment2.this, "Graph Resumed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonStop= (Button)findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                flag = false;
                Toast.makeText(Assignment2.this, "Graph Stopped", Toast.LENGTH_SHORT).show();
                for (int i = 0; i < valueArraySize; i++) {
                    values[i] = 0;
                    valuesy[i] = 0;
                    valuesz[i] = 0;
                }
//                for (int i = 0; i < 10; i++) {
//                    values[i] = 0;
//                    valuesy[i] = 0;
//                    valuesz[i] = 0;
//                }
                g.invalidate();
                g.setValues(values, valuesy, valuesz);
            }
        });
    }

    protected void init(){
        String[] hlabels= new String[10];
        for (int i = 0; i < 10; i++) {
//            hlabels[i]=String.valueOf(i * 18);
            hlabels[i]=String.valueOf(i);
        }

        int k = 0;

        String[] vlabels= new String[10];
        for (int i = 0; i < 10; i++){
//            vlabels[i]=String.valueOf((i * 10) + k);
            vlabels[i]=String.valueOf((i) + k);
        }

//        values= new float[10];valuesy= new float[10];valuesz= new float[10];
        values= new float[valueArraySize];valuesy= new float[valueArraySize];valuesz= new float[valueArraySize];
        alValuesX = new LinkedList<Float>();alValuesY = new LinkedList<Float>();alValuesZ = new LinkedList<Float>();
        g = new GraphView(Assignment2.this, values, valuesy, valuesz, "Main Graph", vlabels, hlabels, GraphView.LINE);
        graph= (LinearLayout)findViewById(R.id.graphll);
        graph.addView(g);

        movingGraph = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int k = 0;
                    while(true){
                        while(flag) {
                            k++;
                            Message msg = threadHandle.obtainMessage(1,Integer.toString(k));
                            threadHandle.sendMessage(msg);
                            Thread.sleep(threadSleepTime);
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(sensorEvent);
        }
    }

    private void getAccelerometer(SensorEvent event)
    {
        float[] values = event.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];
        Log.d("sensorChanged", "X - "+ Float.toString(x) + " | Y - "+ Float.toString(y) + " | Z - "+ Float.toString(z));
        alValuesX.add(x);
        alValuesY.add(y);
        alValuesZ.add(z);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

//    @Override
//    protected void onPause()
//    {
//        super.onPause();
//        mSensorManager.unregisterListener(this);
//    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mSensorManager.registerListener(this, mSensorAccelerometer, 1000000);
//    }
}