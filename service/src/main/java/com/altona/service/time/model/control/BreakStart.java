package com.altona.service.time.model.control;

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

    public static BreakStart started(int timeId) {
        return new BreakStart(Result.BREAK_STARTED, timeId);
    }

    public static BreakStart breakAlreadyStarted(int timeId) {
        return new BreakStart(Result.BREAK_ALREADY_STARTED, timeId);
    }

    public static BreakStart workNotStarted() {
        return new BreakStart(Result.WORK_NOT_STARTED, null);
    }

    public Optional<Integer> getTimeId() {
        return Optional.ofNullable(timeId);
    }

    public enum Result {

        BREAK_STARTED,
        BREAK_ALREADY_STARTED,
        WORK_NOT_STARTED

    }

}
