package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.mapper.UserMapper;
import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.UserRepository;
import com.ednevnik.dnevnik.validation.UserValidator;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;
    private final UserMapper userMapper;


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserDto userDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {

            userDto.setRole(UserRole.ROLE_STUDENT);
            // Validate the user
            userValidator.validate(userMapper.toEntity(userDto), result);
            
            if (result.hasErrors()) {
                return "register";
            }

            // Check for duplicate username
            if (userRepository.existsByUsername(userDto.getUsername())) {
                result.rejectValue("username", "error.username", "Username already exists");
                return "register";
            }

            // Check for duplicate email
            if (userRepository.existsByEmail(userDto.getEmail())) {
                result.rejectValue("email", "error.email", "Email already exists");
                return "register";
            }

            // Check for duplicate national ID only if provided
            if (userDto.getNationalId() != null && !userDto.getNationalId().isEmpty() &&
                userRepository.existsByNationalId(userDto.getNationalId())) {
                result.rejectValue("nationalId", "error.nationalId", "National ID already exists");
                return "register";
            }
            
            // Encode password
            userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

            // Create user
            User user = userMapper.toEntity(userDto);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration. Please try again.");
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}