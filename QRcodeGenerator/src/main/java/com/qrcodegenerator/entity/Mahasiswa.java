package com.qrcodegenerator.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;

@Entity(name = "mahasiswa")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class Mahasiswa {

    @Id
    String nim;

    String kodeProdi;

    String tahunAngkatan;

    String nama;

    String jenisKelamin;

    Date tanggalLahir;

    String tempatLahir;

    String wargaNegara;

    String agama;

    String alamat;

    String sekolahAsal;

    String jurusan;

    Date tanggalMulaiKuliah;

    String kodeposRumah;

    String provinsiRumah;

    String kabKotaRumah;

    String kecamatanRumah;

    String kelurahanRumah;

    String jenisKabKotaRumah;

    String kodeposSekolah;

    String provinsiSekolah;

    String kabKotaSekolah;

    String kecamatanSekolah;

    String kelurahanSekolah;

    String jenisKabKotaSekolah;

    String email;

    boolean flagPemutihan = false;

    String tanggalInput;

    public Mahasiswa (){

    }

    public Mahasiswa(String nim, String kodeProdi, String tahunAngkatan, String nama) {
        this.nim = nim;
        this.kodeProdi = kodeProdi;
        this.tahunAngkatan = tahunAngkatan;
        this.nama = nama;
    }
}
