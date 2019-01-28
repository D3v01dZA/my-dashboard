package com.altona.html;

import com.altona.facade.TimeFacade;
import com.altona.repository.db.time.project.Project;
import com.altona.repository.db.time.synchronization.Synchronization;
import com.altona.security.UserService;
import com.altona.service.time.ZoneTime;
import com.altona.service.time.control.*;
import com.altona.service.time.summary.Summary;
import com.altona.service.time.summary.type.SummaryType;
import com.altona.service.time.synchronize.SynchronizationCommand;
import com.altona.service.time.synchronize.SynchronizationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.TimeZone;

@RestController
@AllArgsConstructor
public class TimeController {

    private UserService userService;
    private TimeFacade timeFacade;
    private ObjectMapper objectMapper;

    // Project

    @RequestMapping(path = "/time/project", method = RequestMethod.GET, produces = "application/json")
    public List<Project> getProjects(
            Authentication authentication
    ) {
        return timeFacade.projects(userService.getUser(authentication));
    }

    @RequestMapping(path = "/time/project/{projectId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Project> getProject(
            Authentication authentication,
            @PathVariable Integer projectId
    ) {
        return timeFacade.project(userService.getUser(authentication), projectId)
                .map(project -> new ResponseEntity<>(project, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Project createProject(
            Authentication authentication,
            @RequestBody Project project
    ) {
        return timeFacade.createProject(userService.getUser(authentication), project);
    }

    // Time

    @RequestMapping(path = "/time/project/{projectId}/start-work", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<WorkStart> startWork(
            Authentication authentication,
            @PathVariable Integer projectId
    ) {
        return timeFacade.startWork(userService.getUser(authentication), projectId)
                .map(workStart -> new ResponseEntity<>(workStart, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/start-break", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<BreakStart> startBreak(
            Authentication authentication,
            @PathVariable Integer projectId
    ) {
        return timeFacade.startBreak(userService.getUser(authentication), projectId)
                .map(breakStart -> new ResponseEntity<>(breakStart, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/stop-work", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<WorkStop> endWork(
            Authentication authentication,
            @PathVariable Integer projectId
    ) {
        return timeFacade.endWork(userService.getUser(authentication), projectId)
                .map(workStop -> new ResponseEntity<>(workStop, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/stop-break", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<BreakStop> endBreak(
            Authentication authentication,
            @PathVariable Integer projectId
    ) {
        return timeFacade.endBreak(userService.getUser(authentication), projectId)
                .map(breakStop -> new ResponseEntity<>(breakStop, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/time-status", method = RequestMethod.POST, produces = "application/json")
    public TimeStatus timeStatus(
            Authentication authentication
    ) {
        return timeFacade.timeStatus(userService.getUser(authentication));
    }

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

    @RequestMapping(path = "/time/project/{projectId}/summary", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Summary> getSummary(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @RequestParam SummaryType type
    ) {
        return timeFacade.summary(userService.getUserContext(authentication, timeZone), projectId, type.getConfiguration())
                .map(timeList -> new ResponseEntity<>(timeList, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Synchronization

    @RequestMapping(path = "/time/project/{projectId}/synchronization/{synchronizationId}/synchronize", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<SynchronizationResult> synchronize(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @PathVariable Integer synchronizationId,
            @RequestParam(required = false) Integer periodsBack
    ) {
        SynchronizationCommand command = periodsBack == null ? SynchronizationCommand.current() : SynchronizationCommand.previous(periodsBack);
        return timeFacade.synchronize(userService.getUserContext(authentication, timeZone), projectId, synchronizationId, command)
                .map(synchronizeResult -> new ResponseEntity<>(synchronizeResult, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/synchronization", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Synchronization> createSynchronization(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @RequestBody Synchronization synchronization
    ) {
        if (!synchronization.hasValidConfiguration(objectMapper)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return timeFacade.createSynchronization(userService.getUserContext(authentication, timeZone), projectId, synchronization)
                .map(created -> new ResponseEntity<>(created, HttpStatus.CREATED))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @RequestMapping(path = "/time/project/{projectId}/synchronize", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<List<SynchronizationResult>> synchronize(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId
    ) {
        return timeFacade.synchronize(userService.getUserContext(authentication, timeZone), projectId)
                .map(synchronizeResult -> new ResponseEntity<>(synchronizeResult, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
