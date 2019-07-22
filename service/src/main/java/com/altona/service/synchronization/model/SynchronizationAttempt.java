package com.altona.service.synchronization.model;

import com.altona.service.synchronization.Screenshot;
import com.altona.service.synchronization.SynchronizationException;
import com.altona.service.synchronization.Synchronizer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public class SynchronizationAttempt {

    public static SynchronizationAttempt pending(Synchronizer service) {
        return new SynchronizationAttempt(-1, SynchronizationStatus.PENDING, null, null, service.getSynchronization().getId());
    }

    public static SynchronizationAttempt failure(SynchronizationError error) {
        return new SynchronizationAttempt(-1, SynchronizationStatus.FAILURE, error.getDetail(), null, error.getSynchronization().getId());
    }

    private int id;
    private SynchronizationStatus status;
    private String message;
    private Screenshot screenshot;
    private int synchronizationId;

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    public Optional<Screenshot> getScreenshot() {
        return Optional.ofNullable(screenshot);
    }

    public SynchronizationAttempt succeeded(Screenshot screenshot) {
        return new SynchronizationAttempt(id, SynchronizationStatus.SUCCESS, null, screenshot, synchronizationId);
    }

    public SynchronizationAttempt failed(SynchronizationException ex) {
        return new SynchronizationAttempt(
                id,
                SynchronizationStatus.FAILURE,
                message,
                ex.getScreenshot().orElse(null),
                synchronizationId
        );
    }

    public SynchronizationAttempt failed(Exception ex) {
        return new SynchronizationAttempt(
                id,
                SynchronizationStatus.FAILURE,
                message,
                null,
                synchronizationId
        );
    }

}
