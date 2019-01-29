package com.altona.service.time.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SummaryFailure {

    CURRENTLY_RUNNING_TIME("Summary period contained a time that has not been stopped");

    private String message;

}
