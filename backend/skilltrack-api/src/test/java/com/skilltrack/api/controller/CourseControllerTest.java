package com.skilltrack.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilltrack.api.dto.request.CourseCreateRequest;
import com.skilltrack.api.dto.request.CourseUpdateRequest;
import com.skilltrack.api.dto.response.CourseResponse;
import com.skilltrack.api.exception.GlobalExceptionHandler;
import com.skilltrack.api.service.CourseService;
import com.skilltrack.common.enums.CourseStatus;
import com.skilltrack.common.enums.DifficultyLevel;
import com.skilltrack.common.exception.BusinessException;
import com.skilltrack.common.exception.ResourceNotFoundException;
import com.skilltrack.common.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private CourseService courseService;

    private final org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    "instructor@example.com", null, List.of());

    @BeforeEach
    void setUp() {
        CourseController controller = new CourseController(courseService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private CourseResponse buildResponse() {
        CourseResponse r = new CourseResponse();
        r.setId("course-1");
        r.setTitle("Java 101");
        r.setStatus(CourseStatus.DRAFT);
        return r;
    }

    @Test
    void createCourse_returns201() throws Exception {
        CourseCreateRequest request = new CourseCreateRequest("Java 101", "Learn Java", DifficultyLevel.BEGINNER);
        when(courseService.createCourse(any(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/courses").principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Java 101"));
    }

    @Test
    void updateCourse_returns200() throws Exception {
        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setTitle("Java 102"); request.setDescription("Advanced"); request.setDifficulty(DifficultyLevel.INTERMEDIATE);
        when(courseService.updateCourse(anyString(), any(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(put("/api/courses/course-1").principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void publishCourse_returns200() throws Exception {
        when(courseService.publishCourse(anyString(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/courses/course-1/publish").principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    void publishCourse_noModules_returns400() throws Exception {
        when(courseService.publishCourse(anyString(), anyString()))
                .thenThrow(new BusinessException("Course cannot be published"));

        mockMvc.perform(post("/api/courses/course-1/publish").principal(auth))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCourse_returns200() throws Exception {
        doNothing().when(courseService).deleteCourse(anyString(), anyString());

        mockMvc.perform(delete("/api/courses/course-1").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Course deleted successfully"));
    }

    @Test
    void getCourseById_returns200() throws Exception {
        when(courseService.getCourseById(anyString(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(get("/api/courses/course-1").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java 101"));
    }

    @Test
    void getCourseById_notFound_returns404() throws Exception {
        when(courseService.getCourseById(anyString(), anyString()))
                .thenThrow(new ResourceNotFoundException("Course not found"));

        mockMvc.perform(get("/api/courses/course-1").principal(auth))
                .andExpect(status().isNotFound());
    }

    @Test
    void getInstructorCourses_returnsList() throws Exception {
        when(courseService.getInstructorCourses(anyString())).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/courses/my-courses").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java 101"));
    }

    @Test
    void getInstructorCoursesPaged_returnsPage() throws Exception {
        Page<CourseResponse> page = new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 20), 1);
        when(courseService.getInstructorCourses(anyString(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/courses/my-courses/paged").principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    void getInstructorCoursesByStatus_returnsList() throws Exception {
        when(courseService.getInstructorCoursesByStatus(anyString(), any())).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/courses/my-courses/status/DRAFT").principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    void getCoursesWithDraftChanges_returnsList() throws Exception {
        when(courseService.getCoursesWithDraftChanges(anyString())).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/courses/my-courses/draft-changes").principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    void getCourseStatistics_returns200() throws Exception {
        CourseRepository.CourseStatistics stats = new CourseRepository.CourseStatistics() {
            public Long getTotalCourses() { return 5L; }
            public Long getPublishedCourses() { return 3L; }
            public Long getDraftCourses() { return 2L; }
        };
        when(courseService.getCourseStatistics(anyString())).thenReturn(stats);

        mockMvc.perform(get("/api/courses/statistics").principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    void getPublishedCourses_returns200() throws Exception {
        Page<CourseResponse> page = new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 20), 1);
        when(courseService.getPublishedCourses(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/courses/public"))
                .andExpect(status().isOk());
    }

    @Test
    void searchPublishedCourses_returns200() throws Exception {
        Page<CourseResponse> page = new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 20), 1);
        when(courseService.searchPublishedCourses(anyString(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/courses/public/search").param("q", "java"))
                .andExpect(status().isOk());
    }

    @Test
    void getPublishedCoursesByDifficulty_returns200() throws Exception {
        Page<CourseResponse> page = new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 20), 1);
        when(courseService.getPublishedCoursesByDifficulty(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/courses/public/difficulty/BEGINNER"))
                .andExpect(status().isOk());
    }

    @Test
    void getPublishedCoursesByTag_returns200() throws Exception {
        Page<CourseResponse> page = new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 20), 1);
        when(courseService.getPublishedCoursesByTag(anyString(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/courses/public/tag/java"))
                .andExpect(status().isOk());
    }

    @Test
    void getPublishedCourseById_returns200() throws Exception {
        when(courseService.getPublishedCourseById(anyString())).thenReturn(buildResponse());

        mockMvc.perform(get("/api/courses/public/course-1"))
                .andExpect(status().isOk());
    }
}
