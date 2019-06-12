package com.altona;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackageClasses = Main.class)
public class SpringTest {

    @Autowired
    private ObjectMapper objectMapper;

    public SpringTest() {
        System.setProperty("webdriver.chrome.driver", "NONE");
        System.setProperty("webdriver.chrome.headless", "NONE");
        System.setProperty("webdriver.chrome.silent", "NONE");
        System.setProperty("webdriver.chrome.linux", "NONE");
    }

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

}
