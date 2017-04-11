package com.mobilecomputing.chaitanya.mcassignment3;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.util.Arrays;
import java.util.LinkedList;

import libsvm.svm_model;

public class UI_Handler extends AppCompatActivity {

    SQLiteDatabase db;
    public String TABLE = "dude" + System.currentTimeMillis();
    public static final String DATABASE_NAME = "group13";
    public static final String FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "Mydata";
    public static final String DATABASE_LOCATION = FILE_PATH + File.separator + DATABASE_NAME;
    long timeUsed, powerUsed;
    WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui__handler);




        // Prepare webview: add zoom controls and start zoomed out
        webview = (WebView) findViewById(R.id.webview);
        final WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setUseWideViewPort(true);
        webview.setWebChromeClient(new WebChromeClient());
        webview.setInitialScale(1);

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                int[][] walking = {{5, 4, 2}, {9, 6, 1}, {2, 7, 3}, {4, 5, 4}, {6, 8, 1}, {3, 4, 0}};
                String text1 = Arrays.toString(walking);

                int[][] running = {{6, 2, 5}, {0, 4, 9}, {3, 5, 9}, {6, 9, 1}, {1, 9, 2}};
                String text2 = Arrays.toString(running);

                int[][] eating = {{2, 2, 6}, {5, 1, 2}, {9, 9, 7}, {6, 9, 9}, {8, 4, 3}, {4, 1, 7}};
                String text3 = Arrays.toString(eating);

                webview.loadUrl("javascript:showGraph(" +
                         text1 + ", " + text2 + ", "+ text3 + ")");
            }
        });
        // Load base html from the assets directory
        webview.loadUrl("file:///android_asset/html/graph.html");





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

}

//mAH = 1.25668449 of lappy
