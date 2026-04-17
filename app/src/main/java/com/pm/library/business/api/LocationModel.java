package com.pm.library.business.api;

import com.google.gson.annotations.SerializedName;

public class LocationModel {
    @SerializedName("lat")
    public double lat;
    @SerializedName("lng")
    public double lng;
}
