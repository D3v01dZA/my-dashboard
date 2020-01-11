package com.altona.service.synchronization.netsuite;

import com.altona.service.synchronization.Screenshot;
import com.altona.service.synchronization.SynchronizationException;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationRequest;
import com.altona.service.synchronization.netsuite.model.NetsuiteConfiguration;
import com.altona.service.synchronization.netsuite.model.NetsuiteContext;
import com.altona.service.synchronization.netsuite.model.NetsuiteTimeData;
import com.altona.service.synchronization.netsuite.model.NetsuiteTimeDataList;
import com.altona.project.time.query.NotStoppedAction;
import com.altona.project.time.query.TimeRounding;
import com.altona.util.Result;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class NetsuiteSynchronizer implements Synchronizer {

    // Application Beans
    @NonNull
    private final TimeService timeService;

    @NonNull
    private final NetsuiteBrowser netsuiteBrowser;

    // Configuration
    @NonNull
    private final Synchronization synchronization;

    @NonNull
    private final SynchronizationRequest request;

    @NonNull
    private final NetsuiteConfiguration netsuiteConfiguration;

    @Override
    public Synchronization getSynchronization() {
        return synchronization;
    }

    @Override
    public Screenshot synchronize(SynchronizationAttempt attempt) throws SynchronizationException {
        log.info("Synchronization {}: Synchronizing Netsuite {} periods back", synchronization.getId(), request.getPeriodsBack());
        return netsuiteBrowser.login(attempt, request, netsuiteConfiguration)
                .successf(netsuiteContext -> synchronizeTime(attempt, netsuiteContext))
                .orElseThrow(SynchronizationException::withoutScreenshot);
    }

    private Result<Screenshot, String> synchronizeTime(SynchronizationAttempt attempt, NetsuiteContext netsuiteContext) {
        try {
            log.info("Retrieving time data for requested period");
            netsuiteBrowser.weeklyTimesheets(attempt, netsuiteContext, request);
            for (int i = 0; i < request.getPeriodsBack(); i++) {
                netsuiteBrowser.previousWeeklyTimesheet(attempt, netsuiteContext, request);
            }
            NetsuiteTimeDataList data = netsuiteBrowser.weeklyData(attempt, netsuiteContext, request);

            SummaryConfiguration configuration = new SummaryConfiguration(
                    request.localize(request.now()),
                    data.getWeekStart(),
                    data.getWeekEnd(),
                    TimeRounding.NEAREST_FIFTEEN,
                    NotStoppedAction.FAIL,
                    false
            );
            return timeService.summary(request, request.getProject(), configuration)
                    .failure(SummaryFailure::getMessage)
                    .successf(summary -> createLine(attempt, netsuiteContext, data, summary));
        } finally {
            netsuiteBrowser.close(attempt, netsuiteContext, request);
        }
    }

    private Result<Screenshot, String> createLine(SynchronizationAttempt attempt, NetsuiteContext netsuiteContext, NetsuiteTimeDataList data, TimeSummary timeSummary) {
        TimeSummary current = data.getAllData();
        return timeSummary.getDifference(current)
                .map(
                        difference -> {
                            NetsuiteTimeData line = new NetsuiteTimeData(
                                    netsuiteConfiguration.getProject(),
                                    netsuiteConfiguration.getTask(),
                                    difference.getTimes().stream()
                                            .collect(Collectors.toMap(
                                                    SummaryTime::getDate,
                                                    SummaryTime::getTime
                                            ))
                            );
                            if (difference.hasTime()) {
                                log.info("Time Difference Found");
                                netsuiteBrowser.addLine(attempt, netsuiteContext, request, difference.getFromDate(), difference.getToDate(), line);
                            } else {
                                log.info("No Time Difference Found");
                            }
                            return Result.success(netsuiteContext.takeScreenshot());
                        },
                        summaryFailure -> Result.failure(summaryFailure.getMessage())
                );
    }

}
