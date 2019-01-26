package com.altona.html;

import com.altona.db.time.project.Project;
import com.altona.service.time.*;
import com.altona.service.time.control.*;
import com.altona.service.time.summary.Summary;
import com.altona.security.UserService;
import com.altona.facade.TimeFacade;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.TimeZone;

@RestController
@AllArgsConstructor
public class TimeController {

    private UserService userService;
    private TimeFacade timeFacade;

    @Transactional(readOnly = true)
    @RequestMapping(path = "/time/project", method = RequestMethod.GET, produces = "application/json")
    public List<Project> getProjects(
            Authentication authentication
    ) {
        return timeFacade.projects(userService.getUser(authentication));
    }

    @Transactional(readOnly = true)
    @RequestMapping(path = "/time/project/{projectId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Project> getProject(
            Authentication authentication,
            @PathVariable Integer projectId
    ) {
        return timeFacade.project(userService.getUser(authentication), projectId)
                .map(project -> new ResponseEntity<>(project, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional
    @RequestMapping(path = "/time/project", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public Project createProject(
            Authentication authentication,
            @RequestBody Project project
    ) {
        return timeFacade.createProject(userService.getUser(authentication), project);
    }

    @Transactional
    @RequestMapping(path = "/time/project/{projectId}/start-work", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<WorkStart> startWork(
            Authentication authentication,
            @PathVariable Integer projectId
    ) {
        return timeFacade.startWork(userService.getUser(authentication), projectId)
                .map(workStart -> new ResponseEntity<>(workStart, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional
    @RequestMapping(path = "/time/project/{projectId}/start-break", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<BreakStart> startBreak(
            Authentication authentication,
            @PathVariable Integer projectId
    ) {
        return timeFacade.startBreak(userService.getUser(authentication), projectId)
                .map(breakStart -> new ResponseEntity<>(breakStart, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional
    @RequestMapping(path = "/time/project/{projectId}/stop-work", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<WorkStop> endWork(
            Authentication authentication,
            @PathVariable Integer projectId
    ) {
        return timeFacade.endWork(userService.getUser(authentication), projectId)
                .map(workStop -> new ResponseEntity<>(workStop, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional
    @RequestMapping(path = "/time/project/{projectId}/stop-break", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<BreakStop> endBreak(
            Authentication authentication,
            @PathVariable Integer projectId
    ) {
        return timeFacade.endBreak(userService.getUser(authentication), projectId)
                .map(breakStop -> new ResponseEntity<>(breakStop, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @RequestMapping(path = "/time/project/{projectId}/time-status", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<TimeStatus> timeStatus(
            Authentication authentication,
            @PathVariable Integer projectId
    ) {
        return timeFacade.timeStatus(userService.getUser(authentication), projectId)
                .map(breakStop -> new ResponseEntity<>(breakStop, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional
    @RequestMapping(path = "/time/project/{projectId}/summary", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Summary> getSummary(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @RequestParam Summary.Type type
    ) {
        return timeFacade.summary(userService.getUserContext(authentication, timeZone), projectId, type)
                .map(timeList -> new ResponseEntity<>(timeList, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @RequestMapping(path = "/time/project/{projectId}/time", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<ZoneTime>> getTime(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId
    ) {
        return timeFacade.times(userService.getUserContext(authentication, timeZone), projectId)
                .map(timeList -> new ResponseEntity<>(timeList, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @RequestMapping(path = "/time/project/{projectId}/time/{timeId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<ZoneTime> getTime(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @PathVariable Integer timeId
    ) {
        return timeFacade.time(userService.getUserContext(authentication, timeZone), projectId, timeId)
                .map(timeList -> new ResponseEntity<>(timeList, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
