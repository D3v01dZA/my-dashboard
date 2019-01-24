package com.altona.dashboard;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public abstract class ObjectMapperHolder {

    public static final ObjectMapper INSTANCE;

    static {
        INSTANCE = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer());
        INSTANCE.registerModule(simpleModule);
    }

    private ObjectMapperHolder() {
        throw new IllegalStateException("You really don't want to instantiate this class");
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
