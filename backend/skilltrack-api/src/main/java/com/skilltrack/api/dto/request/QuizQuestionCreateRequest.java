package com.skilltrack.api.dto.request;

import com.skilltrack.common.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public class QuizQuestionCreateRequest {

    @NotBlank(message = "Question text is required")
    @Size(max = 1000, message = "Question text must not exceed 1000 characters")
    private String questionText;

    @NotNull(message = "Question type is required")
    private QuestionType questionType;

    @Size(max = 500, message = "Explanation must not exceed 500 characters")
    private String explanation;

    @Positive(message = "Points must be positive")
    private Integer points = 1;

    @Valid
    private List<QuizOptionCreateRequest> options;

    // Constructors
    public QuizQuestionCreateRequest() {}

    public QuizQuestionCreateRequest(String questionText, QuestionType questionType) {
        this.questionText = questionText;
        this.questionType = questionType;
    }

    // Getters and Setters
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

    public List<QuizOptionCreateRequest> getOptions() {
        return options;
    }

    public void setOptions(List<QuizOptionCreateRequest> options) {
        this.options = options;
    }
}