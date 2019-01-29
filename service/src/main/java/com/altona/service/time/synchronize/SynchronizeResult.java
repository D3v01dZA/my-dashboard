package com.altona.service.time.synchronize;

import com.altona.service.time.summary.Summary;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SynchronizeResult {

    public static SynchronizeResult success(Synchronizer service, Summary summary) {
        return new SynchronizeResult(true, service.getSynchronizationId(), summary, null);
    }

    public static SynchronizeResult failure(Synchronizer service, String message) {
        return new SynchronizeResult(false, service.getSynchronizationId(), null, message);
    }

    public static SynchronizeResult failure(SynchronizeError error) {
        return new SynchronizeResult(false, error.getSynchronizationId(), null, error.getDetail());
    }

    private boolean success;
    private int synchronizerId;
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
