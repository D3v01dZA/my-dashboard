package com.altona.service.broadcast;

import com.altona.security.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BroadcastService {

    private BroadcastRepository broadcastRepository;

    public Broadcast update(User user, BroadcastUpdate broadcastUpdate) {
        broadcastUpdate.getOldBroadcast().ifPresent(broadcastId -> broadcastRepository.delete(user, broadcastId));
        int id = broadcastRepository.insert(user, broadcastUpdate.getNewBroadcast());
        return broadcastRepository.select(user, id).get();
    }

    public List<Broadcast> broadcasts(User user) {
        return broadcastRepository.select(user);
    }

    public void broadcast(User user) {

    }

}
