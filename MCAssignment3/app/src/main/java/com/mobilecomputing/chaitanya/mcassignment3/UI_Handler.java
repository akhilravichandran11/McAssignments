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

import java.io.File;

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

        try {
            if (cursor.moveToFirst()) {
                do {
//                    values[i] = Float.parseFloat(cursor.getString(1));
//                    valuesy[i] = Float.parseFloat(cursor.getString(2));
//                    valuesz[i] = Float.parseFloat(cursor.getString(3));
//                    g.invalidate();
//                    g.setValues(values, valuesy, valuesz);
                    i--;
                } while (cursor.moveToNext() && i >= 0);
            }
        }
        catch (Exception e)    {
            Toast.makeText(UI_Handler.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
