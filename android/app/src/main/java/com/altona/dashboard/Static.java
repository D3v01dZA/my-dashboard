package com.altona.dashboard;

import android.util.SparseArray;

import com.altona.dashboard.view.BaseActivity;
import com.altona.dashboard.view.configuration.ConfigurationActivity;
import com.altona.dashboard.view.main.MainActivity;
import com.altona.dashboard.view.time.TimeActivity;
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
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

public abstract class Static {

    public static final ObjectMapper OBJECT_MAPPER;
    public static final OkHttpClient HTTP_CLIENT;
    public static final SparseArray<Class<? extends BaseActivity>> NAV_ID_TO_CLASS;
    public static final Map<Class<? extends BaseActivity>, Integer> CLASS_TO_NAV_ID;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer());
        OBJECT_MAPPER.registerModule(simpleModule);

        HTTP_CLIENT = new OkHttpClient();

        NAV_ID_TO_CLASS = new SparseArray<>(3);
        NAV_ID_TO_CLASS.append(R.id.nav_home, MainActivity.class);
        NAV_ID_TO_CLASS.append(R.id.nav_settings, ConfigurationActivity.class);
        NAV_ID_TO_CLASS.append(R.id.nav_time, TimeActivity.class);

        CLASS_TO_NAV_ID = new HashMap<>(3);
        CLASS_TO_NAV_ID.put(MainActivity.class, R.id.nav_home);
        CLASS_TO_NAV_ID.put(ConfigurationActivity.class, R.id.nav_settings);
        CLASS_TO_NAV_ID.put(TimeActivity.class, R.id.nav_time);
    }

    private Static() {
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
