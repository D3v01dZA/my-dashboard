package com.altona.service.synchronization.openair;

import com.altona.service.synchronization.Screenshot;
import com.altona.service.synchronization.SynchronizationException;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationRequest;
import com.altona.service.synchronization.openair.model.OpenairConfiguration;
import com.altona.service.synchronization.openair.model.OpenairContext;
import com.altona.service.synchronization.openair.model.OpenairTimeDataList;
import com.altona.service.time.TimeService;
import com.altona.service.time.model.summary.NotStoppedAction;
import com.altona.service.time.model.summary.SummaryConfiguration;
import com.altona.service.time.model.summary.TimeRounding;
import com.altona.service.time.model.summary.TimeSummary;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
@AllArgsConstructor
public class OpenairSynchronizer implements Synchronizer {

    // Application Beans
    @NonNull
    private final TimeService timeService;

    @NonNull
    private final OpenairBrowser openairBrowser;

    // Configuration
    @NonNull
    private final Synchronization synchronization;

    @NonNull
    private final SynchronizationRequest request;

    @NonNull
    private final OpenairConfiguration openairConfiguration;

    @Override
    public Synchronization getSynchronization() {
        return synchronization;
    }

    @Override
    public Screenshot synchronize(SynchronizationAttempt attempt) throws SynchronizationException {
        OpenairContext context = openairBrowser.login(attempt, request, openairConfiguration);
        return synchronizeTime(attempt, context);
    }

    private Screenshot synchronizeTime(SynchronizationAttempt attempt, OpenairContext context) throws SynchronizationException {
        try {
            int periodsBack = request.getPeriodsBack();
            LocalDate date = request.today().minusWeeks(periodsBack);
            OpenairTimeDataList timeData = openairBrowser.navigateToTimesheet(request, attempt, context, date);
            TimeSummary currentData = timeData.getAllData(openairConfiguration.getProject(), openairConfiguration.getTask());
            SummaryConfiguration configuration = new SummaryConfiguration(
                    request.localizedNow(),
                    timeData.getWeekStart(),
                    timeData.getWeekEnd(),
                    TimeRounding.NEAREST_FIFTEEN,
                    NotStoppedAction.FAIL,
                    false
            );
            TimeSummary difference = timeService.summary(request, request.getProject(), configuration)
                    .successf(timeSummary -> timeSummary.getDifference(currentData))
                    .orElseThrow(summaryFailure -> SynchronizationException.withScreenshot(context.takeScreenshot(), summaryFailure.getMessage()));
            if (difference.hasTime()) {
                log.info("Time difference found, creating line");
                openairBrowser.createLine(context, difference, openairConfiguration);
            } else {
                log.info("No time difference found, exiting");
            }
            return context.takeScreenshot();
        } finally {
            openairBrowser.close(attempt, context, request);
        }
    }
}
