package com.feedbackflow.config;

import com.feedbackflow.entity.*;
import com.feedbackflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds a handful of demo users/courses/forms on startup so the frontend
 * has something to show immediately (H2 in-memory DB is empty otherwise).
 * Safe to delete for a real deployment against a persistent database.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final FeedbackFormRepository formRepository;
    private final QuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // already seeded
        }

        User admin = userRepository.save(User.builder()
                .fullName("Admin User").email("admin@feedbackflow.edu")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN).department("Administration").build());

        User staff1 = userRepository.save(User.builder()
                .fullName("Dr. Rahul Sharma").email("rahul.sharma@feedbackflow.edu")
                .password(passwordEncoder.encode("Staff@123"))
                .role(Role.STAFF).department("Computer Science").build());

        User staff2 = userRepository.save(User.builder()
                .fullName("Prof. Priya Nair").email("priya.nair@feedbackflow.edu")
                .password(passwordEncoder.encode("Staff@123"))
                .role(Role.STAFF).department("Computer Science").build());

        User student1 = userRepository.save(User.builder()
                .fullName("Nishanthi S").email("nishanthi.s@feedbackflow.edu")
                .password(passwordEncoder.encode("Student@123"))
                .role(Role.STUDENT).department("Computer Science").build());

        User student2 = userRepository.save(User.builder()
                .fullName("Arun K").email("arun.k@feedbackflow.edu")
                .password(passwordEncoder.encode("Student@123"))
                .role(Role.STUDENT).department("Computer Science").build());

        String semester = "2026-ODD";

        Course webDev = courseRepository.save(Course.builder()
                .courseCode("CS101").courseName("Web Development")
                .description("HTML, CSS, JavaScript and modern frontend frameworks")
                .semester(semester).faculty(staff1)
                .enrolledStudents(new java.util.HashSet<>(List.of(student1, student2)))
                .build());

        Course dbSystems = courseRepository.save(Course.builder()
                .courseCode("CS102").courseName("Database Systems")
                .description("Relational databases, SQL and normalization")
                .semester(semester).faculty(staff2)
                .enrolledStudents(new java.util.HashSet<>(List.of(student1, student2)))
                .build());

        FeedbackForm webDevForm = formRepository.save(FeedbackForm.builder()
                .title("End of Semester Feedback - Web Development")
                .course(webDev).semester(semester)
                .openDate(LocalDateTime.now().minusDays(3))
                .closeDate(LocalDateTime.now().plusDays(11))
                .active(true).build());

        questionRepository.saveAll(List.of(
                Question.builder().feedbackForm(webDevForm).text("Course Rating").type(QuestionType.RATING).orderIndex(1).build(),
                Question.builder().feedbackForm(webDevForm).text("Teaching Effectiveness").type(QuestionType.RATING).orderIndex(2).build(),
                Question.builder().feedbackForm(webDevForm).text("Course Content").type(QuestionType.RATING).orderIndex(3).build(),
                Question.builder().feedbackForm(webDevForm).text("Assignments & Assessments").type(QuestionType.RATING).orderIndex(4).build(),
                Question.builder().feedbackForm(webDevForm).text("Comments (Optional)").type(QuestionType.TEXT).orderIndex(5).build()
        ));

        FeedbackForm dbForm = formRepository.save(FeedbackForm.builder()
                .title("End of Semester Feedback - Database Systems")
                .course(dbSystems).semester(semester)
                .openDate(LocalDateTime.now().minusDays(3))
                .closeDate(LocalDateTime.now().plusDays(11))
                .active(true).build());

        questionRepository.saveAll(List.of(
                Question.builder().feedbackForm(dbForm).text("Course Rating").type(QuestionType.RATING).orderIndex(1).build(),
                Question.builder().feedbackForm(dbForm).text("Teaching Effectiveness").type(QuestionType.RATING).orderIndex(2).build(),
                Question.builder().feedbackForm(dbForm).text("Course Content").type(QuestionType.RATING).orderIndex(3).build(),
                Question.builder().feedbackForm(dbForm).text("Comments (Optional)").type(QuestionType.TEXT).orderIndex(4).build()
        ));

        System.out.println("=== FeedbackFlow demo data seeded ===");
        System.out.println("Admin login:   admin@feedbackflow.edu / Admin@123");
        System.out.println("Staff login:   rahul.sharma@feedbackflow.edu / Staff@123");
        System.out.println("Student login: nishanthi.s@feedbackflow.edu / Student@123");
    }
}
