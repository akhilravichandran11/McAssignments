package com.mobilecomputing.chaitanya.mcassignment3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import libsvm.svm_model;

public class UI_Handler extends AppCompatActivity {

    SQLiteDatabase db;
    public String TABLE = "dude"+System.currentTimeMillis();
    public static final String DATABASE_NAME = "group13";
    public static final String FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "Mydata";
    public static final String DATABASE_LOCATION = FILE_PATH + File.separator + DATABASE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui__handler);

//        AccelerometerReceiver accelerometerReceiver = new AccelerometerReceiver();
//        Intent intent = new Intent(this, AccelerometerService.class);
//        startService(intent);

        Button buttonTrain;
        Button buttonCalibrate;
        Button buttonTest;

        buttonTrain= (Button)findViewById(R.id.train);
        buttonTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SVM svm = new SVM();
                LinkedList<String> Dataset = getDataFromDatabase();
                String filename = writetocsv(Dataset);
                SVM1 svm1 = new SVM1();
                svm1.train(filename);

//                Toast.makeText(UI_Handler.this, Dataset.get(0), Toast.LENGTH_SHORT).show();
//                svm_model svm_model_instance = svm.svmTrain(Dataset, Dataset.size(), 0);


            }
        });

        buttonCalibrate= (Button)findViewById(R.id.calibrate);
        buttonCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UI_Handler.this, Calibration.class);
                startActivity(i);
            }
        });

        buttonTest= (Button)findViewById(R.id.test);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

//                db.execSQL("insert into " + TABLE + " (x,y,z) values ('" + x + "', '" + y + "','" + z + "' );");
                //db.setTransactionSuccessful();
            }
            catch (SQLiteException e) {

            }
            finally {
//                db.endTransaction();
            }
            Log.d("accelerometerReceiver", "X - "+ Float.toString(x) + " | Y - "+ Float.toString(y) + " | Z - "+ Float.toString(z));
        }

    }

    private LinkedList<String> getDataFromDatabase() {
        db = SQLiteDatabase.openOrCreateDatabase(DATABASE_LOCATION, null);
        db.beginTransaction();
        String query = "SELECT  * FROM training;" ;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            db.setTransactionSuccessful(); //commit your changes
        } catch (Exception e) {
            Log.d("exp",e.getMessage() );
        }
        LinkedList<String> Dataset = new LinkedList<String>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    StringBuilder stringBuilder = new StringBuilder();
                    for(int j=2 ; j<152 ; j++)  {
                        stringBuilder.append(cursor.getString(j));
                        stringBuilder.append(",");
                    }
                    String labelTemp = cursor.getString(1);
                    if(labelTemp.equals("running"))
                        stringBuilder.append("1\n"); //do not remove new line
                    else if(labelTemp.equals("walking"))
                        stringBuilder.append("2\n"); //do not remove new line
                    else
                        stringBuilder.append("3\n"); //do not remove new line
                    Dataset.add(stringBuilder.toString());
                } while (cursor.moveToNext());
            }
        }
        catch (Exception e)    {
            Toast.makeText(UI_Handler.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        db.endTransaction();
        return Dataset;
    }

    public String writetocsv(LinkedList<String> Dataset)    {
        File file = new File(FILE_PATH + File.separator + "traindata.csv");
        if(!file.exists()){
            try {
                file.createNewFile();
                FileWriter fileWriter  = new FileWriter(file);
                BufferedWriter bfWriter = new BufferedWriter(fileWriter);

                for(int i=0 ; i<Dataset.size() ; i++) {
                    bfWriter.write(Dataset.get(i));
                }
                bfWriter.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
    }
        return FILE_PATH + File.separator + "traindata.csv";
    }
}
