package com.altona.facade;

import com.altona.context.facade.ContextFacade;
import com.altona.context.SqlContext;
import com.altona.service.project.ProjectService;
import com.altona.service.project.model.Project;
import com.altona.service.synchronization.SynchronizationService;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationCommand;
import com.altona.service.synchronization.model.SynchronizationTrace;
import com.altona.service.time.util.TimeInfo;
import com.altona.user.service.UserContext;
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

    private ProjectService projectService;
    private SynchronizationService synchronizationService;

    public SynchronizationFacade(SqlContext sqlContext, TimeInfo timeInfo, ProjectService projectService, SynchronizationService synchronizationService) {
        super(sqlContext, timeInfo);
        this.projectService = projectService;
        this.synchronizationService = synchronizationService;
    }

    @Transactional
    public Optional<Result<Synchronization, String>> createSynchronization(Authentication authentication, TimeZone timeZone, int projectId, Synchronization synchronization) {
        UserContext userContext = legacyAuthenticate(authentication, timeZone);
        return projectService.project(userContext, projectId)
                .map(project -> synchronizationService.createSynchronization(userContext, project, synchronization));
    }

    @Transactional(readOnly = true)
    public Optional<List<Synchronization>> getSynchronizations(Authentication authentication, TimeZone timeZone, int projectId) {
        UserContext userContext = legacyAuthenticate(authentication, timeZone);
        return projectService.project(userContext, projectId)
                .map(project -> synchronizationService.getSynchronizations(userContext, project));
    }

    @Transactional(readOnly = true)
    public Optional<Synchronization> getSynchronization(Authentication authentication, TimeZone timeZone, int projectId, int synchronizationId) {
        UserContext userContext = legacyAuthenticate(authentication, timeZone);
        return projectService.project(userContext, projectId)
                .flatMap(project -> synchronizationService.getSynchronization(userContext, project, synchronizationId));
    }

    @Transactional
    public Optional<Synchronization> deleteSynchronization(Authentication authentication, TimeZone timeZone, int projectId, int synchronizationId) {
        UserContext userContext = legacyAuthenticate(authentication, timeZone);
        return projectService.project(userContext, projectId)
                .flatMap(project -> synchronizationService.deleteSynchronization(userContext, project, synchronizationId));
    }

    @Transactional
    public Result<Optional<Synchronization>, String> replaceSynchronization(Authentication authentication, TimeZone timeZone, int projectId, int synchronizationId, Synchronization synchronization) {
        UserContext userContext = legacyAuthenticate(authentication, timeZone);
        Optional<Project> projectOptional = projectService.project(userContext, projectId);
        if (!projectOptional.isPresent()) {
            return Result.success(Optional.empty());
        } else {
            Project project = projectOptional.get();
            return synchronizationService.replaceSynchronization(userContext, project, synchronizationId, synchronization);
        }
    }

    @Transactional
    public Optional<Result<Synchronization, String>> modifySynchronization(Authentication authentication, TimeZone timeZone, int projectId, int synchronizationId, ObjectNode modification) {
        UserContext userContext = legacyAuthenticate(authentication, timeZone);
        return projectService.project(userContext, projectId)
                .flatMap(project -> synchronizationService.getSynchronization(userContext, project, synchronizationId)
                        .map(synchronization -> synchronizationService.updateSynchronization(userContext, project, synchronization, modification))
                );
    }

    @Transactional
    public Optional<List<SynchronizationAttempt>> synchronize(Authentication authentication, TimeZone timeZone, int projectId) {
        UserContext userContext = legacyAuthenticate(authentication, timeZone);
        return projectService.project(userContext, projectId)
                .map(project -> synchronizationService.synchronize(userContext, project));
    }

    @Transactional
    public Optional<SynchronizationAttempt> synchronize(Authentication authentication, TimeZone timeZone, int projectId, int synchronizationId, SynchronizationCommand command) {
        UserContext userContext = legacyAuthenticate(authentication, timeZone);
        return projectService.project(userContext, projectId)
                .flatMap(project -> synchronizationService.synchronize(userContext, project, synchronizationId, command));
    }

    @Transactional(readOnly = true)
    public Optional<SynchronizationAttempt> attempt(Authentication authentication, TimeZone timeZone, int projectId, int synchronizationId, int attemptId) {
        UserContext userContext = legacyAuthenticate(authentication, timeZone);
        return projectService.project(userContext, projectId)
                .flatMap(project -> synchronizationService.attempt(userContext, project, synchronizationId, attemptId));
    }

    @Transactional(readOnly = true)
    public Optional<List<SynchronizationTrace>> traces(Authentication authentication, TimeZone timeZone, int projectId, int synchronizationId, int attemptId) {
        UserContext userContext = legacyAuthenticate(authentication, timeZone);
        return projectService.project(userContext, projectId)
                .flatMap(project -> synchronizationService.traces(userContext, project, synchronizationId, attemptId));
    }

}
