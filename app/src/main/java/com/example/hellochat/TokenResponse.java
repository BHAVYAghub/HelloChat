package com.example.hellochat;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TokenResponse {


    @SerializedName("isAbusive")
    @Expose
    private boolean isAbusive;

    public boolean getIsAbusive() {
        return isAbusive;
    }
}

