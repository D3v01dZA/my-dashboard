package com.altona.service.time.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class SummaryConfiguration {

    @NonNull
    private LocalDate from;

    @NonNull
    private LocalDate to;

    @NonNull
    private TimeRounding rounding;

}
