package com.ednevnik.dnevnik.validation;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.model.User;
import com.ednevnik.dnevnik.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserUpdateValidator implements Validator {

    private final UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserDto userDto = (UserDto) target;
        User existingUser = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only validate fields that have been modified
        if (userDto.getUsername() != null && !userDto.getUsername().equals(existingUser.getUsername())) {
            validateUsername(userDto.getUsername(), errors);
        }

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            validateEmail(userDto.getEmail(), errors);
        }

        if (userDto.getNationalId() != null && !userDto.getNationalId().equals(existingUser.getNationalId())) {
            validateNationalId(userDto.getNationalId(), errors);
        }

        // Validate password only if it's being changed
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            validatePassword(userDto.getPassword(), errors);
        }

        // Validate required fields for update
        if (userDto.getFirstName() == null || userDto.getFirstName().trim().isEmpty()) {
            errors.rejectValue("firstName", "error.firstName", "First name is required");
        }

        if (userDto.getLastName() == null || userDto.getLastName().trim().isEmpty()) {
            errors.rejectValue("lastName", "error.lastName", "Last name is required");
        }
    }

    private void validateUsername(String username, Errors errors) {
        if (username == null || username.trim().isEmpty()) {
            errors.rejectValue("username", "error.username", "Username is required");
            return;
        }

        if (username.length() < 3 || username.length() > 20) {
            errors.rejectValue("username", "error.username", "Username must be between 3 and 20 characters");
            return;
        }

        if (!username.matches("^[a-zA-Z0-9._-]+$")) {
            errors.rejectValue("username", "error.username", "Username can only contain letters, numbers, dots, underscores and hyphens");
            return;
        }

        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            errors.rejectValue("username", "error.username", "Username is already taken");
        }
    }

    private void validateEmail(String email, Errors errors) {
        if (email == null || email.trim().isEmpty()) {
            errors.rejectValue("email", "error.email", "Email is required");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.rejectValue("email", "error.email", "Invalid email format");
            return;
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            errors.rejectValue("email", "error.email", "Email is already registered");
        }
    }

    private void validateNationalId(String nationalId, Errors errors) {
        if (nationalId == null || nationalId.trim().isEmpty()) {
            errors.rejectValue("nationalId", "error.nationalId", "National ID is required");
            return;
        }

        if (!nationalId.matches("^[0-9]{13}$")) {
            errors.rejectValue("nationalId", "error.nationalId", "National ID must be exactly 13 digits");
            return;
        }

        Optional<User> existingUser = userRepository.findByNationalId(nationalId);
        if (existingUser.isPresent()) {
            errors.rejectValue("nationalId", "error.nationalId", "National ID is already registered");
        }
    }

    private void validatePassword(String password, Errors errors) {
        if (password.length() < 8) {
            errors.rejectValue("password", "error.password", "Password must be at least 8 characters long");
            return;
        }

        if (!password.matches(".*[A-Z].*")) {
            errors.rejectValue("password", "error.password", "Password must contain at least one uppercase letter");
            return;
        }

        if (!password.matches(".*[a-z].*")) {
            errors.rejectValue("password", "error.password", "Password must contain at least one lowercase letter");
            return;
        }

        if (!password.matches(".*\\d.*")) {
            errors.rejectValue("password", "error.password", "Password must contain at least one number");
            return;
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            errors.rejectValue("password", "error.password", "Password must contain at least one special character");
        }
    }
} 