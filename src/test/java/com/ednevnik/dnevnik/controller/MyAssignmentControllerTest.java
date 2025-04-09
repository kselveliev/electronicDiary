package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.ClassAssignment;
import com.ednevnik.dnevnik.model.Teacher;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.repository.ClassAssignmentRepository;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyAssignmentControllerTest {

    @Mock
    private ClassAssignmentRepository classAssignmentRepository;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetailsImpl userDetails;

    @InjectMocks
    private MyAssignmentController myAssignmentController;

    private Teacher teacher;
    private List<ClassAssignment> assignments;

    @BeforeEach
    void setUp() {
        // Setup test data
        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setRole(UserRole.ROLE_TEACHER);

        ClassAssignment assignment = new ClassAssignment();
        assignment.setId(1L);
        assignment.setTeacher(teacher);
        assignments = Collections.singletonList(assignment);
    }

    @Test
    void showMyAssignmentsShouldAddAssignmentsToModel() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(teacher);
        when(classAssignmentRepository.findByTeacherId(teacher.getId())).thenReturn(assignments);

        // When
        String viewName = myAssignmentController.showMyAssignments(model, authentication);

        // Then
        verify(model).addAttribute("assignments", assignments);
        assertEquals("teacher-assignments/my-assignments", viewName);
    }
} 