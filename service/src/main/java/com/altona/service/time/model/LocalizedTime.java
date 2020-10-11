package com.altona.service.time.model;

import com.altona.service.time.util.TimeConfig;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@AllArgsConstructor
public class LocalizedTime {

    @Getter
    private int id;

    @Getter
    private TimeType type;

    @Getter
    private LocalDateTime start;

    private LocalDateTime end;

    public Optional<LocalDateTime> getEnd() {
        return Optional.ofNullable(end);
    }

    public Time unlocalize(TimeConfig timeConfig) {
        return new Time(id, type, timeConfig.unlocalize(start), end == null ? null : timeConfig.unlocalize(end));
    }

}
