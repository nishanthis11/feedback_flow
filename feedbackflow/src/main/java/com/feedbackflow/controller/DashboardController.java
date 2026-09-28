package com.feedbackflow.controller;

import com.feedbackflow.dto.DashboardStatsDto;
import com.feedbackflow.dto.FacultyPerformanceDto;
import com.feedbackflow.dto.FormSummaryDto;
import com.feedbackflow.security.UserPrincipal;
import com.feedbackflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** ADMIN: overall dashboard stats (total courses, active forms, total submissions, course-wise averages). */
    @GetMapping("/api/dashboard/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsDto> adminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    /** STAFF: question-wise rating averages for one of their own forms. */
    @GetMapping("/api/staff/forms/{formId}/summary")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<FormSummaryDto> formSummary(@PathVariable Long formId) {
        return ResponseEntity.ok(dashboardService.getFormSummary(formId));
    }

    /** STAFF: their own aggregated faculty performance across all taught courses. */
    @GetMapping("/api/staff/performance")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<FacultyPerformanceDto> myPerformance(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.getFacultyPerformance(principal.getId()));
    }

    /** ADMIN: look up any faculty member's performance by id. */
    @GetMapping("/api/staff/{facultyId}/performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacultyPerformanceDto> facultyPerformance(@PathVariable Long facultyId) {
        return ResponseEntity.ok(dashboardService.getFacultyPerformance(facultyId));
    }
}
