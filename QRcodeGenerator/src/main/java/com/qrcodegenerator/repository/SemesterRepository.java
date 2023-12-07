package com.qrcodegenerator.repository;

import com.qrcodegenerator.entity.KelasSesi;
import com.qrcodegenerator.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterRepository extends JpaRepository<Semester, String> {
}
