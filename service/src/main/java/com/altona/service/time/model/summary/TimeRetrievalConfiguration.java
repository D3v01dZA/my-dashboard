package com.altona.service.time.model.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
@AllArgsConstructor
public class TimeRetrievalConfiguration {

    @NonNull
    private LocalDate from;

    @NonNull
    private LocalDate to;

}
