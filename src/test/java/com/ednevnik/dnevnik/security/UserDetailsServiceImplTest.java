package com.ednevnik.dnevnik.security;

import com.ednevnik.dnevnik.model.Admin;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserDetailsServiceImplTest {

    private UserDetailsServiceImpl userDetailsService;
    private UserRepository userRepository;
    private Admin testUser;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userDetailsService = new UserDetailsServiceImpl(userRepository);

        testUser = new Admin();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@email.com");
        testUser.setPassword("password");
        testUser.setRole(UserRole.ROLE_ADMIN);
    }

    @Test
    void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("password");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsernameShouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("User not found with username: nonexistent");
    }
} 