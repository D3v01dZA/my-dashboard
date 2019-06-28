package com.altona.service.synchronization.test;

import com.altona.service.synchronization.Screenshot;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.util.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class FailingSynchronizer implements Synchronizer {

    private Synchronization synchronization;

    @Override
    public Synchronization getSynchronization() {
        return synchronization;
    }

    @Override
    public Result<Screenshot, String> synchronize(SynchronizationAttempt attempt) {
        log.info("Synchronizing");
        try {
            // Just give us some time
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Result.failure("This synchronizer always fails");
    }

}
