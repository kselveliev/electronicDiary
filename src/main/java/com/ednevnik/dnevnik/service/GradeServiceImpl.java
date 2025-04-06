package com.ednevnik.dnevnik.service;

import com.ednevnik.dnevnik.dto.GradeDto;
import com.ednevnik.dnevnik.mapper.GradeMapper;
import com.ednevnik.dnevnik.model.Grade;
import com.ednevnik.dnevnik.model.Student;
import com.ednevnik.dnevnik.model.Subject;
import com.ednevnik.dnevnik.model.Teacher;
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
        // Validate grade value
        if (gradeDto.getGrade() < 2 || gradeDto.getGrade() > 6) {
            throw new IllegalArgumentException("Grade must be between 2 and 6");
        }

        Student student = studentRepository.findById(gradeDto.getStudentId())
            .orElseThrow(() -> new RuntimeException("Student not found"));
            
        Subject subject = subjectRepository.findById(gradeDto.getSubjectId())
            .orElseThrow(() -> new RuntimeException("Subject not found"));
            
        Teacher teacher = teacherRepository.findById(gradeDto.getTeacherId())
            .orElseThrow(() -> new RuntimeException("Teacher not found"));
            
        // Create a new Grade instance without setting the ID
        Grade grade = new Grade();
        grade.setGrade(gradeDto.getGrade());
        grade.setStudent(student);
        grade.setSubject(subject);
        grade.setTeacher(teacher);
        grade.setCreatedTimestamp(System.currentTimeMillis());
            
        Grade savedGrade = gradeRepository.save(grade);
        return gradeMapper.toDto(savedGrade);
    }

    @Override
    public void deleteGrade(Long gradeId) {
        gradeRepository.deleteById(gradeId);
    }
} 