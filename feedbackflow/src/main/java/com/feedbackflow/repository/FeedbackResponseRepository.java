package com.feedbackflow.repository;

import com.feedbackflow.entity.FeedbackResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackResponseRepository extends JpaRepository<FeedbackResponse, Long> {

    // Used to enforce Rule 1: one response per student per form (course+semester)
    boolean existsByFeedbackForm_IdAndStudent_Id(Long formId, Long studentId);

    Optional<FeedbackResponse> findByFeedbackForm_IdAndStudent_Id(Long formId, Long studentId);

    List<FeedbackResponse> findByStudent_Id(Long studentId);

    List<FeedbackResponse> findByFeedbackForm_Id(Long formId);

    long countByFeedbackForm_Id(Long formId);

    long count();
}
