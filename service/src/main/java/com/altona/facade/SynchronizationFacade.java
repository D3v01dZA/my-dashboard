package com.altona.facade;

import com.altona.service.synchronization.model.SynchronizationTrace;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.security.UserContext;
import com.altona.service.project.ProjectService;
import com.altona.service.synchronization.model.SynchronizeCommand;
import com.altona.service.synchronization.model.SynchronizeResult;
import com.altona.service.synchronization.SynchronizationService;
import com.altona.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    public Optional<Result<Synchronization, String>> createSynchronization(UserContext userContext, int projectId, Synchronization synchronization) {
        return projectService.project(userContext, projectId)
                .map(project -> synchronizationService.createSynchronization(userContext, project, synchronization));
    }

    @Transactional
    public Optional<Synchronization> getSynchronization(UserContext userContext, int projectId, int synchronizationId) {
        return projectService.project(userContext, projectId)
                .flatMap(project -> synchronizationService.getSynchronization(userContext, project, synchronizationId));
    }

    @Transactional
    public Optional<Result<Synchronization, String>> modifySynchronization(UserContext userContext, int projectId, int synchronizationId, ObjectNode modification) {
        return projectService.project(userContext, projectId)
                .flatMap(project -> synchronizationService.getSynchronization(userContext, project, synchronizationId)
                        .map(synchronization -> synchronizationService.updateSynchronization(userContext, project, synchronization, modification))
                );
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

    @Transactional(readOnly = true)
    public Optional<List<SynchronizationTrace>> traces(UserContext userContext, int projectId, int synchronizationId, String attemptId) {
        return projectService.project(userContext, projectId)
                .flatMap(project -> synchronizationService.traces(userContext, project, synchronizationId, attemptId));
    }

}
