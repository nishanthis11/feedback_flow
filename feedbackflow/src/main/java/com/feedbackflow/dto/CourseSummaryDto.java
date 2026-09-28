package com.feedbackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSummaryDto {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Double averageRating;
    private long totalSubmissions;
}
