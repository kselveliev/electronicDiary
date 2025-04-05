package com.ednevnik.dnevnik.validation;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.model.Student;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUpdateValidatorTest {

    @Mock
    private UserRepository userRepository;

    private UserUpdateValidator userUpdateValidator;

    @BeforeEach
    void setUp() {
        userUpdateValidator = new UserUpdateValidator(userRepository);
    }

    @Test
    void supportsUserDto() {
        assertThat(userUpdateValidator.supports(UserDto.class)).isTrue();
    }

    @Test
    void supportString() {
        assertThat(userUpdateValidator.supports(String.class)).isFalse();
    }

    @Test
    void validateShortUsername() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("ab");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        Student existingUser = new Student();
        existingUser.setUsername("oldUsername");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // When
        userUpdateValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("username")).isNotNull();
        assertThat(errors.getFieldError("username").getDefaultMessage()).isEqualTo("Username must be between 3 and 20 characters");
    }

    @Test
    void validateUsernameAlreadyExists() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("newUsername");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        Student existingUser = new Student();
        existingUser.setUsername("oldUsername");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("newUsername")).thenReturn(Optional.of(new Student()));

        // When
        userUpdateValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("username")).isNotNull();
        assertThat(errors.getFieldError("username").getDefaultMessage()).isEqualTo("Username is already taken");
    }

    @Test
    void validateNoErrorWhenUsernameIsNotChanged() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("existingUsername");
        userDto.setFirstName("firstName");
        userDto.setLastName("lastName");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        Student existingUser = new Student();
        existingUser.setUsername("existingUsername");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // When
        userUpdateValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void validateInvalidInvalid() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("invalid-email");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        Student existingUser = new Student();
        existingUser.setEmail("old@email.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // When
        userUpdateValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("email")).isNotNull();
        assertThat(errors.getFieldError("email").getDefaultMessage()).isEqualTo("Invalid email format");
    }

    @Test
    void validateEmailAlreadyExists() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("new@email.com");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        Student existingUser = new Student();
        existingUser.setEmail("old@email.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("new@email.com")).thenReturn(Optional.of(new Student()));

        // When
        userUpdateValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("email")).isNotNull();
        assertThat(errors.getFieldError("email").getDefaultMessage()).isEqualTo("Email is already registered");
    }

    @Test
    void validateNoErrorWhenEmailIsNotChanged() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("existingUsername");
        userDto.setFirstName("firstName");
        userDto.setLastName("lastName");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        Student existingUser = new Student();
        existingUser.setEmail("existing@email.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // When
        userUpdateValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void validateShortPassword() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setPassword("12345");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        Student existingUser = new Student();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // When
        userUpdateValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("password")).isNotNull();
        assertThat(errors.getFieldError("password").getDefaultMessage()).isEqualTo("Password must be at least 8 characters long");
    }

    @Test
    void validateNoErrorWhenPasswordIsNotChanged() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setFirstName("firstName");
        userDto.setLastName("lastName");
        userDto.setPassword(null);
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        Student existingUser = new Student();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // When
        userUpdateValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void validateFirstNameEmpty() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setFirstName("");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        Student existingUser = new Student();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // When
        userUpdateValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("firstName")).isNotNull();
        assertThat(errors.getFieldError("firstName").getDefaultMessage()).isEqualTo("First name is required");
    }

    @Test
    void validateLastNameEmpty() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setLastName("");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        Student existingUser = new Student();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // When
        userUpdateValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("lastName")).isNotNull();
        assertThat(errors.getFieldError("lastName").getDefaultMessage()).isEqualTo("Last name is required");
    }

    @Test
    void validateNationalIdAlreadyExists() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setNationalId("1234567890123");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        Student existingUser = new Student();
        existingUser.setNationalId("9876543210987");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByNationalId("1234567890123")).thenReturn(Optional.of(new Student()));

        // When
        userUpdateValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("nationalId")).isNotNull();
        assertThat(errors.getFieldError("nationalId").getDefaultMessage()).isEqualTo("National ID is already registered");
    }

    @Test
    void validateNoErrorWhenNationalIdIsNotChanged() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setFirstName("firstName");
        userDto.setLastName("lastName");
        userDto.setNationalId("1234567890123");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        Student existingUser = new Student();
        existingUser.setNationalId("1234567890123");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // When
        userUpdateValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void validateNoErrorsWhenAllFieldsAreValid() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("validUser");
        userDto.setEmail("valid@email.com");
        userDto.setPassword("validPassword123.");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setRole(UserRole.ROLE_STUDENT);
        userDto.setPhoneNumber("+38123456789");
        userDto.setNationalId("1234567890123");
        Errors errors = new BeanPropertyBindingResult(userDto, "user");

        Student existingUser = new Student();
        existingUser.setUsername("oldUsername");
        existingUser.setEmail("old@email.com");
        existingUser.setNationalId("9876543210987");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByNationalId(anyString())).thenReturn(Optional.empty());

        // When
        userUpdateValidator.validate(userDto, errors);

        // Then
        assertThat(errors.hasErrors()).isFalse();
    }
} 