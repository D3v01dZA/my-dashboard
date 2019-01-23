package com.altona.dashboard.service.time;

import com.altona.dashboard.view.time.TimeContent;

import java.time.LocalTime;
import java.util.Optional;

public class TimeStatus {

    private TimeContent.Status status;

    private LocalTime runningWorkTotal;
    private LocalTime runningBreakTotal;

    TimeStatus(TimeContent.Status status, LocalTime runningWorkTotal, LocalTime runningBreakTotal) {
        this.status = status;
        this.runningWorkTotal = runningWorkTotal;
        this.runningBreakTotal = runningBreakTotal;
    }

    public TimeContent.Status getStatus() {
        return status;
    }

    public Optional<LocalTime> getRunningWorkTotal() {
        return Optional.ofNullable(runningWorkTotal);
    }

    public Optional<LocalTime> getRunningBreakTotal() {
        return Optional.ofNullable(runningBreakTotal);
    }

}
