package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.ClassAssignment;
import com.ednevnik.dnevnik.model.Teacher;
import com.ednevnik.dnevnik.repository.ClassAssignmentRepository;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MyAssignmentController {

    private final ClassAssignmentRepository classAssignmentRepository;

    @GetMapping("/my-assignments")
    @PreAuthorize("hasRole('TEACHER')")
    public String showMyAssignments(Model model, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Teacher teacher = (Teacher) userDetails.getUser();
        
        // Get all assignments for the logged-in teacher
        model.addAttribute("assignments", classAssignmentRepository.findByTeacherId(teacher.getId()));
        return "teacher-assignments/my-assignments";
    }
} 