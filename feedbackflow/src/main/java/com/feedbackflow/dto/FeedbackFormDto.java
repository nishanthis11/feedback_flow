package com.feedbackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackFormDto {
    private Long id;
    private String title;
    private Long courseId;
    private String courseName;
    private String courseCode;
    private String semester;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;
    private boolean active;
    private boolean currentlyOpen;
    private boolean alreadySubmittedByCurrentUser; // populated for student views
    private List<QuestionDto> questions;
}
