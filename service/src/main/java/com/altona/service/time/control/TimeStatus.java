package com.altona.service.time.control;

import com.altona.db.time.project.Project;
import com.altona.db.time.time.Time;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeStatus {

    public static TimeStatus none() {
        return new TimeStatus(Status.NONE, null, null,null, null);
    }

    public static TimeStatus atWork(Project project, Time time, LocalTime runningWorkTotal, LocalTime runningBreakTotal) {
        return new TimeStatus(Status.WORK, project, time, runningWorkTotal, runningBreakTotal);
    }

    public static TimeStatus onBreak(Project project, Time time, LocalTime runningWorkTotal, LocalTime runningBreakTotal) {
        return new TimeStatus(Status.BREAK, project, time, runningWorkTotal, runningBreakTotal);
    }

    @Getter
    private Status status;
    private Project project;
    private Time time;
    private LocalTime runningWorkTotal;
    private LocalTime runningBreakTotal;

    public Optional<Integer> getProjectId() {
        return Optional.ofNullable(project)
                .map(Project::getId);
    }

    public Optional<Integer> getTimeId() {
        return Optional.ofNullable(time)
                .map(Time::getId);
    }

    public Optional<LocalTime> getRunningWorkTotal() {
        return Optional.ofNullable(runningWorkTotal);
    }

    public Optional<LocalTime> getRunningBreakTotal() {
        return Optional.ofNullable(runningBreakTotal);
    }

    @JsonIgnore
    public Optional<Time> getTime() {
        return Optional.ofNullable(time);
    }

    @JsonIgnore
    public Optional<Project> getProject() {
        return Optional.ofNullable(project);
    }

    @JsonIgnore
    public boolean isTimeRunning() {
        return getStatus() != Status.NONE;
    }

    private enum Status {

        NONE,
        WORK,
        BREAK

    }

}
