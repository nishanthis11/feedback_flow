package com.feedbackflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDto {
    @NotNull
    private Long questionId;

    // Required for RATING questions
    @Min(1) @Max(5)
    private Integer ratingValue;

    // Required for TEXT questions
    private String textValue;
}
