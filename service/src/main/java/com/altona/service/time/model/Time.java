package com.altona.service.time.model;

import com.altona.service.time.util.TimeConfig;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@AllArgsConstructor
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

    public LocalizedTime toLocalizedTime(TimeConfig timeConfig) {
        return new LocalizedTime(id, type, timeConfig.localize(start), end == null ? null : timeConfig.localize(end));
    }

}
