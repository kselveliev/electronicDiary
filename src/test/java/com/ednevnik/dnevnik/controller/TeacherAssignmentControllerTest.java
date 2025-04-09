package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.ClassAssignmentRepository;
import com.ednevnik.dnevnik.repository.ClassRepository;
import com.ednevnik.dnevnik.repository.SubjectRepository;
import com.ednevnik.dnevnik.repository.TeacherRepository;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherAssignmentControllerTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private ClassAssignmentRepository classAssignmentRepository;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetailsImpl userDetails;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private TeacherAssignmentController teacherAssignmentController;

    private Teacher teacher;
    private Director director;
    private Admin admin;
    private School school;
    private Subject subject;
    private com.ednevnik.dnevnik.model.Class schoolClass;
    private ClassAssignment assignment;
    private List<Teacher> teachers;
    private List<Subject> subjects;
    private List<com.ednevnik.dnevnik.model.Class> classes;
    private List<ClassAssignment> assignments;

    @BeforeEach
    void setUp() {
        // Setup test data
        school = new School();
        school.setId(1L);
        school.setName("Test School");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setRole(UserRole.ROLE_TEACHER);
        teacher.setSchool(school);

        director = new Director();
        director.setId(2L);
        director.setFirstName("Jane");
        director.setLastName("Smith");
        director.setRole(UserRole.ROLE_DIRECTOR);
        director.setSchool(school);

        admin = new Admin();
        admin.setId(3L);
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(UserRole.ROLE_ADMIN);

        subject = new Subject();
        subject.setId(1L);
        subject.setName("Mathematics");

        schoolClass = new com.ednevnik.dnevnik.model.Class();
        schoolClass.setId(1L);
        schoolClass.setName("Class A");
        schoolClass.setSchool(school);

        assignment = new ClassAssignment();
        assignment.setId(1L);
        assignment.setTeacher(teacher);
        assignment.setSubject(subject);
        assignment.setSchoolClass(schoolClass);

        teachers = Collections.singletonList(teacher);
        subjects = Collections.singletonList(subject);
        classes = Collections.singletonList(schoolClass);
        assignments = Collections.singletonList(assignment);
    }

    @Test
    void showTeacherAssignmentsShouldAddAllTeachersForAdmin() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(admin);
        when(teacherRepository.findAll()).thenReturn(teachers);

        // When
        String viewName = teacherAssignmentController.showTeacherAssignments(model, authentication);

        // Then
        verify(model).addAttribute("teachers", teachers);
        assertEquals("teacher-assignments/list", viewName);
    }

    @Test
    void showTeacherAssignmentsShouldAddSchoolTeachersForDirector() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(director);
        when(teacherRepository.findBySchoolId(school.getId())).thenReturn(teachers);

        // When
        String viewName = teacherAssignmentController.showTeacherAssignments(model, authentication);

        // Then
        verify(model).addAttribute("teachers", teachers);
        assertEquals("teacher-assignments/list", viewName);
    }

    @Test
    void showTeacherDetailsShouldAddTeacherAndRelatedDataToModel() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(admin);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(subjectRepository.findAll()).thenReturn(subjects);
        when(classRepository.findBySchoolId(school.getId())).thenReturn(classes);
        when(classAssignmentRepository.findByTeacherId(1L)).thenReturn(assignments);

        // When
        String viewName = teacherAssignmentController.showTeacherDetails(1L, model, authentication);

        // Then
        verify(model).addAttribute("teacher", teacher);
        verify(model).addAttribute("subjects", subjects);
        verify(model).addAttribute("classes", classes);
        verify(model).addAttribute("assignments", assignments);
        assertEquals("teacher-assignments/details", viewName);
    }

    @Test
    void showTeacherDetailsShouldThrowExceptionWhenTeacherNotFound() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(director);
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            teacherAssignmentController.showTeacherDetails(1L, model, authentication));
    }

    @Test
    void showTeacherDetailsShouldThrowExceptionWhenDirectorAccessDenied() {
        // Given
        School otherSchool = new School();
        otherSchool.setId(2L);
        Teacher otherTeacher = new Teacher();
        otherTeacher.setId(1L);
        otherTeacher.setSchool(otherSchool);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(director);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(otherTeacher));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            teacherAssignmentController.showTeacherDetails(1L, model, authentication));
    }

    @Test
    void assignSubjectAndClassShouldAddSuccessMessageWhenSuccessful() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(admin);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(classRepository.findById(1L)).thenReturn(Optional.of(schoolClass));
        when(classAssignmentRepository.save(any(ClassAssignment.class))).thenReturn(assignment);

        // When
        String redirectUrl = teacherAssignmentController.assignSubjectAndClass(1L, 1L, 1L, redirectAttributes, authentication);

        // Then
        verify(classAssignmentRepository).save(any(ClassAssignment.class));
        verify(redirectAttributes).addFlashAttribute("success", "Subject and class successfully assigned to teacher");
        assertEquals("redirect:/teacher-assignments/1", redirectUrl);
    }

    @Test
    void assignSubjectAndClassShouldAddErrorMessageWhenExceptionOccurs() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(admin);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(classRepository.findById(1L)).thenReturn(Optional.of(schoolClass));
        when(classAssignmentRepository.save(any(ClassAssignment.class))).thenThrow(new RuntimeException("Test error"));

        // When
        String redirectUrl = teacherAssignmentController.assignSubjectAndClass(1L, 1L, 1L, redirectAttributes, authentication);

        // Then
        verify(classAssignmentRepository).save(any(ClassAssignment.class));
        verify(redirectAttributes).addFlashAttribute("error", "Failed to assign: Test error");
        assertEquals("redirect:/teacher-assignments/1", redirectUrl);
    }

    @Test
    void removeAssignmentShouldAddSuccessMessageWhenSuccessful() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(admin);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        doNothing().when(classAssignmentRepository).deleteById(1L);

        // When
        String redirectUrl = teacherAssignmentController.removeAssignment(1L, 1L, redirectAttributes, authentication);

        // Then
        verify(classAssignmentRepository).deleteById(1L);
        verify(redirectAttributes).addFlashAttribute("success", "Assignment successfully removed");
        assertEquals("redirect:/teacher-assignments/1", redirectUrl);
    }

    @Test
    void removeAssignmentShouldAddErrorMessageWhenExceptionOccurs() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(admin);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        doThrow(new RuntimeException("Test error")).when(classAssignmentRepository).deleteById(1L);

        // When
        String redirectUrl = teacherAssignmentController.removeAssignment(1L, 1L, redirectAttributes, authentication);

        // Then
        verify(classAssignmentRepository).deleteById(1L);
        verify(redirectAttributes).addFlashAttribute("error", "Failed to remove assignment: Test error");
        assertEquals("redirect:/teacher-assignments/1", redirectUrl);
    }
} 