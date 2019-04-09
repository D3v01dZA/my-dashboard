package com.altona.service.time.model.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SummaryFailure {

    CURRENTLY_RUNNING_TIME("TimeSummary period contained a time that has not been stopped"),
    MISMATCHED_START_DATE("TimeSummary difference start dates did not match"),
    MISMATCHED_END_DATE("TimeSummary difference end dates did not match"),
    CURRENT_TIME_SMALLER_THAN_DIFFERENCE("TimeSummary current time was smaller than difference"),
    TIME_CROSSING_DAYS("Time was found crossing two days");

    private String message;

}
