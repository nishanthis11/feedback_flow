package com.feedbackflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCourseRequest {
    @NotBlank
    private String courseCode;

    @NotBlank
    private String courseName;

    private String description;

    @NotBlank
    private String semester;

    @NotNull
    private Long facultyId;
}
