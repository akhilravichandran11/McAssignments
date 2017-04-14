package com.mobilecomputing.chaitanya.mcassignment3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Calibration extends AppCompatActivity {

    SQLiteDatabase db;
    public String TABLE = "training";
    public String activity = "eating";
    public static final String DATABASE_NAME = "group13";
    public static final String FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "Mydata";
    public static final String DATABASE_LOCATION = FILE_PATH + File.separator + DATABASE_NAME;
    AccelerometerReceiver accelerometerReceiver;
    Button run;
    Button walk;
    Button eat;
    boolean running1 = true;
    boolean walking1 = true;
    boolean eating1 = true;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        try{
            File file = new File(DATABASE_LOCATION);
            File folder = new File(FILE_PATH);
            if (!folder.exists()) {
                folder.mkdir();
            }
//            if (file.exists() && !file.isDirectory()){
                makeDB(DATABASE_LOCATION, TABLE);
//            } else {
//                Toast.makeText(Calibration.this, "No file for database", Toast.LENGTH_LONG).show();
//            }
        }
        catch (SQLException e){
            Toast.makeText(Calibration.this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("exp",e.getMessage() );
        }
    }

    @Override
    public void onBackPressed() {
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void makeDB(String DATABASE_LOCATION, String TABLE){
        db = SQLiteDatabase.openOrCreateDatabase(DATABASE_LOCATION, null);
        db.beginTransaction();
        db.execSQL("DROP TABLE training");
        Toast.makeText(Calibration.this, "Press activity button to start calibrating Database", Toast.LENGTH_LONG).show();

        String createQuery;
        createQuery = "create table if not exists " + TABLE + " ("
                + " created_at DATETIME DEFAULT CURRENT_TIMESTAMP, label, ";
        for(int i = 1; i <= 150; i++){
            createQuery += " val" + Integer.toString(i) + " float";
            if(i != 150){
                createQuery += ", ";
            }
        }
        createQuery += ");";
        db.execSQL(createQuery);

        accelerometerReceiver = new AccelerometerReceiver();
        Intent intent = new Intent(Calibration.this, AccelerometerService.class);
        startService(intent);

        run = (Button)findViewById(R.id.run);
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity = "running";
                running1 = false;
                disableAllButtons();
                Toast.makeText(Calibration.this, "Calibrating Run", Toast.LENGTH_LONG).show();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(AccelerometerService.ACCELEROMETER_INTENET_ACTION);
                registerReceiver(accelerometerReceiver, intentFilter);
            }
        });

        walk = (Button)findViewById(R.id.walk);
        walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity = "walking";
                walking1 = false;
                disableAllButtons();
                Toast.makeText(Calibration.this, "Calibrating Walk", Toast.LENGTH_LONG).show();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(AccelerometerService.ACCELEROMETER_INTENET_ACTION);
                registerReceiver(accelerometerReceiver, intentFilter);
            }
        });

        eat = (Button)findViewById(R.id.eat);
        eat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity = "eating";
                eating1 = false;
                disableAllButtons();
                Toast.makeText(Calibration.this, "Calibrating Eat", Toast.LENGTH_LONG).show();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(AccelerometerService.ACCELEROMETER_INTENET_ACTION);
                registerReceiver(accelerometerReceiver, intentFilter);
            }
        });

    }

    private class AccelerometerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //get list of 150 elements from accelerometer

            ArrayList<Float> valueList = (ArrayList<Float>) intent.getSerializableExtra("value_list");
            try {
                String insertQuery;
                insertQuery = "insert into " + TABLE + " (label, ";
                for(int i = 1; i <= 150; i++){
                    insertQuery += " val" + Integer.toString(i);
                    if(i != 150){
                        insertQuery += ", ";
                    }
                }
                insertQuery += ") values ('"+activity+"', ";
                for (int i = 0; i <= 149; i++) {
                    insertQuery += "'"+ valueList.get(i) + "'";
                    if(i != 149){
                        insertQuery += ", ";
                    }
                }
                insertQuery += " );";
                db.execSQL(insertQuery);
                counter = counter + 1;
                if(counter == 20){
                    counter = 0;
                    enableButtons();
                }
                Toast.makeText(Calibration.this, Integer.toString(counter), Toast.LENGTH_LONG).show();
                //db.setTransactionSuccessful();
            }
            catch (SQLiteException e) {

            }
        }
    }

    private void disableAllButtons()
    {
        run.setEnabled(false);
        walk.setEnabled(false);
        eat.setEnabled(false);
    }

    private void enableButtons(){
        unregisterReceiver(accelerometerReceiver);
        if(!running1 && !walking1 && !eating1){
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
            Intent i = new Intent(Calibration.this, UI_Handler.class);
            startActivity(i);
        }
        run.setEnabled(running1);
        walk.setEnabled(walking1);
        eat.setEnabled(eating1);
    }
}
