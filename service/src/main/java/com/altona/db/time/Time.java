package com.altona.db.time;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Time {

    @Getter
    private int id;

    @Getter
    private Time.Type type;

    @Getter
    private Date start;

    private Date end;

    Time(int id, String type, Date start, Date end) {
        this(id, Time.Type.valueOf(type), start, end);
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
