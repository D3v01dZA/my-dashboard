package com.altona.project.time.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class TimeSummaryView {

    @NonNull
    private LocalDate fromDate;

    @NonNull
    private LocalDate toDate;

    @NonNull
    private List<TimeSummaryEntryView> times;

}
