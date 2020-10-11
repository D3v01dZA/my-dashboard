package com.altona.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;

@Configuration
public class ObjectMapperHelper {

    @Bean
    public Module objectMapperModule() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Instant.class, new InstantDeserializer());
        return simpleModule;
    }

    public static String serialize(ObjectMapper objectMapper, Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Programming error serializing object of type " + object.getClass().getName(), e);
        }
    }

    public static <T> T deserialize(ObjectMapper objectMapper, String value, Class<T> clazz) {
        try {
            return objectMapper.readValue(value, clazz);
        } catch (IOException e) {
            throw new IllegalStateException("Programming error deserializing object of type " + clazz.getName(), e);
        }
    }

    private static class InstantDeserializer extends StdDeserializer<Instant> {

        protected InstantDeserializer() {
            super(Instant.class);
        }

        @SneakyThrows
        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String value = p.getValueAsString();
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(value).toInstant();
        }
    }

}
