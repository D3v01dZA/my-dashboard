package com.altona.dashboard.service.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

import lombok.Getter;

public class SynchronizationResult {

    @Getter
    private boolean success;

    @Getter
    private int synchronizerId;

    private String attemptId;

    private String message;

    private TimeSummary summary;

    @JsonCreator
    public SynchronizationResult(
            @JsonProperty(value = "success", required = true) boolean success,
            @JsonProperty(value = "synchronizerId", required = true) int synchronizerId,
            @JsonProperty(value = "attemptId") String attemptId,
            @JsonProperty(value = "message") String message,
            @JsonProperty(value = "summary") TimeSummary summary
    ) {
        this.success = success;
        this.synchronizerId = synchronizerId;
        this.attemptId = attemptId;
        this.message = message;
        this.summary = summary;
    }

    public Optional<String> getAttemptId() {
        return Optional.ofNullable(attemptId);
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    public Optional<TimeSummary> getSummary() {
        return Optional.ofNullable(summary);
    }
}
