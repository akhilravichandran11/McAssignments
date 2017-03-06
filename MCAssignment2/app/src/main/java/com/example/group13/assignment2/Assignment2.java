package com.example.group13.assignment2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.util.*;

import javax.crypto.ExemptionMechanismException;


public class Assignment2 extends AppCompatActivity{
    GraphView g;
    LinearLayout graph;
    AccelerometerReceiver accelerometerReceiver;

    List<Float> alValuesX, alValuesY, alValuesZ;
    float[] values, valuesy, valuesz;
    int valueArraySize = 10;

    Thread movingGraph = null;
    int threadSleepTime = 1000;
    Boolean flag = null;
    Boolean threadStartedFlag=false;

    Button buttonRun;
    Button buttonStop;
    Button buttonUpload;
    Button buttonDownload;
    EditText widgetPatientName;
    EditText widgetPatientID;
    EditText widgetPatientAge;
    RadioGroup widgetPatientSex;

    PatientInfo patientInfo;

    SQLiteDatabase db;
    public String TABLE = "dude"+System.currentTimeMillis();
    public static final String DATABASE_NAME = "group13Database";
    public static final String FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "Mydata";
    public static final String DATABASE_LOCATION = FILE_PATH + File.separator + DATABASE_NAME;

    Handler threadHandle = new Handler(){
        @Override
        public void handleMessage(Message msg){
            getDataFromDatabase();
            g.invalidate();
            g.setValues(values, valuesy, valuesz);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment2);
        Log.d("dude",FILE_PATH );
        Intent intent = new Intent(this, AccelerometerService.class);
        startService(intent);
        accelerometerReceiver = new AccelerometerReceiver();

        init();

        widgetPatientName = (EditText) findViewById(R.id.PNameText);
        widgetPatientAge = (EditText) findViewById(R.id.PAgeText);
        widgetPatientID = (EditText) findViewById(R.id.PIDText);
///        widgetPatientSex = (RadioGroup) findViewById(R.id.rBMale);

        try{
            File folder = new File(FILE_PATH);
            if (!folder.exists()) {
                folder.mkdir();
            }
            db = SQLiteDatabase.openOrCreateDatabase(DATABASE_LOCATION, null);
            db.beginTransaction();
        }
        catch (SQLException e){

            Toast.makeText(Assignment2.this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("exp",e.getMessage() );
        }


        buttonRun= (Button)findViewById(R.id.buttonRun);
        buttonStop= (Button)findViewById(R.id.buttonStop);
        buttonDownload= (Button)findViewById(R.id.buttonDownload);
        buttonUpload= (Button)findViewById(R.id.buttonUpload);

        buttonRun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //start
                if (widgetPatientName.getText().toString().matches("") || widgetPatientAge.getText().toString().matches("") || widgetPatientID.getText().toString().matches(""))    {
                    Toast.makeText(Assignment2.this, "Please complete all fields!", Toast.LENGTH_LONG).show();
                    return;
                }

                if(flag == null || !flag){
                    flag = true;

                    patientInfo = new PatientInfo(widgetPatientName.getText().toString(), widgetPatientAge.getText().toString(), widgetPatientID.getText().toString(), true);
                    TABLE = patientInfo.table_name;
                    TABLE = TABLE.replace(" ", "_");
                    Toast.makeText(Assignment2.this, "DB Name: " + TABLE, Toast.LENGTH_SHORT).show();

                    try {

                        db.execSQL("create table if not exists " + TABLE + " ("
                                + " created_at DATETIME DEFAULT CURRENT_TIMESTAMP, "
                                + " x float, "
                                + " y float,"
                                + " z float"
                                +
                                " ); ");

                        try {
                            db.setTransactionSuccessful();
                            db.endTransaction();
                        }catch (Exception e)    {}
                    }
                    catch (SQLiteException e) {

                    }
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(AccelerometerService.ACCELEROMETER_INTENET_ACTION);
                    registerReceiver(accelerometerReceiver, intentFilter);
                    if(!threadStartedFlag) {
                        movingGraph.start();
                        threadStartedFlag = true;
                    }
                    //Toast.makeText(Assignment2.this, "Graph Started", Toast.LENGTH_SHORT).show();
                }
            }
        });



        buttonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                flag = false;
                for (int i = 0; i < valueArraySize; i++) {
                    values[i] = 0;valuesy[i] = 0;valuesz[i] = 0;
                }
                g.invalidate();
                g.setValues(values, valuesy, valuesz);
                try {
                    unregisterReceiver(accelerometerReceiver);
                }catch (Exception e)    {}
            }
        });


        buttonDownload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonRun.setEnabled(false);
                buttonStop.setEnabled(false);
                buttonUpload.setEnabled(false);
                buttonDownload.setEnabled(false);
                Toast.makeText(Assignment2.this, "Download starting", Toast.LENGTH_SHORT).show();


            }
        });


        buttonUpload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                try{

                    if (widgetPatientName.getText().toString().matches("") || widgetPatientAge.getText().toString().matches("") || widgetPatientID.getText().toString().matches(""))    {
                        Toast.makeText(Assignment2.this, "Please complete all fields!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    disableAllButtons();
                    Toast.makeText(Assignment2.this, "Upload Begins", Toast.LENGTH_SHORT).show();




                    Toast.makeText(Assignment2.this, "Upload Ends", Toast.LENGTH_SHORT).show();

                }catch(Exception exp)
                {
                    Toast.makeText(Assignment2.this, "Exception" + exp.getMessage() , Toast.LENGTH_LONG).show();
                    Log.d("Exception",exp.getMessage() );
                }finally{
                    enableAllButtons();
                }




            }
        });
    }

    protected void init(){
        String[] hlabels= new String[10];
        for (int i = 0; i < 10; i++) {
            hlabels[i]=String.valueOf(i);
        }

        String[] vlabels= new String[10];
        for (int i = 0; i < 10; i++){
            vlabels[i]=String.valueOf(i);
        }
        
        values= new float[valueArraySize];valuesy= new float[valueArraySize];valuesz= new float[valueArraySize];
//        alValuesX = new LinkedList<Float>();alValuesY = new LinkedList<Float>();alValuesZ = new LinkedList<Float>();
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


    private class AccelerometerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            float x = intent.getFloatExtra("X", 0.0f);
            float y = intent.getFloatExtra("Y", 0.0f);
            float z = intent.getFloatExtra("Z", 0.0f);
            try {

                db.execSQL("insert into " + TABLE + " (x,y,z) values ('" + x + "', '" + y + "','" + z + "' );");
            }
            catch (SQLiteException e) {

            }
            finally {
//                db.endTransaction();
            }
            Log.d("accelerometerReceiver", "X - "+ Float.toString(x) + " | Y - "+ Float.toString(y) + " | Z - "+ Float.toString(z));
        }

    }

    private void getDataFromDatabase() {
        String query = "SELECT  * FROM " + TABLE + " ORDER BY created_at desc LIMIT 10;";
        Cursor cursor = null;
        int i=9;
        try {
            cursor = db.rawQuery(query, null);

            db.setTransactionSuccessful(); //commit your changes
        } catch (Exception e) {
            Log.d("exp",e.getMessage() );
        }

        if (cursor.moveToFirst()) {
            do {
                values[i] = Float.parseFloat(cursor.getString(1));
                valuesy[i] = Float.parseFloat(cursor.getString(2));
                valuesz[i] = Float.parseFloat(cursor.getString(3));
                i--;
            } while (cursor.moveToNext() && i >= 0);
        }
    }

    private void disableAllButtons()
    {
        disableOrEnableButtons(false,false,false,false);
    }

    private void enableAllButtons()
    {
        disableOrEnableButtons(true,true,true,true);
    }

    private void disableOrEnableButtons(boolean run,boolean stop,boolean upload,boolean download)
    {
        buttonRun.setEnabled(run);
        buttonStop.setEnabled(stop);
        buttonUpload.setEnabled(upload);
        buttonDownload.setEnabled(download);
    }

    private void uploadDatabaseData()
    {
        
    }


}