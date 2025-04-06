package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.Subject;
import com.ednevnik.dnevnik.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/subjects")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectRepository subjectRepository;

    @GetMapping
    public String listSubjects(Model model) {
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("newSubject", new Subject());
        return "subjects/list";
    }

    @PostMapping
    public String createSubject(@ModelAttribute Subject subject, RedirectAttributes redirectAttributes) {
        try {
            subject.setCreatedTimestamp(System.currentTimeMillis());
            subjectRepository.save(subject);
            redirectAttributes.addFlashAttribute("success", "Subject created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create subject: " + e.getMessage());
        }
        return "redirect:/subjects";
    }

    @PostMapping("/{id}/delete")
    public String deleteSubject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            subjectRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Subject deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete subject: " + e.getMessage());
        }
        return "redirect:/subjects";
    }

    @PostMapping("/{id}/update")
    public String updateSubject(@PathVariable Long id, @ModelAttribute Subject subject, RedirectAttributes redirectAttributes) {
        try {
            Subject existingSubject = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid subject ID"));
            existingSubject.setName(subject.getName());
            subjectRepository.save(existingSubject);
            redirectAttributes.addFlashAttribute("success", "Subject updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update subject: " + e.getMessage());
        }
        return "redirect:/subjects";
    }
} 