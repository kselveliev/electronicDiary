package com.ednevnik.dnevnik.repository;

import com.ednevnik.dnevnik.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByStudentClassId(Long classId);
} 