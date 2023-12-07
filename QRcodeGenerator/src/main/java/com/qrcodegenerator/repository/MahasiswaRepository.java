package com.qrcodegenerator.repository;

import com.qrcodegenerator.entity.Dosen;
import com.qrcodegenerator.entity.Mahasiswa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MahasiswaRepository extends JpaRepository<Mahasiswa, String> {
    Mahasiswa findByEmail(String email);
}
