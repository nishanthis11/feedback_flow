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
public class FacultyPerformanceDto {
    private Long facultyId;
    private String facultyName;
    private Double overallAverageRating;
    private List<CourseSummaryDto> courseBreakdown;
}
