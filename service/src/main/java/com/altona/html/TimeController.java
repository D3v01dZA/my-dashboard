package com.altona.html;

import com.altona.facade.TimeFacade;
import com.altona.service.time.model.LocalizedTime;
import com.altona.service.time.model.Time;
import com.altona.service.time.model.control.BreakStart;
import com.altona.service.time.model.control.BreakStop;
import com.altona.service.time.model.control.TimeStatus;
import com.altona.service.time.model.control.WorkStart;
import com.altona.service.time.model.control.WorkStop;
import com.altona.service.time.model.summary.SummaryType;
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

    private TimeFacade timeFacade;

    @RequestMapping(path = "/time/project/{projectId}/start-work", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<WorkStart> startWork(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId
    ) {
        return timeFacade.startWork(authentication, timeZone, projectId)
                .map(workStart -> new ResponseEntity<>(workStart, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/start-break", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<BreakStart> startBreak(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId
    ) {
        return timeFacade.startBreak(authentication, timeZone, projectId)
                .map(breakStart -> new ResponseEntity<>(breakStart, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/stop-work", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<WorkStop> endWork(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId
    ) {
        return timeFacade.endWork(authentication, timeZone, projectId)
                .map(workStop -> new ResponseEntity<>(workStop, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/stop-break", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<BreakStop> endBreak(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId
    ) {
        return timeFacade.endBreak(authentication, timeZone, projectId)
                .map(breakStop -> new ResponseEntity<>(breakStop, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/time-status", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<TimeStatus> timeStatus(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable int projectId
    ) {
        return timeFacade.timeStatus(authentication, timeZone, projectId)
                .map(workStop -> new ResponseEntity<>(workStop, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
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

    @RequestMapping(path = "/time/project/{projectId}/time/{timeId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<LocalizedTime> getTime(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @PathVariable Integer timeId
    ) {
        return timeFacade.time(authentication, timeZone, projectId, timeId)
                .map(times -> new ResponseEntity<>(times, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/time/{timeId}", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<Object> replaceTime(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @PathVariable Integer timeId,
            @RequestBody LocalizedTime time
    ) {
        return timeFacade.replaceTime(authentication, timeZone, projectId, timeId, time)
                .map(
                        optionalLocalizedTime -> optionalLocalizedTime
                                .map(localizedTime -> new ResponseEntity<Object>(localizedTime, HttpStatus.OK))
                                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND)),
                        failure -> new ResponseEntity<>(failure, HttpStatus.BAD_REQUEST)
                );
    }

    @RequestMapping(path = "/time/project/{projectId}/time/{timeId}", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<LocalizedTime> deleteTime(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @PathVariable Integer timeId
    ) {
        return timeFacade.deleteTime(authentication, timeZone, projectId, timeId)
                .map(times -> new ResponseEntity<>(times, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/time", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<LocalizedTime>> getTimes(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @RequestParam SummaryType type
    ) {
        return timeFacade.times(authentication, timeZone, projectId, type)
                .map(times -> new ResponseEntity<>(times, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
