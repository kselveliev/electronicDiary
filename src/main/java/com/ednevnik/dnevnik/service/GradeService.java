package com.ednevnik.dnevnik.service;

import com.ednevnik.dnevnik.dto.GradeDto;
import java.util.List;

public interface GradeService {
    List<GradeDto> getGradesForStudent(Long studentId);
    List<GradeDto> getGradesForStudentBySubject(Long studentId, Long subjectId);
    GradeDto addGrade(GradeDto gradeDto);
    void deleteGrade(Long gradeId);
} 