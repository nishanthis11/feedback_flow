package com.feedbackflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents ONE student's complete submission for ONE feedback form.
 * The unique constraint below is the database-level backstop for Rule 1
 * (one response per student per course per semester); the authoritative
 * check happens in FeedbackResponseService before this row is ever saved.
 */
@Entity
@Table(
        name = "feedback_responses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"form_id", "student_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private FeedbackForm feedbackForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "feedbackResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionAnswer> answers = new ArrayList<>();
}
