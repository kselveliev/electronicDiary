package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.model.School;
import com.ednevnik.dnevnik.model.Teacher;
import com.ednevnik.dnevnik.model.User;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.repository.SchoolRepository;
import com.ednevnik.dnevnik.repository.UserRepository;
import com.ednevnik.dnevnik.service.UserService;
import com.ednevnik.dnevnik.validation.UserValidator;
import com.ednevnik.dnevnik.validation.UserUpdateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserValidator userValidator;

    @Mock
    private UserUpdateValidator userUpdateValidator;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminController adminController;

    private UserDto userDto;
    private Teacher user;
    private School school;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setUsername("testuser");
        userDto.setEmail("test@example.com");
        userDto.setPassword("password123");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setRole(UserRole.ROLE_TEACHER);

        user = new Teacher();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(UserRole.ROLE_TEACHER);

        school = new School();
        school.setId(1L);
        school.setName("Test School");
    }

    @Test
    void showAdminPanelShouldAddStatisticsToModel() {
        // Given
        when(schoolRepository.count()).thenReturn(5L);
        when(userRepository.countByRole(UserRole.ROLE_TEACHER)).thenReturn(10L);
        when(userRepository.countByRole(UserRole.ROLE_STUDENT)).thenReturn(100L);

        // When
        String viewName = adminController.showAdminPanel(model);

        // Then
        assertThat(viewName).isEqualTo("admin/index");
        verify(model).addAttribute("totalSchools", 5L);
        verify(model).addAttribute("totalTeachers", 10L);
        verify(model).addAttribute("totalStudents", 100L);
    }

    @Test
    void listUsersShouldAddUsersToModel() {
        // Given
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);

        // When
        String viewName = adminController.listUsers(model);

        // Then
        assertThat(viewName).isEqualTo("admin/users/list");
        verify(model).addAttribute("users", users);
    }

    @Test
    void showCreateUserFormShouldAddUserDtoAndRolesToModel() {
        // When
        String viewName = adminController.showCreateUserForm(model);

        // Then
        assertThat(viewName).isEqualTo("admin/users/form");
        verify(model).addAttribute(eq("user"), any(UserDto.class));
        verify(model).addAttribute("roles", UserRole.values());
    }

    @Test
    void createUserShouldReturnFormViewWhenValidationFails() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        String viewName = adminController.createUser(userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("admin/users/form");
        verify(userValidator).validate(userDto, bindingResult);
        verify(model).addAttribute("roles", UserRole.values());
        verifyNoInteractions(userService);
    }

    @Test
    void createUserShouldRedirectToListWhenSuccessful() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.createUser(userDto)).thenReturn(user);

        // When
        String viewName = adminController.createUser(userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/admin/users");
        verify(userService).createUser(userDto);
        verify(redirectAttributes).addFlashAttribute("success", "User created successfully");
    }

    @Test
    void createUserShouldReturnFormViewWhenServiceThrowsException() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.createUser(userDto)).thenThrow(new RuntimeException("Error"));

        // When
        String viewName = adminController.createUser(userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("admin/users/form");
        verify(model).addAttribute("error", "Error");
        verify(model).addAttribute("roles", UserRole.values());
    }

    @Test
    void showEditUserFormShouldAddUserDtoAndRolesToModel() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        String viewName = adminController.showEditUserForm(1L, model);

        // Then
        assertThat(viewName).isEqualTo("admin/users/form");
        verify(model).addAttribute(eq("user"), any(UserDto.class));
        verify(model).addAttribute("roles", UserRole.values());
    }

    @Test
    void showEditUserFormShouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThat(catchThrowable(() -> adminController.showEditUserForm(1L, model)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid user Id:1");
    }

    @Test
    void updateUserShouldReturnFormViewWhenValidationFails() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        String viewName = adminController.updateUser(1L, userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("admin/users/form");
        verify(userUpdateValidator).validate(userDto, bindingResult);
        verify(model).addAttribute("roles", UserRole.values());
        verifyNoInteractions(userService);
    }

    @Test
    void updateUserShouldRedirectToListWhenSuccessful() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.updateUser(1L, userDto)).thenReturn(user);

        // When
        String viewName = adminController.updateUser(1L, userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/admin/users");
        verify(userService).updateUser(1L, userDto);
        verify(redirectAttributes).addFlashAttribute("success", "User updated successfully");
    }

    @Test
    void updateUserShouldReturnFormViewWhenServiceThrowsException() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.updateUser(1L, userDto)).thenThrow(new RuntimeException("Error"));

        // When
        String viewName = adminController.updateUser(1L, userDto, bindingResult, model, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("admin/users/form");
        verify(model).addAttribute("error", "Error");
        verify(model).addAttribute("roles", UserRole.values());
    }

    @Test
    void deleteUserShouldRedirectToListWithSuccessMessage() {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When
        String viewName = adminController.deleteUser(1L, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/admin/users");
        verify(userService).deleteUser(1L);
        verify(redirectAttributes).addFlashAttribute("success", "User deleted successfully");
    }

    @Test
    void deleteUserShouldRedirectToListWithErrorMessage() {
        // Given
        doThrow(new RuntimeException("Error")).when(userService).deleteUser(1L);

        // When
        String viewName = adminController.deleteUser(1L, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/admin/users");
        verify(redirectAttributes).addFlashAttribute("error", "Error");
    }
} 