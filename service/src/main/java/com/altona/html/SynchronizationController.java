package com.altona.html;

import com.altona.facade.SynchronizationFacade;
import com.altona.security.UserContext;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.security.UserService;
import com.altona.service.synchronization.model.SynchronizationTrace;
import com.altona.service.synchronization.model.SynchronizeCommand;
import com.altona.service.synchronization.model.SynchronizeResult;
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
public class SynchronizationController {

    private UserService userService;
    private ObjectMapper objectMapper;
    private SynchronizationFacade synchronizationFacade;

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
        return synchronizationFacade.createSynchronization(userService.getUserContext(authentication, timeZone), projectId, synchronization)
                .map(created -> new ResponseEntity<>(created, HttpStatus.CREATED))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @RequestMapping(path = "/time/project/{projectId}/synchronize", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<List<SynchronizeResult>> synchronize(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId
    ) {
        return synchronizationFacade.synchronize(userService.getUserContext(authentication, timeZone), projectId)
                .map(synchronizeResult -> new ResponseEntity<>(synchronizeResult, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/synchronization/{synchronizationId}/synchronize", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<SynchronizeResult> synchronize(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @PathVariable Integer synchronizationId,
            @RequestParam(required = false) Integer periodsBack
    ) {
        SynchronizeCommand command = periodsBack == null ? SynchronizeCommand.current() : SynchronizeCommand.previous(periodsBack);
        return synchronizationFacade.synchronize(userService.getUserContext(authentication, timeZone), projectId, synchronizationId, command)
                .map(synchronizeResult -> new ResponseEntity<>(synchronizeResult, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/synchronization/{synchronizationId}/trace/{attemptId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<SynchronizationTrace>> synchronizeTrace(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @PathVariable Integer synchronizationId,
            @PathVariable String attemptId
    ) {
        UserContext userContext = userService.getUserContext(authentication, timeZone);
        return synchronizationFacade.traces(userContext, projectId, synchronizationId, attemptId)
                .map(traces -> new ResponseEntity<>(traces, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


}
