package com.feedbackflow.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private FeedbackResponse feedbackResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // Populated when the question type is RATING (1-5)
    private Integer ratingValue;

    // Populated when the question type is TEXT
    @Column(length = 2000)
    private String textValue;
}
