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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.crypto.ExemptionMechanismException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


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
    Boolean downloadButtonPressed=false;

    Button buttonRun;
    Button buttonStop;
    Button buttonUpload;
    Button buttonDownload;

    EditText widgetPatientName;
    EditText widgetPatientID;
    EditText widgetPatientAge;
    RadioGroup widgetPatientSex;

    PatientInfo patientInfo;
    boolean P_Male = true;

    SQLiteDatabase db;
    public String TABLE = "dude"+System.currentTimeMillis();
    public static final String DATABASE_NAME = "group13";
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
        Intent intent = new Intent(this, AccelerometerService.class);
        startService(intent);
        accelerometerReceiver = new AccelerometerReceiver();

        init();

        widgetPatientName = (EditText) findViewById(R.id.PNameText);
        widgetPatientAge = (EditText) findViewById(R.id.PAgeText);
        widgetPatientID = (EditText) findViewById(R.id.PIDText);
        widgetPatientSex = (RadioGroup) findViewById(R.id.radioGroup);


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
        buttonStop.setEnabled(false);

        widgetPatientSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rBFemale) {
                    P_Male=false;
                }
                else {
                    P_Male=true;
                }
            }

        });

        buttonRun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //start
                if (widgetPatientName.getText().toString().matches("") || widgetPatientAge.getText().toString().matches("") || widgetPatientID.getText().toString().matches(""))    {
                    Toast.makeText(Assignment2.this, "Please complete all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(flag == null || !flag){
                    flag = true;

                    //disable relevant buttons
                    disableOrEnableButtons(true, true, false, false);

                    patientInfo = new PatientInfo(widgetPatientName.getText().toString(), widgetPatientAge.getText().toString(), widgetPatientID.getText().toString(), P_Male);
                    TABLE = patientInfo.table_name;
                    TABLE = TABLE.replace(" ", "_");
                    Toast.makeText(Assignment2.this, "Table Name: " + TABLE, Toast.LENGTH_SHORT).show();

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
                //disable relevant buttons
                enableAllButtons();

                try {
                    unregisterReceiver(accelerometerReceiver);
                }catch (Exception e)    {}
            }
        });


        buttonDownload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                disableAllButtons();
                downloadFileFromServer1("https://impact.asu.edu/CSE535Spring17Folder/group13", DATABASE_LOCATION, DATABASE_NAME);

                if (widgetPatientName.getText().toString().matches("") || widgetPatientAge.getText().toString().matches("") || widgetPatientID.getText().toString().matches(""))    {
                    Toast.makeText(Assignment2.this, "Download Complete\nBut Please fill all fields to show last 10 records and press DOWNLOAD again", Toast.LENGTH_LONG).show();
                }
                else {
                    //////
                }
            }
        });


        buttonUpload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Toast.makeText(Assignment2.this, "Upload Begins", Toast.LENGTH_SHORT).show();
                disableAllButtons();
                uploadFileToServer(DATABASE_LOCATION, "https://impact.asu.edu/CSE535Spring17Folder/UploadToServer.php", DATABASE_NAME);
            }
        });
    }

    protected void init(){
        String[] hlabels= new String[10];
        for (int i = 0; i < 10; i++) {
            int temp = (-4)*i + 18;         //to get in the range of 20 to -18
            hlabels[i]=String.valueOf(temp);
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
                    values[i] = Float.parseFloat(cursor.getString(1));
                    valuesy[i] = Float.parseFloat(cursor.getString(2));
                    valuesz[i] = Float.parseFloat(cursor.getString(3));
                    g.invalidate();
                    g.setValues(values, valuesy, valuesz);
                    i--;
                } while (cursor.moveToNext() && i >= 0);
            }
        }
        catch (Exception e)    {
            if(downloadButtonPressed) {
                Toast.makeText(Assignment2.this, "Download Complete\nBut no such person data recorded yet", Toast.LENGTH_LONG).show();
                g.invalidate();
                g.setValues(new float[10], new float[10], new float[10]);
                downloadButtonPressed=false;
            }
        }
    }

    public void downloadFileFromServer1(final String source, String dest, String fileName) {
        final DownloadTask downloadTask = new DownloadTask(Assignment2.this);
        downloadTask.execute("https://impact.asu.edu/CSE535Spring17Folder/group13", DATABASE_LOCATION, DATABASE_NAME);
    }

    public void uploadFileToServer(final String sourceFileUri, String strDestinationUri, String fileName) {
        final UploadTask uploadTask = new UploadTask(Assignment2.this);
        uploadTask.execute(sourceFileUri, strDestinationUri, fileName);
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {
        private Context context;
        //private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpsURLConnection connection = null;
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {return null;}
                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
            } };

            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());

                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpsURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                }
                File sourceFile = new File(FILE_PATH + File.separator + DATABASE_NAME);
                if(sourceFile.isFile())
                {
                    sourceFile.delete();
                }
                // display download percentage or -1
                int fileLength = connection.getContentLength();

                input = connection.getInputStream();
                output = new FileOutputStream(FILE_PATH + File.separator + DATABASE_NAME);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (Exception e) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null){
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_SHORT).show();
            }else{


                //Toast.makeText(context,"Database downloaded", Toast.LENGTH_SHORT).show();
                db = SQLiteDatabase.openOrCreateDatabase(DATABASE_LOCATION, null);
                db.beginTransaction();

                patientInfo = new PatientInfo(widgetPatientName.getText().toString(), widgetPatientAge.getText().toString(), widgetPatientID.getText().toString(), P_Male);
                TABLE = patientInfo.table_name;
                TABLE = TABLE.replace(" ", "_");
                downloadButtonPressed = true;
                getDataFromDatabase();
                db.endTransaction();
            }

            disableOrEnableButtons(true, false, true, true);
        }
    }

    private class UploadTask extends AsyncTask<String, Integer, String> {
        private Context context;
        //private PowerManager.WakeLock mWakeLock;

        public UploadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            //referred from: http://androidexample.com/Upload_File_To_Server_-_Android_Example/index.php?view=article_discription&aid=83&aaid=106
            HttpsURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(params[0]);
            int serverResponseCode=0;

            if (!sourceFile.isFile()) {
                Log.d("D", "file to upload not found");
                Toast.makeText(Assignment2.this, "File to upload not found. Please enter atleast one person's data", Toast.LENGTH_SHORT).show();
            }
            else {
                try {
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {return null;}
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
                    };


                    try {
                        SSLContext sc = SSLContext.getInstance("SSL");
                        sc.init(null, trustAllCerts, new SecureRandom());
                        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                    } catch (Exception e) {
                        Log.d("D", "SSL problem");
                    }
                    URL url = new URL(params[1]);
                    conn = (HttpsURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", params[2]);

                    dos = new DataOutputStream(conn.getOutputStream());
                    Log.d("D", "1");
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    Log.d("D", "2");
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + params[2] + "\"" + lineEnd);
                    Log.d("D", "3");
                    dos.writeBytes(lineEnd);
                    Log.d("D", "4");


                    bytesAvailable = fileInputStream.available();
                    Log.d("D", ""+bytesAvailable);
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null){
                Toast.makeText(context,"Upload error: "+result, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context,"Database uploaded", Toast.LENGTH_SHORT).show();
            }
            disableOrEnableButtons(true, false, true, true);
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