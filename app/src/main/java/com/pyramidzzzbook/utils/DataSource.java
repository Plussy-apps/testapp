package com.pyramidzzzbook.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import static com.pyramidzzzbook.GlobalClass.adId;

public class DataSource {
    private Context context;

    //дефолтный конструктор, в который мы прокидываем контекст
    public DataSource(Context context) {
        this.context = context;
    }



    //получаем AppsFlyer Id
    public String getAppsFlyerUID(){
        return AppsFlyerLib.getInstance().getAppsFlyerUID(context);
    }

    //получаем рекламный идентификатор
    public void getID() {
        AsyncTask.execute(() -> {
            try {
                AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                adId = adInfo != null ? adInfo.getId() : null;
                Log.d("LOG_TAG", adId);
                assert adId != null;

            } catch (Exception ignored) {

            }
        });
    }

    //проверяем, есть ли в отпечатке слово 'generic'
    public Boolean checkOnGeneric(){
        return Build.FINGERPRINT.contains("generic");
    }


    //проверяем, есть ли интернет
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //выключаем ограничение по основному треду
    public void offStrictPolicy(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }


}
