package com.altona.security;

import com.altona.service.time.TimeZoneMapper;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

public class UserContext extends User implements TimeZoneMapper {

    @NonNull
    private TimeZone timeZone;

    UserContext(User user, @NonNull TimeZone timeZone) {
        super(user.getId(), user.getUsername(), user.getPassword());
        if (timeZone == null) {
            throw new NullPointerException("username is marked @NonNull but is null");
        } else {
            this.timeZone = timeZone;
        }
    }

    @Override
    public LocalDateTime mapDateTime(Date date) {
        return date.toInstant().atZone(timeZone.toZoneId()).toLocalDateTime();
    }

    @Override
    public Date mapLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(timeZone.toZoneId()).toInstant());
    }

}
