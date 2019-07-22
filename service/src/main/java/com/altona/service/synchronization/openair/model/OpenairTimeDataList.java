package com.altona.service.synchronization.openair.model;

import com.altona.service.time.model.summary.TimeSummary;
import com.altona.util.LocalDateIterator;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class OpenairTimeDataList {

    @NonNull
    private LocalDate weekStart;


    @NonNull
    private LocalDate weekEnd;

    @NonNull
    private List<OpenairTimeData> timeDataList;

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    public TimeSummary getAllData(String project, String task) {
        LinkedHashMap<LocalDate, LocalTime> all = new LinkedHashMap<>();
        for (LocalDate localDate : LocalDateIterator.inclusive(getWeekStart(), getWeekEnd())) {
            all.put(localDate, LocalTime.of(0, 0));
        }
        for (OpenairTimeData timeData : timeDataList) {
            if (timeData.getProject().equals(project) && timeData.getTask().equals(task)) {
                for (Map.Entry<LocalDate, LocalTime> entry : timeData.getTimeMap().entrySet()) {
                    LocalTime current = all.get(entry.getKey());
                    LocalTime added = current.plus(entry.getValue().toNanoOfDay(), ChronoUnit.NANOS);
                    all.put(entry.getKey(), added);
                }
            }
        }
        return new TimeSummary(getWeekStart(), getWeekEnd(), all);
    }

}
