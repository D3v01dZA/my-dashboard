package com.altona.dashboard.service.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;

@Getter
public class TimeSummary {

    private LocalDate fromDate;
    private LocalDate toDate;
    private List<TimeSummaryEntry> times;

    @JsonCreator
    TimeSummary(
            @JsonProperty(value = "fromDate", required = true) LocalDate fromDate,
            @JsonProperty(value = "toDate", required = true) LocalDate toDate,
            @JsonProperty(value = "times", required = true) List<TimeSummaryEntry> times
    ) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.times = times;
    }

}
