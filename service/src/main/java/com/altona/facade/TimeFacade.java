package com.altona.facade;

import com.altona.repository.db.time.project.Project;
import com.altona.security.User;
import com.altona.security.UserContext;
import com.altona.service.time.ProjectService;
import com.altona.service.time.TimeService;
import com.altona.service.time.ZoneTime;
import com.altona.service.time.control.*;
import com.altona.service.time.summary.Summary;
import com.altona.service.time.summary.SummaryConfiguration;
import com.altona.util.functional.Result;
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
    public Optional<Result<Summary, String>> summary(UserContext userContext, int projectId, SummaryConfiguration configuration) {
        return projectService.project(userContext, projectId)
                .map(project -> timeService.summary(userContext, project, configuration));
    }

}
