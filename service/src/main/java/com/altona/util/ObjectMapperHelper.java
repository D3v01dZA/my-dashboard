package com.altona.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public interface ObjectMapperHelper {

    static String serialize(ObjectMapper objectMapper, Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Programming error serializing object of type " + object.getClass().getName(), e);
        }
    }

    static <T> T deserialize(ObjectMapper objectMapper, String value, Class<T> clazz) {
        try {
            return objectMapper.readValue(value, clazz);
        } catch (IOException e) {
            throw new IllegalStateException("Programming error deserializing object of type " + clazz.getName(), e);
        }
    }

}
