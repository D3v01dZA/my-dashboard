package com.altona.html;

import com.altona.project.ProjectFacade;
import com.altona.project.Project;
import com.altona.project.UnsavedProject;
import com.altona.project.view.ProjectView;
import com.altona.project.view.UnsavedProjectView;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class ProjectController {

    private ProjectFacade projectFacade;

    @RequestMapping(path = "/time/project", method = RequestMethod.GET, produces = "application/json")
    public List<ProjectView> getProjects(
            Authentication authentication,
            TimeZone timeZone
    ) {
        return projectFacade.projects(authentication, timeZone).stream()
                .map(Project::asView)
                .collect(Collectors.toList());
    }

    @RequestMapping(path = "/time/project", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectView createProject(
            Authentication authentication,
            TimeZone timeZone,
            @RequestBody UnsavedProjectView unsavedProjectView
    ) {
        return projectFacade.createProject(authentication, timeZone, new UnsavedProject(unsavedProjectView.getName()))
                .asView();
    }

}
