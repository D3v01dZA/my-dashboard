package com.altona.service.synchronization;

import com.altona.context.EncryptionContext;
import com.altona.context.SqlContext;
import com.altona.context.facade.ContextFacade;
import com.altona.project.query.ProjectById;
import com.altona.project.synchronization.UnsavedSynchronization;
import com.altona.service.synchronization.SynchronizationService;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationCommand;
import com.altona.service.synchronization.model.SynchronizationTrace;
import com.altona.context.TimeInfo;
import com.altona.util.Result;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@Service
public class SynchronizationFacade extends ContextFacade {

    private SynchronizationService synchronizationService;

    public SynchronizationFacade(SqlContext sqlContext, TimeInfo timeInfo, SynchronizationService synchronizationService) {
        super(sqlContext, timeInfo);
        this.synchronizationService = synchronizationService;
    }

    @Transactional
    public Optional<Result<Synchronization, String>> createSynchronization(Authentication authentication, TimeZone timeZone, int projectId, UnsavedSynchronization unsavedSynchronization) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new ProjectById(encryptionContext, projectId).execute()
                .map(project -> unsavedSynchronization.save);
    }

    @Transactional(readOnly = true)
    public Optional<List<Synchronization>> getSynchronizations(Authentication authentication, TimeZone timeZone, int projectId) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new ProjectById(encryptionContext, projectId).execute()
                .map(project -> synchronizationService.getSynchronizations(encryptionContext, project));
    }

    @Transactional(readOnly = true)
    public Optional<Synchronization> getSynchronization(Authentication authentication, TimeZone timeZone, int projectId, int synchronizationId) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new ProjectById(encryptionContext, projectId).execute()
                .flatMap(project -> synchronizationService.getSynchronization(encryptionContext, project, synchronizationId));
    }

    @Transactional
    public Optional<Result<Synchronization, String>> modifySynchronization(Authentication authentication, TimeZone timeZone, int projectId, int synchronizationId, ObjectNode modification) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new ProjectById(encryptionContext, projectId).execute()
                .flatMap(project -> synchronizationService.getSynchronization(encryptionContext, project, synchronizationId)
                        .map(synchronization -> synchronizationService.updateSynchronization(encryptionContext, project, synchronization, modification))
                );
    }

    @Transactional
    public Optional<List<SynchronizationAttempt>> synchronize(Authentication authentication, TimeZone timeZone, int projectId) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new ProjectById(encryptionContext, projectId).execute()
                .map(project -> synchronizationService.synchronize(encryptionContext, project));
    }

    @Transactional
    public Optional<SynchronizationAttempt> synchronize(Authentication authentication, TimeZone timeZone, int projectId, int synchronizationId, SynchronizationCommand command) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new ProjectById(encryptionContext, projectId).execute()
                .flatMap(project -> synchronizationService.synchronize(encryptionContext, project, synchronizationId, command));
    }

    @Transactional(readOnly = true)
    public Optional<SynchronizationAttempt> attempt(Authentication authentication, TimeZone timeZone, int projectId, int synchronizationId, int attemptId) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new ProjectById(encryptionContext, projectId).execute()
                .flatMap(project -> synchronizationService.attempt(encryptionContext, project, synchronizationId, attemptId));
    }

    @Transactional(readOnly = true)
    public Optional<List<SynchronizationTrace>> traces(Authentication authentication, TimeZone timeZone, int projectId, int synchronizationId, int attemptId) {
        EncryptionContext encryptionContext = authenticate(authentication, timeZone);
        return new ProjectById(encryptionContext, projectId).execute()
                .flatMap(project -> synchronizationService.traces(encryptionContext, project, synchronizationId, attemptId));
    }

}
