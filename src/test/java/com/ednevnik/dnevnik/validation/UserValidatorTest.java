package com.ednevnik.dnevnik.validation;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;

    private UserValidator userValidator;

    @BeforeEach
    void setUp() {
        userValidator = new UserValidator(userRepository);
    }

    @Test
    void supportsShouldReturnTrueWhenClassIsUserDto() {
        assertThat(userValidator.supports(UserDto.class)).isTrue();
    }

    @Test
    void supportsShouldReturnFalseWhenClassIsNotUserDto() {
        assertThat(userValidator.supports(String.class)).isFalse();
    }

    @Test
    void validateShouldAddErrorWhenUsernameIsEmpty() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setUsername("");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        // When
        userValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("username")).isNotNull();
        assertThat(errors.getFieldError("username").getDefaultMessage()).isEqualTo("Username is required");
    }

    @Test
    void validateShouldAddErrorWhenUsernameIsTooShort() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setUsername("ab");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        // When
        userValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("username")).isNotNull();
        assertThat(errors.getFieldError("username").getDefaultMessage()).isEqualTo("Username must be at least 3 characters long");
    }

    @Test
    void validateShouldAddErrorWhenUsernameAlreadyExists() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setUsername("existingUser");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");
        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        // When
        userValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("username")).isNotNull();
        assertThat(errors.getFieldError("username").getDefaultMessage()).isEqualTo("Username already exists");
    }

    @Test
    void validateShouldAddErrorWhenEmailIsEmpty() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setEmail("");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        // When
        userValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("email")).isNotNull();
        assertThat(errors.getFieldError("email").getDefaultMessage()).isEqualTo("Email is required");
    }

    @Test
    void validateShouldAddErrorWhenEmailIsInvalid() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setEmail("invalid-email");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        // When
        userValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("email")).isNotNull();
        assertThat(errors.getFieldError("email").getDefaultMessage()).isEqualTo("Please enter a valid email address");
    }

    @Test
    void validateShouldAddErrorWhenEmailAlreadyExists() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setEmail("existing@email.com");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");
        when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);

        // When
        userValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("email")).isNotNull();
        assertThat(errors.getFieldError("email").getDefaultMessage()).isEqualTo("Email already exists");
    }

    @Test
    void validateShouldAddErrorWhenPasswordIsEmpty() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setPassword("");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        // When
        userValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("password")).isNotNull();
        assertThat(errors.getFieldError("password").getDefaultMessage()).isEqualTo("Password must be at least 6 characters long");
    }

    @Test
    void validateShouldAddErrorWhenPasswordIsTooShort() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setPassword("12345");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        // When
        userValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("password")).isNotNull();
        assertThat(errors.getFieldError("password").getDefaultMessage()).isEqualTo("Password must be at least 6 characters long");
    }

    @Test
    void validateShouldAddErrorWhenFirstNameIsEmpty() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setFirstName("");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        // When
        userValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("firstName")).isNotNull();
        assertThat(errors.getFieldError("firstName").getDefaultMessage()).isEqualTo("First name is required");
    }

    @Test
    void validateShouldAddErrorWhenLastNameIsEmpty() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setLastName("");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        // When
        userValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("lastName")).isNotNull();
        assertThat(errors.getFieldError("lastName").getDefaultMessage()).isEqualTo("Last name is required");
    }

    @Test
    void validateShouldAddErrorWhenRoleIsNull() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setRole(null);
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        // When
        userValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("role")).isNotNull();
        assertThat(errors.getFieldError("role").getDefaultMessage()).isEqualTo("Role is required");
    }

    @Test
    void validateShouldAddErrorWhenPhoneNumberIsInvalid() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setPhoneNumber("123"); // Too short
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        // When
        userValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("phoneNumber")).isNotNull();
        assertThat(errors.getFieldError("phoneNumber").getDefaultMessage()).isEqualTo("Please enter a valid phone number (10-15 digits, may start with +)");
    }

    @Test
    void validateShouldAddErrorWhenNationalIdAlreadyExists() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setNationalId("1234567890123");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");
        when(userRepository.existsByNationalId("1234567890123")).thenReturn(true);

        // When
        userValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("nationalId")).isNotNull();
        assertThat(errors.getFieldError("nationalId").getDefaultMessage()).isEqualTo("National ID already exists");
    }

    @Test
    void validateShouldNotAddErrorsWhenAllFieldsAreValid() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setUsername("validUser");
        userDto.setEmail("valid@email.com");
        userDto.setPassword("validPassword123");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setRole(UserRole.ROLE_STUDENT);
        userDto.setPhoneNumber("+38123456789");
        userDto.setNationalId("1234567890123");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByNationalId(anyString())).thenReturn(false);

        // When
        userValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isFalse();
    }
} 