package com.altona.project.time;

import com.altona.context.EncryptionContext;
import com.altona.project.Project;
import com.altona.project.time.view.TimeSummaryEntryView;
import com.altona.project.time.view.TimeSummaryView;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TimeSummary {

    @NonNull
    private EncryptionContext encryptionContext;

    @NonNull
    private Project project;

    @NonNull
    private LocalDate from;

    @NonNull
    private LocalDate to;

    @NonNull
    private LinkedHashMap<LocalDate, LocalTime> times;

    public TimeSummaryView asTimeSummaryView() {
        List<TimeSummaryEntryView> timeSummaryEntryViews = times.entrySet().stream()
                .map(entry -> new TimeSummaryEntryView(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return new TimeSummaryView(from, to, timeSummaryEntryViews);
    }

}
