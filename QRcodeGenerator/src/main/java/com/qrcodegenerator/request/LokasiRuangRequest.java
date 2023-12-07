package com.qrcodegenerator.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LokasiRuangRequest {

    @NotBlank(message = "id Jadwal is required")
    private String idJadwal;
}
