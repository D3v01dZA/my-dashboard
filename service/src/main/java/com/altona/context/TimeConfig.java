package com.altona.context;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TimeConfig extends TimeInfo {

    LocalDate today();

    LocalDate firstDayOfWeek();

    LocalDate lastDayOfWeek();

    LocalDate firstDayOfMonth();

    LocalDate lastDayOfMonth();

    LocalDateTime localizedNow();

    LocalDateTime localize(Instant date);

    Instant unlocalize(LocalDate localDate);

}
