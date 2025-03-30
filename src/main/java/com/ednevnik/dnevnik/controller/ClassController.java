//package com.ednevnik.dnevnik.controller;
//
//import com.ednevnik.dnevnik.model.Class;
//import com.ednevnik.dnevnik.model.School;
//import com.ednevnik.dnevnik.model.Teacher;
//import com.ednevnik.dnevnik.service.ClassService;
//import com.ednevnik.dnevnik.service.SchoolService;
//import com.ednevnik.dnevnik.service.TeacherService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/classes")
//@RequiredArgsConstructor
//public class ClassController {
//
//    private final ClassService classService;
//    private final SchoolService schoolService;
//    private final TeacherService teacherService;
//
//    @PostMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
//    public ResponseEntity<Class> createClass(@Valid @RequestBody ClassRequest request) {
//        School school = schoolService.findById(request.schoolId());
//        Teacher classTeacher = teacherService.findById(request.classTeacherId());
//
//        Class schoolClass = new Class();
//        schoolClass.setName(request.name());
//        schoolClass.setGrade(request.grade());
//        schoolClass.setSchool(school);
//        schoolClass.setClassTeacher(classTeacher);
//
//        return ResponseEntity.ok(classService.save(schoolClass));
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Class>> getAllClasses() {
//        return ResponseEntity.ok(classService.findAll());
//    }
//
//    @GetMapping("/school/{schoolId}")
//    public ResponseEntity<List<Class>> getClassesBySchool(@PathVariable Long schoolId) {
//        return ResponseEntity.ok(classService.findBySchoolId(schoolId));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Class> getClass(@PathVariable Long id) {
//        return ResponseEntity.ok(classService.findById(id));
//    }
//
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
//    public ResponseEntity<Class> updateClass(@PathVariable Long id, @Valid @RequestBody ClassRequest request) {
//        Class schoolClass = classService.findById(id);
//        School school = schoolService.findById(request.schoolId());
//        Teacher classTeacher = teacherService.findById(request.classTeacherId());
//
//        schoolClass.setName(request.name());
//        schoolClass.setGrade(request.grade());
//        schoolClass.setSchool(school);
//        schoolClass.setClassTeacher(classTeacher);
//
//        return ResponseEntity.ok(classService.save(schoolClass));
//    }
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
//    public ResponseEntity<?> deleteClass(@PathVariable Long id) {
//        classService.deleteById(id);
//        return ResponseEntity.ok().build();
//    }
//}
//
//record ClassRequest(String name, Integer grade, Long schoolId, Long classTeacherId) {}