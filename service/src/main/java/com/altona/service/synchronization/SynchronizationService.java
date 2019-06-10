package com.altona.service.synchronization;

import com.altona.security.Encryptor;
import com.altona.security.UserContext;
import com.altona.service.project.model.Project;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationTrace;
import com.altona.service.synchronization.model.SynchronizeCommand;
import com.altona.service.synchronization.model.SynchronizeError;
import com.altona.service.synchronization.model.SynchronizeResult;
import com.altona.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SynchronizationService {

    private ObjectMapper objectMapper;
    private ApplicationContext applicationContext;
    private SynchronizationRepository synchronizationRepository;
    private SynchronizationTraceRepository synchronizationTraceRepository;

    public Result<Synchronization, String> createSynchronization(Encryptor encryptor, Project project, Synchronization synchronization) {
        if (!synchronization.hasValidConfiguration(objectMapper)) {
            return Result.failure("Invalid Configuration");
        }
        int id = synchronizationRepository.createSynchronization(encryptor, project.getId(), synchronization);
        return Result.success(synchronizationRepository.synchronization(encryptor, project.getId(), id).get());
    }

    public Optional<Synchronization> getSynchronization(Encryptor encryptor, Project project, int synchronizationId) {
        return synchronizationRepository.synchronization(encryptor, project.getId(), synchronizationId);
    }

    public Result<Synchronization, String> updateSynchronization(Encryptor encryptor, Project project, Synchronization original, ObjectNode modification) {
        Synchronization modified = original.modify(modification);
        if (!modified.hasValidConfiguration(objectMapper)) {
            return Result.failure("Invalid Configuration");
        }
        synchronizationRepository.updateSynchronization(encryptor, project.getId(), modified);
        return Result.success(synchronizationRepository.synchronization(encryptor, project.getId(), modified.getId()).get());
    }

    public Optional<List<SynchronizationTrace>> traces(UserContext userContext, Project project, int synchronizationId, String attemptId) {
        return synchronizationRepository.synchronization(userContext, project.getId(), synchronizationId)
                .map(synchronization -> synchronizationTraceRepository.traces(userContext, project.getId(), synchronizationId, attemptId));
    }

    public Optional<SynchronizeResult> synchronize(UserContext userContext, Project project, int synchronizationId, SynchronizeCommand command) {
        SynchronizeRequest request = new SynchronizeRequest(synchronizationId, userContext, project, command);
        return synchronizationRepository.synchronization(userContext, project.getId(), synchronizationId)
                .map(synchronization -> createService(synchronization, request))
                .map(this::synchronize);
    }

    public List<SynchronizeResult> synchronize(UserContext userContext, Project project) {
        return synchronizationRepository.synchronizations(userContext, project.getId()).stream()
                .map(synchronization -> createService(synchronization, new SynchronizeRequest(synchronization.getId(), userContext, project, SynchronizeCommand.current())))
                .map(this::synchronize)
                .collect(Collectors.toList());
    }

    private Result<Synchronizer, SynchronizeError> createService(Synchronization synchronization, SynchronizeRequest request) {
        return synchronization.createService(applicationContext, request);
    }

    private SynchronizeResult synchronize(Result<Synchronizer, SynchronizeError> synchronizationResult) {
        return synchronizationResult.map(
                Synchronizer::synchronize,
                SynchronizeResult::failure
        );
    }

}
