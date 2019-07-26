package com.altona.service.synchronization;

import com.altona.broadcast.broadcaster.Broadcaster;
import com.altona.security.Encryptor;
import com.altona.security.UserContext;
import com.altona.broadcast.broadcaster.BroadcastMessage;
import com.altona.service.project.model.Project;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationAttemptBroadcast;
import com.altona.service.synchronization.model.SynchronizationCommand;
import com.altona.service.synchronization.model.SynchronizationError;
import com.altona.service.synchronization.model.SynchronizationRequest;
import com.altona.service.synchronization.model.SynchronizationTrace;
import com.altona.util.Result;
import com.altona.util.threading.TransactionalThreading;
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
    
    public Optional<SynchronizationAttempt> attempt(UserContext userContext, Project project, int synchronizationId, int attemptId) {
        return synchronizationRepository.select(userContext, project, synchronizationId)
                .flatMap(synchronization -> synchronizationAttemptRepository.select(userContext, synchronization, attemptId));
    }

    public Optional<List<SynchronizationTrace>> traces(UserContext userContext, Project project, int synchronizationId, int attemptId) {
        return attempt(userContext, project, synchronizationId, attemptId)
                .map(synchronizationAttempt -> synchronizationTraceRepository.traces(userContext, synchronizationAttempt));
    }

    public Optional<SynchronizationAttempt> synchronize(UserContext userContext, Project project, int synchronizationId, SynchronizationCommand command) {
        SynchronizationRequest request = new SynchronizationRequest(synchronizationId, userContext, project, command);
        return synchronizationRepository.select(userContext, project, synchronizationId)
                .map(synchronization -> createService(synchronization, request))
                .map(result -> synchronize(userContext, project, result));
    }

    public List<SynchronizationAttempt> synchronize(UserContext userContext, Project project) {
        return synchronizationRepository.select(userContext, project).stream()
                .map(synchronization -> createService(synchronization, new SynchronizationRequest(synchronization.getId(), userContext, project, SynchronizationCommand.current())))
                .map(result -> synchronize(userContext, project, result))
                .collect(Collectors.toList());
    }

    private Result<Synchronizer, SynchronizationError> createService(Synchronization synchronization, SynchronizationRequest request) {
        return synchronization.createService(applicationContext, request);
    }

    private SynchronizationAttempt synchronize(UserContext userContext, Project project, Result<Synchronizer, SynchronizationError> synchronizationResult) {
        return synchronizationResult.map(
                synchronizer -> {
                    SynchronizationAttempt pending = SynchronizationAttempt.pending(synchronizer);
                    SynchronizationAttempt inserted = synchronizationAttemptRepository.insert(userContext, synchronizer.getSynchronization(), pending);
                    synchronizeInBackground(userContext, synchronizer, inserted, project);
                    return inserted;
                },
                error -> {
                    SynchronizationAttempt failure = SynchronizationAttempt.failure(error);
                    return synchronizationAttemptRepository.insert(userContext, error.getSynchronization(), failure);
                }
        );
    }

    private void synchronizeInBackground(UserContext userContext, Synchronizer synchronizer, SynchronizationAttempt attempt, Project project) {
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
            synchronizationAttemptRepository.update(userContext, update);
            broadcaster.broadcast(userContext, BroadcastMessage.synchronization(SynchronizationAttemptBroadcast.of(update, synchronizer, project)));
        });
    }

}
