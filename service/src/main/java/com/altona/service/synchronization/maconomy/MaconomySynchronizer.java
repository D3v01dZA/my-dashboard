package com.altona.service.synchronization.maconomy;

import com.altona.service.synchronization.SynchronizeRequest;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.synchronization.maconomy.model.MaconomyConfiguration;
import com.altona.service.synchronization.maconomy.model.TimeData;
import com.altona.service.synchronization.maconomy.model.get.*;
import com.altona.service.synchronization.maconomy.model.searchjob.JobData;
import com.altona.service.synchronization.maconomy.model.searchproject.ProjectData;
import com.altona.service.synchronization.model.SynchronizeResult;
import com.altona.service.time.TimeService;
import com.altona.service.time.model.summary.*;
import com.altona.util.LocalDateIterator;
import com.altona.util.Result;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

// This is not an @Service because it is constructed based on the user
@AllArgsConstructor
public class MaconomySynchronizer implements Synchronizer {

    // Application Beans
    private TimeService timeService;
    private MaconomyRepository maconomyRepository;

    // Configuration
    private int synchronizationServiceId;
    private SynchronizeRequest request;
    private MaconomyConfiguration configuration;

    @Override
    public int getSynchronizationId() {
        return synchronizationServiceId;
    }

    @Override
    public SynchronizeResult synchronize() {
        int periodsBack = request.getPeriodsBack();
        // Logic isn't clean for the 0 case because of where Card information is - might do an API review to remove this special case?
        if (periodsBack == 0) {
            return maconomyRepository.timeData(request, configuration)
                    .flatMapSuccess(this::saveTimeData)
                    .map(
                            summary -> SynchronizeResult.success(this, summary),
                            error -> SynchronizeResult.failure(this, error)
                    );
        }
        return maconomyRepository.timeData(request, configuration)
                .flatMapSuccess(currentTime -> saveTimeDataRelative(currentTime.getCardData(), periodsBack - 1))
                .map(
                        summary -> SynchronizeResult.success(this, summary),
                        error -> SynchronizeResult.failure(this, error)
                );
    }

    private Result<Summary, String> saveTimeDataRelative(CardData cardData, int periodsBack) {
        LocalDate periodStart = cardData.getPeriodstartvar();
        String employee = cardData.getEmployeenumbervar();
        return maconomyRepository.timeData(request, configuration, periodStart.minusDays(1), employee)
                .flatMapSuccess(currentTime -> {
                    if (periodsBack == 0) {
                        return saveTimeData(currentTime);
                    } else {
                        // IntelliJ doesn't recognize it but this is recursion because we need to resolve when the next period back is each time!
                        return saveTimeDataRelative(currentTime.getCardData(), periodsBack - 1);
                    }
                });
    }

    private Result<Summary, String> saveTimeData(Get currentTime) {
        CardRecord cardRecord = currentTime.getCardRecord();
        CardData cardData = cardRecord.getData();
        SummaryConfiguration configuration = new SummaryConfiguration(
                request.localize(new Date()),
                cardData.getPeriodstartvar(),
                cardData.getPeriodendvar(),
                TimeRounding.NEAREST_FIFTEEN,
                NotStoppedAction.FAIL
        );

        return timeService.summary(request, request.getProject(), configuration)
                .mapError(SummaryFailure::getMessage)
                .flatMapSuccess(summary -> saveTimeDataWithSummary(currentTime, summary, cardRecord));
    }

    private Result<Summary, String> saveTimeDataWithSummary(Get currentTime, Summary summary, CardRecord cardRecord) {
        CardData cardData = cardRecord.getData();
        List<TableRecord> tableRecords = currentTime.getTableRecords();
        if (tableRecords.isEmpty()) {
            return maconomyRepository.initTimeData(request, configuration, cardData.getDatevar(), cardData.getEmployeenumbervar(), cardRecord.getMeta().getConcurrencyControl())
                    .flatMapSuccess(init -> setProjectAndJobAndWriteTimeData(init.getData(), summary, cardRecord));
        } else if (tableRecords.size() == 1) {
            TableRecord tableRecord = tableRecords.get(0);
            TimeData timeData = tableRecord.getData();
            rewriteTimes(timeData, summary);
            TableMeta meta = tableRecord.getMeta();
            return maconomyRepository.writeTimeData(
                    request,
                    configuration,
                    cardData.getDatevar(),
                    cardData.getEmployeenumbervar(),
                    meta.getConcurrencyControl(),
                    meta.getRowNumber(),
                    timeData
            ).flatMapSuccess(get -> checkTimeData(get, summary));
        } else {
            return Result.error("Expected only one meta record");
        }
    }

    private Result<Summary, String> setProjectAndJobAndWriteTimeData(TimeData timeData, Summary summary, CardRecord cardRecord) {
        return maconomyRepository.searchProjectData(request, configuration, timeData)
                .flatMapSuccess(project -> project.getProjectData(configuration.getProjectId())
                        .map(projectData -> {
                            rewriteProject(timeData, projectData);
                            return maconomyRepository.searchJobData(request, configuration, timeData)
                                    .flatMapSuccess(job -> job.getJobData(configuration.getProjectTaskId())
                                            .map(jobData -> {
                                                rewriteJob(timeData, jobData);
                                                rewriteTimes(timeData, summary);
                                                CardData cardData = cardRecord.getData();
                                                return maconomyRepository.writeNewTimeData(
                                                        request,
                                                        configuration,
                                                        cardData.getDatevar(),
                                                        cardData.getEmployeenumbervar(),
                                                        cardRecord.getMeta().getConcurrencyControl(),
                                                        timeData
                                                ).flatMapSuccess(get -> checkTimeData(get, summary));
                                            })
                                            .orElseGet(() -> Result.error("Job Not Found With Id/Name " + configuration.getProjectTaskId()))
                                    );
                        })
                        .orElseGet(() -> Result.error("Project Not Found With Id" + configuration.getProjectId()))
                );
    }

    private static Result<Summary, String> checkTimeData(Get get, Summary summary) {
        return Result.error("Time Data Was Saved But Not Verified");
    }

    private static void rewriteProject(TimeData timeData, ProjectData projectData) {
        timeData.setCustomernumbervar(projectData.getCustomernumber());
        timeData.setCustomernamevar(projectData.getName1());
        timeData.setJobnamevar(projectData.getJobname());
        timeData.setJobnumber(projectData.getJobnumber());
    }

    private static void rewriteJob(TimeData timeData, JobData jobData) {
        timeData.setTaskname(jobData.getTaskname());
        timeData.setTasktextvar(jobData.getDescription());
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
