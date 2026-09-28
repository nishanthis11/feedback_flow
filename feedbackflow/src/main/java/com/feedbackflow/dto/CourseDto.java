package com.feedbackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private String semester;
    private Long facultyId;
    private String facultyName;
    private Integer enrolledCount;
    // present only when returned in a "my courses" context for a student
    private Long activeFormId;
}
