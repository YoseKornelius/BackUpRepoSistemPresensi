package com.qrcodegenerator.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;

@Embeddable
public class PresensiMahasiswaId implements Serializable {
    @ManyToOne
    @JoinColumn(name = "kode_jadwal")
    private Jadwal jadwal;

    @ManyToOne
    @JoinColumn(name = "nim")
    private Mahasiswa mahasiswa;

    public PresensiMahasiswaId(Jadwal jadwal, Mahasiswa mahasiswa) {
        this.jadwal = jadwal;
        this.mahasiswa = mahasiswa;
    }

    public PresensiMahasiswaId() {

    }
}
