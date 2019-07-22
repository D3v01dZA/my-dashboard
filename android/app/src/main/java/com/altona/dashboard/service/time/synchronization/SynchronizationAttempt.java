package com.altona.dashboard.service.time.synchronization;

import android.content.Context;

import com.altona.dashboard.service.Settings;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.function.Consumer;

import lombok.Getter;

public class SynchronizationAttempt {

    private int id;

    private SynchronizationStatus status;

    @Getter
    private String message;

    private Screenshot screenshot;

    private int synchronizationId;

    @JsonCreator
    public SynchronizationAttempt(
            @JsonProperty(value = "id", required = true) int id,
            @JsonProperty(value = "status", required = true) SynchronizationStatus status,
            @JsonProperty(value = "message") String message,
            @JsonProperty(value = "screenshot") Screenshot screenshot,
            @JsonProperty(value = "synchronizationId", required = true) int synchronizationId
    ) {
        this.id = id;
        this.status = status;
        this.message = message;
        this.screenshot = screenshot;
        this.synchronizationId = synchronizationId;
    }

    public boolean isPending() {
        return status.equals(SynchronizationStatus.PENDING);
    }

    public void saveIfEnabled(Context context, Consumer<String> savedListener, Runnable notSavedListener) {
        if (screenshot != null && new Settings(context).isSaveImages()) {
            screenshot.save(context, id);
            savedListener.accept(screenshot.file(id));
        } else {

        }
    }
}