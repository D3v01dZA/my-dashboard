package com.altona.html;

import com.altona.db.time.*;
import com.altona.db.time.control.BreakStart;
import com.altona.db.time.control.BreakStop;
import com.altona.db.time.control.WorkStart;
import com.altona.db.time.control.WorkStop;
import com.altona.db.time.summary.Summary;
import com.altona.db.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class TimeController {

    private UserService userService;
    private ProjectService projectService;
    private TimeService timeService;

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

    @Transactional
    @RequestMapping(path = "/time/project/{projectId}/start-work", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<WorkStart> startWork(Authentication authentication, @PathVariable Integer projectId) {
        return projectService.getProject(userService.getUser(authentication), projectId)
                .map(project -> timeService.startProjectWork(project))
                .map(workStart -> new ResponseEntity<>(workStart, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional
    @RequestMapping(path = "/time/project/{projectId}/start-break", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<BreakStart> startBreak(Authentication authentication, @PathVariable Integer projectId) {
        return projectService.getProject(userService.getUser(authentication), projectId)
                .map(project -> timeService.startProjectBreak(project))
                .map(breakStart -> new ResponseEntity<>(breakStart, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional
    @RequestMapping(path = "/time/project/{projectId}/end-work", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<WorkStop> endWork(Authentication authentication, @PathVariable Integer projectId) {
        return projectService.getProject(userService.getUser(authentication), projectId)
                .map(project -> timeService.endProjectWork(project))
                .map(workStop -> new ResponseEntity<>(workStop, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional
    @RequestMapping(path = "/time/project/{projectId}/end-break", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<BreakStop> endBreak(Authentication authentication, @PathVariable Integer projectId) {
        return projectService.getProject(userService.getUser(authentication), projectId)
                .map(project -> timeService.endProjectBreak(project))
                .map(breakStop -> new ResponseEntity<>(breakStop, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional
    @RequestMapping(path = "/time/project/{projectId}/summary", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Summary> getSummary(Authentication authentication, @PathVariable Integer projectId, @RequestParam Summary.Type type) {
        return projectService.getProject(userService.getUser(authentication), projectId)
                .map(project -> timeService.getSummary(project, type))
                .map(timeList -> new ResponseEntity<>(timeList, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @RequestMapping(path = "/time/project/{projectId}/time", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<Time>> getTime(Authentication authentication, @PathVariable Integer projectId) {
        return projectService.getProject(userService.getUser(authentication), projectId)
                .map(project -> timeService.getTimes(project))
                .map(timeList -> new ResponseEntity<>(timeList, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @RequestMapping(path = "/time/project/{projectId}/time/{timeId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Optional<Time>> getTime(Authentication authentication, @PathVariable Integer projectId, @PathVariable Integer timeId) {
        return projectService.getProject(userService.getUser(authentication), projectId)
                .map(project -> timeService.getTime(project, timeId))
                .map(timeList -> new ResponseEntity<>(timeList, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
