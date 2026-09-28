package com.feedbackflow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateDeadlineRequest {
    @NotNull
    private LocalDateTime closeDate;
}
