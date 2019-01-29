package com.altona.dashboard.service.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Getter;

@Getter
public class TimeSummaryEntry {

    private LocalDate date;
    private LocalTime time;

    @JsonCreator
    public TimeSummaryEntry(
            @JsonProperty(value = "date", required = true) LocalDate date,
            @JsonProperty(value = "time", required = true) LocalTime time
    ) {
        this.date = date;
        this.time = time;
    }

}
