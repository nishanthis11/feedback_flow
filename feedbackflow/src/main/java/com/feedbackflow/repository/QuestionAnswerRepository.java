package com.feedbackflow.repository;

import com.feedbackflow.dto.QuestionAverageDto;
import com.feedbackflow.entity.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {

    /** Average rating for ONE specific question (e.g. "Course Content" on one form). */
    @Query("SELECT AVG(qa.ratingValue) FROM QuestionAnswer qa " +
           "WHERE qa.question.id = :questionId AND qa.ratingValue IS NOT NULL")
    Double findAverageRatingByQuestionId(@Param("questionId") Long questionId);

    /** Question-wise averages for every RATING question on a given form. */
    @Query("SELECT new com.feedbackflow.dto.QuestionAverageDto(q.id, q.text, AVG(qa.ratingValue), COUNT(qa)) " +
           "FROM QuestionAnswer qa JOIN qa.question q " +
           "WHERE q.feedbackForm.id = :formId AND qa.ratingValue IS NOT NULL " +
           "GROUP BY q.id, q.text ORDER BY q.orderIndex ASC")
    List<QuestionAverageDto> findQuestionAveragesByFormId(@Param("formId") Long formId);

    /** Overall average rating across ALL questions of ONE form (used for course-wise dashboard bars). */
    @Query("SELECT AVG(qa.ratingValue) FROM QuestionAnswer qa " +
           "WHERE qa.question.feedbackForm.id = :formId AND qa.ratingValue IS NOT NULL")
    Double findOverallAverageByFormId(@Param("formId") Long formId);

    /** Overall average rating across ALL forms/courses taught by ONE faculty member. */
    @Query("SELECT AVG(qa.ratingValue) FROM QuestionAnswer qa " +
           "WHERE qa.question.feedbackForm.course.faculty.id = :facultyId AND qa.ratingValue IS NOT NULL")
    Double findAverageRatingByFacultyId(@Param("facultyId") Long facultyId);

    /** System-wide average rating, shown on the admin dashboard. */
    @Query("SELECT AVG(qa.ratingValue) FROM QuestionAnswer qa WHERE qa.ratingValue IS NOT NULL")
    Double findSystemWideAverageRating();
}
