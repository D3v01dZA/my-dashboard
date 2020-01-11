package com.altona.service.synchronization.model;

import com.altona.project.Project;
import com.altona.service.synchronization.Synchronizer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SynchronizationAttemptBroadcast {
    
    public static SynchronizationAttemptBroadcast of(SynchronizationAttempt attempt, Synchronizer synchronizer, Project project) {
        return new SynchronizationAttemptBroadcast(attempt.getId(), synchronizer.getSynchronization().getId(), project.id(), attempt.getStatus());
    }

    private int id;
    private int synchronizationId;
    private int projectId;
    private SynchronizationStatus status;
    
}
