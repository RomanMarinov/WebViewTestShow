package com.dev_marinov.webviewtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.onesignal.OneSignal;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String ONESIGNAL_APP_ID = "399e4775-3b2c-44f5-965b-bc23963a9451";
    private WebView webview;
    LinearLayout ll_frag_form_reg, ll_frag_enter;
    String[] arURLloadsetting;
    String url_loadsetting = "";
    int num;
    int count_load =0;
    int flag = 0;
    String url_all = "";
    WebBackForwardList mWebBackForwardList;
    SharedPreferences sharedPreferences;
    String historyUrl = "";
    private boolean clearHistory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CookieManager.getInstance().setAcceptCookie(true);
        ll_frag_form_reg = findViewById(R.id.ll_frag_form_reg);
        ll_frag_enter = findViewById(R.id.ll_frag_enter);

        webview = findViewById(R.id.webview);
        // отключение toolbar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

       //  сохранение куки
        CookieManager.getInstance().setAcceptThirdPartyCookies(webview, true);
        CookieManager.getInstance().setCookie("https://yandex.ru",  loadSettingString("cookies", ""));

        // включение настроек для лучшей работы webview
        WebSettings webSettings = webview.getSettings();
      //  webSettings.setJavaScriptEnabled(true); // отключил, постоянно запрашивает регистрацию почты и гео
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDisplayZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettings.setSafeBrowsingEnabled(true);
        }
        webSettings.setAppCacheEnabled(true);

        //update_list();
        if(flag == 0)
        {
        mySetWebViewClient();
        myLoadSaveList();
        }
        else if (flag == 1)

// метод internet резалирует в UI данные метода isOnline
        internet();
