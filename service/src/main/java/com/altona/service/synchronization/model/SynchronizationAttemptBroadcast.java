package com.altona.service.synchronization.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SynchronizationAttemptBroadcast {
    
    public static SynchronizationAttemptBroadcast of(SynchronizationAttempt attempt) {
        return new SynchronizationAttemptBroadcast(attempt.getId(), attempt.getStatus());
    }

    private int id;
    private SynchronizationStatus status;
    
}
