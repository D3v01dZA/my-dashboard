package com.altona.service.time.model.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SummaryFailure {

    CURRENTLY_RUNNING_TIME("Summary period contained a time that has not been stopped"),
    TIME_CROSSING_DAYS("Time was found crossing two days");

    private String message;

}
