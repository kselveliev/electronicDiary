package com.ednevnik.dnevnik.repository;

import com.ednevnik.dnevnik.model.CurriculumSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurriculumSubjectRepository extends JpaRepository<CurriculumSubject, Long> {
    List<CurriculumSubject> findByCurriculumId(Long curriculumId);
} 