//  Метод для доставки Push-уведомлений и автоматизации
        myOnsignal();
    }

    // метод получения от сервера ссылки, либо yandex.ru (откроется webview), либо без yandex.ru (откроется фрагмент)
    public  void update_list()
    {
        Log.e("MAIN_ACT ","-сработал метод update_list()");
        runOnUiThread(new Runnable() { // главный поток
            @Override
            public void run() {
                AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
                asyncHttpClient.setConnectTimeout(2000);
                asyncHttpClient.get("https://dev-marinov.ru/server/WebViewTestServer/WebViewTestServer.php", null,
                        new TextHttpResponseHandler() {
                            @Override
                            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers,
                                                  String responseString, Throwable throwable) {
                                transitionToFragRag(); // переход во FRAGREG
                            }
                            @Override
                            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers,
                                                  String responseString) {
                                try {
                                    JSONObject jsonObject = new JSONObject(responseString);
                                    Log.e("MAIN_ACT", "jsonObject" + jsonObject.toString());
                                    // если в приходящем от сервера json есть значение http, т.е. ссылка
                                    // indexOf() возвращает индекс первого вхождения указанного значения в строковый объект String
                                    // Метод indexOf() ищет в строке заданный символ или строку, и их возвращает индекс
                                    // (т. е. ... возвращает индекс, под которым символ или строка первый раз появляется в строке;
                                    // возвращает (-1) если символ или строка не найдены
                                    if (jsonObject.getString("url").indexOf("http") !=-1) {
                                        Log.e("MAIN_ACT", "-jsonObject.getString-" + (jsonObject.getString("url").indexOf("http") != -1));
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    flag = 0;

                                                    // в webview запишется только https://yandex.ru
                                                    webview.loadUrl(jsonObject.getString("url"));
                                                    Log.e("MAIN_ACT ","-пришла ссылка https://yandex.ru в update_list");

                                                } catch (Exception e) {
                                                    Log.e("MAIN_ACT", "-try-catch1-" + e);
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        Log.e("MAIN_ACT","ДОЛЖЕН ПЕРЕХОД-");

                                        flag = 1; // 1 - значит в данных от сервера нет сайта yandex.ru

                                        transitionToFragRag(); // переход во FRAGREG
                                    }

                                }
                                catch (Exception e)
                                {
                                        transitionToFragRag(); // переход во FRAGREG
                                }
                            }
                        });


            }
        });

    }

    public void mySetWebViewClient()
    {
        Log.e("MAIN_ACT ","-сработал mySetWebViewClient");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    webview.setWebViewClient(new WebViewClient(){ // срабатывает при нажатии вперед
                        // метод может выполниться нескольк раз(рекланый аннер, или скрип открыть новое окно)
                        // поэтому надо реализовать выполнение одного раза
                        @Override
                        public void onPageFinished(WebView view, String url) {

                            if (clearHistory)
                            {
                                clearHistory = false;
                                webview.clearHistory();
                            }
                            super.onPageFinished(view, url);
                        }

                        // метод выаолнеия до загрузки странцы ,т.е в момент клика
                        // если передать tru то загрузка прервется
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                            Log.e("url","url="+request.getUrl().toString());
                            count_load++;

                            if(request.getUrl().toString() != null)
                            {
                                {
                                    mWebBackForwardList = webview.copyBackForwardList(); // запись в массив всех ссылок
                                    url_all = "";

                                    for (int i = 0; i < mWebBackForwardList.getSize(); i++) { // перебор массива
                                        historyUrl = mWebBackForwardList.getItemAtIndex(i).getUrl(); //
                                        // запись каждой новой url в массив истории при переходе вперед
                                        // если все i не равны индексу последней ссылки, то не записывать ";"
                                        if (i != (mWebBackForwardList.getSize() - 1)) {
                                            url_all = url_all + historyUrl + ";"; //9
                                        }
                                        else
                                        {
                                            url_all = url_all + historyUrl; //1
                                        }
                                    }

                                    url_all = url_all +  ";"+request.getUrl().toString();

                                    // искоючить
                                    url_all = url_all.replace("about:blank;","");
                                    Log.e("MAIN_ACT ","-сработал mySetWebViewClient и url_all записалось -" + url_all);

                                    saveSettingString("url_all", url_all); // сохраняем историю ссылок в sharpref
                                    saveSettingString("cookie", CookieManager.getInstance().getCookie("https://yandex.ru"));
                                }
                            }

                            return false; // разрешаем дальше исполняться коду
                        }

                        @Override
                        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                            Log.e("MAIN_ACT","view = " + view + " =description= " + description + " =failingUrl= " + failingUrl);
                            super.onReceivedError(view, errorCode, description, failingUrl);
                        }
                    });
                }
                catch (Exception e)
                {
                    Log.e("MAIN_ACT ","-try-catch -" + e);
                }
            }
        });
    }

     public void clearHistory()
     {
        clearHistory = true;
     }

    @Override
    public void onBackPressed()
    {
        if(flag == 0)
        {
            String url_allSH = loadSettingString("url_all", "");
            // вернет строку через разделитель и запишем в новый массив
            String[] url_all_array =url_allSH.split(";");
            count_load = url_all_array.length; // передадим в счетчик значение длины массив ссылок

            if (count_load == 1) // сработает когда мы попытаемся выйти из приложения со стартовой страницы
            {
                Log.e("MAIN_ACT","-onBackPressed()- myAlertDialog(); = " + count_load);
                myAlertDialog();
            }
            else
            {
                Log.e("MAIN_ACT","-onBackPressed()-ELSE count_load() ДО; = " + count_load);
                count_load--; // уменьшаем счетчик
                if (url_all_array.length>=1) { // передаем в массив предпоследнюю ссылку
                    Log.e("MAIN_ACT", "-onBackPressed()-ELSE url_allSH; = " + url_allSH);
                    webview.loadUrl(url_all_array[url_all_array.length - 2]);

                    int count = 0;
                    // перебираем массив ссылок для получения значения count без последней ссылки
                    for (int i = 0; i < url_all_array.length - 1; i++) {
                        if(i != url_all_array.length - 1)
                        {
                            count++;
                        }
                    }

                    String[] newurl_allSH = new String[count]; // создаем пустой масив
                    // копируем старый массив вновый
                    System.arraycopy(url_all_array, 0, newurl_allSH, 0, url_all_array.length-1);
                    Log.e("MAIN_ACT", "-onBackPressed()-ELSE newurl_allSHДО; = " + newurl_allSH.length);

                    String arrayString = "";
                    // записываем в новую переменную arrayString все ссылки из массива newurl_allSH
                    for (int i = 0; i < newurl_allSH.length; i++) {
                        if(i != newurl_allSH.length - 1) { arrayString = arrayString + newurl_allSH[i] + ";"; }
                        else { arrayString = arrayString + newurl_allSH[i]; }
                    }
                    saveSettingString("url_all", arrayString);
                    Log.e("MAIN_ACT", "-onBackPressed()-ELSE arrayString; = " + arrayString);
                    Log.e("MAIN_ACT", "-onBackPressed()-ELSE newurl_allSHПОСЛЕ; = " + newurl_allSH.length);
                }
                //      webview.goBack(); // закрыл потому что goback удалять текущию ссылку
            }
            //   super.onBackPressed(); // закрыд метод чтоб он не выполнлся по умолчанию, а выполнялся как я сам переопередлил
        }
        else  if (flag == 1) // если мы не получили ссылку с yandex.ru с сервера с http и перешли работать во фрагменты
        {
            // как только будет ноль (последний экран) выполниться else
            if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
                super.onBackPressed();
                Log.e("MAIN_ACT","getFragmentManager().getBackStackEntryCount()== " + getSupportFragmentManager().getBackStackEntryCount() );
            }
            else {
                getSupportFragmentManager().popBackStack();
                myAlertDialog();
            }
        }

    }

    // выгрузка из sharedpref массива ссылок 1 раз когда запускаем приложение
    // в webview загрузиться такая история переходов по ссылкам, какая сохранилась в sharedpref
    public void myLoadSaveList()
    {
        Log.e("MAIN_ACT ","-сработал метод myLoadSaveList()");
        url_loadsetting = loadSettingString("url_all", "");
        Log.e("MAIN_ACT","-sharedpref loadSettingString- " + url_loadsetting);
        if (!url_loadsetting.equals("")) { // если sharedpref не пустой то запуститься update_list();
            arURLloadsetting = url_loadsetting.split(";"); // split - разделить каждую сыылку знаком ";"
            count_load = arURLloadsetting.length; // записываем int кол-во записей в массиве
            // передача в webview последней открытой ссылки при первом создании макета
            webview.loadUrl(arURLloadsetting[arURLloadsetting.length-1]);
        }
        else // ВРЕМЕННО ЗАКРЫЛ, ЗАПУСКАЛСЯ ДВА РАЗА
        {
            update_list();
        }
    }

    //  Метод для доставки Push-уведомлений и автоматизации
    public void myOnsignal()
    {
        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        // Pass in email provided by customer
        OneSignal.setEmail("marinov37@yandex.ru");

        // Pass in phone number provided by customer
        //    OneSignal.setSMSNumber("+79303454564");
    }

    // метод определения наличия интернет соединения
    public static boolean isOnline(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
        {
            return true;
        }
        return false;
    }

    // метод internet резалирует в UI данные метода isOnline
    public void internet()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) { // бесконечный цикл с таймером
                    if (isOnline(getBaseContext())) {
                        if (num == 1)
                        {
                            String est = "есть";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //  createImageAlertsDialog(est); // если интернет есть
                                    //update_list(); // метод получения данных от серра запускается как интернет появляется
                                }
                            });
                        }
                        num = 0;

                    } else {

                        if(num == 0)
                        {
                            String net = "Please, check your internet connection status and restart app";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    createImageAlertsDialog(net); // если интернета нет
                                }
                            });
                        }
                        num = 1;
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    // метод для создания диалогового окна
    public void createImageAlertsDialog (String string) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder
                (MainActivity.this);
        builder.setMessage(string);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    // метод реализации диалога с пользователем закрыть приложение или нет
    public void myAlertDialog()
    {
        AlertDialog.Builder alertbox = new AlertDialog.Builder(MainActivity.this);
        alertbox.setTitle("Do you wish to exit ?");
        alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                // finish used for destroyed activity
                finish();
            }
        });

        alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                // Nothing will be happened when clicked on no button
                // of Dialog

                // очистка данных webview для того, чтобы массив с ссылками очистился
                webview.clearHistory();  // REMOVE THIS LINE
                webview.clearCache(true); // REMOVE THIS LINE
                clearHistory = true; // ADD THIS LINE

                count_load = 0; // стартовое положение счетчика

                // принудительная заргузка (обновление стартовой страницы)
                // это не очень, других варинатов не придумал, работает
                webview.loadUrl("https://yandex.ru/");

            }
        });
        alertbox.show();
    }

    // метод сохраняет пароль и логин почты яндекс
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // считывает файл sharedPreferences
    public String loadSettingString(String key, String default_value) {
        sharedPreferences = getSharedPreferences("myPref", MODE_PRIVATE);
        return sharedPreferences.getString(key, default_value);
    }

    // сохраняет в файл sharedPreferences
    public void saveSettingString(String key, String value) {
        sharedPreferences = getSharedPreferences("myPref", MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPreferences.edit(); // edit() - редактирование файлов
        ed.putString(key, value); // добавляем ключ и его значение
        if (ed.commit()) // сохранить файл
        {
            //успешно записано данные в файл
        }
        else
        {
            //ошибка при записи
            Toast.makeText(this, "Write error", Toast.LENGTH_SHORT).show();
        }
    }

    // переход во фрагмент FRAGREG
    public void transitionToFragRag()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentFormReg fragmentFormReg = new FragmentFormReg();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.ll_frag_form_reg, fragmentFormReg);
        fragmentTransaction.commit();
    }

}