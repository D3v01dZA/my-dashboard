package com.altona.project.time.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalTime;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class TimeStatusView {

    @NonNull
    private Status status;

    private Integer timeId;
    private Integer projectId;

    private LocalTime runningWorkTotal;
    private LocalTime runningBreakTotal;

    public Optional<Integer> getTimeId() {
        return Optional.ofNullable(timeId);
    }

    public Optional<Integer> getProjectId() {
        return Optional.ofNullable(projectId);
    }

    public Optional<LocalTime> getRunningWorkTotal() {
        return Optional.ofNullable(runningWorkTotal);
    }

    public Optional<LocalTime> getRunningBreakTotal() {
        return Optional.ofNullable(runningBreakTotal);
    }

    public enum Status {

        NONE,
        WORK,
        BREAK

    }

}
