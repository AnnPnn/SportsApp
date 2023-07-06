package com.anpn.sportsapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;


import com.anpn.sportsapp.todos.TodoActivity;
import com.google.firebase.BuildConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Locale;
import java.util.Objects;


/**
 * Created by Anikeeva on 27.06.2023.
 */
public class MainActivity extends AppCompatActivity {

    public SharedPreferences sP;//локальное хранилище данных
    private static final String APP_PREFERENCES_NAME = "url";
    private String url;
    private static final String TAG = "MyTag";
    private WebView webView;
    FirebaseRemoteConfig mFirebaseRemoteConfig;
    LinearLayout networkErrorLayout;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;


    @SuppressLint({"NonConstantResourceId", "MissingInflatedId", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Objects.requireNonNull(getSupportActionBar()).hide();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sP = getSharedPreferences("App_Preference", MODE_PRIVATE);

        int time;
        if (BuildConfig.DEBUG) {
            time = 0;
        } else {
            time = 3600;
        }
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(time)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        networkErrorLayout = findViewById(R.id.networkErrorLayout);


        //инициализация и настройка компонента webView
        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(false);
        webSettings.setAllowContentAccess(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            /**
             * Загрузка изображений из галереи
             */
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                }

                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    return false;
                }
                return true;
            }
        });

        if (sP.contains(APP_PREFERENCES_NAME)) {
            //ссылка сохранена
            //получаем значение из локальных настроек
            url = sP.getString(APP_PREFERENCES_NAME, "");

            //Есть интернет?
            if (isNetworkConnected(getApplicationContext())) {
                //Есть
                if (savedInstanceState != null) {
                    webView.restoreState(savedInstanceState);
                } else {
                    //Загружаем ссылку
                    webView.loadUrl(url);

                }
            } else {
                //Нет
                networkErrorLayout.setVisibility(View.VISIBLE);
            }

        } else {
            //ссылка не сохранена
            getData(savedInstanceState);

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_SELECT_FILE) {
            if (uploadMessage == null)
                return;
            uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
            uploadMessage = null;
        }
    }

    /**
     * Получение ссылки из remote config
     */
    private void getData(Bundle savedInstanceState) {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        //Удалось получить знаенчие
                        boolean updated = task.getResult();
                        Log.d(TAG, "Config params updated: " + updated);
                        //получаем значение из remote config
                        url = mFirebaseRemoteConfig.getString(APP_PREFERENCES_NAME);

                        if (!Objects.equals(url, "") || checkIsEmu()) {
                            //ссылка не пустая
                            //проверка на эмуляторе прошла успешно
                            saveUrl();
                            if (savedInstanceState != null) {
                                webView.restoreState(savedInstanceState);
                            } else {
                                //Загружаем ссылку
                                webView.loadUrl(url);
                            }
                        } else {
                            startStub();
                        }
                    } else {
                        //Не удалось получить значение
                        networkErrorLayout.setVisibility(View.VISIBLE);
                    }
                });

    }

    /**
     * Заглушка
     */
    private void startStub() {
        startActivity(new Intent(getApplicationContext(), TodoActivity.class));
        overridePendingTransition(0, 0);

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * Сохранение ссылки в локальное хранилище
     */
    private void saveUrl() {
        //сохраняем значение ссылки локально
        SharedPreferences.Editor ed = sP.edit();
        ed.putString(APP_PREFERENCES_NAME, url);
        ed.apply();
    }

    public static boolean isNetworkConnected(Context mContext) {
        if (mContext == null) return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final Network network = connectivityManager.getActiveNetwork();
                if (network != null) {
                    final NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(network);

                    return (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
                }
            } else {
                NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
                for (NetworkInfo tempNetworkInfo : networkInfo) {
                    if (tempNetworkInfo.isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Проверка на эмуляторе
     */
    private boolean checkIsEmu() {
        if (BuildConfig.DEBUG) return false;
        String phoneModel = Build.MODEL;
        String buildProduct = Build.PRODUCT;
        String buildHardware = Build.HARDWARE;
        String brand = Build.BRAND;
        return (Build.FINGERPRINT.startsWith("generic")
                || phoneModel.contains("google_sdk")
                || phoneModel.toLowerCase(Locale.getDefault()).contains("droid4x")
                || phoneModel.contains("Emulator")
                || phoneModel.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")

                || buildHardware.equals("goldfish")
                || brand.contains("google")
                || buildHardware.equals("vbox86")
                || buildProduct.equals("sdk")
                || buildProduct.equals("google_sdk")
                || buildProduct.equals("sdk_x86")
                || buildProduct.equals("vbox86p")
                || Build.BOARD.toLowerCase(Locale.getDefault()).contains("nox")
                || Build.BOOTLOADER.toLowerCase(Locale.getDefault()).contains("nox")
                || buildHardware.toLowerCase(Locale.getDefault()).contains("nox")
                || buildProduct.toLowerCase(Locale.getDefault()).contains("nox"))
                || (brand.startsWith("generic") && Build.DEVICE.startsWith("generic"));
    }


    /**
     * Настройка стрелки назад для webView
     */
    @Override
    public void onBackPressed() {
        if (webView.isFocused() && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
            finish();
        }
    }

}