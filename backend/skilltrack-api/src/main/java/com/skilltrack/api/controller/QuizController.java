package com.skilltrack.api.controller;

import com.skilltrack.api.dto.request.QuizQuestionCreateRequest;
import com.skilltrack.api.dto.response.MessageResponse;
import com.skilltrack.api.dto.response.QuizQuestionResponse;
import com.skilltrack.api.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/modules/{moduleId}/lessons/{lessonId}/quiz")
@Tag(name = "Quiz Management", description = "APIs for managing quiz questions and options")
public class QuizController {

    private final QuizService quizService;

    @Autowired
    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/questions")
    @Operation(summary = "Add question to quiz lesson")
    public ResponseEntity<QuizQuestionResponse> addQuestion(
            @PathVariable String courseId,
            @PathVariable String moduleId,
            @PathVariable String lessonId,
            @Valid @RequestBody QuizQuestionCreateRequest request,
            Authentication authentication) {
        
        QuizQuestionResponse response = quizService.addQuestionToLesson(
                courseId, moduleId, lessonId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/questions/{questionId}")
    @Operation(summary = "Update quiz question")
    public ResponseEntity<QuizQuestionResponse> updateQuestion(
            @PathVariable String courseId,
            @PathVariable String moduleId,
            @PathVariable String lessonId,
            @PathVariable String questionId,
            @Valid @RequestBody QuizQuestionCreateRequest request,
            Authentication authentication) {
        
        QuizQuestionResponse response = quizService.updateQuestion(
                courseId, moduleId, lessonId, questionId, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/questions/{questionId}")
    @Operation(summary = "Delete quiz question")
    public ResponseEntity<MessageResponse> deleteQuestion(
            @PathVariable String courseId,
            @PathVariable String moduleId,
            @PathVariable String lessonId,
            @PathVariable String questionId,
            Authentication authentication) {
        
        quizService.deleteQuestion(courseId, moduleId, lessonId, questionId, authentication.getName());
        return ResponseEntity.ok(new MessageResponse("Question deleted successfully"));
    }

    @GetMapping("/questions")
    @Operation(summary = "Get all questions for quiz lesson")
    public ResponseEntity<List<QuizQuestionResponse>> getQuestions(
            @PathVariable String courseId,
            @PathVariable String moduleId,
            @PathVariable String lessonId,
            Authentication authentication) {
        
        List<QuizQuestionResponse> questions = quizService.getQuestionsForLesson(
                courseId, moduleId, lessonId, authentication.getName());
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questions/{questionId}")
    @Operation(summary = "Get quiz question by ID")
    public ResponseEntity<QuizQuestionResponse> getQuestion(
            @PathVariable String courseId,
            @PathVariable String moduleId,
            @PathVariable String lessonId,
            @PathVariable String questionId,
            Authentication authentication) {
        
        QuizQuestionResponse response = quizService.getQuestionById(
                courseId, moduleId, lessonId, questionId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}