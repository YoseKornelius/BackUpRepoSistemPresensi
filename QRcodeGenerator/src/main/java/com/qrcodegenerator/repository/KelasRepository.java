package com.qrcodegenerator.repository;

import com.qrcodegenerator.entity.Dosen;
import com.qrcodegenerator.entity.Kelas;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KelasRepository extends JpaRepository<Kelas, String> {
}
