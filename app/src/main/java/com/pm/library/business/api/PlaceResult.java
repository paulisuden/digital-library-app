package com.pm.library.business.api;

import com.google.gson.annotations.SerializedName;

public class PlaceResult {
    @SerializedName("name")
    public String name;
    @SerializedName("vicinity") // dirección corta
    public String vicinity;
    @SerializedName("geometry")
    public Geometry geometry;
}