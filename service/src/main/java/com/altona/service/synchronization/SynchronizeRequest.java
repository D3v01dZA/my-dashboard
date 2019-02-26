package com.altona.service.synchronization;

import com.altona.security.Encryptor;
import com.altona.security.UserContext;
import com.altona.service.project.model.Project;
import com.altona.service.synchronization.model.SynchronizeCommand;
import com.altona.service.time.util.TimeConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.util.UUID;

@RequiredArgsConstructor
public class SynchronizeRequest implements TimeConfig, Encryptor {

    @Getter
    private String attemptId = UUID.randomUUID().toString();
    @Getter
    @NonNull
    private int synchronizationId;
    @NonNull
    @Delegate(types = { TimeConfig.class, Encryptor.class })
    private UserContext user;
    @Getter
    @NonNull
    private Project project;
    @NonNull
    private SynchronizeCommand command;

    public int getPeriodsBack() {
        return command.getPeriodsBack();
    }

}
