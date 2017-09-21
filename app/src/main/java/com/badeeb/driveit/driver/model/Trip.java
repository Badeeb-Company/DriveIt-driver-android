package com.badeeb.driveit.driver.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Amr Alghawy on 9/18/2017.
 */
@Parcel(Parcel.Serialization.BEAN)
public class Trip {

    @Expose
    @SerializedName("id")
    private int id;
    @Expose
    @SerializedName("user_id")
    private int clientId;
//    @Expose
//    @SerializedName("driver_id")
//    private int driverId;
    @Expose
    @SerializedName("trip_state")
    private String state;
    @Expose
    @SerializedName("destination")
    private String destination;
    @Expose
    @SerializedName("lat")
    private double lat;
    @Expose
    @SerializedName("long")
    private double lng;
    @Expose
    @SerializedName("created_at")
    private String createdAt;
    @Expose
    @SerializedName("updated_at")
    private String updatedAt;

    // Firebase Database Properties
    private double distance_to_arrive;
    private String client_address;
    private int client_id;
    private String client_image_url;
    private double client_lat;
    private double client_long;
    private String client_name;
    private String client_phone;
    private double time_to_arrive;

    public Trip() {
    }

    // Setters and Getters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

//    public int getDriverId() {
//        return driverId;
//    }
//
//    public void setDriverId(int driverId) {
//        this.driverId = driverId;
//    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
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

    // Firebase Database Setters and Getters
    public double getDistance_to_arrive() {
        return distance_to_arrive;
    }

    public void setDistance_to_arrive(double distance_to_arrive) {
        this.distance_to_arrive = distance_to_arrive;
    }

    public double getTime_to_arrive() {
        return time_to_arrive;
    }

    public void setTime_to_arrive(double time_to_arrive) {
        this.time_to_arrive = time_to_arrive;
    }

    public String getClient_address() {
        return client_address;
    }

    public void setClient_address(String client_address) {
        this.client_address = client_address;
    }

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }

    public String getClient_image_url() {
        return client_image_url;
    }

    public void setClient_image_url(String client_image_url) {
        this.client_image_url = client_image_url;
    }

    public double getClient_lat() {
        return client_lat;
    }

    public void setClient_lat(double client_lat) {
        this.client_lat = client_lat;
    }

    public double getClient_long() {
        return client_long;
    }

    public void setClient_long(double client_long) {
        this.client_long = client_long;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getClient_phone() {
        return client_phone;
    }

    public void setClient_phone(String client_phone) {
        this.client_phone = client_phone;
    }
}
