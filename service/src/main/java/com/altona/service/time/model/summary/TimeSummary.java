package com.altona.service.time.model.summary;

import com.altona.util.LocalDateIterator;
import com.altona.util.Result;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TimeSummary {

    @Getter
    @NonNull
    private LocalDate fromDate;

    @Getter
    @NonNull
    private LocalDate toDate;

    @NonNull
    private LinkedHashMap<LocalDate, LocalTime> times;

    public List<SummaryTime> getTimes() {
        return times.entrySet().stream()
                .map(entry -> new SummaryTime(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public Optional<LocalTime> getActualTime(LocalDate date) {
        return Optional.ofNullable(times.get(date));
    }

    public boolean hasTime() {
        return times.values().stream().anyMatch(localTime -> localTime.isAfter(LocalTime.of(0, 0, 0)));
    }

    public Result<TimeSummary, SummaryFailure>  getDifference(TimeSummary other) {
        if (!other.fromDate.equals(fromDate)) {
            return Result.failure(SummaryFailure.MISMATCHED_START_DATE);
        }
        if (!other.toDate.equals(toDate)) {
            return Result.failure(SummaryFailure.MISMATCHED_END_DATE);
        }

        LinkedHashMap<LocalDate, LocalTime> differenceTimes = new LinkedHashMap<>();
        for (LocalDate current : LocalDateIterator.inclusive(fromDate, toDate)) {
            Optional<LocalTime> left = getActualTime(current);
            Optional<LocalTime> right = other.getActualTime(current);
            if (left.isPresent()) {
                if (right.isPresent()) {
                    LocalTime l = left.get();
                    LocalTime r = right.get();
                    if (l.isBefore(r)) {
                        return Result.failure(SummaryFailure.CURRENT_TIME_SMALLER_THAN_DIFFERENCE);
                    }
                    differenceTimes.put(current, l.minus(r.toNanoOfDay(), ChronoUnit.NANOS));
                } else {
                    differenceTimes.put(current, left.get());
                }
            } else if (right.isPresent() && right.get().isAfter(LocalTime.of(0, 0))) {
                return Result.failure(SummaryFailure.CURRENT_TIME_SMALLER_THAN_DIFFERENCE);
            }
        }
        return Result.success(new TimeSummary(fromDate, toDate, differenceTimes));
    }

}
