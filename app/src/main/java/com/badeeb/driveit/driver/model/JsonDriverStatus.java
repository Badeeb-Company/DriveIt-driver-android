package com.badeeb.driveit.driver.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Amr Alghawy on 9/27/2017.
 */

public class JsonDriverStatus {

    @Expose(serialize = false, deserialize = true)
    @SerializedName("meta")
    private JsonMeta jsonMeta;

    @Expose(serialize = true, deserialize = false)
    @SerializedName("avilability")
    private String avilability;

    public JsonDriverStatus() {
    }

    public JsonMeta getJsonMeta() {
        return jsonMeta;
    }

    public void setJsonMeta(JsonMeta jsonMeta) {
        this.jsonMeta = jsonMeta;
    }

    public String getAvilability() {
        return avilability;
    }

    public void setAvilability(String avilability) {
        this.avilability = avilability;
    }
}
