package com.badeeb.driveit.driver.model;

import com.badeeb.driveit.driver.shared.AppPreferences;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Amr Alghawy on 9/18/2017.
 */
@Parcel(Parcel.Serialization.BEAN)
public class User {

    // Class Attributes
    @Expose
    @SerializedName("id")
    private int id;
    @Expose
    @SerializedName("name")
    private String name;
    @Expose
    @SerializedName("email")
    private String email;
    @Expose
    @SerializedName("password")
    private String password;
    @Expose
    @SerializedName("image_url")
    private String photoUrl;
    @Expose
    @SerializedName("phone")
    private String phoneNumber;

    //-------------- No need for the following information
    @Expose
    @SerializedName("created_at")
    private String createdAt;
    @Expose
    @SerializedName("updated_at")
    private String updatedAt;
    @Expose
    @SerializedName("driver_state")
    private String state;

    @Expose
    @SerializedName("driver_availability")
    private String availability;
    @Expose
    @SerializedName("token")
    private String token;

    // Constructor
    public User() {
        this.id = 0;
        this.name = "";
        this.email = "";
        this.password = "";
        this.photoUrl = "";
        this.phoneNumber = "";
    }

    public boolean isOnline(){
        return AppPreferences.USER_ONLINE.equals(availability);
    }

    public void setOnline(){
        availability = AppPreferences.USER_ONLINE;
    }

    public void setOffline(){
        availability = AppPreferences.USER_OFFLINE;
    }

    public boolean isInTrip(){
        return AppPreferences.USER_IN_TRIP .equals(state);
    }

    public void setInTrip(){
        state = AppPreferences.USER_IN_TRIP;
    }

    public boolean isAvailable(){
        return AppPreferences.USER_AVAILABLE.equals(state);
    }

    public void setAvailable(){
        state = AppPreferences.USER_AVAILABLE;
    }

    // Setters and Getters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
