package com.dev_marinov.webviewtest;

import android.app.Application;

import com.yandex.metrica.ReporterConfig;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

public class test extends android.app.Application {

    public static final String API_key = "2cbfd6a3-a0e6-4cf4-b938-f800d920e990";

    @Override
    public void onCreate() {
        super.onCreate();
        // Creating an extended library configuration.
        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(API_key)
        // Если для отправки статистических данных не требуется согласие пользователя
                .withStatisticsSending(true)

        // Setting up the configuration. For example, to enable logging.
                .withLogs()
                .build();
        // Initializing the AppMetrica SDK.
        YandexMetrica.activate(getApplicationContext(), config);
        // Automatic tracking of user activity.
        YandexMetrica.enableActivityAutoTracking(this);
        // Возобновить определение местоположения библиотекой,
 //       YandexMetrica.setLocation(null); // возникает ошибка если не комментировать
//        // отправить собственное событие без вложенных параметров
        YandexMetrica.reportEvent("Updates installed");





// Attention. The reporter with the extended configuration should be initialized
// before the first call to the reporter. Otherwise, the reporter will be initialized without a configuration.
        // Creating extended configuration of the reporter.
// To create it, pass an API_key that is different from the app's API_key.

//        ReporterConfig reporterConfig = ReporterConfig.newConfigBuilder(API_key)
//                // Setting up the configuration. For example, to enable logging.
//                    .withLogs()
//                    .build();
//// Initializing a reporter.
//        YandexMetrica.activateReporter(getApplicationContext(), reporterConfig);


    }


}
