package com.altona.db.time.control;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkStop {

    @Getter
    @NonNull
    private WorkStop.Result result;

    private Integer workTimeId;
    private Integer breakTimeId;

    public static WorkStop notStarted() {
        return new WorkStop(Result.WORK_NOT_STARTED, null, null);
    }

    public static WorkStop ended(int workId) {
        return new WorkStop(Result.WORK_STOPPED, workId, null);
    }

    public static WorkStop ended(int workId, int breakId) {
        return new WorkStop(Result.WORK_AND_BREAK_STOPPED, workId, breakId);
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
        WORK_AND_BREAK_STOPPED
    }

}
