package com.altona.service.time.model.summary;

import com.altona.service.time.model.TimeCombination;
import com.altona.service.time.util.TimeConfig;
import com.altona.service.time.util.TimeInfo;
import com.altona.util.LocalDateIterator;
import com.altona.util.Result;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class SummaryCreator {

    private static LocalTime NO_TIME = LocalTime.of(0, 0);

    @NonNull
    private TimeConfig timeConfig;

    @NonNull
    private SummaryConfiguration summaryConfiguration;

    public Result<TimeSummary, SummaryFailure> create(TimeInfo timeInfo, List<TimeCombination> timeCombinations) {
        Instant now = timeInfo.now();
        Map<LocalDate, LocalTime> timeMap = new HashMap<>();
        for (TimeCombination timeCombination : timeCombinations) {
            if (!timeCombination.isStopped()) {
                if (summaryConfiguration.getNotStoppedAction() == NotStoppedAction.FAIL) {
                    log.info("Summary failure due to not stopped time");
                    return Result.failure(SummaryFailure.CURRENTLY_RUNNING_TIME);
                } else if (summaryConfiguration.getNotStoppedAction() == NotStoppedAction.INCLUDE) {
                    Optional<SummaryFailure> failure = addTime(timeMap, timeCombination, now);
                    if (failure.isPresent()) {
                        SummaryFailure msg = failure.get();
                        log.info("Summary failure {}", msg);
                        return Result.failure(msg);
                    }
                }
            } else {
                Optional<SummaryFailure> failure = addTime(timeMap, timeCombination, now);
                if (failure.isPresent()) {
                    SummaryFailure msg = failure.get();
                    log.info("Summary failure {}", msg);
                    return Result.failure(failure.get());
                }
            }
        }

        LocalDate from = summaryConfiguration.getFrom();
        LocalDate to = summaryConfiguration.getTo();

        if (summaryConfiguration.isIncludeZeroDays()) {
            for (LocalDate localDate : LocalDateIterator.exclusive(from, to)) {
                if (!timeMap.containsKey(localDate)) {
                    timeMap.put(localDate, NO_TIME);
                }
            }
        }

        Predicate<LocalDate> between = betweenInclusive(from, to);
        LinkedHashMap<LocalDate, LocalTime> summaryTimes = timeMap.entrySet().stream()
                .filter(entry -> between.test(entry.getKey()))
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> new SummaryTime(entry.getKey(), summaryConfiguration.getRounding().round(entry.getValue())))
                .collect(Collectors.toMap(
                        SummaryTime::getDate,
                        SummaryTime::getTime,
                        (one, two) -> { throw new IllegalStateException("Should be impossible but " + one + " and " + two + " are identical"); },
                        LinkedHashMap::new
                ));
        return Result.success(new TimeSummary(from, to, summaryTimes));
    }

    private Optional<SummaryFailure> addTime(Map<LocalDate, LocalTime> map, TimeCombination time, Instant now) {
        LocalDate fromDate = timeConfig.localize(time.getStart()).toLocalDate();
        LocalDate toDate = timeConfig.localize(time.getEnd(now)).toLocalDate();
        if (fromDate.equals(toDate)) {
            LocalTime currentTime = map.get(fromDate);
            if (currentTime == null) {
                currentTime = NO_TIME;
            }
            map.put(fromDate, currentTime.plus(time.time(now).toNanoOfDay(), ChronoUnit.NANOS));
            return Optional.empty();
        } else {
            return Optional.of(SummaryFailure.TIME_CROSSING_DAYS);
        }
    }

    private static Predicate<LocalDate> betweenInclusive(LocalDate from, LocalDate to) {
        return date -> date.isEqual(from) || date.isEqual(to) || (date.isAfter(from) && date.isBefore(to));
    }

}
