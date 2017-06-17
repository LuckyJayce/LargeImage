package com.example.lagerimage_test;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by luckyjayce on 2017/5/3.
 */

public class AppContext extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}
