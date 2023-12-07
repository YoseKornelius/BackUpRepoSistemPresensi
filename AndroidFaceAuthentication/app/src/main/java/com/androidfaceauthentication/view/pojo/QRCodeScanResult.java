package com.androidfaceauthentication.view.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QRCodeScanResult {

    @Expose
    private String qrCodeData;
    @Expose
    private String kodeJadwal;

    public QRCodeScanResult(String qrCodeData, String kodeJadwal) {
        this.qrCodeData = qrCodeData;
        this.kodeJadwal = kodeJadwal;
    }

    public QRCodeScanResult(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public String getKodeJadwal() {
        return kodeJadwal;
    }
}
