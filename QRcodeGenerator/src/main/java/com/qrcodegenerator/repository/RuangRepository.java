package com.qrcodegenerator.repository;

import com.qrcodegenerator.entity.KelasSesi;
import com.qrcodegenerator.entity.Ruang;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuangRepository extends JpaRepository<Ruang, String> {
}
