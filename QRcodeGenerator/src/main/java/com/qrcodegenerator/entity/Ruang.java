package com.qrcodegenerator.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ruang")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class Ruang {

    @Id
    @Column(name = "id_ruang")
    String idRuang;

    @Column(name = "nama")
    String nama;

    @Column(name = "latitude")
    String latitude;

    @Column(name = "longitude")
    String longitude;

    public Ruang() {

    }

    public Ruang(String idRuang, String nama, String latitude, String longitude) {
        this.idRuang = idRuang;
        this.nama = nama;
        this.latitude = latitude;
        this.longitude = longitude;
    }


}
