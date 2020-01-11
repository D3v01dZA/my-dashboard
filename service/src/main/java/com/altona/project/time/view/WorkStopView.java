package com.altona.project.time.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;

@Getter
@AllArgsConstructor
public class WorkStopView {

    @NonNull
    private WorkStopView.Result result;

    private int projectId;

    private Integer timeRunningProjectId;

    private Integer workTimeId;

    private Integer breakTimeId;

    public Optional<Integer> getTimeRunningProjectId() {
        return Optional.ofNullable(timeRunningProjectId);
    }

    public Optional<Integer> getWorkTimeId() {
        return Optional.ofNullable(workTimeId);
    }

    public Optional<Integer> getBreakTimeId() {
        return Optional.ofNullable(breakTimeId);
    }

    public enum Result {
        WORK_NOT_STARTED,
        WORK_STOPPED,
        WORK_AND_BREAK_STOPPED,
        OTHER_PROJECT_TIME_RUNNING
    }

}
