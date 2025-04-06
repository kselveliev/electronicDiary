package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.Parent;
import com.ednevnik.dnevnik.model.Student;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import com.ednevnik.dnevnik.service.ParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class ChildrenController {

    private final ParentService parentService;

    @GetMapping("/children")
    public String showChildren(Model model, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Parent parent = (Parent) userDetails.getUser();

        Set<Student> children = parentService.getChildren(parent.getId());
        if (children.isEmpty()) {
            model.addAttribute("message", "No children found in your account.");
        } else {
            model.addAttribute("children", children);
        }

        return "children/list";
    }

    @GetMapping("/children/{childId}/grades")
    public String showChildGrades(@PathVariable Long childId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Parent parent = (Parent) userDetails.getUser();

        // This will throw an exception if the child is not found or not associated with the parent
        parentService.getChildren(parent.getId())
                .stream()
                .filter(child -> child.getId().equals(childId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Child not found or not associated with parent"));

        return "redirect:/grades?studentId=" + childId;
    }
} 