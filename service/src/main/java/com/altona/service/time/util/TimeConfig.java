package com.altona.service.time.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public interface TimeConfig {

    LocalDate today();

    LocalDate firstDayOfWeek();

    LocalDateTime localize(Date date);

    Date unlocalize(LocalDate localDate);

}
