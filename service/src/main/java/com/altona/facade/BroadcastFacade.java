package com.altona.facade;

import com.altona.security.User;
import com.altona.service.broadcast.Broadcast;
import com.altona.service.broadcast.BroadcastService;
import com.altona.service.broadcast.BroadcastUpdate;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class BroadcastFacade {

    private BroadcastService broadcastService;

    @Transactional
    public Broadcast update(User user, BroadcastUpdate broadcastUpdate) {
        return broadcastService.update(user, broadcastUpdate);
    }

    @Transactional(readOnly = true)
    public List<Broadcast> broadcasts(User user) {
        return broadcastService.broadcasts(user);
    }

}
