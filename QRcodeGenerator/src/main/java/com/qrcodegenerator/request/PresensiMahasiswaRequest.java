package com.qrcodegenerator.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PresensiMahasiswaRequest {
    @NotBlank(message = "id Jadwal is required")
    public String idJadwal;

    @NotBlank(message = "nim mahasiswa is required")
    public String nim;
}
