package com.altona.service.synchronization.test;

import com.altona.service.synchronization.SynchronizeRequest;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.synchronization.model.SynchronizeResult;
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
        SummaryConfiguration configuration = new SummaryConfiguration(
                request.localize(request.now()),
                request.firstDayOfWeek(),
                request.today(),
                TimeRounding.NEAREST_FIFTEEN,
                NotStoppedAction.INCLUDE,
                false
        );
        return timeService.summary(request, request.getProject(), configuration)
                .map(
                        summary -> SynchronizeResult.success(this, request, summary),
                        summaryFailure -> SynchronizeResult.failure(this, request, summaryFailure.getMessage())
                );
    }

}
