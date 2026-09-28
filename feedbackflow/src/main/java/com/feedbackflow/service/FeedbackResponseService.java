package com.feedbackflow.service;

import com.feedbackflow.dto.AnswerDto;
import com.feedbackflow.dto.SubmitFeedbackRequest;
import com.feedbackflow.entity.*;
import com.feedbackflow.exception.BadRequestException;
import com.feedbackflow.exception.DuplicateSubmissionException;
import com.feedbackflow.exception.FormClosedException;
import com.feedbackflow.exception.ResourceNotFoundException;
import com.feedbackflow.repository.FeedbackResponseRepository;
import com.feedbackflow.repository.QuestionRepository;
import com.feedbackflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Owns the two strict business rules from the spec (Section 3):
 *
 *   Rule 1 - A student can submit only ONE response per course per semester
 *            (i.e. per FeedbackForm). Violation -> DuplicateSubmissionException -> HTTP 409.
 *
 *   Rule 2 - Responses cannot be submitted after the form's closing date,
 *            before its opening date, or while the form is marked inactive.
 *            Violation -> FormClosedException -> HTTP 403.
 *
 * Both checks are performed here, in the service layer, BEFORE anything is
 * persisted - never relying on the UI to enforce them.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackResponseService {

    private final FeedbackResponseRepository responseRepository;
    private final FeedbackFormService feedbackFormService;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public FeedbackResponse submitFeedback(SubmitFeedbackRequest request, Long studentId) {

        FeedbackForm form = feedbackFormService.getFormOrThrow(request.getFormId());

        // ---- Rule 2: form must be open right now ----
        LocalDateTime now = LocalDateTime.now();
        if (!form.isActive()) {
            throw new FormClosedException("This feedback form has been closed by the administrator.");
        }
        if (now.isBefore(form.getOpenDate())) {
            throw new FormClosedException("This feedback form is not open yet. It opens on " + form.getOpenDate());
        }
        if (now.isAfter(form.getCloseDate())) {
            throw new FormClosedException("The deadline for this feedback form (" + form.getCloseDate() + ") has passed.");
        }

        // ---- Rule 1: one submission per student per form ----
        if (responseRepository.existsByFeedbackForm_IdAndStudent_Id(form.getId(), studentId)) {
            throw new DuplicateSubmissionException(
                    "You have already submitted feedback for this course this semester.");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        Map<Long, Question> questionsById = form.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        FeedbackResponse response = FeedbackResponse.builder()
                .feedbackForm(form)
                .student(student)
                .submittedAt(now)
                .build();

        List<QuestionAnswer> answers = request.getAnswers().stream().map(a -> buildAnswer(a, questionsById, response)).toList();
        response.setAnswers(answers);

        // Defensive re-check right before the write to close the race-condition window
        // between the existsBy... check above and this save (belt-and-braces alongside
        // the DB unique constraint on (form_id, student_id)).
        try {
            return responseRepository.save(response);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new DuplicateSubmissionException(
                    "You have already submitted feedback for this course this semester.");
        }
    }

    private QuestionAnswer buildAnswer(AnswerDto dto, Map<Long, Question> questionsById, FeedbackResponse response) {
        Question question = questionsById.get(dto.getQuestionId());
        if (question == null) {
            throw new BadRequestException("Question " + dto.getQuestionId() + " does not belong to this form");
        }

        if (question.getType() == QuestionType.RATING) {
            if (dto.getRatingValue() == null) {
                throw new BadRequestException("A rating (1-5) is required for question: " + question.getText());
            }
        }

        return QuestionAnswer.builder()
                .feedbackResponse(response)
                .question(question)
                .ratingValue(dto.getRatingValue())
                .textValue(dto.getTextValue())
                .build();
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getResponsesForStudent(Long studentId) {
        return responseRepository.findByStudent_Id(studentId);
    }
}
