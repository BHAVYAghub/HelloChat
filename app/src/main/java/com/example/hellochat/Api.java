package com.example.hellochat;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Api {
    @POST("/")
    Call<TokenResponse> getTokenAccess(@Body TokenRequest tokenRequest);
}
