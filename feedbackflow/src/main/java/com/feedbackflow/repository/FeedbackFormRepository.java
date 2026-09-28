package com.feedbackflow.repository;

import com.feedbackflow.entity.FeedbackForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackFormRepository extends JpaRepository<FeedbackForm, Long> {

    List<FeedbackForm> findByCourse_Id(Long courseId);

    @Query("SELECT f FROM FeedbackForm f WHERE f.course.id IN :courseIds")
    List<FeedbackForm> findByCourseIds(@Param("courseIds") List<Long> courseIds);

    long countByActiveTrue();
}
