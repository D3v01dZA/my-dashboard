package com.altona.service.synchronization.model;

import com.altona.service.synchronization.SynchronizeRequest;
import com.altona.service.synchronization.Synchronizer;
import com.altona.service.synchronization.maconomy.MaconomyRepository;
import com.altona.service.synchronization.maconomy.MaconomySynchronizer;
import com.altona.service.synchronization.maconomy.model.MaconomyConfiguration;
import com.altona.service.synchronization.netsuite.NetsuiteBrowser;
import com.altona.service.synchronization.netsuite.NetsuiteSynchronizer;
import com.altona.service.synchronization.netsuite.model.NetsuiteConfiguration;
import com.altona.service.synchronization.test.FailingSynchronizer;
import com.altona.service.synchronization.test.SucceedingSynchronizer;
import com.altona.service.time.TimeService;
import com.altona.util.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.function.Function;

public enum SynchronizationServiceType {
    MACONOMY {
        @Override
        public boolean hasValidConfiguration(ObjectMapper objectMapper, Synchronization synchronization) {
            return SynchronizationServiceType.checkConfiguration(objectMapper, synchronization, MaconomyConfiguration.class);
        }

        @Override
        public Result<Synchronizer, SynchronizeError> createService(ApplicationContext applicationContext, Synchronization synchronization, SynchronizeRequest request) {
            return SynchronizationServiceType.readJson(
                    applicationContext,
                    synchronization,
                    MaconomyConfiguration.class,
                    maconomyConfiguration ->
                            new MaconomySynchronizer(
                                    applicationContext.getBean(TimeService.class),
                                    applicationContext.getBean(MaconomyRepository.class),
                                    synchronization.getId(),
                                    request,
                                    maconomyConfiguration
                            )
            );
        }
    },
    NETSUITE {
        @Override
        public boolean hasValidConfiguration(ObjectMapper objectMapper, Synchronization synchronization) {
            return SynchronizationServiceType.checkConfiguration(objectMapper, synchronization, NetsuiteConfiguration.class);
        }

        @Override
        public Result<Synchronizer, SynchronizeError> createService(ApplicationContext applicationContext, Synchronization synchronization, SynchronizeRequest request) {
            return SynchronizationServiceType.readJson(
                    applicationContext,
                    synchronization,
                    NetsuiteConfiguration.class,
                    netsuiteConfiguration -> new NetsuiteSynchronizer(
                            applicationContext.getBean(TimeService.class),
                            applicationContext.getBean(NetsuiteBrowser.class),
                            synchronization.getId(),
                            request,
                            netsuiteConfiguration
                    )
            );
        }
    },
    SUCCEEDING {
        @Override
        public boolean hasValidConfiguration(ObjectMapper objectMapper, Synchronization synchronization) {
            return true;
        }

        @Override
        public Result<Synchronizer, SynchronizeError> createService(ApplicationContext applicationContext, Synchronization synchronization, SynchronizeRequest request) {
            return Result.success(new SucceedingSynchronizer(
                    applicationContext.getBean(TimeService.class),
                    synchronization.getId(),
                    request
            ));
        }
    },
    FAILING {
        @Override
        public boolean hasValidConfiguration(ObjectMapper objectMapper, Synchronization synchronization) {
            return true;
        }

        @Override
        public Result<Synchronizer, SynchronizeError> createService(ApplicationContext applicationContext, Synchronization synchronization, SynchronizeRequest request) {
            return Result.success(new FailingSynchronizer(synchronization.getId(), request));
        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizationServiceType.class);

    public abstract boolean hasValidConfiguration(ObjectMapper objectMapper, Synchronization synchronization);

    public abstract Result<Synchronizer, SynchronizeError> createService(ApplicationContext applicationContext, Synchronization synchronization, SynchronizeRequest request);

    private static <C> boolean checkConfiguration(ObjectMapper objectMapper, Synchronization synchronization, Class<C> configurationClazz) {
        try {
            if (synchronization.getConfiguration().isNull()) {
                LOGGER.warn("Invalid Configuration With Null Configuration");
                return false;
            }
            objectMapper.treeToValue(synchronization.getConfiguration(), configurationClazz);
            return true;
        } catch (JsonProcessingException e) {
            LOGGER.warn("Invalid Configuration", e);
            return false;
        }
    }

    private static <T extends Synchronizer, C> Result<T, SynchronizeError> readJson(
            ApplicationContext applicationContext,
            Synchronization synchronization,
            Class<C> configurationClazz,
            Function<C, T> synchronizerCreator
    ) {
        ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
        try {
            C configuration = objectMapper.treeToValue(synchronization.getConfiguration(), configurationClazz);
            return Result.success(synchronizerCreator.apply(configuration));
        } catch (IOException e) {
            LOGGER.warn("Invalid Configuration", e);
            return Result.error(new SynchronizeError(synchronization.getId(), "Could not read saved configuration"));
        }
    }

}
