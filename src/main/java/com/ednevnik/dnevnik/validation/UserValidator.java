package com.ednevnik.dnevnik.validation;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

    private final UserRepository userRepository;

    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UserDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserDto userDto = (UserDto) target;

        // Username validation
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "field.required", "Username is required");
        if (userDto.getUsername() != null && userDto.getUsername().length() < 3) {
            errors.rejectValue("username", "field.min.length", 
                "Username must be at least 3 characters long");
        }
        if (userDto.getId() == null && userRepository.existsByUsername(userDto.getUsername())) {
            errors.rejectValue("username", "field.duplicate", "Username already exists");
        }

        // Email validation
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "field.required", "Email is required");
        if (userDto.getEmail() != null && !userDto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.rejectValue("email", "field.invalid", "Please enter a valid email address");
        }
        if (userDto.getId() == null && userRepository.existsByEmail(userDto.getEmail())) {
            errors.rejectValue("email", "field.duplicate", "Email already exists");
        }

        // Password validation (only for new users or when password is provided)
        if (userDto.getId() == null || (userDto.getPassword() != null && !userDto.getPassword().isEmpty())) {
            if (userDto.getPassword() == null || userDto.getPassword().length() < 6) {
                errors.rejectValue("password", "field.min.length", 
                    "Password must be at least 6 characters long");
            }
        }

        // Name validation
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "field.required", "First name is required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "field.required", "Last name is required");

        // Role validation
        if (userDto.getRole() == null) {
            errors.rejectValue("role", "field.required", "Role is required");
        }

        // Phone number validation (optional)
        if (userDto.getPhoneNumber() != null && !userDto.getPhoneNumber().isEmpty()) {
            if (!userDto.getPhoneNumber().matches("^\\+?[0-9]{10,15}$")) {
                errors.rejectValue("phoneNumber", "field.invalid", 
                    "Please enter a valid phone number (10-15 digits, may start with +)");
            }
        }

        // National ID validation (optional but unique if provided)
        if (userDto.getNationalId() != null && !userDto.getNationalId().isEmpty()) {
            if (userDto.getId() == null && userRepository.existsByNationalId(userDto.getNationalId())) {
                errors.rejectValue("nationalId", "field.duplicate", "National ID already exists");
            }
        }
    }
} 