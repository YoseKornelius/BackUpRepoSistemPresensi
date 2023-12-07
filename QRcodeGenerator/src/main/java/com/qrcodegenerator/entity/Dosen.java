package com.qrcodegenerator.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity(name = "dosen")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class Dosen {

    @Id
    String kodeDosen;
    String nik;

    String kodeProdi;

    String nama;

    String namaGelar;

    String nomorInduk;

    String tipeNomorInduk;

    String noSertifikat;

    String jenisKelamin;

    String tanggalLahir;

    String statusDosen;

    String statusDikti;

    String statusYayasan;

    String tanggalMulai;

    String tanggalSelesai;

    String tanggalInput;

    String urlGoogleScholar;

    String urlResearchGate;

    String urlLinkedIn;

    String urlSinta2;


    public Dosen() {
    }

    public Dosen(String kodeDosen, String nik, String kodeProdi, String nama) {
        this.kodeDosen = kodeDosen;
        this.nik = nik;
        this.kodeProdi = kodeProdi;
        this.nama = nama;
    }
}
