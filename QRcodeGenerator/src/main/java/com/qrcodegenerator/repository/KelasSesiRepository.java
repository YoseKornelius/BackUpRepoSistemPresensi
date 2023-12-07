package com.qrcodegenerator.repository;

import com.qrcodegenerator.entity.Kelas;
import com.qrcodegenerator.entity.KelasSesi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KelasSesiRepository extends JpaRepository<KelasSesi, String> {
}
