package com.altona.service.synchronization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;

@AllArgsConstructor
public class SynchronizationException extends Exception {

    public static SynchronizationException withScreenshot(Screenshot screenshot, String message) {
        return new SynchronizationException(screenshot, message);
    }

    public static SynchronizationException withoutScreenshot(String message) {
        return new SynchronizationException(null, message);
    }

    private Screenshot screenshot;

    @Getter
    @NonNull
    private String message;

    public Optional<Screenshot> getScreenshot() {
        return Optional.ofNullable(screenshot);
    }

}
