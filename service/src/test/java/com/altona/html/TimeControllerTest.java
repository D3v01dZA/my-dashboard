package com.altona.html;

import com.altona.SpringTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TimeControllerTest extends SpringTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void contextLoads() throws Exception {
        mvc.perform(get("/").header("Authorization", testAuth()))
                .andExpect(status().isOk());
    }

}