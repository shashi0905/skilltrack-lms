package com.skilltrack.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PublicControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PublicController()).build();
    }

    @Test
    void health_returnsUpStatus() throws Exception {
        mockMvc.perform(get("/api/public/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("SkillTrack API"));
    }

    @Test
    void info_returnsApiInfo() throws Exception {
        mockMvc.perform(get("/api/public/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("SkillTrack LMS"))
                .andExpect(jsonPath("$.version").value("1.0.0-SNAPSHOT"));
    }
}
