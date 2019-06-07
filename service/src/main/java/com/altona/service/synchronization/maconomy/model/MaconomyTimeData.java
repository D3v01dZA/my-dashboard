package com.altona.service.synchronization.maconomy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class MaconomyTimeData {

    @Getter
    @NonNull
    private String projectName;

    @Getter
    @NonNull
    private String taskName;

    @Getter
    @NonNull
    private Map<LocalDate, LocalTime> timeMap;

    public Optional<LocalTime> getTime(LocalDate date) {
        return Optional.ofNullable(timeMap.get(date));
    }

}
