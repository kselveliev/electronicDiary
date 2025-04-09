package com.ednevnik.dnevnik.repository;

import com.ednevnik.dnevnik.model.ClassAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ClassAssignmentRepository extends JpaRepository<ClassAssignment, Long> {
    List<ClassAssignment> findByTeacherId(Long teacherId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ClassAssignment ca WHERE ca.schoolClass.id = :classId")
    void deleteBySchoolClassId(@Param("classId") Long classId);
} 