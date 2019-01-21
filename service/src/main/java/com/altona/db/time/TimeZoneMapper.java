package com.altona.db.time;

import java.time.LocalDateTime;
import java.util.Date;

public interface TimeZoneMapper {

    LocalDateTime mapDateTime(Date date);

}
