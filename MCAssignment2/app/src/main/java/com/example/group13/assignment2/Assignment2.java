package com.example.group13.assignment2;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Assignment2 extends AppCompatActivity implements SensorEventListener{
    GraphView g;
    LinearLayout graph;
    float[] values;
    float[] valuesy;
    float[] valuesz;
    Thread movingGraph = null;
    Boolean flag = null;

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;

    Handler threadHandle = new Handler(){
        @Override
        public void handleMessage(Message msg){
            for (int i = 0; i < 9; i++) {
                values[i] = values[i+1];
                valuesy[i] = valuesy[i+1];
                valuesz[i] = valuesz[i+1];
            }

            values[9]= (float)Math.ceil(Math.random()*180);
            valuesy[9]= (float)Math.ceil(Math.random()*180);
            valuesz[9]= (float)Math.ceil(Math.random()*180);
            g.invalidate();
            g.setValues(values, valuesy, valuesz);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment2);

        init();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


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
                for (int i = 0; i < 10; i++) {
                    values[i] = 0;
                    valuesy[i] = 0;
                    valuesz[i] = 0;
                }
                g.invalidate();
                g.setValues(values, valuesy, valuesz);
            }
        });
    }

    protected void init(){
        String[] hlabels= new String[10];
        for (int i = 0; i < 10; i++) {
            hlabels[i]=String.valueOf(i * 18);
        }

        int k = 0;

        String[] vlabels= new String[10];
        for (int i = 0; i < 10; i++){
            vlabels[i]=String.valueOf((i * 10) + k);
        }

        values= new float[10];
        valuesy= new float[10];
        valuesz= new float[10];
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
                            Thread.sleep(250);
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor temp = event.sensor;
        if (temp.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}