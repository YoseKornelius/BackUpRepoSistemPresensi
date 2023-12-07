package com.qrcodegenerator.repository;

import com.qrcodegenerator.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JadwalRepository extends JpaRepository<Jadwal, String> {
    List<Jadwal> findAllByKelas(Kelas kelas);

    Jadwal findByKelas(Kelas kelas);
}
