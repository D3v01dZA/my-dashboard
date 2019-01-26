package com.altona.service.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public interface TimeZoneMapper {

    LocalDateTime mapDateTime(Date date);

    Date mapLocalDate(LocalDate localDate);

}
