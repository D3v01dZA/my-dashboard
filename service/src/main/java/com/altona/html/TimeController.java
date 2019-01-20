package com.altona.html;

import com.altona.db.time.Project;
import com.altona.db.time.Time;
import com.altona.db.time.ProjectService;
import com.altona.db.time.TimeService;
import com.altona.db.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TimeController {

    private UserService userService;
    private ProjectService projectService;
    private TimeService timeService;

    @Autowired
    public TimeController(UserService userService, ProjectService projectService, TimeService timeService) {
        this.userService = userService;
        this.projectService = projectService;
        this.timeService = timeService;
    }

    @Transactional(readOnly = true)
    @RequestMapping(path = "/time/project", method = RequestMethod.GET, produces = "application/json")
    public List<Project> getProjects(Authentication authentication) {
        return projectService.getProjects(userService.getUser(authentication));
    }

    @Transactional(readOnly = true)
    @RequestMapping(path = "/time/project/{projectId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Project> getProject(Authentication authentication, @PathVariable Integer projectId) {
        return projectService.getProject(userService.getUser(authentication), projectId)
                .map(project -> new ResponseEntity<>(project, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional
    @RequestMapping(path = "/time/project", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public Project createProject(Authentication authentication, @RequestBody Project project) {
        return projectService.createProject(userService.getUser(authentication), project);
    }

    @Transactional(readOnly = true)
    @RequestMapping(path = "/time/project/{projectId}/time", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<Time>> getTime(Authentication authentication, @PathVariable Integer projectId) {
        return timeService.getTimes(userService.getUser(authentication), projectId)
                .map(timeList -> new ResponseEntity<>(timeList, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
