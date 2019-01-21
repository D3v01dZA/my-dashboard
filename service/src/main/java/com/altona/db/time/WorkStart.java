package com.altona.db.time;

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
    @NonNull
    private int timeId;

    public static WorkStart alreadyStarted(int timeId) {
        return new WorkStart(Result.ALREADY_STARTED, timeId);
    }

    public static WorkStart started(int timeId) {
        return new WorkStart(Result.STARTED, timeId);
    }

    private enum Result {
        ALREADY_STARTED,
        STARTED
    }

}
