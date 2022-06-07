package com.websarva.wings.android.groupproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class SpotShow extends AppCompatActivity{

    //観光スポットをGoogleで検索

    public String CityName;

    public String CityWeather;

    public String URL = "https://www.google.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_show);

        //インテントオブジェクトを取得
        Intent intent = getIntent();

        CityName = intent.getStringExtra("etSearchWord3");
        CityWeather = intent.getStringExtra("weather");

        System.out.println(CityWeather);
        System.out.println(CityName);

        WebView myWebView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        switch (CityWeather) {
            case "雨":
            case "小雨":
            case "小雪":
            case "雷雨":
            case "雪":
            case "霧":
            case "弱いにわか雪":
            case "強い雨":
            case "大雨":
                URL = "https://www.google.com/search?q=" + CityName + "雨おすすめスポット";
                break;
            default:
                URL = URL = "https://www.google.com/search?q=" + CityName + "おすすめスポット";
                break;
        }
        myWebView.loadUrl(URL);

    }

}