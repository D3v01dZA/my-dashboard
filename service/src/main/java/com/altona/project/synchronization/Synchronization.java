package com.altona.project.synchronization;

import com.altona.context.EncryptionContext;
import com.altona.service.synchronization.SynchronizationService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;

public class Synchronization {

    @NonNull
    private EncryptionContext encryptionContext;

    private int id;

    private boolean enabled;

    @NonNull
    private SynchronizationService service;

    @NonNull
    private ObjectNode configuration;

}
