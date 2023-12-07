package com.qrcodegenerator.response;

import lombok.Data;

@Data
public class LokasiRuangResponse {
    private String latitude;
    private String longtitude;

    public LokasiRuangResponse(String latitude, String longtitude) {
        this.latitude = latitude;
        this.longtitude = longtitude;
    }
}
