package com.ednevnik.dnevnik.repository;

import com.ednevnik.dnevnik.model.Curriculum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {
    List<Curriculum> findBySchoolId(Long schoolId);
    List<Curriculum> findBySchoolIdAndActive(Long schoolId, boolean active);
} 