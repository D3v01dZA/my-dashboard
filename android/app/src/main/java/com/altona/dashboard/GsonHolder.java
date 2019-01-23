package com.altona.dashboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public abstract class GsonHolder {

    public static final Gson INSTANCE = new GsonBuilder()
            .registerTypeAdapter(LocalTime.class, new LocalTimeDeserializer())
            .create();

    private GsonHolder() {
        throw new IllegalStateException("You really don't want to instantiate this class");
    }

    private static class LocalTimeDeserializer implements JsonDeserializer<LocalTime> {

        @Override
        public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonPrimitive()) {
                throw new JsonParseException("Expected primitive");
            }
            JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
            if (!jsonPrimitive.isString()) {
                throw new JsonParseException("Expected primitve to be a string");
            }
            String jsonString = jsonPrimitive.getAsString();
            try {
                return LocalTime.parse(jsonString);
            } catch (DateTimeParseException e) {
                throw new JsonParseException(jsonString + " is not formatted correctly", e);
            }
        }

    }

}
