package com.altona.project.synchronization.view;

import com.altona.service.synchronization.model.SynchronizationServiceType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class UnsavedSynchronizationView {

    @NonNull
    private Boolean enabled;

    @NonNull
    private SynchronizationServiceType service;

    @NonNull
    private ObjectNode configuration;

}
