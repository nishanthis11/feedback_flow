package com.feedbackflow.controller;

import com.feedbackflow.dto.SubmitFeedbackRequest;
import com.feedbackflow.entity.FeedbackResponse;
import com.feedbackflow.security.UserPrincipal;
import com.feedbackflow.service.FeedbackResponseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/responses")
@RequiredArgsConstructor
public class FeedbackResponseController {

    private final FeedbackResponseService feedbackResponseService;

    /**
     * STUDENT submits feedback for a course's active form.
     * Rule 1 (duplicate) -> 409, Rule 2 (closed/not-yet-open) -> 403 -
     * both surfaced automatically via GlobalExceptionHandler.
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> submit(@Valid @RequestBody SubmitFeedbackRequest request,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        FeedbackResponse saved = feedbackResponseService.submitFeedback(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "message", "Thank you! Your feedback has been submitted successfully.",
                "submittedAt", saved.getSubmittedAt()
        ));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> myResponses(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(feedbackResponseService.getResponsesForStudent(principal.getId()).stream()
                .map(r -> Map.of(
                        "id", r.getId(),
                        "formId", r.getFeedbackForm().getId(),
                        "courseName", r.getFeedbackForm().getCourse().getCourseName(),
                        "submittedAt", r.getSubmittedAt()
                )).toList());
    }
}
