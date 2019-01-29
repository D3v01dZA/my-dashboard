package com.altona.service.time.synchronize;

import com.altona.repository.db.time.project.Project;
import com.altona.repository.db.time.synchronization.Synchronization;
import com.altona.repository.db.time.synchronization.SynchronizationRepository;
import com.altona.security.Encryptor;
import com.altona.security.UserContext;
import com.altona.util.functional.Result;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SynchronizationService {

    private ApplicationContext applicationContext;
    private SynchronizationRepository synchronizationRepository;

    public Synchronization createSynchronization(Encryptor encryptor, Project project, Synchronization synchronization) {
        int id = synchronizationRepository.createSynchronization(encryptor, project.getId(), synchronization);
        return synchronizationRepository.synchronization(encryptor, project.getId(), id).get();
    }

    public Optional<SynchronizeResult> synchronize(UserContext userContext, Project project, int synchronizationId, SynchronizeCommand command) {
        return synchronizationRepository.synchronization(userContext, project.getId(), synchronizationId)
                .map(this::createService)
                .map(synchronizationResult -> synchronize(userContext, project, command, synchronizationResult));
    }

    public List<SynchronizeResult> synchronize(UserContext userContext, Project project) {
        return synchronizationRepository.synchronizations(userContext, project.getId()).stream()
                .map(this::createService)
                .map(synchronizationServiceResult -> synchronize(userContext, project, SynchronizeCommand.current(), synchronizationServiceResult))
                .collect(Collectors.toList());
    }

    private Result<Synchronizer, SynchronizeError> createService(Synchronization synchronization) {
        return synchronization.createService(applicationContext);
    }

    private SynchronizeResult synchronize(UserContext userContext, Project project, SynchronizeCommand command, Result<Synchronizer, SynchronizeError> synchronizationResult) {
        return synchronizationResult.map(
                synchronizationService -> synchronizationService.synchronize(userContext, project, command),
                SynchronizeResult::failure
        );
    }

}
