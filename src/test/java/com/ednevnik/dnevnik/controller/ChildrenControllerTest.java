package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.Parent;
import com.ednevnik.dnevnik.model.Student;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import com.ednevnik.dnevnik.service.ParentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChildrenControllerTest {

    @Mock
    private ParentService parentService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetailsImpl userDetails;

    @InjectMocks
    private ChildrenController childrenController;

    private Parent parent;
    private Student student1;
    private Student student2;
    private Set<Student> children;

    @BeforeEach
    void setUp() {
        // Setup test data
        parent = new Parent();
        parent.setId(1L);
        parent.setFirstName("John");
        parent.setLastName("Doe");

        student1 = new Student();
        student1.setId(1L);
        student1.setFirstName("Jane");
        student1.setLastName("Doe");

        student2 = new Student();
        student2.setId(2L);
        student2.setFirstName("Jim");
        student2.setLastName("Doe");

        children = new HashSet<>();
        children.add(student1);
        children.add(student2);
    }

    @Test
    void showChildrenShouldReturnListViewWithChildren() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(parent);
        when(parentService.findChildrenByParentId(1L)).thenReturn(children);

        // When
        String viewName = childrenController.showChildren(model, authentication);

        // Then
        verify(parentService).findChildrenByParentId(1L);
        verify(model).addAttribute("children", children);
        assertEquals("children/list", viewName);
    }

    @Test
    void showChildrenShouldReturnListViewWithMessageWhenNoChildren() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(parent);
        when(parentService.findChildrenByParentId(1L)).thenReturn(new HashSet<>());

        // When
        String viewName = childrenController.showChildren(model, authentication);

        // Then
        verify(parentService).findChildrenByParentId(1L);
        verify(model).addAttribute("message", "No children found in your account.");
        assertEquals("children/list", viewName);
    }

    @Test
    void showChildGradesShouldRedirectToGrades() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(parent);
        when(parentService.findChildrenByParentId(1L)).thenReturn(children);

        // When
        String redirectUrl = childrenController.showChildGrades(1L, authentication);

        // Then
        verify(parentService).findChildrenByParentId(1L);
        assertEquals("redirect:/grades?studentId=1", redirectUrl);
    }

    @Test
    void showChildGradesShouldThrowExceptionWhenChildNotFound() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(parent);
        when(parentService.findChildrenByParentId(1L)).thenReturn(children);

        // When & Then
        try {
            childrenController.showChildGrades(999L, authentication);
            // If we reach here, the test should fail
            org.junit.jupiter.api.Assertions.fail("Expected exception was not thrown");
        } catch (RuntimeException e) {
            assertEquals("Child not found or not associated with parent", e.getMessage());
        }
    }
} 