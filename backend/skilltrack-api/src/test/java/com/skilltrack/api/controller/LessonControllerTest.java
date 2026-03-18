package com.skilltrack.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilltrack.api.dto.request.LessonCreateRequest;
import com.skilltrack.api.dto.response.LessonResponse;
import com.skilltrack.api.exception.GlobalExceptionHandler;
import com.skilltrack.api.service.LessonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LessonControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock private LessonService lessonService;

    private static final String BASE = "/api/courses/c1/modules/m1/lessons";

    private final org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    "instructor@example.com", null, List.of());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LessonController(lessonService))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private LessonResponse buildResponse() {
        LessonResponse r = new LessonResponse();
        r.setId("lesson-1"); r.setTitle("Lesson 1"); r.setOrderIndex(1);
        return r;
    }

    @Test
    void createLesson_returns201() throws Exception {
        when(lessonService.createLesson(anyString(), anyString(), any(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(post(BASE).principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LessonCreateRequest("Lesson 1", "desc", "content"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Lesson 1"));
    }

    @Test
    void updateLesson_returns200() throws Exception {
        when(lessonService.updateLesson(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(put(BASE + "/l1").principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LessonCreateRequest("Updated", "desc", "content"))))
                .andExpect(status().isOk());
    }

    @Test
    void deleteLesson_returns200() throws Exception {
        doNothing().when(lessonService).deleteLesson(anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(delete(BASE + "/l1").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Lesson deleted successfully"));
    }

    @Test
    void getModuleLessons_returnsList() throws Exception {
        when(lessonService.getModuleLessons(anyString(), anyString(), anyString())).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get(BASE).principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Lesson 1"));
    }

    @Test
    void getLessonById_returns200() throws Exception {
        when(lessonService.getLessonById(anyString(), anyString(), anyString(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(get(BASE + "/l1").principal(auth))
                .andExpect(status().isOk());
    }
}
