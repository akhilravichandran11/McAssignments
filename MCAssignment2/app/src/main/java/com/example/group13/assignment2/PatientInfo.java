package com.example.group13.assignment2;

/**
 * Created by ddeepak on 3/4/2017.
 */

public class PatientInfo {

    public int P_ID;
    public int P_Age;
    public String P_Name;
    public boolean P_Male;
    public String table_name;
    public StringBuilder sb;
    public long timestamp;
    public float x_value;
    public float y_value;
    public float z_value;

    public PatientInfo(String P_Name, int P_ID, int P_Age, boolean P_Male){
        sb=new StringBuilder();
        sb.append(P_Name);
        sb.append("_");
        sb.append(P_ID);
        sb.append("_");
        sb.append(P_Age);
        sb.append("_");
        if(P_Male) {
            sb.append("M");
        }
        else {
            sb.append("F");
        }
        table_name=sb.toString();

        //create sql database with name
        //create sql query table name here;
    }

    public void set_value(long timestamp, float x, float y, float z){
        this.timestamp=timestamp;
        this.x_value=x;
        this.y_value=y;
        this.z_value=z;
    }
}
