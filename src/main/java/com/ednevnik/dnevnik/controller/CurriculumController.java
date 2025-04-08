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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/curriculum")
public class CurriculumController {

    private final CurriculumRepository curriculumRepository;
    private final CurriculumSubjectRepository curriculumSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    private final ClassRepository classRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String listCurriculums(Model model, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            model.addAttribute("curriculums", curriculumRepository.findAll());
        } else if (user.getRole() == UserRole.ROLE_DIRECTOR) {
            // For directors, show only their school's curriculums
            Director director = (Director) user;
            model.addAttribute("curriculums", curriculumRepository.findBySchoolId(director.getSchool().getId()));
        } else if (user.getRole() == UserRole.ROLE_TEACHER) {
            // For teachers, show curriculums for their school
            Teacher teacher = (Teacher) user;
            model.addAttribute("curriculums", curriculumRepository.findBySchoolId(teacher.getSchool().getId()));
        } else if (user.getRole() == UserRole.ROLE_STUDENT) {
            // For students, show curriculums for their school
            Student student = (Student) user;
            model.addAttribute("curriculums", curriculumRepository.findBySchoolId(student.getStudentClass().getSchool().getId()));
        } else if (user.getRole() == UserRole.ROLE_PARENT) {
            // For parents, show curriculums for their children's school
            Parent parent = (Parent) user;
            if (!parent.getChildren().isEmpty()) {
                Student child = parent.getChildren().iterator().next();
                model.addAttribute("curriculums", curriculumRepository.findBySchoolId(child.getStudentClass().getSchool().getId()));
            }
        }
        
