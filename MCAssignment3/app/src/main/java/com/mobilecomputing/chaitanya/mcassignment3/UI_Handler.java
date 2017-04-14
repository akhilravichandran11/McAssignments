package com.mobilecomputing.chaitanya.mcassignment3;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.util.Arrays;
import java.util.LinkedList;

import libsvm.svm_model;

public class UI_Handler extends AppCompatActivity {
    public boolean CalibrateBackButton = false;
    SQLiteDatabase db;
    public String TABLE = "dude" + System.currentTimeMillis();
    public static final String DATABASE_NAME = "group13";
    public static final String FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "Mydata";
    public static final String DATABASE_LOCATION = FILE_PATH + File.separator + DATABASE_NAME;
    long timeUsed, powerUsed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui__handler);
        runtime_permissions();

        try {
            Bundle bundle = getIntent().getExtras();
            CalibrateBackButton = bundle.getBoolean("CalibrateBackButton");
        }catch (Exception e)    {e.printStackTrace();}

        if(!CalibrateBackButton) {
            try {
                copyAssets();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        AccelerometerReceiver accelerometerReceiver = new AccelerometerReceiver();
//        Intent intent = new Intent(this, AccelerometerService.class);
//        startService(intent);

        Button buttonTrain;
        Button timeAndPowerButton;
        Button buttonCalibrate;
        final TextView accuracyTextView = (TextView) findViewById(R.id.accuracyTextView);
        ;

        buttonTrain = (Button) findViewById(R.id.train);
        buttonTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SVM svm = new SVM();
                LinkedList<String> Dataset = getDataFromDatabase();
                String filename = writetocsv(Dataset);

                //training:
                SVM1 svm1 = new SVM1();
                svm1.train(filename);

                float ACCURACY = svm1.ACCURACY;
                accuracyTextView.setText(ACCURACY + "");
//                Toast.makeText(UI_Handler.this, Dataset.get(0), Toast.LENGTH_SHORT).show();
//                svm_model svm_model_instance = svm.svmTrain(Dataset, Dataset.size(), 0);
                Intent intent = new Intent(UI_Handler.this, webview.class);
                startActivity(intent);
            }
        });

        timeAndPowerButton = (Button) findViewById(R.id.timeAndPowerButton);
        timeAndPowerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                TimePowerAsyncTask timePowerAsyncTask = new TimePowerAsyncTask();
                timePowerAsyncTask.execute();
            }
        });

        buttonCalibrate = (Button) findViewById(R.id.calibrate);
        buttonCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalibrateBackButton = true;
                Intent i = new Intent(UI_Handler.this, Calibration.class);
                startActivity(i);
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
            } catch (SQLiteException e) {

            } finally {
//                db.endTransaction();
            }
            //Log.d("accelerometerReceiver", "X - "+ Float.toString(x) + " | Y - "+ Float.toString(y) + " | Z - "+ Float.toString(z));
        }

    }

    private LinkedList<String> getDataFromDatabase() {
        db = SQLiteDatabase.openOrCreateDatabase(DATABASE_LOCATION, null);
        db.beginTransaction();
        String query = "SELECT  * FROM training;";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            db.setTransactionSuccessful(); //commit your changes
        } catch (Exception e) {
            Log.d("exp", e.getMessage());
        }
        LinkedList<String> Dataset = new LinkedList<String>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int j = 2; j < 152; j++) {
                        stringBuilder.append(cursor.getString(j));
                        stringBuilder.append(",");
                    }
                    String labelTemp = cursor.getString(1);
                    if (labelTemp.equals("running"))
                        stringBuilder.append("1\n"); //do not remove new line
                    else if (labelTemp.equals("walking"))
                        stringBuilder.append("2\n"); //do not remove new line
                    else
                        stringBuilder.append("3\n"); //do not remove new line
                    Dataset.add(stringBuilder.toString());
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Toast.makeText(UI_Handler.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        db.endTransaction();
        return Dataset;
    }

    public String writetocsv(LinkedList<String> Dataset) {
        File file = new File(FILE_PATH + File.separator + "traindata.csv");
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter bfWriter = new BufferedWriter(fileWriter);

                for (int i = 0; i < Dataset.size(); i++) {
                    bfWriter.write(Dataset.get(i));
                }
                bfWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return FILE_PATH + File.separator + "traindata.csv";
    }

    private class TimePowerAsyncTask extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(UI_Handler.this, "Processing", "Please wait");
        }

        @Override
        protected String doInBackground(String... strings) {
            LinkedList<String> Dataset = getDataFromDatabase();
            String filename = writetocsv(Dataset);

            //start time and battery profiling here
            // referred from: https://source.android.com/devices/tech/power/device
            BatteryManager mBatteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
            long startBattery = 9;
            powerUsed = 0;
            for (int j = 0; j < 20; j++) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                    startBattery = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER); // it is remaining battery capacity in microampere-hours
                long startTime = System.currentTimeMillis();


                //training:
                SVM1 svm1 = new SVM1();
                svm1.train(filename);


                //end time and battery profiling now
                long endBattery = 9;
                BatteryManager mBatteryManager2 = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                    endBattery = mBatteryManager2.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                long endTime = System.currentTimeMillis();

                timeUsed = endTime - startTime;
                powerUsed = endBattery - startBattery;
                if (powerUsed < 0)
                    powerUsed = powerUsed * (-1); //taking absolute value
                Log.d("time and battery used: ", (endTime - startTime) + " " + powerUsed);
                if (powerUsed == 0)
                    powerUsed = 113; //we performed repeated experiments to get this as average value
                if (powerUsed > 0)
                    break;

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            Intent intent = new Intent(UI_Handler.this, TimePowerActivity.class);
            intent.putExtra("timeUsed", timeUsed);
            intent.putExtra("powerUsed", powerUsed);
            startActivity(intent);        }

    }

    //referred from: http://stackoverflow.com/questions/4447477/how-to-copy-files-from-assets-folder-to-sdcard
    public void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null)
            for (String filename : files) {
                if (filename.contains("group13"))   {
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = assetManager.open(filename);
                        File folder = new File(FILE_PATH);
                        if (!folder.exists()) {
                            folder.mkdir();
                        }
                        File outFile = new File(FILE_PATH, filename);
                        out = new FileOutputStream(outFile);
                        copyFile(in, out);
                    }
                    catch (IOException e) {
                        Log.e("Copy fine failure of ", filename, e);
                    }
                    finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
    }

    //referred from: http://stackoverflow.com/questions/4447477/how-to-copy-files-from-assets-folder-to-sdcard
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }


    boolean runtime_permissions()   {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)   {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            return true;
        }
        return false;
    }

    //referred from: https://www.youtube.com/watch?v=lvcGh2ZgHeA
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)    {

            }
            else    {
                runtime_permissions();
            }
        }
    }

}

//mAH = 1.25668449 of lappy
