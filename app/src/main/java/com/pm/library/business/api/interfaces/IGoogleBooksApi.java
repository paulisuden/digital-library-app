package com.pm.library.business.api.interfaces;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IGoogleBooksApi {
    //Call: solicitud http que entrega un objeto json
    @GET("volumes")
    Call<JsonObject> searchBooks(
            @Query("q") String title,
            @Query("maxResults") int maxResults,
            @Query("langRestrict") String langRestrict
    );
}
