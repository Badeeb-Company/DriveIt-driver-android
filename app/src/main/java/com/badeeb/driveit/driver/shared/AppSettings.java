package com.badeeb.driveit.driver.shared;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.badeeb.driveit.driver.R;
import com.badeeb.driveit.driver.controllers.DriveItApplication;
import com.badeeb.driveit.driver.model.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by meldeeb on 9/25/17.
 */

public class AppSettings {

    private final static String PREF_USER_ID = "PREF_USER_ID";
    private final static String PREF_USER_MOBILE_NUMBER = "PREF_PHONE_NUMBER";
    private final static String PREF_USER_NAME = "PREF_USER_NAME";
    private final static String PREF_USER_EMAIL = "PREF_USER_EMAIL";
    private final static String PREF_USER_TOKEN = "PREF_USER_TOKEN";
    private final static String PREF_USER_IMAGE_URL = "PREF_USER_IMAGE_URL";
    private final static String PREF_USER_STATE = "PREF_USER_STATE";

    private static AppSettings sInstance;

    private SharedPreferences sPreferences;

    public static AppSettings getInstance() {
        if (sInstance == null) {
            sInstance = new AppSettings(DriveItApplication.getInstance());
        }
        return sInstance;
    }


    private AppSettings(Context context) {
        String fileName = context.getString(R.string.app_name);
        this.sPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    private void putValue(String key, String value) {
        sPreferences.edit().putString(key, value).commit();
    }

    private String getValue(String key, String defaultValue) {
        return sPreferences.getString(key, defaultValue);
    }

    public User getUser() {
        User user = new User();
        user.setId(getUserId());
        user.setName(getUserName());
        user.setEmail(getUserEmail());
        user.setPhoneNumber(getUserMobileNumber());
        user.setPhotoUrl(getUserImageUrl());
        user.setToken(getUserToken());
        user.setState(getUserState());
        return user;
    }

    public void saveUser(User user) {
        setUserId(user.getId());
        setUserMobileNumber(user.getPhoneNumber());
        setUserName(user.getName());
        setUserEmail(user.getEmail());
        setUserImageUrl(user.getPhotoUrl());
        setUserToken(user.getToken());
        setUserState(user.getState());
    }

    public void clearUserInfo() {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.remove(PREF_USER_ID)
                .remove(PREF_USER_EMAIL)
                .remove(PREF_USER_IMAGE_URL)
                .remove(PREF_USER_TOKEN)
                .remove(PREF_USER_NAME)
                .remove(PREF_USER_MOBILE_NUMBER)
                .remove(PREF_USER_STATE);
        editor.commit();
    }

    public boolean isLoggedIn() {
        String authenticationToken = getUserToken();
        return !TextUtils.isEmpty(authenticationToken);
    }

    public void setUserId(int userId) {
        putValue(PREF_USER_ID, userId);
    }

    public void setUserEmail(String prefEmail) {
        putValue(PREF_USER_EMAIL, prefEmail);
    }

    public void setUserName(String prefFirstName) {
        putValue(PREF_USER_NAME, prefFirstName);
    }

    public void setUserMobileNumber(String prefMobileNumber) {
        putValue(PREF_USER_MOBILE_NUMBER, prefMobileNumber);
    }

    public void setUserImageUrl(String prefUserImageUrl) {
        putValue(PREF_USER_IMAGE_URL, prefUserImageUrl);
    }

    public void setUserToken(String prefUserToken) {
        putValue(PREF_USER_TOKEN, prefUserToken);
    }

    public void setUserState(String userStatus) {
        putValue(PREF_USER_STATE, userStatus);
    }

    public int getUserId() {
        return getValue(PREF_USER_ID, 0);
    }

    public String getUserToken() {
        return getValue(PREF_USER_TOKEN, "");
    }

    public String getUserEmail() {
        return getValue(PREF_USER_EMAIL, "");
    }

    public String getUserName() {
        return getValue(PREF_USER_NAME, "");
    }

    public String getUserMobileNumber() {
        return getValue(PREF_USER_MOBILE_NUMBER, "");
    }

    public String getUserImageUrl() {
        return getValue(PREF_USER_IMAGE_URL, "");
    }

    public String getUserState() {
        return getValue(PREF_USER_STATE, "");
    }

    private void putValue(String key, int value) {
        sPreferences.edit().putInt(key, value).commit();
    }

    private int getValue(String key, int defaultValue) {
        return sPreferences.getInt(key, defaultValue);
    }


}

