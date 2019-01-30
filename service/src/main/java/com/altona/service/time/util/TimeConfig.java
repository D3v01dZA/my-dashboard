package com.altona.service.time.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TimeConfig {

    LocalDate today();

    LocalDate firstDayOfWeek();

    LocalDateTime localize(Instant instant);

    Instant unlocalize(LocalDate localDate);

}
