package com.altona.dashboard;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import okhttp3.OkHttpClient;

public abstract class Static {

    public static final ObjectMapper OBJECT_MAPPER;
    public static final OkHttpClient HTTP_CLIENT;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        HTTP_CLIENT = new OkHttpClient();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer());
        OBJECT_MAPPER.registerModule(simpleModule);
    }

    private Static() {
        throw new IllegalStateException("You really don't want to instantiate this class");
    }

    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {

        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            try {
                return LocalTime.parse(p.getValueAsString());
            } catch (DateTimeParseException e) {
                throw new JsonParseException(p, p.getValueAsString() + " is not formatted correctly", e);
            }
        }
    }

}
