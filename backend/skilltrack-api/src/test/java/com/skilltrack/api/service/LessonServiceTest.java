package com.skilltrack.api.service;

import com.skilltrack.api.dto.request.LessonCreateRequest;
import com.skilltrack.api.dto.response.LessonResponse;
import com.skilltrack.common.entity.Course;
import com.skilltrack.common.entity.CourseModule;
import com.skilltrack.common.entity.Lesson;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.DifficultyLevel;
import com.skilltrack.common.exception.BusinessException;
import com.skilltrack.common.exception.ResourceNotFoundException;
import com.skilltrack.common.repository.CourseModuleRepository;
import com.skilltrack.common.repository.CourseRepository;
import com.skilltrack.common.repository.LessonRepository;
import com.skilltrack.common.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock private LessonRepository lessonRepository;
    @Mock private CourseModuleRepository moduleRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @Mock private LessonMapper lessonMapper;

    @InjectMocks
    private LessonService lessonService;

    private User instructor;
    private Course course;
    private CourseModule module;
    private Lesson lesson;
    private LessonResponse lessonResponse;

    private static final String EMAIL = "instructor@example.com";
    private static final String COURSE_ID = "course-1";
    private static final String MODULE_ID = "module-1";
    private static final String LESSON_ID = "lesson-1";

    @BeforeEach
    void setUp() {
        instructor = User.builder().email(EMAIL).fullName("Instructor").build();
        course = new Course("Java 101", "Learn Java", DifficultyLevel.BEGINNER, instructor);
        module = new CourseModule("Module 1", "First module", course);
        module.setOrderIndex(1);
        lesson = new Lesson("Lesson 1", "First lesson", "Content here", module);
        lesson.setOrderIndex(1);

        lessonResponse = new LessonResponse();
        lessonResponse.setTitle("Lesson 1");
    }

    private void stubOwnershipChain() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor(COURSE_ID, instructor)).thenReturn(Optional.of(course));
        when(moduleRepository.findByIdAndCourse(MODULE_ID, course)).thenReturn(Optional.of(module));
    }

    // ==================== createLesson ====================

    @Test
    void createLesson_success() {
        LessonCreateRequest request = new LessonCreateRequest("Lesson 1", "First lesson", "Content");
        request.setEstimatedDurationMinutes(30);

        stubOwnershipChain();
        when(lessonRepository.existsByTitleAndModule("Lesson 1", module)).thenReturn(false);
        when(lessonRepository.findNextOrderIndex(module)).thenReturn(1);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);
        when(lessonMapper.toResponse(lesson)).thenReturn(lessonResponse);

        LessonResponse result = lessonService.createLesson(COURSE_ID, MODULE_ID, request, EMAIL);

        assertThat(result.getTitle()).isEqualTo("Lesson 1");
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    void createLesson_duplicateTitle_throwsBusinessException() {
        LessonCreateRequest request = new LessonCreateRequest("Lesson 1", "desc", "content");

        stubOwnershipChain();
        when(lessonRepository.existsByTitleAndModule("Lesson 1", module)).thenReturn(true);

        assertThatThrownBy(() -> lessonService.createLesson(COURSE_ID, MODULE_ID, request, EMAIL))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createLesson_instructorNotFound_throwsResourceNotFoundException() {
        LessonCreateRequest request = new LessonCreateRequest("Lesson 1", "desc", "content");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.createLesson(COURSE_ID, MODULE_ID, request, EMAIL))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createLesson_courseNotFound_throwsResourceNotFoundException() {
        LessonCreateRequest request = new LessonCreateRequest("Lesson 1", "desc", "content");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor(COURSE_ID, instructor)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.createLesson(COURSE_ID, MODULE_ID, request, EMAIL))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createLesson_moduleNotFound_throwsResourceNotFoundException() {
        LessonCreateRequest request = new LessonCreateRequest("Lesson 1", "desc", "content");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor(COURSE_ID, instructor)).thenReturn(Optional.of(course));
        when(moduleRepository.findByIdAndCourse(MODULE_ID, course)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.createLesson(COURSE_ID, MODULE_ID, request, EMAIL))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== updateLesson ====================

    @Test
    void updateLesson_success() {
        LessonCreateRequest request = new LessonCreateRequest("Updated Lesson", "Updated desc", "New content");

        stubOwnershipChain();
        when(lessonRepository.findByIdAndModule(LESSON_ID, module)).thenReturn(Optional.of(lesson));
        when(lessonRepository.existsByTitleAndModule("Updated Lesson", module)).thenReturn(false);
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        when(lessonMapper.toResponse(lesson)).thenReturn(lessonResponse);

        lessonService.updateLesson(COURSE_ID, MODULE_ID, LESSON_ID, request, EMAIL);

        verify(lessonRepository).save(lesson);
    }

    @Test
    void updateLesson_sameTitleNoConflict_succeeds() {
        LessonCreateRequest request = new LessonCreateRequest("Lesson 1", "Updated desc", "New content");

        stubOwnershipChain();
        when(lessonRepository.findByIdAndModule(LESSON_ID, module)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        when(lessonMapper.toResponse(lesson)).thenReturn(lessonResponse);

        lessonService.updateLesson(COURSE_ID, MODULE_ID, LESSON_ID, request, EMAIL);

        verify(lessonRepository, never()).existsByTitleAndModule(anyString(), any());
    }

    @Test
    void updateLesson_duplicateTitle_throwsBusinessException() {
        LessonCreateRequest request = new LessonCreateRequest("Existing Lesson", "desc", "content");

        stubOwnershipChain();
        when(lessonRepository.findByIdAndModule(LESSON_ID, module)).thenReturn(Optional.of(lesson));
        when(lessonRepository.existsByTitleAndModule("Existing Lesson", module)).thenReturn(true);

        assertThatThrownBy(() -> lessonService.updateLesson(COURSE_ID, MODULE_ID, LESSON_ID, request, EMAIL))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void updateLesson_lessonNotFound_throwsResourceNotFoundException() {
        LessonCreateRequest request = new LessonCreateRequest("Lesson", "desc", "content");

        stubOwnershipChain();
        when(lessonRepository.findByIdAndModule(LESSON_ID, module)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.updateLesson(COURSE_ID, MODULE_ID, LESSON_ID, request, EMAIL))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== deleteLesson ====================

    @Test
    void deleteLesson_success() {
        stubOwnershipChain();
        when(lessonRepository.findByIdAndModule(LESSON_ID, module)).thenReturn(Optional.of(lesson));

        lessonService.deleteLesson(COURSE_ID, MODULE_ID, LESSON_ID, EMAIL);

        verify(lessonRepository).delete(lesson);
    }

    @Test
    void deleteLesson_lessonNotFound_throwsResourceNotFoundException() {
        stubOwnershipChain();
        when(lessonRepository.findByIdAndModule(LESSON_ID, module)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.deleteLesson(COURSE_ID, MODULE_ID, LESSON_ID, EMAIL))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== getModuleLessons ====================

    @Test
    void getModuleLessons_returnsList() {
        stubOwnershipChain();
        when(lessonRepository.findByModuleOrderByOrderIndexAsc(module)).thenReturn(List.of(lesson));
        when(lessonMapper.toResponseList(List.of(lesson))).thenReturn(List.of(lessonResponse));

        List<LessonResponse> result = lessonService.getModuleLessons(COURSE_ID, MODULE_ID, EMAIL);

        assertThat(result).hasSize(1);
    }

    // ==================== getLessonById ====================

    @Test
    void getLessonById_found_returnsResponse() {
        stubOwnershipChain();
        when(lessonRepository.findByIdAndModule(LESSON_ID, module)).thenReturn(Optional.of(lesson));
        when(lessonMapper.toResponse(lesson)).thenReturn(lessonResponse);

        LessonResponse result = lessonService.getLessonById(COURSE_ID, MODULE_ID, LESSON_ID, EMAIL);

        assertThat(result.getTitle()).isEqualTo("Lesson 1");
    }

    @Test
    void getLessonById_notFound_throwsResourceNotFoundException() {
        stubOwnershipChain();
        when(lessonRepository.findByIdAndModule(LESSON_ID, module)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.getLessonById(COURSE_ID, MODULE_ID, LESSON_ID, EMAIL))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
