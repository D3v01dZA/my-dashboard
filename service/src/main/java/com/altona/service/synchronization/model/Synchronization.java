package com.altona.service.synchronization.model;

import com.altona.service.synchronization.Synchronizer;
import com.altona.util.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.context.ApplicationContext;

import java.util.Iterator;
import java.util.Map;

@Getter
@AllArgsConstructor
public class Synchronization {

    private int id;

    @NonNull
    private SynchronizationServiceType service;

    @NonNull
    private ObjectNode configuration;

    public Synchronization(int id, String service, ObjectNode configuration) {
        this(id, SynchronizationServiceType.valueOf(service), configuration);
    }

    public boolean hasValidConfiguration(ObjectMapper objectMapper) {
        return service.hasValidConfiguration(objectMapper, this);
    }

    public Synchronization modify(ObjectNode parameters) {
        ObjectNode newConfiguration = configuration.deepCopy();
        Iterator<Map.Entry<String, JsonNode>> fields = parameters.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            newConfiguration.set(field.getKey(), field.getValue());
        }
        return new Synchronization(id, service, newConfiguration);
    }

    public Result<Synchronizer, SynchronizationError> createService(ApplicationContext applicationContext, SynchronizationRequest request) {
        return service.createService(applicationContext, this, request);
    }

}
