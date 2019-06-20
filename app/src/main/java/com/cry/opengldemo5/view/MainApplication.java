package com.cry.opengldemo5.view;

import android.app.Application;

/**
 * Created by subeiting on 2019/6/20.
 */
public class MainApplication extends Application {

    public static Application INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }
}
