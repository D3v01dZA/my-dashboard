package com.altona.service.time.model.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class SummaryTime {

    private LocalDate date;
    private LocalTime time;

}
