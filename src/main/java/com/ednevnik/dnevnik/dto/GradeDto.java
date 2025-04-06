package com.ednevnik.dnevnik.dto;

import lombok.Data;

@Data
public class GradeDto {
    private Long id;
    private Integer grade;
    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private Long createdTimestamp;
    private String className;
} 