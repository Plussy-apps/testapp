package com.pyramidzzzbook.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsHelper {
    private static SharedPreferences settings;
    private static SharedPreferences.Editor editor;


    @SuppressLint("CommitPrefEdits")
    public SharedPrefsHelper(Context context) {
        settings = context.getSharedPreferences("LOCAL", 0);
        editor = settings.edit();
    }

    //получаем последнюю ссылку
    public String getPrefsUrl() {
        return settings.getString("url", "");
    }

    //сохраняем последнюю ссылку
    public void setPrefsUrl(String string) {
        editor.putString("url", string);
        editor.apply();
    }

    //получаем последнюю ссылку
    public String getPrefsSafeLink() {
        return settings.getString("safe", "");
    }

    //сохраняем последнюю ссылку
    public void setPrefsSafeLink(String string) {
        editor.putString("safe", string);
        editor.apply();
    }

    //ставим значение первого запуска в WebActivity
    public void setFirstLaunch() {
        editor.putBoolean("first", true);
        editor.apply();
    }

    //получаем значение первого запуска в WebActivity
    public Boolean getFirstLaunch() {
        return settings.getBoolean("first", false);
    }

    //получаем значение в проверку в SplashScreen
    public Boolean getConfigNotPassed(){
        return settings.getBoolean("configPass", false);
    }

    //если не прошёл проверку, то ставим в SplashScreen = true
    public  void setConfigNotPassed(Boolean bool){
        editor.putBoolean("configPass", bool);
        editor.apply();
    }

    //сеттим кампанию в хранилище
    public void setCampaignName(String campaign){
        editor.putString("campaign", campaign);
        editor.apply();
    }

    //получаем кампанию из хранилища
    public String getCampaign(){
        return settings.getString("campaign", "");
    }

    //facebook sub5
    public void setSub5(String string){
        editor.putString("sub5", string);
        editor.apply();
    }

    //facebook sub5
    public String getSub5(){
        return settings.getString("sub5", "");
    }

    //facebook sub9
    public void setSub9(String string){
        editor.putString("sub9", string);
        editor.apply();
    }

    //facebook sub9
    public String getSub9(){
        return settings.getString("sub9", "");
    }

    //facebook sub10
    public void setSub10(String string){
        editor.putString("sub10", string);
        editor.apply();
    }

    //facebook sub10
    public String getSub10(){
        return settings.getString("sub10", "");
    }

    //проверяем на наличие успешно полученного диплинка от фб
    public Boolean ifContains(){
        if (settings.contains("sub5")){
            return true;
        } else {
            return false;
        }
    }


    //ставим значение первого получения ремоут-ссылки
    public void setFirstLaunchRemote() {
        editor.putBoolean("remote", true);
        editor.apply();
    }

    //получаем значение первого получения ремоут-ссылки
    public Boolean getFirstLaunchRemote() {
        return settings.getBoolean("remote", false);
    }

    //сеттим ремоут ссылку в префы
    public void setResponse(String string){
        editor.putString("response", string);
        editor.apply();
    }

    //получаем ремоут ссылку с префов
    public String getResponse(){
        return settings.getString("response", "");
    }
}
