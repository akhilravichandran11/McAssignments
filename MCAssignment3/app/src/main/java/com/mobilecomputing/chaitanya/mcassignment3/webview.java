package com.mobilecomputing.chaitanya.mcassignment3;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

public class webview extends AppCompatActivity {

    SQLiteDatabase db;
    public String TABLE = "dude" + System.currentTimeMillis();
    public static final String DATABASE_NAME = "group13";
    public static final String FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "Mydata";
    public static final String DATABASE_LOCATION = FILE_PATH + File.separator + DATABASE_NAME;
    WebView webview;
    public String[][] walkingArray, runningArray, eatingArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

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

                getDataFromDatabase();

                String walkingtext = Arrays.deepToString(walkingArray);
                String runningtext = Arrays.deepToString(runningArray);
                String eatingtext = Arrays.deepToString(eatingArray);

                Log.d("Running", runningtext);
                Log.d("Walking", walkingtext);
                Log.d("Eating", eatingtext);

                webview.loadUrl("javascript:showGraph(" + walkingtext + ", " + runningtext + ", " +eatingtext + ")");
            }
        });
        webview.loadUrl("file:///android_asset/html/graph.html");

    }
    private void getDataFromDatabase() {
        walkingArray = new String[1000][3];
        runningArray = new String[1000][3];
        eatingArray = new String[1000][3];
        int run=0, walk=0, eat=0;
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
        try {
            if (cursor.moveToFirst()) {
                do {
                    if(cursor.getString(1).equals("walking"))   {
                        for(int m=0; m<50; m++){
                            walkingArray[walk][0] = cursor.getString((3 * m) + 2);
                            walkingArray[walk][1] = cursor.getString((3 * m) + 3);
                            walkingArray[walk][2] = cursor.getString((3 * m) + 4);
                            walk = walk + 1;
                        }
                    }
                    if(cursor.getString(1).equals("running"))   {
                        for(int m=0; m<50; m++){
                            runningArray[run][0] = cursor.getString((3 * m) + 2);
                            runningArray[run][1] = cursor.getString((3 * m) + 3);
                            runningArray[run][2] = cursor.getString((3 * m) + 4);
                            run = run + 1;
                        }
                    }
                    if(cursor.getString(1).equals("eating"))   {
                        for(int m=0; m<50; m++){
                            eatingArray[eat][0] = cursor.getString((3 * m) + 2);
                            eatingArray[eat][1] = cursor.getString((3 * m) + 3);
                            eatingArray[eat][2] = cursor.getString((3 * m) + 4);
                            eat = eat + 1;
                        }
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
//            Toast.makeText(UI_Handler.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        db.endTransaction();
        return;
    }
}
