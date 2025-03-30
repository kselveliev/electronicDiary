package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.School;
import com.ednevnik.dnevnik.model.User;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.model.Director;
import com.ednevnik.dnevnik.repository.SchoolRepository;
import com.ednevnik.dnevnik.repository.UserRepository;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/schools")
public class SchoolController {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public String listSchools(Model model) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userDetails.getUser();

        if (currentUser.getRole() == UserRole.ROLE_DIRECTOR) {
            // Directors can only see their own school
            Director director = (Director) currentUser;
            if (director.getSchool() != null) {
                // Get fresh data from database
                School directorSchool = schoolRepository.findById(director.getSchool().getId())
                    .orElseThrow(() -> new RuntimeException("School not found"));
                model.addAttribute("schools", List.of(directorSchool));
            } else {
                model.addAttribute("schools", List.of());
            }
        } else {
            // Admins can see all schools - get fresh data
            model.addAttribute("schools", schoolRepository.findAll());
        }
        
        // Add the current user's role to the model for UI control
        model.addAttribute("currentUserRole", currentUser.getRole());
        return "schools/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("school", new School());
        model.addAttribute("availableDirectors", userRepository.findAvailableDirectors());
        model.addAttribute("isAdmin", true);
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
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public String showEditForm(@PathVariable Long id, Model model) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found"));

        // Check if director is editing their own school
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userDetails.getUser();
        
        if (currentUser.getRole() == UserRole.ROLE_DIRECTOR) {
            Director director = (Director) currentUser;
            if (director.getSchool() == null || !director.getSchool().getId().equals(id)) {
                throw new RuntimeException("Access denied");
            }
            model.addAttribute("isAdmin", false);
        } else {
            model.addAttribute("isAdmin", true);
            model.addAttribute("availableDirectors", userRepository.findAvailableDirectors());
        }

        model.addAttribute("school", school);
        return "schools/form";
    }

    @PostMapping("/{id}/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public String updateSchool(@PathVariable Long id, @ModelAttribute School school, RedirectAttributes redirectAttributes) {
        // Get existing school
        School existingSchool = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found"));

        // Check if director is updating their own school
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userDetails.getUser();
        
        if (currentUser.getRole() == UserRole.ROLE_DIRECTOR) {
            Director director = (Director) currentUser;
            if (director.getSchool() == null || !director.getSchool().getId().equals(id)) {
                throw new RuntimeException("Access denied");
            }
        }

        // Update basic school details
        existingSchool.setName(school.getName());
        existingSchool.setAddress(school.getAddress());
        existingSchool.setCity(school.getCity());
        existingSchool.setPhoneNumber(school.getPhoneNumber());
        existingSchool.setEmail(school.getEmail());

        // Only admin can update director
        if (currentUser.getRole() == UserRole.ROLE_ADMIN) {
            existingSchool.setDirector(school.getDirector());
        }

        schoolRepository.save(existingSchool);
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
    @PreAuthorize("hasRole('ADMIN')")
    public String showDirectorForm(@PathVariable Long id, Model model) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found"));

        List<User> directors = userRepository.findAvailableDirectors();

        model.addAttribute("school", school);
        model.addAttribute("directors", directors);
        return "schools/director-form";
    }

    @PostMapping("/{id}/director")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateDirector(@PathVariable Long id,
                               @ModelAttribute User director,
                               RedirectAttributes redirectAttributes) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found"));

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