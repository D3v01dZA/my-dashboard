package com.altona.dashboard.service.time.synchronization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class SynchronizationAttemptBroadcast {

    private int id;
    private int synchronizationId;
    private int projectId;
    private SynchronizationStatus status;

    @JsonCreator
    public SynchronizationAttemptBroadcast(
            @JsonProperty(value = "id", required = true) int id,
            @JsonProperty(value = "synchronizationId", required = true) int synchronizationId,
            @JsonProperty(value = "projectId", required = true) int projectId,
            @JsonProperty(value = "status", required = true) SynchronizationStatus status
    ) {
        this.id = id;
        this.status = status;
        this.synchronizationId = synchronizationId;
        this.projectId = projectId;
    }

    public boolean isSuccess() {
        return status.equals(SynchronizationStatus.SUCCESS);
    }

    public boolean isComplete() {
        return !status.equals(SynchronizationStatus.PENDING);
    }

}
