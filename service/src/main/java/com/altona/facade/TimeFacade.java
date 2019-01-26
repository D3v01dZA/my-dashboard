package com.altona.facade;

import com.altona.db.time.project.Project;
import com.altona.service.time.ProjectService;
import com.altona.service.time.TimeService;
import com.altona.service.time.ZoneTime;
import com.altona.service.time.control.*;
import com.altona.service.time.summary.Summary;
import com.altona.security.User;
import com.altona.security.UserContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TimeFacade {
    
    private ProjectService projectService;
    private TimeService timeService;

    public List<Project> projects(User user) {
        return projectService.projects(user);
    }
    
    public Optional<Project> project(User user, int projectId) {
        return projectService.project(user, projectId);
    }
    
    public Project createProject(User user, Project project) {
        return projectService.createProject(user, project);
    }

    public Optional<WorkStart> startWork(User user, int projectId) {
        return projectService.project(user, projectId)
                .map(project -> timeService.startProjectWork(project));
    }
    
    public Optional<BreakStart> startBreak(User user, int projectId) {
        return projectService.project(user, projectId)
                .map(project -> timeService.startProjectBreak(project));
    }

    public Optional<WorkStop> endWork(User user, int projectId) {
        return projectService.project(user, projectId)
                .map(project -> timeService.endProjectWork(project));
    }
    
    public Optional<BreakStop> endBreak(User user, int projectId) {
        return projectService.project(user, projectId)
                .map(project -> timeService.endProjectBreak(project));
    }

    public Optional<TimeStatus> timeStatus(User user, int projectId) {
        return projectService.project(user, projectId)
                .map(project -> timeService.timeStatus(project));
    }

    public Optional<Summary> summary(UserContext userContext, int projectId, Summary.Type type) {
        return projectService.project(userContext, projectId)
                .map(project -> timeService.summary(userContext, project, type));
    }

    public Optional<List<ZoneTime>> times(UserContext userContext, int projectId) {
        return projectService.project(userContext, projectId)
                .map(project -> timeService.zoneTimes(userContext, project));
    }
    
    public Optional<ZoneTime> time(UserContext userContext, int projectId, int timeId) {
        return projectService.project(userContext, projectId)
                .flatMap(project -> timeService.zoneTime(userContext, project, timeId));
    }
    
}
