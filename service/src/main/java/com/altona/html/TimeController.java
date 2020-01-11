package com.altona.html;

import com.altona.project.time.TimeFacade;
import com.altona.project.time.view.*;
import com.altona.project.time.SummaryType;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.TimeZone;

@RestController
@AllArgsConstructor
public class TimeController {

    private TimeFacade timeFacade;

    @RequestMapping(path = "/time/project/{projectId}/start-work", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<WorkStartView> startWork(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId
    ) {
        return timeFacade.startWork(authentication, timeZone, projectId)
                .map(workStart -> new ResponseEntity<>(workStart.asWorkStartView(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/start-break", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<BreakStartView> startBreak(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId
    ) {
        return timeFacade.startBreak(authentication, timeZone, projectId)
                .map(breakStart -> new ResponseEntity<>(breakStart.asBreakStartView(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/stop-work", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<WorkStopView> endWork(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId
    ) {
        return timeFacade.endWork(authentication, timeZone, projectId)
                .map(workStop -> new ResponseEntity<>(workStop.asWorkStopView(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/stop-break", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<BreakStopView> endBreak(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId
    ) {
        return timeFacade.endBreak(authentication, timeZone, projectId)
                .map(breakStop -> new ResponseEntity<>(breakStop.asBreakStopView(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/time-status", method = RequestMethod.POST, produces = "application/json")
    public TimeStatusView timeStatus(
            Authentication authentication,
            TimeZone timeZone
    ) {
        return timeFacade.currentTime(authentication, timeZone)
                .asTimeStatusView();
    }

    @RequestMapping(path = "/time/project/{projectId}/summary", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Object> getSummary(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @RequestParam SummaryType type
    ) {
        return timeFacade.summary(authentication, timeZone, projectId, type)
                .map(summaryResult -> summaryResult.map(
                        summary -> new ResponseEntity<Object>(summary, HttpStatus.OK),
                        error -> new ResponseEntity<Object>(error, HttpStatus.CONFLICT)
                ))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
