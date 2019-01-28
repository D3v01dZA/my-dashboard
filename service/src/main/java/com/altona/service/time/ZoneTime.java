package com.altona.service.time;

import com.altona.repository.db.time.time.Time;
import com.altona.repository.db.time.time.TimeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ZoneTime {

    @Getter
    private int id;

    @Getter
    @NonNull
    private TimeType type;

    @Getter
    @NonNull
    private LocalDateTime start;

    private LocalDateTime end;

    ZoneTime(TimeZoneMapper timeZoneMapper, Time time) {
        this(time.getId(), time.getType(), timeZoneMapper.mapDateTime(time.getStart()), time.getEnd().map(timeZoneMapper::mapDateTime).orElse(null));
    }

    public Optional<LocalDateTime> getEnd() {
        return Optional.ofNullable(end);
    }

}
