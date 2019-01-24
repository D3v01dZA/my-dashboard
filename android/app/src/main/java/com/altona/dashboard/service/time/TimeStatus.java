package com.altona.dashboard.service.time;

import com.altona.dashboard.view.time.TimeContent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import lombok.Getter;

public class TimeStatus {

    private static final LocalTime NO_TIME = LocalTime.of(0, 0);

    @Getter
    private TimeContent.Status status;

    private LocalDateTime lastUpdate;

    private LocalTime runningWorkTotal;
    private LocalTime runningBreakTotal;

    @JsonCreator
    TimeStatus(
            @JsonProperty(value = "status", required = true) TimeContent.Status status,
            @JsonProperty(value = "runningWorkTotal") LocalTime runningWorkTotal,
            @JsonProperty(value = "runningBreakTotal") LocalTime runningBreakTotal
    ) {
        this.status = status;
        this.lastUpdate = LocalDateTime.now();
        this.runningWorkTotal = runningWorkTotal == null ? NO_TIME : runningWorkTotal;
        this.runningBreakTotal = runningBreakTotal == null ? NO_TIME : runningBreakTotal;
    }

    public void update() {
        LocalDateTime now = LocalDateTime.now();
        if (status.updateWork()) {
            runningWorkTotal = runningWorkTotal.plus(ChronoUnit.NANOS.between(lastUpdate, now), ChronoUnit.NANOS);
        }
        if (status.updateBreak()) {
            runningBreakTotal = runningBreakTotal.plus(ChronoUnit.NANOS.between(lastUpdate, now), ChronoUnit.NANOS);
        }
        lastUpdate = now;
    }

    public void startWork() {
        status = TimeContent.Status.WORK;
        if (runningWorkTotal == null) {
            runningWorkTotal = NO_TIME;
        }
        if (runningBreakTotal == null) {
            runningBreakTotal = NO_TIME;
        }
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

    public LocalTime getRunningWorkTotal() {
        return runningWorkTotal;
    }

    public LocalTime getRunningBreakTotal() {
        return runningBreakTotal;
    }

}
