package com.altona.html;

import com.altona.SpringTest;
import com.altona.service.project.model.Project;
import com.altona.service.time.util.TimeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IntegrationTests extends SpringTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TimeInfo timeInfo;

    @Test
    void basicSequence() throws Exception {
        String root = mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        assertEquals("Root Controller test!", root);

        Project project = read(
                mvc.perform(post("/time/project", new Project(11, "Test Project")))
                        .andExpect(status().isCreated())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                Project.class
        );

        assertEquals("Test Project", project.getName());

        List<Project> projects = read(mvc.perform(get("/time/project"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                new TypeReference<List<Project>>() {}
        );

    }

}