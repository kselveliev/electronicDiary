package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.School;
import com.ednevnik.dnevnik.model.Director;
import com.ednevnik.dnevnik.model.User;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.repository.SchoolRepository;
import com.ednevnik.dnevnik.repository.UserRepository;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolControllerTest {

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private SchoolController schoolController;

    private School school;
    private Director director;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);
        school.setName("Test School");
        school.setAddress("123 Test St");
        school.setCity("Test City");
        school.setPhoneNumber("1234567890");
        school.setEmail("school@test.com");

        director = new Director();
        director.setId(1L);
        director.setUsername("director");
        director.setEmail("director@test.com");
        director.setFirstName("Test");
        director.setLastName("Director");
        director.setRole(UserRole.ROLE_DIRECTOR);
        director.setSchool(school);

        school.setDirector(director);

        userDetails = new UserDetailsImpl(director);

        // Only set up the security context, don't stub the behavior yet
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void listSchoolsShouldShowAllSchoolsForAdmin() {
        // Given
        Director admin = new Director();
        admin.setRole(UserRole.ROLE_ADMIN);
        UserDetailsImpl adminDetails = new UserDetailsImpl(admin);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminDetails);
        when(schoolRepository.findAll()).thenReturn(List.of(school));

        // When
        String viewName = schoolController.listSchools(model);

        // Then
        assertThat(viewName).isEqualTo("schools/list");
        verify(model).addAttribute("schools", List.of(school));
        verify(model).addAttribute("currentUserRole", UserRole.ROLE_ADMIN);
    }

    @Test
    void listSchoolsShouldShowOnlyDirectorSchoolForDirector() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));

        // When
        String viewName = schoolController.listSchools(model);

        // Then
        assertThat(viewName).isEqualTo("schools/list");
        verify(model).addAttribute("schools", List.of(school));
        verify(model).addAttribute("currentUserRole", UserRole.ROLE_DIRECTOR);
    }

    @Test
    void listSchoolsShouldShowEmptyListForDirectorWithoutSchool() {
        // Given
        director.setSchool(null);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        String viewName = schoolController.listSchools(model);

        // Then
        assertThat(viewName).isEqualTo("schools/list");
        verify(model).addAttribute("schools", List.of());
        verify(model).addAttribute("currentUserRole", UserRole.ROLE_DIRECTOR);
    }

    @Test
    void showCreateFormShouldAddSchoolAndDirectorsToModel() {
        // Given
        List<User> availableDirectors = List.of(director);
        when(userRepository.findAvailableDirectors()).thenReturn(availableDirectors);

        // When
        String viewName = schoolController.showCreateForm(model);

        // Then
        assertThat(viewName).isEqualTo("schools/form");
        verify(model).addAttribute(anyString(), any(School.class));
        verify(model).addAttribute("availableDirectors", availableDirectors);
        verify(model).addAttribute("isAdmin", true);
    }

    @Test
    void createSchoolShouldSaveAndRedirect() {
        // When
        String viewName = schoolController.createSchool(school, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/schools");
        verify(schoolRepository).save(school);
        verify(redirectAttributes).addFlashAttribute("success", "School created successfully");
    }

    @Test
    void showEditFormShouldAddSchoolAndDirectorsToModelForAdmin() {
        // Given
        Director admin = new Director();
        admin.setRole(UserRole.ROLE_ADMIN);
        UserDetailsImpl adminDetails = new UserDetailsImpl(admin);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminDetails);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(userRepository.findAvailableDirectors()).thenReturn(List.of(director));

        // When
        String viewName = schoolController.showEditForm(1L, model);

        // Then
        assertThat(viewName).isEqualTo("schools/form");
        verify(model).addAttribute("school", school);
        verify(model).addAttribute("availableDirectors", List.of(director));
        verify(model).addAttribute("isAdmin", true);
    }

    @Test
    void showEditFormShouldAddSchoolToModelForDirector() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));

        // When
        String viewName = schoolController.showEditForm(1L, model);

        // Then
        assertThat(viewName).isEqualTo("schools/form");
        verify(model).addAttribute("school", school);
        verify(model).addAttribute("isAdmin", false);
        verifyNoInteractions(userRepository);
    }

    @Test
    void showEditFormShouldThrowExceptionWhenSchoolNotFound() {
        // Given
        when(schoolRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThat(catchThrowable(() -> schoolController.showEditForm(999L, model)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("School not found");
    }

    @Test
    void updateSchoolShouldUpdateAndRedirectForAdmin() {
        // Given
        Director admin = new Director();
        admin.setRole(UserRole.ROLE_ADMIN);
        UserDetailsImpl adminDetails = new UserDetailsImpl(admin);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminDetails);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));

        School updatedSchool = new School();
        updatedSchool.setName("Updated School");
        updatedSchool.setDirector(director);

        // When
        String viewName = schoolController.updateSchool(1L, updatedSchool, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/schools");
        verify(schoolRepository).save(any(School.class));
        verify(redirectAttributes).addFlashAttribute("success", "School updated successfully");
    }

    @Test
    void updateSchoolShouldUpdateBasicDetailsForDirector() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));

        School updatedSchool = new School();
        updatedSchool.setName("Updated School");
        Director newDirector = new Director();
        newDirector.setId(2L);
        updatedSchool.setDirector(newDirector);

        // When
        String viewName = schoolController.updateSchool(1L, updatedSchool, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/schools");
        verify(schoolRepository).save(argThat(savedSchool -> 
            savedSchool.getName().equals("Updated School") &&
            savedSchool.getDirector().equals(director) // Director should not be updated
        ));
        verify(redirectAttributes).addFlashAttribute("success", "School updated successfully");
    }

    @Test
    void updateSchoolShouldThrowExceptionWhenSchoolNotFound() {
        // Given
        when(schoolRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThat(catchThrowable(() -> schoolController.updateSchool(999L, school, redirectAttributes)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("School not found");
    }

    @Test
    void deleteSchoolShouldRemoveAssociationsAndDelete() {
        // Given
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        List<User> schoolUsers = List.of(director);
        when(userRepository.findAllBySchool(school)).thenReturn(schoolUsers);

        // When
        String viewName = schoolController.deleteSchool(1L, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/schools");
        verify(userRepository).saveAll(argThat(users -> {
            List<User> userList = new ArrayList<>();
            users.forEach(userList::add);
            return userList.stream().allMatch(user -> user.getSchool() == null);
        }));
        verify(userRepository).save(argThat(user -> 
            user instanceof Director && user.getSchool() == null
        ));
        verify(schoolRepository).deleteById(1L);
        verify(redirectAttributes).addFlashAttribute("success", "School deleted successfully");
    }

    @Test
    void deleteSchoolShouldThrowExceptionWhenSchoolNotFound() {
        // Given
        when(schoolRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThat(catchThrowable(() -> schoolController.deleteSchool(999L, redirectAttributes)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("School not found");
    }

    @Test
    void showDirectorFormShouldAddSchoolAndDirectorsToModel() {
        // Given
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(userRepository.findAvailableDirectors()).thenReturn(List.of(director));

        // When
        String viewName = schoolController.showDirectorForm(1L, model);

        // Then
        assertThat(viewName).isEqualTo("schools/director-form");
        verify(model).addAttribute("school", school);
        verify(model).addAttribute("directors", List.of(director));
    }

    @Test
    void updateDirectorShouldUpdateAndRedirect() {
        // Given
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(userRepository.findById(1L)).thenReturn(Optional.of(director));

        // When
        String viewName = schoolController.updateDirector(1L, director, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/schools");
        verify(schoolRepository).save(argThat(savedSchool -> 
            savedSchool.getDirector().equals(director)
        ));
        verify(redirectAttributes).addFlashAttribute("success", "School director updated successfully");
    }

    @Test
    void updateDirectorShouldThrowExceptionWhenDirectorNotFound() {
        // Given
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Director invalidDirector = new Director();
        invalidDirector.setId(999L);

        // When/Then
        assertThat(catchThrowable(() -> schoolController.updateDirector(1L, invalidDirector, redirectAttributes)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Director not found");
    }

    @Test
    void updateDirectorShouldThrowExceptionWhenUserNotDirector() {
        // Given
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        
        Director nonDirector = new Director();
        nonDirector.setId(2L);
        nonDirector.setRole(UserRole.ROLE_TEACHER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(nonDirector));

        // When/Then
        assertThat(catchThrowable(() -> schoolController.updateDirector(1L, nonDirector, redirectAttributes)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User is not a director");
    }
} 