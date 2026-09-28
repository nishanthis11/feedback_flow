package com.feedbackflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmitFeedbackRequest {
    @NotNull
    private Long formId;

    @NotEmpty
    @Valid
    private List<AnswerDto> answers;
}
