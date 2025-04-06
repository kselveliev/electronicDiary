package com.ednevnik.dnevnik.service;

import com.ednevnik.dnevnik.dto.GradeDto;
import com.ednevnik.dnevnik.mapper.GradeMapper;
import com.ednevnik.dnevnik.model.Grade;
import com.ednevnik.dnevnik.repository.GradeRepository;
import com.ednevnik.dnevnik.repository.StudentRepository;
import com.ednevnik.dnevnik.repository.SubjectRepository;
import com.ednevnik.dnevnik.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final GradeMapper gradeMapper;

    @Override
    public List<GradeDto> getGradesForStudent(Long studentId) {
        return gradeRepository.findByStudentId(studentId).stream()
                .map(gradeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GradeDto> getGradesForStudentBySubject(Long studentId, Long subjectId) {
        return gradeRepository.findByStudentIdAndSubjectId(studentId, subjectId).stream()
                .map(gradeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public GradeDto addGrade(GradeDto gradeDto) {
        Grade grade = new Grade();
        grade.setGrade(gradeDto.getGrade());
        
        grade.setStudent(studentRepository.findById(gradeDto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found")));
        
        grade.setSubject(subjectRepository.findById(gradeDto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found")));
        
        grade.setTeacher(teacherRepository.findById(gradeDto.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found")));

        grade.setCreatedTimestamp(System.currentTimeMillis());
        
        Grade savedGrade = gradeRepository.save(grade);
        return gradeMapper.toDto(savedGrade);
    }

    @Override
    public void deleteGrade(Long gradeId) {
        gradeRepository.deleteById(gradeId);
    }
} 