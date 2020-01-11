package com.altona.project.time.query;

import com.altona.context.TimeConfig;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@AllArgsConstructor
class TimeEntry {

    @NonNull
    private TimeRecord workRecord;

    @NonNull
    private List<TimeRecord> breakRecords;

    boolean isStopped() {
        return workRecord.isStopped();
    }

    LocalDate fromDate(TimeConfig timeConfig) {
        return workRecord.fromDate(timeConfig);
    }

    LocalDate toDate(TimeConfig timeConfig) {
        return workRecord.toDate(timeConfig);
    }

    LocalTime totalTime() {
        LocalTime totalTime = workRecord.totalTime();
        for (TimeRecord breakRecord : breakRecords) {
            totalTime = totalTime.minus(breakRecord.totalTime().toNanoOfDay(), ChronoUnit.NANOS);
        }
        return totalTime;
    }

}
