package com.altona.service.synchronization.model;

import com.altona.service.synchronization.SynchronizeRequest;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.time.model.summary.Summary;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SynchronizeResult {

    public static SynchronizeResult success(Synchronizer service, SynchronizeRequest request, Summary summary) {
        return new SynchronizeResult(true, service.getSynchronizationId(), request.getAttemptId(), summary, null);
    }

    public static SynchronizeResult failure(Synchronizer service, SynchronizeRequest request, String message) {
        return new SynchronizeResult(false, service.getSynchronizationId(), request.getAttemptId(), null, message);
    }

    public static SynchronizeResult failure(SynchronizeError error) {
        return new SynchronizeResult(false, error.getSynchronizationId(), null, null, error.getDetail());
    }

    private boolean success;
    private int synchronizerId;
    private String attemptId;
    private Summary summary;
    private String message;

    public Optional<String> getAttemptId() {
        return Optional.ofNullable(attemptId);
    }

    public Optional<Summary> getSummary() {
        return Optional.ofNullable(summary);
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }
}
