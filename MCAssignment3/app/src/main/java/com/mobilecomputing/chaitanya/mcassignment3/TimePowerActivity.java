package com.mobilecomputing.chaitanya.mcassignment3;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;

/**
 * Created by SAHIL on 09-04-2017.
 */

public class TimePowerActivity extends AppCompatActivity {
    int timeUsedMobile, powerUsedMobile;
    int timeUsedServer, powerUsedServer;
    int zero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_power);

        zero = 0;
        Bundle bundle = getIntent().getExtras();
        timeUsedMobile = (int) bundle.getLong("timeUsed");
        powerUsedMobile = (int) bundle.getLong("powerUsed");

        /*
         * referred from: https://github.com/appsthatmatter/GraphView
         * referred from: http://www.android-graphview.org/points-graph/
         *
         */

        //graph for mobile stats:
        GraphView graphMobile = (GraphView) findViewById(R.id.mobileGraph);
        PointsGraphSeries<DataPoint> pointsMobile = new PointsGraphSeries<>(new DataPoint[] {
                new DataPoint(timeUsedMobile,powerUsedMobile),
                new DataPoint(zero, zero),
        });
        graphMobile.addSeries(pointsMobile);
        pointsMobile.setShape(PointsGraphSeries.Shape.POINT);
        pointsMobile.setColor(Color.RED);

        //graph for server stats:
        timeUsedServer = 1;
        powerUsedServer = 1;
        GraphView graphServer = (GraphView) findViewById(R.id.serverGraph);
        PointsGraphSeries<DataPoint> pointsServer = new PointsGraphSeries<>(new DataPoint[] {
                new DataPoint(0, -2),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graphServer.addSeries(pointsServer);
        pointsServer.setShape(PointsGraphSeries.Shape.POINT);
        pointsServer.setColor(Color.BLUE);
    }
}
