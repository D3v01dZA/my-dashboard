package com.altona.service.synchronization.test;

import com.altona.service.synchronization.SynchronizeRequest;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.synchronization.model.SynchronizeResult;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class FailingSynchronizer implements Synchronizer {

    private int synchronizationId;

    @NonNull
    private SynchronizeRequest request;

    @Override
    public int getSynchronizationId() {
        return synchronizationId;
    }

    @Override
    public SynchronizeResult synchronize() {
        log.info("Synchronizing");
        return SynchronizeResult.failure(this, request, "This synchronizer always fails");
    }

}
