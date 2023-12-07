package com.qrcodegenerator.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "kelas")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class Kelas {
    @Id
    @Column(name = "kode_kelas")
    String kodeKelas;

    @ManyToOne
    @JoinColumn(name = "kode_matakuliah")
    private Matakuliah matakuliah;

    @Column(name = "group_kelas")
    String group;

    @ManyToOne
    @JoinColumn(name = "kode_semester", nullable = false)
    private Semester semester;

    @ManyToOne
    @JoinColumn(name = "kode_dosen", nullable = false)
    private Dosen dosen;

    public Kelas() {
    }

    public Kelas(String kodeKelas, Matakuliah matakuliah, String group, Semester semester, Dosen dosen) {
        this.kodeKelas = kodeKelas;
        this.matakuliah = matakuliah;
        this.group = group;
        this.semester = semester;
        this.dosen = dosen;
    }
}