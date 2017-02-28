package com.example.hackintosh.lexicon;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * Created by hackintosh on 2/17/17.
 */

public interface RetrofitResponse {

    @GET("api/v1.5/tr.json/translate")
    Call<DataModel> getTranslation(@Query("key") String key, @Query("lang") String language,@Query("text") String text);

    @GET("api/v1.5/tr.json/detect")
    Call<GetLanguage> getLanguage(@Query("key") String key, @Query("text") String text);

    @GET("api/")
    Call<ImageDataModel> getImage(@Query("key") String key, @Query("q") String imageRequest, @Query("lang") String language);
}
