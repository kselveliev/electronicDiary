package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.model.User;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.repository.SchoolRepository;
import com.ednevnik.dnevnik.repository.UserRepository;
import com.ednevnik.dnevnik.service.UserService;
import com.ednevnik.dnevnik.validation.UserValidator;
import com.ednevnik.dnevnik.validation.UserUpdateValidator;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@AllArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final UserService userService;
    private final UserValidator userValidator;
    private final UserUpdateValidator userUpdateValidator;

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
        model.addAttribute("roles", UserRole.values());
        return "admin/users/form";
    }

    @PostMapping("/users/create")
    public String createUser(@Valid @ModelAttribute("user") UserDto userDto,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        // Validate using the creation validator
        userValidator.validate(userDto, result);
        
        if (result.hasErrors()) {
            model.addAttribute("roles", UserRole.values());
            return "admin/users/form";
        }

        try {
            userService.createUser(userDto);
            redirectAttributes.addFlashAttribute("success", "User created successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", UserRole.values());
            return "admin/users/form";
        }
    }

    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setNationalId(user.getNationalId());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setRole(user.getRole());
        
        model.addAttribute("user", userDto);
        model.addAttribute("roles", UserRole.values());
        return "admin/users/form";
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                           @Valid @ModelAttribute("user") UserDto userDto,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        // Validate using the update validator
        userUpdateValidator.validate(userDto, result);
        
        if (result.hasErrors()) {
            model.addAttribute("roles", UserRole.values());
            return "admin/users/form";
        }

        try {
            userService.updateUser(id, userDto);
            redirectAttributes.addFlashAttribute("success", "User updated successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", UserRole.values());
            return "admin/users/form";
        }
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
} 