package com.altona.security;

import com.altona.service.time.util.TimeConfig;
import lombok.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class UserContext extends User implements TimeConfig, Encryptor {

    private TimeZone timeZone;
    private TextEncryptor encryptor;

    private Date now = new Date();

    UserContext(User user, @NonNull Authentication authentication, @NonNull TimeZone timeZone) {
        super(user.getId(), user.getUsername(), user.getPassword(), user.getSalt());
        this.timeZone = Objects.requireNonNull(timeZone);
        Object credentials = authentication.getCredentials();
        if (!(credentials instanceof String)) {
            throw new IllegalStateException("I'm supposed to have access to the credentials");
        }
        this.encryptor = Encryptors.delux((String) credentials, getSalt().replace("-", ""));
    }

    @Override
    public LocalDate today() {
        return localize(now).toLocalDate();
    }

    @Override
    public LocalDate firstDayOfWeek() {
        return today().with(DayOfWeek.MONDAY);
    }

    @Override
    public LocalDate firstDayOfMonth() {
        return today().withDayOfMonth(1);
    }

    @Override
    public LocalDateTime localize(Date date) {
        return date.toInstant().atZone(timeZone.toZoneId()).toLocalDateTime();
    }

    @Override
    public Date unlocalize(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(timeZone.toZoneId()).toInstant());
    }

    @Override
    public String encrypt(String original) {
        return encryptor.encrypt(original);
    }

    @Override
    public String decrypt(String original) {
        return encryptor.decrypt(original);
    }

}
