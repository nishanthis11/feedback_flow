package com.feedbackflow.controller;

import com.feedbackflow.dto.CreateFeedbackFormRequest;
import com.feedbackflow.dto.FeedbackFormDto;
import com.feedbackflow.dto.UpdateDeadlineRequest;
import com.feedbackflow.security.UserPrincipal;
import com.feedbackflow.service.FeedbackFormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FeedbackFormController {

    private final FeedbackFormService feedbackFormService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeedbackFormDto> createForm(@Valid @RequestBody CreateFeedbackFormRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackFormService.createForm(request));
    }

    @PutMapping("/{formId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeedbackFormDto> closeForm(@PathVariable Long formId) {
        return ResponseEntity.ok(feedbackFormService.closeForm(formId));
    }

    @PutMapping("/{formId}/reopen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeedbackFormDto> reopenForm(@PathVariable Long formId) {
        return ResponseEntity.ok(feedbackFormService.reopenForm(formId));
    }

    @PutMapping("/{formId}/deadline")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeedbackFormDto> updateDeadline(@PathVariable Long formId,
                                                           @Valid @RequestBody UpdateDeadlineRequest request) {
        return ResponseEntity.ok(feedbackFormService.updateDeadline(formId, request));
    }

    @GetMapping("/{formId}")
    public ResponseEntity<FeedbackFormDto> getForm(@PathVariable Long formId,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(feedbackFormService.getFormForStudent(formId, principal.getId()));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<FeedbackFormDto>> getFormsForCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(feedbackFormService.getFormsForCourse(courseId));
    }
}
