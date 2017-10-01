package com.badeeb.driveit.driver.controllers;

import android.app.Application;

import com.badeeb.driveit.driver.shared.AppSettings;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;


/**
 * Created by meldeeb on 9/25/17.
 */

public class DriveItApplication extends Application {
    private static DriveItApplication sDriverItApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        sDriverItApplication = this;
    }

    public static DriveItApplication getInstance() {
        if (sDriverItApplication == null) {
            sDriverItApplication = new DriveItApplication();
        }
        return sDriverItApplication;
    }

    public static boolean isLoggedIn() {
        return AppSettings.getInstance().isLoggedIn();
    }

}
