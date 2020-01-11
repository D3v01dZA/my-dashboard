package com.altona.service.synchronization.maconomy.model;

import com.altona.project.time.TimeSummary;
import com.altona.project.time.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class MaconomyTimeDataList {

    @NonNull
    private LocalDate weekStart;

    @NonNull
    private LocalDate weekEnd;

    @NonNull
    private List<MaconomyTimeData> timeDataList;

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    public TimeSummary getAllData(String projectName, String taskName) {
        LinkedHashMap<LocalDate, LocalTime> all = new LinkedHashMap<>();
        for (LocalDate localDate : TimeUtil.LocalDateIterator.inclusive(getWeekStart(), getWeekEnd())) {
            all.put(localDate, LocalTime.of(0, 0));
        }
        for (MaconomyTimeData timeData : timeDataList) {
            if (timeData.getProjectName().equals(projectName) && timeData.getTaskName().equals(taskName)) {
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
