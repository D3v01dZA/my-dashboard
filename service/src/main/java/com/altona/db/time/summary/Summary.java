package com.altona.db.time.summary;

import com.altona.db.time.Time;
import com.altona.db.time.ZoneTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Summary {

    private Map<LocalDate, LocalTime> timeMap;

    public static Summary create(List<ZoneTime> zoneTimes) {
        LocalDateTime now = LocalDateTime.now();
        Map<LocalDate, LocalTime> timeMap = new LinkedHashMap<>();
        for (ZoneTime zoneTime : zoneTimes) {
            LocalDateTime fromDateTime = zoneTime.getStart();
            LocalDateTime toDateTime = zoneTime.getEnd().orElse(now);
            LocalDate fromDate = fromDateTime.toLocalDate();
            LocalDate toDate = toDateTime.toLocalDate();
            if (fromDate.equals(toDate)) {
                addTime(timeMap, fromDate, fromDateTime, toDateTime, zoneTime.getType());
            } else {
                addTimes(timeMap, fromDate, toDate, fromDateTime, toDateTime, zoneTime.getType());
            }
        }
        return new Summary(timeMap);
    }

    private static void addTimes(Map<LocalDate, LocalTime> map, LocalDate lowerDate, LocalDate upperDate, LocalDateTime fromDate, LocalDateTime toDate, Time.Type type) {
        Assert.isTrue(fromDate.isBefore(toDate), "Somehow ended up with a from date less than the to date");
        // Add the first dates values
        addTime(map, lowerDate, fromDate, lowerDate.plusDays(1).atStartOfDay(), type);
        // Add the middle dates values
        LocalDate currentDate = lowerDate.plusDays(1);
        while (!currentDate.equals(upperDate)) {
            addTime(map, currentDate, lowerDate.atStartOfDay(), lowerDate.plusDays(1).atStartOfDay(), type);
            currentDate = currentDate.plusDays(1);
        }
        // Add the last dates values
        addTime(map, upperDate, upperDate.atStartOfDay(), toDate, type);
    }

    private static void addTime(Map<LocalDate, LocalTime> map, LocalDate onDate, LocalDateTime fromDate, LocalDateTime toDate, Time.Type type) {
        LocalTime difference = LocalTime.ofNanoOfDay(ChronoUnit.NANOS.between(fromDate, toDate));
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
