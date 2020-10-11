package com.altona.html;

import com.altona.facade.ProjectFacade;
import com.altona.service.project.model.Project;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class ProjectController {

    private ProjectFacade projectFacade;

    @RequestMapping(path = "/time/project", method = RequestMethod.GET, produces = "application/json")
    public List<Project> getProject(
            Authentication authentication
    ) {
        return projectFacade.projects(authentication);
    }

    @RequestMapping(path = "/time/project/{projectId}", method = RequestMethod.GET, produces = "application/json")
    public Optional<Project> getProject(
            Authentication authentication,
            @PathVariable int projectId
    ) {
        return projectFacade.project(authentication, projectId);
    }

    @RequestMapping(path = "/time/project", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Project createProject(
            Authentication authentication,
            @RequestBody Project project
    ) {
        return projectFacade.createProject(authentication, project);
    }

    @RequestMapping(path = "/time/project/{projectId}", method = RequestMethod.PUT, produces = "application/json")
    public Optional<Project> replaceProject(
            Authentication authentication,
            @PathVariable int projectId,
            @RequestBody Project project
    ) {
        return projectFacade.replaceProject(authentication, projectId, project);
    }

    @RequestMapping(path = "/time/project/{projectId}", method = RequestMethod.DELETE, produces = "application/json")
    public Optional<Project> deleteProject(
            Authentication authentication,
            @PathVariable int projectId
    ) {
        return projectFacade.deleteProject(authentication, projectId);
    }

}
