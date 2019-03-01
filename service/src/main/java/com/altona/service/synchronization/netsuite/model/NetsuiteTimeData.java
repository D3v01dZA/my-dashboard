package com.altona.service.synchronization.netsuite.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class NetsuiteTimeData {

    @Getter
    @NonNull
    private String customer;

    @Getter
    @NonNull
    private String task;

    @Getter
    @NonNull
    private Map<LocalDate, LocalTime> timeMap;

    public Optional<LocalTime> getTime(LocalDate date) {
        return Optional.ofNullable(timeMap.get(date));
    }

}
