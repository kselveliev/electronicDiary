package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.Parent;
import com.ednevnik.dnevnik.model.Student;
import com.ednevnik.dnevnik.model.User;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.service.ParentService;
import com.ednevnik.dnevnik.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParentAssignmentControllerTest {

    @Mock
    private ParentService parentService;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ParentAssignmentController parentAssignmentController;

    private Parent parent;
    private Student student;
    private List<User> parents;
    private List<User> students;
    private Set<Student> assignedStudents;

    @BeforeEach
    void setUp() {
        // Setup test data
        parent = new Parent();
        parent.setId(1L);
        parent.setFirstName("John");
        parent.setLastName("Doe");
        parent.setRole(UserRole.ROLE_PARENT);

        student = new Student();
        student.setId(1L);
        student.setFirstName("Jane");
        student.setLastName("Smith");
        student.setRole(UserRole.ROLE_STUDENT);

        parents = Collections.singletonList(parent);
        students = Collections.singletonList(student);
        assignedStudents = new HashSet<>(Collections.singletonList(student));
    }

    @Test
    void showParentAssignmentsShouldAddParentsAndStudentsToModel() {
        // Given
        when(userService.findAllByRole("ROLE_PARENT")).thenReturn(parents);
        when(userService.findAllByRole("ROLE_STUDENT")).thenReturn(students);

        // When
        String viewName = parentAssignmentController.showParentAssignments(model);

        // Then
        verify(model).addAttribute("parents", parents);
        verify(model).addAttribute("students", students);
        assertEquals("parent-assignments/list", viewName);
    }

    @Test
    void showParentDetailsShouldAddParentAndStudentsToModel() {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(parent));
        when(parentService.findChildrenByParentId(1L)).thenReturn(assignedStudents);
        when(userService.findAllByRole("ROLE_STUDENT")).thenReturn(students);

        // When
        String viewName = parentAssignmentController.showParentDetails(1L, model);

        // Then
        verify(model).addAttribute("parent", parent);
        verify(model).addAttribute("assignedStudents", assignedStudents);
        verify(model).addAttribute("availableStudents", students);
        assertEquals("parent-assignments/details", viewName);
    }

    @Test
    void showParentDetailsShouldThrowExceptionWhenParentNotFound() {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            parentAssignmentController.showParentDetails(1L, model));
    }

    @Test
    void assignStudentShouldAddSuccessMessage() {
        // Given
        doNothing().when(parentService).assignChildToParent(1L, 1L);

        // When
        String redirectUrl = parentAssignmentController.assignStudent(1L, 1L, redirectAttributes);

        // Then
        verify(parentService).assignChildToParent(1L, 1L);
        verify(redirectAttributes).addFlashAttribute("success", "Student successfully assigned to parent");
        assertEquals("redirect:/parent-assignments/1", redirectUrl);
    }

    @Test
    void assignStudentShouldAddErrorMessageWhenExceptionOccurs() {
        // Given
        doThrow(new RuntimeException("Test error")).when(parentService).assignChildToParent(1L, 1L);

        // When
        String redirectUrl = parentAssignmentController.assignStudent(1L, 1L, redirectAttributes);

        // Then
        verify(parentService).assignChildToParent(1L, 1L);
        verify(redirectAttributes).addFlashAttribute("error", "Failed to assign student: Test error");
        assertEquals("redirect:/parent-assignments/1", redirectUrl);
    }

    @Test
    void removeStudentShouldAddSuccessMessage() {
        // Given
        doNothing().when(parentService).removeChildFromParent(1L, 1L);

        // When
        String redirectUrl = parentAssignmentController.removeStudent(1L, 1L, redirectAttributes);

        // Then
        verify(parentService).removeChildFromParent(1L, 1L);
        verify(redirectAttributes).addFlashAttribute("success", "Student successfully removed from parent");
        assertEquals("redirect:/parent-assignments/1", redirectUrl);
    }

    @Test
    void removeStudentShouldAddErrorMessageWhenExceptionOccurs() {
        // Given
        doThrow(new RuntimeException("Test error")).when(parentService).removeChildFromParent(1L, 1L);

        // When
        String redirectUrl = parentAssignmentController.removeStudent(1L, 1L, redirectAttributes);

        // Then
        verify(parentService).removeChildFromParent(1L, 1L);
        verify(redirectAttributes).addFlashAttribute("error", "Failed to remove student: Test error");
        assertEquals("redirect:/parent-assignments/1", redirectUrl);
    }
} 