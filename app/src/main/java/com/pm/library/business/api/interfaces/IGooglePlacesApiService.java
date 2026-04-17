package com.pm.library.business.api.interfaces;

import com.pm.library.business.api.PlacesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IGooglePlacesApiService {
    @GET("maps/api/place/nearbysearch/json")
    Call<PlacesResponse> getNearbyBookstores(
                @Query("location") String location,
                @Query("radius") int radius,
                @Query("type") String type,
                @Query("keyword") String keyword,
                @Query("key") String apiKey
    );
}
