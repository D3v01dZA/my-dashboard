package com.altona.service.time;

import com.altona.repository.time.project.Project;
import com.altona.repository.time.project.ProjectRepository;
import com.altona.security.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProjectService {

    private ProjectRepository projectRepository;

    public List<Project> projects(User user) {
        return projectRepository.projects(user);
    }

    public Optional<Project> project(User user, int projectId) {
        return projectRepository.project(user, projectId);
    }

    public Project createProject(User user, Project project) {
        int key = projectRepository.createProject(user, project);
        return project(user, key).get();
    }

}
