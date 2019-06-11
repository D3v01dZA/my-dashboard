package com.altona.service.synchronization.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class SynchronizationTrace {

    private int id;

    private int projectId;

    private int synchronizationId;

    @NonNull
    private String atttemptId;

    @NonNull
    private String stage;

    @NonNull
    private JsonNode value;

}
