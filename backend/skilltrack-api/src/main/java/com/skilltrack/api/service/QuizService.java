package com.skilltrack.api.service;

import com.skilltrack.api.dto.request.QuizOptionCreateRequest;
import com.skilltrack.api.dto.request.QuizQuestionCreateRequest;
import com.skilltrack.api.dto.response.QuizQuestionResponse;
import com.skilltrack.common.entity.*;
import com.skilltrack.common.enums.ContentType;
import com.skilltrack.common.enums.QuestionType;
import com.skilltrack.common.exception.BusinessException;
import com.skilltrack.common.exception.ResourceNotFoundException;
import com.skilltrack.common.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class QuizService {

    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final LessonRepository lessonRepository;
    private final CourseModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final QuizQuestionMapper quizQuestionMapper;

    @Autowired
    public QuizService(QuizQuestionRepository quizQuestionRepository,
                      QuizOptionRepository quizOptionRepository,
                      LessonRepository lessonRepository,
                      CourseModuleRepository moduleRepository,
                      CourseRepository courseRepository,
                      UserRepository userRepository,
                      QuizQuestionMapper quizQuestionMapper) {
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizOptionRepository = quizOptionRepository;
        this.lessonRepository = lessonRepository;
        this.moduleRepository = moduleRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.quizQuestionMapper = quizQuestionMapper;
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public QuizQuestionResponse addQuestionToLesson(String courseId, String moduleId, String lessonId,
                                                  QuizQuestionCreateRequest request, String instructorEmail) {
        
        Lesson lesson = validateInstructorAccess(courseId, moduleId, lessonId, instructorEmail);
        
        // Ensure lesson is a quiz lesson
        if (lesson.getContentType() != ContentType.QUIZ) {
            throw new BusinessException("Lesson must be of type QUIZ to add questions");
        }

        // Validate question request
        validateQuestionRequest(request);

        // Create question
        QuizQuestion question = new QuizQuestion(lesson, request.getQuestionText(), request.getQuestionType());
        question.setExplanation(request.getExplanation());
        question.setPoints(request.getPoints());
        
        Integer nextOrderIndex = quizQuestionRepository.findNextOrderIndex(lesson);
        question.setOrderIndex(nextOrderIndex);

        QuizQuestion savedQuestion = quizQuestionRepository.save(question);

        // Create options
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            for (int i = 0; i < request.getOptions().size(); i++) {
                QuizOptionCreateRequest optionRequest = request.getOptions().get(i);
                QuizOption option = new QuizOption(savedQuestion, optionRequest.getOptionText(), optionRequest.getIsCorrect());
                option.setOrderIndex(i + 1);
                savedQuestion.addOption(option);
            }
            quizQuestionRepository.save(savedQuestion);
        }

        return quizQuestionMapper.toResponse(savedQuestion);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public QuizQuestionResponse updateQuestion(String courseId, String moduleId, String lessonId,
                                             String questionId, QuizQuestionCreateRequest request, String instructorEmail) {
        
        Lesson lesson = validateInstructorAccess(courseId, moduleId, lessonId, instructorEmail);
        
        QuizQuestion question = quizQuestionRepository.findByIdAndLesson(questionId, lesson)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        // Validate question request
        validateQuestionRequest(request);

        // Update question
        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(request.getQuestionType());
        question.setExplanation(request.getExplanation());
        question.setPoints(request.getPoints());

        // Update options - remove existing and add new ones
        question.getOptions().clear();
        
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            for (int i = 0; i < request.getOptions().size(); i++) {
                QuizOptionCreateRequest optionRequest = request.getOptions().get(i);
                QuizOption option = new QuizOption(question, optionRequest.getOptionText(), optionRequest.getIsCorrect());
                option.setOrderIndex(i + 1);
                question.addOption(option);
            }
        }

        QuizQuestion savedQuestion = quizQuestionRepository.save(question);
        return quizQuestionMapper.toResponse(savedQuestion);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public void deleteQuestion(String courseId, String moduleId, String lessonId,
                             String questionId, String instructorEmail) {
        
        Lesson lesson = validateInstructorAccess(courseId, moduleId, lessonId, instructorEmail);
        
        QuizQuestion question = quizQuestionRepository.findByIdAndLesson(questionId, lesson)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        lesson.removeQuizQuestion(question);
        quizQuestionRepository.delete(question);
    }

    @Transactional(readOnly = true)
    public List<QuizQuestionResponse> getQuestionsForLesson(String courseId, String moduleId, String lessonId,
                                                          String instructorEmail) {
        
        Lesson lesson = validateInstructorAccess(courseId, moduleId, lessonId, instructorEmail);
        
        List<QuizQuestion> questions = quizQuestionRepository.findByLessonOrderByOrderIndexAsc(lesson);
        return quizQuestionMapper.toResponseList(questions);
    }

    @Transactional(readOnly = true)
    public QuizQuestionResponse getQuestionById(String courseId, String moduleId, String lessonId,
                                              String questionId, String instructorEmail) {
        
        Lesson lesson = validateInstructorAccess(courseId, moduleId, lessonId, instructorEmail);
        
        QuizQuestion question = quizQuestionRepository.findByIdAndLesson(questionId, lesson)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        return quizQuestionMapper.toResponse(question);
    }

    private void validateQuestionRequest(QuizQuestionCreateRequest request) {
        if (request.getOptions() == null || request.getOptions().isEmpty()) {
            throw new BusinessException("Question must have at least one option");
        }

        QuestionType questionType = request.getQuestionType();
        List<QuizOptionCreateRequest> options = request.getOptions();

        // Validate option count
        if (options.size() < questionType.getMinOptions() || options.size() > questionType.getMaxOptions()) {
            throw new BusinessException(String.format("Question type %s requires between %d and %d options",
                    questionType.getDisplayName(), questionType.getMinOptions(), questionType.getMaxOptions()));
        }

        // Count correct answers
        long correctCount = options.stream().mapToLong(opt -> Boolean.TRUE.equals(opt.getIsCorrect()) ? 1 : 0).sum();

        // Validate correct answers based on question type
        if (questionType == QuestionType.SINGLE_CHOICE || questionType == QuestionType.TRUE_FALSE) {
            if (correctCount != 1) {
                throw new BusinessException(questionType.getDisplayName() + " questions must have exactly one correct answer");
            }
        } else if (questionType == QuestionType.MULTIPLE_CHOICE) {
            if (correctCount < 1) {
                throw new BusinessException("Multiple choice questions must have at least one correct answer");
            }
        }
    }

    private Lesson validateInstructorAccess(String courseId, String moduleId, String lessonId, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findCourseByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        CourseModule module = moduleRepository.findByIdAndCourse(moduleId, course)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        return lessonRepository.findByIdAndModule(lessonId, module)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
    }
}