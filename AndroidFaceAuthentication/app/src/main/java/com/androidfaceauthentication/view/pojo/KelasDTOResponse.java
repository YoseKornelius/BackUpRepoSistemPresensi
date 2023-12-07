package com.androidfaceauthentication.view.pojo;

public class KelasDTOResponse {

    private String kodeKelas;
    private String matakuliah;
    private String semester;
    private String dosen;
    private String grup;

    public KelasDTOResponse(String kodeKelas, String matakuliah, String semester,String dosen, String
            grup) {
        this.kodeKelas = kodeKelas;
        this.matakuliah = matakuliah;
        this.semester = semester;
        this.dosen = dosen;
        this.grup = grup;
    }

    public void setKodeKelas(String kodeKelas) {
        this.kodeKelas = kodeKelas;
    }

    public void setMatakuliah(String matakuliah) {
        this.matakuliah = matakuliah;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public void setDosen(String dosen) {
        this.dosen = dosen;
    }

    public void setGrup(String grup) {
        this.grup = grup;
    }

    public String getKodeKelas() {
        return kodeKelas;
    }

    public String getMatakuliah() {
        return matakuliah;
    }

    public String getSemester() {
        return semester;
    }

    public String getDosen() {
        return dosen;
    }

    public String getGrup() {
        return grup;
    }
}
