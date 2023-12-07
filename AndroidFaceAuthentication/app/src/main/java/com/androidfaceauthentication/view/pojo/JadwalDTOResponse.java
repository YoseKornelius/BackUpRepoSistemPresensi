package com.androidfaceauthentication.view.pojo;

public class JadwalDTOResponse {
    public String jadwal;
    public String kelas;
    public String sesi;
    public String ruang;
    public String tanggal;

    public JadwalDTOResponse(String jadwal, String kelas, String sesi, String ruang, String tanggal) {
        this.jadwal = jadwal;
        this.kelas = kelas;
        this.sesi = sesi;
        this.ruang = ruang;
        this.tanggal = tanggal;
    }

    public String getJadwal() {
        return jadwal;
    }

    public String getKelas() {
        return kelas;
    }

    public String getSesi() {
        return sesi;
    }

    public String getRuang() {
        return ruang;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setJadwal(String jadwal) {
        this.jadwal = jadwal;
    }

    public void setKelas(String kelas) {
        this.kelas = kelas;
    }

    public void setSesi(String sesi) {
        this.sesi = sesi;
    }

    public void setRuang(String ruang) {
        this.ruang = ruang;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }
}
