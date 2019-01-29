package com.altona.service.time.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Time {

    @Getter
    private int id;

    @Getter
    private TimeType type;

    @Getter
    private Date start;

    private Date end;

    public Time(int id, String type, Date start, Date end) {
        this(id, TimeType.valueOf(type), start, end);
    }

    public Optional<Date> getEnd() {
        return Optional.ofNullable(end);
    }

    public LocalTime time(Date now) {
        return LocalTime.ofNanoOfDay(ChronoUnit.NANOS.between(start.toInstant(), getEnd().orElse(now).toInstant()));
    }

}
