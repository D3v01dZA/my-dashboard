package com.altona.service.synchronization.model;

import com.altona.service.synchronization.maconomy.model.MaconomyConfiguration;
import com.altona.service.synchronization.maconomy.MaconomyRepository;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.time.TimeService;
import com.altona.service.synchronization.maconomy.MaconomySynchronizer;
import com.altona.util.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

public enum SynchronizationServiceType {
    MACONOMY {
        @Override
        public boolean hasValidConfiguration(ObjectMapper objectMapper, Synchronization synchronization) {
            try {
                if (synchronization.getConfiguration().isNull()) {
                    LOGGER.warn("Invalid Configuration With Null Configuration");
                    return false;
                }
                objectMapper.treeToValue(synchronization.getConfiguration(), MaconomyConfiguration.class);
                return true;
            } catch (JsonProcessingException e) {
                LOGGER.warn("Invalid Configuration", e);
                return false;
            }
        }

        @Override
        public Result<Synchronizer, SynchronizeError> createService(ApplicationContext applicationContext, Synchronization synchronization) {
            ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
            try {
                MaconomyConfiguration maconomyConfiguration = objectMapper.treeToValue(synchronization.getConfiguration(), MaconomyConfiguration.class);
                return Result.success(
                        new MaconomySynchronizer(
                                applicationContext.getBean(TimeService.class),
                                applicationContext.getBean(MaconomyRepository.class),
                                synchronization.getId(),
                                maconomyConfiguration
                        )
                );
            } catch (IOException e) {
                LOGGER.warn("Invalid Configuration", e);
                return Result.error(new SynchronizeError(synchronization.getId(), "Could not read saved maconomy configuration"));
            }
        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizationServiceType.class);

    public abstract boolean hasValidConfiguration(ObjectMapper objectMapper, Synchronization synchronization);

    public abstract Result<Synchronizer, SynchronizeError> createService(ApplicationContext applicationContext, Synchronization synchronization);

}
