package com.qrcodegenerator.response;

import jakarta.validation.constraints.NotBlank;

public class QrCodeScanResult {

    @NotBlank(message = "QR code data is required")
    private String qrCodeData;

    public QrCodeScanResult(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    // Getter dan setter
    public String getQrCodeData() {
        return qrCodeData;
    }


    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

}
