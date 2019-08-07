package com.altona;

import com.altona.html.MockBroadcast;
import com.altona.html.MockBroadcastInteractor;
import com.altona.service.time.util.TimeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackageClasses = Main.class)
@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfilesResolver.class)
public abstract class SpringTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockBroadcastInteractor mockFirebaseInteractor;

    @Autowired
    protected NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    protected TimeInfo timeInfo;

    @PostConstruct
    public void postConstruct() throws Exception {
        String username = getTestUsername();
        Map<String, Object> queryMap = jdbcTemplate.queryForMap(
                "SELECT count(id) FROM users WHERE username = :username",
                new MapSqlParameterSource().addValue("username", username));
        Long count = (Long) queryMap.get("count");
        Assertions.assertNotNull(count, "Could not count users with username " + username);
        if (count == 0) {
            Map<String, Object> insertMap = jdbcTemplate.queryForMap(
                    "INSERT INTO users (username, password, salt) VALUES (:username, :password, :salt) RETURNING id",
                    new MapSqlParameterSource()
                            .addValue("username", username)
                            .addValue("password", "$2a$10$22t/X7uEYPQxSS7C9aOBYeaNGy4gYzcIX8X/GbuQZ82i6BG/lnR2a")
                            .addValue("salt", UUID.fromString("715814fc-98db-49ab-af16-69617351d382"))
            );
            Assertions.assertNotNull(insertMap.get("id"), "Could not insert username " + username);
        }
        String root = mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();
        assertEquals(String.format("Root Controller %s!", getTestUsername()), root);
    }

    protected abstract String getTestUsername();

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
        return "Basic " + Base64.getEncoder().encodeToString((getTestUsername() + ":password").getBytes());
    }

    protected static <T> T assertInstanceOf(Object object, Class<T> clazz) {
        assertTrue(clazz.isInstance(object), "Expected " + object.getClass().getName() + " to be " + clazz.getName());
        return clazz.cast(object);
    }

}
