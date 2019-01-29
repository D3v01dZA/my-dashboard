package com.altona.facade;

import com.altona.repository.db.time.project.Project;
import com.altona.security.User;
import com.altona.service.time.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProjectFacade {

    private ProjectService projectService;

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

}
