package com.altona.repository.db.time.time;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public enum TimeType {
    WORK {
        @Override
        public LocalTime add(LocalTime left, LocalTime right) {
            return left.plus(right.toNanoOfDay(), ChronoUnit.NANOS);
        }
    },
    BREAK {
        @Override
        public LocalTime add(LocalTime left, LocalTime right) {
            return left.minus(right.toNanoOfDay(), ChronoUnit.NANOS);
        }
    };

    public abstract LocalTime add(LocalTime left, LocalTime right);
}
