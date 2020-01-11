package com.altona.html;

import com.altona.broadcast.BroadcastFacade;
import com.altona.broadcast.Broadcast;
import com.altona.broadcast.operation.BroadcastDelete;
import com.altona.broadcast.operation.BroadcastUpdate;
import com.altona.broadcast.view.BroadcastDeleteView;
import com.altona.broadcast.view.BroadcastUpdateView;
import com.altona.broadcast.view.BroadcastView;
import com.altona.broadcast.view.UnsavedBroadcastView;
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

    private BroadcastFacade broadcastFacade;

    @RequestMapping(path = "/broadcast/update", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<BroadcastView> update(
            Authentication authentication,
            @RequestBody BroadcastUpdateView broadcastUpdate
    ) {
        return new ResponseEntity<>(
                broadcastFacade.update(
                        authentication,
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
        return broadcastFacade.delete(authentication, new BroadcastDelete(broadcastDelete.getBroadcast()))
                .map(broadcast -> new ResponseEntity<>(broadcast.asView(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/broadcast", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<BroadcastView>> get(
            Authentication authentication
    ) {
        return new ResponseEntity<>(
                broadcastFacade.broadcasts(authentication).stream()
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
        return broadcastFacade.broadcast(authentication, broadcastId)
                .map(broadcast -> new ResponseEntity<>(broadcast.asView(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


}
