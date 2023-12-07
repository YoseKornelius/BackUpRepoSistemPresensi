package com.qrcodegenerator.request;

import jakarta.validation.constraints.NotBlank;

public class QRCodeScanResultRequest {
    @NotBlank(message = "QR code data is required")
    private String qrCodeData;

    private String kodeJadwal;

    public String getKodeJadwal() {
        return kodeJadwal;
    }

    // Getter dan setter
    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }
}
