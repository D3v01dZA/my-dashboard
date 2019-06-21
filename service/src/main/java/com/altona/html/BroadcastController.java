package com.altona.html;

import com.altona.facade.BroadcastFacade;
import com.altona.security.User;
import com.altona.security.UserService;
import com.altona.service.broadcast.Broadcast;
import com.altona.service.broadcast.BroadcastDelete;
import com.altona.service.broadcast.BroadcastUpdate;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class BroadcastController {

    private UserService userService;
    private BroadcastFacade broadcastFacade;

    @RequestMapping(path = "/broadcast/update", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Broadcast> update(
            Authentication authentication,
            @RequestBody BroadcastUpdate broadcastUpdate
    ) {
        User user = userService.getUser(authentication);
        return new ResponseEntity<>(broadcastFacade.update(user, broadcastUpdate), HttpStatus.OK);
    }

    @RequestMapping(path = "/broadcast/delete", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Broadcast> delete(
            Authentication authentication,
            @RequestBody BroadcastDelete broadcastDelete
    ) {
        User user = userService.getUser(authentication);
        return broadcastFacade.delete(user, broadcastDelete)
                .map(broadcast -> new ResponseEntity<>(broadcast, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/broadcast", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<Broadcast>> get(
            Authentication authentication
    ) {
        User user = userService.getUser(authentication);
        return new ResponseEntity<>(broadcastFacade.broadcasts(user), HttpStatus.OK);
    }

    @RequestMapping(path = "/broadcast/{broadcastId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Broadcast> get(
            Authentication authentication,
            @PathVariable int broadcastId
    ) {
        User user = userService.getUser(authentication);
        return broadcastFacade.broadcast(user, broadcastId)
                .map(broadcast -> new ResponseEntity<>(broadcast, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


}
