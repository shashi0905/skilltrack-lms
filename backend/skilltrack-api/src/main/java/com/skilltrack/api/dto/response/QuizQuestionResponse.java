package com.skilltrack.api.dto.response;

import com.skilltrack.common.enums.QuestionType;

import java.time.LocalDateTime;
import java.util.List;

public class QuizQuestionResponse {

    private String id;
    private String questionText;
    private QuestionType questionType;
    private Integer orderIndex;
    private String explanation;
    private Integer points;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuizOptionResponse> options;

    // Constructors
    public QuizQuestionResponse() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<QuizOptionResponse> getOptions() {
        return options;
    }

    public void setOptions(List<QuizOptionResponse> options) {
        this.options = options;
    }
}