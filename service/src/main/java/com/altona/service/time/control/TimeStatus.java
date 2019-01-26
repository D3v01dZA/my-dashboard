package com.altona.service.time.control;

import java.time.LocalTime;
import java.util.Optional;

public class TimeStatus {

    public static TimeStatus none() {
        return new TimeStatus(Status.NONE, null, null);
    }

    public static TimeStatus atWork(LocalTime runningWorkTotal, LocalTime runningBreakTotal) {
        return new TimeStatus(Status.WORK, runningWorkTotal, runningBreakTotal);
    }

    public static TimeStatus onBreak(LocalTime runningWorkTotal, LocalTime runningBreakTotal) {
        return new TimeStatus(Status.BREAK, runningWorkTotal, runningBreakTotal);
    }

    private Status status;
    private LocalTime runningWorkTotal;
    private LocalTime runningBreakTotal;

    private TimeStatus(Status status, LocalTime runningWorkTotal, LocalTime runningBreakTotal) {
        this.status = status;
        this.runningWorkTotal = runningWorkTotal;
        this.runningBreakTotal = runningBreakTotal;
    }

    public Status getStatus() {
        return status;
    }

    public Optional<LocalTime> getRunningWorkTotal() {
        return Optional.ofNullable(runningWorkTotal);
    }

    public Optional<LocalTime> getRunningBreakTotal() {
        return Optional.ofNullable(runningBreakTotal);
    }

    private enum Status {

        NONE,
        WORK,
        BREAK

    }

}
