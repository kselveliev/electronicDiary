package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.mapper.UserMapper;
import com.ednevnik.dnevnik.model.Student;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.repository.UserRepository;
import com.ednevnik.dnevnik.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserValidator userValidator;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AuthController authController;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setUsername("testuser");
        userDto.setEmail("test@example.com");
        userDto.setPassword("password123");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setPhoneNumber("1234567890");
    }

    @Test
    void showRegistrationFormShouldAddUserDtoToModel() {
        // When
        String viewName = authController.showRegistrationForm(model);

        // Then
        assertThat(viewName).isEqualTo("register");
        verify(model).addAttribute(anyString(), any(UserDto.class));

    }

    @Test
    void showLoginFormShouldReturnLoginView() {
        // When
        String viewName = authController.showLoginForm();

        // Then
        assertThat(viewName).isEqualTo("login");
    }

    @Test
    void registerUserShouldReturnRegisterViewWhenValidationFails() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        String viewName = authController.registerUser(userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("register");
        verify(userValidator).validate(userDto, bindingResult);
        verifyNoInteractions(userRepository, passwordEncoder, userMapper);
    }

    @Test
    void registerUserShouldReturnRegisterViewWhenUsernameExists() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        String viewName = authController.registerUser(userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("register");
        verify(bindingResult).rejectValue("username", "error.username", "Username already exists");
        verifyNoInteractions(passwordEncoder, userMapper);
    }

    @Test
    void registerUserShouldReturnRegisterViewWhenEmailExists() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        String viewName = authController.registerUser(userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("register");
        verify(bindingResult).rejectValue("email", "error.email", "Email already exists");
        verifyNoInteractions(passwordEncoder, userMapper);
    }

    @Test
    void registerUserShouldReturnRegisterViewWhenNationalIdExists() {
        // Given
        userDto.setNationalId("1234567890123");
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByNationalId("1234567890123")).thenReturn(true);

        // When
        String viewName = authController.registerUser(userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("register");
        verify(bindingResult).rejectValue("nationalId", "error.nationalId", "National ID already exists");
        verifyNoInteractions(passwordEncoder, userMapper);
    }

    @Test
    void registerUserShouldSetDefaultNationalIdWhenNotProvided() {
        // Given
        Student student = new Student();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userMapper.toEntity(any(UserDto.class))).thenReturn(student);

        // When
        String viewName = authController.registerUser(userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/login");
        assertThat(userDto.getNationalId()).isNotNull();
        assertThat(userDto.getRole()).isEqualTo(UserRole.ROLE_STUDENT);
        verify(passwordEncoder).encode("password123");
        verify(userMapper).toEntity(userDto);
        verify(userRepository).save(student);
        verify(redirectAttributes).addFlashAttribute("success", "Registration successful! Please login.");
    }

    @Test
    void registerUserShouldHandleException() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.toEntity(any(UserDto.class))).thenThrow(new RuntimeException("Database error"));

        // When
        String viewName = authController.registerUser(userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("register");
        verify(model).addAttribute("error", "An error occurred during registration. Please try again.");
    }
} 