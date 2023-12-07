package com.qrcodegenerator.entity;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "kelas_sesi")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class KelasSesi {

    @Id
    @Column(name= "kode_sesi")
    String kodeSesi;

    @Column(name = "sesi_start")
    String sesiStart;

    @Column(name = "sesi_end")
    String sesiEnd;

    public KelasSesi() {

    }

    public KelasSesi(String kodeSesi, String sesiStart, String sesiEnd) {
        this.kodeSesi = kodeSesi;
        this.sesiStart = sesiStart;
        this.sesiEnd = sesiEnd;
    }
}