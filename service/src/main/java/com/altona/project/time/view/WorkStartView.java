package com.altona.project.time.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;

@Getter
@AllArgsConstructor
public class WorkStartView {

    @NonNull
    private Result result;

    private int projectId;

    private Integer timeRunningProjectId;

    private Integer timeId;

    public Optional<Integer> getTimeRunningProjectId() {
        return Optional.ofNullable(timeRunningProjectId);
    }

    public Optional<Integer> getTimeId() {
        return Optional.ofNullable(timeId);
    }

    public enum Result {
        WORK_ALREADY_STARTED,
        BREAK_ALREADY_STARTED,
        WORK_STARTED,
        OTHER_PROJECT_TIME_RUNNING
    }

}
