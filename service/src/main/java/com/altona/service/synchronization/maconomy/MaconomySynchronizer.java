package com.altona.service.synchronization.maconomy;

import com.altona.service.synchronization.SynchronizeRequest;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.synchronization.maconomy.model.MaconomyConfiguration;
import com.altona.service.synchronization.maconomy.model.MaconomyTimeData;
import com.altona.service.synchronization.maconomy.model.get.*;
import com.altona.service.synchronization.maconomy.model.searchjob.JobData;
import com.altona.service.synchronization.maconomy.model.searchproject.ProjectData;
import com.altona.service.synchronization.model.SynchronizeResult;
import com.altona.service.time.TimeService;
import com.altona.service.time.model.summary.*;
import com.altona.util.LocalDateIterator;
import com.altona.util.Result;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class MaconomySynchronizer implements Synchronizer {

    // Application Beans
    @NonNull
    private TimeService timeService;

    @NonNull
    private MaconomyRepository maconomyRepository;

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
        int periodsBack = request.getPeriodsBack();
        // Logic isn't clean for the 0 case because of where Card information is - might do an API review to remove this special case?
        log.info("Synchronization {}: Synchronizing Maconomy {} periods back", synchronizationId, periodsBack);
        if (periodsBack == 0) {
            return maconomyRepository.timeData(request, configuration)
                    .successf(this::saveTimeData)
                    .map(
                            summary -> SynchronizeResult.success(this, request, summary),
                            error -> SynchronizeResult.failure(this, request, error)
                    );
        }
        return maconomyRepository.timeData(request, configuration)
                .successf(currentTime -> saveTimeDataRelative(currentTime.getCardData(), periodsBack - 1))
                .map(
                        summary -> SynchronizeResult.success(this, request, summary),
                        error -> SynchronizeResult.failure(this, request, error)
                );
    }

    private Result<TimeSummary, String> saveTimeDataRelative(CardData cardData, int periodsBack) {
        LocalDate periodStart = cardData.getPeriodstartvar();
        String employee = cardData.getEmployeenumbervar();

        log.info("Retrieving Relative Maconomy {} periods back with date {}", periodsBack, periodStart);
        return maconomyRepository.timeData(request, configuration, periodStart.minusDays(1), employee)
                .successf(currentTime -> {
                    if (periodsBack == 0) {
                        return saveTimeData(currentTime);
                    } else {
                        // IntelliJ doesn't recognize it but this is recursion because we need to resolve when the next period back is each time!
                        return saveTimeDataRelative(currentTime.getCardData(), periodsBack - 1);
                    }
                });
    }

    private Result<TimeSummary, String> saveTimeData(Get currentTime) {
        CardRecord cardRecord = currentTime.getCardRecord();
        CardData cardData = cardRecord.getData();
        LocalDate periodStart = cardData.getPeriodstartvar();
        LocalDate periodEnd = cardData.getPeriodendvar();
        SummaryConfiguration configuration = new SummaryConfiguration(
                request.localize(new Date()),
                periodStart,
                periodEnd,
                TimeRounding.NEAREST_FIFTEEN,
                NotStoppedAction.FAIL,
                false
        );
        log.info("Retrieving time data {}-{}", periodStart, periodEnd);
        return timeService.summary(request, request.getProject(), configuration)
                .error(SummaryFailure::getMessage)
                .successf(summary -> saveTimeDataWithSummary(currentTime, summary, cardRecord));
    }

    private Result<TimeSummary, String> saveTimeDataWithSummary(Get currentTime, TimeSummary timeSummary, CardRecord cardRecord) {
        CardData cardData = cardRecord.getData();
        List<TableRecord> tableRecords = currentTime.getTableRecords();
        LocalDate date = cardData.getDatevar();
        if (tableRecords.isEmpty()) {
            log.info("Initializing time data for period with date {}", date);
            return maconomyRepository.initTimeData(request, configuration, date, cardData.getEmployeenumbervar(), cardRecord.getMeta().getConcurrencyControl())
                    .successf(init -> setProjectAndJobAndWriteTimeData(init.getData(), timeSummary, cardRecord));
        } else if (tableRecords.size() == 1) {
            TableRecord tableRecord = tableRecords.get(0);
            MaconomyTimeData maconomyTimeData = tableRecord.getData();
            rewriteTimes(maconomyTimeData, timeSummary);
            TableMeta meta = tableRecord.getMeta();
            log.info("Updating time data");
            return maconomyRepository.updateTimeData(
                    request,
                    configuration,
                    date,
                    cardData.getEmployeenumbervar(),
                    meta.getConcurrencyControl(),
                    meta.getRowNumber(),
                    maconomyTimeData
            ).successf(get -> checkTimeData(get, timeSummary));
        } else {
            return Result.error("Expected only one meta record");
        }
    }

    private Result<TimeSummary, String> setProjectAndJobAndWriteTimeData(MaconomyTimeData maconomyTimeData, TimeSummary timeSummary, CardRecord cardRecord) {
        log.info("Finding project");
        return maconomyRepository.searchProjectData(request, configuration, maconomyTimeData)
                .successf(project -> project.getProjectData(configuration.getProjectId())
                        .map(projectData -> {
                            rewriteProject(maconomyTimeData, projectData);
                            log.info("Finding job");
                            return maconomyRepository.searchJobData(request, configuration, maconomyTimeData)
                                    .successf(job -> job.getJobData(configuration.getProjectTaskId())
                                            .map(jobData -> {
                                                rewriteJob(maconomyTimeData, jobData);
                                                rewriteTimes(maconomyTimeData, timeSummary);
                                                CardData cardData = cardRecord.getData();
                                                log.info("Writing new time data");
                                                return maconomyRepository.writeTimeData(
                                                        request,
                                                        configuration,
                                                        cardData.getDatevar(),
                                                        cardData.getEmployeenumbervar(),
                                                        cardRecord.getMeta().getConcurrencyControl(),
                                                        maconomyTimeData
                                                ).successf(get -> checkTimeData(get, timeSummary));
                                            })
                                            .orElseGet(() -> Result.error("Job Not Found With Id/Name " + configuration.getProjectTaskId()))
                                    );
                        })
                        .orElseGet(() -> Result.error("Project Not Found With Id" + configuration.getProjectId()))
                );
    }

    private Result<TimeSummary, String> checkTimeData(Get get, TimeSummary timeSummary) {
        List<TableRecord> tableRecords = get.getTableRecords();
        if (tableRecords.size() != 1) {
            return Result.error("There are " + tableRecords.size() + " time records which is not 1");
        }
        MaconomyTimeData maconomyTimeData = tableRecords.get(0).getData();
        LocalDate fromDate = timeSummary.getFromDate();
        LocalDate toDate = timeSummary.getToDate();
        for (LocalDate date : LocalDateIterator.inclusive(fromDate, toDate)) {
            if (date.getDayOfWeek() == DayOfWeek.MONDAY) {
                Optional<LocalTime> actualTime = timeSummary.getActualTime(date);
                if (actualTime.isPresent()) {
                    BigDecimal time = maconomyTimeData.getNumberday1();
                    BigDecimal actualTimeDecimal = asDecimal(actualTime.get());
                    if (!isCloseEnough(actualTimeDecimal, time)) {
                        return notCloseEnough(actualTimeDecimal, time, DayOfWeek.MONDAY);
                    }
                } else {
                    BigDecimal time = maconomyTimeData.getNumberday1();
                    if (!isZero(time)) {
                        return notZero(time, DayOfWeek.MONDAY);
                    }
                }
            } else if (date.getDayOfWeek() == DayOfWeek.TUESDAY) {
                Optional<LocalTime> actualTime = timeSummary.getActualTime(date);
                if (actualTime.isPresent()) {
                    BigDecimal time = maconomyTimeData.getNumberday2();
                    BigDecimal actualTimeDecimal = asDecimal(actualTime.get());
                    if (!isCloseEnough(actualTimeDecimal, time)) {
                        return notCloseEnough(actualTimeDecimal, time, DayOfWeek.TUESDAY);
                    }
                } else {
                    BigDecimal time = maconomyTimeData.getNumberday2();
                    if (!isZero(time)) {
                        return notZero(time, DayOfWeek.TUESDAY);
                    }
                }
            } else if (date.getDayOfWeek() == DayOfWeek.WEDNESDAY) {
                Optional<LocalTime> actualTime = timeSummary.getActualTime(date);
                if (actualTime.isPresent()) {
                    BigDecimal time = maconomyTimeData.getNumberday3();
                    BigDecimal actualTimeDecimal = asDecimal(actualTime.get());
                    if (!isCloseEnough(actualTimeDecimal, time)) {
                        return notCloseEnough(actualTimeDecimal, time, DayOfWeek.WEDNESDAY);
                    }
                } else {
                    BigDecimal time = maconomyTimeData.getNumberday3();
                    if (!isZero(time)) {
                        return notZero(time, DayOfWeek.WEDNESDAY);
                    }
                }
            } else if (date.getDayOfWeek() == DayOfWeek.THURSDAY) {
                Optional<LocalTime> actualTime = timeSummary.getActualTime(date);
                if (actualTime.isPresent()) {
                    BigDecimal time = maconomyTimeData.getNumberday4();
                    BigDecimal actualTimeDecimal = asDecimal(actualTime.get());
                    if (!isCloseEnough(actualTimeDecimal, time)) {
                        return notCloseEnough(actualTimeDecimal, time, DayOfWeek.THURSDAY);
                    }
                } else {
                    BigDecimal time = maconomyTimeData.getNumberday4();
                    if (!isZero(time)) {
                        return notZero(time, DayOfWeek.THURSDAY);
                    }
                }
            } else if (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
                Optional<LocalTime> actualTime = timeSummary.getActualTime(date);
                if (actualTime.isPresent()) {
                    BigDecimal time = maconomyTimeData.getNumberday5();
                    BigDecimal actualTimeDecimal = asDecimal(actualTime.get());
                    if (!isCloseEnough(actualTimeDecimal, time)) {
                        return notCloseEnough(actualTimeDecimal, time, DayOfWeek.FRIDAY);
                    }
                } else {
                    BigDecimal time = maconomyTimeData.getNumberday5();
                    if (!isZero(time)) {
                        return notZero(time, DayOfWeek.FRIDAY);
                    }
                }
            } else if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                Optional<LocalTime> actualTime = timeSummary.getActualTime(date);
                if (actualTime.isPresent()) {
                    BigDecimal time = maconomyTimeData.getNumberday6();
                    BigDecimal actualTimeDecimal = asDecimal(actualTime.get());
                    if (!isCloseEnough(actualTimeDecimal, time)) {
                        return notCloseEnough(actualTimeDecimal, time, DayOfWeek.SATURDAY);
                    }
                } else {
                    BigDecimal time = maconomyTimeData.getNumberday6();
                    if (!isZero(time)) {
                        return notZero(time, DayOfWeek.SATURDAY);
                    }
                }
            } else if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                Optional<LocalTime> actualTime = timeSummary.getActualTime(date);
                if (actualTime.isPresent()) {
                    BigDecimal time = maconomyTimeData.getNumberday7();
                    BigDecimal actualTimeDecimal = asDecimal(actualTime.get());
                    if (!isCloseEnough(actualTimeDecimal, time)) {
                        return notCloseEnough(actualTimeDecimal, time, DayOfWeek.SATURDAY);
                    }
                } else {
                    BigDecimal time = maconomyTimeData.getNumberday7();
                    if (!isZero(time)) {
                        return notZero(time, DayOfWeek.SUNDAY);
                    }
                }
            } else {
                throw new IllegalStateException("TimeSummary came with more than 7 dates between " + fromDate + " and " + toDate);
            }
        }
        return Result.success(timeSummary);
    }

    private boolean isZero(BigDecimal time) {
        return time == null || time.compareTo(BigDecimal.ZERO) == 0;
    }

    private boolean isCloseEnough(BigDecimal actualTime, BigDecimal time) {
        if (time == null) {
            return false;
        }
        if (actualTime.compareTo(time) == 0) {
            return true;
        }
        BigDecimal difference = time.subtract(actualTime).abs();
        // Within one minute
        return difference.compareTo(new BigDecimal("0.02")) == -1;
    }

    private Result<TimeSummary, String> notCloseEnough(BigDecimal actualTime, BigDecimal time, DayOfWeek day) {
        return Result.error("Time data for " + day + " expected " + actualTime + " but was " + time);
    }

    private Result<TimeSummary, String> notZero(BigDecimal time, DayOfWeek day) {
        return Result.error("Time data for " + day + " expected zero but was " + time);
    }

    private void rewriteProject(MaconomyTimeData maconomyTimeData, ProjectData projectData) {
        maconomyTimeData.setCustomernumbervar(projectData.getCustomernumber());
        maconomyTimeData.setCustomernamevar(projectData.getName1());
        maconomyTimeData.setJobnamevar(projectData.getJobname());
        maconomyTimeData.setJobnumber(projectData.getJobnumber());
    }

    private void rewriteJob(MaconomyTimeData maconomyTimeData, JobData jobData) {
        maconomyTimeData.setTaskname(jobData.getTaskname());
        maconomyTimeData.setTasktextvar(jobData.getDescription());
    }

    private void rewriteTimes(MaconomyTimeData maconomyTimeData, TimeSummary timeSummary) {
        LocalDate fromDate = timeSummary.getFromDate();
        LocalDate toDate = timeSummary.getToDate();
        for (LocalDate date : LocalDateIterator.inclusive(fromDate, toDate)) {
            if (date.getDayOfWeek() == DayOfWeek.MONDAY) {
                timeSummary.getActualTime(date)
                        .ifPresent(time -> maconomyTimeData.setNumberday1(asDecimal(time)));
            } else if (date.getDayOfWeek() == DayOfWeek.TUESDAY) {
                timeSummary.getActualTime(date)
                        .ifPresent(time -> maconomyTimeData.setNumberday2(asDecimal(time)));
            } else if (date.getDayOfWeek() == DayOfWeek.WEDNESDAY) {
                timeSummary.getActualTime(date)
                        .ifPresent(time -> maconomyTimeData.setNumberday3(asDecimal(time)));
            } else if (date.getDayOfWeek() == DayOfWeek.THURSDAY) {
                timeSummary.getActualTime(date)
                        .ifPresent(time -> maconomyTimeData.setNumberday4(asDecimal(time)));
            } else if (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
                timeSummary.getActualTime(date)
                        .ifPresent(time -> maconomyTimeData.setNumberday5(asDecimal(time)));
            } else if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                timeSummary.getActualTime(date)
                        .ifPresent(time -> maconomyTimeData.setNumberday6(asDecimal(time)));
            } else if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                timeSummary.getActualTime(date)
                        .ifPresent(time -> maconomyTimeData.setNumberday7(asDecimal(time)));
            } else {
                throw new IllegalStateException("TimeSummary came with more than 7 dates between " + fromDate + " and " + toDate);
            }
        }
    }

    private BigDecimal asDecimal(LocalTime time) {
        // On the site 1:40 becomes 1.6666666666666665 which I have no idea how to replicate so imma ignore it
        String minutes = new BigDecimal(time.getMinute()).divide(new BigDecimal(60), 16, RoundingMode.FLOOR).toPlainString();
        String minutesWithoutZero = minutes.substring(2);
        return new BigDecimal(time.getHour() + "." + minutesWithoutZero);
    }

}
