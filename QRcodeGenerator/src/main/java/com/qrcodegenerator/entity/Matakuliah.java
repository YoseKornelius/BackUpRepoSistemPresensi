package com.qrcodegenerator.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "matakuliah")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class Matakuliah {

    @Id
    @Column(name = "kode_matakuliah")
    String kodeMatakuliah;

    @Column(name = "kode_mapping", columnDefinition = "char")
    String kodeMapping;

    @Column(name = "kode_kurikulum", columnDefinition = "char")
    String kodeKurikulum;

    @Column(name = "kode_kelompok_matakuliah", columnDefinition = "char")
    String kodeKelompokMatakuliah;

    @Column(name = "kode_prodi", columnDefinition = "char")
    String kodeProdi;

    @Column(name = "kode_matakuliah_praktikum", columnDefinition = "char")
    String kodeMatakuliahPraktikum;

    @Column(name = "nama_matakuliah")
    String namaMatakuliah;

    @Column(name = "sks")
    int sks;

    @Column(name = "harga", columnDefinition = "int")
    String harga;

    @Column(name = "is_praktikum")
    boolean isPraktikum;

    @Column(name = "minimal_sks", columnDefinition = "int")
    String minimalSks;

    @Column(name = "tanggal_input", columnDefinition = "datetime")
    String tanggalInput;

    public Matakuliah() {

    }

    public Matakuliah(String kodeMatakuliah, String namaMatakuliah, int sks) {
        this.kodeMatakuliah = kodeMatakuliah;
        this.namaMatakuliah = namaMatakuliah;
        this.sks = sks;
    }

}