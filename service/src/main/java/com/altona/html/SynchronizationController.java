package com.altona.html;

import com.altona.facade.SynchronizationFacade;
import com.altona.security.UserContext;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.security.UserService;
import com.altona.service.synchronization.model.SynchronizationTrace;
import com.altona.service.synchronization.model.SynchronizationCommand;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private SynchronizationFacade synchronizationFacade;

    @RequestMapping(path = "/time/project/{projectId}/synchronization", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Synchronization> createSynchronization(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @RequestBody Synchronization synchronization
    ) {
        return synchronizationFacade.createSynchronization(userService.getUserContext(authentication, timeZone), projectId, synchronization)
                .map(created -> created.map(
                        success -> new ResponseEntity<>(success, HttpStatus.CREATED),
                        failure -> new ResponseEntity<Synchronization>(HttpStatus.BAD_REQUEST)
                ))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/synchronization/{synchronizationId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<Synchronization>> getSynchronizations(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId
    ) {
        return synchronizationFacade.getSynchronizations(userService.getUserContext(authentication, timeZone), projectId)
                .map(found -> new ResponseEntity<>(found, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/synchronization/{synchronizationId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Synchronization> getSynchronization(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @PathVariable Integer synchronizationId
    ) {
        return synchronizationFacade.getSynchronization(userService.getUserContext(authentication, timeZone), projectId, synchronizationId)
                .map(found -> new ResponseEntity<>(found, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/synchronization/{synchronizationId}/configuration", method = RequestMethod.PATCH, produces = "application/json")
    public ResponseEntity<Synchronization> modifySynchronization(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @PathVariable Integer synchronizationId,
            @RequestBody ObjectNode modification
    ) {
        return synchronizationFacade.modifySynchronization(userService.getUserContext(authentication, timeZone), projectId, synchronizationId, modification)
                .map(modified -> modified.map(
                        success -> new ResponseEntity<>(success, HttpStatus.ACCEPTED),
                        failure -> new ResponseEntity<Synchronization>(HttpStatus.BAD_REQUEST)
                ))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/synchronize", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<List<SynchronizationAttempt>> synchronize(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId
    ) {
        return synchronizationFacade.synchronize(userService.getUserContext(authentication, timeZone), projectId)
                .map(synchronizeResult -> new ResponseEntity<>(synchronizeResult, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/synchronization/{synchronizationId}/synchronize", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<SynchronizationAttempt> synchronize(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @PathVariable Integer synchronizationId,
            @RequestParam(required = false) Integer periodsBack
    ) {
        SynchronizationCommand command = periodsBack == null ? SynchronizationCommand.current() : SynchronizationCommand.previous(periodsBack);
        return synchronizationFacade.synchronize(userService.getUserContext(authentication, timeZone), projectId, synchronizationId, command)
                .map(synchronizeResult -> new ResponseEntity<>(synchronizeResult, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/synchronization/{synchronizationId}/attempt/{attemptId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<SynchronizationAttempt> synchronizeAttempt(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @PathVariable Integer synchronizationId,
            @PathVariable Integer attemptId
    ) {
        UserContext userContext = userService.getUserContext(authentication, timeZone);
        return synchronizationFacade.attempt(userContext, projectId, synchronizationId, attemptId)
                .map(traces -> new ResponseEntity<>(traces, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/time/project/{projectId}/synchronization/{synchronizationId}/attempt/{attemptId}/trace", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<SynchronizationTrace>> synchronizeAttemptTrace(
            Authentication authentication,
            TimeZone timeZone,
            @PathVariable Integer projectId,
            @PathVariable Integer synchronizationId,
            @PathVariable Integer attemptId
    ) {
        UserContext userContext = userService.getUserContext(authentication, timeZone);
        return synchronizationFacade.traces(userContext, projectId, synchronizationId, attemptId)
                .map(traces -> new ResponseEntity<>(traces, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


}
