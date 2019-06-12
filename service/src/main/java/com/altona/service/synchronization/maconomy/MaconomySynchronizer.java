package com.altona.service.synchronization.maconomy;

import com.altona.service.synchronization.Screenshot;
import com.altona.service.synchronization.SynchronizeRequest;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.synchronization.maconomy.model.MaconomyConfiguration;
import com.altona.service.synchronization.maconomy.model.MaconomyContext;
import com.altona.service.synchronization.maconomy.model.MaconomyTimeData;
import com.altona.service.synchronization.maconomy.model.MaconomyTimeDataList;
import com.altona.service.synchronization.model.SynchronizeResult;
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
public class MaconomySynchronizer implements Synchronizer {

    // Application Beans
    @NonNull
    private TimeService timeService;

    @NonNull
    private MaconomyBrowser maconomyBrowser;

    // Configuration
    private int synchronizationId;

    @NonNull
    private SynchronizeRequest request;

    @NonNull
    private MaconomyConfiguration configuration;

    @Override
    public int getSynchronizationId() {
        return synchronizationId;
    }

    @Override
    public SynchronizeResult synchronize() {
        return maconomyBrowser.login(request, configuration)
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

    private Result<Screenshot, String> synchronizeTime(MaconomyContext context) {
        try {
            log.info("Retrieving time data for requested period");
            for (int i = 0; i < request.getPeriodsBack(); i++) {
                maconomyBrowser.previousWeeklyTimesheet(context, request);
            }

            return maconomyBrowser.weeklyData(context, request)
                    .successf(timeData -> synchronizeTime(context, timeData));
        } finally {
            maconomyBrowser.close(context, request);
        }
    }

    private Result<Screenshot, String> synchronizeTime(MaconomyContext context, MaconomyTimeDataList currentData) {
        SummaryConfiguration configuration = new SummaryConfiguration(
                request.localizedNow(),
                currentData.getWeekStart(),
                currentData.getWeekEnd(),
                TimeRounding.NEAREST_FIFTEEN,
                NotStoppedAction.FAIL,
                false
        );
        return timeService.summary(request, request.getProject(), configuration)
                .failure(SummaryFailure::getMessage)
                .successf(timeSummary -> createLine(context, currentData, timeSummary));
    }

    private Result<Screenshot, String> createLine(
            MaconomyContext context,
            MaconomyTimeDataList currentData,
            TimeSummary timeSummary
    ) {
        log.info("Calculating difference to add line");
        return timeSummary.getDifference(currentData.getAllData(configuration.getProjectName(), configuration.getTaskName()))
                .failure(SummaryFailure::getMessage)
                .successf(difference -> {
                    MaconomyTimeData timeData = new MaconomyTimeData(
                            configuration.getProjectName(),
                            configuration.getTaskName(),
                            difference.getTimes().stream()
                                    .collect(Collectors.toMap(
                                            SummaryTime::getDate,
                                            SummaryTime::getTime
                                    ))
                    );
                    if (difference.hasTime()) {
                        log.info("Time difference found");
                        return maconomyBrowser.addLine(context, request, difference.getFromDate(), difference.getToDate(), timeData)
                                .<Result<Screenshot, String>>map(Result::failure)
                                .orElseGet(() -> Result.success(Screenshot.take(context)));
                    } else {
                        log.info("No time difference found");
                    }
                    return Result.success(Screenshot.take(context));
                });
    }

}