package com.altona.service.time.summary.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class SummaryDates {

    private LocalDate from;
    private LocalDate to;

}
