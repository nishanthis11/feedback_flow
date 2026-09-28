package com.feedbackflow.service;

import com.feedbackflow.dto.CourseDto;
import com.feedbackflow.dto.CreateCourseRequest;
import com.feedbackflow.entity.Course;
import com.feedbackflow.entity.FeedbackForm;
import com.feedbackflow.entity.Role;
import com.feedbackflow.entity.User;
import com.feedbackflow.exception.BadRequestException;
import com.feedbackflow.exception.ResourceNotFoundException;
import com.feedbackflow.repository.CourseRepository;
import com.feedbackflow.repository.FeedbackFormRepository;
import com.feedbackflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final FeedbackFormRepository formRepository;

    public CourseDto createCourse(CreateCourseRequest request) {
        if (courseRepository.findByCourseCode(request.getCourseCode()).isPresent()) {
            throw new BadRequestException("A course with code " + request.getCourseCode() + " already exists");
        }

        User faculty = userRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty user not found: " + request.getFacultyId()));

        if (faculty.getRole() != Role.STAFF) {
            throw new BadRequestException("Assigned faculty must have the STAFF role");
        }

        Course course = Course.builder()
                .courseCode(request.getCourseCode())
                .courseName(request.getCourseName())
                .description(request.getDescription())
                .semester(request.getSemester())
                .faculty(faculty)
                .build();

        course = courseRepository.save(course);
        return toDto(course, null);
    }

    public void enrollStudent(Long courseId, Long studentId) {
        Course course = getCourseOrThrow(courseId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        course.getEnrolledStudents().add(student);
        courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(c -> toDto(c, null))
                .toList();
    }

    /** Courses a STUDENT is enrolled in, each annotated with its currently-open form (if any). */
    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesForStudent(Long studentId) {
        return courseRepository.findCoursesByStudentId(studentId).stream()
                .map(course -> {
                    Optional<FeedbackForm> openForm = formRepository.findByCourse_Id(course.getId()).stream()
                            .filter(FeedbackForm::isCurrentlyOpen)
                            .max(Comparator.comparing(FeedbackForm::getOpenDate));
                    return toDto(course, openForm.map(FeedbackForm::getId).orElse(null));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesForFaculty(Long facultyId) {
        return courseRepository.findByFaculty_Id(facultyId).stream()
                .map(c -> toDto(c, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public Course getCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
    }

    private CourseDto toDto(Course c, Long activeFormId) {
        return CourseDto.builder()
                .id(c.getId())
                .courseCode(c.getCourseCode())
                .courseName(c.getCourseName())
                .description(c.getDescription())
                .semester(c.getSemester())
                .facultyId(c.getFaculty() != null ? c.getFaculty().getId() : null)
                .facultyName(c.getFaculty() != null ? c.getFaculty().getFullName() : "Unassigned")
                .enrolledCount(c.getEnrolledStudents().size())
                .activeFormId(activeFormId)
                .build();
    }
}
