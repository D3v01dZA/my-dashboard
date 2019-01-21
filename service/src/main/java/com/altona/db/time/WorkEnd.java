package com.altona.db.time;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;

@AllArgsConstructor
public class WorkEnd {

    @Getter
    @NonNull
    private WorkEnd.Result result;

    private Integer timeId;

    static WorkEnd notStarted() {
        return new WorkEnd(Result.NOT_STARTED, null);
    }

    static WorkEnd ended(int timeId) {
        return new WorkEnd(Result.ENDED, timeId);
    }

    public Optional<Integer> getTimeId() {
        return Optional.ofNullable(timeId);
    }

    public enum Result {
        NOT_STARTED,
        ENDED
    }

}
