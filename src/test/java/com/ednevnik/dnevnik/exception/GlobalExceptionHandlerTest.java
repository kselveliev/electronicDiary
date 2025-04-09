package com.ednevnik.dnevnik.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private static final String REFERER = "http://localhost:8080/test";
    private static final String DEFAULT_REDIRECT = "/";

    @BeforeEach
    void setUp() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
    }

    @Test
    void handleDataIntegrityViolationShouldRedirectToReferer() {
        // Given
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Test error", null);

        // When
        RedirectView result = globalExceptionHandler.handleDataIntegrityViolation(ex, request, redirectAttributes);

        // Then
        assertEquals(REFERER, result.getUrl());
        verify(redirectAttributes).addFlashAttribute("error", "An error occurred while processing your request. Please try again.");
    }

    @Test
    void handleDataIntegrityViolationWithConstraintViolationShouldRedirectToReferer() {
        // Given
        ConstraintViolationException cause = mock(ConstraintViolationException.class);
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Test error", cause);

        // When
        RedirectView result = globalExceptionHandler.handleDataIntegrityViolation(ex, request, redirectAttributes);

        // Then
        assertEquals(REFERER, result.getUrl());
        verify(redirectAttributes).addFlashAttribute("error", "Unable to complete the operation due to data constraints.");
    }

    @Test
    void handleDataIntegrityViolationWithoutRefererShouldRedirectToHome() {
        // Given
        when(request.getHeader("Referer")).thenReturn(null);
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Test error", null);

        // When
        RedirectView result = globalExceptionHandler.handleDataIntegrityViolation(ex, request, redirectAttributes);

        // Then
        assertEquals(DEFAULT_REDIRECT, result.getUrl());
        verify(redirectAttributes).addFlashAttribute("error", "An error occurred while processing your request. Please try again.");
    }

    @Test
    void handleIllegalArgumentShouldRedirectToReferer() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

        // When
        RedirectView result = globalExceptionHandler.handleIllegalArgument(ex, request, redirectAttributes);

        // Then
        assertEquals(REFERER, result.getUrl());
        verify(redirectAttributes).addFlashAttribute("error", "Invalid input provided: Invalid input");
    }

    @Test
    void handleIllegalArgumentWithoutRefererShouldRedirectToHome() {
        // Given
        when(request.getHeader("Referer")).thenReturn(null);
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

        // When
        RedirectView result = globalExceptionHandler.handleIllegalArgument(ex, request, redirectAttributes);

        // Then
        assertEquals(DEFAULT_REDIRECT, result.getUrl());
        verify(redirectAttributes).addFlashAttribute("error", "Invalid input provided: Invalid input");
    }

    @Test
    void handleGenericExceptionShouldRedirectToReferer() {
        // Given
        Exception ex = new RuntimeException("Unexpected error");

        // When
        RedirectView result = globalExceptionHandler.handleGenericException(ex, request, redirectAttributes);

        // Then
        assertEquals(REFERER, result.getUrl());
        verify(redirectAttributes).addFlashAttribute("error", "An unexpected error occurred. Please try again later.");
    }

    @Test
    void handleGenericExceptionWithoutRefererShouldRedirectToHome() {
        // Given
        when(request.getHeader("Referer")).thenReturn(null);
        Exception ex = new RuntimeException("Unexpected error");

        // When
        RedirectView result = globalExceptionHandler.handleGenericException(ex, request, redirectAttributes);

        // Then
        assertEquals(DEFAULT_REDIRECT, result.getUrl());
        verify(redirectAttributes).addFlashAttribute("error", "An unexpected error occurred. Please try again later.");
    }
} 