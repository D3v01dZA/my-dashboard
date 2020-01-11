package com.altona.project.synchronization;

import com.altona.context.EncryptionContext;
import com.altona.service.synchronization.model.SynchronizationServiceType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class UnsavedSynchronization {

    private boolean enabled;

    @NonNull
    private SynchronizationServiceType service;

    @NonNull
    private ObjectNode configuration;

    public Synchronization save(EncryptionContext encryptionContext) {
        return
    }

}
