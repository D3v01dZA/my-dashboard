package com.altona.service.synchronization.model;

import com.altona.context.EncryptionContext;
import com.altona.context.Encryptor;
import com.altona.project.Project;
import com.altona.user.UserContext;
import com.altona.context.TimeConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class SynchronizationRequest implements TimeConfig, Encryptor, EncryptionContext {

    @Getter
    private final int synchronizationId;
    @NonNull
    @Delegate(types = { TimeConfig.class, Encryptor.class, EncryptionContext.class })
    private final EncryptionContext user;
    @Getter
    @NonNull
    private final Project project;
    @NonNull
    private final SynchronizationCommand command;

    public int getPeriodsBack() {
        return command.getPeriodsBack();
    }

}
