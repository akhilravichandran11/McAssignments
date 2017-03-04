package com.example.chaitanya.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    GraphView g;
    LinearLayout graph;
    float[] values;
    Thread movingGraph = null;
    Boolean flag = null;

    Handler threadHandle = new Handler(){
        @Override
        public void handleMessage(Message msg){
            for (int i = 0; i < 10; i++) {
                values[i] = (float) Math.ceil(Math.random() * 180);
            }
            g.invalidate();
            g.setValues(values);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        Button buttonRun= (Button)findViewById(R.id.buttonRun);
        buttonRun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(flag == null){
                    flag = true;
                    movingGraph.start();
                    Toast.makeText(MainActivity.this, "Graph Started", Toast.LENGTH_SHORT).show();
                } else if(!flag){
                    flag = true;
                    Toast.makeText(MainActivity.this, "Graph Resumed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonStop= (Button)findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                flag = false;
                Toast.makeText(MainActivity.this, "Graph Stopped", Toast.LENGTH_SHORT).show();
                for (int i = 0; i < 10; i++) {
                    values[i] = 0;
                }
                g.invalidate();
                g.setValues(values);
            }
        });
    }

    protected void init(){
        String[] hlabels= new String[10];
        for (int i = 0; i < 10; i++) {
            hlabels[i]=String.valueOf((9 - i) * 18);
        }

        int k = 0;

        String[] vlabels= new String[10];
        for (int i = 0; i < 10; i++){
            vlabels[i]=String.valueOf((i * 10) + k);
        }

        values= new float[10];
        g = new GraphView(MainActivity.this, values, "Main Graph", vlabels, hlabels, GraphView.LINE);
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
                            Thread.sleep(1000);
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

//    public void onRadioButtonClicked(View view) {
//        // Is the button now checked?
//        boolean checked = ((RadioButton) view).isChecked();
//
//        // Check which radio button was clicked
//        switch(view.getId()) {
//            case R.id.radio_pirates:
//                if (checked)
//                    // Pirates are the best
//                    break;
//            case R.id.radio_ninjas:
//                if (checked)
//                    // Ninjas rule
//                    break;
//        }
//    }
}
