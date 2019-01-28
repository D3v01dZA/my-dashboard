package com.altona.repository.db.time.synchronization;

import com.altona.service.time.synchronize.SynchronizationError;
import com.altona.service.time.synchronize.SynchronizationService;
import com.altona.util.functional.Result;
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

    public Result<SynchronizationService, SynchronizationError> createService(ApplicationContext applicationContext) {
        return service.createService(applicationContext, this);
    }

}