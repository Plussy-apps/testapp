package com.pyramidzzzbook;


import android.app.Application;

import com.pyramidzzzbook.utils.DataSource;
import com.pyramidzzzbook.utils.SharedPrefsHelper;
import com.onesignal.OneSignal;


public class GlobalClass extends Application {

    //ключ OneSignal
    private static final String ONESIGNAL_APP_ID = "4aa3a017-28b3-4ea8-9d03-703f65ac75f0";

    //advertising ID
    public static String adId;

    //AppsFlyer ID для сабок
    public static String appsflyerId;

    //ключ, который получаешь с AppsFlyer
    public static final String AF_DEV_KEY = "c6GoiT6pQtVw6TyimyRgCU";

    //преф на глобальном контексте
    public static SharedPrefsHelper sharedPrefsHelper;

    //имя кампании
    public static String campaign;

    //сабки
    public static String sub1;
    public static String sub2;
    public static String sub3;
    public static String sub4;

    //firebase remote key
    public static String REMOTE_KEY = "pyramidzzzbook";

    //глобальная переменная для первой ссылки, получаемой из remote config'a
    public static String remoteResponse;

    //переменная для чтения ссылки с ремоут конфига
    public static String inputLine;

    //переменная для конечной ссылки после добавления к ней сабок
    public static String finalInputLine;

    //сабки для получения данных с fb deeplink
    public static String sub5, sub9, sub10;

    //счётчик открытия ссылок
    public static Integer counter = 1;

    //класс помощник (туда вынесены все методы)
    public static DataSource dataSource;



    @Override
    public void onCreate() {
        super.onCreate();

        dataSource = new DataSource(getApplicationContext());

        //инициализируем префы на глобальном контексте
        sharedPrefsHelper = new SharedPrefsHelper(getApplicationContext());

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

    }



}
