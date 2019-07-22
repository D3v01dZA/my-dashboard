package com.altona.service.synchronization.test;

import com.altona.service.synchronization.SynchronizationException;
import com.altona.service.synchronization.SynchronizationTraceRepository;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationRequest;
import com.altona.service.synchronization.test.model.SucceedingConfiguration;
import com.altona.service.synchronization.test.model.SucceedingContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@AllArgsConstructor
public class SucceedingBrowser {

    private SynchronizationTraceRepository synchronizationTraceRepository;

    public SucceedingContext login(SynchronizationAttempt attempt, SynchronizationRequest request, SucceedingConfiguration configuration) throws SynchronizationException {
        SucceedingContext succeedingContext = null;
        try {
            succeedingContext = new SucceedingContext();
            succeedingContext.get(configuration.getWebsite());
            synchronizationTraceRepository.trace(attempt, request, "Load", succeedingContext);
            return succeedingContext;
        } catch (RuntimeException ex) {
            log.error("Exception opening " + configuration.getWebsite(), ex);
            SynchronizationException exception;
            if (succeedingContext != null) {
                exception = SynchronizationException.withScreenshot(succeedingContext.takeScreenshot(), "Error opening " + configuration.getWebsite());
                succeedingContext.quit();
            } else {
                exception = SynchronizationException.withoutScreenshot("Error opening " + configuration.getWebsite());
            }
            throw exception;
        }
    }
}
