package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.School;
import com.ednevnik.dnevnik.model.User;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.repository.SchoolRepository;
import com.ednevnik.dnevnik.repository.UserRepository;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/schools")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
public class SchoolController {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String listSchools(Model model) {
        model.addAttribute("schools", schoolRepository.findAll());
        return "schools/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("school", new School());
        model.addAttribute("availableDirectors", userRepository.findAvailableDirectors());
        return "schools/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createSchool(@ModelAttribute School school, RedirectAttributes redirectAttributes) {
        schoolRepository.save(school);
        redirectAttributes.addFlashAttribute("success", "School created successfully");
        return "redirect:/schools";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found"));
        model.addAttribute("school", school);
        model.addAttribute("availableDirectors", userRepository.findAvailableDirectors());
        return "schools/form";
    }

    @PostMapping("/{id}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateSchool(@PathVariable Long id, @ModelAttribute School school, RedirectAttributes redirectAttributes) {
        school.setId(id);
        schoolRepository.save(school);
        redirectAttributes.addFlashAttribute("success", "School updated successfully");
        return "redirect:/schools";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteSchool(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found"));

        // Get all users associated with this school
        List<User> schoolUsers = userRepository.findAllBySchool(school);

        // Remove school association from all users
        for (User user : schoolUsers) {
            user.setSchool(null);
        }
        userRepository.saveAll(schoolUsers);

        // Remove director association
        if (school.getDirector() != null) {
            school.getDirector().setSchool(null);
            userRepository.save(school.getDirector());
        }

        // Now delete the school
        schoolRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "School deleted successfully");
        return "redirect:/schools";
    }

    @GetMapping("/{id}/director")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public String showDirectorForm(@PathVariable Long id, Model model) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userDetails.getUser();

        if (currentUser.getRole() == UserRole.ROLE_DIRECTOR && !school.getDirector().equals(currentUser)) {
            throw new RuntimeException("Access denied");
        }

        List<User> directors = userRepository.findAvailableDirectors();

        model.addAttribute("school", school);
        model.addAttribute("directors", directors);
        return "schools/director-form";
    }

    @PostMapping("/{id}/director")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public String updateDirector(@PathVariable Long id,
                               @ModelAttribute User director,
                               RedirectAttributes redirectAttributes) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userDetails.getUser();

        if (currentUser.getRole() == UserRole.ROLE_DIRECTOR && !school.getDirector().equals(currentUser)) {
            throw new RuntimeException("Access denied");
        }

        User existingDirector = userRepository.findById(director.getId())
                .orElseThrow(() -> new RuntimeException("Director not found"));

        if (existingDirector.getRole() != UserRole.ROLE_DIRECTOR) {
            throw new RuntimeException("User is not a director");
        }

        school.setDirector(existingDirector);
        schoolRepository.save(school);
        redirectAttributes.addFlashAttribute("success", "School director updated successfully");
        return "redirect:/schools";
    }
} 