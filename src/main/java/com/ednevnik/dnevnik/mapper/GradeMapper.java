package com.ednevnik.dnevnik.mapper;

import com.ednevnik.dnevnik.dto.GradeDto;
import com.ednevnik.dnevnik.model.Grade;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {

    public GradeDto toDto(Grade grade) {
        if (grade == null) {
            return null;
        }

        GradeDto dto = new GradeDto();
        dto.setId(grade.getId());
        dto.setGrade(grade.getGrade());
        dto.setCreatedTimestamp(grade.getCreatedTimestamp());

        if (grade.getStudent() != null) {
            dto.setStudentId(grade.getStudent().getId());
            dto.setStudentName(grade.getStudent().getFirstName() + " " + grade.getStudent().getLastName());
        }

        if (grade.getSubject() != null) {
            dto.setSubjectId(grade.getSubject().getId());
            dto.setSubjectName(grade.getSubject().getName());
        }

        if (grade.getTeacher() != null) {
            dto.setTeacherId(grade.getTeacher().getId());
            dto.setTeacherName(grade.getTeacher().getFirstName() + " " + grade.getTeacher().getLastName());
        }

        return dto;
    }
} 