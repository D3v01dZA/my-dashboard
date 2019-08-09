package com.altona.service.synchronization.model;

import com.altona.security.Encryptor;
import com.altona.user.service.UserContext;
import com.altona.service.project.model.Project;
import com.altona.service.time.util.TimeConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class SynchronizationRequest implements TimeConfig, Encryptor {

    @Getter
    private final int synchronizationId;
    @NonNull
    @Delegate(types = { TimeConfig.class, Encryptor.class })
    private final UserContext user;
    @Getter
    @NonNull
    private final Project project;
    @NonNull
    private final SynchronizationCommand command;

    public int getPeriodsBack() {
        return command.getPeriodsBack();
    }

}
