package com.altona.facade;

import com.altona.security.User;
import com.altona.service.broadcast.Broadcast;
import com.altona.service.broadcast.BroadcastDelete;
import com.altona.service.broadcast.BroadcastService;
import com.altona.service.broadcast.BroadcastUpdate;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BroadcastFacade {

    private BroadcastService broadcastService;

    @Transactional
    public Broadcast update(User user, BroadcastUpdate broadcastUpdate) {
        return broadcastService.update(user, broadcastUpdate);
    }

    @Transactional
    public Optional<Broadcast> delete(User user, BroadcastDelete broadcastDelete) {
        return broadcastService.delete(user, broadcastDelete);
    }

    @Transactional(readOnly = true)
    public Optional<Broadcast> broadcast(User user, int broadcastId) {
        return broadcastService.broadcast(user, broadcastId);
    }

    @Transactional(readOnly = true)
    public List<Broadcast> broadcasts(User user) {
        return broadcastService.broadcasts(user);
    }

}
