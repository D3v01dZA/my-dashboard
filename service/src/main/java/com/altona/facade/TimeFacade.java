package com.altona.facade;

import com.altona.context.facade.ContextFacade;
import com.altona.context.SqlContext;
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
import com.altona.service.time.model.summary.SummaryType;
import com.altona.service.time.model.summary.TimeSummary;
import com.altona.service.time.util.TimeInfo;
import com.altona.user.service.UserContext;
import com.altona.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@Service
public class TimeFacade extends ContextFacade {

    private ProjectService projectService;
    private TimeService timeService;

    @Autowired
    public TimeFacade(SqlContext sqlContext, TimeInfo timeInfo, ProjectService projectService, TimeService timeService) {
        super(sqlContext, timeInfo);
        this.projectService = projectService;
        this.timeService = timeService;
    }

    @Transactional
    public Optional<WorkStart> startWork(Authentication authentication, TimeZone timeZone, int projectId) {
        UserContext user = legacyAuthenticate(authentication, timeZone);
        return projectService.project(user, projectId)
                .map(project -> {
                    List<Project> projects = projectService.projects(user);
                    return timeService.startProjectWork(projects, project, user);
                });
    }

    @Transactional
    public Optional<BreakStart> startBreak(Authentication authentication, TimeZone timeZone, int projectId) {
        UserContext user = legacyAuthenticate(authentication, timeZone);
        return projectService.project(user, projectId)
                .map(project -> timeService.startProjectBreak(project, user));
    }

    @Transactional
    public Optional<WorkStop> endWork(Authentication authentication, TimeZone timeZone, int projectId) {
        UserContext user = legacyAuthenticate(authentication, timeZone);
        return projectService.project(user, projectId)
                .map(project -> timeService.endProjectWork(project, user));
    }

    @Transactional
    public Optional<BreakStop> endBreak(Authentication authentication, TimeZone timeZone, int projectId) {
        UserContext user = legacyAuthenticate(authentication, timeZone);
        return projectService.project(user, projectId)
                .map(project -> timeService.endProjectBreak(project, user));
    }

    @Transactional(readOnly = true)
    public TimeStatus timeStatus(Authentication authentication, TimeZone timeZone) {
        UserContext user = legacyAuthenticate(authentication, timeZone);
        List<Project> projects = projectService.projects(user);
        return timeService.timeStatus(projects, user);
    }

    @Transactional(readOnly = true)
    public Optional<Result<TimeSummary, SummaryFailure>> summary(Authentication authentication, TimeZone timeZone, int projectId, SummaryType summaryType) {
        UserContext user = legacyAuthenticate(authentication, timeZone);
        SummaryConfiguration configuration = summaryType.getConfiguration(user);
        return projectService.project(user, projectId)
                .map(project -> timeService.summary(user, project, configuration));
    }

}
