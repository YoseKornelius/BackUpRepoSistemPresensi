package com.qrcodegenerator.response;

import lombok.Data;

@Data
public class ProfilResponse {

    private String nama;
    private String nim;

    public ProfilResponse(String nama, String nim) {
        this.nama = nama;
        this.nim = nim;
    }
}
