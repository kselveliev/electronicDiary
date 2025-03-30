package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.mapper.UserMapper;
import com.ednevnik.dnevnik.model.School;
import com.ednevnik.dnevnik.model.User;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.model.Director;
import com.ednevnik.dnevnik.repository.UserRepository;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@AllArgsConstructor
@RequestMapping("/director")
@PreAuthorize("hasRole('DIRECTOR')")
public class DirectorController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    @GetMapping("/users")
    public String listUsers(Model model) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User director = userDetails.getUser();
        School school = ((Director) director).getSchool();

        model.addAttribute("teachers", userRepository.findBySchoolAndRole(school, UserRole.ROLE_TEACHER));
        model.addAttribute("students", userRepository.findBySchoolAndRole(school, UserRole.ROLE_STUDENT));
        model.addAttribute("parents", userRepository.findBySchoolAndRole(school, UserRole.ROLE_PARENT));
        return "director/users/list";
    }

    @GetMapping("/users/new")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "director/users/form";
    }

    @PostMapping("/users/create")
    public String createUser(@Valid @ModelAttribute("user") UserDto userDto,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "director/users/form";
        }

        // Check for duplicate username
        if (userRepository.existsByUsername(userDto.getUsername())) {
            result.rejectValue("username", "error.username", "Username already exists");
            return "director/users/form";
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(userDto.getEmail())) {
            result.rejectValue("email", "error.email", "Email already exists");
            return "director/users/form";
        }

        // Check for duplicate national ID
        if (userRepository.existsByNationalId(userDto.getNationalId())) {
            result.rejectValue("nationalId", "error.nationalId", "National ID already exists");
            return "director/users/form";
        }

        // Get director's school
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User director = userDetails.getUser();
        School school = ((Director) director).getSchool();

        // Set school ID for the new user
        userDto.setSchoolId(school.getId());

        // Encode password
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Create user
        User user = userMapper.toEntity(userDto);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "User created successfully");
        return "redirect:/director/users";
    }
} 