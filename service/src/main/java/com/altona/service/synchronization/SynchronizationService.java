package com.altona.service.synchronization;

import com.altona.broadcast.broadcaster.BroadcastMessage;
import com.altona.broadcast.broadcaster.Broadcaster;
import com.altona.context.EncryptionContext;
import com.altona.project.Project;
import com.altona.context.Encryptor;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationAttemptBroadcast;
import com.altona.service.synchronization.model.SynchronizationCommand;
import com.altona.service.synchronization.model.SynchronizationError;
import com.altona.service.synchronization.model.SynchronizationRequest;
import com.altona.service.synchronization.model.SynchronizationTrace;
import com.altona.util.Result;
import com.altona.util.TransactionalThreading;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SynchronizationService {

    private Broadcaster broadcaster;
    private ObjectMapper objectMapper;
    private ApplicationContext applicationContext;
    private TransactionalThreading transactionalThreading;
    private SynchronizationRepository synchronizationRepository;
    private SynchronizationTraceRepository synchronizationTraceRepository;
    private SynchronizationAttemptRepository synchronizationAttemptRepository;

    public Result<Synchronization, String> createSynchronization(Encryptor encryptor, Project project, Synchronization synchronization) {
        if (!synchronization.hasValidConfiguration(objectMapper)) {
            return Result.failure("Invalid Configuration");
        }
        int id = synchronizationRepository.insert(encryptor, project, synchronization);
        return Result.success(synchronizationRepository.select(encryptor, project, id).get());
    }

    public List<Synchronization> getSynchronizations(Encryptor encryptor, Project project) {
        return synchronizationRepository.select(encryptor, project);
    }

    public Optional<Synchronization> getSynchronization(Encryptor encryptor, Project project, int synchronizationId) {
        return synchronizationRepository.select(encryptor, project, synchronizationId);
    }

    public Result<Synchronization, String> updateSynchronization(Encryptor encryptor, Project project, Synchronization original, ObjectNode modification) {
        Synchronization modified = original.modify(modification);
        if (!modified.hasValidConfiguration(objectMapper)) {
            return Result.failure("Invalid Configuration");
        }
        synchronizationRepository.update(encryptor, project, modified);
        return Result.success(synchronizationRepository.select(encryptor, project, modified.getId()).get());
    }
    
    public Optional<SynchronizationAttempt> attempt(EncryptionContext encryptionContext, Project project, int synchronizationId, int attemptId) {
        return synchronizationRepository.select(encryptionContext, project, synchronizationId)
                .flatMap(synchronization -> synchronizationAttemptRepository.select(encryptionContext, synchronization, attemptId));
    }

    public Optional<List<SynchronizationTrace>> traces(EncryptionContext encryptionContext, Project project, int synchronizationId, int attemptId) {
        return attempt(encryptionContext, project, synchronizationId, attemptId)
                .map(synchronizationAttempt -> synchronizationTraceRepository.traces(encryptionContext, synchronizationAttempt));
    }

    public Optional<SynchronizationAttempt> synchronize(EncryptionContext encryptionContext, Project project, int synchronizationId, SynchronizationCommand command) {
        SynchronizationRequest request = new SynchronizationRequest(synchronizationId, encryptionContext, project, command);
        return synchronizationRepository.select(encryptionContext, project, synchronizationId)
                .map(synchronization -> createService(synchronization, request))
                .map(result -> synchronize(encryptionContext, project, result));
    }

    public List<SynchronizationAttempt> synchronize(EncryptionContext encryptionContext, Project project) {
        return synchronizationRepository.select(encryptionContext, project).stream()
                .filter(Synchronization::isEnabled)
                .map(synchronization -> createService(synchronization, new SynchronizationRequest(synchronization.getId(), encryptionContext, project, SynchronizationCommand.current())))
                .map(result -> synchronize(encryptionContext, project, result))
                .collect(Collectors.toList());
    }

    private Result<Synchronizer, SynchronizationError> createService(Synchronization synchronization, SynchronizationRequest request) {
        return synchronization.createService(applicationContext, request);
    }

    private SynchronizationAttempt synchronize(EncryptionContext encryptionContext, Project project, Result<Synchronizer, SynchronizationError> synchronizationResult) {
        return synchronizationResult.map(
                synchronizer -> {
                    SynchronizationAttempt pending = SynchronizationAttempt.pending(synchronizer);
                    SynchronizationAttempt inserted = synchronizationAttemptRepository.insert(encryptionContext, synchronizer.getSynchronization(), pending);
                    synchronizeInBackground(encryptionContext, synchronizer, inserted, project);
                    return inserted;
                },
                error -> {
                    SynchronizationAttempt failure = SynchronizationAttempt.failure(error);
                    return synchronizationAttemptRepository.insert(encryptionContext, error.getSynchronization(), failure);
                }
        );
    }

    private void synchronizeInBackground(EncryptionContext encryptionContext, Synchronizer synchronizer, SynchronizationAttempt attempt, Project project) {
        transactionalThreading.executeInTransaction(() -> {
            SynchronizationAttempt update;
            try {
                Screenshot result = synchronizer.synchronize(attempt);
                log.info("Successfully synchronized {} {}", synchronizer.getSynchronization().getService(), synchronizer.getSynchronization().getId());
                update = attempt.succeeded(result);
            } catch (SynchronizationException ex) {
                update = attempt.failed(ex);
                log.info("Synchronization exception {} {}", synchronizer.getSynchronization().getService(), synchronizer.getSynchronization().getId(), ex);
            } catch (RuntimeException ex) {
                update = attempt.failed(ex);
                log.info("Runtime exception {} {}", synchronizer.getSynchronization().getService(), synchronizer.getSynchronization().getId(), ex);
            }
            synchronizationAttemptRepository.update(encryptionContext, update);
            broadcaster.broadcast(encryptionContext, BroadcastMessage.synchronization(SynchronizationAttemptBroadcast.of(update, synchronizer, project)));
        });
    }

}
