package com.feedbackflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateFeedbackFormRequest {
    @NotBlank
    private String title;

    @NotNull
    private Long courseId;

    @NotBlank
    private String semester;

    @NotNull
    private LocalDateTime openDate;

    @NotNull
    @Future(message = "closeDate must be in the future")
    private LocalDateTime closeDate;

    @NotEmpty(message = "A feedback form needs at least one question")
    @Valid
    private List<QuestionDto> questions;
}
