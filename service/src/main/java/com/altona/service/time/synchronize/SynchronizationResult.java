package com.altona.service.time.synchronize;

import com.altona.service.time.summary.Summary;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SynchronizationResult {

    public static SynchronizationResult success(SynchronizationService service, Summary summary) {
        return new SynchronizationResult(true, service.getSynchronizationId(), summary, null);
    }

    public static SynchronizationResult failure(SynchronizationService service, String message) {
        return new SynchronizationResult(false, service.getSynchronizationId(), null, message);
    }

    public static SynchronizationResult failure(SynchronizationError error) {
        return new SynchronizationResult(false, error.getSynchronizationId(), null, error.getDetail());
    }

    private boolean success;
    private int synchronizationId;
    private Summary summary;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public Optional<Summary> getSummary() {
        return Optional.ofNullable(summary);
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }
}
