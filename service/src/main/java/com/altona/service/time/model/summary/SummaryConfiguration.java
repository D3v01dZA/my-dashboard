package com.altona.service.time.model.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
public class SummaryConfiguration {

    @NonNull
    private LocalDateTime localizedUserNow;

    @NonNull
    private LocalDate from;

    @NonNull
    private LocalDate to;

    @NonNull
    private TimeRounding rounding;

    @NonNull
    private NotStoppedAction notStoppedAction;

}
