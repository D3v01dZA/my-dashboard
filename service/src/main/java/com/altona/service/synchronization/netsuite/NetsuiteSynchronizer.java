package com.altona.service.synchronization.netsuite;

import com.altona.service.synchronization.Screenshot;
import com.altona.service.synchronization.SynchronizeRequest;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.synchronization.model.SynchronizeResult;
import com.altona.service.synchronization.netsuite.model.NetsuiteConfiguration;
import com.altona.service.synchronization.netsuite.model.NetsuiteContext;
import com.altona.service.synchronization.netsuite.model.NetsuiteTimeData;
import com.altona.service.synchronization.netsuite.model.NetsuiteTimeDataList;
import com.altona.service.time.TimeService;
import com.altona.service.time.model.summary.NotStoppedAction;
import com.altona.service.time.model.summary.SummaryConfiguration;
import com.altona.service.time.model.summary.SummaryFailure;
import com.altona.service.time.model.summary.SummaryTime;
import com.altona.service.time.model.summary.TimeRounding;
import com.altona.service.time.model.summary.TimeSummary;
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
    private final int synchronizationId;

    @NonNull
    private final SynchronizeRequest request;

    @NonNull
    private final NetsuiteConfiguration netsuiteConfiguration;

    @Override
    public int getSynchronizationId() {
        return synchronizationId;
    }

    @Override
    public SynchronizeResult synchronize() {
        log.info("Synchronization {}: Synchronizing Netsuite {} periods back", synchronizationId, request.getPeriodsBack());
        return netsuiteBrowser.login(request, netsuiteConfiguration)
                .successf(this::synchronizeTime)
                .map(
                        screenshot -> SynchronizeResult.success(
                                this,
                                request,
                                screenshot
                        ),
                        error -> SynchronizeResult.failure(this, request, error)
                );
    }

    private Result<Screenshot, String> synchronizeTime(NetsuiteContext netsuiteContext) {
        try {
            log.info("Retrieving time data for requested period");
            netsuiteBrowser.weeklyTimesheets(netsuiteContext, request);
            for (int i = 0; i < request.getPeriodsBack(); i++) {
                netsuiteBrowser.previousWeeklyTimesheet(netsuiteContext, request);
            }
            NetsuiteTimeDataList data = netsuiteBrowser.weeklyData(netsuiteContext, request);

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
                    .successf(summary -> createLine(netsuiteContext, data, summary));
        } finally {
            netsuiteBrowser.close(netsuiteContext, request);
        }
    }

    private Result<Screenshot, String> createLine(NetsuiteContext netsuiteContext, NetsuiteTimeDataList data, TimeSummary timeSummary) {
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
                                netsuiteBrowser.addLine(netsuiteContext, request, difference.getFromDate(), difference.getToDate(), line);
                            } else {
                                log.info("No Time Difference Found");
                            }
                            return Result.success(Screenshot.take(netsuiteContext));
                        },
                        summaryFailure -> Result.failure(summaryFailure.getMessage())
                );
    }

}
