package com.ednevnik.dnevnik.service;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        testUser = new Teacher();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(UserRole.ROLE_TEACHER);

        userDto = new UserDto();
        userDto.setUsername("newuser");
        userDto.setPassword("password123");
        userDto.setEmail("newuser@test.com");
        userDto.setFirstName("New");
        userDto.setLastName("User");
        userDto.setRole(UserRole.ROLE_TEACHER);
    }

    @Test
    void saveShouldEncodePasswordAndSaveUser() {
        when(passwordEncoder.encode(testUser.getPassword())).thenReturn(testUser.getPassword());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.save(testUser);

        assertThat(result).isEqualTo(testUser);
        verify(passwordEncoder).encode(testUser.getPassword());
        verify(userRepository).save(testUser);
    }

    @Test
    void existsByUsernameShouldReturnTrueWhenExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        boolean result = userService.existsByUsername("testuser");

        assertThat(result).isTrue();
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void existsByEmailShouldReturnTrueWhenExists() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        boolean result = userService.existsByEmail("test@test.com");

        assertThat(result).isTrue();
        verify(userRepository).existsByEmail("test@test.com");
    }

    @Test
    void findByUsernameShouldReturnUserWhenExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User result = userService.findByUsername("testuser");

        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findByUsernameShouldThrowExceptionWhenNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("unknown"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User not found");
    }

    @Test
    void createUserShouldCreateUserWithCorrectRole() {
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser(userDto);

        assertThat(result).isInstanceOf(Teacher.class);
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("newuser@test.com");
        assertThat(result.getRole()).isEqualTo(UserRole.ROLE_TEACHER);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(Teacher.class));
    }

    @Test
    void updateUserShouldUpdateExistingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser(1L, userDto);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(userDto.getUsername());
        assertThat(result.getEmail()).isEqualTo(userDto.getEmail());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserShouldThrowExceptionWhenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(1L, userDto))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User not found");
    }

    @Test
    void deleteUserShouldDeleteExistingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteUser(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUserShouldThrowExceptionWhenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User not found");
    }
} 