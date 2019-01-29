package com.altona.service.time.summary;

import com.altona.repository.db.time.time.TimeType;
import com.altona.service.time.ZoneTime;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SummaryCreator {

    private static LocalTime NO_TIME = LocalTime.of(0, 0);

    public static Summary create(SummaryConfiguration configuration, List<ZoneTime> zoneTimes) {
        Map<LocalDate, LocalTime> timeMap = new LinkedHashMap<>();
        for (ZoneTime zoneTime : zoneTimes) {
            LocalDateTime fromDateTime = zoneTime.getStart();
            LocalDateTime toDateTime = zoneTime.getEnd().orElse(configuration.getLocalizedUserNow());
            LocalDate fromDate = fromDateTime.toLocalDate();
            LocalDate toDate = toDateTime.toLocalDate();
            if (fromDate.equals(toDate)) {
                addTime(timeMap, fromDate, fromDateTime, toDateTime, zoneTime.getType());
            } else {
                addTimes(timeMap, fromDate, toDate, fromDateTime, toDateTime, zoneTime.getType());
            }
        }

        LocalDate from = configuration.getFrom();
        LocalDate to = configuration.getTo();
        Predicate<LocalDate> between = betweenInclusive(from, to);
        LinkedHashMap<LocalDate, SummaryTime> summaryTimes = timeMap.entrySet().stream()
                .filter(entry -> between.test(entry.getKey()))
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> new SummaryTime(entry.getKey(), configuration.getRounding().round(entry.getValue())))
                .collect(Collectors.toMap(
                        SummaryTime::getDate,
                        Function.identity(),
                        (one, two) -> { throw new IllegalStateException("Should be impossible but " + one + " and " + two + " are identical"); },
                        LinkedHashMap::new
                ));
        return new Summary(from, to, summaryTimes);
    }


    private static void addTimes(Map<LocalDate, LocalTime> map, LocalDate lowerDate, LocalDate upperDate, LocalDateTime fromDate, LocalDateTime toDate, TimeType type) {
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

    private static void addTime(Map<LocalDate, LocalTime> map, LocalDate onDate, LocalDateTime fromDate, LocalDateTime toDate, TimeType type) {
        LocalTime difference = LocalTime.ofNanoOfDay(ChronoUnit.NANOS.between(fromDate, toDate));
        if (map.containsKey(onDate)) {
            map.put(onDate, type.add(map.get(onDate), difference));
        } else {
            map.put(onDate, type.add(NO_TIME, difference));
        }
    }

    private static Predicate<LocalDate> betweenInclusive(LocalDate from, LocalDate to) {
        return date -> date.isEqual(from) || date.isEqual(to) || (date.isAfter(from) && date.isBefore(to));
    }

}
