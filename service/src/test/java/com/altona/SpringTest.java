package com.altona;

import com.altona.service.broadcast.MockBroadcast;
import com.altona.service.broadcast.MockFirebaseInteractor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackageClasses = Main.class)
@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfilesResolver.class)
public class SpringTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected MockFirebaseInteractor mockFirebaseInteractor;

    protected MockHttpServletRequestBuilder get(String url) {
        return MockMvcRequestBuilders.get(url)
                .header("Authorization", testAuth());
    }

    protected MockHttpServletRequestBuilder post(String url) throws JsonProcessingException {
        return post(url, null);
    }

    protected MockHttpServletRequestBuilder post(String url, Object content) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(url)
                .content(objectMapper.writeValueAsBytes(content))
                .header("Authorization", testAuth())
                .contentType(APPLICATION_JSON_UTF8);
    }

    protected MockBroadcast getBroadcast() {
        return mockFirebaseInteractor.result()
                .map(
                        mockBroadcast -> mockBroadcast,
                        exception -> {
                            throw exception;
                        }
                );
    }

    protected <T> T read(ResultActions resultActions, Class<T> clazz) throws Exception {
        return read(
                resultActions.andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                clazz
        );
    }

    protected <T> T read(ResultActions resultActions, TypeReference<T> typeReference) throws Exception {
        return read(
                resultActions.andExpect(content().contentType(APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse().getContentAsByteArray(),
                typeReference
        );
    }

    protected <T> T read(byte[] content, Class<T> clazz) throws IOException {
        return objectMapper.readValue(content, clazz);
    }

    protected <T> T read(byte[] content, TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(content, typeReference);
    }

    protected Instant instant(int year, int month, int date, int hour, int minute) {
        return LocalDateTime.of(year, month, date, hour, minute, minute).toInstant(ZoneOffset.UTC);
    }

    protected String testAuth() {
        return "Basic " + Base64.getEncoder().encodeToString("test:password".getBytes());
    }

    protected static <T> T assertInstanceOf(Object object, Class<T> clazz) {
        assertTrue(clazz.isInstance(object), "Expected " + object.getClass().getName() + " to be " + clazz.getName());
        return clazz.cast(object);
    }

}
