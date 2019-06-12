package com.altona.security;

import com.altona.service.time.util.TimeConfig;
import com.altona.service.time.util.TimeInfo;
import lombok.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import java.util.TimeZone;

public class UserContext extends User implements TimeConfig, Encryptor {

    private TimeZone timeZone;
    private TextEncryptor encryptor;

    private Instant now;

    UserContext(User user, @NonNull Authentication authentication, @NonNull TimeZone timeZone, @NonNull TimeInfo timeInfo) {
        super(user.getId(), user.getUsername(), user.getPassword(), user.getSalt());
        this.timeZone = Objects.requireNonNull(timeZone);
        Object credentials = authentication.getCredentials();
        if (!(credentials instanceof String)) {
            throw new IllegalStateException("I'm supposed to have access to the credentials");
        }
        this.encryptor = Encryptors.delux((String) credentials, getSalt().replace("-", ""));
        this.now = timeInfo.now();
    }

    @Override
    public Instant now() {
        return now;
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
    public LocalDate lastDayOfWeek() {
        return today().with(DayOfWeek.SUNDAY);
    }

    @Override
    public LocalDate firstDayOfMonth() {
        return today().with(TemporalAdjusters.firstDayOfMonth());
    }

    @Override
    public LocalDate lastDayOfMonth() {
        return today().with(TemporalAdjusters.lastDayOfMonth());
    }

    @Override
    public LocalDateTime localizedNow() {
        return localize(now);
    }

    @Override
    public LocalDateTime localize(Instant date) {
        return date.atZone(timeZone.toZoneId()).toLocalDateTime();
    }

    @Override
    public Instant unlocalize(LocalDate localDate) {
        return localDate.atStartOfDay(timeZone.toZoneId()).toInstant();
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
