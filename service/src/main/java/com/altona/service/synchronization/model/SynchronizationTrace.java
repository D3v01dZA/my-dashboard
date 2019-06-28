package com.altona.service.synchronization.model;

import com.altona.service.synchronization.Screenshot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class SynchronizationTrace {

    private int id;

    private int synchronizationAttemptId;

    @NonNull
    private String stage;

    @NonNull
    private Screenshot screenshot;

}
