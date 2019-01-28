package com.altona.service.time.synchronize.maconomy;

import com.altona.repository.db.time.project.Project;
import com.altona.repository.integration.maconomy.MaconomyConfiguration;
import com.altona.repository.integration.maconomy.MaconomyRepository;
import com.altona.repository.integration.maconomy.TimeData;
import com.altona.repository.integration.maconomy.get.*;
import com.altona.security.UserContext;
import com.altona.service.time.TimeService;
import com.altona.service.time.summary.Summary;
import com.altona.service.time.summary.SummaryConfiguration;
import com.altona.service.time.summary.TimeRounding;
import com.altona.service.time.synchronize.SynchronizationCommand;
import com.altona.service.time.synchronize.SynchronizationResult;
import com.altona.service.time.synchronize.SynchronizationService;
import com.altona.util.LocalDateIterator;
import com.altona.util.functional.Result;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;

import static java.util.function.Function.identity;

// This is not an @Service because it is constructed based on the user
@AllArgsConstructor
public class MaconomyService implements SynchronizationService {

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
    public SynchronizationResult synchronize(UserContext userContext, Project project, SynchronizationCommand command) {
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

    private Result<SynchronizationResult, String> saveTimeDataRelative(CardData cardData, UserContext userContext, Project project, int periodsBack) {
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

    private Result<SynchronizationResult, String> saveTimeData(Get currentTime, UserContext userContext, Project project) {
        TableRecord tableRecord = currentTime.getTableRecord();
        CardRecord cardRecord = currentTime.getCardRecord();
        CardData cardData = cardRecord.getData();
        TimeData timeData = tableRecord.getData();
        TableMeta tableMeta = tableRecord.getMeta();

        SummaryConfiguration configuration = new SummaryConfiguration(cardData.getPeriodstartvar(), cardData.getPeriodendvar(), TimeRounding.NEAREST_FIFTEEN);
        Summary summary = timeService.summary(userContext, project, configuration);
        rewriteTimes(timeData, summary);

        return maconomyRepository.writeTimeData(userContext, userContext.getId(), project.getId(), maconomyConfiguration, cardData, tableMeta, timeData)
                .mapSuccess(
                        savedTime -> SynchronizationResult.success(this, summary)
                );
    }

    private SynchronizationResult error(String error) {
        return SynchronizationResult.failure(this, error);
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
