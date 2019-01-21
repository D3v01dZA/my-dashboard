package com.altona.db.time;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BreakStart {

    @Getter
    @NonNull
    private Result result;

    private Integer timeId;

    static BreakStart started(int timeId) {
        return new BreakStart(Result.BREAK_STARTED, timeId);
    }

    static BreakStart breakAlreadyStarted(int timeId) {
        return new BreakStart(Result.BREAK_ALREADY_STARTED, timeId);
    }

    static BreakStart workNotStarted() {
        return new BreakStart(Result.WORK_NOT_STARTED, null);
    }

    public Optional<Integer> getTimeId() {
        return Optional.ofNullable(timeId);
    }

    private enum Result {

        BREAK_STARTED,
        BREAK_ALREADY_STARTED,
        WORK_NOT_STARTED

    }

}
