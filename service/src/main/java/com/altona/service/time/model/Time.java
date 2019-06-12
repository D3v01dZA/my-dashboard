package com.altona.service.time.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Time {

    @Getter
    private int id;

    @Getter
    private TimeType type;

    @Getter
    private Instant start;

    private Instant end;

    public Time(int id, String type, Instant start, Instant end) {
        this(id, TimeType.valueOf(type), start, end);
    }

    public Optional<Instant> getEnd() {
        return Optional.ofNullable(end);
    }

    public LocalTime time(Instant now) {
        return LocalTime.ofNanoOfDay(ChronoUnit.NANOS.between(start, getEnd().orElse(now)));
    }

}
