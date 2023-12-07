package com.androidfaceauthentication;

public class RvKelasModel {

    private String namaKelas;

    private String jadwalKelas;

    public RvKelasModel(String namaKelas, String jadwalKelas) {
        this.namaKelas = namaKelas;
        this.jadwalKelas = jadwalKelas;
    }
    public String getNamaKelas() {
        return namaKelas;
    }

    public String getJadwalKelas() {
        return jadwalKelas;
    }
}
