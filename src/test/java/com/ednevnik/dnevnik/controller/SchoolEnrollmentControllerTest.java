package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.SchoolRepository;
import com.ednevnik.dnevnik.repository.UserRepository;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolEnrollmentControllerTest {

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private SchoolEnrollmentController schoolEnrollmentController;

    private School school;
    private Director director;
    private Teacher teacher;
    private Student student;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);
        school.setName("Test School");

        director = new Director();
        director.setId(1L);
        director.setUsername("director");
        director.setRole(UserRole.ROLE_DIRECTOR);
        director.setSchool(school);
        school.setDirector(director);

        teacher = new Teacher();
        teacher.setId(2L);
        teacher.setUsername("teacher");
        teacher.setFirstName("Test");
        teacher.setLastName("Teacher");
        teacher.setRole(UserRole.ROLE_TEACHER);

        student = new Student();
        student.setId(3L);
        student.setUsername("student");
        student.setFirstName("Test");
        student.setLastName("Student");
        student.setRole(UserRole.ROLE_STUDENT);
    }

    @Test
    void showEnrollmentPageShouldDisplaySchoolUsers() {
        // Given
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = new UserDetailsImpl(director);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(userRepository.findBySchoolAndRole(school, UserRole.ROLE_TEACHER))
            .thenReturn(List.of(teacher));
        when(userRepository.findBySchoolAndRole(school, UserRole.ROLE_STUDENT))
            .thenReturn(List.of(student));
        when(userRepository.findByRoleAndSchoolIsNull(UserRole.ROLE_TEACHER))
            .thenReturn(List.of());
        when(userRepository.findByRoleAndSchoolIsNull(UserRole.ROLE_STUDENT))
            .thenReturn(List.of());

        try {
            // When
            String viewName = schoolEnrollmentController.showEnrollmentPage(1L, model);

            // Then
            assertThat(viewName).isEqualTo("schools/enrollment");
            verify(model).addAttribute("school", school);
            verify(model).addAttribute("schoolTeachers", List.of(teacher));
            verify(model).addAttribute("schoolStudents", List.of(student));
            verify(model).addAttribute("availableTeachers", List.of());
            verify(model).addAttribute("availableStudents", List.of());
        } finally {
            // Clean up the security context
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void showEnrollmentPageShouldThrowExceptionWhenSchoolNotFound() {
        // Given
        when(schoolRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThat(catchThrowable(() -> schoolEnrollmentController.showEnrollmentPage(999L, model)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("School not found");
    }

    @Test
    void assignTeacherShouldAddTeacherToSchool() {
        // Given
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));

        // When
        String viewName = schoolEnrollmentController.assignTeacher(1L, 2L, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/schools/1/enrollment");
        assertThat(teacher.getSchool()).isEqualTo(school);
        verify(userRepository).save(teacher);
        verify(redirectAttributes).addFlashAttribute("success", 
            String.format("Teacher %s %s successfully assigned to %s", 
                teacher.getFirstName(), teacher.getLastName(), school.getName()));
    }

    @Test
    void assignTeacherShouldThrowExceptionWhenSchoolNotFound() {
        // Given
        when(schoolRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThat(catchThrowable(() -> schoolEnrollmentController.assignTeacher(999L, 2L, redirectAttributes)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("School not found");
    }

    @Test
    void assignTeacherShouldThrowExceptionWhenTeacherNotFound() {
        // Given
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThat(catchThrowable(() -> schoolEnrollmentController.assignTeacher(1L, 999L, redirectAttributes)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Teacher not found");
    }

    @Test
    void assignTeacherShouldThrowExceptionWhenUserNotTeacher() {
        // Given
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(userRepository.findById(3L)).thenReturn(Optional.of(student));

        // When/Then
        assertThat(catchThrowable(() -> schoolEnrollmentController.assignTeacher(1L, 3L, redirectAttributes)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Selected user is not a teacher");
    }

    @Test
    void assignStudentShouldAddStudentToSchool() {
        // Given
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(userRepository.findById(3L)).thenReturn(Optional.of(student));

        // When
        String viewName = schoolEnrollmentController.assignStudent(1L, 3L, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/schools/1/enrollment");
        assertThat(student.getSchool()).isEqualTo(school);
        verify(userRepository).save(student);
        verify(redirectAttributes).addFlashAttribute("success", 
            String.format("Student %s %s successfully assigned to %s", 
                student.getFirstName(), student.getLastName(), school.getName()));
    }

    @Test
    void assignStudentShouldThrowExceptionWhenSchoolNotFound() {
        // Given
        when(schoolRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThat(catchThrowable(() -> schoolEnrollmentController.assignStudent(999L, 3L, redirectAttributes)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("School not found");
    }

    @Test
    void assignStudentShouldThrowExceptionWhenStudentNotFound() {
        // Given
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThat(catchThrowable(() -> schoolEnrollmentController.assignStudent(1L, 999L, redirectAttributes)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Student not found");
    }

    @Test
    void assignStudentShouldThrowExceptionWhenUserNotStudent() {
        // Given
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));

        // When/Then
        assertThat(catchThrowable(() -> schoolEnrollmentController.assignStudent(1L, 2L, redirectAttributes)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Selected user is not a student");
    }

    @Test
    void removeTeacherShouldRemoveTeacherFromSchool() {
        // Given
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        teacher.setSchool(school);
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));

        // When
        String viewName = schoolEnrollmentController.removeTeacher(1L, 2L, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/schools/1/enrollment");
        assertThat(teacher.getSchool()).isNull();
        verify(userRepository).save(teacher);
        verify(redirectAttributes).addFlashAttribute("success", 
            String.format("Teacher %s %s successfully removed from %s", 
                teacher.getFirstName(), teacher.getLastName(), school.getName()));
    }

    @Test
    void removeStudentShouldRemoveStudentFromSchool() {
        // Given
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        student.setSchool(school);
        when(userRepository.findById(3L)).thenReturn(Optional.of(student));

        // When
        String viewName = schoolEnrollmentController.removeStudent(1L, 3L, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/schools/1/enrollment");
        assertThat(student.getSchool()).isNull();
        verify(userRepository).save(student);
        verify(redirectAttributes).addFlashAttribute("success", 
            String.format("Student %s %s successfully removed from %s", 
                student.getFirstName(), student.getLastName(), school.getName()));
    }
} 