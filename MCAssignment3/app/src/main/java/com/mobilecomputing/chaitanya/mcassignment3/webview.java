package com.mobilecomputing.chaitanya.mcassignment3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class webview extends AppCompatActivity {

    WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

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

                Log.d("Walking", text1);
                Log.d("Running", text1);
                Log.d("Eating", text1);
                webview.loadUrl("javascript:showGraph()");
            }
        });
        // Load base html from the assets directory
        webview.loadUrl("file:///android_asset/html/graph.html");

    }
}
