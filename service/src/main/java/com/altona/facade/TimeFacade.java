package com.altona.facade;

import com.altona.repository.db.time.project.Project;
import com.altona.repository.db.time.synchronization.Synchronization;
import com.altona.service.time.summary.SummaryConfiguration;
import com.altona.service.time.synchronize.SynchronizationResult;
import com.altona.service.time.ProjectService;
import com.altona.service.time.TimeService;
import com.altona.service.time.ZoneTime;
import com.altona.service.time.control.*;
import com.altona.service.time.summary.Summary;
import com.altona.security.User;
import com.altona.security.UserContext;
import com.altona.service.time.synchronize.TimeSynchronizationService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TimeFacade {
    
    private ProjectService projectService;
    private TimeService timeService;
    private TimeSynchronizationService timeSynchronizationService;

    // Projects

    @Transactional(readOnly = true)
    public List<Project> projects(User user) {
        return projectService.projects(user);
    }

    @Transactional(readOnly = true)
    public Optional<Project> project(User user, int projectId) {
        return projectService.project(user, projectId);
    }

    @Transactional
    public Project createProject(User user, Project project) {
        return projectService.createProject(user, project);
    }

    // Time

    @Transactional
    public Optional<WorkStart> startWork(User user, int projectId) {
        return projectService.project(user, projectId)
                .map(project -> {
                    List<Project> projects = projectService.projects(user);
                    return timeService.startProjectWork(projects, project);
                });
    }

    @Transactional
    public Optional<BreakStart> startBreak(User user, int projectId) {
        return projectService.project(user, projectId)
                .map(project -> timeService.startProjectBreak(project));
    }

    @Transactional
    public Optional<WorkStop> endWork(User user, int projectId) {
        return projectService.project(user, projectId)
                .map(project -> timeService.endProjectWork(project));
    }

    @Transactional
    public Optional<BreakStop> endBreak(User user, int projectId) {
        return projectService.project(user, projectId)
                .map(project -> timeService.endProjectBreak(project));
    }

    @Transactional(readOnly = true)
    public TimeStatus timeStatus(User user) {
        List<Project> projects = projectService.projects(user);
        return timeService.timeStatus(projects);
    }

    @Transactional(readOnly = true)
    public Optional<List<ZoneTime>> times(UserContext userContext, int projectId) {
        return projectService.project(userContext, projectId)
                .map(project -> timeService.zoneTimes(userContext, project));
    }

    @Transactional(readOnly = true)
    public Optional<ZoneTime> time(UserContext userContext, int projectId, int timeId) {
        return projectService.project(userContext, projectId)
                .flatMap(project -> timeService.zoneTime(userContext, project, timeId));
    }

    @Transactional(readOnly = true)
    public Optional<Summary> summary(UserContext userContext, int projectId, SummaryConfiguration configuration) {
        return projectService.project(userContext, projectId)
                .map(project -> timeService.summary(userContext, project, configuration));
    }

    // Synchronization

    @Transactional
    public Optional<Synchronization> createSynchronization(UserContext userContext, int projectId, Synchronization synchronization) {

        return projectService.project(userContext, projectId)
                .map(project -> timeSynchronizationService.createSynchronization(userContext, project, synchronization));
    }

    @Transactional(readOnly = true)
    public Optional<List<SynchronizationResult>> synchronize(UserContext userContext, int projectId) {
        return projectService.project(userContext, projectId)
                .map(project -> timeSynchronizationService.synchronize(userContext, project));
    }
    
}
