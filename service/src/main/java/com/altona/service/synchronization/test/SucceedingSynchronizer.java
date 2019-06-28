package com.altona.service.synchronization.test;

import com.altona.service.synchronization.Screenshot;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationRequest;
import com.altona.service.time.TimeService;
import com.altona.service.time.model.summary.NotStoppedAction;
import com.altona.service.time.model.summary.SummaryConfiguration;
import com.altona.service.time.model.summary.TimeRounding;
import com.altona.util.Result;
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

    @Override
    public Synchronization getSynchronization() {
        return synchronization;
    }

    @Override
    public Result<Screenshot, String> synchronize(SynchronizationAttempt attempt) {
        log.info("Synchronizing");
        SummaryConfiguration configuration = new SummaryConfiguration(
                request.localize(request.now()),
                request.firstDayOfWeek(),
                request.today(),
                TimeRounding.NEAREST_FIFTEEN,
                NotStoppedAction.INCLUDE,
                false
        );
        return succeedingBrowser.login(attempt, request)
                .successf(chromeDriver -> timeService.summary(request, request.getProject(), configuration)
                        .map(
                                summary -> {
                                    Result<Screenshot, String> success = Result.success(Screenshot.take(chromeDriver));
                                    chromeDriver.close();
                                    return success;
                                },
                                summaryFailure -> Result.failure(summaryFailure.getMessage())
                        ));
    }

}
