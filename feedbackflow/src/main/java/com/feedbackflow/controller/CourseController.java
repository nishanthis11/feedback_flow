package com.feedbackflow.controller;

import com.feedbackflow.dto.CourseDto;
import com.feedbackflow.dto.CreateCourseRequest;
import com.feedbackflow.security.UserPrincipal;
import com.feedbackflow.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request));
    }

    @PostMapping("/{courseId}/enroll/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> enrollStudent(@PathVariable Long courseId, @PathVariable Long studentId) {
        courseService.enrollStudent(courseId, studentId);
        return ResponseEntity.noContent().build();
    }

    /** Returns courses scoped to the caller's role: STUDENT -> enrolled courses, STAFF -> taught courses, ADMIN -> all. */
    @GetMapping("/my")
    public ResponseEntity<List<CourseDto>> myCourses(@AuthenticationPrincipal UserPrincipal principal) {
        List<CourseDto> courses = switch (principal.getRole()) {
            case "STUDENT" -> courseService.getCoursesForStudent(principal.getId());
            case "STAFF" -> courseService.getCoursesForFaculty(principal.getId());
            default -> courseService.getAllCourses();
        };
        return ResponseEntity.ok(courses);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseDto>> allCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }
}
