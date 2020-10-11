package com.altona.service.project;

import com.altona.user.service.User;
import com.altona.service.project.model.Project;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProjectService {

    private ProjectRepository projectRepository;

    public List<Project> projects(User user) {
        return projectRepository.select(user);
    }

    public Optional<Project> project(User user, int projectId) {
        return projectRepository.select(user, projectId);
    }

    public Project createProject(User user, Project project) {
        int key = projectRepository.insert(user, project);
        return project(user, key).get();
    }

    public Optional<Project> replaceProject(User user, int projectId, Project project) {
        Optional<Project> projectOptional = projectRepository.select(user, projectId);
        if (!projectOptional.isPresent()) {
            return Optional.empty();
        }
        projectRepository.update(user, projectId, project);
        return projectRepository.select(user, projectId);
    }

    public Optional<Project> deleteProject(User user, int projectId) {
        Optional<Project> project = projectRepository.select(user, projectId);
        project.ifPresent(value -> projectRepository.delete(user, value));
        return project;
    }

}
