package com.altona.broadcast;

import com.altona.broadcast.service.Broadcast;
import com.altona.broadcast.service.operation.BroadcastDelete;
import com.altona.broadcast.service.Broadcasts;
import com.altona.broadcast.service.UnsavedBroadcast;
import com.altona.security.User;
import com.altona.broadcast.service.operation.BroadcastUpdate;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BroadcastFacade {

    private Broadcasts broadcasts;
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    public Broadcast update(User user, BroadcastUpdate broadcastUpdate) {
        return broadcastUpdate.execute(broadcasts, jdbcTemplate, user);
    }

    @Transactional
    public Optional<UnsavedBroadcast> delete(User user, BroadcastDelete broadcastDelete) {
        return broadcastDelete.execute(broadcasts, user);
    }

    @Transactional(readOnly = true)
    public Optional<Broadcast> broadcast(User user, int id) {
        return broadcasts.broadcast(user, id);
    }

    @Transactional(readOnly = true)
    public List<Broadcast> broadcasts(User user) {
        return broadcasts.broadcasts(user);
    }

}
