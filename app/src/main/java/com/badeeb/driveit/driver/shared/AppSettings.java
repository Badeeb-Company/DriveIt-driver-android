package com.badeeb.driveit.driver.shared;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.badeeb.driveit.driver.R;
import com.badeeb.driveit.driver.controllers.DriveItApplication;
import com.badeeb.driveit.driver.model.Trip;
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
    private final static String PREF_USER_AVAILABILITY = "PREF_USER_AVAILABILITY";

    private final static String PREF_TRIP_ID = "PREF_TRIP_ID";
    private final static String PREF_TRIP_DISTANCE_TO_ARRIVE = "PREF_TRIP_DISTANCE_TO_ARRIVE";
    private final static String PREF_TRIP_CLIENT_ADDRESS = "PREF_TRIP_CLIENT_ADDRESS";
    private final static String PREF_TRIP_CLIENT_ID = "PREF_TRIP_CLIENT_ID";
    private final static String PREF_TRIP_CLIENT_IMAGE_URL = "PREF_TRIP_CLIENT_IMAGE_URL";
    private final static String PREF_TRIP_CLIENT_LAT = "PREF_TRIP_CLIENT_LAT";
    private final static String PREF_TRIP_CLIENT_LONG = "PREF_TRIP_CLIENT_LONG";
    private final static String PREF_TRIP_CLIENT_NAME = "PREF_TRIP_CLIENT_NAME";
    private final static String PREF_TRIP_CLIENT_PHONE = "PREF_TRIP_CLIENT_PHONE";
    private final static String PREF_TRIP_TIME_TO_ARRIVE = "PREF_TRIP_TIME_TO_ARRIVE";

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

    private void putValue(String key, int value) {
        sPreferences.edit().putInt(key, value).commit();
    }

    private int getValue(String key, int defaultValue) {
        return sPreferences.getInt(key, defaultValue);
    }

    private void putValue(String key, double value) {
        sPreferences.edit().putString(key, value+"").commit();
    }

    private double getValue(String key, double defaultValue) {
        return Double.parseDouble(sPreferences.getString(key, defaultValue+""));
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
        user.setAvailability(getUserAvailability());
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
        setUserAvailability(user.getAvailability());
    }

    public void clearUserInfo() {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.remove(PREF_USER_ID)
                .remove(PREF_USER_EMAIL)
                .remove(PREF_USER_IMAGE_URL)
                .remove(PREF_USER_TOKEN)
                .remove(PREF_USER_NAME)
                .remove(PREF_USER_MOBILE_NUMBER)
                .remove(PREF_USER_STATE)
                .remove(PREF_USER_AVAILABILITY);
        editor.commit();
    }

    public boolean isLoggedIn() {
        String authenticationToken = getUserToken();
        return !TextUtils.isEmpty(authenticationToken);
    }

    public void saveTrip(Trip trip) {
        setTripId(trip.getId());
        setTripDistanceToArrive(trip.getDistance_to_arrive());
        setTripClientAddress(trip.getClient_address());
        setTripClientId(trip.getClient_id());
        setTripClientImageURL(trip.getClient_image_url());
        setTripClientLat(trip.getClient_lat());
        setTripClientLong(trip.getClient_long());
        setTripClientName(trip.getClient_name());
        setTripClientPhone(trip.getClient_phone());
        setTripTimeToArrive(trip.getTime_to_arrive());
    }

    public Trip getTrip() {
        Trip trip = new Trip();
        trip.setId(getTripId());
        trip.setDistance_to_arrive(getTripDistanceToArrive());
        trip.setClient_address(getTripClientAddress());
        trip.setClient_id(getTripClientId());
        trip.setClient_image_url(getTripClientImageURL());
        trip.setClient_lat(getTripClientLat());
        trip.setClient_long(getTripClientLong());
        trip.setClient_name(getTripClientName());
        trip.setClient_phone(getTripClientPhone());
        trip.setTime_to_arrive(getTripTimeToArrive());

        return trip;
    }

    public void clearTripInfo() {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.remove(PREF_TRIP_ID)
                .remove(PREF_TRIP_DISTANCE_TO_ARRIVE)
                .remove(PREF_TRIP_CLIENT_ADDRESS)
                .remove(PREF_TRIP_CLIENT_ID)
                .remove(PREF_TRIP_CLIENT_IMAGE_URL)
                .remove(PREF_TRIP_CLIENT_LAT)
                .remove(PREF_TRIP_CLIENT_LONG)
                .remove(PREF_TRIP_CLIENT_NAME)
                .remove(PREF_TRIP_CLIENT_PHONE)
                .remove(PREF_TRIP_TIME_TO_ARRIVE)
        ;
        editor.commit();
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

    public void setUserAvailability(String userAvailability){
        putValue(PREF_USER_AVAILABILITY, userAvailability);
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

    public String getUserAvailability() {
        return getValue(PREF_USER_AVAILABILITY, "");
    }

    public void setTripId(int prefTripId) {
        putValue(PREF_TRIP_ID, prefTripId);
    }

    public int getTripId() {
        return getValue(PREF_TRIP_ID, 0);
    }

    public void setTripDistanceToArrive(double prefTripDistanceToArrive) {
        putValue(PREF_TRIP_DISTANCE_TO_ARRIVE, prefTripDistanceToArrive);
    }

    public double getTripDistanceToArrive() {
        return getValue(PREF_TRIP_DISTANCE_TO_ARRIVE, 0.0);
    }

    public void setTripClientAddress(String prefTripClientAddress) {
        putValue(PREF_TRIP_CLIENT_ADDRESS, prefTripClientAddress);
    }

    public String getTripClientAddress() {
        return getValue(PREF_TRIP_CLIENT_ADDRESS, "");
    }

    public void setTripClientId(int prefTripClientId) {
        putValue(PREF_TRIP_CLIENT_ID, prefTripClientId);
    }

    public int getTripClientId() {
        return getValue(PREF_TRIP_CLIENT_ID, 0);
    }

    public void setTripClientImageURL(String prefTripClientImageURL) {
        putValue(PREF_TRIP_CLIENT_IMAGE_URL, prefTripClientImageURL);
    }

    public String getTripClientImageURL() {
        return getValue(PREF_TRIP_CLIENT_IMAGE_URL, "");
    }

    public void setTripClientLat(double prefTripClientLat) {
        putValue(PREF_TRIP_CLIENT_LAT, prefTripClientLat);
    }

    public double getTripClientLat() {
        return getValue(PREF_TRIP_CLIENT_LAT, 0.0);
    }

    public void setTripClientLong(double prefTripClientLong) {
        putValue(PREF_TRIP_CLIENT_LONG, prefTripClientLong);
    }

    public double getTripClientLong() {
        return getValue(PREF_TRIP_CLIENT_LONG, 0.0);
    }

    public void setTripClientName(String prefTripClientName) {
        putValue(PREF_TRIP_CLIENT_NAME, prefTripClientName);
    }

    public String getTripClientName() {
        return getValue(PREF_TRIP_CLIENT_NAME, "");
    }

    public void setTripClientPhone(String prefTripClientPhone) {
        putValue(PREF_TRIP_CLIENT_PHONE, prefTripClientPhone);
    }

    public String getTripClientPhone() {
        return getValue(PREF_TRIP_CLIENT_PHONE, "");
    }


    public void setTripTimeToArrive(double prefTripTimeToArrive) {
        putValue(PREF_TRIP_TIME_TO_ARRIVE, prefTripTimeToArrive);
    }

    public double getTripTimeToArrive() {
        return getValue(PREF_TRIP_TIME_TO_ARRIVE, 0.0);
    }

}

