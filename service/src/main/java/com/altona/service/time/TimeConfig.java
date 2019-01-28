package com.altona.service.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public interface TimeConfig {

    LocalDate today();

    LocalDate firstDayOfWeek();

    LocalDateTime mapDateTime(Date date);

    Date mapLocalDate(LocalDate localDate);

}
