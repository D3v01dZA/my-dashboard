package com.altona.service.synchronization.test;

import com.altona.service.synchronization.Screenshot;
import com.altona.service.synchronization.SynchronizationException;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationRequest;
import com.altona.service.synchronization.test.model.SucceedingConfiguration;
import com.altona.service.synchronization.test.model.SucceedingContext;
import com.altona.service.time.TimeService;
import com.altona.service.time.model.summary.NotStoppedAction;
import com.altona.service.time.model.summary.SummaryConfiguration;
import com.altona.service.time.model.summary.TimeRounding;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class SucceedingSynchronizer implements Synchronizer {

    @NonNull
    private TimeService timeService;

    @NonNull
    private SucceedingBrowser succeedingBrowser;

    @NonNull
    private Synchronization synchronization;

    @NonNull
    private SynchronizationRequest request;

    @NonNull
    private SucceedingConfiguration succeedingConfiguration;

    @Override
    public Synchronization getSynchronization() {
        return synchronization;
    }

    @Override
    public Screenshot synchronize(SynchronizationAttempt attempt) throws SynchronizationException {
        log.info("Synchronizing");
        SummaryConfiguration configuration = new SummaryConfiguration(
                request.localize(request.now()),
                request.firstDayOfWeek(),
                request.today(),
                TimeRounding.NEAREST_FIFTEEN,
                NotStoppedAction.INCLUDE,
                false
        );
        SucceedingContext context = succeedingBrowser.login(attempt, request, succeedingConfiguration);
        return timeService.summary(request, request.getProject(), configuration)
                .success(summary -> {
                    Screenshot success = context.takeScreenshot();
                    context.quit();
                    return success;
                }).orElseThrow(summaryFailure -> SynchronizationException.withoutScreenshot(summaryFailure.getMessage()));
    }

}
