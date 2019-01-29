package com.altona.service.time.synchronize.maconomy;

import com.altona.repository.db.time.project.Project;
import com.altona.repository.integration.maconomy.MaconomyConfiguration;
import com.altona.repository.integration.maconomy.MaconomyRepository;
import com.altona.repository.integration.maconomy.TimeData;
import com.altona.repository.integration.maconomy.get.*;
import com.altona.security.UserContext;
import com.altona.service.time.TimeService;
import com.altona.service.time.summary.*;
import com.altona.service.time.synchronize.SynchronizeCommand;
import com.altona.service.time.synchronize.SynchronizeResult;
import com.altona.service.time.synchronize.Synchronizer;
import com.altona.util.LocalDateIterator;
import com.altona.util.functional.Result;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

import static java.util.function.Function.identity;

// This is not an @Service because it is constructed based on the user
@AllArgsConstructor
public class MaconomySynchronizer implements Synchronizer {

    // Application Beans
    private TimeService timeService;
    private MaconomyRepository maconomyRepository;

    // Configuration
    private int synchronizationServiceId;
    private MaconomyConfiguration maconomyConfiguration;

    @Override
    public int getSynchronizationId() {
        return synchronizationServiceId;
    }

    @Override
    public SynchronizeResult synchronize(UserContext userContext, Project project, SynchronizeCommand command) {
        int periodsBack = command.getPeriodsBack();
        // Logic isn't clean for the 0 case because of where Card information is - might do an API review to remove this special case?
        if (periodsBack == 0) {
            return maconomyRepository.timeData(userContext, userContext.getId(), project.getId(), maconomyConfiguration)
                    .flatMapSuccess(currentTime -> saveTimeData(currentTime, userContext, project))
                    .map(identity(), this::error);
        }
        return maconomyRepository.timeData(userContext, userContext.getId(), project.getId(), maconomyConfiguration)
                .flatMapSuccess(currentTime -> saveTimeDataRelative(currentTime.getCardData(), userContext, project, periodsBack - 1))
                .map(identity(), this::error);
    }

    private Result<SynchronizeResult, String> saveTimeDataRelative(CardData cardData, UserContext userContext, Project project, int periodsBack) {
        LocalDate periodStart = cardData.getPeriodstartvar();
        String employee = cardData.getEmployeenumbervar();
        return maconomyRepository.timeData(userContext, userContext.getId(), project.getId(), maconomyConfiguration, periodStart.minusDays(1), employee)
                .flatMapSuccess(currentTime -> {
                    if (periodsBack == 0) {
                        return saveTimeData(currentTime, userContext, project);
                    } else {
                        // IntelliJ doesn't recognize it but this is recursion because we need to resolve when the next period back is each time!
                        return saveTimeDataRelative(currentTime.getCardData(), userContext, project, periodsBack - 1);
                    }
                });
    }

    private Result<SynchronizeResult, String> saveTimeData(Get currentTime, UserContext userContext, Project project) {
        CardData cardData = currentTime.getCardRecord().getData();
        SummaryConfiguration configuration = new SummaryConfiguration(
                userContext.localize(new Date()),
                cardData.getPeriodstartvar(),
                cardData.getPeriodendvar(),
                TimeRounding.NEAREST_FIFTEEN,
                NotStoppedAction.FAIL
        );

        return timeService.summary(userContext, project, configuration)
                .mapError(SummaryFailure::getMessage)
                .flatMapSuccess(summary -> saveTimeDataWithSummary(currentTime, summary, cardData, userContext, project));
    }

    private Result<SynchronizeResult, String> saveTimeDataWithSummary(Get currentTime, Summary summary, CardData cardData, UserContext userContext, Project project) {
        TableRecord tableRecord = currentTime.getTableRecord();
        TimeData timeData = tableRecord.getData();
        rewriteTimes(timeData, summary);
        return maconomyRepository.writeTimeData(userContext, userContext.getId(), project.getId(), maconomyConfiguration, cardData, tableRecord.getMeta(), timeData)
                .mapSuccess(savedTimeData -> SynchronizeResult.success(this, summary));
    }

    private SynchronizeResult error(String error) {
        return SynchronizeResult.failure(this, error);
    }

    private static void rewriteTimes(TimeData timeData, Summary summary) {
        int current = 0;
        LocalDate fromDate = summary.getFromDate();
        LocalDate toDate = summary.getToDate();
        for (LocalDate date : LocalDateIterator.inclusive(fromDate, toDate)) {
            if (current == 0) {
                summary.getActualTime(date)
                        .ifPresent(time -> timeData.setNumberday1(asDecimal(time)));
            } else if (current == 1) {
                summary.getActualTime(date)
                        .ifPresent(time -> timeData.setNumberday2(asDecimal(time)));
            } else if (current == 2) {
                summary.getActualTime(date)
                        .ifPresent(time -> timeData.setNumberday3(asDecimal(time)));
            } else if (current == 3) {
                summary.getActualTime(date)
                        .ifPresent(time -> timeData.setNumberday4(asDecimal(time)));
            } else if (current == 4) {
                summary.getActualTime(date)
                        .ifPresent(time -> timeData.setNumberday5(asDecimal(time)));
            } else if (current == 5) {
                summary.getActualTime(date)
                        .ifPresent(time -> timeData.setNumberday6(asDecimal(time)));
            } else if (current == 6) {
                summary.getActualTime(date)
                        .ifPresent(time -> timeData.setNumberday7(asDecimal(time)));
            } else {
                throw new IllegalStateException("Summary came with more than 7 dates between " + fromDate + " and " + toDate);
            }
            current++;
        }
    }

    private static BigDecimal asDecimal(LocalTime time) {
        // On the site 1:40 becomes 1.6666666666666665 which I have no idea how to replicate so imma ignore it
        String minutes = new BigDecimal(time.getMinute()).divide(new BigDecimal(60), 16, RoundingMode.FLOOR).toPlainString();
        String minutesWithoutZero = minutes.substring(2);
        return new BigDecimal(time.getHour() + "." + minutesWithoutZero);
    }

}
