package com.altona.db.time.summary;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class SummaryTime {

    private LocalDate date;
    private LocalTime time;

}
