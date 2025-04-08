package com.ednevnik.dnevnik.repository;

import com.ednevnik.dnevnik.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentId(Long studentId);
    List<Grade> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
    List<Grade> findByTeacherId(Long teacherId);

    @Query("SELECT g FROM Grade g " +
           "JOIN g.teacher t " +
           "WHERE t.school.id = :schoolId AND g.subject.id = :subjectId")
    List<Grade> findBySchoolAndSubject(@Param("schoolId") Long schoolId, @Param("subjectId") Long subjectId);
} 