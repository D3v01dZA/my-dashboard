package com.altona.db.time;

import lombok.Getter;
import lombok.NonNull;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class Time {

    @Getter
    private int id;

    @Getter
    private Type type;

    @Getter
    private Date start;

    private Date end;

    Time(int id, String type, Date start, Date end) {
        this(id, Type.valueOf(type), start, end);
    }

    private Time(int id, @NonNull Type type, @NonNull Date start, Date end) {
        this.id = id;
        this.type = Objects.requireNonNull(type);
        this.start = Objects.requireNonNull(start);
        this.end = end;
    }

    public Optional<Date> getEnd() {
        return Optional.ofNullable(end);
    }

    public enum Type {
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
}
