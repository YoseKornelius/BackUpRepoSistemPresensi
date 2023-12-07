package com.qrcodegenerator.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "jadwal")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class Jadwal {

    @Id
    @Column(name = "kode_jadwal")
    String kodeJadwal;

    @ManyToOne
    @JoinColumn(name = "kode_kelas", nullable = false)
    private Kelas kelas;

    @ManyToOne
    @JoinColumn(name = "kode_sesi")
    private KelasSesi sesi;

    @ManyToOne
    @JoinColumn(name = "kode_ruang")
    private Ruang ruang;

    @Column(name = "tanggal")
    String tanggal;

    @Column(name = "kata_kunci_qr")
    String kataKunciQR;

    public Jadwal() {
    }

    public Jadwal(String kodeJadwal, Kelas kelas, KelasSesi sesi, Ruang ruang, String tanggal) {
        this.kodeJadwal = kodeJadwal;
        this.kelas = kelas;
        this.sesi = sesi;
        this.ruang = ruang;
        this.tanggal = tanggal;
    }
}
