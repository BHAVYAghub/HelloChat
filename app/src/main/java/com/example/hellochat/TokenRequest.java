package com.example.hellochat;



import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TokenRequest {
    @SerializedName("MSG")
    @Expose
    private String MSG;
    public String getMSG() {
        return MSG;
    }

    public void setMSG(String MSG) {
        this.MSG = MSG;
    }



}

