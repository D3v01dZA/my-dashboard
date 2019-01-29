package com.altona.facade;

import com.altona.repository.db.time.synchronization.Synchronization;
import com.altona.security.UserContext;
import com.altona.service.time.ProjectService;
import com.altona.service.time.synchronize.SynchronizeCommand;
import com.altona.service.time.synchronize.SynchronizeResult;
import com.altona.service.time.synchronize.SynchronizationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SynchronizationFacade {

    private ProjectService projectService;
    private SynchronizationService synchronizationService;

    @Transactional
    public Optional<Synchronization> createSynchronization(UserContext userContext, int projectId, Synchronization synchronization) {
        return projectService.project(userContext, projectId)
                .map(project -> synchronizationService.createSynchronization(userContext, project, synchronization));
    }

    @Transactional(readOnly = true)
    public Optional<List<SynchronizeResult>> synchronize(UserContext userContext, int projectId) {
        return projectService.project(userContext, projectId)
                .map(project -> synchronizationService.synchronize(userContext, project));
    }

    @Transactional(readOnly = true)
    public Optional<SynchronizeResult> synchronize(UserContext userContext, int projectId, int synchronizationId, SynchronizeCommand command) {
        return projectService.project(userContext, projectId)
                .flatMap(project -> synchronizationService.synchronize(userContext, project, synchronizationId, command));
    }

}
