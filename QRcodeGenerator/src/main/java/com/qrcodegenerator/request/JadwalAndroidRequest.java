package com.qrcodegenerator.request;

import jakarta.validation.constraints.NotBlank;

public class JadwalAndroidRequest {
    @NotBlank(message = "id kode kelas is required")
    public String kodeKelas;
}
