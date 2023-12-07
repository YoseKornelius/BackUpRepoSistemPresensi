package com.qrcodegenerator.entity;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "presensi_mahasiswa")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class PresensiMahasiswa {

    /*compound primary key, mixed of mhs and jadwal*/
//    @EmbeddedId
//    private PresensiMahasiswaId presensiMahasiswaId;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "kode_jadwal")
    private Jadwal jadwal;

    @ManyToOne
    @JoinColumn(name = "nim")
    private Mahasiswa mahasiswa;

    @Column(name = "hadir")
    private boolean hadir;

    public PresensiMahasiswa(Jadwal jadwal, Mahasiswa mahasiswa, boolean hadir) {
//        this.presensiMahasiswaId = new PresensiMahasiswaId(jadwal, mahasiswa);
        this.jadwal = jadwal;
        this.mahasiswa = mahasiswa;
        this.hadir = hadir;

    }

    public PresensiMahasiswa() {

    }
}
