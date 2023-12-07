package com.androidfaceauthentication.view.rvKelas;

public class itemKelas {

    private String matakuliah;

    private String kodeKelas;
    private String dosen;

    public itemKelas(String matakuliah, String kodeKelas, String dosen) {
        this.matakuliah = matakuliah;
        this.kodeKelas = kodeKelas;
        this.dosen = dosen;
    }

    public String getMatakuliah() {
        return matakuliah;
    }


    public String getKodeKelas() {
        return kodeKelas;
    }

    public String getDosen() {
        return dosen;
    }

    public void setMatakuliah(String matakuliah) {
        this.matakuliah = matakuliah;
    }


}

