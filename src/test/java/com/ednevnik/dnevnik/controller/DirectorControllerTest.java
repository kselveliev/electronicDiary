package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.mapper.UserMapper;
import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.UserRepository;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectorControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private DirectorController directorController;

    private School school;
    private Director director;
    private Teacher teacher;
    private Student student;
    private Parent parent;
    private UserDto userDto;

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
        teacher.setSchool(school);

        student = new Student();
        student.setId(3L);
        student.setUsername("student");
        student.setFirstName("Test");
        student.setLastName("Student");
        student.setRole(UserRole.ROLE_STUDENT);
        student.setSchool(school);

        parent = new Parent();
        parent.setId(4L);
        parent.setUsername("parent");
        parent.setFirstName("Test");
        parent.setLastName("Parent");
        parent.setRole(UserRole.ROLE_PARENT);

        userDto = new UserDto();
        userDto.setUsername("newuser");
        userDto.setEmail("newuser@test.com");
        userDto.setPassword("password123");
        userDto.setFirstName("New");
        userDto.setLastName("User");
        userDto.setPhoneNumber("1234567890");
        userDto.setSchoolId(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupAuthentication() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserDetailsImpl userDetails = new UserDetailsImpl(director);
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void listUsersShouldDisplaySchoolUsers() {
        // Given
        setupAuthentication();
        when(userRepository.findBySchoolAndRole(any(), eq(UserRole.ROLE_TEACHER)))
            .thenReturn(List.of(teacher));
        when(userRepository.findBySchoolAndRole(any(), eq(UserRole.ROLE_STUDENT)))
            .thenReturn(List.of(student));
        when(userRepository.findBySchoolAndRole(any(), eq(UserRole.ROLE_PARENT)))
            .thenReturn(List.of(parent));

        // When
        String viewName = directorController.listUsers(model);

        // Then
        assertThat(viewName).isEqualTo("director/users/list");
        verify(model).addAttribute("teachers", List.of(teacher));
        verify(model).addAttribute("students", List.of(student));
        verify(model).addAttribute("parents", List.of(parent));
    }

    @Test
    void showCreateUserFormShouldAddUserDtoToModel() {
        // When
        String viewName = directorController.showCreateUserForm(model);

        // Then
        assertThat(viewName).isEqualTo("director/users/form");
        verify(model).addAttribute(eq("user"), any(UserDto.class));
    }

    @Test
    void createUserShouldReturnFormViewWhenValidationFails() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        String viewName = directorController.createUser(userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("director/users/form");
        verifyNoInteractions(userRepository, passwordEncoder, userMapper);
    }

    @Test
    void createUserShouldReturnFormViewWhenUsernameExists() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // When
        String viewName = directorController.createUser(userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("director/users/form");
        verify(bindingResult).rejectValue("username", "error.username", "Username already exists");
        verifyNoInteractions(passwordEncoder, userMapper);
    }

    @Test
    void createUserShouldReturnFormViewWhenEmailExists() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(true);

        // When
        String viewName = directorController.createUser(userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("director/users/form");
        verify(bindingResult).rejectValue("email", "error.email", "Email already exists");
        verifyNoInteractions(passwordEncoder, userMapper);
    }

    @Test
    void createUserShouldReturnFormViewWhenNationalIdExists() {
        // Given
        userDto.setNationalId("1234567890");
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(userRepository.existsByNationalId("1234567890")).thenReturn(true);

        // When
        String viewName = directorController.createUser(userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("director/users/form");
        verify(bindingResult).rejectValue("nationalId", "error.nationalId", "National ID already exists");
        verifyNoInteractions(passwordEncoder, userMapper);
    }

    @Test
    void createUserShouldSaveAndRedirectWhenValid() {
        // Given
        setupAuthentication();
        Student newStudent = new Student();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userMapper.toEntity(userDto)).thenReturn(newStudent);

        // When
        String viewName = directorController.createUser(userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/director/users");
        verify(passwordEncoder).encode("password123");
        verify(userMapper).toEntity(userDto);
        verify(userRepository).save(newStudent);
        verify(redirectAttributes).addFlashAttribute("success", "User created successfully");
    }
} 