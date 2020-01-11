package com.altona.project.time.query;

import com.altona.context.EncryptionContext;
import com.altona.project.Project;
import com.altona.project.time.TimeSummary;
import com.altona.project.time.TimeUtil;
import com.altona.util.Result;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TimeSelection {

    @NonNull
    private EncryptionContext encryptionContext;

    @NonNull
    private Project project;

    @NonNull
    private LocalDate from;

    @NonNull
    private LocalDate to;

    @NonNull
    private TimeRounding rounding;

    @NonNull
    private NotStoppedAction notStoppedAction;

    private boolean includeZeroDays;

    public Result<TimeSummary, String> execute() {
        Map<LocalDate, LocalTime> timeMap = new LinkedHashMap<>();
        List<TimeEntry> timeEntries = new TimeEntriesBetween(encryptionContext, project, from, to).execute();

        for (TimeEntry timeEntry : timeEntries) {
            if (!timeEntry.isStopped()) {
                if (notStoppedAction == NotStoppedAction.FAIL) {
                    return Result.failure("Summary failed due to time not being stopped");
                } else if (notStoppedAction == NotStoppedAction.INCLUDE) {
                    Optional<String> failure = addTime(timeMap, timeEntry);
                    if (failure.isPresent()) {
                        return Result.failure(failure.get());
                    }
                }
            } else {
                Optional<String> failure = addTime(timeMap, timeEntry);
                if (failure.isPresent()) {
                    return Result.failure(failure.get());
                }
            }
        }

        if (includeZeroDays) {
            for (LocalDate localDate : TimeUtil.LocalDateIterator.exclusive(from, to)) {
                if (!timeMap.containsKey(localDate)) {
                    timeMap.put(localDate, TimeUtil.ZERO);
                }
            }
        }

        Predicate<LocalDate> between = betweenInclusive(from, to);
        LinkedHashMap<LocalDate, LocalTime> summaryTimes = timeMap.entrySet().stream()
                .filter(entry -> between.test(entry.getKey()))
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> rounding.round(entry.getValue()),
                        (one, two) -> { throw new IllegalStateException("Should be impossible but " + one + " and " + two + " are identical"); },
                        LinkedHashMap::new
                ));
        return Result.success(new TimeSummary(encryptionContext, project, from, to, summaryTimes));
    }

    private Optional<String> addTime(Map<LocalDate, LocalTime> timeMap, TimeEntry timeEntry) {
        LocalDate fromDate = timeEntry.fromDate(encryptionContext);
        LocalDate toDate = timeEntry.toDate(encryptionContext);
        if (fromDate.equals(toDate)) {
            LocalTime currentTime = timeMap.get(fromDate);
            if (currentTime == null) {
                currentTime = TimeUtil.ZERO;
            }
            timeMap.put(fromDate, currentTime.plus(timeEntry.totalTime().toNanoOfDay(), ChronoUnit.NANOS));
            return Optional.empty();
        } else {
            return Optional.of("Time was found crossing two days");
        }
    }

    private static Predicate<LocalDate> betweenInclusive(LocalDate from, LocalDate to) {
        return date -> date.isEqual(from) || date.isEqual(to) || (date.isAfter(from) && date.isBefore(to));
    }

}
