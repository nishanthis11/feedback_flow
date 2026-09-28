package com.feedbackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of the JPQL "constructor expression" query that computes
 * the average rating for a single question on a form.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAverageDto {
    private Long questionId;
    private String questionText;
    private Double averageRating;
    private Long responseCount;
}
