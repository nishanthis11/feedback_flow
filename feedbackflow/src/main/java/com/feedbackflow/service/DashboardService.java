package com.feedbackflow.service;

import com.feedbackflow.dto.*;
import com.feedbackflow.entity.Course;
import com.feedbackflow.entity.FeedbackForm;
import com.feedbackflow.entity.FeedbackResponse;
import com.feedbackflow.entity.User;
import com.feedbackflow.exception.ResourceNotFoundException;
import com.feedbackflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final CourseRepository courseRepository;
    private final FeedbackFormRepository formRepository;
    private final FeedbackResponseRepository responseRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final UserRepository userRepository;

    /** ADMIN dashboard: total courses, active forms, total submissions, course-wise averages, recent feedback. */
    public DashboardStatsDto getAdminDashboard() {
        long totalCourses = courseRepository.count();
        long activeForms = formRepository.countByActiveTrue();
        long totalSubmissions = responseRepository.count();
        Double systemAvg = questionAnswerRepository.findSystemWideAverageRating();

        List<CourseSummaryDto> courseWise = courseRepository.findAll().stream()
                .map(this::buildCourseSummary)
                .toList();

        List<RecentFeedbackDto> recent = responseRepository.findAll().stream()
                .sorted(Comparator.comparing(FeedbackResponse::getSubmittedAt).reversed())
                .limit(10)
                .map(r -> RecentFeedbackDto.builder()
                        .studentName(r.getStudent().getFullName())
                        .courseName(r.getFeedbackForm().getCourse().getCourseName())
                        .averageRatingGiven(r.getAnswers().stream()
                                .filter(a -> a.getRatingValue() != null)
                                .mapToInt(a -> a.getRatingValue())
                                .average().stream().boxed().findFirst().orElse(null))
                        .submittedAt(r.getSubmittedAt())
                        .build())
                .toList();

        return DashboardStatsDto.builder()
                .totalCourses(totalCourses)
                .activeFormsCount(activeForms)
                .totalSubmissions(totalSubmissions)
                .systemWideAverageRating(systemAvg)
                .courseWiseAverages(courseWise)
                .recentFeedback(recent)
                .build();
    }

    /** STAFF: question-wise average rating summary for one of their own forms. */
    public FormSummaryDto getFormSummary(Long formId) {
        FeedbackForm form = formRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found: " + formId));

        List<QuestionAverageDto> questionAverages = questionAnswerRepository.findQuestionAveragesByFormId(formId);
        Double overall = questionAnswerRepository.findOverallAverageByFormId(formId);
        long total = responseRepository.countByFeedbackForm_Id(formId);

        return FormSummaryDto.builder()
                .formId(form.getId())
                .formTitle(form.getTitle())
                .courseCode(form.getCourse().getCourseCode())
                .courseName(form.getCourse().getCourseName())
                .totalSubmissions(total)
                .overallAverageRating(overall)
                .questionAverages(questionAverages)
                .build();
    }

    /** STAFF: aggregated performance across every course a faculty member teaches. */
    public FacultyPerformanceDto getFacultyPerformance(Long facultyId) {
        User faculty = userRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + facultyId));

        List<Course> courses = courseRepository.findByFaculty_Id(facultyId);
        List<CourseSummaryDto> breakdown = courses.stream().map(this::buildCourseSummary).toList();
        Double overall = questionAnswerRepository.findAverageRatingByFacultyId(facultyId);

        return FacultyPerformanceDto.builder()
                .facultyId(faculty.getId())
                .facultyName(faculty.getFullName())
                .overallAverageRating(overall)
                .courseBreakdown(breakdown)
                .build();
    }

    private CourseSummaryDto buildCourseSummary(Course course) {
        List<FeedbackForm> forms = formRepository.findByCourse_Id(course.getId());

        double weightedSum = 0;
        long totalSubmissions = 0;
        for (FeedbackForm f : forms) {
            Double avg = questionAnswerRepository.findOverallAverageByFormId(f.getId());
            long count = responseRepository.countByFeedbackForm_Id(f.getId());
            if (avg != null) {
                weightedSum += avg * count;
                totalSubmissions += count;
            }
        }
        Double courseAvg = totalSubmissions > 0 ? weightedSum / totalSubmissions : null;

        return CourseSummaryDto.builder()
                .courseId(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .averageRating(courseAvg)
                .totalSubmissions(totalSubmissions)
                .build();
    }
}
