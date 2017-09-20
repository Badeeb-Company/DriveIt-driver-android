package com.badeeb.driveit.client.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Amr Alghawy on 9/18/2017.
 */

public class Trip {

    @Expose
    @SerializedName("id")
    private int id;
    @Expose
    @SerializedName("user_id")
    private int clientId;
    @Expose
    @SerializedName("driver_id")
    private int driverId;
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

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

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
}
