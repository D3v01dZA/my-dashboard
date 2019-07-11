package com.altona.broadcast;

import com.altona.broadcast.service.Broadcast;
import com.altona.broadcast.service.operation.BroadcastDelete;
import com.altona.broadcast.service.operation.BroadcastUpdate;
import com.altona.broadcast.service.view.BroadcastDeleteView;
import com.altona.broadcast.service.view.BroadcastUpdateView;
import com.altona.broadcast.service.view.BroadcastView;
import com.altona.broadcast.service.view.UnsavedBroadcastView;
import com.altona.security.User;
import com.altona.security.UserService;
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
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class BroadcastController {

    private UserService userService;
    private BroadcastFacade broadcastFacade;

    @RequestMapping(path = "/broadcast/update", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<BroadcastView> update(
            Authentication authentication,
            @RequestBody BroadcastUpdateView broadcastUpdate
    ) {
        User user = userService.getUser(authentication);
        return new ResponseEntity<>(
                broadcastFacade.update(
                        user,
                        new BroadcastUpdate(broadcastUpdate.getOldBroadcast(), broadcastUpdate.getNewBroadcast())
                ).asView(),
                HttpStatus.OK
        );
    }

    @RequestMapping(path = "/broadcast/delete", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<UnsavedBroadcastView> delete(
            Authentication authentication,
            @RequestBody BroadcastDeleteView broadcastDelete
    ) {
        User user = userService.getUser(authentication);
        return broadcastFacade.delete(user, new BroadcastDelete(broadcastDelete.getBroadcast()))
                .map(broadcast -> new ResponseEntity<>(broadcast.asView(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/broadcast", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<BroadcastView>> get(
            Authentication authentication
    ) {
        User user = userService.getUser(authentication);
        return new ResponseEntity<>(
                broadcastFacade.broadcasts(user).stream()
                        .map(Broadcast::asView)
                        .collect(Collectors.toList()),
                HttpStatus.OK
        );
    }

    @RequestMapping(path = "/broadcast/{broadcastId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<BroadcastView> get(
            Authentication authentication,
            @PathVariable int broadcastId
    ) {
        User user = userService.getUser(authentication);
        return broadcastFacade.broadcast(user, broadcastId)
                .map(broadcast -> new ResponseEntity<>(broadcast.asView(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


}
