package com.altona.service.synchronization.test;

import com.altona.service.synchronization.SynchronizationTraceRepository;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationRequest;
import com.altona.service.synchronization.test.model.SucceedingConfiguration;
import com.altona.service.synchronization.test.model.SucceedingContext;
import com.altona.util.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@AllArgsConstructor
public class SucceedingBrowser {

    private SynchronizationTraceRepository synchronizationTraceRepository;

    public Result<SucceedingContext, String> login(SynchronizationAttempt attempt, SynchronizationRequest request, SucceedingConfiguration configuration) {
        SucceedingContext succeedingContext = null;
        try {
            succeedingContext = new SucceedingContext();
            succeedingContext.get(configuration.getWebsite());
            synchronizationTraceRepository.trace(attempt, request, "Load", succeedingContext);
            return Result.success(succeedingContext);
        } catch (Exception ex) {
            log.error("Exception opening " + configuration.getWebsite(), ex);
            if (succeedingContext != null) {
                succeedingContext.quit();
            }
            return Result.failure("Exception occurred while logging in");
        }
    }
}
