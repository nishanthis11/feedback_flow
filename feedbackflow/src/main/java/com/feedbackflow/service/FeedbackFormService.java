package com.feedbackflow.service;

import com.feedbackflow.dto.*;
import com.feedbackflow.entity.Course;
import com.feedbackflow.entity.FeedbackForm;
import com.feedbackflow.entity.Question;
import com.feedbackflow.exception.ResourceNotFoundException;
import com.feedbackflow.repository.FeedbackFormRepository;
import com.feedbackflow.repository.FeedbackResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackFormService {

    private final FeedbackFormRepository formRepository;
    private final FeedbackResponseRepository responseRepository;
    private final CourseService courseService;

    public FeedbackFormDto createForm(CreateFeedbackFormRequest request) {
        Course course = courseService.getCourseOrThrow(request.getCourseId());

        FeedbackForm form = FeedbackForm.builder()
                .title(request.getTitle())
                .course(course)
                .semester(request.getSemester())
                .openDate(request.getOpenDate())
                .closeDate(request.getCloseDate())
                .active(true)
                .build();

        final FeedbackForm finalForm = form;

        List<Question> questions = request.getQuestions()
                .stream()
                .map(q -> Question.builder()
                        .feedbackForm(finalForm)
                        .text(q.getText())
                        .type(q.getType())
                        .orderIndex(q.getOrderIndex() != null ? q.getOrderIndex() : 0)
                        .build())
                .toList();

        form.setQuestions(questions);

        FeedbackForm savedForm = formRepository.save(form);

        return toDto(savedForm, null);
    }

    public FeedbackFormDto closeForm(Long formId) {
        FeedbackForm form = getFormOrThrow(formId);
        form.setActive(false);

        FeedbackForm savedForm = formRepository.save(form);

        return toDto(savedForm, null);
    }

    public FeedbackFormDto reopenForm(Long formId) {
        FeedbackForm form = getFormOrThrow(formId);
        form.setActive(true);

        FeedbackForm savedForm = formRepository.save(form);

        return toDto(savedForm, null);
    }

    public FeedbackFormDto updateDeadline(
            Long formId,
            UpdateDeadlineRequest request) {

        FeedbackForm form = getFormOrThrow(formId);
        form.setCloseDate(request.getCloseDate());

        FeedbackForm savedForm = formRepository.save(form);

        return toDto(savedForm, null);
    }

    @Transactional(readOnly = true)
    public FeedbackFormDto getFormForStudent(
            Long formId,
            Long studentId) {

        FeedbackForm form = getFormOrThrow(formId);

        boolean already =
                responseRepository.existsByFeedbackForm_IdAndStudent_Id(
                        formId,
                        studentId
                );

        return toDto(form, already);
    }

    @Transactional(readOnly = true)
    public List<FeedbackFormDto> getFormsForCourse(Long courseId) {
        return formRepository.findByCourse_Id(courseId)
                .stream()
                .map(f -> toDto(f, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public FeedbackForm getFormOrThrow(Long formId) {
        return formRepository.findById(formId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feedback form not found: " + formId
                        )
                );
    }

    private FeedbackFormDto toDto(
            FeedbackForm f,
            Boolean alreadySubmitted) {

        List<QuestionDto> questionDtos = f.getQuestions()
                .stream()
                .map(q -> QuestionDto.builder()
                        .id(q.getId())
                        .text(q.getText())
                        .type(q.getType())
                        .orderIndex(q.getOrderIndex())
                        .build())
                .toList();

        return FeedbackFormDto.builder()
                .id(f.getId())
                .title(f.getTitle())
                .courseId(f.getCourse().getId())
                .courseName(f.getCourse().getCourseName())
                .courseCode(f.getCourse().getCourseCode())
                .semester(f.getSemester())
                .openDate(f.getOpenDate())
                .closeDate(f.getCloseDate())
                .active(f.isActive())
                .currentlyOpen(f.isCurrentlyOpen())
                .alreadySubmittedByCurrentUser(
                        alreadySubmitted != null && alreadySubmitted
                )
                .questions(questionDtos)
                .build();
    }
}
