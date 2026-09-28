package com.feedbackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormSummaryDto {
    private Long formId;
    private String formTitle;
    private String courseCode;
    private String courseName;
    private long totalSubmissions;
    private Double overallAverageRating;
    private List<QuestionAverageDto> questionAverages;
}
