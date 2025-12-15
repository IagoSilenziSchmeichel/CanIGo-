package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GegenstandControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void postMitLeeremNameGibt400() throws Exception {
        mvc.perform(post("/gegenstaende")
                        .contentType("application/json")
                        .content("{\"name\":\"\",\"ort\":\"Flur\",\"status\":\"wichtig\"}"))
                .andExpect(status().isBadRequest());
    }
}