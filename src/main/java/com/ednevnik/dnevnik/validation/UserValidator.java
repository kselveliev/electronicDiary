package com.ednevnik.dnevnik.validation;

import com.ednevnik.dnevnik.model.User;
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
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;

        // Username validation
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "field.required", "Username is required");
        if (user.getUsername() != null && user.getUsername().length() < 3) {
            errors.rejectValue("username", "field.min.length", 
                "Username must be at least 3 characters long");
        }
        if (user.getId() == null && userRepository.existsByUsername(user.getUsername())) {
            errors.rejectValue("username", "field.duplicate", "Username already exists");
        }

        // Email validation
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "field.required", "Email is required");
        if (user.getEmail() != null && !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.rejectValue("email", "field.invalid", "Please enter a valid email address");
        }
        if (user.getId() == null && userRepository.existsByEmail(user.getEmail())) {
            errors.rejectValue("email", "field.duplicate", "Email already exists");
        }

        // Password validation (only for new users or when password is provided)
        if (user.getId() == null || (user.getPassword() != null && !user.getPassword().isEmpty())) {
            if (user.getPassword() == null || user.getPassword().length() < 6) {
                errors.rejectValue("password", "field.min.length", 
                    "Password must be at least 6 characters long");
            }
        }

        // Name validation
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "field.required", "First name is required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "field.required", "Last name is required");

        // Role validation
        if (user.getRole() == null) {
            errors.rejectValue("role", "field.required", "Role is required");
        }

        // Phone number validation (optional)
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            if (!user.getPhoneNumber().matches("^\\+?[0-9]{10,15}$")) {
                errors.rejectValue("phoneNumber", "field.invalid", 
                    "Please enter a valid phone number (10-15 digits, may start with +)");
            }
        }

        // National ID validation (optional but unique if provided)
        if (user.getNationalId() != null && !user.getNationalId().isEmpty()) {
            if (user.getId() == null && userRepository.existsByNationalId(user.getNationalId())) {
                errors.rejectValue("nationalId", "field.duplicate", "National ID already exists");
            }
        }
    }
} 