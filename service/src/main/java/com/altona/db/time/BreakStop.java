package com.altona.db.time;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BreakStop {

    @Getter
    @NonNull
    private Result result;

    private Integer timeId;

    static BreakStop stopped(int timeId) {
        return new BreakStop(Result.BREAK_STOPPED, timeId);
    }

    static BreakStop workNotStarted() {
        return new BreakStop(Result.WORK_NOT_STARTED, null);
    }

    static BreakStop breakNotStarted() {
        return new BreakStop(Result.BREAK_NOT_STARTED, null);
    }

    public Optional<Integer> getTimeId() {
        return Optional.ofNullable(timeId);
    }

    private enum Result {

        BREAK_STOPPED,
        WORK_NOT_STARTED,
        BREAK_NOT_STARTED

    }

}
