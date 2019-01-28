package com.altona.repository.db.time.synchronization;

import com.altona.repository.integration.maconomy.MaconomyConfiguration;
import com.altona.repository.integration.maconomy.MaconomyRepository;
import com.altona.service.time.TimeService;
import com.altona.service.time.synchronize.SynchronizationError;
import com.altona.service.time.synchronize.SynchronizationService;
import com.altona.service.time.synchronize.maconomy.MaconomyService;
import com.altona.util.functional.Result;
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
        public Result<SynchronizationService, SynchronizationError> createService(ApplicationContext applicationContext, Synchronization synchronization) {
            ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
            try {
                MaconomyConfiguration maconomyConfiguration = objectMapper.treeToValue(synchronization.getConfiguration(), MaconomyConfiguration.class);
                return Result.success(
                        new MaconomyService(
                                applicationContext.getBean(TimeService.class),
                                applicationContext.getBean(MaconomyRepository.class),
                                synchronization.getId(),
                                maconomyConfiguration
                        )
                );
            } catch (IOException e) {
                LOGGER.warn("Invalid Configuration", e);
                return Result.error(new SynchronizationError(synchronization.getId(), "Could not read saved maconomy configuration"));
            }
        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizationServiceType.class);

    public abstract boolean hasValidConfiguration(ObjectMapper objectMapper, Synchronization synchronization);

    public abstract Result<SynchronizationService, SynchronizationError> createService(ApplicationContext applicationContext, Synchronization synchronization);

}
