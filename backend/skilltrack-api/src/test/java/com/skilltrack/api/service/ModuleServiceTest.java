package com.skilltrack.api.service;

import com.skilltrack.api.dto.request.ModuleCreateRequest;
import com.skilltrack.api.dto.response.ModuleResponse;
import com.skilltrack.common.entity.Course;
import com.skilltrack.common.entity.CourseModule;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.DifficultyLevel;
import com.skilltrack.common.exception.BusinessException;
import com.skilltrack.common.exception.ResourceNotFoundException;
import com.skilltrack.common.repository.CourseModuleRepository;
import com.skilltrack.common.repository.CourseRepository;
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
class ModuleServiceTest {

    @Mock private CourseModuleRepository moduleRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @Mock private ModuleMapper moduleMapper;

    @InjectMocks
    private ModuleService moduleService;

    private User instructor;
    private Course course;
    private CourseModule module;
    private ModuleResponse moduleResponse;

    @BeforeEach
    void setUp() {
        instructor = User.builder().email("instructor@example.com").fullName("Instructor").build();
        course = new Course("Java 101", "Learn Java", DifficultyLevel.BEGINNER, instructor);
        module = new CourseModule("Module 1", "First module", course);
        module.setOrderIndex(1);

        moduleResponse = new ModuleResponse();
        moduleResponse.setTitle("Module 1");
    }

    // ==================== createModule ====================

    @Test
    void createModule_success() {
        ModuleCreateRequest request = new ModuleCreateRequest("Module 1", "First module");

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(moduleRepository.existsByTitleAndCourse("Module 1", course)).thenReturn(false);
        when(moduleRepository.findNextOrderIndex("course-1")).thenReturn(1);
        when(moduleRepository.save(any(CourseModule.class))).thenReturn(module);
        when(moduleMapper.toResponse(module)).thenReturn(moduleResponse);

        ModuleResponse result = moduleService.createModule("course-1", request, "instructor@example.com");

        assertThat(result.getTitle()).isEqualTo("Module 1");
        verify(moduleRepository).save(any(CourseModule.class));
    }

    @Test
    void createModule_duplicateTitle_throwsBusinessException() {
        ModuleCreateRequest request = new ModuleCreateRequest("Module 1", "First module");

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(moduleRepository.existsByTitleAndCourse("Module 1", course)).thenReturn(true);

        assertThatThrownBy(() -> moduleService.createModule("course-1", request, "instructor@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createModule_courseNotFound_throwsResourceNotFoundException() {
        ModuleCreateRequest request = new ModuleCreateRequest("Module 1", "First module");

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moduleService.createModule("course-1", request, "instructor@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== updateModule ====================

    @Test
    void updateModule_success() {
        ModuleCreateRequest request = new ModuleCreateRequest("Updated Module", "Updated desc");

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(moduleRepository.findByIdAndCourse("module-1", course)).thenReturn(Optional.of(module));
        when(moduleRepository.existsByTitleAndCourse("Updated Module", course)).thenReturn(false);
        when(moduleRepository.save(module)).thenReturn(module);
        when(moduleMapper.toResponse(module)).thenReturn(moduleResponse);

        moduleService.updateModule("course-1", "module-1", request, "instructor@example.com");

        verify(moduleRepository).save(module);
    }

    @Test
    void updateModule_sameTitleNoConflict_succeeds() {
        ModuleCreateRequest request = new ModuleCreateRequest("Module 1", "Updated desc");

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(moduleRepository.findByIdAndCourse("module-1", course)).thenReturn(Optional.of(module));
        when(moduleRepository.save(module)).thenReturn(module);
        when(moduleMapper.toResponse(module)).thenReturn(moduleResponse);

        moduleService.updateModule("course-1", "module-1", request, "instructor@example.com");

        verify(moduleRepository, never()).existsByTitleAndCourse(anyString(), any());
    }

    @Test
    void updateModule_duplicateTitle_throwsBusinessException() {
        ModuleCreateRequest request = new ModuleCreateRequest("Existing Module", "desc");

        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(moduleRepository.findByIdAndCourse("module-1", course)).thenReturn(Optional.of(module));
        when(moduleRepository.existsByTitleAndCourse("Existing Module", course)).thenReturn(true);

        assertThatThrownBy(() -> moduleService.updateModule("course-1", "module-1", request, "instructor@example.com"))
                .isInstanceOf(BusinessException.class);
    }

    // ==================== deleteModule ====================

    @Test
    void deleteModule_success() {
        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(moduleRepository.findByIdAndCourse("module-1", course)).thenReturn(Optional.of(module));

        moduleService.deleteModule("course-1", "module-1", "instructor@example.com");

        verify(moduleRepository).delete(module);
    }

    @Test
    void deleteModule_moduleNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(moduleRepository.findByIdAndCourse("module-1", course)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moduleService.deleteModule("course-1", "module-1", "instructor@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== getCourseModules ====================

    @Test
    void getCourseModules_returnsList() {
        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(moduleRepository.findByCourseWithLessonsOrderByOrderIndexAsc(course)).thenReturn(List.of(module));
        when(moduleMapper.toResponseList(List.of(module))).thenReturn(List.of(moduleResponse));

        List<ModuleResponse> result = moduleService.getCourseModules("course-1", "instructor@example.com");

        assertThat(result).hasSize(1);
    }

    // ==================== getModuleById ====================

    @Test
    void getModuleById_found_returnsResponse() {
        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(moduleRepository.findByIdAndCourse("module-1", course)).thenReturn(Optional.of(module));
        when(moduleMapper.toResponse(module)).thenReturn(moduleResponse);

        ModuleResponse result = moduleService.getModuleById("course-1", "module-1", "instructor@example.com");

        assertThat(result.getTitle()).isEqualTo("Module 1");
    }

    @Test
    void getModuleById_notFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(courseRepository.findCourseByIdAndInstructor("course-1", instructor)).thenReturn(Optional.of(course));
        when(moduleRepository.findByIdAndCourse("module-1", course)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moduleService.getModuleById("course-1", "module-1", "instructor@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
