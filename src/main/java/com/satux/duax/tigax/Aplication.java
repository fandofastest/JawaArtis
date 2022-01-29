package com.satux.duax.tigax;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

public class Aplication extends MultiDexApplication {

    private static Context mContext;
    public static Context getContext() {
        return mContext;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
}
