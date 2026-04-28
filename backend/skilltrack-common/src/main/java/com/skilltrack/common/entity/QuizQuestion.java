package com.skilltrack.common.entity;

import com.skilltrack.common.enums.QuestionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_questions")
public class QuizQuestion extends BaseEntity {

    @NotNull(message = "Lesson is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @NotBlank(message = "Question text is required")
    @Size(max = 1000, message = "Question text must not exceed 1000 characters")
    @Column(name = "question_text", nullable = false, length = 1000)
    private String questionText;

    @NotNull(message = "Question type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private QuestionType questionType;

    @NotNull(message = "Order index is required")
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Size(max = 500, message = "Explanation must not exceed 500 characters")
    @Column(length = 500)
    private String explanation;

    @NotNull(message = "Points is required")
    @Column(nullable = false)
    private Integer points = 1;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QuizOption> options = new ArrayList<>();

    // Constructors
    public QuizQuestion() {}

    public QuizQuestion(Lesson lesson, String questionText, QuestionType questionType) {
        this.lesson = lesson;
        this.questionText = questionText;
        this.questionType = questionType;
    }

    // Business methods
    public boolean isMultipleChoice() {
        return questionType == QuestionType.MULTIPLE_CHOICE;
    }

    public boolean isSingleChoice() {
        return questionType == QuestionType.SINGLE_CHOICE;
    }

    public boolean isTrueFalse() {
        return questionType == QuestionType.TRUE_FALSE;
    }

    public List<QuizOption> getCorrectOptions() {
        return options.stream()
                .filter(QuizOption::isCorrect)
                .toList();
    }

    public boolean hasCorrectAnswer() {
        return options.stream().anyMatch(QuizOption::isCorrect);
    }

    // Helper methods for managing options
    public void addOption(QuizOption option) {
        options.add(option);
        option.setQuestion(this);
    }

    public void removeOption(QuizOption option) {
        options.remove(option);
        option.setQuestion(null);
    }

    // Getters and Setters
    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public List<QuizOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuizOption> options) {
        this.options = options;
    }
}