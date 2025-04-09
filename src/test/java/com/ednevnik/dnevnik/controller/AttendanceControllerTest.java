package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.*;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceControllerTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetailsImpl userDetails;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AttendanceController attendanceController;

    private Teacher teacher;
    private Student student;
    private Subject subject;
    private com.ednevnik.dnevnik.model.Class studentClass;
    private School school;
    private Attendance attendance;

    @BeforeEach
    void setUp() {
        // Setup test data
        school = new School();
        school.setId(1L);
        school.setName("Test School");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setSchool(school);

        student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");

        subject = new Subject();
        subject.setId(1L);
        subject.setName("Mathematics");

        studentClass = new com.ednevnik.dnevnik.model.Class();
        studentClass.setId(1L);
        studentClass.setName("Class A");
        studentClass.setSchool(school);

        attendance = new Attendance();
        attendance.setId(1L);
        attendance.setStudent(student);
        attendance.setTeacher(teacher);
        attendance.setSubject(subject);
    }

    @Test
    void showAttendanceFormShouldReturnFormView() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(teacher);
        when(subjectRepository.findAll()).thenReturn(Arrays.asList(subject));
        when(classRepository.findBySchoolId(any())).thenReturn(Arrays.asList(studentClass));

        // When
        String viewName = attendanceController.showAttendanceForm(model, authentication);

        // Then
        verify(subjectRepository).findAll();
        verify(classRepository).findBySchoolId(teacher.getSchool().getId());
        verify(model).addAttribute("subjects", Arrays.asList(subject));
        verify(model).addAttribute("classes", Arrays.asList(studentClass));
        verify(model).addAttribute("teacher", teacher);
        assertEquals("attendance/form", viewName);
    }

    @Test
    void takeAttendanceShouldReturnTakeView() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(teacher);
        when(classRepository.findById(1L)).thenReturn(java.util.Optional.of(studentClass));
        when(subjectRepository.findById(1L)).thenReturn(java.util.Optional.of(subject));
        when(studentRepository.findByStudentClassId(1L)).thenReturn(Arrays.asList(student));

        // When
        String viewName = attendanceController.takeAttendance(1L, 1L, 1, model, authentication);

        // Then
        verify(classRepository).findById(1L);
        verify(subjectRepository).findById(1L);
        verify(studentRepository).findByStudentClassId(1L);
        verify(model).addAttribute("students", Arrays.asList(student));
        verify(model).addAttribute("class", studentClass);
        verify(model).addAttribute("subject", subject);
        verify(model).addAttribute("lessonNumber", 1);
        verify(model).addAttribute("teacher", teacher);
        assertEquals("attendance/take", viewName);
    }

    @Test
    void saveAttendanceShouldReturnRedirect() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(teacher);
        when(classRepository.findById(1L)).thenReturn(java.util.Optional.of(studentClass));
        when(subjectRepository.findById(1L)).thenReturn(java.util.Optional.of(subject));
        when(studentRepository.findById(1L)).thenReturn(java.util.Optional.of(student));

        // When
        String redirectUrl = attendanceController.saveAttendance(1L, 1L, 1, 
                Arrays.asList(1L), Arrays.asList("Absent"), authentication, redirectAttributes);

        // Then
        verify(classRepository).findById(1L);
        verify(subjectRepository).findById(1L);
        verify(studentRepository).findById(1L);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
        verify(redirectAttributes).addFlashAttribute(anyString(), anyString());
        assertEquals("redirect:/attendance", redirectUrl);
    }

    @Test
    void showMyAttendanceShouldReturnMyAttendanceView() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(student);
        when(attendanceRepository.findByStudentOrderByCreatedTimestampDesc(any())).thenReturn(Arrays.asList(attendance));

        // When
        String viewName = attendanceController.showMyAttendance(model, authentication);

        // Then
        verify(attendanceRepository).findByStudentOrderByCreatedTimestampDesc(student);
        verify(model).addAttribute("student", student);
        verify(model).addAttribute("attendances", Arrays.asList(attendance));
        assertEquals("attendance/student-view", viewName);
    }

    @Test
    void showChildrenAttendanceShouldReturnParentView() {
        // Given
        Parent parent = new Parent();
        parent.setId(1L);
        List<Student> children = Arrays.asList(student);
        parent.setChildren(new HashSet<>(children));
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(parent);
        when(attendanceRepository.findByStudentInOrderByCreatedTimestampDesc(any())).thenReturn(Arrays.asList(attendance));

        // When
        String viewName = attendanceController.showChildrenAttendance(model, authentication);

        // Then
        verify(attendanceRepository).findByStudentInOrderByCreatedTimestampDesc(children);
        verify(model).addAttribute("children", children);
        verify(model).addAttribute("attendances", Arrays.asList(attendance));
        assertEquals("attendance/parent-view", viewName);
    }

    @Test
    void showClassAttendanceShouldReturnClassView() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(teacher);
        when(classRepository.findBySchoolId(any())).thenReturn(Arrays.asList(studentClass));
        when(subjectRepository.findAll()).thenReturn(Arrays.asList(subject));
        when(classRepository.findById(1L)).thenReturn(java.util.Optional.of(studentClass));
        when(studentRepository.findByStudentClassId(1L)).thenReturn(Arrays.asList(student));
        when(subjectRepository.findById(1L)).thenReturn(java.util.Optional.of(subject));
        when(attendanceRepository.findByStudentClassIdAndSubjectIdOrderByCreatedTimestampDesc(1L, 1L))
                .thenReturn(Arrays.asList(attendance));

        // When
        String viewName = attendanceController.showClassAttendance(1L, 1L, model, authentication);

        // Then
        verify(classRepository).findBySchoolId(teacher.getSchool().getId());
        verify(subjectRepository).findAll();
        verify(classRepository).findById(1L);
        verify(studentRepository).findByStudentClassId(1L);
        verify(subjectRepository).findById(1L);
        verify(attendanceRepository).findByStudentClassIdAndSubjectIdOrderByCreatedTimestampDesc(1L, 1L);
        verify(model).addAttribute("classes", Arrays.asList(studentClass));
        verify(model).addAttribute("subjects", Arrays.asList(subject));
        verify(model).addAttribute("selectedClass", studentClass);
        verify(model).addAttribute("students", Arrays.asList(student));
        verify(model).addAttribute("selectedSubject", subject);
        verify(model).addAttribute("attendances", Arrays.asList(attendance));
        assertEquals("attendance/class-view", viewName);
    }
} 