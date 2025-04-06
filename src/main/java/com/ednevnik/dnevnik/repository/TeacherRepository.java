package com.ednevnik.dnevnik.repository;

import com.ednevnik.dnevnik.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
} 