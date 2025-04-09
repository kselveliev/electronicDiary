package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.Parent;
import com.ednevnik.dnevnik.service.ParentService;
import com.ednevnik.dnevnik.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/parent-assignments")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
public class ParentAssignmentController {

    private final ParentService parentService;
    private final UserService userService;

    @GetMapping
    public String showParentAssignments(Model model) {
        model.addAttribute("parents", userService.findAllByRole("ROLE_PARENT"));
        model.addAttribute("students", userService.findAllByRole("ROLE_STUDENT"));
        return "parent-assignments/list";
    }

    @GetMapping("/{parentId}")
    public String showParentDetails(@PathVariable Long parentId, Model model) {
        Parent parent = (Parent) userService.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid parent ID"));
        
        model.addAttribute("parent", parent);
        model.addAttribute("assignedStudents", parentService.findChildrenByParentId(parentId));
        model.addAttribute("availableStudents", userService.findAllByRole("ROLE_STUDENT"));
        return "parent-assignments/details";
    }

    @PostMapping("/{parentId}/assign")
    public String assignStudent(
            @PathVariable Long parentId,
            @RequestParam Long studentId,
            RedirectAttributes redirectAttributes) {
        try {
            parentService.assignChildToParent(studentId, parentId);
            redirectAttributes.addFlashAttribute("success", "Student successfully assigned to parent");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to assign student: " + e.getMessage());
        }
        return "redirect:/parent-assignments/" + parentId;
    }

    @PostMapping("/{parentId}/remove")
    public String removeStudent(
            @PathVariable Long parentId,
            @RequestParam Long studentId,
            RedirectAttributes redirectAttributes) {
        try {
            parentService.removeChildFromParent(studentId, parentId);
            redirectAttributes.addFlashAttribute("success", "Student successfully removed from parent");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove student: " + e.getMessage());
        }
        return "redirect:/parent-assignments/" + parentId;
    }
} 