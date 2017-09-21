package com.badeeb.driveit.driver.shared;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Amr Alghawy on 6/12/2017.
 */

public class AppPreferences {

    // For logging purpose
    public static final String TAG = AppPreferences.class.getName();

    public static final String BASE_URL = "https://drive-it-badeeb.herokuapp.com/api/v1";

    // Volley constants
    public static final int VOLLEY_TIME_OUT = 2000; // Milliseconds
    public static final int VOLLEY_RETRY_COUNTER = 2;

    // Trip constants
    public static boolean isOnline = false;
    public static final String TRIP_ACCEPTED = "accepted";
    public static final String TRIP_REJECTED = "notServed";
    public static final String TRIP_PENDING = "pending";

    // Location updates constants
    public static final int UPDATE_TIME = 5000;    // Millisecoonds
    public static final int UPDATE_DISTANCE = 0;

    // Shared Preferences Keys

    public static SharedPreferences getAppPreferences(Context context) {
        return context.getSharedPreferences(TAG, Activity.MODE_PRIVATE);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
