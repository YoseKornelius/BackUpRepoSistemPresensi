package com.qrcodegenerator.repository;

import com.qrcodegenerator.entity.Dosen;
import com.qrcodegenerator.entity.Matakuliah;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatakuliahRepository extends JpaRepository<Matakuliah, String> {
}
