package com.altona.service.synchronization.model;

import com.altona.service.synchronization.Synchronizer;
import com.altona.service.synchronization.maconomy.MaconomyBrowser;
import com.altona.service.synchronization.maconomy.MaconomySynchronizer;
import com.altona.service.synchronization.maconomy.model.MaconomyConfiguration;
import com.altona.service.synchronization.netsuite.NetsuiteBrowser;
import com.altona.service.synchronization.netsuite.NetsuiteSynchronizer;
import com.altona.service.synchronization.netsuite.model.NetsuiteConfiguration;
import com.altona.service.synchronization.openair.OpenairBrowser;
import com.altona.service.synchronization.openair.OpenairSynchronizer;
import com.altona.service.synchronization.openair.model.OpenairConfiguration;
import com.altona.service.synchronization.test.FailingSynchronizer;
import com.altona.service.synchronization.test.SucceedingBrowser;
import com.altona.service.synchronization.test.SucceedingSynchronizer;
import com.altona.service.synchronization.test.model.SucceedingConfiguration;
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
        public Result<Synchronizer, SynchronizationError> createService(ApplicationContext applicationContext, Synchronization synchronization, SynchronizationRequest request) {
            return SynchronizationServiceType.readJson(
                    applicationContext,
                    synchronization,
                    MaconomyConfiguration.class,
                    maconomyConfiguration ->
                            new MaconomySynchronizer(
                                    applicationContext.getBean(TimeService.class),
                                    applicationContext.getBean(MaconomyBrowser.class),
                                    synchronization,
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
        public Result<Synchronizer, SynchronizationError> createService(ApplicationContext applicationContext, Synchronization synchronization, SynchronizationRequest request) {
            return SynchronizationServiceType.readJson(
                    applicationContext,
                    synchronization,
                    NetsuiteConfiguration.class,
                    netsuiteConfiguration -> new NetsuiteSynchronizer(
                            applicationContext.getBean(TimeService.class),
                            applicationContext.getBean(NetsuiteBrowser.class),
                            synchronization,
                            request,
                            netsuiteConfiguration
                    )
            );
        }
    },
    OPENAIR {
        @Override
        public boolean hasValidConfiguration(ObjectMapper objectMapper, Synchronization synchronization) {
            return SynchronizationServiceType.checkConfiguration(objectMapper, synchronization, OpenairConfiguration.class);
        }

        @Override
        public Result<Synchronizer, SynchronizationError> createService(ApplicationContext applicationContext, Synchronization synchronization, SynchronizationRequest request) {
            return SynchronizationServiceType.readJson(
                    applicationContext,
                    synchronization,
                    OpenairConfiguration.class,
                    openairConfiguration -> new OpenairSynchronizer(
                            applicationContext.getBean(TimeService.class),
                            applicationContext.getBean(OpenairBrowser.class),
                            synchronization,
                            request,
                            openairConfiguration
                    )
            );
        }
    },
    SUCCEEDING {
        @Override
        public boolean hasValidConfiguration(ObjectMapper objectMapper, Synchronization synchronization) {
            return SynchronizationServiceType.checkConfiguration(objectMapper, synchronization, SucceedingConfiguration.class);
        }

        @Override
        public Result<Synchronizer, SynchronizationError> createService(ApplicationContext applicationContext, Synchronization synchronization, SynchronizationRequest request) {
            return SynchronizationServiceType.readJson(
                    applicationContext,
                    synchronization,
                    SucceedingConfiguration.class,
                    succeedingConfiguration -> new SucceedingSynchronizer(
                            applicationContext.getBean(TimeService.class),
                            applicationContext.getBean(SucceedingBrowser.class),
                            synchronization,
                            request,
                            succeedingConfiguration
                    )
            );
        }
    },
    FAILING {
        @Override
        public boolean hasValidConfiguration(ObjectMapper objectMapper, Synchronization synchronization) {
            return true;
        }

        @Override
        public Result<Synchronizer, SynchronizationError> createService(ApplicationContext applicationContext, Synchronization synchronization, SynchronizationRequest request) {
            return Result.success(new FailingSynchronizer(synchronization));
        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizationServiceType.class);

    public abstract boolean hasValidConfiguration(ObjectMapper objectMapper, Synchronization synchronization);

    public abstract Result<Synchronizer, SynchronizationError> createService(ApplicationContext applicationContext, Synchronization synchronization, SynchronizationRequest request);

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

    private static <T extends Synchronizer, C> Result<T, SynchronizationError> readJson(
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
            return Result.failure(new SynchronizationError(synchronization, "Could not read saved configuration"));
        }
    }

}
