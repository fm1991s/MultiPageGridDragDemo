package com.huangjie.demo;

import android.app.Application;

/**
 * Created by huangjie on 2017/6/4.
 */

public class GApplication extends Application {

    private static GApplication mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
    }

    public static GApplication getApplication() {
        return mApplication;
    }
}
