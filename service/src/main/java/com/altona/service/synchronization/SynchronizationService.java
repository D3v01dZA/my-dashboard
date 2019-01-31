package com.altona.service.synchronization;

import com.altona.service.project.model.Project;
import com.altona.security.Encryptor;
import com.altona.security.UserContext;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizeCommand;
import com.altona.service.synchronization.model.SynchronizeError;
import com.altona.service.synchronization.model.SynchronizeResult;
import com.altona.util.Result;
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
