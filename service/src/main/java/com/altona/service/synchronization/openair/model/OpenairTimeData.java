package com.altona.service.synchronization.openair.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@AllArgsConstructor
public class OpenairTimeData {

    @Getter
    @NonNull
    private String project;

    @Getter
    @NonNull
    private String task;

    @Getter
    @NonNull
    private Map<LocalDate, LocalTime> timeMap;

}
