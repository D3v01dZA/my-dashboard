package com.altona.dashboard.service.time;

import com.altona.dashboard.GsonHolder;
import com.altona.dashboard.service.login.LoginService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import okhttp3.Request;
import okhttp3.RequestBody;

public class TimeService {

    private static final Logger LOGGER = Logger.getLogger(TimeService.class.getName());
    private static final Type PROJECT_LIST = new TypeToken<List<Project>>() {}.getType();

    private LoginService loginService;

    public TimeService(LoginService loginService) {
        this.loginService = loginService;
    }

    public void getProjects(Consumer<List<Project>> onSuccess, Consumer<String> onFailure) {
        request(get(), "/time/project", PROJECT_LIST, onSuccess, onFailure);
    }

    public void startWork(Project project, Consumer<JsonObject> onSuccess, Consumer<String> onFailure) {
        request(emptyPost(), projectUrl(project) + "/start-work", JsonObject.class, onSuccess, onFailure);
    }

    public void stopWork(Project project, Consumer<JsonObject> onSuccess, Consumer<String> onFailure) {
        request(emptyPost(), projectUrl(project) + "/stop-work", JsonObject.class, onSuccess, onFailure);
    }

    public void startBreak(Project project, Consumer<JsonObject> onSuccess, Consumer<String> onFailure) {
        request(emptyPost(), projectUrl(project) + "/start-break", JsonObject.class, onSuccess, onFailure);
    }

    public void stopBreak(Project project, Consumer<JsonObject> onSuccess, Consumer<String> onFailure) {
        request(emptyPost(), projectUrl(project) + "/stop-break", JsonObject.class, onSuccess, onFailure);
    }

    public void timeStatus(Project project, Consumer<TimeStatus> onSuccess, Consumer<String> onFailure) {
        request(emptyPost(), projectUrl(project) + "/time-status", TimeStatus.class, onSuccess, onFailure);
    }

    private static Request.Builder emptyPost() {
        return new Request.Builder().post(RequestBody.create(null, ""));
    }

    private static Request.Builder get() {
        return new Request.Builder().get();
    }

    private static String projectUrl(Project project) {
        return "/time/project/" + project.getId();
    }

    private <T> void request(Request.Builder builder, String subUrl, Class<T> clazz, Consumer<T> onSuccess, Consumer<String> onFailure) {
        request(builder, subUrl, (Type) clazz, onSuccess, onFailure);
    }

    // This T is super hacky, but is very similar in intent to findViewById(id)
    private <T> void request(Request.Builder builder, String subUrl, Type type, Consumer<T> onSuccess, Consumer<String> onFailure) {
        loginService.tryExecute(builder, subUrl, serviceResponse -> {
            int code = serviceResponse.getCode();
            if (code >= 500) {
                LOGGER.warning(() -> "Failed to call " + subUrl + " with " + code);
                onFailure.accept("Server " + code);
            } else if (code >= 400) {
                LOGGER.warning(() -> "Failed to call " + subUrl + " with " + code);
                onFailure.accept("Request " + code);
            } else {
                try {
                    T deserialized = GsonHolder.INSTANCE.fromJson(serviceResponse.getValue(), type);
                    onSuccess.accept(deserialized);
                } catch (JsonParseException ex) {
                    onFailure.accept(ex.getMessage());
                }
            }
        }, onFailure);
    }

}
