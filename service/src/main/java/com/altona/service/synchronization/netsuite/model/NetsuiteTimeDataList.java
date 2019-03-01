package com.altona.service.synchronization.netsuite.model;

import com.altona.service.time.model.summary.Summary;
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
public class NetsuiteTimeDataList {

    @NonNull
    private LocalDate week;

    @NonNull
    private List<NetsuiteTimeData> timeDataList;

    public LocalDate getWeekStart() {
        return week;
    }

    public LocalDate getWeekEnd() {
        return week.plusDays(7);
    }

    public Summary getAllData() {
        LinkedHashMap<LocalDate, LocalTime> all = new LinkedHashMap<>();
        for (LocalDate localDate : LocalDateIterator.inclusive(getWeekStart(), getWeekEnd())) {
            all.put(localDate, LocalTime.of(0, 0));
        }
        for (NetsuiteTimeData timeData : timeDataList) {
            for (Map.Entry<LocalDate, LocalTime> entry : timeData.getTimeMap().entrySet()) {
                LocalTime current = all.get(entry.getKey());
                LocalTime added = current.plus(entry.getValue().toNanoOfDay(), ChronoUnit.NANOS);
                all.put(entry.getKey(), added);
            }
        }
        return new Summary(getWeekStart(), getWeekEnd(), all);
    }

}
