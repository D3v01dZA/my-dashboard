package com.altona.dashboard.service.time;

import com.altona.dashboard.Static;
import com.altona.dashboard.service.login.LoginService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

public class TimeService {

    public static final DateTimeFormatter LONG_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter();

    public static final DateTimeFormatter SHORT_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .toFormatter();

    private static final Logger LOGGER = Logger.getLogger(TimeService.class.getName());
    private static final TypeReference<List<SynchronizationResult>> SYNCHRONIZATION_RESULT_LIST = new TypeReference<List<SynchronizationResult>>() {};

    private LoginService loginService;

    public TimeService(LoginService loginService) {
        this.loginService = loginService;
    }

    public void createProject(Project project, Consumer<Project> onSuccess, Consumer<String> onFailure) {
        request(post(project), "/time/project", Project.class, onSuccess, onFailure, unauthenticated(onFailure));
    }

    public void getTimeScreen(Consumer<TimeScreen> onSuccess, Consumer<String> onFailure) {
        request(get(), "/interface/time", TimeScreen.class, onSuccess, onFailure, unauthenticated(onFailure));
    }

    public void startWork(Project project, Consumer<ObjectNode> onSuccess, Consumer<String> onFailure) {
        request(emptyPost(), projectUrl(project) + "/start-work", ObjectNode.class, onSuccess, onFailure, unauthenticated(onFailure));
    }

    public void stopWork(Project project, Consumer<ObjectNode> onSuccess, Consumer<String> onFailure) {
        request(emptyPost(), projectUrl(project) + "/stop-work", ObjectNode.class, onSuccess, onFailure, unauthenticated(onFailure));
    }

    public void startBreak(Project project, Consumer<ObjectNode> onSuccess, Consumer<String> onFailure) {
        request(emptyPost(), projectUrl(project) + "/start-break", ObjectNode.class, onSuccess, onFailure, unauthenticated(onFailure));
    }

    public void stopBreak(Project project, Consumer<ObjectNode> onSuccess, Consumer<String> onFailure) {
        request(emptyPost(), projectUrl(project) + "/stop-break", ObjectNode.class, onSuccess, onFailure, unauthenticated(onFailure));
    }

    public void synchronize(Project project, Consumer<List<SynchronizationResult>> onSuccess, Consumer<String> onFailure) {
        request(emptyPost(), projectUrl(project) + "/synchronize", SYNCHRONIZATION_RESULT_LIST, onSuccess, onFailure, unauthenticated(onFailure));
    }

    public void queryStatus(Consumer<TimeStatus> onSuccess, Consumer<String> onFailure, Runnable onUnauthenticated) {
        request(emptyPost(), "/time/project/time-status", TimeStatus.class, onSuccess, onFailure, onUnauthenticated);
    }

    private static Request.Builder emptyPost() {
        return new Request.Builder().post(RequestBody.create(null, ""));
    }

    private static Request.Builder post(Object body) {
        try {
            return new Request.Builder().post(RequestBody.create(MediaType.get("application/json"), Static.OBJECT_MAPPER.writeValueAsBytes(body)));
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize", e);
            throw new IllegalStateException("Failed to serialize", e);
        }
    }

    private static Request.Builder get() {
        return new Request.Builder().get();
    }

    private static String projectUrl(Project project) {
        return "/time/project/" + project.getId();
    }

    private <T> void request(
            Request.Builder builder,
            String subUrl,
            TypeReference<T> typeReference,
            Consumer<T> onSuccess,
            Consumer<String> onFailure,
            Runnable onUnauthenticated
    ) {
        request(builder, subUrl, result -> Static.OBJECT_MAPPER.readValue(result, typeReference), onSuccess, onFailure, onUnauthenticated);
    }

    private <T> void request(
            Request.Builder builder,
            String subUrl,
            Class<T> clazz,
            Consumer<T> onSuccess,
            Consumer<String> onFailure,
            Runnable onUnauthenticated
    ) {
        request(builder, subUrl, result -> Static.OBJECT_MAPPER.readValue(result, clazz), onSuccess, onFailure, onUnauthenticated);
    }

    private <T> void request(
            Request.Builder builder,
            String subUrl,
            ObjectMapperFunction<T> extractor,
            Consumer<T> onSuccess,
            Consumer<String> onFailure,
            Runnable onUnauthenticated
    ) {
        loginService.tryExecute(
                builder,
                subUrl,
                serviceResponse -> extractor.apply(serviceResponse.getValue()),
                onSuccess,
                onFailure,
                onUnauthenticated
        );
    }

    private static Runnable unauthenticated(Consumer<String> onFailure) {
        return () -> onFailure.accept("Not Authenticated");
    }

    private interface ObjectMapperFunction<T> {

        T apply(String value) throws IOException;

    }

}
