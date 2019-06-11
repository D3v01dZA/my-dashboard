package com.altona.facade;

import com.altona.security.UserContext;
import com.altona.service.project.ProjectService;
import com.altona.service.project.model.Project;
import com.altona.service.time.TimeService;
import com.altona.service.time.model.control.BreakStart;
import com.altona.service.time.model.control.BreakStop;
import com.altona.service.time.model.control.TimeStatus;
import com.altona.service.time.model.control.WorkStart;
import com.altona.service.time.model.control.WorkStop;
import com.altona.service.time.model.summary.SummaryConfiguration;
import com.altona.service.time.model.summary.SummaryFailure;
import com.altona.service.time.model.summary.TimeSummary;
import com.altona.util.Result;
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
    public Optional<WorkStart> startWork(UserContext user, int projectId) {
        return projectService.project(user, projectId)
                .map(project -> {
                    List<Project> projects = projectService.projects(user);
                    return timeService.startProjectWork(projects, project, user);
                });
    }

    @Transactional
    public Optional<BreakStart> startBreak(UserContext user, int projectId) {
        return projectService.project(user, projectId)
                .map(project -> timeService.startProjectBreak(project, user));
    }

    @Transactional
    public Optional<WorkStop> endWork(UserContext user, int projectId) {
        return projectService.project(user, projectId)
                .map(project -> timeService.endProjectWork(project, user));
    }

    @Transactional
    public Optional<BreakStop> endBreak(UserContext user, int projectId) {
        return projectService.project(user, projectId)
                .map(project -> timeService.endProjectBreak(project, user));
    }

    @Transactional(readOnly = true)
    public TimeStatus timeStatus(UserContext user) {
        List<Project> projects = projectService.projects(user);
        return timeService.timeStatus(projects, user);
    }

    @Transactional(readOnly = true)
    public Optional<Result<TimeSummary, SummaryFailure>> summary(UserContext userContext, int projectId, SummaryConfiguration configuration) {
        return projectService.project(userContext, projectId)
                .map(project -> timeService.summary(userContext, project, configuration));
    }

}
