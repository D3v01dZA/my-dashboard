package com.altona.service.time.control;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkStart {

    @Getter
    @NonNull
    private Result result;

    @Getter
    private int projectId;

    @Getter
    private int timeId;

    public static WorkStart alreadyStarted(int projectId, int timeId) {
        return new WorkStart(Result.WORK_ALREADY_STARTED, projectId, timeId);
    }

    public static WorkStart started(int projectId, int timeId) {
        return new WorkStart(Result.WORK_STARTED, projectId, timeId);
    }

    private enum Result {
        WORK_ALREADY_STARTED,
        WORK_STARTED
    }

}
