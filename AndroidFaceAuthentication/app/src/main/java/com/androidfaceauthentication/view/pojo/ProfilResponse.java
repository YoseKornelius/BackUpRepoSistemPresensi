package com.androidfaceauthentication.view.pojo;

public class ProfilResponse {
    private String nama;
    private String nim;

    public ProfilResponse(String nama, String nim) {
        this.nama = nama;
        this.nim = nim;
    }

    public String getNama() {
        return nama;
    }

    public String getNim() {
        return nim;
    }
}
