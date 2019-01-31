package com.altona.service.synchronization;

import com.altona.security.Encryptor;
import com.altona.security.UserContext;
import com.altona.service.project.model.Project;
import com.altona.service.synchronization.model.SynchronizeCommand;
import com.altona.service.time.util.TimeConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
public class SynchronizeRequest implements TimeConfig, Encryptor {

    @Getter
    private String attemptId = UUID.randomUUID().toString();
    @Getter
    @NonNull
    private int synchronizationId;
    @NonNull
    private UserContext user;
    @Getter
    @NonNull
    private Project project;
    @NonNull
    private SynchronizeCommand command;

    @Override
    public LocalDate today() {
        return user.today();
    }

    @Override
    public LocalDate firstDayOfWeek() {
        return user.firstDayOfWeek();
    }

    @Override
    public LocalDateTime localize(Date date) {
        return user.localize(date);
    }

    @Override
    public Date unlocalize(LocalDate localDate) {
        return user.unlocalize(localDate);
    }

    @Override
    public String encrypt(String original) {
        return user.encrypt(original);
    }

    @Override
    public String decrypt(String original) {
        return user.decrypt(original);
    }

    public int getPeriodsBack() {
        return command.getPeriodsBack();
    }

}
