package com.qrcodegenerator.repository;

import com.qrcodegenerator.entity.Jadwal;
import com.qrcodegenerator.entity.Mahasiswa;
import com.qrcodegenerator.entity.PresensiMahasiswa;
import com.qrcodegenerator.entity.PresensiMahasiswaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PresensiMahasiswaRepository extends JpaRepository<PresensiMahasiswa, Integer> {

    /*List<PresensiMahasiswa> findAllByPresensiMahasiswaId(PresensiMahasiswaId id);
    PresensiMahasiswa findByPresensiMahasiswaId(PresensiMahasiswaId id);*/

    List<PresensiMahasiswa> findAllByJadwalAndMahasiswa(Jadwal jadwal, Mahasiswa mahasiswa);
    PresensiMahasiswa findByJadwalAndMahasiswa(Jadwal jadwal, Mahasiswa mahasiswa);

    List<PresensiMahasiswa> findAllByJadwal(Jadwal jadwal);

    List<PresensiMahasiswa> findAllByMahasiswa(Mahasiswa mahasiswa);
}
