package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.Subject;
import com.ednevnik.dnevnik.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectControllerTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private SubjectController subjectController;

    private Subject subject;
    private List<Subject> subjects;

    @BeforeEach
    void setUp() {
        // Setup test data
        subject = new Subject();
        subject.setId(1L);
        subject.setName("Mathematics");
        subjects = Collections.singletonList(subject);
    }

    @Test
    void listSubjectsShouldAddSubjectsAndNewSubjectToModel() {
        // Given
        when(subjectRepository.findAll()).thenReturn(subjects);

        // When
        String viewName = subjectController.listSubjects(model);

        // Then
        verify(model).addAttribute("subjects", subjects);
        verify(model).addAttribute(any(), any(Subject.class));
        assertEquals("subjects/list", viewName);
    }

    @Test
    void createSubjectShouldAddSuccessMessageWhenSuccessful() {
        // Given
        when(subjectRepository.save(any(Subject.class))).thenReturn(subject);

        // When
        String redirectUrl = subjectController.createSubject(subject, redirectAttributes);

        // Then
        verify(subjectRepository).save(subject);
        verify(redirectAttributes).addFlashAttribute("success", "Subject created successfully");
        assertEquals("redirect:/subjects", redirectUrl);
    }

    @Test
    void createSubjectShouldAddErrorMessageWhenExceptionOccurs() {
        // Given
        when(subjectRepository.save(any(Subject.class))).thenThrow(new RuntimeException("Test error"));

        // When
        String redirectUrl = subjectController.createSubject(subject, redirectAttributes);

        // Then
        verify(subjectRepository).save(subject);
        verify(redirectAttributes).addFlashAttribute("error", "Failed to create subject: Test error");
        assertEquals("redirect:/subjects", redirectUrl);
    }

    @Test
    void deleteSubjectShouldAddSuccessMessageWhenSuccessful() {
        // Given
        doNothing().when(subjectRepository).deleteById(1L);

        // When
        String redirectUrl = subjectController.deleteSubject(1L, redirectAttributes);

        // Then
        verify(subjectRepository).deleteById(1L);
        verify(redirectAttributes).addFlashAttribute("success", "Subject deleted successfully");
        assertEquals("redirect:/subjects", redirectUrl);
    }

    @Test
    void deleteSubjectShouldAddErrorMessageWhenExceptionOccurs() {
        // Given
        doThrow(new RuntimeException("Test error")).when(subjectRepository).deleteById(1L);

        // When
        String redirectUrl = subjectController.deleteSubject(1L, redirectAttributes);

        // Then
        verify(subjectRepository).deleteById(1L);
        verify(redirectAttributes).addFlashAttribute("error", "Failed to delete subject: Test error");
        assertEquals("redirect:/subjects", redirectUrl);
    }

    @Test
    void updateSubjectShouldAddSuccessMessageWhenSuccessful() {
        // Given
        Subject existingSubject = new Subject();
        existingSubject.setId(1L);
        existingSubject.setName("Old Name");

        Subject updatedSubject = new Subject();
        updatedSubject.setId(1L);
        updatedSubject.setName("New Name");

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(existingSubject));
        when(subjectRepository.save(any(Subject.class))).thenReturn(updatedSubject);

        // When
        String redirectUrl = subjectController.updateSubject(1L, updatedSubject, redirectAttributes);

        // Then
        verify(subjectRepository).findById(1L);
        verify(subjectRepository).save(any(Subject.class));
        verify(redirectAttributes).addFlashAttribute("success", "Subject updated successfully");
        assertEquals("redirect:/subjects", redirectUrl);
    }

    @Test
    void updateSubjectShouldAddErrorMessageWhenSubjectNotFound() {
        // Given
        when(subjectRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        String redirectUrl = subjectController.updateSubject(1L, subject, redirectAttributes);

        // Then
        verify(subjectRepository).findById(1L);
        verify(redirectAttributes).addFlashAttribute("error", "Failed to update subject: Invalid subject ID");
        assertEquals("redirect:/subjects", redirectUrl);
    }

    @Test
    void updateSubjectShouldAddErrorMessageWhenExceptionOccurs() {
        // Given
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(any(Subject.class))).thenThrow(new RuntimeException("Test error"));

        // When
        String redirectUrl = subjectController.updateSubject(1L, subject, redirectAttributes);

        // Then
        verify(subjectRepository).findById(1L);
        verify(subjectRepository).save(any(Subject.class));
        verify(redirectAttributes).addFlashAttribute("error", "Failed to update subject: Test error");
        assertEquals("redirect:/subjects", redirectUrl);
    }
} 