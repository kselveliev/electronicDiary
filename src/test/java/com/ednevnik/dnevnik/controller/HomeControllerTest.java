package com.ednevnik.dnevnik.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private HomeController homeController;

    @Test
    void indexShouldRedirectToLoginWhenNotAuthenticated() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        String viewName = homeController.index(authentication, model);

        // Then
        assertThat(viewName).isEqualTo("redirect:/login");
    }

    @Test
    void indexShouldRedirectToLoginWhenAuthenticationIsNull() {
        // When
        String viewName = homeController.index(null, model);

        // Then
        assertThat(viewName).isEqualTo("redirect:/login");
    }

    @Test
    void indexShouldShowIndexPageWhenAuthenticated() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);

        // When
        String viewName = homeController.index(authentication, model);

        // Then
        assertThat(viewName).isEqualTo("index");
    }
} 