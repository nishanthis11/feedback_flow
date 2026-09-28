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
public class DashboardStatsDto {
    private long totalCourses;
    private long activeFormsCount;
    private long totalSubmissions;
    private Double systemWideAverageRating;
    private List<CourseSummaryDto> courseWiseAverages;   // powers the bar chart
    private List<RecentFeedbackDto> recentFeedback;
}