        model.addAttribute("canEdit", user.getRole() == UserRole.ROLE_ADMIN || user.getRole() == UserRole.ROLE_DIRECTOR);
        return "curriculum/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public String createCurriculum(@RequestParam String name,
                                 @RequestParam Integer grade,
                                 @RequestParam String startDate,
                                 @RequestParam String endDate,
                                 @RequestParam Long schoolId,
                                 RedirectAttributes redirectAttributes) {
        try {
            Curriculum curriculum = new Curriculum();
            curriculum.setName(name);
            curriculum.setGrade(grade);
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
    @PreAuthorize("isAuthenticated()")
    public String showCurriculum(@PathVariable Long id, Model model, Authentication authentication) {
        Curriculum curriculum = curriculumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curriculum not found"));
        
        // Check if the user has access to this curriculum
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        
        boolean hasAccess = false;
        
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            hasAccess = true;
        } else if (user.getRole() == UserRole.ROLE_DIRECTOR) {
            Director director = (Director) user;
            hasAccess = director.getSchool().getId().equals(curriculum.getSchool().getId());
        } else if (user.getRole() == UserRole.ROLE_TEACHER) {
            Teacher teacher = (Teacher) user;
            hasAccess = teacher.getSchool().getId().equals(curriculum.getSchool().getId());
        } else if (user.getRole() == UserRole.ROLE_STUDENT) {
            Student student = (Student) user;
            hasAccess = student.getStudentClass().getSchool().getId().equals(curriculum.getSchool().getId());
        } else if (user.getRole() == UserRole.ROLE_PARENT) {
            Parent parent = (Parent) user;
            if (!parent.getChildren().isEmpty()) {
                Student child = parent.getChildren().iterator().next();
                hasAccess = child.getStudentClass().getSchool().getId().equals(curriculum.getSchool().getId());
            }
        }
        
        if (!hasAccess) {
            throw new RuntimeException("You don't have access to this curriculum");
        }
        
        // Get classes for this grade in the school
        List<com.ednevnik.dnevnik.model.Class> classes = classRepository.findBySchoolIdAndGrade(
            curriculum.getSchool().getId(), 
            curriculum.getGrade()
        );
        
        model.addAttribute("curriculum", curriculum);
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("teachers", teacherRepository.findBySchoolId(curriculum.getSchool().getId()));
        model.addAttribute("classes", classes);
        model.addAttribute("curriculumSubjects", curriculumSubjectRepository.findByCurriculumId(id));
        model.addAttribute("canEdit", user.getRole() == UserRole.ROLE_ADMIN || user.getRole() == UserRole.ROLE_DIRECTOR);
        
        return "curriculum/details";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public String editCurriculum(@PathVariable Long id, Model model, Authentication authentication) {
        Curriculum curriculum = curriculumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curriculum not found"));
        
        // Check if the user has access to this curriculum
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        
        boolean hasAccess = false;
        
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            hasAccess = true;
        } else if (user.getRole() == UserRole.ROLE_DIRECTOR) {
            Director director = (Director) user;
            hasAccess = director.getSchool().getId().equals(curriculum.getSchool().getId());
        }
        
        if (!hasAccess) {
            throw new RuntimeException("You don't have access to edit this curriculum");
        }
        
        model.addAttribute("curriculum", curriculum);
        model.addAttribute("schools", schoolRepository.findAll());
        
        return "curriculum/edit";
    }
    
    @PostMapping("/{id}/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public String updateCurriculum(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam Integer grade,
                                 @RequestParam String startDate,
                                 @RequestParam String endDate,
                                 @RequestParam Long schoolId,
                                 @RequestParam(required = false) Boolean active,
                                 RedirectAttributes redirectAttributes,
                                 Authentication authentication) {
        try {
            Curriculum curriculum = curriculumRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Curriculum not found"));
            
            // Check if the user has access to this curriculum
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            boolean hasAccess = false;
            
            if (user.getRole() == UserRole.ROLE_ADMIN) {
                hasAccess = true;
            } else if (user.getRole() == UserRole.ROLE_DIRECTOR) {
                Director director = (Director) user;
                hasAccess = director.getSchool().getId().equals(curriculum.getSchool().getId());
            }
            
            if (!hasAccess) {
                throw new RuntimeException("You don't have access to edit this curriculum");
            }
            
            // Parse dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate parsedStartDate = LocalDate.parse(startDate, formatter);
            LocalDate parsedEndDate = LocalDate.parse(endDate, formatter);
            
            // Validate dates
            if (parsedEndDate.isBefore(parsedStartDate)) {
                redirectAttributes.addFlashAttribute("error", "End date cannot be before start date");
                return "redirect:/curriculum/" + id + "/edit";
            }
            
            // Update curriculum
            curriculum.setName(name);
            curriculum.setGrade(grade);
            curriculum.setStartDate(parsedStartDate);
            curriculum.setEndDate(parsedEndDate);
            
            // Only admin can change the school
            if (user.getRole() == UserRole.ROLE_ADMIN) {
                School school = schoolRepository.findById(schoolId)
                        .orElseThrow(() -> new RuntimeException("School not found"));
                curriculum.setSchool(school);
            }
            
            // Update active status if provided
            if (active != null) {
                curriculum.setActive(active);
            }
            
            curriculumRepository.save(curriculum);
            
            redirectAttributes.addFlashAttribute("success", "Curriculum updated successfully");
            return "redirect:/curriculum";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating curriculum: " + e.getMessage());
            return "redirect:/curriculum/" + id + "/edit";
        }
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
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
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
            
            // Verify the class is from the correct grade
            com.ednevnik.dnevnik.model.Class schoolClass = classRepository.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Class not found"));
            
            if (!schoolClass.getGrade().equals(curriculum.getGrade())) {
                throw new RuntimeException("Selected class is not from grade " + curriculum.getGrade());
            }
            
            CurriculumSubject curriculumSubject = new CurriculumSubject();
            curriculumSubject.setCurriculum(curriculum);
            curriculumSubject.setSubject(subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new RuntimeException("Subject not found")));
            curriculumSubject.setTeacher(teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new RuntimeException("Teacher not found")));
            curriculumSubject.setSchoolClass(schoolClass);
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
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
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