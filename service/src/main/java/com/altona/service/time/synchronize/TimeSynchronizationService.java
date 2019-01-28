package com.altona.service.time.synchronize;

import com.altona.repository.db.time.project.Project;
import com.altona.repository.db.time.synchronization.Synchronization;
import com.altona.repository.db.time.synchronization.SynchronizationRepository;
import com.altona.security.Encryptor;
import com.altona.security.User;
import com.altona.security.UserContext;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TimeSynchronizationService {

    private ApplicationContext applicationContext;
    private SynchronizationRepository synchronizationRepository;

    public Synchronization createSynchronization(Encryptor encryptor, Project project, Synchronization synchronization) {
        int id = synchronizationRepository.createSynchronization(encryptor, project.getId(), synchronization);
        return synchronizationRepository.synchronization(encryptor, project.getId(), id).get();
    }

    public List<SynchronizationResult> synchronize(UserContext userContext, Project project) {
        return synchronizationRepository.synchronizations(userContext, project.getId()).stream()
                .map(synchronization -> synchronization.createService(applicationContext))
                .map(synchronizationServiceResult ->
                        synchronizationServiceResult.map(
                                synchronizationService -> synchronizationService.synchronize(userContext, project),
                                SynchronizationResult::failure
                        )
                )
                .collect(Collectors.toList());
    }

}
