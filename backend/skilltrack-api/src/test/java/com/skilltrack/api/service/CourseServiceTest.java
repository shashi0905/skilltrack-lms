package com.skilltrack.api.service;

import com.skilltrack.api.dto.request.CourseCreateRequest;
import com.skilltrack.api.dto.request.CourseUpdateRequest;
import com.skilltrack.api.dto.response.CourseResponse;
import com.skilltrack.common.entity.Course;
import com.skilltrack.common.entity.CourseModule;
import com.skilltrack.common.entity.Lesson;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.CourseStatus;
import com.skilltrack.common.enums.DifficultyLevel;
import com.skilltrack.common.exception.BusinessException;
import com.skilltrack.common.exception.ResourceNotFoundException;
import com.skilltrack.common.repository.CourseRepository;
import com.skilltrack.common.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourseMapper courseMapper;

    @InjectMocks
    private CourseService courseService;

    private User instructor;
    private Course course;
    private CourseResponse courseResponse;

    @BeforeEach
    void setUp() {
        instructor = User.builder().email("instructor@example.com").fullName("Instructor").build();

        course = new Course("Java 101", "Learn Java", DifficultyLevel.BEGINNER, instructor);

        courseResponse = new CourseResponse();
        courseResponse.setTitle("Java 101");
        courseResponse.setInstructorName("Instructor");
    }

    // ==================== createCourse ====================

    @Test
    void createCourse_success() {
        CourseCreateRequest request = new CourseCreateRequest("Java 101", "Learn Java", DifficultyLevel.BEGINNER);
        request.setTags("java,programming");
        request.setEstimatedDurationHours(10);

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.existsByTitleAndInstructor("Java 101", instructor)).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(courseMapper.toResponse(course)).thenReturn(courseResponse);

        CourseResponse result = courseService.createCourse(request, "instructor@example.com");

        assertThat(result.getTitle()).isEqualTo("Java 101");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createCourse_duplicateTitle_throwsBusinessException() {
        CourseCreateRequest request = new CourseCreateRequest("Java 101", "Learn Java", DifficultyLevel.BEGINNER);

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.existsByTitleAndInstructor("Java 101", instructor)).thenReturn(true);

        assertThatThrownBy(() -> courseService.createCourse(request, "instructor@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createCourse_instructorNotFound_throwsResourceNotFoundException() {
        CourseCreateRequest request = new CourseCreateRequest("Java 101", "Learn Java", DifficultyLevel.BEGINNER);

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.createCourse(request, "unknown@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== updateCourse ====================

    @Test
    void updateCourse_success() {
        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setTitle("Java 102");
        request.setDescription("Advanced Java");
        request.setDifficulty(DifficultyLevel.INTERMEDIATE);

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(courseRepository.existsByTitleAndInstructor("Java 102", instructor)).thenReturn(false);
        when(courseRepository.save(course)).thenReturn(course);
        when(courseMapper.toResponse(course)).thenReturn(courseResponse);

        courseService.updateCourse("course-1", request, "instructor@example.com");

        assertThat(course.getTitle()).isEqualTo("Java 102");
        assertThat(course.getDescription()).isEqualTo("Advanced Java");
        verify(courseRepository).save(course);
    }

    @Test
    void updateCourse_duplicateTitle_throwsBusinessException() {
        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setTitle("Existing Course");
        request.setDescription("desc");
        request.setDifficulty(DifficultyLevel.BEGINNER);

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(courseRepository.existsByTitleAndInstructor("Existing Course", instructor)).thenReturn(true);

        assertThatThrownBy(() -> courseService.updateCourse("course-1", request, "instructor@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateCourse_sameTitleNoConflict_succeeds() {
        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setTitle("Java 101"); // same title as existing
        request.setDescription("Updated desc");
        request.setDifficulty(DifficultyLevel.BEGINNER);

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(courseRepository.save(course)).thenReturn(course);
        when(courseMapper.toResponse(course)).thenReturn(courseResponse);

        courseService.updateCourse("course-1", request, "instructor@example.com");

        verify(courseRepository).save(course);
        // existsByTitleAndInstructor should NOT be called since title didn't change
        verify(courseRepository, never()).existsByTitleAndInstructor(anyString(), any());
    }

    @Test
    void updateCourse_notOwner_throwsResourceNotFoundException() {
        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setTitle("title");
        request.setDescription("desc");
        request.setDifficulty(DifficultyLevel.BEGINNER);

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.updateCourse("course-1", request, "instructor@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== publishCourse ====================

    @Test
    void publishCourse_withModulesAndLessons_succeeds() {
        CourseModule module = new CourseModule("Module 1", "Desc", course);
        Lesson lesson = new Lesson("Lesson 1", "Desc", "Content", module);
        module.setLessons(new ArrayList<>(List.of(lesson)));
        course.setModules(new ArrayList<>(List.of(module)));

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(courseRepository.save(course)).thenReturn(course);
        when(courseMapper.toResponse(course)).thenReturn(courseResponse);

        courseService.publishCourse("course-1", "instructor@example.com");

        assertThat(course.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        assertThat(course.getPublishedAt()).isNotNull();
    }

    @Test
    void publishCourse_noModules_throwsBusinessException() {
        course.setModules(new ArrayList<>());

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.publishCourse("course-1", "instructor@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannot be published");
    }

    @Test
    void publishCourse_modulesWithNoLessons_throwsBusinessException() {
        CourseModule emptyModule = new CourseModule("Module 1", "Desc", course);
        emptyModule.setLessons(new ArrayList<>());
        course.setModules(new ArrayList<>(List.of(emptyModule)));

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.publishCourse("course-1", "instructor@example.com"))
                .isInstanceOf(BusinessException.class);
    }

    // ==================== deleteCourse ====================

    @Test
    void deleteCourse_draftCourse_succeeds() {
        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));

        courseService.deleteCourse("course-1", "instructor@example.com");

        verify(courseRepository).delete(course);
    }

    @Test
    void deleteCourse_publishedCourse_throwsBusinessException() {
        course.setStatus(CourseStatus.PUBLISHED);

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.deleteCourse("course-1", "instructor@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Published courses cannot be deleted");
    }

    // ==================== getCourseById ====================

    @Test
    void getCourseById_found_returnsResponse() {
        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(courseMapper.toResponse(course)).thenReturn(courseResponse);

        CourseResponse result = courseService.getCourseById("course-1", "instructor@example.com");

        assertThat(result.getTitle()).isEqualTo("Java 101");
    }

    @Test
    void getCourseById_notFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById("course-1", "instructor@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== getInstructorCourses ====================

    @Test
    void getInstructorCourses_returnsList() {
        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findByInstructorOrderByCreatedAtDesc(instructor)).thenReturn(List.of(course));
        when(courseMapper.toResponseList(List.of(course))).thenReturn(List.of(courseResponse));

        List<CourseResponse> result = courseService.getInstructorCourses("instructor@example.com");

        assertThat(result).hasSize(1);
    }

    // ==================== getPublishedCourseById ====================

    @Test
    void getPublishedCourseById_found_returnsResponse() {
        CourseModule module = new CourseModule("Module 1", "Desc", course);
        Lesson lesson = new Lesson("Lesson 1", "Desc", "Content", module);
        module.setLessons(new ArrayList<>(List.of(lesson)));
        course.setModules(new ArrayList<>(List.of(module)));

        when(courseRepository.findPublishedCourseByIdWithModules("course-1")).thenReturn(Optional.of(course));
        when(courseMapper.toResponse(course)).thenReturn(courseResponse);

        CourseResponse result = courseService.getPublishedCourseById("course-1");

        assertThat(result.getTitle()).isEqualTo("Java 101");
    }

    @Test
    void getPublishedCourseById_notFound_throwsResourceNotFoundException() {
        when(courseRepository.findPublishedCourseByIdWithModules("course-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getPublishedCourseById("course-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
