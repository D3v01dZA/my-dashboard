package com.altona.project.time.query;

import com.altona.context.TimeConfig;
import com.altona.project.time.TimeType;
import com.altona.project.time.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@AllArgsConstructor
class TimeRecord {

    private int id;

    @NonNull
    private TimeType timeType;

    @NonNull
    private Instant startTime;

    private Instant endTime;

    boolean isWork() {
        return timeType.map(() -> true, () -> false);
    }

    boolean isStopped() {
        return endTime != null;
    }

    LocalDate fromDate(TimeConfig timeConfig) {
        return timeConfig.localize(startTime).toLocalDate();
    }

    LocalDate toDate(TimeConfig timeConfig) {
        return toDateTime(timeConfig).toLocalDate();
    }

    LocalTime totalTime() {
        return TimeUtil.difference(startTime, endTime);
    }

    private LocalDateTime toDateTime(TimeConfig timeConfig) {
        if (endTime != null) {
            return timeConfig.localize(endTime);
        }
        return timeConfig.localizedNow();
    }

}