package com.qrcodegenerator.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "semester")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class Semester {

    @Id
    @Column(name = "kode_semester")
    String kodeSemester;

    /*@ManyToOne
    @JoinColumn(name = "semester")
    private MappingSemester semester;*/

    @Column(name = "nama_semester")
    private String namaSemester;

    @Column(name = "tahun_ajaran")
    String tahunAjaran;

    @Column(name = "tanggal_mulai")
    String tanggalMulai;

    @Column(name = "tanggal_selesai")
    String tanggalSelesai;

    @Column(name="tanggal_input")
    String tanggalInput;

    public Semester() {

    }

    public Semester(String kodeSemester, String namaSemester, String tahunAjaran, String tanggalMulai, String tanggalSelesai) {
        this.kodeSemester = kodeSemester;
        this.namaSemester = namaSemester;
        this.tahunAjaran = tahunAjaran;
        this.tanggalMulai = tanggalMulai;
        this.tanggalSelesai = tanggalSelesai;
    }
}
