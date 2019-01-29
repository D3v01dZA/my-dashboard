package com.altona.html;

import com.altona.facade.ProjectFacade;
import com.altona.service.project.model.Project;
import com.altona.security.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class ProjectController {

    private UserService userService;
    private ProjectFacade projectFacade;

    @RequestMapping(path = "/time/project", method = RequestMethod.GET, produces = "application/json")
    public List<Project> getProjects(
            Authentication authentication
    ) {
        return projectFacade.projects(userService.getUser(authentication));
    }

    @RequestMapping(path = "/time/project", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Project createProject(
            Authentication authentication,
            @RequestBody Project project
    ) {
        return projectFacade.createProject(userService.getUser(authentication), project);
    }

}
