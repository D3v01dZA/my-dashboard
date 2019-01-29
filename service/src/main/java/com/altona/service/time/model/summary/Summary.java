package com.altona.service.time.model.summary;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Summary {

    @Getter
    private LocalDate fromDate;
    @Getter
    private LocalDate toDate;

    private LinkedHashMap<LocalDate, SummaryTime> times;

    public Collection<SummaryTime> getTimes() {
        return times.values();
    }

    public Optional<LocalTime> getActualTime(LocalDate date) {
        return Optional.ofNullable(times.get(date))
                .map(SummaryTime::getTime);
    }

}
