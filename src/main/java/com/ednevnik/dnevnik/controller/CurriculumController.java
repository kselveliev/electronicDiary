package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.*;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/curriculum")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
public class CurriculumController {

    private final CurriculumRepository curriculumRepository;
    private final CurriculumSubjectRepository curriculumSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    private final ClassRepository classRepository;

    @GetMapping
    public String listCurriculums(Model model, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            model.addAttribute("curriculums", curriculumRepository.findAll());
        } else {
            // For directors, show only their school's curriculums
            Director director = (Director) user;
            model.addAttribute("curriculums", curriculumRepository.findBySchoolId(director.getSchool().getId()));
        }
        
        return "curriculum/list";
    }

    @GetMapping("/new")
    public String showNewCurriculumForm(Model model, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            model.addAttribute("schools", schoolRepository.findAll());
        } else {
            Director director = (Director) user;
            model.addAttribute("schools", schoolRepository.findById(director.getSchool().getId()).stream().toList());
        }
        
        return "curriculum/form";
    }

    @PostMapping
    public String createCurriculum(@RequestParam String name,
                                 @RequestParam String startDate,
                                 @RequestParam String endDate,
                                 @RequestParam Long schoolId,
                                 RedirectAttributes redirectAttributes) {
        try {
            Curriculum curriculum = new Curriculum();
            curriculum.setName(name);
            curriculum.setStartDate(LocalDate.parse(startDate));
            curriculum.setEndDate(LocalDate.parse(endDate));
            curriculum.setSchool(schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new RuntimeException("School not found")));
            curriculum.setActive(true);
            
            curriculumRepository.save(curriculum);
            redirectAttributes.addFlashAttribute("success", "Curriculum created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating curriculum: " + e.getMessage());
        }
        
        return "redirect:/curriculum";
    }

    @GetMapping("/{id}")
    public String showCurriculum(@PathVariable Long id, Model model) {
        Curriculum curriculum = curriculumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curriculum not found"));
        
        // Get all classes for the school
        List<com.ednevnik.dnevnik.model.Class> allClasses = classRepository.findBySchoolId(curriculum.getSchool().getId());
        
        // Group classes by grade
        Map<Integer, List<com.ednevnik.dnevnik.model.Class>> classesByGrade = allClasses.stream()
                .collect(Collectors.groupingBy(com.ednevnik.dnevnik.model.Class::getGrade));
        
        model.addAttribute("curriculum", curriculum);
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("teachers", teacherRepository.findBySchoolId(curriculum.getSchool().getId()));
        model.addAttribute("classesByGrade", classesByGrade);
        model.addAttribute("curriculumSubjects", curriculumSubjectRepository.findByCurriculumId(id));
        
        return "curriculum/details";
    }

    /**
     * Helper method to filter curriculum subjects by class, day, and lesson number
     */
    private CurriculumSubject findSubjectForSlot(List<CurriculumSubject> subjects, Long classId, DayOfWeek day, Integer lessonNumber) {
        return subjects.stream()
                .filter(cs -> cs.getSchoolClass().getId().equals(classId) 
                        && cs.getDayOfWeek() == day 
                        && cs.getLessonNumber().equals(lessonNumber))
                .findFirst()
                .orElse(null);
    }

    @PostMapping("/{id}/subjects")
    public String addSubjectToCurriculum(@PathVariable Long id,
                                       @RequestParam Long subjectId,
                                       @RequestParam Long teacherId,
                                       @RequestParam Long classId,
                                       @RequestParam DayOfWeek dayOfWeek,
                                       @RequestParam Integer lessonNumber,
                                       RedirectAttributes redirectAttributes) {
        try {
            // Check if the time slot is already taken
            boolean slotTaken = curriculumSubjectRepository.findByCurriculumId(id).stream()
                .anyMatch(cs -> cs.getSchoolClass().getId().equals(classId) 
                    && cs.getDayOfWeek() == dayOfWeek 
                    && cs.getLessonNumber().equals(lessonNumber));

            if (slotTaken) {
                redirectAttributes.addFlashAttribute("error", "This time slot is already taken for the selected class");
                return "redirect:/curriculum/" + id;
            }

            Curriculum curriculum = curriculumRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Curriculum not found"));
            
            CurriculumSubject curriculumSubject = new CurriculumSubject();
            curriculumSubject.setCurriculum(curriculum);
            curriculumSubject.setSubject(subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new RuntimeException("Subject not found")));
            curriculumSubject.setTeacher(teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new RuntimeException("Teacher not found")));
            curriculumSubject.setSchoolClass(classRepository.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Class not found")));
            curriculumSubject.setDayOfWeek(dayOfWeek);
            curriculumSubject.setLessonNumber(lessonNumber);
            
            curriculumSubjectRepository.save(curriculumSubject);
            redirectAttributes.addFlashAttribute("success", "Subject added to schedule successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding subject to schedule: " + e.getMessage());
        }
        
        return "redirect:/curriculum/" + id;
    }

    @PostMapping("/{curriculumId}/subjects/{subjectId}/remove")
    public String removeSubjectFromCurriculum(@PathVariable Long curriculumId,
                                            @PathVariable Long subjectId,
                                            RedirectAttributes redirectAttributes) {
        try {
            CurriculumSubject curriculumSubject = curriculumSubjectRepository.findById(subjectId)
                    .orElseThrow(() -> new RuntimeException("Subject not found in curriculum"));
            
            curriculumSubjectRepository.delete(curriculumSubject);
            redirectAttributes.addFlashAttribute("success", "Subject removed from schedule successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error removing subject from schedule: " + e.getMessage());
        }
        
        return "redirect:/curriculum/" + curriculumId;
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleCurriculumActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Curriculum curriculum = curriculumRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Curriculum not found"));
            
            curriculum.setActive(!curriculum.isActive());
            curriculumRepository.save(curriculum);
            
            String status = curriculum.isActive() ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("success", "Curriculum " + status + " successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error toggling curriculum status: " + e.getMessage());
        }
        
        return "redirect:/curriculum";
    }
} 