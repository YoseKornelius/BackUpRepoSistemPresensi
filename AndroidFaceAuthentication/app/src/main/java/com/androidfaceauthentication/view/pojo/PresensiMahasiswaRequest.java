package com.androidfaceauthentication.view.pojo;

import com.google.gson.annotations.Expose;

public class PresensiMahasiswaRequest {


    public String idJadwal;


    public String nim;

    public PresensiMahasiswaRequest(String idJadwal, String nim) {
        this.idJadwal = idJadwal;
        this.nim = nim;
    }

}
