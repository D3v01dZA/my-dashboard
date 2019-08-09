package com.altona.facade;

import com.altona.context.facade.ContextFacade;
import com.altona.context.SqlContext;
import com.altona.service.project.ProjectService;
import com.altona.service.project.model.Project;
import com.altona.service.time.util.TimeInfo;
import com.altona.user.service.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectFacade extends ContextFacade {

    private ProjectService projectService;

    public ProjectFacade(SqlContext sqlContext, TimeInfo timeInfo, ProjectService projectService) {
        super(sqlContext, timeInfo);
        this.projectService = projectService;
    }

    @Transactional(readOnly = true)
    public List<Project> projects(Authentication authentication) {
        User user = legacyAuthenticate(authentication);
        return projectService.projects(user);
    }

    @Transactional
    public Project createProject(Authentication authentication, Project project) {
        User user = legacyAuthenticate(authentication);
        return projectService.createProject(user, project);
    }

}
