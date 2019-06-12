package com.altona.service.synchronization.model;

import com.altona.service.synchronization.Screenshot;
import com.altona.service.synchronization.SynchronizeRequest;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.time.model.summary.TimeSummary;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SynchronizeResult {

    public static SynchronizeResult success(Synchronizer service, SynchronizeRequest request, TimeSummary timeSummary, Screenshot screenshot) {
        return new SynchronizeResult(true, service.getSynchronizationId(), request.getAttemptId(), timeSummary, null, screenshot);
    }

    public static SynchronizeResult failure(Synchronizer service, SynchronizeRequest request, String message) {
        return new SynchronizeResult(false, service.getSynchronizationId(), request.getAttemptId(), null, message, null);
    }

    public static SynchronizeResult failure(SynchronizeError error) {
        return new SynchronizeResult(false, error.getSynchronizationId(), null, null, error.getDetail(), null);
    }

    private boolean success;
    private int synchronizerId;
    private String attemptId;
    private TimeSummary timeSummary;
    private String message;
    private Screenshot screenshot;

    public Optional<String> getAttemptId() {
        return Optional.ofNullable(attemptId);
    }

    public Optional<TimeSummary> getTimeSummary() {
        return Optional.ofNullable(timeSummary);
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    public Optional<Screenshot> getScreenshot() {
        return Optional.of(screenshot);
    }
}
