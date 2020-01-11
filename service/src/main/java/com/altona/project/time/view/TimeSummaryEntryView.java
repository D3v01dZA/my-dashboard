package com.altona.project.time.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class TimeSummaryEntryView {

    @NonNull
    private LocalDate date;

    @NonNull
    private LocalTime time;

}
