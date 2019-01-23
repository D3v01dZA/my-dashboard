package com.altona.dashboard.service.time;

import com.altona.dashboard.view.time.TimeContent;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class TimeStatus {

    private static final LocalTime NO_TIME = LocalTime.of(0, 0);

    private TimeContent.Status status;

    private LocalTime runningWorkTotal;
    private LocalTime runningBreakTotal;

    TimeStatus(TimeContent.Status status, LocalTime runningWorkTotal, LocalTime runningBreakTotal) {
        this.status = status;
        this.runningWorkTotal = runningWorkTotal;
        this.runningBreakTotal = runningBreakTotal;
    }

    public void secondTick() {
        if (status.addToWorkOnSecondTick()) {
            runningWorkTotal = runningWorkTotal.plus(1, ChronoUnit.SECONDS);
        }
        if (status.addToBreakOnSecondTick()) {
            runningBreakTotal = runningBreakTotal.plus(1, ChronoUnit.SECONDS);
        }
    }

    public void startWork() {
        status = TimeContent.Status.WORK;
    }

    public void startBreak() {
        status = TimeContent.Status.BREAK;
    }

    public void stopBreak() {
        status = TimeContent.Status.WORK;
    }

    public void stopWork() {
        status = TimeContent.Status.NONE;
        runningWorkTotal = NO_TIME;
        runningBreakTotal = NO_TIME;
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
