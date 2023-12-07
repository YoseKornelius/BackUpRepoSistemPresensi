package com.androidfaceauthentication.view.pojo;

public class LokasiRuangResponse {
    private String latitude;
    private String longtitude;

    public LokasiRuangResponse(String latitude, String longtitude) {
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }
}
