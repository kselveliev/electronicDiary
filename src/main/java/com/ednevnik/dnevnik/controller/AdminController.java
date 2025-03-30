package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.mapper.UserMapper;
import com.ednevnik.dnevnik.model.User;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.repository.SchoolRepository;
import com.ednevnik.dnevnik.repository.UserRepository;
import com.ednevnik.dnevnik.validation.UserValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final UserMapper userMapper;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository,
                         SchoolRepository schoolRepository,
                         UserMapper userMapper,
                         UserValidator userValidator,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.userMapper = userMapper;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showAdminPanel(Model model) {
        model.addAttribute("totalSchools", schoolRepository.count());
        model.addAttribute("totalTeachers", userRepository.countByRole(UserRole.ROLE_TEACHER));
        model.addAttribute("totalStudents", userRepository.countByRole(UserRole.ROLE_STUDENT));
        return "admin/index";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users/list";
    }

    @GetMapping("/users/new")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "admin/users/form";
    }

    @PostMapping("/users/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(@Valid @ModelAttribute("user") UserDto userDto,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("schools", schoolRepository.findAll());
            return "admin/users/form";
        }

        // Check for duplicate username
        if (userRepository.existsByUsername(userDto.getUsername())) {
            result.rejectValue("username", "error.username", "Username already exists");
            model.addAttribute("schools", schoolRepository.findAll());
            return "admin/users/form";
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(userDto.getEmail())) {
            result.rejectValue("email", "error.email", "Email already exists");
            model.addAttribute("schools", schoolRepository.findAll());
            return "admin/users/form";
        }

        // Check for duplicate national ID
        if (userRepository.existsByNationalId(userDto.getNationalId())) {
            result.rejectValue("nationalId", "error.nationalId", "National ID already exists");
            model.addAttribute("schools", schoolRepository.findAll());
            return "admin/users/form";
        }

        // Encode password
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Create user
        User user = userMapper.toEntity(userDto);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "User created successfully");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", userMapper.toDto(user));
        return "admin/users/form";
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                           @ModelAttribute("user") @Valid UserDto userDto,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        // Set the ID to ensure we're updating the correct user
        userDto.setId(id);
        
        // Validate the user
        userValidator.validate(userMapper.toEntity(userDto), result);
        
        if (result.hasErrors()) {
            return "admin/users/form";
        }

        // Update the existing user with the new data
        userMapper.updateEntityFromDto(existingUser, userDto);
        
        // If a new password was provided, encode it
        if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        userRepository.save(existingUser);
        redirectAttributes.addFlashAttribute("success", "User updated successfully");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        
        userRepository.delete(user);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        return "redirect:/admin/users";
    }
} 