package com.altona.html;

import com.altona.facade.TimeFacade;
import com.altona.security.UserContext;
import com.altona.security.UserService;
import com.altona.service.time.ZoneTime;
import com.altona.service.time.control.*;
import com.altona.service.time.summary.type.SummaryType;
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
    public ResponseEntity<Object> getSummary(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @RequestParam SummaryType type
    ) {
        UserContext userContext = userService.getUserContext(authentication, timeZone);
        return timeFacade.summary(userContext, projectId, type.getConfiguration(userContext))
                .map(summaryResult -> summaryResult.map(
                        summary -> new ResponseEntity<Object>(summary, HttpStatus.OK),
                        error -> new ResponseEntity<Object>(error, HttpStatus.CONFLICT)
                ))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
