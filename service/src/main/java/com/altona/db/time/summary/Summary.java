package com.altona.db.time.summary;

import com.altona.db.time.Time;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Summary {

    private Map<LocalDate, LocalTime> timeMap;

    public static Summary create(List<Time> times) {
        Date now = new Date();
        Map<LocalDate, LocalTime> timeMap = new HashMap<>();
        for (Time time : times) {
            Date toDateTime = time.getEnd().orElse(now);
            LocalDate fromDate = LocalDate.from(time.getStart().toInstant());
            LocalDate toDate = LocalDate.from(toDateTime.toInstant());
            if (fromDate.equals(toDate)) {
                addTime(timeMap, fromDate, time.getStart(), toDateTime, time.getType());
            } else {

            }
        }
        return new Summary(timeMap);
    }

    private static void addTime(Map<LocalDate, LocalTime> map, LocalDate onDate, Date fromDate, Date toDate, Time.Type type) {
        LocalTime difference = LocalTime.ofNanoOfDay(ChronoUnit.NANOS.between(fromDate.toInstant(), toDate.toInstant()));
        if (map.containsKey(onDate)) {
            map.put(onDate, type.add(map.get(onDate), difference));
        } else {
            map.put(onDate, difference);
        }
    }

    public enum Type {
        DAY {
            @Override
            public Dates getDates() {
                LocalDate now = LocalDate.now();
                return new Dates(now, now);
            }
        },
        WEEK {
            @Override
            public Dates getDates() {
                LocalDate now = LocalDate.now();
                return new Dates(now.minusDays(7), now);
            }
        };

        public abstract Dates getDates();
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Dates {

        private LocalDate from;
        private LocalDate to;

    }
}
