package com.ednevnik.dnevnik.repository;

import com.ednevnik.dnevnik.model.ClassAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassAssignmentRepository extends JpaRepository<ClassAssignment, Long> {
    List<ClassAssignment> findByTeacherId(Long teacherId);
} 