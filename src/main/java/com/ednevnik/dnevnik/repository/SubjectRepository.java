package com.ednevnik.dnevnik.repository;

import com.ednevnik.dnevnik.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
} 