package com.altona.service.time.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public interface TimeConfig extends TimeInfo {

    LocalDate today();

    LocalDate firstDayOfWeek();

    LocalDate lastDayOfWeek();

    LocalDate firstDayOfMonth();

    LocalDate lastDayOfMonth();

    LocalDateTime localizedNow();

    LocalDateTime localize(Date date);

    Date unlocalize(LocalDate localDate);

}
