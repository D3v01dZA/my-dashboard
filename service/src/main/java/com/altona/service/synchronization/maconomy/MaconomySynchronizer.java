package com.altona.service.synchronization.maconomy;

import com.altona.service.synchronization.Screenshot;
import com.altona.service.synchronization.SynchronizationException;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.synchronization.maconomy.model.MaconomyConfiguration;
import com.altona.service.synchronization.maconomy.model.MaconomyContext;
import com.altona.service.synchronization.maconomy.model.MaconomyTimeData;
import com.altona.service.synchronization.maconomy.model.MaconomyTimeDataList;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationRequest;
import com.altona.project.time.query.NotStoppedAction;
import com.altona.project.time.query.TimeRounding;
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
    private Synchronization synchronization;

    @NonNull
    private SynchronizationRequest request;

    @NonNull
    private MaconomyConfiguration configuration;

    @Override
    public Synchronization getSynchronization() {
        return synchronization;
    }

    @Override
    public Screenshot synchronize(SynchronizationAttempt attempt) throws SynchronizationException {
        return maconomyBrowser.login(attempt, request, configuration)
                .successf(maconomyContext -> synchronizeTime(attempt, maconomyContext))
                .orElseThrow(SynchronizationException::withoutScreenshot);
    }

    private Result<Screenshot, String> synchronizeTime(SynchronizationAttempt attempt, MaconomyContext context) {
        try {
            log.info("Retrieving time data for requested period");
            for (int i = 0; i < request.getPeriodsBack(); i++) {
                maconomyBrowser.previousWeeklyTimesheet(attempt, context, request);
            }

            return maconomyBrowser.weeklyData(attempt, context, request)
                    .successf(timeData -> synchronizeTime(attempt, context, timeData));
        } finally {
            maconomyBrowser.close(attempt, context, request);
        }
    }

    private Result<Screenshot, String> synchronizeTime(SynchronizationAttempt attempt, MaconomyContext context, MaconomyTimeDataList currentData) {
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
                .successf(timeSummary -> createLine(attempt, context, currentData, timeSummary));
    }

    private Result<Screenshot, String> createLine(
            SynchronizationAttempt attempt,
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
                        return maconomyBrowser.addLine(attempt, context, request, difference.getFromDate(), difference.getToDate(), timeData)
                                .<Result<Screenshot, String>>map(Result::failure)
                                .orElseGet(() -> Result.success(context.takeScreenshot()));
                    } else {
                        log.info("No time difference found");
                    }
                    return Result.success(context.takeScreenshot());
                });
    }

}