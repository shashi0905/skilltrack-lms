package com.skilltrack.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilltrack.api.dto.request.ModuleCreateRequest;
import com.skilltrack.api.dto.response.ModuleResponse;
import com.skilltrack.api.exception.GlobalExceptionHandler;
import com.skilltrack.api.service.ModuleService;
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
class ModuleControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock private ModuleService moduleService;

    private final org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    "instructor@example.com", null, List.of());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ModuleController(moduleService))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private ModuleResponse buildResponse() {
        ModuleResponse r = new ModuleResponse();
        r.setId("module-1"); r.setTitle("Module 1"); r.setOrderIndex(1);
        return r;
    }

    @Test
    void createModule_returns201() throws Exception {
        when(moduleService.createModule(anyString(), any(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/courses/c1/modules").principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ModuleCreateRequest("Module 1", "desc"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Module 1"));
    }

    @Test
    void updateModule_returns200() throws Exception {
        when(moduleService.updateModule(anyString(), anyString(), any(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(put("/api/courses/c1/modules/m1").principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ModuleCreateRequest("Updated", "desc"))))
                .andExpect(status().isOk());
    }

    @Test
    void deleteModule_returns200() throws Exception {
        doNothing().when(moduleService).deleteModule(anyString(), anyString(), anyString());

        mockMvc.perform(delete("/api/courses/c1/modules/m1").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Module deleted successfully"));
    }

    @Test
    void getCourseModules_returnsList() throws Exception {
        when(moduleService.getCourseModules(anyString(), anyString())).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/courses/c1/modules").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Module 1"));
    }

    @Test
    void getModuleById_returns200() throws Exception {
        when(moduleService.getModuleById(anyString(), anyString(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(get("/api/courses/c1/modules/m1").principal(auth))
                .andExpect(status().isOk());
    }
}
