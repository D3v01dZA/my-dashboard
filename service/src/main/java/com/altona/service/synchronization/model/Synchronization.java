package com.altona.service.synchronization.model;

import com.altona.service.synchronization.Synchronizer;
import com.altona.util.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.context.ApplicationContext;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Synchronization {

    private int id;

    @NonNull
    private SynchronizationServiceType service;

    @NonNull
    private JsonNode configuration;

    public Synchronization(int id, String service, JsonNode configuration) {
        this(id, SynchronizationServiceType.valueOf(service), configuration);
    }

    public boolean hasValidConfiguration(ObjectMapper objectMapper) {
        return service.hasValidConfiguration(objectMapper, this);
    }

    public Result<Synchronizer, SynchronizeError> createService(ApplicationContext applicationContext) {
        return service.createService(applicationContext, this);
    }

}
