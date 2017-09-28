package com.badeeb.driveit.driver.shared;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Amr Alghawy on 6/12/2017.
 */

public class AppPreferences {

    public static final String TOKEN_KEY = "TOKEN_KEY";

    // For logging purpose
    public static final String TAG = AppPreferences.class.getName();

    public static final String BASE_URL = "https://drive-it-badeeb.herokuapp.com/api/v1";

    // Volley constants
    public static final int VOLLEY_TIME_OUT = 2000; // Milliseconds
    public static final int VOLLEY_RETRY_COUNTER = 2;

    // Trip constants
    public static boolean isOnline = false;
    public static final String TRIP_PENDING = "PENDING";

    // Driver status
    public static final String LOGGED_IN = "LOGGED_IN";
    public static final String ONLINE = "ONLINE";
    public static final String IN_TRIP = "IN_TRIP";
    public static final String TRIP_COMPLETED = "TRIP_COMPLETED";

    // Location updates constants
    public static final int UPDATE_TIME = 0;    // Millisecoonds
    public static final int UPDATE_DISTANCE = 0;

    // Splash Screen timeout
    public static final int SPLASH_TIME_OUT = 3000;

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

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    public static boolean isPhoneNumberValid(String email) {
        String expression = "^[+]?[0-9]{8,25}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
