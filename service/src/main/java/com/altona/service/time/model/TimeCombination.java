package com.altona.service.time.model;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class TimeCombination {

    public static List<TimeCombination> createCombinations(List<Time> times) {
        ImmutableRangeMap.Builder<Instant, TimeCombination> builder = ImmutableRangeMap.builder();
        for (Time time : times) {
            if (time.getType() == TimeType.WORK) {
                Range<Instant> range = time.getEnd()
                        .map(end -> Range.closedOpen(time.getStart(), end))
                        .orElseGet(() -> Range.atLeast(time.getStart()));
                builder.put(range, new TimeCombination(time, new ArrayList<>()));
            }
        }
        ImmutableRangeMap<Instant, TimeCombination> map = builder.build();
        for (Time time : times) {
            if (time.getType() == TimeType.BREAK) {
                map.get(time.getStart()).breakTimes.add(time);
            }
        }
        return new ArrayList<>(map.asMapOfRanges().values());
    }

    private Time workTime;
    private List<Time> breakTimes;

    public boolean isStopped() {
        return workTime.getEnd().isPresent();
    }

    public Instant getStart() {
        return workTime.getStart();
    }

    public Instant getEnd(Instant now) {
        return workTime.getEnd().orElse(now);
    }

    public LocalTime time(Instant now) {
        LocalTime time = workTime.time(now);
        for (Time breakTime: breakTimes) {
            time = time.minus(ChronoUnit.NANOS.between(breakTime.getStart(), breakTime.getEnd().orElse(now)), ChronoUnit.NANOS);
        }
        return time;
    }

}
