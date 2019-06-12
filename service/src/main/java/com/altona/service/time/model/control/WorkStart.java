package com.altona.service.time.model.control;

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
    private int timeId;

    public static WorkStart alreadyStarted(int timeId) {
        return new WorkStart(Result.WORK_ALREADY_STARTED, timeId);
    }

    public static WorkStart started(int timeId) {
        return new WorkStart(Result.WORK_STARTED, timeId);
    }

    public enum Result {
        WORK_ALREADY_STARTED,
        WORK_STARTED
    }

}
