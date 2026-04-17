package com.pm.library.business.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlacesResponse {
    @SerializedName("results")
    public List<PlaceResult> results;
    @SerializedName("status")
    public String status;
}
